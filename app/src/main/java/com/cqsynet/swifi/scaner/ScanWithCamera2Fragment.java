/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：使用Camera2 API的图形扫描Fragment (Android5.0及以后的版本)
 *
 *
 * 创建标识：zhaosiyang 20170516
 */
package com.cqsynet.swifi.scaner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.LotteryListActivity;
import com.cqsynet.swifi.model.ARLotteryResponseObject;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.LotteryDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class ScanWithCamera2Fragment extends Fragment implements
        View.OnTouchListener, View.OnClickListener, IScanFragment {

    private static final String TAG = "ScanerActivity";

    private static final int MSG_SHOW_RESULT = 0;
    private static final int MSG_IDENTIFY_IMAGE = 1;
    private static final int MSG_GIF_BOX_FINISH = 2;
    private static final int MSG_GIF_FIRE_FINISH = 3;

    private static final int MAX_IDENTIFY_COUNT = 20;
    private static final int IDENTIFY_SUCCESS_LEVEL = 97;

    private static final int STATE_PREVIEW = 0;  //显示预览
    //    private static final int STATE_WAITING_LOCK = 1; //等待对焦成功(拍照片前将预览锁上保证图像不在变化)
//    private static final int STATE_WAITING_PRECAPTURE = 2; //等待预拍照(对焦, 曝光等操作)
//    private static final int STATE_WAITING_NON_PRECAPTURE = 3; //等待非预拍照(闪光灯等操作)
//    private static final int STATE_PICTURE_TAKEN = 4; //已经获取照片
    private int mState = STATE_PREVIEW; //当前的相机状态, 这里初始化为预览, 因为刚载入这个fragment时应显示预览
    private static final int MAX_PREVIEW_WIDTH = 1920; //最大预览宽度
    private static final int MAX_PREVIEW_HEIGHT = 1080; //最大预览高度
    private String mCameraId; //0为后摄像头,1为前摄像头
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private HandlerThread mBackgroundThread; //后台线程,防止阻塞ui,处理拍照等工作
    private Handler mBackgroundHandler; //后台线程的handler
    private ImageReader mImageReader;
    private CaptureRequest.Builder mPreviewRequestBuilder; //预览请求构建器, 用来构建"预览请求"(下面定义的)通过pipeline发送到Camera device
    private CaptureRequest mPreviewRequest; //预览请求, 由上面的构建器构建出来
    private Semaphore mCameraOpenCloseLock = new Semaphore(1); //信号量控制器, 防止相机没有关闭时退出本应用(若没有关闭就退出, 会造成其他应用无法调用相机).当某处获得这个许可时,其他需要许可才能执行的代码需要等待许可被释放才能获取
    private boolean mFlashSupported; //是否支持闪光的
    private int mSensorOrientation;// 获取相机传感器的方向("自然"状态下垂直放置为0, 顺时针算起, 每次加90读).这个参数是由设备的生产商来决定的,大多数情况下,该值为90
    private long mPassiveFocusTime = 0l; //触发PassiveFocus状态的时间
    private float mFingerSpacing = 0; //触摸点之间的距离
    private int mZoomLevel = 1; //屏幕缩放等级
    private boolean isFlash = false; // 闪光灯是否开启

    private IImageIdentify mImageIdentify;
    private ARLotteryResponseObject.ARLotteryResponseBody mPrize;
    private boolean mIsScan = true; // 是否可以扫描
    private int mIdentifyCount;
    private boolean mIsGiftAnimationCompleted = true; // 礼物动画是否完成

    private Activity mContext;
    private AutoFitTextureView mTextureView;
    private MaskView mMaskView;
    private ImageView mIvMask;
    private TextView mTvHint;
    private LinearLayout mBtnLayout;
    private ImageView mIvGiftBox;
    private ImageView mIvGifFire;
    private ImageView mIvBack;
    private ImageView mIvFlash;
    private TextView mTvCheat;
    private TextView mTvLottery;

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // 状态是预览时, 不需要做任何事情
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
//                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@   preview   afstate:   " + afState);
                    if (afState == null) {
                        //nexus 6p有时候会为null
                    } else if (afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED) {
                        if (System.currentTimeMillis() - mPassiveFocusTime > AppConstants.SCAN_INTERVAL && mIsScan) {
                            mIsScan = false;
                            mPassiveFocusTime = System.currentTimeMillis();
                            Message msg = Message.obtain();
                            msg.what = MSG_IDENTIFY_IMAGE;
                            msg.obj = mTextureView.getBitmap();
                            mMsgHandler.sendMessage(msg);
                        }
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    // Conversion from screen rotation to JPEG orientation.
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String mModelProto = Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/ocr/ocr.deploy";
    private String mModelBinary = Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/ocr/ocr.model";
    private ImageScaner mImageScaner;

    public static ScanWithCamera2Fragment newInstance() {
        return new ScanWithCamera2Fragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mImageIdentify = (IImageIdentify) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_with_camera2, container, false);
        mTextureView = view.findViewById(R.id.texture);
        mTextureView.setOnTouchListener(this);
        mMaskView = view.findViewById(R.id.mask_surface);
        mIvMask = view.findViewById(R.id.iv_mask);
        mTvHint = view.findViewById(R.id.tv_hint);
        mBtnLayout = view.findViewById(R.id.btn_layout);
        mIvGiftBox = view.findViewById(R.id.iv_gift_box);
        mIvGifFire = view.findViewById(R.id.iv_gift_fire);
        mIvBack = view.findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mIvFlash = view.findViewById(R.id.iv_flash);
        mIvFlash.setOnClickListener(this);
        mTvCheat = view.findViewById(R.id.tv_cheat);
        mTvCheat.setOnClickListener(this);
        mTvLottery = view.findViewById(R.id.tv_lottery);
        mTvLottery.setOnClickListener(this);

        // 识别
        mImageScaner = new ImageScaner();
        mImageScaner.setNumThreads(4);
        mImageScaner.loadModel(mModelProto, mModelBinary);
        float[] meanValues = {104, 117, 123};
        mImageScaner.setMean(meanValues);

        mContext = getActivity();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        startBackgroundThread();
        initTextureView();

        if (!mIsGiftAnimationCompleted) {
            showGiftAnimation();
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();

        mMsgHandler.removeMessages(MSG_IDENTIFY_IMAGE);
        mMsgHandler.removeMessages(MSG_SHOW_RESULT);
        mMsgHandler.removeMessages(MSG_GIF_BOX_FINISH);
        mMsgHandler.removeMessages(MSG_GIF_FIRE_FINISH);

        if (!mIsGiftAnimationCompleted) {
            mIvGifFire.setImageBitmap(null);
            mIvGiftBox.setVisibility(View.GONE);
            mIvGifFire.setImageBitmap(null);
            mIvGifFire.setVisibility(View.GONE);
        }
        super.onPause();
    }

    /**
     * 当屏幕关闭后重新打开, 若SurfaceTexture已经就绪, 此时onSurfaceTextureAvailable不会被回调, 这种情况下
     * 如果SurfaceTexture已经就绪, 则直接打开相机, 否则等待SurfaceTexture已经就绪的回调
     */
    private void initTextureView() {
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture texture) {
                }
            });
        }
    }

    /**
     * 返回最合适的预览尺寸,选择相机输出列表中的最接近预览尺寸并且最大的一个.
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           相机希望输出类支持的尺寸list
     * @param textureViewWidth  texture view 宽度
     * @param textureViewHeight texture view 高度
     * @param maxWidth          能够选择的最大宽度
     * @param maxHeight         能够选择的最大高度
     * @param aspectRatio       图像的比例(pictureSize, 只有当pictureSize和textureSize保持一致, 才不会失真)
     * @return 最合适的预览尺寸
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // 存放小于等于限定尺寸, 大于等于texture控件尺寸的Size
        List<Size> bigEnough = new ArrayList<>();
        // 存放小于限定尺寸, 小于texture控件尺寸的Size
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // 1. 若存在bigEnough数据, 则返回里面最小的
        // 2. 若不存bigEnough数据, 但是存在notBigEnough数据, 则返回里面最大的
        // 3. 上述两种数据都没有时, 返回空, 并在日志上显示错误信息
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * 设置相机的输出, 包括预览和拍照
     * <p>
     * 处理流程如下:
     * 1. 获取当前的摄像头, 并将拍照输出设置为所需要的画质
     * 2. 判断显示方向和摄像头传感器方向是否一致, 是否需要旋转画面
     * 3. 获取当前显示尺寸和相机的输出尺寸, 选择最合适的预览尺寸
     *
     * @param width  预览宽度
     * @param height 预览高度
     */
    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            // 遍历运行本应用的设备的所有摄像头
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // 如果该摄像头是前置摄像头, 则看下一个摄像头(本应用不使用前置摄像头)
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // 最高画质(镜头的最高分辨率)
//                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
//                configImageReader(largest);

                // 获取手机目前的旋转方向(横屏还是竖屏, 对于"自然"状态下高度大于宽度的设备来说横屏是ROTATION_90或者ROTATION_270,竖屏是ROTATION_0或者ROTATION_180)
                int displayRotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
                // 获取相机传感器的方向("自然"状态下垂直放置为0, 顺时针算起, 每次加90读).这个参数是由设备的生产商来决定的,大多数情况下,该值为90,以下的switch是为了配适某些特殊的手机
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    // ROTATION_0和ROTATION_180都是竖屏只需做同样的处理操作
                    // 显示为竖屏时, 若传感器方向为90或者270, 则需要进行转换(标志位置true)
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    // ROTATION_90和ROTATION_270都是横屏只需做同样的处理操作
                    // 显示为横屏时, 若传感器方向为0或者180, 则需要进行转换(标志位置true)
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                int rotatedPreviewWidth = width; // 旋转前的预览宽度(相机给出的), 通过传进来的参数获得
                int rotatedPreviewHeight = height; // 旋转前的预览高度(相机给出的), 通过传进来的参数获得
                // 将当前的显示尺寸赋给最大的预览尺寸(能够显示的尺寸, 用来计算用的(texture可能比它小需要配适))
                int maxPreviewWidth = AppUtil.getScreenW(mContext);
                int maxPreviewHeight = AppUtil.getScreenH(mContext);

                // 如果需要进行画面旋转, 将宽度和高度对调
                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = AppUtil.getScreenH(mContext);
                    maxPreviewHeight = AppUtil.getScreenW(mContext);
                }

                // 尺寸太大时的极端处理
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                Size aspectRatio = new Size(16, 9);

                // 自动计算出最适合的预览尺寸,第一个参数:map.getOutputSizes(SurfaceTexture.class)表示SurfaceTexture支持的尺寸List
                // (使用较大的预览尺寸会可能会超过相机的带宽限制,导致内存泄漏)
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth,
                        rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, aspectRatio);

                // 获取当前的屏幕方向
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // 是否支持闪光灯
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置ImageReader
     */
    private void configImageReader(Size largest) {
        if (mImageReader != null) {
            mImageReader.close();
        }
        mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/5);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
            }
        }, new Handler(mContext.getMainLooper()));
    }

    /**
     * 通过cameraId打开特定的相机
     */
    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw new RuntimeException("Time out waiting to lock camera opening.");
                ToastUtil.showToast(getActivity(), "请允许嘿快使用摄像头功能");
            }
            manager.openCamera(mCameraId, new CameraDevice.StateCallback() {

                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    // 尝试获得相机开打关闭许可, 等待2500时间仍没有获得则排除异常
                    mCameraOpenCloseLock.release();
                    mCameraDevice = cameraDevice;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    mCameraOpenCloseLock.release();
                    cameraDevice.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int error) {
                    mCameraOpenCloseLock.release();
                    cameraDevice.close();
                    mCameraDevice = null;
                    mContext.finish();
                }

            }, mBackgroundHandler);
        } catch (CameraAccessException | SecurityException e) {
            e.printStackTrace();
            ToastUtil.showToast(mContext, "请先打开拍照权限");
            mCameraOpenCloseLock.release();
            mCameraDevice = null;
            mContext.finish();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * 关闭正在使用的相机
     */
    private void closeCamera() {
        try {
            // 获得相机开打关闭许可
            mCameraOpenCloseLock.acquire();
            // 关闭捕获会话
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            // 关闭当前相机
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            // 关闭拍照处理器
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            // 释放相机开打关闭许可
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * 开启子线程
     */
    private void startBackgroundThread() {
        if (mBackgroundThread == null) {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
    }

    /**
     * 停止子线程
     */
    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建预览对话
     */
    private void createCameraPreviewSession() {
        try {
            // 获取texture实例
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // 设置宽度和高度
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // 用来开始预览的输出surface
            Surface surface = new Surface(texture);

            // 预览请求构建
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // 创建预览的捕获会话
//            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // 相机关闭时, 直接返回
                            if (null == mCameraDevice) {
                                return;
                            }

                            // 会话可行时, 将构建的会话赋给field
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // 自动对焦
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 自动闪光
//                                if (mFlashSupported) {
//                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//                                }

                                // 构建上述的请求
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                // 重复进行上面构建的请求, 以便显示预览
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            System.out.println("on ConfigureFailed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 屏幕方向发生改变时调用转换数据方法
     *
     * @param viewWidth  mTextureView 的宽度
     * @param viewHeight mTextureView 的高度
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    @Override
    public void scanPicture() {
        mIsScan = true;

        mTvHint.setVisibility(View.VISIBLE);
        mBtnLayout.setVisibility(View.VISIBLE);
        mMaskView.setVisibility(View.VISIBLE);
        mIvMask.setVisibility(View.GONE);
    }

    @Override
    public void showGiftAnimation() {
//        mIsGiftAnimationCompleted = false;
//        mIvGiftBox.setVisibility(View.VISIBLE);
//        GlideApp.with(getActivity())
//                .load(R.drawable.gift_box)
//                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                .listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        GifDrawable drawable = (GifDrawable) resource;
//                        GifDecoder decoder = drawable.getDecoder();
//                        int duration = 0;
//                        for (int i = 0; i < drawable.getFrameCount(); i++) {
//                            duration += decoder.getDelay(i);
//                        }
//                        mMsgHandler.sendEmptyMessageDelayed(MSG_GIF_BOX_FINISH, duration + 500);
//                        return false;
//                    }
//                }
//                .into(new DrawableImageViewTarget(mIvGiftBox, 1)));
    }

    @Override
    public void updatePrize(ARLotteryResponseObject.ARLotteryResponseBody prize) {
        this.mPrize = prize;
    }

//    /**
//     * 拍照操作
//     * 在mCaptureCallback回调状态为lockFocus后调用
//     */
//    private void captureStillPicture() {
//        try {
//            if (null == mCameraDevice) {
//                return;
//            }
//            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.addTarget(mImageReader.getSurface());
//
//            // 使用和预览一样的AE和AF设置
//            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//            if (mFlashSupported) {
//                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//            }
//
//            // 设置旋转方向
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
//
//            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
//                @Override
//                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
//                                               @NonNull CaptureRequest request,
//                                               @NonNull TotalCaptureResult result) {
//                    Log.d(TAG, "已拍照");
//                    unlockFocus();
//                }
//            };
//
//            mCaptureSession.stopRepeating();
//            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 根据屏幕的旋转方向获取JPEG的旋转方向
//     * 大多数设备设备mSensorOrientation的默认方向是90,也有一些设备是270(比如Nexus 5X)
//     * mSensorOrientation默认方向为90时,直接从ORIENTATIONS返回值即可
//     * mSensorOrientation默认方向为270时,需要旋转180度.
//     *
//     * @param rotation 屏幕的旋转方向
//     * @return JPEG的旋转方向 (one of 0, 90, 270, and 360)
//     */
//    private int getOrientation(int rotation) {
//        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
//    }
//
//    /**
//     * 解开锁定的焦点. 在拍照完成后调用
//     */
//    private void unlockFocus() {
//        try {
////            // 构建重置AF到初始化状态的请求
////            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
////            // 构建自动闪光请求(之前拍照前会构建为需要或者不需要闪光灯, 这里重新设回自动)
////            if (mFlashSupported) {
////                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
////            }
//            // 自动对焦
//            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//            // 自动闪光
//            if (mFlashSupported) {
//                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//            }
//
//
//            // 提交以上构建的请求
//            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
//            // 拍完照后, 设置成预览状态, 并重复预览请求
//            mState = STATE_PREVIEW;
//            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
//                    mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 比较两个Size的面积大小
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * 保存图片
     */
    private String saveMyBitmap(Bitmap mBitmap) {
        FileOutputStream fOut = null;
        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/scan_pic.jpg");
        try {
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f.getPath();
    }

    /**
     * 在线程中识别图片
     *
     * @param path
     */
    private void identifyImage(final String path) {
        new Thread() {
            public void run() {
                int result[] = mImageScaner.predictImage(path);
                Message msg = Message.obtain();
                msg.what = MSG_SHOW_RESULT;
                msg.arg1 = result[0];
                mMsgHandler.sendMessage(msg);
            }
        }.start();
    }

    private Handler mMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_RESULT:
                    if (msg.arg1 > IDENTIFY_SUCCESS_LEVEL) {
                        if (mImageIdentify != null && getActivity() != null) {
                            mIdentifyCount = 0;
                            mTvHint.setVisibility(View.GONE);
                            mBtnLayout.setVisibility(View.GONE);
                            mMaskView.setVisibility(View.GONE);
                            mIvMask.setVisibility(View.VISIBLE);
                            mImageIdentify.onIdentifySuccess();
                        }
                    } else {
                        if (++mIdentifyCount == MAX_IDENTIFY_COUNT) {
                            mIdentifyCount = 0;
                            if (mImageIdentify != null && getActivity() != null) {
                                mTvHint.setVisibility(View.GONE);
                                mBtnLayout.setVisibility(View.GONE);
                                mMaskView.setVisibility(View.GONE);
                                mIvMask.setVisibility(View.VISIBLE);
                                mImageIdentify.onIdentifyFail();
                            }
                        } else {
                            mIsScan = true;
                        }
                    }
                    break;
                case MSG_IDENTIFY_IMAGE:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (bitmap != null) {
                        int wScreen = AppUtil.getScreenW(mContext);
                        int hScreen = AppUtil.getScreenH(mContext);
                        // 遮罩框的尺寸
                        int sideLength = wScreen * 3 / 5;
                        float wRatio = (float) (bitmap.getWidth()) / (float) (wScreen);
                        float hRatio = (float) (bitmap.getHeight()) / (float) (hScreen);
                        float ratio = wRatio >= hRatio ? wRatio : hRatio;
                        sideLength = (int) (sideLength * ratio);
                        Bitmap rectBm = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - sideLength) / 2, (bitmap.getHeight() - sideLength) / 2, sideLength, sideLength);
                        bitmap.recycle();
                        Bitmap finalBm = Bitmap.createScaledBitmap(rectBm, 256, 256, false);
                        rectBm.recycle();
                        // 保存图片到sd卡
                        String path = saveMyBitmap(finalBm);
                        // 识别图片
                        identifyImage(path);
                    } else {
                        mIsScan = true;
                    }
                    break;
                case MSG_GIF_BOX_FINISH:
                    mIvGifFire.setVisibility(View.VISIBLE);
                    if (getActivity() != null) {
//                        GlideApp.with(getActivity())
//                                .load(R.drawable.gift_fire)
//                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                                .into(mIvGifFire);
                        mMsgHandler.sendEmptyMessageDelayed(MSG_GIF_FIRE_FINISH, 2000);
                    }
                    break;
                case MSG_GIF_FIRE_FINISH:
                    if (mPrize != null) {
                        mIsGiftAnimationCompleted = true;
                        mIvGiftBox.setVisibility(View.GONE);
                        mIvGifFire.setImageBitmap(null);
                        mIvGifFire.setVisibility(View.GONE);

                        showLotteryDialog();
                    } else {
                        mMsgHandler.sendEmptyMessageDelayed(MSG_GIF_FIRE_FINISH, 2000);
                    }
                    break;
            }
        }
    };

    private void showLotteryDialog() {
        LotteryDialog dialog = new LotteryDialog(getActivity());
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mTvHint.setVisibility(View.VISIBLE);
                mBtnLayout.setVisibility(View.VISIBLE);
                mMaskView.setVisibility(View.VISIBLE);
                mIvMask.setVisibility(View.GONE);

                mPrize = null;
                scanPicture();
            }
        });
        if ("0".equals(mPrize.type)) {
            dialog.setBackground(R.drawable.bg_no_lottery);
            dialog.setLogo("");
            dialog.setDescription(mPrize.description);
            dialog.setBtnStyle(R.drawable.btn_corner_red, getResources().getColor(R.color.red));
        } else if ("1".equals(mPrize.type)) {
            dialog.setBtnStyle(R.drawable.btn_corner_white, getResources().getColor(R.color.white));
//            if ("火锅".equals(mPrize.description)) {
//                dialog.setBackground(R.drawable.bg_huoguo);
//            } else if ("美女".equals(mPrize.description)) {
//                dialog.setBackground(R.drawable.bg_meinv);
//            } else if ("桥都".equals(mPrize.description)) {
//                dialog.setBackground(R.drawable.bg_qiaodu);
//            } else if ("山城".equals(mPrize.description)) {
//                dialog.setBackground(R.drawable.bg_shancheng);
//            } else if ("夜景".equals(mPrize.description)) {
//                dialog.setBackground(R.drawable.bg_yejing);
//            } else {
                dialog.setBackground(R.drawable.bg_no_lottery);
//            }
        } else if ("2".equals(mPrize.type)) {
            dialog.setBackground(R.drawable.bg_no_lottery);
            dialog.setLogo(mPrize.value);
            dialog.setDescription(mPrize.description);
            dialog.setBtnStyle(R.drawable.btn_corner_red, getResources().getColor(R.color.red));
        }
        dialog.show();
    }

    /**
     * 用于缩放
     *
     * @param v
     * @param event
     * @return
     */
    public boolean onTouch(View v, MotionEvent event) {
        try {
            CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            float maxzoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;

            Rect m = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            int action = event.getAction();
            float current_finger_spacing;

            if (event.getPointerCount() > 1) {
                // Multi touch logic
                current_finger_spacing = getFingerSpacing(event);

                if (mFingerSpacing != 0) {
                    if (current_finger_spacing > mFingerSpacing && maxzoom > mZoomLevel) {
                        mZoomLevel++;

                    } else if (current_finger_spacing < mFingerSpacing && mZoomLevel > 1) {
                        mZoomLevel--;

                    }
                    int minW = (int) (m.width() / maxzoom);
                    int minH = (int) (m.height() / maxzoom);
                    int difW = m.width() - minW;
                    int difH = m.height() - minH;
                    int cropW = difW / 100 * mZoomLevel;
                    int cropH = difH / 100 * mZoomLevel;
                    cropW -= cropW & 3;
                    cropH -= cropH & 3;
                    Rect zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
                    mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                }
                mFingerSpacing = current_finger_spacing;
            } else {
                if (action == MotionEvent.ACTION_UP) {
                    //single touch logic
                }
            }

            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback,
                        null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException("can not access camera.", e);
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                getActivity().finish();
                break;
            case R.id.iv_flash:
                if (!isFlash) {
                    openFlash();
                    mIvFlash.setImageResource(R.drawable.ic_flash_open);
                    isFlash = true;
                } else {
                    closeFlash();
                    mIvFlash.setImageResource(R.drawable.ic_flash_close);
                    isFlash = false;
                }
                break;
            case R.id.tv_cheat:
                gotoWebPage(AppConstants.LOTTERY_CHEATS_PAGE);
                break;
            case R.id.tv_lottery:
//                gotoWebPage(AppConstants.MY_LOTERY_PAGE);
                Intent intent = new Intent();
                intent.setClass(getActivity(), LotteryListActivity.class);
                getActivity().startActivity(intent);
                break;
        }
    }

    private void gotoWebPage(String url) {
        Intent intent = new Intent();
        intent.putExtra("url", url);
        WebActivityDispatcher dispatcher = new WebActivityDispatcher();
        dispatcher.dispatch(intent, getActivity());
    }

    private void openFlash() {
        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeFlash() {
        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算两个触摸点之间的距离
     *
     * @param event
     * @return
     */
    private float getFingerSpacing(MotionEvent event) {

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}

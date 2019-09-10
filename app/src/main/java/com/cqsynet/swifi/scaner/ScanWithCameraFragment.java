/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：使用Camera2 API的图形扫描Fragment (Android5.0及以后的版本)
 *
 *
 * 创建标识：sayaki 20170517
 */
package com.cqsynet.swifi.scaner;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.LotteryListActivity;
import com.cqsynet.swifi.model.ARLotteryResponseObject;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.LotteryDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Author: sayaki
 * Date: 2017/5/16
 */
public class ScanWithCameraFragment extends Fragment implements
        View.OnTouchListener, View.OnClickListener,
        Camera.PreviewCallback, IScanFragment {

    private static final int MSG_SHOT_PREVIEW = 0;
    private static final int MSG_IDENTIFY_IMAGE = 1;
    private static final int MSG_SHOW_RESULT = 2;
    private static final int MSG_GIF_BOX_FINISH = 3;
    private static final int MSG_GIF_FIRE_FINISH = 4;

    private static final int MAX_IDENTIFY_COUNT = 20;
    private static final int IDENTIFY_SUCCESS_LEVEL = 97;

    private Camera mCamera;
    private CameraPreview mPreview;
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

    private float mFingerDistance = 0; // 触摸点之间的距离
    private int mZoomLevel = 0; // 相机缩放等级
    private boolean isFlash = false; // 闪光灯是否开启
    private IImageIdentify mImageIdentify;
    private ARLotteryResponseObject.ARLotteryResponseBody mPrize;
    private SensorManager mSensorManager;
    private float[] mAccValue = {0f, 0f, 0f};
    private boolean mIsShake;
    private int mIdentifyCount;
    private boolean mIsScanCompleted; // 是否已经完成了一次扫描
    private boolean mIsGiftAnimationCompleted = true; // 礼物动画是否完成

    private ImageScaner mImageScaner;
    private String mModelProto = Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/ocr/ocr.deploy";
    private String mModelBinary = Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/ocr/ocr.model";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOT_PREVIEW:
                    if (!mIsShake && mCamera != null) {
                        mCamera.setOneShotPreviewCallback(ScanWithCameraFragment.this);
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_SHOT_PREVIEW, AppConstants.SCAN_INTERVAL);
                    }
                    break;
                case MSG_IDENTIFY_IMAGE:
                    mHandler.removeMessages(MSG_SHOT_PREVIEW);
                    identifyImage(msg.obj.toString());
                    break;
                case MSG_SHOW_RESULT:
                    if (msg.arg1 > IDENTIFY_SUCCESS_LEVEL) {
                        if (mImageIdentify != null && getActivity() != null) {
                            mIdentifyCount = 0;
                            mIsScanCompleted = true;
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
                                mIsScanCompleted = true;
                                mTvHint.setVisibility(View.GONE);
                                mBtnLayout.setVisibility(View.GONE);
                                mMaskView.setVisibility(View.GONE);
                                mIvMask.setVisibility(View.VISIBLE);
                                mImageIdentify.onIdentifyFail();
                            }
                        } else {
                            mHandler.sendEmptyMessageDelayed(MSG_SHOT_PREVIEW, AppConstants.SCAN_INTERVAL);
                        }
                    }
                    break;
                case MSG_GIF_BOX_FINISH:
                    mIvGifFire.setVisibility(View.VISIBLE);
                    if (getActivity() != null) {
//                        GlideApp.with(getActivity())
//                                .load(R.drawable.gift_fire)
//                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                                .into(mIvGifFire);
                        mHandler.sendEmptyMessageDelayed(MSG_GIF_FIRE_FINISH, 2000);
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
                        mHandler.sendEmptyMessageDelayed(MSG_GIF_FIRE_FINISH, 2000);
                    }
                    break;
            }
        }
    };

    public static ScanWithCameraFragment newInstance() {
        return new ScanWithCameraFragment();
    }

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
        View view = inflater.inflate(R.layout.fragment_scan_with_camera, container, false);

        mPreview = view.findViewById(R.id.camera_preview);
        mPreview.setOnTouchListener(this);
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

        mImageScaner = new ImageScaner();
        mImageScaner.setNumThreads(4);
        mImageScaner.loadModel(mModelProto, mModelBinary);
        float[] meanValues = {104, 117, 123};
        mImageScaner.setMean(meanValues);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(sensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);

        try {
            setUpCamera();
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToast(getActivity(), "请先打开拍照权限");
            getActivity().finish();
        }
        mPreview.setCamera(mCamera);

        if (!mIsScanCompleted) {
            mHandler.sendEmptyMessageDelayed(MSG_SHOT_PREVIEW, AppConstants.SCAN_INTERVAL);
        }
        if (!mIsGiftAnimationCompleted) {
            showGiftAnimation();
        }
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = event.values;
                float x = Math.abs(values[0] - mAccValue[0]);
                float y = Math.abs(values[1] - mAccValue[1]);
                float z = Math.abs(values[2] - mAccValue[2]);
                mIsShake = x > 0.1 || y > 0.1 || z > 0.1;

                mAccValue[0] = values[0];
                mAccValue[1] = values[1];
                mAccValue[2] = values[2];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public void onPause() {
        super.onPause();

        mHandler.removeMessages(MSG_SHOT_PREVIEW);
        mHandler.removeMessages(MSG_IDENTIFY_IMAGE);
        mHandler.removeMessages(MSG_SHOW_RESULT);
        mHandler.removeMessages(MSG_GIF_BOX_FINISH);
        mHandler.removeMessages(MSG_GIF_FIRE_FINISH);

        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (!mIsGiftAnimationCompleted) {
            mIvGifFire.setImageBitmap(null);
            mIvGiftBox.setVisibility(View.GONE);
            mIvGifFire.setImageBitmap(null);
            mIvGifFire.setVisibility(View.GONE);
        }

        mSensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void scanPicture() {
        mIsScanCompleted = false;
        mTvHint.setVisibility(View.VISIBLE);
        mBtnLayout.setVisibility(View.VISIBLE);
        mMaskView.setVisibility(View.VISIBLE);
        mIvMask.setVisibility(View.GONE);
        mHandler.sendEmptyMessageDelayed(MSG_SHOT_PREVIEW, AppConstants.SCAN_INTERVAL);
    }

    @Override
    public void showGiftAnimation() {
        mIsGiftAnimationCompleted = false;
        mIvGiftBox.setVisibility(View.VISIBLE);
//        GlideApp.with(getActivity())
//                .load(R.drawable.gift_box)
//                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                .listener(new RequestListener<Integer, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        GifDrawable drawable = (GifDrawable) resource;
//                        GifDecoder decoder = drawable.getDecoder();
//                        int duration = 0;
//                        for (int i = 0; i < drawable.getFrameCount(); i++) {
//                            duration += decoder.getDelay(i);
//                        }
//                        mHandler.sendEmptyMessageDelayed(MSG_GIF_BOX_FINISH, duration + 500);
//                        return false;
//                    }
//                })
//                .into(new GlideDrawableImageViewTarget(mIvGiftBox, 1));
    }

    @Override
    public void updatePrize(ARLotteryResponseObject.ARLotteryResponseBody prize) {
        this.mPrize = prize;
    }

    private void setUpCamera() {
        if (Camera.getNumberOfCameras() > 0) {
            mCamera = Camera.open(0);
            Camera.Parameters parameters = mCamera.getParameters();
            if (isAutoFocusSupported(parameters)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            if (isAutoFlashSupported(parameters)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            if (isAutoSceneSupported(parameters)) {
                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            }
            Camera.Size previewSize = chooseOptimalSize(mPreview.getWidth(), mPreview.getHeight(),
                    16.0f / 9.0f, parameters.getSupportedPreviewSizes());
            Camera.Size pictureSize = chooseOptimalSize(mPreview.getWidth(), mPreview.getHeight(),
                    16.0f / 9.0f, parameters.getSupportedPictureSizes());
            parameters.setPreviewSize(previewSize.width, previewSize.height);
//            parameters.setPreviewFpsRange(4, 10);
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);

            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(parameters);
            mCamera.cancelAutoFocus();

            try {
                mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtil.showToast(getActivity(), "您的手机不支持拍照功能");
        }
    }

    private static boolean isAutoFocusSupported(Camera.Parameters parameters) {
        List<String> modes = parameters.getSupportedFocusModes();
        return modes.contains(Camera.Parameters.FOCUS_MODE_AUTO);
    }

    private static boolean isAutoFlashSupported(Camera.Parameters parameters) {
        List<String> modes = parameters.getSupportedFlashModes();
        return modes.contains(Camera.Parameters.FLASH_MODE_AUTO);
    }

    private static boolean isAutoSceneSupported(Camera.Parameters parameters) {
        List<String> modes = parameters.getSupportedSceneModes();
        return modes.contains(Camera.Parameters.SCENE_MODE_AUTO);
    }

    private Camera.Size chooseOptimalSize(int width, int height, float aspectRatio, List<Camera.Size> sizes) {
        for (int i = 0; i < sizes.size(); i++) {
            if (sizes.get(i).width >= width && sizes.get(i).height >= height
                    && equalsRatio(sizes.get(i), aspectRatio)) {
                return sizes.get(i);
            }
        }
        return sizes.get(sizes.size() - 1);
    }

    private boolean equalsRatio(Camera.Size size, float aspectRatio) {
        float ratio = size.width * 1.0f / size.height;
        return Math.abs(ratio - aspectRatio) <= 0.2;
    }

    /**
     * 计算两个触摸点之间的距离
     *
     * @param event 触摸事件
     * @return 两个触摸点之间的距离
     */
    private float getFingerDistance(MotionEvent event) {

        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void identifyImage(final String path) {
        new Thread() {
            public void run() {
                int result[] = mImageScaner.predictImage(path);
                Message msg = Message.obtain();
                msg.what = MSG_SHOW_RESULT;
                msg.arg1 = result[0];
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getPointerCount() > 1 && mCamera.getParameters().isZoomSupported()) {
            float fingerDistance = getFingerDistance(event);
            float maxZoom = mCamera.getParameters().getMaxZoom();
            Camera.Parameters parameters = mCamera.getParameters();
            if (mFingerDistance != 0) {
                if (fingerDistance > mFingerDistance && maxZoom > mZoomLevel) {
                    mZoomLevel += 2;
                } else if (fingerDistance < mFingerDistance && mZoomLevel > 0) {
                    mZoomLevel -= 2;
                }
                parameters.setZoom(mZoomLevel);
                mCamera.setParameters(parameters);
            }
            mFingerDistance = fingerDistance;
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
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);
    }

    private void closeFlash() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(parameters);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);
        byte[] rawImage = baos.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
        // 遮罩框的尺寸
        int sideLength = (int) (previewSize.height * 3.0f / 5.0f);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - sideLength) / 2,
                (bitmap.getHeight() - sideLength) / 2, sideLength, sideLength, matrix, false);
        Bitmap finalBmp = Bitmap.createScaledBitmap(bitmap, 256, 256, false);
        if (finalBmp != null) {
            Message msg = Message.obtain();
            msg.what = MSG_IDENTIFY_IMAGE;
            msg.obj = saveMyBitmap(finalBmp);
            mHandler.sendMessage(msg);
        }
    }

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
}

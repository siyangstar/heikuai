/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：选择图片的Activity。
 *
 *
 * 创建标识：br 20150210
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.BuildConfig;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.PhotoAlbumLVAdapter;
import com.cqsynet.swifi.model.PhotoAlbumLVItem;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION_CODES.N;

public class SelectionPictureActivity extends HkActivity implements OnClickListener {

    private TitleBar mTitleBar;
    private final int mRequestCodeFromCamera = 3033001; // 拍照
    private final int mRequestCodeFromPhoto = 3033002; // 相册
    private final int mRequestCodeCutImage = 3033003; // 裁剪
    private final int mRequestCodeChuseFromPhoto = 3033004;
    private File mGetFile; // 拍照、相册到的图片文件
    private File mCutFile; // 裁剪后的图片文件
    private File mSaveDir;
    private String mTitleVar = "";
    private ListView mListView;
    private PhotoAlbumLVAdapter adapter;
    private int mAspectX; //图片需要裁剪的参数
    private int mAspectY;
    private int mOutputX;
    private int mOutputY;
    private boolean mIsNeedCut; // 是否需要裁剪图片
    private ImageView[] mImageViews;
    private final static int SCAN_OK = 0;
    private ArrayList<PhotoAlbumLVItem> albumList = new ArrayList<PhotoAlbumLVItem>();
    private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();
    private ArrayList<String> mRecentImgList; //最新的6张

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        mTitleVar = intent.getStringExtra("title");
        setImageZoomParam(intent.getIntExtra("aspectX", 1), intent.getIntExtra("aspectY", 1),
                intent.getIntExtra("outputX", 100), intent.getIntExtra("outputY", 100),
                intent.getBooleanExtra("isNeedCut", false));

        setContentView(R.layout.activity_selection_picture);
        mTitleBar = findViewById(R.id.titlebar_activity_selection_picture);
        mTitleBar.setTitle(mTitleVar);
        mTitleBar.setLeftIconClickListener(this);
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ToastUtil.showToast(this, "SD卡不可用");
            return;
        }
        int[] imageViewIds = {R.id.nearrest0, R.id.nearrest1, R.id.nearrest2, R.id.nearrest3, R.id.nearrest4,
                R.id.nearrest5};
        mImageViews = new ImageView[imageViewIds.length];
        for (int i = 0; i < imageViewIds.length; i++) {
            mImageViews[i] = findViewById(imageViewIds[i]);
            mImageViews[i].setOnClickListener(this);
        }
        findViewById(R.id.from_camera).setOnClickListener(this);
        // 获取最近照片
        mRecentImgList = getLatestImagePaths(6);
        if (mRecentImgList != null) {
            for (int i = 0; i < mRecentImgList.size(); i++) {
                GlideApp.with(this)
                        .load(mRecentImgList.get(i))
                        .centerCrop()
                        .error(R.drawable.image_bg)
                        .into(mImageViews[i]);
            }
        } else {
            ToastUtil.showToast(getApplicationContext(), "无最近照片");
        }


        // 获取相册列表
        mListView = findViewById(R.id.list_album);
        getImages(); // 获取所有图片

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<String> pathList = mGruopMap.get(albumList.get(position).getPathName());
                Intent intent = new Intent(getApplicationContext(), PhotoWallActivity.class);
                intent.putStringArrayListExtra("folderPath", (ArrayList<String>) pathList);
                startActivityForResult(intent, mRequestCodeChuseFromPhoto);
            }
        });

        mSaveDir = new File(Environment.getExternalStorageDirectory(), AppConstants.CACHE_DIR);
        if (!mSaveDir.exists()) {
            mSaveDir.mkdirs();
        }
    }

    /**
     * 使用ContentProvider读取SD卡最近图片。
     */
    private ArrayList<String> getLatestImagePaths(int maxCount) {
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
        String key_DATA = MediaStore.Images.Media.DATA;

        ContentResolver mContentResolver = getContentResolver();

        // 只查询jpg和png的图片,按最新修改排序
        Cursor cursor = mContentResolver.query(mImageUri, new String[]{key_DATA}, key_MIME_TYPE + "=? or "
                + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?", new String[]{"image/jpg", "image/jpeg",
                "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

        ArrayList<String> latestImagePaths = null;
        if (cursor != null) {
            // 从最新的图片开始读取.
            // 当cursor中没有数据时，cursor.moveToLast()将返回false
            if (cursor.moveToLast()) {
                latestImagePaths = new ArrayList<String>();
                while (true) {
                    // 获取图片的路径
                    String path = cursor.getString(0);
                    latestImagePaths.add(path);

                    if (latestImagePaths.size() >= maxCount || !cursor.moveToPrevious()) {
                        break;
                    }
                }
            }
            cursor.close();
        }
        return latestImagePaths;
    }

    /**
     * 通过拍照获取图片
     */
    public void fromCamera() {
        mGetFile = new File(mSaveDir, "get-" + System.currentTimeMillis() + ".jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= N) {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, mGetFile.getAbsolutePath());
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mGetFile));
        }
        startActivityForResult(intent, mRequestCodeFromCamera);
    }

    /**
     * 从相册选择图片
     */
    public void fromPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, mRequestCodeFromPhoto);
    }

    /**
     * 设置剪切图片的参数，例如setZoomParam(1, 1, 180, 180)
     *
     * @param aspectX 是宽高的比例
     * @param aspectY 是宽高的比例
     * @param outputX 是裁剪图片宽
     * @param outputY 是裁剪图片高
     */
    public void setImageZoomParam(int aspectX, int aspectY, int outputX, int outputY, boolean isNeedCut) {
        mIsNeedCut = isNeedCut;
        mAspectX = aspectX;
        mAspectY = aspectY;
        mOutputX = outputX;
        mOutputY = outputY;
    }

    /**
     * 显示截取框
     *
     * @param file
     */
    private void startPhotoZoom(File file) {
        if (Build.VERSION.SDK_INT >= N) {
            Intent intent = new Intent("com.android.camera.action.CROP");
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
            grantUriPermission("com.android.camera", uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            // aspectX aspectY 是宽高的比例
            intent.putExtra("aspectX", mAspectX);
            intent.putExtra("aspectY", mAspectY);
            // outputX outputY 是裁剪图片宽高
            intent.putExtra("outputX", mOutputX);
            intent.putExtra("outputY", mOutputY);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);
//            Uri cutUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider" + mCutFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCutFile));
            startActivityForResult(intent, mRequestCodeCutImage);
        } else {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            intent.putExtra("crop", "true");
            // aspectX aspectY 是宽高的比例
            intent.putExtra("aspectX", mAspectX);
            intent.putExtra("aspectY", mAspectY);
            // outputX outputY 是裁剪图片宽高
            intent.putExtra("outputX", mOutputX);
            intent.putExtra("outputY", mOutputY);
            intent.putExtra("scale", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCutFile));
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);
            startActivityForResult(intent, mRequestCodeCutImage);
        }
    }

    /**
     * android N之后的版本获取file的uri
     *
     * @param context
     * @param imageFile
     * @return
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 从相册或拍照获取到图片后的下一步处理
     *
     * @param file
     */
    private void doImage(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (mIsNeedCut) {
            mCutFile = new File(mSaveDir, "cut-" + (System.currentTimeMillis() + 1) + ".jpg");
            startPhotoZoom(file);
        } else {
            resut(mGetFile.getAbsolutePath());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            if (requestCode == mRequestCodeFromCamera) { // 拍照
                // 此处应该结束选择，返回图片链接 mGetFile
                doImage(mGetFile);
            } else if (requestCode == mRequestCodeFromPhoto) { // 相册
                Uri originalUri = data.getData(); // 获得图片的uri
                // 获取相册列表返回的图片路径
                String[] proj = {MediaStore.Images.Media.DATA};
                // 好像是android多媒体数据库的封装接口，具体的看Android文档
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                // 按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                // 最后根据索引值获取图片路径
                String path = cursor.getString(column_index);
                mGetFile = new File(path);
                doImage(mGetFile);
            } else if (requestCode == mRequestCodeCutImage) { // 裁剪
                if (mCutFile != null) {
                    resut(mCutFile.getAbsolutePath());
                }
            } else if (requestCode == mRequestCodeChuseFromPhoto) { // 从相册选
                if (data.hasExtra("path")) {
                    String file = data.getStringExtra("path");
                    mGetFile = new File(file);
                    doImage(mGetFile);
                }

            }
        }
    }

    private void resut(String file) {
        Intent intent = new Intent();
        intent.putExtra("file", file);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivBack_titlebar_layout) { // 返回
            finish();
        } else if (v.getId() == R.id.from_camera) {
            fromCamera();
        } else if (v.getId() == R.id.nearrest0 || v.getId() == R.id.nearrest1 || v.getId() == R.id.nearrest2
                || v.getId() == R.id.nearrest3 || v.getId() == R.id.nearrest4 || v.getId() == R.id.nearrest5) {
            if (mRecentImgList == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.nearrest0:
                    if (mRecentImgList.size() > 0) {
                        mGetFile = new File(mRecentImgList.get(0));
                    }
                    break;
                case R.id.nearrest1:
                    if (mRecentImgList.size() > 1) {
                        mGetFile = new File(mRecentImgList.get(1));
                    }
                    break;
                case R.id.nearrest2:
                    if (mRecentImgList.size() > 2) {
                        mGetFile = new File(mRecentImgList.get(2));
                    }
                    break;
                case R.id.nearrest3:
                    if (mRecentImgList.size() > 3) {
                        mGetFile = new File(mRecentImgList.get(3));
                    }
                    break;
                case R.id.nearrest4:
                    if (mRecentImgList.size() > 4) {
                        mGetFile = new File(mRecentImgList.get(4));
                    }
                    break;
                case R.id.nearrest5:
                    if (mRecentImgList.size() > 5) {
                        mGetFile = new File(mRecentImgList.get(5));
                    }
                    break;
            }

            if (mGetFile != null && mGetFile.exists()) {
                doImage(mGetFile);
            }

        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_OK:
                    getAlbumList(mGruopMap);
                    adapter = new PhotoAlbumLVAdapter(SelectionPictureActivity.this, albumList);
                    mListView.setAdapter(adapter);
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 利用ContentProvider扫描手机中的图片
     */
    private void getImages() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = getContentResolver();

                // 只查询jpg,jpeg和png的图片
                Cursor mCursor = mContentResolver.query(mImageUri, null, // new String[] { MediaStore.Images.Media.DATA }
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpg", "image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

                while (mCursor.moveToNext()) {
                    //获取图片的路径
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));

                    //获取该图片的父路径名
                    String parentName = new File(path).getParentFile().getName();


                    //根据父路径名将图片放入到mGruopMap中
                    if (!mGruopMap.containsKey(parentName)) {
                        List<String> childList = new ArrayList<String>();
                        childList.add(path);
                        mGruopMap.put(parentName, childList);
                    } else {
                        mGruopMap.get(parentName).add(path);
                    }
                }
                mCursor.close();
                // 通知Handler扫描图片完成
                handler.sendEmptyMessage(SCAN_OK);
            }
        }).start();
    }


    /**
     * 组装相册列表listview的数据源
     * 遍历HashMap将数据组装成List
     *
     * @param mMap
     */
    private void getAlbumList(HashMap<String, List<String>> mMap) {
        if (mMap.size() == 0) {
            return;
        }
        Iterator<Map.Entry<String, List<String>>> it = mMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            String key = entry.getKey();
            List<String> value = entry.getValue();
            PhotoAlbumLVItem mPhotoItem = new PhotoAlbumLVItem(key, value.size(), value.get(0));
            albumList.add(mPhotoItem);
        }
    }
}

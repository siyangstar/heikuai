/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：编辑头像的Activity
 * 		     主要提供拍照和从相册获取图片，可设置是否需要剪切，
 * 		     图片获取成功后，回调onHeadPortraitsBack方法
 *
 *
 * 创建标识：duxl 20141216
 */
package com.cqsynet.swifi.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

/**
 * 编辑头像的Activity<br />
 * 主要提供拍照和从相册获取图片，可设置是否需要剪切<br />
 * 图片获取成功后，回调{@link #onHeadPortraitsBack(String)}方法
 * 
 * @author duxl
 * 
 */
public abstract class EditHeadPortraitsActivity extends HkActivity {

	private final int mRequestCodeFromCamera = 3033001; // 拍照
	private final int mRequestCodeFromPhoto = 3033002; // 相册
	private final int mRequestCodeCutImage = 3033003; // 裁剪
	private File mGetFile; // 拍照、相册到的图片文件
	private File mCutFile; // 裁剪后的图片文件
	private File saveDir;
	private int mAspectX, mAspectY, mOutputX, mOutputY; // 图片需要裁剪是的参数
	private boolean mIsNeedCut; // 是否需要裁剪图片

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		saveDir = new File(Environment.getExternalStorageDirectory(), "head");
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}
	}

	private void createFile() {
		mGetFile = new File(saveDir, "get-" + System.currentTimeMillis() + ".jpg");
		mCutFile = new File(saveDir, "cut-" + (System.currentTimeMillis() + 1) + ".jpg");
	}

	/**
	 * 显示默认拍照、相册选择对话框，如需裁剪，先调用{@link #setHeadZoomParam setHeadZoomParam(int,
	 * int, int, int)}方法
	 */
	public void showDefaultHeadFromDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("编辑头像");
		String[] items = { "拍照", "从相册" };
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) { // 拍照
					fromCamera();
				} else if (which == 1) { // 从相册
					fromPhoto();
				}
			}
		});
		builder.create().show();
	}

	/**
	 * 通过拍照获取图片，如需裁剪，先调用{@link #setHeadZoomParam setHeadZoomParam(int, int, int,
	 * int)}方法
	 */
	public void fromCamera() {
		createFile();
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mGetFile));
		startActivityForResult(intent, mRequestCodeFromCamera);
	}

	/**
	 * 从相册选择图片，如需裁剪，先调用{@link #setHeadZoomParam setHeadZoomParam(int, int, int,
	 * int)}方法
	 */
	public void fromPhoto() {
		createFile();
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		startActivityForResult(intent, mRequestCodeFromPhoto);
	}

	/**
	 * 设置剪切图片的参数，例如setZoomParam(1, 1, 180, 180)
	 * 
	 * @param aspectX
	 *            是宽高的比例
	 * @param aspectY
	 *            是宽高的比例
	 * @param outputX
	 *            是裁剪图片宽
	 * @param outputY
	 *            是裁剪图片高
	 */
	public void setHeadZoomParam(int aspectX, int aspectY, int outputX, int outputY) {
		mIsNeedCut = true;
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

	/**
	 * 从相册或拍照获取到图片后的下一步处理
	 * 
	 * @param file
	 */
	private void doImage(File file) {
		if (mIsNeedCut) {
			startPhotoZoom(file);
		} else {
			onHeadPortraitsBack(file.getAbsolutePath());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Activity.RESULT_OK == resultCode) {
			if (requestCode == mRequestCodeFromCamera) { // 拍照
				doImage(mGetFile);
			} else if (requestCode == mRequestCodeFromPhoto) { // 相册
				ContentResolver cr = getContentResolver();
				try {
					DisplayMetrics dm = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(dm);
					int screenWidth = dm.widthPixels;
					Options option = ImageUtils.getBitmapOption(cr.openInputStream(data.getData()), screenWidth);
					Bitmap b = ImageUtils.getBitmapFromStream(cr.openInputStream(data.getData()), option, screenWidth);
					mGetFile.deleteOnExit();
					mGetFile.createNewFile();
					FileOutputStream out = new FileOutputStream(mGetFile);
					b.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
					doImage(mGetFile);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (requestCode == mRequestCodeCutImage) { // 裁剪
				onHeadPortraitsBack(mCutFile.getAbsolutePath());
			}
		}
	}

	public abstract void onHeadPortraitsBack(String path);

	private static class ImageUtils {
		public static Options getBitmapOption(InputStream in, int screenWidth) {
			int sampleSize = 1;
			Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, options);
			if (options.outWidth > screenWidth || options.outHeight > screenWidth) {
				if (options.outWidth > options.outHeight) {
					sampleSize = options.outWidth / screenWidth;
				} else {
					sampleSize = options.outHeight / screenWidth;
				}
			}
			options.inDither = false;
			options.inSampleSize = sampleSize;
			options.inTempStorage = new byte[16 * 1024];
			options.inJustDecodeBounds = false;
			return options;
		}

		public static Bitmap getBitmapFromStream(InputStream in, Options options, int screenWidth) {
			Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, screenWidth, screenWidth);
			return bitmap;
		}
	}
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：截屏工具类
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.util;

import static android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND;
import static android.graphics.Bitmap.Config.ARGB_8888;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import com.cqsynet.swifi.AppConstants;

public class ScreenShotUtil {

	// /**
	// * 截图
	// *
	// * @param context
	// * @param view
	// * @return 图片保存路径
	// */
	// public static String takeScreenShot(Context context, View view) {
	// File imgFile = null;
	// File path = null;
	// FileOutputStream fOut = null;
	// try {
	// view.setDrawingCacheEnabled(true);
	// Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
	// view.setDrawingCacheEnabled(false);
	//
	// if (android.os.Environment.MEDIA_MOUNTED
	// .equals(android.os.Environment.getExternalStorageState())) {
	// path = new File(Environment.getExternalStorageDirectory()
	// .getPath()
	// + "/"
	// + AppConstants.SHARED_PREF_FILE_NAME
	// + "/pic/");
	// } else {
	// path = new File(context.getFilesDir().getPath());
	// }
	//
	// if (!path.exists()) {
	// path.mkdirs();
	// }
	// imgFile = new File(path.getPath(), System.currentTimeMillis()
	// + ".jpg");
	// if (imgFile.exists()) {
	// imgFile.delete();
	// }
	// imgFile.createNewFile();
	// fOut = new FileOutputStream(imgFile);
	// bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
	// fOut.flush();
	// fOut.close();
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// return imgFile.getPath();
	// }

	/**
	 * 进行截取屏幕
	 * 
	 * @param pActivity
	 * @return bitmap
	 */
	public static Bitmap ShotScreen(Activity activity) {
//		Bitmap bitmap = null;
		View view = activity.getWindow().getDecorView();
		// 获取状态栏高度
		Rect frame = new Rect();
		// 测量屏幕宽和高
		view.getWindowVisibleDisplayFrame(frame);
		int statusHeight = frame.top;// 状态栏的高度
				
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight() - statusHeight, ARGB_8888);
//		// 设置是否可以进行绘图缓存
//		view.setDrawingCacheEnabled(true);
//		// 如果绘图缓存无法，强制构建绘图缓存
//		view.buildDrawingCache();
//		// 返回这个缓存视图
//		bitmap = Bitmap.createBitmap(view.getDrawingCache());
//		view.setDrawingCacheEnabled(false);
//
//		// 获取状态栏高度
//		Rect frame = new Rect();
//		// 测量屏幕宽和高
//		view.getWindowVisibleDisplayFrame(frame);
//		int stautsHeight = frame.top;// 状态栏的高度
//
//		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
//		int height = activity.getWindowManager().getDefaultDisplay().getHeight();
//		// 根据坐标点和需要的宽和高创建bitmap
//		bitmap = Bitmap.createBitmap(bitmap, 0, stautsHeight, width, height - stautsHeight);

		// 将所有根view画到画布上
		List<ViewRootData> list = getRootViews(activity);
		for (ViewRootData rootData : list) {
			drawRootToBitmap(rootData, bitmap, statusHeight);
		}

		return bitmap;
	}

	/**
	 * 保存图片到sdcard中
	 * 
	 * @param bitmap
	 */
	private static boolean savePic(Bitmap bitmap, String strName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(strName);
			if (null != fos) {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
				fos.flush();
				fos.close();
				return true;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 截图
	 * 
	 * @param pActivity
	 * @return 截图并且保存sdcard成功返回true，否则返回false
	 */
	public static boolean ShotScreenAndSave(Activity activity) {
		String path = Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/"
				+ System.currentTimeMillis() + ".jpg";
		return savePic(ShotScreen(activity), path);
	}

	/**
	 * 获取该activity上的所有根节点view
	 * 
	 * @param activity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<ViewRootData> getRootViews(Activity activity) {
		List<ViewRootData> rootViews = new ArrayList<ViewRootData>();

		Object globalWindowManager;
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			globalWindowManager = getFieldValue("mWindowManager", activity.getWindowManager());
		} else {
			try {
				globalWindowManager = getFieldValue("mGlobal", activity.getWindowManager());
			} catch(Exception e) {
				globalWindowManager = getFieldValue("mWindowManager", activity.getWindowManager());
			}
		}
		Object rootObjects = getFieldValue("mRoots", globalWindowManager);
		Object paramsObject = getFieldValue("mParams", globalWindowManager);

		Object[] roots;
		LayoutParams[] params;

		// There was a change to ArrayList implementation in 4.4
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			roots = ((List<?>) rootObjects).toArray();

			List<LayoutParams> paramsList = (List<LayoutParams>) paramsObject;
			params = paramsList.toArray(new LayoutParams[paramsList.size()]);
		} else {
			roots = (Object[]) rootObjects;
			params = (LayoutParams[]) paramsObject;
		}

		for (int i = 0; i < roots.length; i++) {
			Object root = roots[i];
			Object attachInfo = getFieldValue("mAttachInfo", root);
			int top = (Integer) getFieldValue("mWindowTop", attachInfo);
			int left = (Integer) getFieldValue("mWindowLeft", attachInfo);
			Rect winFrame = (Rect) getFieldValue("mWinFrame", root);
			Rect area = new Rect(left, top, left + winFrame.width(), top + winFrame.height());
			View view = (View) getFieldValue("mView", root);
			rootViews.add(new ViewRootData(view, area, params[i]));
		}
		return rootViews;
	}

	private static Object getFieldValue(String fieldName, Object target) {
		try {
			return getFieldValueUnchecked(fieldName, target);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Object getFieldValueUnchecked(String fieldName, Object target) throws NoSuchFieldException,
			IllegalAccessException {
		Field field = findField(fieldName, target.getClass());
		field.setAccessible(true);
		return field.get(target);
	}

	private static Field findField(String name, Class<?> clazz) throws NoSuchFieldException {
		Class<?> currentClass = clazz;
		while (currentClass != Object.class) {
			for (Field field : currentClass.getDeclaredFields()) {
				if (name.equals(field.getName())) {
					return field;
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchFieldException("Field " + name + " not found for class " + clazz);
	}

	private static class ViewRootData {
		private final View _view;
		private final Rect _winFrame;
		private final LayoutParams _layoutParams;

		public ViewRootData(View view, Rect winFrame, LayoutParams layoutParams) {
			_view = view;
			_winFrame = winFrame;
			_layoutParams = layoutParams;
		}
	}

	/**
	 * 将view画到bitmap上
	 * @param rootData
	 * @param bitmap
	 */
	private static void drawRootToBitmap(ViewRootData rootData, Bitmap bitmap, int offsetY) {
		if ((rootData._layoutParams.flags & FLAG_DIM_BEHIND) == FLAG_DIM_BEHIND) {
			Canvas dimCanvas = new Canvas(bitmap);
			int alpha = (int) (255 * rootData._layoutParams.dimAmount);
			dimCanvas.drawARGB(alpha, 0, 0, 0);
		}
		Canvas canvas = new Canvas(bitmap);
		canvas.translate(rootData._winFrame.left, rootData._winFrame.top - offsetY);
		rootData._view.draw(canvas);
	}
}
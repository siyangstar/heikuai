/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：activity基类
 *
 *
 * 创建标识：zhaosy 20151110
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.app.Dialog;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;

import com.cqsynet.swifi.AppManager;
import com.cqsynet.swifi.util.ShakeListenerUtil;
import com.cqsynet.swifi.view.LoadingDialog;

public class HkActivity extends Activity {

	private SensorManager mSensorManager;
	private ShakeListenerUtil mShakeUtils;
    private Dialog mLoadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);// 加入Activity容器
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置样式
		mShakeUtils = new ShakeListenerUtil(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensorManager.registerListener(mShakeUtils, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
//		Bugtags.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mShakeUtils);
//		Bugtags.onPause(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
        AppManager.getInstance().removeActivity(this);// 从Activity容器中删除该activity
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
//		Bugtags.onDispatchTouchEvent(this, ev);
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 显示加载框
	 * @param message
	 */
	protected void showProgressDialog(String message) {
        mLoadingDialog = LoadingDialog.createLoadingDialog(this, message);
        mLoadingDialog.show();
	}
	
	/**
	 * 显示加载框
	 * @param messageResid
	 */
	protected void showProgressDialog(int messageResid) {
		showProgressDialog(getString(messageResid));
	}
	
	/**
	 * 取消加载显示
	 */
	protected void dismissProgressDialog() {
		if(mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
		}
	}

}
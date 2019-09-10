/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：摇一摇功能
 *
 *
 * 创建标识：zhaosy 20151110
 */
package com.cqsynet.swifi.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;

import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.activity.ScreenShotActivity;

public class ShakeListenerUtil implements SensorEventListener {

	private Activity mContext;
	private Vibrator mVibrator;
	private int mShakeTime = 0;
	private MediaActionSound mSound;
	private AudioManager mAudioManager;
	private float[] mAccValue = {0f, 0f, 0f};

	@SuppressLint("NewApi")
	public ShakeListenerUtil(Activity context) {
		super();
		mContext = context;
		mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mSound = new MediaActionSound();  
	        mSound.load(MediaActionSound.SHUTTER_CLICK);
	        mAudioManager = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@SuppressLint("NewApi")
	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		if (sensorType == Sensor.TYPE_ACCELEROMETER) {
			//values[0]:X轴，values[1]：Y轴，values[2]：Z轴
			float[] values = event.values;
			float x = Math.abs(values[0] - mAccValue[0]);
			float y = Math.abs(values[1] - mAccValue[1]);
			float z = Math.abs(values[2] - mAccValue[2]);
			if (x > 15 || y > 15 || z > 15) {
				if(mShakeTime == 3) {
//					ToastUtil.showToast(mContext, "摇一摇,更健康~");
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						int systemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						if(systemVolume == 0) {
							mVibrator.vibrate(500);
						} else {
							mSound.play(MediaActionSound.SHUTTER_CLICK);
						}
					}
					hdl.sendEmptyMessageDelayed(0, 1000);
					Globals.g_screenshot = ScreenShotUtil.ShotScreen(mContext);
					Intent intent = new Intent(mContext, ScreenShotActivity.class);
					mContext.startActivity(intent);
				} else if(mShakeTime < 3) {
					hdl.sendEmptyMessageDelayed(0, 300);
				}
				mShakeTime++;
			}
			mAccValue[0] = values[0];
			mAccValue[1] = values[1];
			mAccValue[2] = values[2];
		}
	}
	
	private Handler hdl = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 0:
				mShakeTime = 0;
			}
		}
    };
}
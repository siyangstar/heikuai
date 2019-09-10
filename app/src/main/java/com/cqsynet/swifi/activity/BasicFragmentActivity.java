/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：父Activity，安卓2.3系统如果要用Fragment，需要FragmentActivity的支持。
 *
 *
 * 创建标识：luchaowei 20140922
 */
package com.cqsynet.swifi.activity;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.Window;

import com.cqsynet.swifi.AppManager;
import com.cqsynet.swifi.util.ShakeListenerUtil;

public abstract class BasicFragmentActivity extends FragmentActivity {

	private SensorManager mSensorManager;
	private ShakeListenerUtil mShakeUtils;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		mShakeUtils = new ShakeListenerUtil(this); 
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensorManager.registerListener(mShakeUtils, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
//        Bugtags.onResume(this);
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mShakeUtils);
//        Bugtags.onPause(this);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getInstance().removeActivity(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Bugtags.onDispatchTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }
}

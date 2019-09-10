/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：保持手机cpu唤醒
 *
 *
 * 创建标识：zhaosy 20150811
 */
package com.cqsynet.swifi.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class WakeupUtil {

	private static WakeupUtil mWakeupUtil;
	private WakeLock mWakeLock;

	public static WakeupUtil getInstance() {
		if (mWakeupUtil == null) {
			mWakeupUtil = new WakeupUtil();
		}
		return mWakeupUtil;
	}

	/**
	 * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	 */
	public void acquireWakeLock(Context context) {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "heikuai");
			if (null != mWakeLock) {
				mWakeLock.acquire(10000);
			}
		}
	}

	/**
	 * 释放设备电源锁
	 */
	public void releaseWakeLock() {
		if (null != mWakeLock && mWakeLock.isHeld()) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
}
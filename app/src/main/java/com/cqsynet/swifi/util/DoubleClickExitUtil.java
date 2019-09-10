/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：双击退出应用
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

/**
 * 双击退出应用
 * 
 * @author zhaosiyang
 * 
 */
public class DoubleClickExitUtil implements Runnable {

    private Activity mActivity;
    private Handler mHandler = new Handler();
    private boolean mExit = false;

    public DoubleClickExitUtil(Activity activity) {
        mActivity = activity;
    }

    /**
     * 退出应用
     * 
     * @param isCloseApp
     *            为true时完全关闭应用,为false时仅仅回到桌面
     */
    public void exit(boolean isCloseApp) {
        if (mExit) {
            if (isCloseApp) {
                mActivity.finish();
            } else {
                // 不关闭activity,直接退到桌面
                Intent setIntent = new Intent(Intent.ACTION_MAIN);
                setIntent.addCategory(Intent.CATEGORY_HOME);
                setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(setIntent);
            }
//            // 断开尚WiFi连接
//            NetworkUtil.disableSWiFi(mActivity.getApplicationContext());
        } else {
            mExit = true;
            ToastUtil.showToast(mActivity, "再按一次退出应用");
            mHandler.postDelayed(this, 2000);
        }
    }

    @Override
    public void run() {
        mExit = false;
    }
}

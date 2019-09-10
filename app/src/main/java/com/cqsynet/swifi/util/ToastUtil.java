/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：Toast工具类
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 防止Toast重复刷新,支持自定义Toast
 * 
 * @author zhaosiyang
 * 
 */
public class ToastUtil {
	
	public static Toast mToast;

    public static void showToast(Context context, String text) {
        if (mToast == null) {
            if (context != null) {
                mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            } else {
                return;
            }
        } else {
        	mToast.setText(text);
        	mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void showToast(Context context, int StringResId) {
        if (mToast == null) {
            if (context != null) {
            	mToast = Toast.makeText(context, StringResId, Toast.LENGTH_SHORT);
            } else {
                return;
            }
        } else {
        	mToast.setText(StringResId);
        	mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void cancelToast() {
        if (mToast != null) {
        	mToast.cancel();
        }
    }

}
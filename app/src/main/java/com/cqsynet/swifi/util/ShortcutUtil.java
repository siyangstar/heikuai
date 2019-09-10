/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：创建快捷方式工具类
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcelable;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;

public class ShortcutUtil {

    public static void createShortCut(Context context, int iconResId,
            int appnameResId) {
        Intent intent = new Intent(context.getApplicationContext(),
                context.getClass());
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        Intent shortcutintent = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        // 不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        // 需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                context.getString(appnameResId));
        // 快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(
                context.getApplicationContext(), iconResId);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        // 点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        // 发送广播
        context.sendBroadcast(shortcutintent);
    }

    public static void installShortcut(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                AppConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean addShortcut = sp.getBoolean("addShortcut", true);
        if (addShortcut) {
            ShortcutUtil.createShortCut(context, R.drawable.ic_launcher, R.string.app_name);
            Editor e = sp.edit();
            e.putBoolean("addShortcut", false);
            e.commit();
        }
    }
}
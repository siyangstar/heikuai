/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：短信工具类,监听短信
 *
 *
 * 创建标识：zhaosy 20160315
 */
package com.cqsynet.swifi.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.List;

public class SmsUtil extends ContentObserver {

    private Context mContext;
    private Handler mHandler;
    public static Uri SMS = Uri.parse("content://sms");
    public static Uri SMS_INBOX = Uri.parse("content://sms/inbox");

    public SmsUtil(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        String content = getSmsFromPhone();
        mHandler.obtainMessage(0, content).sendToTarget();
    }

    /**
     * 读取短信验证码
     *
     * @return
     */
    private String getSmsFromPhone() {
        final String[] sms = {""};
        AndPermission.with(mContext)
                .requestCode(100)
                .permission(Manifest.permission.READ_SMS)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        ContentResolver cr = mContext.getContentResolver();
                        String[] projection = new String[]{"body"};//"_id", "address", "person",, "date", "type
                        String where = "date > " + (System.currentTimeMillis() - 10 * 60 * 1000);
                        Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
                        if (cur != null) {
                            while (cur.moveToNext()) {
//                                String number = cur.getString(cur.getColumnIndex("address"));//手机号
//                                String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
                                String body = cur.getString(cur.getColumnIndex("body"));
                                if (body.contains("嘿快") && body.contains("验证码")) {
                                    String verifyCode = body.substring(body.indexOf("为") + 1, body.indexOf("为") + 7);
                                    if (!TextUtils.isEmpty(verifyCode)) {
                                        sms[0] = verifyCode;
                                    }
                                }
                            }
                            cur.close();
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        ToastUtil.showToast(mContext, "读取短信被拒绝");
                    }
                }).start();
        return sms[0];
    }

}
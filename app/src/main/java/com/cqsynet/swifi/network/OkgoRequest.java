/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：使用OkGo发起http请求
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.network;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.LoginActivity;
import com.cqsynet.swifi.model.RequestObject;
import com.cqsynet.swifi.util.DESUtil;
import com.cqsynet.swifi.util.LogUtil;
import com.cqsynet.swifi.util.LogoutUtil;
import com.cqsynet.swifi.util.RSAUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.PostRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OkgoRequest {

    public static final String TAG = "OkgoRequest";
    private static final long UPDATE_KEY_INTERVAL = 3600000;
    private static Context mContext;

    /**
     * 处理纯字符串post请求
     *
     * @param ctx
     * @param url
     * @param requestObject
     * @param callbackIf
     */
    public static void excute(Context ctx, String url, final RequestObject requestObject, final WebServiceIf.IResponseCallback callbackIf) {
        excute(ctx, url, null, requestObject, callbackIf, false);
    }

    /**
     * 处理含有文件上传的请求
     *
     * @param ctx
     * @param url
     */
    public static void excute(Context ctx, String url, final ArrayList<File> files, final RequestObject requestObject, final WebServiceIf.IResponseCallback callbackIf, boolean isMultipart) {
        mContext = ctx;
        Gson gson = new Gson();
        if (Globals.DEBUG) {
            System.out.println("请求:  " + gson.toJson(requestObject));
            if (files != null && files.size() != 0) {
                for (File file : files) {
                    System.out.println("上传文件:  " + file.getPath());
                }
            }
        }

        String tempDesKey = null;
        String tempSign = null;
        String priSignature = null;
        String pubSignature = null;
        if (System.currentTimeMillis() - Globals.g_encrypTime < UPDATE_KEY_INTERVAL && !TextUtils.isEmpty(Globals.g_tempKey)
                && !TextUtils.isEmpty(Globals.g_tempPriSign) && !TextUtils.isEmpty(Globals.g_tempPubSign)) {
            tempDesKey = Globals.g_tempKey;
            priSignature = Globals.g_tempPriSign;
            pubSignature = Globals.g_tempPubSign;
        } else {
            try {
                tempDesKey = DESUtil.initDesKey();
            } catch (Exception e) {
                e.printStackTrace();
            }
            pubSignature = RSAUtil.encrypt(tempDesKey, AppConstants.RSA_PUBLIC_KEY);
            if (!TextUtils.isEmpty(SharedPreferencesInfo.getTagString(ctx, SharedPreferencesInfo.RSA_KEY))) {
                priSignature = RSAUtil.encrypt(tempDesKey, SharedPreferencesInfo.getTagString(ctx, SharedPreferencesInfo.RSA_KEY));
            }
            Globals.g_encrypTime = System.currentTimeMillis();
            Globals.g_tempKey = tempDesKey;
            Globals.g_tempPriSign = priSignature;
            Globals.g_tempPubSign = pubSignature;
        }

        final String tid = requestObject.tid;

        if (AppConstants.IF_REGIST.equals(tid) || AppConstants.IF_FORGET_PSW.equals(tid)
                || AppConstants.IF_GET_VERIFY_CODE.equals(tid) || AppConstants.IF_LOGIN.equals(tid)
                || AppConstants.IF_GET_LAUNCH.equals(tid) || AppConstants.IF_GET_AD.equals(tid)
                || AppConstants.IF_GET_VERSION.equals(tid) || AppConstants.IF_UPDATE_USER_GROUP.equals(tid)
                || AppConstants.IF_GET_URL_RULE.equals(tid)) {
            // 登陆前的接口RSA密钥是初始密钥
            tempSign = pubSignature;
        } else {
            // 登陆后访问的接口RSA密钥是登陆时新生成的密钥
            tempSign = priSignature;
        }
        final String signature = tempSign;
        final String desKey = tempDesKey;
//        if (Globals.DEBUG) {
//            System.out.println("加密前的des密码 = " + desKey);
//            System.out.println("加密后的des密码 = " + signature);
//        }
        String dataStr = gson.toJson(requestObject.data);
        String encryptData = "";
        try {
            encryptData = DESUtil.encrypt(dataStr, desKey);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToast(mContext, "加密失败");
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("msgId", requestObject.msgId);
        map.put("useraccount", requestObject.useraccount);
        map.put("sid", requestObject.sid);
        map.put("tid", requestObject.tid);
        map.put("rid", requestObject.rid);
        map.put("tver", requestObject.tver);
        map.put("sver", requestObject.sver);
        map.put("data", encryptData);
        map.put("signature", signature);
        String jsonStr = gson.toJson(map).trim();

        PostRequest post = OkGo.post(url);
        if (isMultipart) {
            post.isMultipart(true);
            post.addFileParams("fileList", files);
        } else {
            post.isMultipart(false);
        }
        post.tag(mContext)
                .params("json", jsonStr)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                        String s = response.body();
                        //rsa解密出错,单独处理
                        if (s.startsWith(AppConstants.ERROR_CODE_RSA)) {
                            if (Globals.DEBUG) {
                                System.out.println(s);
                            }
                            LogUtil.writeToFile("rsa密钥错误");
                            ToastUtil.showToast(mContext, R.string.error_rsa);
                            LogoutUtil.cleanLoginInfo(mContext);
                            Intent intent = new Intent();
                            intent.setClass(mContext, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            mContext.getApplicationContext().startActivity(intent);
                        } else if (s.startsWith(AppConstants.ERROR_CODE_DES)
                                || s.startsWith(AppConstants.ERROR_CODE_RSA_NULL)
                                || s.startsWith(AppConstants.ERROR_CODE_JSON)
                                || s.startsWith(AppConstants.ERROR_CODE_SPEC_RSA)
                                || s.startsWith(AppConstants.ERROR_CODE_SERVER)
                                || s.startsWith(AppConstants.ERROR_CODE_FILESYS)) {
                            if (Globals.DEBUG) {
                                System.out.println(s);
                            }
                            callbackIf.onErrorResponse();
                        } else {
                            try {
                                String msg = DESUtil.decrypt(s, desKey);
                                if (Globals.DEBUG) {
                                    Log.d(TAG, "响应:  ");
                                    msg = msg.trim();
                                    int index = 0;
                                    int maxLength = 4000;
                                    String sub;
                                    while (index < msg.length()) {
                                        // java的字符不允许指定超过总的长度end
                                        if (msg.length() <= index + maxLength) {
                                            sub = msg.substring(index);
                                        } else {
                                            sub = msg.substring(index, index + maxLength);
                                        }

                                        index += maxLength;
                                        Log.d(TAG, sub.trim());
                                    }
                                }
                                // 调用UI端的回调函数
                                callbackIf.onResponse(msg);
                            } catch (Exception e) {
                                callbackIf.onErrorResponse();
                                if (Globals.DEBUG) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        // 调用UI端注册的错误处理回调
                        callbackIf.onErrorResponse();
                        if (Globals.DEBUG) {
                            response.getException().printStackTrace();
                        }
                    }
                });
    }
}

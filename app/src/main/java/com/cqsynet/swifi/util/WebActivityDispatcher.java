/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 用于分发不同webview的跳转:
 * 1.常规内网内容页面
 * 2.带有requestCode的内网内容页面
 * 3.有赞页面
 *
 * 创建标识：zhaosy 20160729
 */
package com.cqsynet.swifi.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.activity.GalleryActivity;
import com.cqsynet.swifi.activity.TopicActivity;
import com.cqsynet.swifi.activity.WebActivity;
import com.cqsynet.swifi.activity.YouzanWebActivity;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class WebActivityDispatcher {

    private int mType;
    private static final int NORMAL = 0;
    private static final int NORMAL_RESPONSE = 1;
    private static final int YOUZAN = 2;

    /**
     *
     * @param intent
     * @param context
     */
    public void dispatch(Intent intent, Context context) {
        dispatch(intent, context, -1);
    }

    /**
     *
     * @param intent
     * @param context
     * @param requestCode
     */
    public void dispatch(Intent intent, Context context, int requestCode) {
        String url = intent.getStringExtra("url");
        if(url.startsWith("http")) {
            if (url.toLowerCase().contains("&yz=1") || url.toLowerCase().contains("?yz=1")) {
                mType = YOUZAN;
            } else if (requestCode != -1) {
                mType = NORMAL_RESPONSE;
            } else {
                mType = NORMAL;
            }
            switch (mType) {
                case NORMAL:
                    intent.setClass(context, WebActivity.class);
                    context.startActivity(intent);
                    break;
                case NORMAL_RESPONSE:
                    intent.setClass(context, WebActivity.class);
                    ((Activity) context).startActivityForResult(intent, requestCode);
                    break;
                case YOUZAN:
                    intent.setClass(context, YouzanWebActivity.class);
                    if(requestCode != -1) {
                        ((Activity) context).startActivityForResult(intent, requestCode);
                    } else {
                        context.startActivity(intent);
                    }
                    break;
            }
        } else if (url.startsWith("heikuai://gallery")) {
            if (url.split("=").length >= 2) {
                intent.putExtra("id", url.split("=")[1]);
                intent.setClass(context, GalleryActivity.class);
                context.startActivity(intent);
            }
        } else if (url.startsWith("heikuai://topic")) {
            if (url.split("=").length >= 2) {
                intent.putExtra("id", url.split("=")[1]);
                intent.setClass(context, TopicActivity.class);
                context.startActivity(intent);
            }
        } else if (url.startsWith("heikuai://miniProgram")) {
            IWXAPI api = WXAPIFactory.createWXAPI(context, AppConstants.WECHAT_APP_ID);
            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
            url = url.split("\\?")[0]; //去掉参数
            String path = "";
            String[] ary = url.split("=");
            if (ary.length > 1) {
                req.userName = ary[1]; // 填小程序原始id
                if (ary.length > 2) {
                    path = ary[2];
                    if(!TextUtils.isEmpty(path)) {
                        try {
                            path = URLDecoder.decode(path, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
                req.path = path;//拉起小程序页面的可带参路径，不填默认拉起小程序首页
//                req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;// 可选打开 开发版，体验版和正式版
                api.sendReq(req);
            }
        }
    }
}

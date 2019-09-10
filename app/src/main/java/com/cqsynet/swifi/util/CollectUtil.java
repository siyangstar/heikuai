/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：收藏工具类
 *
 *
 * 创建标识：xy 20150706
 */
package com.cqsynet.swifi.util;

import android.content.Context;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.db.CollectCacheDao;
import com.cqsynet.swifi.model.GetCollectResponseObject;
import com.cqsynet.swifi.model.GetCollectResponseObject.GetCollectResponseBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.google.gson.Gson;

public class CollectUtil {
    /**
     * 从网络获取收藏列表
     */
    public static void getCollectData(final Context context) {
        IResponseCallback getCollectCallbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    GetCollectResponseObject responseObj = gson.fromJson(response, GetCollectResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            CollectCacheDao.deleteAll(context);
                            if (responseObj.body.favorListCount != 0) {
                                saveCollect(context, responseObj.body);
                            }
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {

            }
        };
        // 调用接口获取
        WebServiceIf.getCollect(context, getCollectCallbackIf);
    }

    /**
     * 缓存收藏数据至本地
     */
    public static void saveCollect(Context context, GetCollectResponseBody body) {
        if (body.favorListCount > 0) {
            for (int i = body.favorList.size() - 1; i >= 0; i--) {
                CollectCacheDao.insertData(context,
                        body.favorList.get(i).type,
                        body.favorList.get(i).id,
                        body.favorList.get(i).title,
                        body.favorList.get(i).url,
                        body.favorList.get(i).image,
                        body.favorList.get(i).source,
                        body.favorList.get(i).timestamp);
            }
        }
    }
}

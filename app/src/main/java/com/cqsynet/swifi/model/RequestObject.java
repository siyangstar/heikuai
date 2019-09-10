/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：http请求对象基类
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.model;

import java.util.UUID;

import android.content.Context;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

public class RequestObject {
	
	public String msgId; //消息id,每个请求独一无二
    public String useraccount; // 用户账户
    public String sid; // 消息发送端id
    public String rid; // 消息接收服务器id
    public String tid; // 接口id
    public String tver; // 接口版本号
    public String sver; // 发送端版本号
    public RequestData data; // 请求消息体
    public String signature; // 经RSA加密的DES密钥
    
    public RequestObject(Context ctx, String rid, String tid, String tver) {
    	this.msgId = UUID.randomUUID().toString();
    	this.useraccount = SharedPreferencesInfo.getTagString(ctx, SharedPreferencesInfo.ACCOUNT);
    	this.sid = AppConstants.ANDROID_SYS;
    	this.rid = rid;
    	this.tid = tid;
    	this.tver = tver;
    	this.sver = AppUtil.getVersionCode(ctx) + "";
    	data = new RequestData(ctx);
    	this.signature = "";
    }
    
    
    public class RequestData {
    	public RequestHeader header;
    	public RequestBody body;

    	public RequestData(Context ctx) {
    		header = RequestHeader.initHeader(ctx);
    	}
    }
}
/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：启动图片配置实体信息
 *
 *
 * 创建标识：duxl 20150108
 */
package com.cqsynet.swifi.model;

import java.io.File;
import java.io.Serializable;

import android.content.Context;

public class LaunchImageObject implements Serializable {

	/**
	 * 图片保存到本地的文件名<br />
	 * 保存的时候，调用Context.openFileOutput(name, model)，其name值传入该值
	 */
	public String localFileName;
	
	// 广告id
	public String advId;
	
	/**
	 * 图片远程服务器路径
	 */
	public String url;
	
	// 跳转地址
	public String jumpUrl;
	
	/**
	 * 显示图片的开始时间
	 */
	public String startDate;
	
	/**
	 * 显示图片的结束时间
	 */
	public String endDate;
	
	/**
	 * 是否已下载改图片
	 * @param context
	 * @return
	 */
	public boolean exists(Context context) {
		File file = new File(context.getCacheDir().getParent()+"/files/" + localFileName);
		return file.exists();
	}
}

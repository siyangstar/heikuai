/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取图集请求实体类。
 *
 *
 * 创建标识：zhaosy 20150327
 */
package com.cqsynet.swifi.model;

public class GalleryRequestBody extends RequestBody {
	// 要获取的图集id
	public String id;
	// 栏目id
	public String channelId;
}

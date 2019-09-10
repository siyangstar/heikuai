/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻搜索请求类
 *
 *
 * 创建标识：xy 20160407
 */
package com.cqsynet.swifi.model;

public class NewsSearchRequestBody extends RequestBody {
	public String key; // 搜索关键字
	public String start; // 起始资讯id 
}

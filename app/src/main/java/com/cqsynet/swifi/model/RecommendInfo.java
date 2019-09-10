/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：图集对象实体信息
 *
 *
 * 创建标识：zhaosy 20140927
 */
package com.cqsynet.swifi.model;

public class RecommendInfo {
	public String img;
	public String title;
	public String id;
	public String url; //跳转链接,只有类型为页面的时候才有值
	public String type; //1图集,2页面
}
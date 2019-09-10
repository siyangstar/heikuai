/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：wifi使用情况请求对象body类
 *
 *
 * 创建标识：duxl 20141218
 */
package com.cqsynet.swifi.model;

public class WifiUseInfoRequestBody extends RequestBody {

	/**
	 * 已获取的最后一条记录的date,从第一条开始时,为””
	 */
	public String date;
}

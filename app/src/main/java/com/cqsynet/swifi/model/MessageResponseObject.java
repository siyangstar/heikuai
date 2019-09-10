/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取抽奖列表返回类
 *
 *
 * 创建标识：xy 20150918
 */
package com.cqsynet.swifi.model;

public class MessageResponseObject extends BaseResponseObject{

	public MessageResponseBody body;
	
	public class MessageResponseBody {
		 public long date;
	}
}

/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：丢瓶子请求类
 *
 *
 * 创建标识：zhaosy 20161115
 */
package com.cqsynet.swifi.model;

public class SendMessageRequestBody extends RequestBody{
	public String category; //类别 0:漂流瓶   1:好友聊天
	public String type; // 类型
	public String content; // 内容
	public String chatId; //聊天id
	public String msgId; //聊天内容id
	public String friendAccount;
}

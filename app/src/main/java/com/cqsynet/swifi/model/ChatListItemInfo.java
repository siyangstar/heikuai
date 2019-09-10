/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：聊天列表项实体类
 *
 *
 * 创建标识：zhaosy 20161111
 */
package com.cqsynet.swifi.model;

public class ChatListItemInfo {

	public String chatId; //聊天id
	public String myAccount; //我的用户名
	public String userAccount; //对方的用户名
    public String updateTime; //更新时间
	public String type; //最新的一条内容的类型 0文字,1语音,2图片
	public String content; //最新的一条内容
	public String position; //位置(比如两路口)
	public String draft; //草稿
	public String itemType;
}

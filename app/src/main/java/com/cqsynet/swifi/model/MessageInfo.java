/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：消息实体类
 *
 *
 * 创建标识：xy 20160302
 */
package com.cqsynet.swifi.model;

public class MessageInfo {
	public int id; 
	public String account; // 用户账户
	public String title; // 标题
	public String content; // 内容
	public String createTime; // 创建时间
	public String isRead; // 读取状态
	public String url; // 营销类消息携带的url
	public String type; // 类型
    public String msgId; // 消息ID;
	public String contentId; //内容的id(比如图集id,专题id)
//
//	@Override
//	public String toString() {
//		return "MessageInfo [id=" + id + ", account=" + account + ", title="
//				+ title + ", content=" + content + ", createTime=" + createTime
//				+ ", isRead=" + isRead + "]";
//	}
	
}

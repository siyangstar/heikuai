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

import java.io.Serializable;

public class ChatMsgInfo extends RequestBody implements Serializable {

	public String msgId; //该条消息的id(客户端自己生成)
	public String chatId; //聊天id
	public String userAccount; //发送方用户名
    public String receiveAccount; //接收方用户名
	public String type; //类型: 文字,语音,图片,系统提示
    public String content; //消息内容
	public String date; //发送的时间
	public int sendStatus = 1; //消息状态 0:发送成功 1:正在发送 2:发送失败
	public int readStatus = 0; //阅读状态 0:未读 1:已读
	public String position; //对方位置
	public String owner; //该消息记录属于的账号(防止切换账号后出现聊天信息重复异常)
	public String category; //消息分类：好友；漂流瓶
}

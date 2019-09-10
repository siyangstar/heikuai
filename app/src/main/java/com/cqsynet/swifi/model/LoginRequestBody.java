/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：登陆请求对象body类
 *
 *
 * 创建标识：zhaosy 20141103
 */
package com.cqsynet.swifi.model;

public class LoginRequestBody extends RequestBody {
    // 登陆手机号
	public String phoneNo;
	// 登陆密码
	public String password;
	// 公钥
	public String rsaPubKey;
}
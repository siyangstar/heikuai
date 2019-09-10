/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：修改手机号码请求对象body类
 *
 *
 * 创建标识：duxl 20141227
 */
package com.cqsynet.swifi.model;

public class UpdatePhoneRequestBody extends RequestBody {
	
	/**
	 * 手机号码
	 */
	public String phone;
	
	/**
	 * 验证码
	 */
	public String verifyCode;
}

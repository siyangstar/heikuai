/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：修改密码请求对象body类
 *
 *
 * 创建标识：duxl 20141223
 */
package com.cqsynet.swifi.model;

public class UpdatePwdRequestBody extends RequestBody {
	
	/**
	 * 旧密码
	 */
	public String oldPwd;
	
	/**
	 * 新密码
	 */
	public String newPwd;
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：注册请求对象body类
 *
 *
 * 创建标识：luchaowei 20141204
 */
package com.cqsynet.swifi.model;

public class RegistRequestBody extends RequestBody {
    // 注册手机号
    public String phoneNo;
    // 注册密码
    public String password;
    // 注册验证码
    public String verifyCode;
}

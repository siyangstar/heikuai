/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取验证码请求对象body类
 *
 *
 * 创建标识：luchaowei 20141204
 */
package com.cqsynet.swifi.model;

public class VerifyCodeRequestBody extends RequestBody {
    // 要获取验证码的手机号
    public String phoneNo;
    //类型: 0注册 1忘记密码 2修改手机号
    public String type;
}

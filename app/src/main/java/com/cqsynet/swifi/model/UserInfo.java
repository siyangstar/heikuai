/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：用户信息实体类
 *
 *
 * 创建标识：duxl 20141223
 */
package com.cqsynet.swifi.model;

import java.io.Serializable;

public class UserInfo extends RequestBody implements Serializable {

	/**
	 * 登陆返回用户的RSA公钥
	 */
	public String rsaPubKey;
	/**
	 * 用户id
	 */
	public String userAccount;
	
	/**
	 * 头像url
	 */
	public String headUrl;
	
	/**
	 * 昵称
	 */
	public String nickname;
	
	/**
	 * 生日
	 */
	public String birthday;
	
	/**
	 * 性别
	 */
	public String sex;
	
	/**
	 * 职业
	 */
	public String career;
	
	/**
	 * 省市县地区code
	 */
	public String areaCode;
	
	/**
	 * 详细地址
	 */
	public String address;
	
	/**
	 * 人生阶段
	 */
	public String step;

	public String sign; //签名

	public String age; //年龄段

	public String remark; // 备注

	public String setting; //是否需要打开完善个人信息 1要打开,0不打开

	public String lock = "0"; //是否被冻结

	public String lockMsg; //冻结提示语

	public String socialStatus; //社交模块状态 0:可用 1:不可用

    public String socialErrorPage; //社交模块不可用时,需要显示的页面地址
}

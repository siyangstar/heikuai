/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：版本信息实体类
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.model;

import java.io.Serializable;

public class LastVerInfo implements Serializable {
	/**
	 * 显示版本号
	 */
	public int verCode;
	/**
	 * 版本名称
	 */
	public String verName;
	/**
	 * 版本文件大小
	 */
	public String verSize;
	/**
	 * 版本发布时间
	 */
	public String publishDate;
	/**
	 * 版本描述
	 */
	public String des;
	/**
	 * 强制更新最小版本号
	 */
	public int forceMiniVer;
	/**
	 * 下载地址
	 */
	public String downloadUrl;
}

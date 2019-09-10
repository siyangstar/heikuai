/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：统计信息请求类
 *
 *
 * 创建标识：xy 20150728
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class StatisticsRequestBody extends RequestBody{
	public ArrayList<ArrayList<String>> statistics; // 统计信息对象
}

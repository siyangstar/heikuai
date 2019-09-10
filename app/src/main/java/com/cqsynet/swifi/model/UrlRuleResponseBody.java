/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：网址黑白名单返回数据结构对象
 *
 *
 * 创建标识：zhaosy 20170623
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class UrlRuleResponseBody {
		public ArrayList<String> whiteList;
		public ArrayList<BlackUrlObject> blackList;
}

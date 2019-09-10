/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取搜索结果列表或热门推荐返回类
 *
 *
 * 创建标识：xy 20160407
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class NewsSearchOrHotResponseObject extends BaseResponseObject {
	
	public NewsSearchResponseBody body;
	
	public class NewsSearchResponseBody {
		
		public int newsListCount; // 返回结果条数
		
        public ArrayList<NewsItemInfo> newsList; // 搜索结果或热门新闻列表list
		
	}
}

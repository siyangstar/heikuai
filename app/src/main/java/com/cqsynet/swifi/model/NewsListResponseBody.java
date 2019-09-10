/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻列表的volley返回body对象。
 *
 *
 * 创建标识：luchaowei 20141020
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class NewsListResponseBody {
       // 频道新闻列表总条数
       public int newsListCount;
        // 头图的新闻list
       public ArrayList<NewsItemInfo> topList;
       // 新闻列表的list
       public ArrayList<NewsItemInfo> newsList;
       // 新闻更新条数
       public int updateNum;
       // cms的服务器更新时间
       public String updateTime;
       // 浮层广告位
       public NewsItemInfo floatingAdv;
       // 导航
       public ArrayList<NavItemInfo> navList;
}

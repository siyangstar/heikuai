/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻专题列表的volley返回body对象。
 *
 *
 * 创建标识：luchaowei 20141020
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class NewsTopicResponseBody {
    // 频道新闻列表总条数
    public int newsListCount;
    // 专题列表顶部图片url
    public String imgUrl;
    // 顶部头图标题
    public String summary;
    // 新闻列表的list
    public ArrayList<NewsItemInfo> newsList;
    // 专题名称
    public String topicTitle;
    // 分享出去的html页面地址
    public String shareUrl;
    // 分享内容的图片
    public String sharePic;
    // 分享的标题
    public String shareTitle;
    // 分享的内容
    public String shareContent;
}

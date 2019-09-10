/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻缓存对象。
 *
 *
 * 创建标识：luchaowei 20150125
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class NewsCacheObject {
    public String channelId; // 频道id
    public String imgUrl; // 专题列表顶部图片url
    public long date; // 新闻刷新时间
    public String serverDate; // 服务器刷新时间
    public int newsListCount; // 新闻总条数
    public String summary; // 顶部头图标题
    public ArrayList<NewsItemInfo> topList; // 新闻头图列表数据
    public ArrayList<NewsItemInfo> newsList; // 新闻列表数据
    public NewsItemInfo floatingAdv; //浮层广告
    public ArrayList<NavItemInfo> navList; //导航
    public String topicTitle; //专题名称
    public String shareContent;
    public String shareUrl;
    public String sharePic;
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：分类新闻页面列表项对应的数据结构。
 *
 *
 * 创建标识：luchaowei 20141020
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class NewsItemInfo {

    public String id; // 新闻列表项id(若为广告,则为排期id)
    public String type; // 按新闻内容来区分的类型。包括 活动，专题，广告，等类型

    public String title; // 新闻标题
    public String content; // 新闻内容摘要
    public String author; // 新闻来源
    public String template; // 新闻列表项View类型。有右侧图片文字混排，纯文本，一张水平大图，三张并排小图，广告灯类型。
    public String label; //新闻标签
    public String restTime; //活动剩余时间
    public String status; //活动进展状态

    public ArrayList<String> advId; //广告的id
    public ArrayList<String> advTemplate; //广告模板
    public ArrayList<String> advTitle; //广告标题
    public ArrayList<String> advLabel; //广告标签
    public ArrayList<ArrayList<String>> advImg; //广告图片
    public ArrayList<String> advAuthor; //广告来源
    public String plan; //广告轮播顺序

    public ArrayList<String> url; // 点击跳转url
    public ArrayList<String> img; // 要显示的图片列表
}

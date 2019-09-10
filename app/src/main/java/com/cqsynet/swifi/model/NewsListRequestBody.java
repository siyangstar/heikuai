/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取新闻列表请求实体类。
 *
 *
 * 创建标识：luchaowei 20141215
 */
package com.cqsynet.swifi.model;

public class NewsListRequestBody extends RequestBody {
        // 要获取的新闻频道的id
        public String id;
        // 获取哪一条新闻id之后的10条新闻。id是item的id值，不是index。为“”时表示获取最新列表
        public String start;
        // 上次更新时间
        public String updateTime;
}

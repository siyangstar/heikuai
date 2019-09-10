/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻频道的数据类。
 *
 *
 * 创建标识：luchaowei 20141008
 */
package com.cqsynet.swifi.model;

public class ChannelInfo {

    public String id; // 频道id，用于区分不同频道
    public String name; // 频道的名称

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
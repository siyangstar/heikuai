/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻列表item的图片列表对象类。
 *
 *
 * 创建标识：luchaowei 20141215
 */
package com.cqsynet.swifi.model;

public class NewsImgListObject {
    public String imgUrl;  // 图片链接url
    public String url; // 点击跳转url
    public String number; // 第几张？

    public String getImgUrl() {
        return imgUrl;
    }
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
}

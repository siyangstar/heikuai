/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：奖项实体类
 *
 * 创建标识：sayaki 20170321
 */
package com.cqsynet.swifi.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/3/21
 */
public class LotteryInfo {

    /**
     * id : 12432432497
     * title : 标题
     * prizeClass : 二等奖
     * personCount : 74692
     * url : 跳转链接
     */
    private String id;
    private String title;
    private String prizeClass;
    private String personCount;
    private String url;

    public static LotteryInfo objectFromData(String str) {

        return new Gson().fromJson(str, LotteryInfo.class);
    }

    public static List<LotteryInfo> arrayLotteryAvailableInfoFromData(String str) {

        Type listType = new TypeToken<ArrayList<LotteryInfo>>() {
        }.getType();

        return new Gson().fromJson(str, listType);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrizeClass() {
        return prizeClass;
    }

    public void setPrizeClass(String prizeClass) {
        this.prizeClass = prizeClass;
    }

    public String getPersonCount() {
        return personCount;
    }

    public void setPersonCount(String personCount) {
        this.personCount = personCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

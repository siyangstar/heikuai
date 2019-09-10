/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：提交wifi列表请求的body
 *
 *
 * 创建标识：sayaki 20170830
 */
package com.cqsynet.swifi.model;

import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/8/30
 */
public class SubmitWifiListRequestBody extends RequestBody {

    public List<WiFiObject> wifiList;
}

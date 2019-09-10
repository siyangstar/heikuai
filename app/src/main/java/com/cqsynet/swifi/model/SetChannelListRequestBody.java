/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：修改频道列表的请求body对象
 *
 *
 * 创建标识：luchaowei 20141216
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class SetChannelListRequestBody extends RequestBody {
    // 需要修改的频道列表内容 只上传add部分
    public ArrayList<String> add;
}

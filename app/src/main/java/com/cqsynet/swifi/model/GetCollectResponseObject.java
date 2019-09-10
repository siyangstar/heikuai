/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取收藏返回对象类
 *
 *
 * 创建标识：xy 20150619
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;


public class GetCollectResponseObject extends BaseResponseObject {
    public GetCollectResponseBody body;

    public class GetCollectResponseBody {
        // 收藏列表总条数
        public int favorListCount;
        // 收藏列表的list
        public ArrayList<CollectInfo> favorList;
    }

}

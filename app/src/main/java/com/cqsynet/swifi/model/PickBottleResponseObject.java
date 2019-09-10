/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：登陆返回对象类
 *
 *
 * 创建标识：luchaowei 20141210
 */
package com.cqsynet.swifi.model;

public class PickBottleResponseObject extends BaseResponseObject {
    public PickBottleResponseBody body;

    public class PickBottleResponseBody {
        public ChatMsgInfo bottle;
    }

}

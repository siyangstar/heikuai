/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：评论请求body
 *
 *
 * 创建标识：sayaki 20170831
 */
package com.cqsynet.swifi.model;

/**
 * Author: sayaki
 * Date: 2017/8/31
 */
public class CommentListRequestBody extends RequestBody {

    public String id;    // 文章的id
    public String start; // 起始评论的id
}

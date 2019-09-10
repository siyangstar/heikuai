/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：评论回复的响应类
 *
 *
 * 创建标识：zhaosy 20180320
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class CommentReplyResponseObject extends BaseResponseObject {

    public CommentReplyResponseBody body = new CommentReplyResponseBody();

    public class CommentReplyResponseBody {
        public int commentCount;
        public ArrayList<CommentReplyInfo> commentList;
    }
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取一级评论列表响应类
 *
 *
 * 创建标识：zhaosy 20180326
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class LevelOneCommentResponseObject extends BaseResponseObject {

    public LevelOneCommentResponseBody body;

    public class LevelOneCommentResponseBody {
        public ArrayList<CommentInfo> hotComment;
        public ArrayList<CommentInfo> newComment;
    }
}

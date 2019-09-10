/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取二级评论列表响应类
 *
 *
 * 创建标识：zhaosy 20180326
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class LevelTwoCommentResponseObject extends BaseResponseObject {

    public LevelTwoCommentResponseBody body;

    public class LevelTwoCommentResponseBody {
        public String newsId;
        public CommentInfo levelOneComment;
        public ArrayList<CommentInfo> levelTwoCommentList;
    }
}

package com.cqsynet.swifi.model;

/**
 * Author: sayaki
 * Date: 2017/9/11
 */
public class CommentRequestBody extends RequestBody {

    public String type;         // 评论类型
    public String newsId;       // 文章id
    public String levelOneId;   // 一级评论id
    public String levelTwoId;   // 二级评论id
    public String content;      // 评论内容
}

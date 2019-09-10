/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：评论实例
 *
 *
 * 创建标识：sayaki 20170831
 */
package com.cqsynet.swifi.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/8/31
 */
public class CommentInfo implements Parcelable {

    public String id;
    public String userAccount;
    public String nickname;
    public String headUrl;
    public String userLevel;
    public String content;
    public String date;
    public String like;
    public String likeCount;
    public String replyCount;
    public List<ReplyInfo> reply;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.userAccount);
        dest.writeString(this.nickname);
        dest.writeString(this.headUrl);
        dest.writeString(this.userLevel);
        dest.writeString(this.content);
        dest.writeString(this.date);
        dest.writeString(this.like);
        dest.writeString(this.likeCount);
        dest.writeString(this.replyCount);
        dest.writeList(this.reply);
    }

    public CommentInfo() {
    }

    protected CommentInfo(Parcel in) {
        this.id = in.readString();
        this.userAccount = in.readString();
        this.nickname = in.readString();
        this.headUrl = in.readString();
        this.userLevel = in.readString();
        this.content = in.readString();
        this.date = in.readString();
        this.like = in.readString();
        this.likeCount = in.readString();
        this.replyCount = in.readString();
        this.reply = new ArrayList<ReplyInfo>();
        in.readList(this.reply, ReplyInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<CommentInfo> CREATOR = new Parcelable.Creator<CommentInfo>() {
        @Override
        public CommentInfo createFromParcel(Parcel source) {
            return new CommentInfo(source);
        }

        @Override
        public CommentInfo[] newArray(int size) {
            return new CommentInfo[size];
        }
    };
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：回复实例
 *
 *
 * 创建标识：sayaki 20170831
 */
package com.cqsynet.swifi.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: sayaki
 * Date: 2017/8/31
 */
public class ReplyInfo implements Parcelable {

    public String userAccount;
    public String nickname;
    public String content;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userAccount);
        dest.writeString(this.nickname);
        dest.writeString(this.content);
    }

    public ReplyInfo() {
    }

    protected ReplyInfo(Parcel in) {
        this.userAccount = in.readString();
        this.nickname = in.readString();
        this.content = in.readString();
    }

    public static final Parcelable.Creator<ReplyInfo> CREATOR = new Parcelable.Creator<ReplyInfo>() {
        @Override
        public ReplyInfo createFromParcel(Parcel source) {
            return new ReplyInfo(source);
        }

        @Override
        public ReplyInfo[] newArray(int size) {
            return new ReplyInfo[size];
        }
    };
}

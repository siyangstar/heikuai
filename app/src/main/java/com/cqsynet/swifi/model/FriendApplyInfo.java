package com.cqsynet.swifi.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: sayaki
 * Date: 2017/11/29
 */
public class FriendApplyInfo implements Parcelable {

    public String messageId;
    public String userAccount;
    public String nickname;
    public String avatar;
    public String age;
    public String sex;
    public String sign;
    public String content;
    public String date;
    public String readStatus;  // 0: 未读; 1: 已读
    public String replyStatus; // 回应状态; 0: 未处理; 1: 已同意; 2: 已拒绝; 3: 已拉黑

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.messageId);
        dest.writeString(this.userAccount);
        dest.writeString(this.nickname);
        dest.writeString(this.avatar);
        dest.writeString(this.age);
        dest.writeString(this.sex);
        dest.writeString(this.sign);
        dest.writeString(this.content);
        dest.writeString(this.date);
        dest.writeString(this.readStatus);
        dest.writeString(this.replyStatus);
    }

    public FriendApplyInfo() {
    }

    protected FriendApplyInfo(Parcel in) {
        this.messageId = in.readString();
        this.userAccount = in.readString();
        this.nickname = in.readString();
        this.avatar = in.readString();
        this.age = in.readString();
        this.sex = in.readString();
        this.sign = in.readString();
        this.content = in.readString();
        this.date = in.readString();
        this.readStatus = in.readString();
        this.replyStatus = in.readString();
    }

    public static final Parcelable.Creator<FriendApplyInfo> CREATOR = new Parcelable.Creator<FriendApplyInfo>() {
        @Override
        public FriendApplyInfo createFromParcel(Parcel source) {
            return new FriendApplyInfo(source);
        }

        @Override
        public FriendApplyInfo[] newArray(int size) {
            return new FriendApplyInfo[size];
        }
    };
}

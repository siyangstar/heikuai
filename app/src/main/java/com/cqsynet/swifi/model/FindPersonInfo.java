package com.cqsynet.swifi.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: sayaki
 * Date: 2017/11/29
 */
public class FindPersonInfo implements Parcelable {

    public String userAccount;
    public String headUrl;
    public String nickname;
    public String sex;
    public String sign;
    public String age;
    public String remark;
    public String isFriend;          // 0: 不是; 1: 是
    public String areaCode;
    public String isSameLocation;    // 是否在同一个位置;0: 不在; 1:同列车; 2: 同站台; 3: 同线路
    public String distance;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userAccount);
        dest.writeString(this.headUrl);
        dest.writeString(this.nickname);
        dest.writeString(this.sex);
        dest.writeString(this.sign);
        dest.writeString(this.age);
        dest.writeString(this.remark);
        dest.writeString(this.isFriend);
        dest.writeString(this.areaCode);
        dest.writeString(this.isSameLocation);
        dest.writeString(this.distance);
    }

    public FindPersonInfo() {
    }

    protected FindPersonInfo(Parcel in) {
        this.userAccount = in.readString();
        this.headUrl = in.readString();
        this.nickname = in.readString();
        this.sex = in.readString();
        this.sign = in.readString();
        this.age = in.readString();
        this.remark = in.readString();
        this.isFriend = in.readString();
        this.areaCode = in.readString();
        this.isSameLocation = in.readString();
        this.distance = in.readString();
    }

    public static final Parcelable.Creator<FindPersonInfo> CREATOR = new Parcelable.Creator<FindPersonInfo>() {
        @Override
        public FindPersonInfo createFromParcel(Parcel source) {
            return new FindPersonInfo(source);
        }

        @Override
        public FindPersonInfo[] newArray(int size) {
            return new FindPersonInfo[size];
        }
    };
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：漂流瓶的好友界面
 *
 *
 * 创建标识：sayaki 20180210
 */
package com.cqsynet.swifi.activity.social;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.activity.SimpleWebActivity;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendsDao;
import com.cqsynet.swifi.model.GetFriendInfoRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.UserInfoResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.google.gson.Gson;

/**
 * Author: sayaki
 * Date: 2018/2/10
 */
public class BottleFriendInfoActivity extends HkActivity {

    private String mFriendAccount;
    private String mPosition;
    private ImageView mIvAvatar;
    private ImageView mIvSex;
    private TextView mTvLocation;
    private TextView mTvSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottle_friend_info);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mIvAvatar = findViewById(R.id.iv_avatar);
        mIvSex = findViewById(R.id.iv_sex);
        mTvLocation = findViewById(R.id.tv_location);
        mTvSign = findViewById(R.id.tv_sign);
        TextView tvAddFriend = findViewById(R.id.tv_add_friend);
        tvAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BottleFriendInfoActivity.this, FriendApplyActivity.class);
                intent.putExtra("friendAccount", mFriendAccount);
                startActivity(intent);
            }
        });
        TextView tvComplaint = findViewById(R.id.tv_complaint);
        tvComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent complainIntent = new Intent(BottleFriendInfoActivity.this, SimpleWebActivity.class);
                complainIntent.putExtra("title", "投诉");
                complainIntent.putExtra("url", AppConstants.COMPLAIN_PAGE);
                complainIntent.putExtra("friendAccount", mFriendAccount);
                complainIntent.putExtra("chatId", getIntent().getStringExtra("chatId"));
                complainIntent.putExtra("complainType", "chat");
                startActivity(complainIntent);
            }
        });

        mFriendAccount = getIntent().getStringExtra("friendAccount");
        mPosition = getIntent().getStringExtra("position");

        FriendsDao friendsDao = FriendsDao.getInstance(this);
        if (friendsDao.query(mFriendAccount, SharedPreferencesInfo.getTagString(BottleFriendInfoActivity.this, SharedPreferencesInfo.ACCOUNT)) != null) {
            tvAddFriend.setText("已添加好友");
            tvAddFriend.setBackgroundResource(R.drawable.bg_gray_radius);
            tvAddFriend.setEnabled(false);
        } else {
            tvAddFriend.setText("添加好友");
            tvAddFriend.setBackgroundResource(R.drawable.bg_green_radius_selector);
            tvAddFriend.setEnabled(true);
        }
        mTvLocation.setText(mPosition);
        getFriendInfo(mFriendAccount);
    }

    private void getFriendInfo(final String userAccount) {
        final GetFriendInfoRequestBody requestBody = new GetFriendInfoRequestBody();
        requestBody.friendAccount = userAccount;
        WebServiceIf.IResponseCallback getFriendInfoCallbackIf = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    UserInfoResponseObject responseObj = gson.fromJson(response, UserInfoResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            UserInfo userInfo = responseObj.body;
                            userInfo.userAccount = userAccount;
                            //将联系人数据存数据库
                            try {
                                ContactDao contactDao = ContactDao.getInstance(BottleFriendInfoActivity.this);
                                contactDao.saveUser(userInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (!TextUtils.isEmpty(userInfo.headUrl)) {
                                GlideApp.with(BottleFriendInfoActivity.this)
                                        .load(userInfo.headUrl)
                                        .circleCrop()
                                        .into(mIvAvatar);
                            }
                            if ("男".equals(userInfo.sex)) {
                                mIvSex.setImageResource(R.drawable.ic_male);
                            } else if ("女".equals(userInfo.sex)) {
                                mIvSex.setImageResource(R.drawable.ic_female);
                            }
                            if (!TextUtils.isEmpty(userInfo.sign)) {
                                mTvSign.setText(userInfo.sign);
                            }
                        } else {
                            ToastUtil.showToast(BottleFriendInfoActivity.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(BottleFriendInfoActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.getFriendInfo(this, requestBody, getFriendInfoCallbackIf);
    }
}

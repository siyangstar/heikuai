/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：好友申请的操作界面
 *
 *
 * 创建标识：sayaki 20171128
 */
package com.cqsynet.swifi.activity.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.ChatActivity;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.activity.SimpleWebActivity;
import com.cqsynet.swifi.db.ChatListDao;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendApplyDao;
import com.cqsynet.swifi.db.FriendsDao;
import com.cqsynet.swifi.model.ChatListItemInfo;
import com.cqsynet.swifi.model.ChatMsgInfo;
import com.cqsynet.swifi.model.FriendApplyInfo;
import com.cqsynet.swifi.model.FriendsInfo;
import com.cqsynet.swifi.model.ReplyFriendRequestBody;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;

import org.json.JSONException;

/**
 * Author: sayaki
 * Date: 2017/11/28
 */
public class FriendReplyActivity extends HkActivity {

    private static final int SET_REMARK_REQUEST = 0;

    private FriendApplyInfo mFriendApplyInfo;
    private TextView mTvRemark;
    private TextView mTvAgree;
    private TextView mTvRefuse;
    private TextView mTvSendMsg;
    private TextView mTvHint;
    private TextView mTvBlacklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_reply);

        mFriendApplyInfo = getIntent().getParcelableExtra("friendApplyInfo");
        ContactDao contactDao = ContactDao.getInstance(this);
        UserInfo userInfo = contactDao.queryUser(mFriendApplyInfo.userAccount);

        ImageView ivBack = findViewById(R.id.iv_back);
        ImageView ivAvatar = findViewById(R.id.iv_avatar);
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvAge = findViewById(R.id.tv_age);
        ImageView ivSex = findViewById(R.id.iv_sex);
        TextView tvMessage = findViewById(R.id.tv_message);
        mTvRemark = findViewById(R.id.tv_remark);
        mTvAgree = findViewById(R.id.tv_agree);
        mTvRefuse = findViewById(R.id.tv_refuse);
        mTvBlacklist = findViewById(R.id.tv_blacklist);
        mTvHint = findViewById(R.id.tv_hint);
        mTvSendMsg = findViewById(R.id.tvSend_activity_friend_reply);
        TextView tvComplaint = findViewById(R.id.tv_complaint);

        if (!TextUtils.isEmpty(mFriendApplyInfo.avatar)) {
            GlideApp.with(this)
                    .load(mFriendApplyInfo.avatar)
                    .circleCrop()
                    .into(ivAvatar);
        }
        if (!TextUtils.isEmpty(mFriendApplyInfo.nickname)) {
            tvName.setText(mFriendApplyInfo.nickname);
        }
        if (!TextUtils.isEmpty(mFriendApplyInfo.age)) {
            tvAge.setText(mFriendApplyInfo.age);
        }
        if ("男".equals(mFriendApplyInfo.sex)) {
            ivSex.setImageResource(R.drawable.ic_male);
        } else if ("女".equals(mFriendApplyInfo.sex)) {
            ivSex.setImageResource(R.drawable.ic_female);
        }
        if (!TextUtils.isEmpty(mFriendApplyInfo.content)) {
            tvMessage.setText(mFriendApplyInfo.content);
        }
        if (userInfo != null && !TextUtils.isEmpty(userInfo.remark)) {
            mTvRemark.setText(userInfo.remark);
        }

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTvRemark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendReplyActivity.this, RemarkActivity.class);
                intent.putExtra("friendAccount", mFriendApplyInfo.userAccount);
                intent.putExtra("nickname", mFriendApplyInfo.nickname);
                if ("0".equals(mFriendApplyInfo.replyStatus)) {
                    intent.putExtra("action", "set");
                    startActivityForResult(intent, SET_REMARK_REQUEST);
                } else if ("1".equals(mFriendApplyInfo.replyStatus)){
                    intent.putExtra("action", "modify");
                    startActivityForResult(intent, SET_REMARK_REQUEST);
                } else {
                    ToastUtil.showToast(FriendReplyActivity.this, "已拒绝的好友申请不能设置备注哦");
                }
            }
        });

        mTvAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyFriendRequest("0");
            }
        });

        mTvRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyFriendRequest("1");
            }
        });

        mTvBlacklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyFriendRequest("2");
            }
        });

        tvComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFriendApplyInfo != null) {
                    Intent complainIntent = new Intent(FriendReplyActivity.this, SimpleWebActivity.class);
                    complainIntent.putExtra("title", "投诉");
                    complainIntent.putExtra("url", AppConstants.COMPLAIN_PAGE);
                    complainIntent.putExtra("friendAccount", mFriendApplyInfo.userAccount);
                    complainIntent.putExtra("userAccount", SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    complainIntent.putExtra("complainType", "social");
                    startActivity(complainIntent);
                }
            }
        });

        mTvSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendReplyActivity.this, ChatActivity.class);
                intent.putExtra("chatId", "");
                intent.putExtra("userAccount", mFriendApplyInfo.userAccount);
                intent.putExtra("position", "");
                intent.putExtra("owner", SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                intent.putExtra("category", "1");
                startActivity(intent);
            }
        });

        refreshStatus(mFriendApplyInfo.replyStatus);
    }

    private void replyFriendRequest(final String type) {
        final ReplyFriendRequestBody body = new ReplyFriendRequestBody();
        body.messageId = mFriendApplyInfo.messageId;
        body.type = type;
        body.friendAccount = mFriendApplyInfo.userAccount;
        if (!TextUtils.isEmpty(mTvRemark.getText().toString())) {
            body.remark = mTvRemark.getText().toString();
        } else {
            body.remark = "";
        }
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (type.equals("0")) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.userAccount = mFriendApplyInfo.userAccount;
                    userInfo.nickname = mFriendApplyInfo.nickname;
                    userInfo.headUrl = mFriendApplyInfo.avatar;
                    userInfo.age = mFriendApplyInfo.age;
                    userInfo.sex = mFriendApplyInfo.sex;
                    userInfo.remark = body.remark;
                    //保存联系人信息
                    ContactDao contactDao = ContactDao.getInstance(FriendReplyActivity.this);
                    contactDao.saveUser(userInfo);
                    //保存好友信息
                    FriendsDao friendsDao = FriendsDao.getInstance(FriendReplyActivity.this);
                    FriendsInfo friendsInfo = friendsDao.query(mFriendApplyInfo.userAccount, SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    if (friendsInfo == null) {
                        friendsDao.insert(mFriendApplyInfo.userAccount, SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    }
                    //保存好友申请信息
                    FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendReplyActivity.this);
                    FriendApplyInfo friendApplyInfo = friendApplyDao.query(mFriendApplyInfo.userAccount, SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    if (friendApplyInfo != null) {
                        friendApplyInfo.replyStatus = "1";
                        friendApplyDao.insert(friendApplyInfo, SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    }
                    mFriendApplyInfo.replyStatus = "1";
                    //保存可以开始聊天的消息到聊天记录
                    ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(FriendReplyActivity.this);
                    ChatMsgInfo chatMsgInfo = new ChatMsgInfo();
                    chatMsgInfo.msgId = java.util.UUID.randomUUID().toString();
                    chatMsgInfo.type = "0"; //文字信息
                    chatMsgInfo.userAccount = SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT);
                    chatMsgInfo.receiveAccount = mFriendApplyInfo.userAccount;
                    chatMsgInfo.chatId = "";
                    chatMsgInfo.content = "我们已经是好友了,快来聊天吧";
                    chatMsgInfo.sendStatus = 0; //已发送成功
                    chatMsgInfo.readStatus = 1; //已读
                    chatMsgInfo.date = System.currentTimeMillis() + "";
                    chatMsgDao.saveChatMsgItem(chatMsgInfo, "friend");
                    //更新聊天列表数据
                    ChatListItemInfo chatItem = new ChatListItemInfo();
                    chatItem.chatId = chatMsgInfo.chatId;
                    chatItem.type = chatMsgInfo.type;
                    chatItem.content = chatMsgInfo.content;
                    chatItem.updateTime = chatMsgInfo.date;
                    chatItem.userAccount = chatMsgInfo.receiveAccount;
                    chatItem.myAccount = SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT);
                    ChatListDao.getInstance(FriendReplyActivity.this).insert(chatItem);
                    sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
                    sendBroadcast(new Intent(AppConstants.ACTION_MODIFY_REMARK));
                } else if (type.equals("1")) {
                    FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendReplyActivity.this);
                    FriendApplyInfo friendApplyInfo = friendApplyDao.query(mFriendApplyInfo.userAccount,
                            SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    if (friendApplyInfo != null) {
                        friendApplyInfo.replyStatus = "2";
                        friendApplyDao.insert(friendApplyInfo, SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    }
                    mFriendApplyInfo.replyStatus = "2";
                } else if (type.equals("2")) {
                    FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendReplyActivity.this);
                    FriendApplyInfo friendApplyInfo = friendApplyDao.query(mFriendApplyInfo.userAccount,
                            SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    if (friendApplyInfo != null) {
                        friendApplyInfo.replyStatus = "3";
                        friendApplyDao.insert(friendApplyInfo, SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    }
                    mFriendApplyInfo.replyStatus = "3";
                }

                refreshStatus(mFriendApplyInfo.replyStatus);
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(FriendReplyActivity.this, "发生错误，操作未完成");
            }
        };
        WebServiceIf.replyFriendRequest(this, body, callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SET_REMARK_REQUEST) {
                mTvRemark.setText(data.getStringExtra("remark"));
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("replyStatus", mFriendApplyInfo.replyStatus);
        setResult(Activity.RESULT_OK, intent);
        FriendReplyActivity.this.finish();
    }

    /**
     * 刷新页面状态
     */
    private void refreshStatus(String status) {
        if ("0".equals(mFriendApplyInfo.replyStatus)) {
            mTvAgree.setVisibility(View.VISIBLE);
            mTvRefuse.setVisibility(View.VISIBLE);
            mTvSendMsg.setVisibility(View.GONE);
            mTvHint.setVisibility(View.GONE);
        } else if ("1".equals(mFriendApplyInfo.replyStatus)) {
            mTvAgree.setVisibility(View.GONE);
            mTvRefuse.setVisibility(View.GONE);
            mTvSendMsg.setVisibility(View.VISIBLE);
            mTvHint.setVisibility(View.GONE);
            mTvBlacklist.setVisibility(View.GONE);
        } else if ("2".equals(mFriendApplyInfo.replyStatus)) {
            mTvAgree.setVisibility(View.GONE);
            mTvRefuse.setVisibility(View.GONE);
            mTvSendMsg.setVisibility(View.GONE);
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText("已拒绝");
        } else if ("3".equals(mFriendApplyInfo.replyStatus)) {
            mTvAgree.setVisibility(View.GONE);
            mTvRefuse.setVisibility(View.GONE);
            mTvSendMsg.setVisibility(View.GONE);
            mTvHint.setVisibility(View.VISIBLE);
            mTvHint.setText("已拉黑");
        }
    }
}

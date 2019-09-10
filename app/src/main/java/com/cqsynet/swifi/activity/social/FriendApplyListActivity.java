/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：好友申请列表界面
 *
 *
 * 创建标识：sayaki 20180104
 */
package com.cqsynet.swifi.activity.social;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.ChatActivity;
import com.cqsynet.swifi.activity.HkActivity;
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
import com.cqsynet.swifi.view.DeleteDialog;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2018/1/4
 */
public class FriendApplyListActivity extends HkActivity implements
        FriendApplyAdapter.ItemChildListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private static final int FRIEND_REPLY_REQUEST = 0;

    private ListView mListView;
    private LinearLayout mLlHint;
    private TextView mTvEdit;
    private FrameLayout mFlAction;
    private TextView mTvCancel;
    private TextView mTvDelete;
    private DeleteDialog mDeleteDialog;

    private FriendApplyAdapter mAdapter;
    // 全部的好友申请列表
    private List<FriendApplyInfo> mFriendApplyInfos = new ArrayList<>();
    // 选中的好友申请列表
    private List<FriendApplyInfo> mSelectedFriendApplyInfos = new ArrayList<>();
    // 是否是多选模式
    private boolean isMultiMode;
    private MessageReceiver mMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_apply_list);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mListView = findViewById(R.id.list_view);
        mAdapter = new FriendApplyAdapter(this, mFriendApplyInfos);
        mAdapter.setItemChildListener(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mLlHint = findViewById(R.id.ll_hint);
        mTvEdit = findViewById(R.id.tv_edit);
        mTvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMultiMode = true;
                mTvEdit.setVisibility(View.GONE);
                mFlAction.setVisibility(View.VISIBLE);
                mAdapter.setMultiMode(true);
            }
        });
        mFlAction = findViewById(R.id.fl_action);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMultiMode = false;
                mTvEdit.setVisibility(View.VISIBLE);
                mFlAction.setVisibility(View.GONE);
                mAdapter.setMultiMode(false);
                mSelectedFriendApplyInfos.clear();
            }
        });
        mTvDelete = findViewById(R.id.tv_delete);
        mTvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMultiMode = false;
                mTvEdit.setVisibility(View.VISIBLE);
                mFlAction.setVisibility(View.GONE);
                mAdapter.setMultiMode(false);

                FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendApplyListActivity.this);
                for (FriendApplyInfo friendApplyInfo : mSelectedFriendApplyInfos) {
                    mFriendApplyInfos.remove(friendApplyInfo);
                    friendApplyDao.delete(friendApplyInfo.userAccount,
                            SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                }
                mSelectedFriendApplyInfos.clear();

                mAdapter.notifyDataSetChanged();
                updateHint();
                sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
            }
        });

        FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(this);
        mFriendApplyInfos.addAll(friendApplyDao.queryList(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT)));
        mAdapter.notifyDataSetChanged();

        updateHint();

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_SOCKET_PUSH);
        filter.addAction(AppConstants.ACTION_MODIFY_REMARK);
        filter.addAction(AppConstants.ACTION_DELETE_FRIEND);
        registerReceiver(mMessageReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        for (FriendApplyInfo friendApplyInfo : mFriendApplyInfos) {
            friendApplyInfo.readStatus = "1";
            FriendApplyDao.getInstance(this).insert(friendApplyInfo, SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        }
        sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
    }

    @Override
    public void onBackPressed() {
        if (isMultiMode) {
            isMultiMode = false;
            mTvEdit.setVisibility(View.VISIBLE);
            mFlAction.setVisibility(View.GONE);
            mAdapter.setMultiMode(false);
            mSelectedFriendApplyInfos.clear();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAgreeClick(int position) {
        replyFriendRequest(position);
    }

    @Override
    public void onSendMsgClick(int position) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", "");
        intent.putExtra("userAccount", mFriendApplyInfos.get(position).userAccount);
        intent.putExtra("position", "");
        intent.putExtra("owner", SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        intent.putExtra("category", "1");
        startActivity(intent);
    }

    private void replyFriendRequest(final int position) {
        ReplyFriendRequestBody body = new ReplyFriendRequestBody();
        body.messageId = mFriendApplyInfos.get(position).messageId;
        body.type = "0";
        body.friendAccount = mFriendApplyInfos.get(position).userAccount;
        body.remark = "";
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                // 将好友信息插入到联系人表中
                UserInfo userInfo = new UserInfo();
                userInfo.userAccount = mFriendApplyInfos.get(position).userAccount;
                userInfo.nickname = mFriendApplyInfos.get(position).nickname;
                userInfo.headUrl = mFriendApplyInfos.get(position).avatar;
                userInfo.age = mFriendApplyInfos.get(position).age;
                userInfo.sex = mFriendApplyInfos.get(position).sex;
                userInfo.remark = "";
                ContactDao contactDao = ContactDao.getInstance(FriendApplyListActivity.this);
                contactDao.saveUser(userInfo);

                // 在好友表中插入一条新的数据
                FriendsDao friendsDao = FriendsDao.getInstance(FriendApplyListActivity.this);
                FriendsInfo friendsInfo = friendsDao.query(mFriendApplyInfos.get(position).userAccount, SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                if (friendsInfo == null) {
                    friendsDao.insert(mFriendApplyInfos.get(position).userAccount, SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                }

                // 修改好友申请表的数据
                FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendApplyListActivity.this);
                FriendApplyInfo friendApplyInfo = friendApplyDao.query(mFriendApplyInfos.get(position).userAccount, SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                if (friendApplyInfo != null) {
                    friendApplyInfo.replyStatus = "1";
                    friendApplyDao.insert(friendApplyInfo, SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                }
                //保存可以开始聊天的消息到聊天记录
                ChatMsgInfo chatMsgInfo = new ChatMsgInfo();
                chatMsgInfo.msgId = java.util.UUID.randomUUID().toString();
                chatMsgInfo.type = "0"; //文字信息
                chatMsgInfo.userAccount = SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT);
                chatMsgInfo.receiveAccount = mFriendApplyInfos.get(position).userAccount;
                chatMsgInfo.chatId = "";
                chatMsgInfo.content = "我们已经是好友了,快来聊天吧";
                chatMsgInfo.sendStatus = 0; //已发送成功
                chatMsgInfo.readStatus = 1; //已读
                chatMsgInfo.date = System.currentTimeMillis() + "";
                ChatMsgDao.getInstance(FriendApplyListActivity.this).saveChatMsgItem(chatMsgInfo, "friend");
                //更新聊天列表数据
                ChatListItemInfo chatItem = new ChatListItemInfo();
                chatItem.chatId = chatMsgInfo.chatId;
                chatItem.type = chatMsgInfo.type;
                chatItem.content = chatMsgInfo.content;
                chatItem.updateTime = chatMsgInfo.date;
                chatItem.userAccount = chatMsgInfo.receiveAccount;
                chatItem.myAccount = SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT);
                ChatListDao.getInstance(FriendApplyListActivity.this).insert(chatItem);
                sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
                sendBroadcast(new Intent(AppConstants.ACTION_ADD_FRIEND));

                mFriendApplyInfos.clear();
                mFriendApplyInfos.addAll(friendApplyDao.queryList(SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT)));
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(FriendApplyListActivity.this, R.string.social_add_friend_failed);
            }
        };
        WebServiceIf.replyFriendRequest(this, body, callback);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FriendApplyInfo friendApplyInfo = mFriendApplyInfos.get(position);
        if (!isMultiMode) {
            Intent intent = new Intent(this, FriendReplyActivity.class);
            intent.putExtra("friendApplyInfo", friendApplyInfo);
            startActivityForResult(intent, FRIEND_REPLY_REQUEST);
        } else {
            mAdapter.setSelectedFriendApply(friendApplyInfo);
            if (mSelectedFriendApplyInfos.contains(friendApplyInfo)) {
                mSelectedFriendApplyInfos.remove(friendApplyInfo);
            } else {
                mSelectedFriendApplyInfos.add(friendApplyInfo);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        showDeleteDialog(mFriendApplyInfos.get(position));

        return true;
    }

    private void showDeleteDialog(final FriendApplyInfo friendApplyInfo) {
        mDeleteDialog = new DeleteDialog(this, R.style.round_corner_dialog,
                getString(R.string.social_define_delete_friends_apply), new DeleteDialog.MyDialogListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_confirm_collect:
                        mDeleteDialog.dismiss();
                        mFriendApplyInfos.remove(friendApplyInfo);
                        mAdapter.notifyDataSetChanged();
                        updateHint();
                        FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendApplyListActivity.this);
                        friendApplyDao.delete(friendApplyInfo.userAccount,
                                SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                        sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
                        break;
                    case R.id.tv_cancel_collect:
                        mDeleteDialog.dismiss();
                        break;
                }
            }
        });
        mDeleteDialog.show();
    }

    private void updateHint() {
        if (mFriendApplyInfos.size() > 0) {
            mLlHint.setVisibility(View.GONE);
            mTvEdit.setVisibility(View.VISIBLE);
        } else {
            mLlHint.setVisibility(View.VISIBLE);
            mTvEdit.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FRIEND_REPLY_REQUEST) {
                FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(this);
                mFriendApplyInfos.clear();
                mFriendApplyInfos.addAll(friendApplyDao.queryList(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT)));
                mAdapter.notifyDataSetChanged();
                String type = data.getStringExtra("replyStatus");
                if ("2".equals(type) || "3".equals(type)) {
                    sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
                }
            }
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppConstants.ACTION_SOCKET_PUSH.equals(action)) {
                String type = intent.getStringExtra("type");
                if (AppConstants.PUSH_FRIEND_APPLY.equals(type)) {
                    updateFriendApply();
                }
            } else if (AppConstants.ACTION_MODIFY_REMARK.equals(action) || AppConstants.ACTION_DELETE_FRIEND.equals(action)) {
                updateFriendApply();
            }
        }
    }

    private void updateFriendApply() {
        mFriendApplyInfos.clear();
        FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendApplyListActivity.this);
        mFriendApplyInfos.addAll(friendApplyDao.queryList(SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT)));
        mAdapter.notifyDataSetChanged();
        updateHint();
    }
}

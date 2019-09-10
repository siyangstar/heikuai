/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：聊天列表界面
 *
 *
 * 创建标识：sayaki 20171123
 */
package com.cqsynet.swifi.activity.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.ChatActivity;
import com.cqsynet.swifi.activity.MyBottleActivity;
import com.cqsynet.swifi.adapter.ChatListAdapter;
import com.cqsynet.swifi.db.BottleListDao;
import com.cqsynet.swifi.db.ChatListDao;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendApplyDao;
import com.cqsynet.swifi.model.ChatListItemInfo;
import com.cqsynet.swifi.model.FriendApplyInfo;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.view.DeleteDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/11/23
 */
public class ChatListFragment extends Fragment implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView mListView;
    private LinearLayout mLlHint;
    private DeleteDialog mDeleteDialog;

    private ChatListAdapter mAdapter;
    private List<ChatListItemInfo> mChatList = new ArrayList<>();
    private MessageReceiver mMessageReceiver; // 监听推送消息

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mListView = view.findViewById(R.id.list_view);
        mLlHint = view.findViewById(R.id.ll_hint);
        mAdapter = new ChatListAdapter(getActivity(), mChatList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        View searchView = getLayoutInflater().inflate(R.layout.layout_chat_search, null);
        mListView.addHeaderView(searchView);

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_SOCKET_PUSH);
        filter.addAction(AppConstants.ACTION_UPDATE_DRAFT);
        filter.addAction(AppConstants.ACTION_UPDATE_MSG);
        filter.addAction(AppConstants.ACTION_DELETE_FRIEND);
        getActivity().registerReceiver(mMessageReceiver, filter);

        updateChatList();

        return view;
    }

    /**
     * 构造聊天列表数据
     */
    private void updateChatList() {
        mChatList.clear();
        if (SharedPreferencesInfo.getTagBoolean(getActivity(), SharedPreferencesInfo.MSG_BOTTLE, true)) {
            BottleListDao bottleListDao = BottleListDao.getInstance(getActivity());
            List<ChatListItemInfo> bottleListItemInfos = bottleListDao.queryBottleList(SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.ACCOUNT));
            if (bottleListItemInfos != null && bottleListItemInfos.size() > 0) {
                ChatListItemInfo chatListItemInfo = bottleListItemInfos.get(0);
                ChatListItemInfo bottleChat = new ChatListItemInfo();
                bottleChat.chatId = chatListItemInfo.chatId;
                bottleChat.position = "我的瓶子";
                bottleChat.content = chatListItemInfo.content;
                bottleChat.type = chatListItemInfo.type;
                bottleChat.myAccount = chatListItemInfo.myAccount;
                bottleChat.userAccount = chatListItemInfo.userAccount;
                bottleChat.updateTime = chatListItemInfo.updateTime;
                bottleChat.draft = chatListItemInfo.draft;
                bottleChat.itemType = "MyBottle";
                mChatList.add(bottleChat);
            }
        }

        if (SharedPreferencesInfo.getTagBoolean(getActivity(), SharedPreferencesInfo.MSG_FRIEND_APPLY, true)) {
            FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(getActivity());
            List<FriendApplyInfo> friendApplyInfos = friendApplyDao.queryList(SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.ACCOUNT));
            if (friendApplyInfos != null && friendApplyInfos.size() > 0) {
                FriendApplyInfo friendApplyInfo = friendApplyInfos.get(0);
                ChatListItemInfo friendApplyChat = new ChatListItemInfo();
                friendApplyChat.chatId = friendApplyInfo.messageId;
                friendApplyChat.position = "好友申请";
                friendApplyChat.content = friendApplyInfo.content;
                friendApplyChat.type = "0";
                friendApplyChat.myAccount = SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.ACCOUNT);
                friendApplyChat.userAccount = friendApplyInfo.userAccount;
                friendApplyChat.updateTime = friendApplyInfo.date;
                friendApplyChat.draft = "";
                friendApplyChat.itemType = "FriendApply";
                mChatList.add(friendApplyChat);
            }
        }

        ChatListDao chatListDao = ChatListDao.getInstance(getActivity());
        List<ChatListItemInfo> chatListItemInfos = chatListDao.queryList(SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.ACCOUNT));
        if (chatListItemInfos != null) {
            ContactDao contactDao = ContactDao.getInstance(getActivity());
            for (ChatListItemInfo chat : chatListItemInfos) {
                UserInfo userInfo = contactDao.queryUser(chat.userAccount);
                if (userInfo != null) {
                    if (!TextUtils.isEmpty(userInfo.remark)) {
                        chat.position = userInfo.remark;
                    } else {
                        chat.position = userInfo.nickname;
                    }
                }
                chat.itemType = "FriendChat";
            }
            mChatList.addAll(chatListItemInfos);
        }
        mAdapter.notifyDataSetChanged();

        updateHint();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMessageReceiver != null) {
            getActivity().unregisterReceiver(mMessageReceiver);
        }
    }

    /**
     * 显示聊天列表是否为空
     */
    private void updateHint() {
        if (mChatList.size() == 0) {
            mListView.setVisibility(View.GONE);
            mLlHint.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.VISIBLE);
            mLlHint.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            startActivity(new Intent(getActivity(), ChatSearchActivity.class));
        } else {
            ChatListItemInfo chatListItemInfo = mChatList.get(position - 1);
            if ("MyBottle".equals(chatListItemInfo.itemType)) {
                Intent bottle = new Intent(getActivity(), MyBottleActivity.class);
                startActivity(bottle);
            } else if ("FriendApply".equals(chatListItemInfo.itemType)) {
                Intent friendApply = new Intent(getActivity(), FriendApplyListActivity.class);
                startActivity(friendApply);
            } else {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("chatId", chatListItemInfo.chatId);
                intent.putExtra("userAccount", chatListItemInfo.userAccount);
                intent.putExtra("position", chatListItemInfo.position);
                intent.putExtra("category", "1");
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            ChatListItemInfo chatListItemInfo = mChatList.get(position - 1);
            if ("MyBottle".equals(chatListItemInfo.itemType)) {
                showDeleteBottleDialog(chatListItemInfo);
            } else if ("FriendApply".equals(chatListItemInfo.itemType)) {
                showDeleteFriendApplyDialog(chatListItemInfo);
            } else {
                showDeleteMsgDialog(chatListItemInfo);
            }

            return true;
        }
        return false;
    }

    private void showDeleteBottleDialog(final ChatListItemInfo chatListItemInfo) {
        mDeleteDialog = new DeleteDialog(getActivity(), R.style.round_corner_dialog,
                getString(R.string.social_delete_bottle), new DeleteDialog.MyDialogListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_confirm_collect:
                        mDeleteDialog.dismiss();
                        mChatList.remove(chatListItemInfo);
                        mAdapter.notifyDataSetChanged();
                        updateHint();
                        SharedPreferencesInfo.setTagBoolean(getActivity(), SharedPreferencesInfo.MSG_BOTTLE, false);

                        Intent intent = new Intent();
                        intent.putExtra("type", AppConstants.PUSH_BOTTLE);
                        intent.setAction(AppConstants.ACTION_SOCKET_PUSH);
                        getActivity().sendBroadcast(intent);

                        break;
                    case R.id.tv_cancel_collect:
                        mDeleteDialog.dismiss();
                        break;
                }
            }
        });
        mDeleteDialog.show();
    }

    private void showDeleteFriendApplyDialog(final ChatListItemInfo chatListItemInfo) {
        mDeleteDialog = new DeleteDialog(getActivity(), R.style.round_corner_dialog,
                getString(R.string.social_delete_friends_apply), new DeleteDialog.MyDialogListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_confirm_collect:
                        mDeleteDialog.dismiss();
                        mChatList.remove(chatListItemInfo);
                        mAdapter.notifyDataSetChanged();
                        updateHint();
                        SharedPreferencesInfo.setTagBoolean(getActivity(), SharedPreferencesInfo.MSG_FRIEND_APPLY, false);

                        Intent intent = new Intent();
                        intent.putExtra("type", AppConstants.PUSH_FRIEND_APPLY);
                        intent.setAction(AppConstants.ACTION_SOCKET_PUSH);
                        getActivity().sendBroadcast(intent);

                        break;
                    case R.id.tv_cancel_collect:
                        mDeleteDialog.dismiss();
                        break;
                }
            }
        });
        mDeleteDialog.show();
    }

    private void showDeleteMsgDialog(final ChatListItemInfo chatListItemInfo) {
        mDeleteDialog = new DeleteDialog(getActivity(), R.style.round_corner_dialog,
                getString(R.string.social_delete_chat_msg), new DeleteDialog.MyDialogListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_confirm_collect:
                        mDeleteDialog.dismiss();
                        mChatList.remove(chatListItemInfo);
                        mAdapter.notifyDataSetChanged();
                        updateHint();
                        ChatListDao chatListDao = ChatListDao.getInstance(getActivity());
                        chatListDao.delete(chatListItemInfo);
                        ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(getActivity());
                        chatMsgDao.delAllChatMsgFromAccount(chatListItemInfo.userAccount, chatListItemInfo.myAccount);

                        Intent intent = new Intent();
                        intent.putExtra("type", AppConstants.PUSH_CHAT);
                        intent.setAction(AppConstants.ACTION_SOCKET_PUSH);
                        getActivity().sendBroadcast(intent);

                        break;
                    case R.id.tv_cancel_collect:
                        mDeleteDialog.dismiss();
                        break;
                }
            }
        });
        mDeleteDialog.show();
    }

    /**
     * 监听到新信息刷新界面
     */
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppConstants.ACTION_SOCKET_PUSH.equals(action)) {
                String type = intent.getStringExtra("type");
                if (AppConstants.PUSH_BOTTLE.equals(type)
                        || AppConstants.PUSH_FRIEND_APPLY.equals(type)
                        || AppConstants.PUSH_CHAT.equals(type)) {
                    updateChatList();
                    updateHint();
                }
            } else if (AppConstants.ACTION_UPDATE_DRAFT.equals(action)) {
                updateChatList();
                updateHint();
            } else if (AppConstants.ACTION_UPDATE_MSG.equals(action)) {
                updateChatList();
                updateHint();
            } else if (AppConstants.ACTION_DELETE_FRIEND.equals(action)) {
                updateChatList();
                updateHint();
            }
        }
    }
}

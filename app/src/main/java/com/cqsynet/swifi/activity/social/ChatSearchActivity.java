/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：聊天记录搜索界面
 *
 *
 * 创建标识：sayaki 20180116
 */
package com.cqsynet.swifi.activity.social;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.ChatActivity;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.adapter.ChatListAdapter;
import com.cqsynet.swifi.db.ChatListDao;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.model.ChatListItemInfo;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.view.SearchView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2018/1/16
 */
public class ChatSearchActivity extends HkActivity implements SearchView.SearchViewListener {

    private SearchView mSearchView;
    private ListView mLvSearchResult;
    private TextView mTvSearchNoResult;

    private List<ChatListItemInfo> mChatList = new ArrayList<>();
    private ChatListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_search);

        mSearchView = findViewById(R.id.search_view);
        mSearchView.setSearchViewListener(this);
        TextView tvBack = mSearchView.findViewById(R.id.search_btn_back);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mLvSearchResult = findViewById(R.id.lv_search_result);
        mAdapter = new ChatListAdapter(this, mChatList);
        mLvSearchResult.setAdapter(mAdapter);
        mLvSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ChatSearchActivity.this, ChatActivity.class);
                intent.putExtra("chatId", mAdapter.getItem(position).chatId);
                intent.putExtra("userAccount", mAdapter.getItem(position).userAccount);
                intent.putExtra("position", mAdapter.getItem(position).position);
                intent.putExtra("category", "1");
                startActivity(intent);
                finish();
            }
        });
        mTvSearchNoResult = findViewById(R.id.tv_search_no_result);
    }

    @Override
    public void onSearch(String text) {
        if (!TextUtils.isEmpty(text)) {
            searchChatList(text);
        }
    }

    private void searchChatList(String text) {
        mChatList.clear();
        List<UserInfo> userInfos = ContactDao.getInstance(this).queryUserForName(text, text);
        ChatListDao chatListDao = ChatListDao.getInstance(this);
        for (UserInfo userInfo : userInfos) {
            ChatListItemInfo chatListItemInfo = chatListDao.query(userInfo.userAccount, SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
            if(chatListItemInfo == null) {
                continue;
            }
            if (!TextUtils.isEmpty(userInfo.remark)) {
                chatListItemInfo.position = userInfo.remark;
            } else {
                chatListItemInfo.position = userInfo.nickname;
            }
            mChatList.add(chatListItemInfo);
        }
        mAdapter.notifyDataSetChanged();
        if (mChatList.size() > 0) {
            mLvSearchResult.setVisibility(View.VISIBLE);
            mTvSearchNoResult.setVisibility(View.GONE);
        } else {
            mLvSearchResult.setVisibility(View.GONE);
            mTvSearchNoResult.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSearchClick() {

    }
}

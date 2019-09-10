/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：好友搜索界面
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
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendsDao;
import com.cqsynet.swifi.model.FriendsInfo;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.view.SearchView;

import java.util.ArrayList;
import java.util.List;

public class FriendSearchActivity extends HkActivity implements SearchView.SearchViewListener {

    private SearchView mSearchView;
    private ListView mLvSearchResult;
    private TextView mTvSearchNoResult;

    private List<FriendsInfo> mFriendList = new ArrayList<>();
    private FriendsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_search);

        mSearchView = findViewById(R.id.search_view);
        mSearchView.setSearchViewListener(this);
        mSearchView.setHint("请输入好友昵称/备注");
        TextView tvBack = mSearchView.findViewById(R.id.search_btn_back);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mLvSearchResult = findViewById(R.id.lv_search_result);
        mAdapter = new FriendsAdapter(this, mFriendList);
        mLvSearchResult.setAdapter(mAdapter);
        mLvSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FriendSearchActivity.this, PersonInfoActivity.class);
                intent.putExtra("friendAccount", mFriendList.get(position).userAccount);
                intent.putExtra("isFriend", "1");
                intent.putExtra("category", "1"); //1表示是社交,0表示是漂流瓶
                startActivity(intent);
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
        mFriendList.clear();
        List<UserInfo> userInfos = ContactDao.getInstance(this).queryUserForName(text, text);
        List<FriendsInfo> friendsInfos = FriendsDao.getInstance(this).queryList(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        for (UserInfo userInfo : userInfos) {
            for(FriendsInfo friendsInfo : friendsInfos) {
                if(userInfo.userAccount.equals(friendsInfo.userAccount)) {
                    friendsInfo.nickname = userInfo.nickname;
                    friendsInfo.headUrl = userInfo.headUrl;
                    friendsInfo.sex = userInfo.sex;
                    friendsInfo.remark = userInfo.remark;
                    mFriendList.add(friendsInfo);
                }
            }
        }
        mAdapter.notifyDataSetChanged();
        if (mFriendList.size() > 0) {
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

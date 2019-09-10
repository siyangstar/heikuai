/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：好友列表界面
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
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendsDao;
import com.cqsynet.swifi.model.FriendsInfo;
import com.cqsynet.swifi.model.FriendsRequestBody;
import com.cqsynet.swifi.model.FriendsResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.view.SideIndexBar;
import com.github.promeg.pinyinhelper.Pinyin;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/11/23
 */
public class FriendsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView mListFriends;
    private LinearLayout mLlHint;
    private SideIndexBar mSideIndexBar;
    private TextView mTvLetter;

    private FriendsAdapter mAdapter;
    private List<FriendsInfo> mFriends = new ArrayList<>();
    private MessageReceiver mMessageReceiver;
    
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        mListFriends = view.findViewById(R.id.list_friends);
        mLlHint = view.findViewById(R.id.ll_hint);
        mTvLetter = view.findViewById(R.id.tvLetter_fragment_friends);

        mAdapter = new FriendsAdapter(mContext, mFriends);
        mListFriends.setAdapter(mAdapter);
        mListFriends.setOnItemClickListener(this);
        View searchView = getLayoutInflater().inflate(R.layout.layout_chat_search, null);
        mListFriends.addHeaderView(searchView);
        mSideIndexBar = view.findViewById(R.id.index_bar);
        mSideIndexBar.setLetterChangedListener(new SideIndexBar.OnLetterChangedListener() {
            @Override
            public void onChanged(String s, int position) {
                scrollToPosition(s);
            }
        });
        mSideIndexBar.setTextDialog(mTvLetter);

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_SOCKET_PUSH);
        filter.addAction(AppConstants.ACTION_DELETE_FRIEND);
        filter.addAction(AppConstants.ACTION_ADD_FRIEND);
        filter.addAction(AppConstants.ACTION_MODIFY_REMARK);
        mContext.registerReceiver(mMessageReceiver, filter);

        getFriends();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext.unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //解决mContext为null的问题
        mContext = context;
    }

    /**
     * 滚动到对应的好友名称
     * @param s 名称
     */
    private void scrollToPosition(String s) {
        int position = 0;
        for (int i = 0; i < mFriends.size(); i++) {
            String name;
            if (!TextUtils.isEmpty(mFriends.get(i).remark)) {
                name = Pinyin.toPinyin(mFriends.get(i).remark.toCharArray()[0]);
            } else {
                name = Pinyin.toPinyin(mFriends.get(i).nickname.toCharArray()[0]);
            }
            if (name.toUpperCase().startsWith(s)) {
                position = i;
                mListFriends.setSelection(position + 1); //+1是因为有个header搜索
                break;
            }
        }
    }

    private void getFriends() {
        FriendsRequestBody body = new FriendsRequestBody();
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    FriendsResponseObject object = gson.fromJson(response, FriendsResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        mFriends.clear();
                        mFriends.addAll(object.body.userList);

                        Collections.sort(mFriends, new FriendsComparator());

                        mAdapter.notifyDataSetChanged();
                        updateHint();

                        FriendsDao friendsDao = FriendsDao.getInstance(mContext);
                        ContactDao contactDao = ContactDao.getInstance(mContext);
                        friendsDao.deleteAll(SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT));
                        Iterator<FriendsInfo> iterator = mFriends.iterator();
                        while (iterator.hasNext()) {
                            FriendsInfo friendsInfo = iterator.next();
                            friendsDao.insert(friendsInfo.userAccount, SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT));
                            UserInfo userInfo = new UserInfo();
                            userInfo.userAccount = friendsInfo.userAccount;
                            userInfo.nickname = friendsInfo.nickname;
                            userInfo.sex = friendsInfo.sex;
                            userInfo.headUrl = friendsInfo.headUrl;
                            userInfo.remark = friendsInfo.remark;
                            contactDao.saveUser(userInfo);
                        }
                    } else {
                        getFriendsFromDB();
                    }
                } else {
                    getFriendsFromDB();
                }
            }

            @Override
            public void onErrorResponse() {
                getFriendsFromDB();
            }
        };
        WebServiceIf.getFriends(mContext, body, callback);
    }

    private void getFriendsFromDB() {
        FriendsDao friendsDao = FriendsDao.getInstance(mContext);
        List<FriendsInfo> friendsInfos = friendsDao.queryList(SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT));
        if (friendsInfos != null && friendsInfos.size() > 0) {
            ContactDao contactDao = ContactDao.getInstance(mContext);
            for (FriendsInfo friendsInfo : friendsInfos) {
                UserInfo userInfo = contactDao.queryUser(friendsInfo.userAccount);
                if (userInfo != null) {
                    friendsInfo.nickname = userInfo.nickname;
                    friendsInfo.headUrl = userInfo.headUrl;
                    friendsInfo.sex = userInfo.sex;
                    friendsInfo.remark = userInfo.remark;
                    mFriends.add(friendsInfo);
                }
            }
            mAdapter.notifyDataSetChanged();
            updateHint();
        }
    }

    private void updateHint() {
        if (mFriends.size() == 0) {
            mListFriends.setVisibility(View.GONE);
            mLlHint.setVisibility(View.VISIBLE);
        } else {
            mListFriends.setVisibility(View.VISIBLE);
            mLlHint.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            startActivity(new Intent(mContext, FriendSearchActivity.class));
        } else {
            Intent intent = new Intent(mContext, PersonInfoActivity.class);
            intent.putExtra("friendAccount", mFriends.get(position - 1).userAccount);
            intent.putExtra("isFriend", "1");
            intent.putExtra("category", "1"); //1表示是社交,0表示是漂流瓶
            startActivity(intent);
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppConstants.ACTION_DELETE_FRIEND.equals(action)) {
                String userAccount = intent.getStringExtra("userAccount");
                for (FriendsInfo friendsInfo : mFriends) {
                    if (friendsInfo.userAccount.equals(userAccount)) {
                        mFriends.remove(friendsInfo);
                        mAdapter.notifyDataSetChanged();
                        updateHint();
                        break;
                    }
                }
            } else if (AppConstants.ACTION_SOCKET_PUSH.equals(action)) {
                getFriends();
            } else if (AppConstants.ACTION_ADD_FRIEND.equals(action)) {
                getFriends();
            } else if (AppConstants.ACTION_MODIFY_REMARK.equals(action)) {
                getFriends();
            }
        }
    }

    private class FriendsComparator implements Comparator<FriendsInfo> {
        @Override
        public int compare(FriendsInfo o1, FriendsInfo o2) {
            String name1 = TextUtils.isEmpty(o1.remark) ? o1.nickname : o1.remark;
            String name2 = TextUtils.isEmpty(o2.remark) ? o2.nickname : o2.remark;
            String firstLetter1 = Pinyin.toPinyin(name1.toCharArray()[0]).substring(0, 1).toUpperCase();
            String firstLetter2 = Pinyin.toPinyin(name2.toCharArray()[0]).substring(0, 1).toUpperCase();

            if (FriendsAdapter.LETTERS.contains(firstLetter1) && !FriendsAdapter.LETTERS.contains(firstLetter2)) {
                return -1;
            } else if (!FriendsAdapter.LETTERS.contains(firstLetter1) && FriendsAdapter.LETTERS.contains(firstLetter2)) {
                return 1;
            } else {
                return firstLetter1.compareTo(firstLetter2);
            }


//            if (!TextUtils.isEmpty(o1.remark)) {
//                if (!TextUtils.isEmpty(o2.remark)) {
//                    return Pinyin.toPinyin(o1.remark.toCharArray()[0]).compareTo(Pinyin.toPinyin(o2.remark.toCharArray()[0]));
//                } else {
//                    return Pinyin.toPinyin(o1.remark.toCharArray()[0]).compareTo(Pinyin.toPinyin(o2.nickname.toCharArray()[0]));
//                }
//            } else {
//                if (!TextUtils.isEmpty(o2.remark)) {
//                    return Pinyin.toPinyin(o1.nickname.toCharArray()[0]).compareTo(Pinyin.toPinyin(o2.remark.toCharArray()[0]));
//                } else {
//                    return Pinyin.toPinyin(o1.nickname.toCharArray()[0]).compareTo(Pinyin.toPinyin(o2.nickname.toCharArray()[0]));
//                }
//            }
        }
    }
}

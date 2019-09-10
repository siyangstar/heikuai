/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：好友列表的适配器
 *
 *
 * 创建标识：sayaki 20171204
 */
package com.cqsynet.swifi.activity.social;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.FriendsInfo;
import com.github.promeg.pinyinhelper.Pinyin;

import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/12/4
 */
public class FriendsAdapter extends BaseAdapter {

    public static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//    public static final String NUMBERS = "0123456789";

    private Context mContext;
    private List<FriendsInfo> mFriends;

    public FriendsAdapter(Context context, List<FriendsInfo> friends) {
        this.mContext = context;
        this.mFriends = friends;
    }

    @Override
    public int getCount() {
        return mFriends != null ? mFriends.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mFriends != null ? mFriends.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final FriendsInfo friend = mFriends.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_friends, parent, false);
            viewHolder.tvLetter = convertView.findViewById(R.id.tv_letter);
            viewHolder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            viewHolder.tvName = convertView.findViewById(R.id.tv_name);
            viewHolder.ivSex = convertView.findViewById(R.id.iv_sex);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            if (!TextUtils.isEmpty(friend.remark)) {
                viewHolder.tvLetter.setVisibility(View.VISIBLE);
                viewHolder.tvLetter.setText(Pinyin.toPinyin(friend.remark.toCharArray()[0]).substring(0, 1).toUpperCase());
            } else if (!TextUtils.isEmpty(friend.nickname)) {
                viewHolder.tvLetter.setVisibility(View.VISIBLE);
                viewHolder.tvLetter.setText(Pinyin.toPinyin(friend.nickname.toCharArray()[0]).substring(0, 1).toUpperCase());
            }
        } else if (position > 0) {
            String currentName = "";
            String lastName = "";
            if (!TextUtils.isEmpty(friend.remark)) {
                currentName = friend.remark;
            } else if (!TextUtils.isEmpty(friend.nickname)) {
                currentName = friend.nickname;
            } else {
                currentName = "神秘用户";
            }
            if (!TextUtils.isEmpty(mFriends.get(position - 1).remark)) {
                lastName = mFriends.get(position - 1).remark;
            } else if (!TextUtils.isEmpty(mFriends.get(position - 1).nickname)) {
                lastName = mFriends.get(position - 1).nickname;
            }
            String firstLetter = Pinyin.toPinyin(currentName.toCharArray()[0]).substring(0, 1).toUpperCase();
            String oldFisrtLetter = Pinyin.toPinyin(lastName.toCharArray()[0]).substring(0, 1).toUpperCase(); //上一个好友的首字母

            if(!LETTERS.contains(oldFisrtLetter)) {
                //前一个已经是#,所以后面都隐藏
                viewHolder.tvLetter.setVisibility(View.GONE);
            } else {
                if (!LETTERS.contains(firstLetter)) {
                    firstLetter = "#";
                }
                viewHolder.tvLetter.setText(firstLetter);
                if (!firstLetter.equals(oldFisrtLetter)) {
                    viewHolder.tvLetter.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.tvLetter.setVisibility(View.GONE);
                }
            }
        }
        if (!TextUtils.isEmpty(friend.remark)) {
            viewHolder.tvName.setText(friend.remark);
        } else if (!TextUtils.isEmpty(friend.nickname)) {
            viewHolder.tvName.setText(friend.nickname);
        }
        GlideApp.with(mContext)
                .load(friend.headUrl)
                .circleCrop()
                .placeholder(R.drawable.icon_profile_default_round)
                .error(R.drawable.icon_profile_default_round)
                .into(viewHolder.ivAvatar);
        if ("男".equals(friend.sex)) {
            viewHolder.ivSex.setImageResource(R.drawable.ic_male);
        } else if ("女".equals(friend.sex)) {
            viewHolder.ivSex.setImageResource(R.drawable.ic_female);
        }

        return convertView;
    }

    private class ViewHolder {
        private TextView tvLetter;
        private ImageView ivAvatar;
        private TextView tvName;
        private ImageView ivSex;
    }
}

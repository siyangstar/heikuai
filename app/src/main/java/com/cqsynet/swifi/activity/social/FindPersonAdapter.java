/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：找人列表的适配器
 *
 *
 * 创建标识：sayaki 20171218
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
import com.cqsynet.swifi.model.FindPersonInfo;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/12/18
 */
public class FindPersonAdapter extends BaseAdapter {

    private Context mContext;
    private List<FindPersonInfo> mPersonList;

    public FindPersonAdapter(Context context, List<FindPersonInfo> personList) {
        this.mContext = context;
        this.mPersonList = personList;
    }

    @Override
    public int getCount() {
        return mPersonList != null ? mPersonList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mPersonList != null ? mPersonList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final FindPersonInfo person = mPersonList.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_person, parent, false);
            viewHolder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            viewHolder.tvName = convertView.findViewById(R.id.tv_name);
            viewHolder.tvFriend = convertView.findViewById(R.id.tv_friend);
            viewHolder.tvAge = convertView.findViewById(R.id.tv_age);
            viewHolder.ivSex = convertView.findViewById(R.id.iv_sex);
            viewHolder.tvSign = convertView.findViewById(R.id.tv_sign);
            viewHolder.tvDistance = convertView.findViewById(R.id.tv_distance);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (TextUtils.isEmpty(person.remark)) {
            viewHolder.tvName.setText(person.nickname);
        } else {
            viewHolder.tvName.setText(person.remark);
        }
        if ("1".equals(person.isFriend)) {
            viewHolder.tvFriend.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvFriend.setVisibility(View.GONE);
        }
        if ("男".equals(person.sex)) {
            viewHolder.ivSex.setVisibility(View.VISIBLE);
            viewHolder.ivSex.setImageResource(R.drawable.ic_male);
        } else if ("女".equals(person.sex)) {
            viewHolder.ivSex.setVisibility(View.VISIBLE);
            viewHolder.ivSex.setImageResource(R.drawable.ic_female);
        } else {
            viewHolder.ivSex.setVisibility(View.GONE);
        }
        viewHolder.tvAge.setText(person.age);
        viewHolder.tvSign.setText(person.sign);
        if (!TextUtils.isEmpty(person.distance)) {
            int distance = Integer.valueOf(person.distance);
            String distanceStr;
            // 小于10m都算作10m
            if (distance <= 10) {
                distanceStr = "0.01km";
            } else {
                DecimalFormat df = new DecimalFormat("0.00");
                distanceStr = df.format(distance * 1.0f / 1000) + "km";
            }
            viewHolder.tvDistance.setText(distanceStr);
            viewHolder.tvDistance.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvDistance.setVisibility(View.GONE);
        }
        GlideApp.with(mContext)
                .load(person.headUrl)
                .circleCrop()
                .placeholder(R.drawable.icon_profile_default_round)
                .error(R.drawable.icon_profile_default_round)
                .into(viewHolder.ivAvatar);

        return convertView;
    }

    private class ViewHolder {
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvFriend;
        private TextView tvAge;
        private ImageView ivSex;
        private TextView tvSign;
        private TextView tvDistance;
    }
}

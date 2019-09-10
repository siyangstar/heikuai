/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：好友申请列表的适配器
 *
 *
 * 创建标识：sayaki 20180104
 */
package com.cqsynet.swifi.activity.social;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.model.FriendApplyInfo;
import com.cqsynet.swifi.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2018/1/4
 */
public class FriendApplyAdapter extends BaseAdapter {

    private Context mContext;
    private List<FriendApplyInfo> mFriendApplyInfos;
    private List<FriendApplyInfo> mSelectedFriendApplyInfos = new ArrayList<>();
    private ItemChildListener mListener;
    private boolean isMultiMode;

    public FriendApplyAdapter(Context context, List<FriendApplyInfo> friendApplyInfos) {
        this.mContext = context;
        this.mFriendApplyInfos = friendApplyInfos;
    }

    @Override
    public int getCount() {
        return mFriendApplyInfos != null ? mFriendApplyInfos.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mFriendApplyInfos != null ? mFriendApplyInfos.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final FriendApplyInfo friendApply = mFriendApplyInfos.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_friend_apply, parent, false);
            viewHolder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            viewHolder.tvName = convertView.findViewById(R.id.tv_name);
            viewHolder.tvMessage = convertView.findViewById(R.id.tv_message);
            viewHolder.tvAgree = convertView.findViewById(R.id.tv_agree);
            viewHolder.cbDelete = convertView.findViewById(R.id.cb_delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        GlideApp.with(mContext)
                .load(friendApply.avatar)
                .circleCrop()
                .placeholder(R.drawable.icon_profile_default_round)
                .error(R.drawable.icon_profile_default_round)
                .into(viewHolder.ivAvatar);
        ContactDao contactDao = ContactDao.getInstance(mContext);
        UserInfo userInfo = contactDao.queryUser(friendApply.userAccount);
        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.remark)) {
                viewHolder.tvName.setText(userInfo.remark);
            } else {
                viewHolder.tvName.setText(friendApply.nickname);
            }
        } else {
            viewHolder.tvName.setText(friendApply.nickname);
        }
        if (!TextUtils.isEmpty(friendApply.content)) {
            String[] temp = friendApply.content.split("\n");
            if (temp.length > 0) {
                viewHolder.tvMessage.setText(temp[temp.length - 1]);
            } else {
                viewHolder.tvMessage.setText(friendApply.content);
            }
        }
        viewHolder.tvAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("0".equals(friendApply.replyStatus)) {
                    mListener.onAgreeClick(position);
                } else if ("1".equals(friendApply.replyStatus)) {
                    mListener.onSendMsgClick(position);
                }
            }
        });
        if ("0".equals(friendApply.replyStatus)) {
            viewHolder.tvAgree.setText("同意");
            viewHolder.tvAgree.setTextColor(mContext.getResources().getColor(R.color.white));
            viewHolder.tvAgree.setBackgroundResource(R.drawable.bg_green_radius_selector);
            viewHolder.tvAgree.setClickable(true);
        } else if ("1".equals(friendApply.replyStatus)) {
            viewHolder.tvAgree.setText("发消息");
            viewHolder.tvAgree.setTextColor(mContext.getResources().getColor(R.color.text2));
            viewHolder.tvAgree.setBackgroundResource(R.drawable.bg_white_radius_selector);
            viewHolder.tvAgree.setClickable(true);
        } else if ("2".equals(friendApply.replyStatus)) {
            viewHolder.tvAgree.setText("已拒绝");
            viewHolder.tvAgree.setTextColor(mContext.getResources().getColor(R.color.text3));
            viewHolder.tvAgree.setBackgroundColor(mContext.getResources().getColor(R.color.bg_grey));
            viewHolder.tvAgree.setClickable(false);
        } else if ("3".equals(friendApply.replyStatus)) {
            viewHolder.tvAgree.setText("已拉黑");
            viewHolder.tvAgree.setTextColor(mContext.getResources().getColor(R.color.text3));
            viewHolder.tvAgree.setBackgroundColor(mContext.getResources().getColor(R.color.bg_grey));
            viewHolder.tvAgree.setClickable(false);
        }
        viewHolder.cbDelete.setVisibility(isMultiMode ? View.VISIBLE : View.GONE);
        viewHolder.cbDelete.setChecked(mSelectedFriendApplyInfos.contains(friendApply));

        return convertView;
    }

    public void setItemChildListener(ItemChildListener listener) {
        mListener = listener;
    }

    public void setMultiMode(boolean isMultiMode) {
        this.isMultiMode = isMultiMode;
        notifyDataSetChanged();
    }

    public void setSelectedFriendApply(FriendApplyInfo friendApplyInfo) {
        if (mSelectedFriendApplyInfos.contains(friendApplyInfo)) {
            mSelectedFriendApplyInfos.remove(friendApplyInfo);
        } else {
            mSelectedFriendApplyInfos.add(friendApplyInfo);
        }
        notifyDataSetChanged();
    }

    public void clearSelectedFriendApply() {
        mSelectedFriendApplyInfos.clear();
        notifyDataSetChanged();
    }

    public interface ItemChildListener {
        void onAgreeClick(int position);
        void onSendMsgClick(int position);
    }

    private class ViewHolder {
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvMessage;
        private TextView tvAgree;
        private CheckBox cbDelete;
    }
}

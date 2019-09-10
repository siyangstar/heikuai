/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：聊天列表数据适配器
 *
 *
 * 创建标识：zhaosy 20161114
 */
package com.cqsynet.swifi.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.BottleListDao;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendApplyDao;
import com.cqsynet.swifi.model.ChatListItemInfo;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.util.DateUtil;

import java.util.List;

public class ChatListAdapter extends BaseAdapter {

    private Context mContext;
    private List<ChatListItemInfo> mListData;

    public ChatListAdapter(Context cxt, List<ChatListItemInfo> data) {
        super();
        this.mContext = cxt;
        this.mListData = data;
    }

    @Override
    public int getCount() {
        return mListData == null ? 0 : mListData.size();
    }

    @Override
    public ChatListItemInfo getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HolderView holderView;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chatlistitem_layout, parent, false);
            holderView = new HolderView();
            holderView.ivHead = convertView.findViewById(R.id.ivHead_chatlistitem);
            holderView.tvName = convertView.findViewById(R.id.tvName_chatlistitem);
            holderView.tvTime = convertView.findViewById(R.id.tvTime_chatlistitem);
            holderView.tvContent = convertView.findViewById(R.id.tvContent_chatlistitem);
            holderView.tvRedPoint = convertView.findViewById(R.id.tvRedPoint_chatlistitem);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }
        ChatListItemInfo itemObj = getItem(position);
        if ("MyBottle".equals(itemObj.itemType)) {
            holderView.ivHead.setImageResource(R.drawable.ic_my_bottle);
        } else if ("FriendApply".equals(itemObj.itemType)) {
            holderView.ivHead.setImageResource(R.drawable.ic_friend_apply);
        } else {
            ContactDao dao = ContactDao.getInstance(mContext);
            UserInfo userInfo = dao.queryUser(itemObj.userAccount);
            if (userInfo != null) {
                if (!TextUtils.isEmpty(userInfo.headUrl)) {
                    GlideApp.with(mContext)
                            .load(userInfo.headUrl)
                            .centerCrop()
                            .circleCrop()
                            .error(R.drawable.icon_profile_default_round)
                            .into(holderView.ivHead);
                } else {
                    holderView.ivHead.setImageResource(R.drawable.icon_profile_default_round);
                }
            }
        }
        holderView.tvName.setText(itemObj.position);
        ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(mContext);
//        ArrayList<ChatMsgInfo> msgList = chatMsgDao.queryFromChatId(itemObj.chatId, itemObj.myAccount);
//        ChatMsgInfo chatMsgInfo = new ChatMsgInfo();
//        if (msgList.size() > 0) {
//            chatMsgInfo = msgList.get(msgList.size() - 1);
//        }
        if (!TextUtils.isEmpty(itemObj.updateTime)) {
            holderView.tvTime.setText(DateUtil.getRelativeTimeSpanString(Long.parseLong(itemObj.updateTime)));
        }
        if (!TextUtils.isEmpty(itemObj.draft)) {
            holderView.tvTime.setText(DateUtil.getRelativeTimeSpanString(Long.parseLong(itemObj.updateTime)));
            SpannableStringBuilder builder = new SpannableStringBuilder("[草稿]" + itemObj.draft);
            builder.setSpan(new ForegroundColorSpan(Color.RED), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holderView.tvContent.setText(builder);
        } else if ("0".equals(itemObj.type)) {
            String[] temp = itemObj.content.split("\n");
            if (temp.length > 0) {
                holderView.tvContent.setText(temp[temp.length - 1]);
            } else {
                holderView.tvContent.setText(itemObj.content);
            }
        } else if ("1".equals(itemObj.type)) {
            holderView.tvContent.setText("[语音]");
        } else if ("2".equals(itemObj.type)) {
            holderView.tvContent.setText("[图片]");
        }
        //设置红点未读数量
        int count;
        if ("MyBottle".equals(itemObj.itemType)) {
            count = BottleListDao.getInstance(mContext).queryUnReadMsgCount();
        } else if ("FriendApply".equals(itemObj.itemType)) {
            count = FriendApplyDao.getInstance(mContext).queryUnReadApplyCount();
        } else if ("FriendChat".equals(itemObj.itemType)){
            count = chatMsgDao.queryUnReadChatMsgCount(itemObj.userAccount);
        } else {
            count = chatMsgDao.queryUnReadBottleMsgCount(itemObj.chatId);
        }
        if (count < 100) {
            holderView.tvRedPoint.setText(count + "");
        } else {
            holderView.tvRedPoint.setText("···");
        }
        if (count != 0) {
            holderView.tvRedPoint.setVisibility(View.VISIBLE);
        } else {
            holderView.tvRedPoint.setVisibility(View.GONE);
        }

        return convertView;
    }

    private final class HolderView {
        private ImageView ivHead;
        private TextView tvName;
        private TextView tvTime;
        private TextView tvContent;
        private TextView tvRedPoint;
    }
}
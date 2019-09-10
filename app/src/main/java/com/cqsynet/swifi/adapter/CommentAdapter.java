/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：评论适配器
 *
 *
 * 创建标识：sayaki 20170904
 */
package com.cqsynet.swifi.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.CommentInfo;
import com.cqsynet.swifi.model.LikeRequestBody;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.DateUtil;

import org.json.JSONException;

import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/9/4
 */
public class CommentAdapter extends BaseAdapter {

    private Context mContext;
    private List<CommentInfo> mComments;
    private OnItemChildClickListener mListener;
    private boolean mHasHotComment;
    private int mNewCommentPosition;
    private boolean mDisable = false;

    public interface OnItemChildClickListener {
        void onLikeClick(int position, boolean isSuccess);

        void onLikeRepeat();

        void onBrowserAllReply(int position);

        void onReplyClick(int position);
    }

    public CommentAdapter(Context context, List<CommentInfo> comments, OnItemChildClickListener listener) {
        this.mContext = context;
        this.mComments = comments;
        this.mListener = listener;
    }

    @Override
    public int getCount() {
        return mComments != null ? mComments.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final CommentInfo comment = mComments.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false);
            viewHolder.tvLabel = convertView.findViewById(R.id.tv_label);
            viewHolder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            viewHolder.tvUserLevel = convertView.findViewById(R.id.tv_user_level);
            viewHolder.tvNickname = convertView.findViewById(R.id.tv_nickname);
            viewHolder.ivLike = convertView.findViewById(R.id.iv_like);
            viewHolder.tvLike = convertView.findViewById(R.id.tv_like);
            viewHolder.tvContent = convertView.findViewById(R.id.tv_content);
            viewHolder.tvDate = convertView.findViewById(R.id.tv_date);
            viewHolder.tvReply = convertView.findViewById(R.id.tv_reply);
            viewHolder.layoutReplay = convertView.findViewById(R.id.layout_reply);
            viewHolder.tvReply1 = convertView.findViewById(R.id.tv_replay_1);
            viewHolder.tvReply2 = convertView.findViewById(R.id.tv_replay_2);
            viewHolder.tvReply3 = convertView.findViewById(R.id.tv_replay_3);
            viewHolder.tvMoreReply = convertView.findViewById(R.id.tv_more_reply);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (mHasHotComment && position == 0) {
            viewHolder.tvLabel.setText(mContext.getString(R.string.hot_comment));
            viewHolder.tvLabel.setVisibility(View.VISIBLE);
        } else if (position == mNewCommentPosition) {
            viewHolder.tvLabel.setText(mContext.getString(R.string.new_comment));
            viewHolder.tvLabel.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvLabel.setVisibility(View.GONE);
        }

        GlideApp.with(mContext)
                .load(comment.headUrl)
                .circleCrop()
                .error(R.drawable.icon_profile_default_round)
                .into(viewHolder.ivAvatar);
        String nickname;
        if (comment.nickname.length() > 16) {
            nickname = comment.nickname.substring(0, 16) + "...";
        } else {
            nickname = comment.nickname;
        }
        viewHolder.tvNickname.setText(nickname);
        if (TextUtils.isEmpty(comment.userLevel)) {
            viewHolder.tvUserLevel.setVisibility(View.GONE);
        } else {
            viewHolder.tvUserLevel.setVisibility(View.VISIBLE);
            viewHolder.tvUserLevel.setText(comment.userLevel);
        }
        if ("0".equals(comment.like)) {
            viewHolder.ivLike.setImageResource(R.drawable.ic_like);
        } else if ("1".equals(comment.like)) {
            viewHolder.ivLike.setImageResource(R.drawable.ic_liked);
        }
        int likeCount = !TextUtils.isEmpty(comment.likeCount) ? Integer.parseInt(comment.likeCount) : 0;
        if (likeCount == 0) {
            viewHolder.tvLike.setText("0");
        } else if (likeCount % 10000 == 0) {
            viewHolder.tvLike.setText(String.valueOf(likeCount / 10000 + "W"));
        } else if (likeCount % 1000 == 0) {
            viewHolder.tvLike.setText(String.valueOf(likeCount / 1000 + "K"));
        } else if (likeCount > 10000) {
            viewHolder.tvLike.setText(String.valueOf(likeCount / 10000 + "." + likeCount % 10000 / 1000 + "W"));
        } else if (likeCount > 1000) {
            viewHolder.tvLike.setText(String.valueOf(likeCount / 1000 + "." + likeCount % 1000 / 100 + "K"));
        } else {
            viewHolder.tvLike.setText(String.valueOf(likeCount));
        }
        viewHolder.tvContent.setText(comment.content);
        viewHolder.tvDate.setText(DateUtil.getRelativeTimeSpanString(Long.valueOf(comment.date)));
        viewHolder.tvReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onReplyClick(position);
            }
        });
        viewHolder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("0".equals(comment.like) && !mDisable) {
                    viewHolder.tvLike.setText(String.valueOf(Integer.parseInt(comment.likeCount) + 1));
                    viewHolder.ivLike.setImageResource(R.drawable.ic_liked);
                    Animation likeAnimation = AnimationUtils.loadAnimation(mContext, R.anim.like);
                    viewHolder.ivLike.startAnimation(likeAnimation);
                    submitLike(position, comment.id);
                } else if ("1".equals(comment.like) && !mDisable) {
                    if (mListener != null) {
                        mListener.onLikeRepeat();
                    }
                }
            }
        });
        if (Integer.valueOf(comment.replyCount) > 3) {
            viewHolder.tvMoreReply.setText("查看全部" + comment.replyCount + "条回复");
            viewHolder.tvMoreReply.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvMoreReply.setVisibility(View.GONE);
        }
        viewHolder.tvMoreReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onBrowserAllReply(position);
            }
        });
        if (Integer.valueOf(comment.replyCount) > 2 && comment.reply.size() > 2) {
            viewHolder.layoutReplay.setVisibility(View.VISIBLE);

            viewHolder.tvReply1.setVisibility(View.VISIBLE);
            SpannableString replay1 = new SpannableString(comment.reply.get(0).nickname + "：" + comment.reply.get(0).content);
            replay1.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), 0, (comment.reply.get(0).nickname + "：").length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            if (comment.reply.get(0).content.startsWith("回复 ")) {
//                int start = (comment.reply.get(0).nickname + "：").length() + 3;
//                int end =  (comment.reply.get(0).nickname + "：").length() + comment.reply.get(0).content.indexOf("：") + 1;
//                replay1.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            }
            viewHolder.tvReply1.setText(replay1);

            viewHolder.tvReply2.setVisibility(View.VISIBLE);
            SpannableString replay2 = new SpannableString(comment.reply.get(1).nickname + "：" + comment.reply.get(1).content);
            replay2.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), 0, (comment.reply.get(1).nickname + "：").length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            if (comment.reply.get(1).content.startsWith("回复 ")) {
//                int start = (comment.reply.get(1).nickname + "：").length() + 3;
//                int end =  (comment.reply.get(1).nickname + "：").length() + comment.reply.get(1).content.indexOf("：") + 1;
//                replay2.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            }
            viewHolder.tvReply2.setText(replay2);

            viewHolder.tvReply3.setVisibility(View.VISIBLE);
            SpannableString replay3 = new SpannableString(comment.reply.get(2).nickname + "：" + comment.reply.get(2).content);
            replay3.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), 0, (comment.reply.get(2).nickname + "：").length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            if (comment.reply.get(2).content.startsWith("回复 ")) {
//                int start = (comment.reply.get(2).nickname + "：").length() + 3;
//                int end =  (comment.reply.get(2).nickname + "：").length() + comment.reply.get(2).content.indexOf("：") + 1;
//                replay3.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            }
            viewHolder.tvReply3.setText(replay3);
        } else if (Integer.valueOf(comment.replyCount) > 1 && comment.reply.size() > 1) {
            viewHolder.layoutReplay.setVisibility(View.VISIBLE);

            viewHolder.tvReply1.setVisibility(View.VISIBLE);
            SpannableString replay1 = new SpannableString(comment.reply.get(0).nickname + "：" + comment.reply.get(0).content);
            replay1.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), 0, (comment.reply.get(0).nickname + "：").length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            if (comment.reply.get(0).content.startsWith("回复 ")) {
//                int start = (comment.reply.get(0).nickname + "：").length() + 3;
//                int end =  (comment.reply.get(0).nickname + "：").length() + comment.reply.get(0).content.indexOf("：") + 1;
//                replay1.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            }
            viewHolder.tvReply1.setText(replay1);

            viewHolder.tvReply2.setVisibility(View.VISIBLE);
            SpannableString replay2 = new SpannableString(comment.reply.get(1).nickname + "：" + comment.reply.get(1).content);
            replay2.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), 0, (comment.reply.get(1).nickname + "：").length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            if (comment.reply.get(1).content.startsWith("回复 ")) {
//                int start = (comment.reply.get(1).nickname + "：").length() + 3;
//                int end =  (comment.reply.get(1).nickname + "：").length() + comment.reply.get(1).content.indexOf("：") + 1;
//                replay2.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            }
            viewHolder.tvReply2.setText(replay2);
            viewHolder.tvReply3.setVisibility(View.GONE);
        } else if (Integer.valueOf(comment.replyCount) > 0 && comment.reply.size() > 0) {
            viewHolder.layoutReplay.setVisibility(View.VISIBLE);

            SpannableString replay1 = new SpannableString(comment.reply.get(0).nickname + "：" + comment.reply.get(0).content);
            replay1.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), 0, (comment.reply.get(0).nickname + "：").length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            if (comment.reply.get(0).content.startsWith("回复 ")) {
//                int start = (comment.reply.get(0).nickname + "：").length() + 3;
//                int end =  (comment.reply.get(0).nickname + "：").length() + comment.reply.get(0).content.indexOf("：") + 1;
//                replay1.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            }
            viewHolder.tvReply1.setText(replay1);

            viewHolder.tvReply2.setVisibility(View.GONE);
            viewHolder.tvReply3.setVisibility(View.GONE);
        } else {
            viewHolder.layoutReplay.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void submitLike(final int position, String id) {
        LikeRequestBody body = new LikeRequestBody();
        body.id = id;

        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                mListener.onLikeClick(position, true);
            }

            @Override
            public void onErrorResponse() {
                mListener.onLikeClick(position, false);
            }
        };
        WebServiceIf.submitLike(mContext, body, callback);
    }

    public void setHasHotComment() {
        mHasHotComment = true;
    }

    public void setDisable() {
        mDisable = true;
    }

    public void setNewCommentPosition(int position) {
        mNewCommentPosition = position;
    }

    private class ViewHolder {
        private TextView tvLabel;
        private ImageView ivAvatar;
        private TextView tvNickname;
        private TextView tvUserLevel;
        private ImageView ivLike;
        private TextView tvLike;
        private TextView tvContent;
        private TextView tvDate;
        private TextView tvReply;
        private LinearLayout layoutReplay;
        private TextView tvReply1;
        private TextView tvReply2;
        private TextView tvReply3;
        private TextView tvMoreReply;
    }
}

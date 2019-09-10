/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：回复适配器
 *
 *
 * 创建标识：sayaki 20170907
 */
package com.cqsynet.swifi.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.CommentInfo;
import com.cqsynet.swifi.model.LikeRequestBody;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.ToastUtil;

import org.json.JSONException;

import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/9/7
 */
public class ReplyAdapter extends BaseAdapter {

    private Context mContext;
    private List<CommentInfo> mComments;
    private OnItemChildClickListener mListener;
    private boolean mDisable = false;

    public interface OnItemChildClickListener {
        void onLikeClick(int position, boolean isSuccess);

        void onLikeRepeat();
    }

    public ReplyAdapter(Context context, List<CommentInfo> comments, OnItemChildClickListener listener) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_reply, parent, false);
            viewHolder.rootLayout = convertView.findViewById(R.id.root_layout);
            viewHolder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            viewHolder.tvNickname = convertView.findViewById(R.id.tv_nickname);
            viewHolder.tvUserLevel = convertView.findViewById(R.id.tv_user_level);
            viewHolder.ivLike = convertView.findViewById(R.id.iv_like);
            viewHolder.tvLike = convertView.findViewById(R.id.tv_like);
            viewHolder.tvContent = convertView.findViewById(R.id.tv_content);
            viewHolder.tvDate = convertView.findViewById(R.id.tv_date);
            viewHolder.tvReply = convertView.findViewById(R.id.tv_reply);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            viewHolder.rootLayout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        } else {
            viewHolder.rootLayout.setBackgroundResource(R.drawable.bg_reply_item);
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
        int likeCount = !TextUtils.isEmpty(comment.like) ? Integer.parseInt(comment.likeCount) : 0;
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

            }
        });

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
                ToastUtil.showToast(mContext, "点赞失败");
                mListener.onLikeClick(position, false);
            }
        };
        WebServiceIf.submitLike(mContext, body, callback);
    }

    public void setDisable() {
        mDisable = true;
    }

    private class ViewHolder {
        private View rootLayout;
        private ImageView ivAvatar;
        private TextView tvNickname;
        private TextView tvUserLevel;
        private ImageView ivLike;
        private TextView tvLike;
        private TextView tvContent;
        private TextView tvDate;
        private TextView tvReply;
    }
}

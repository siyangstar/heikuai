/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：收藏适配器
 *
 *
 * 创建标识：sayaki 20170420
 */
package com.cqsynet.swifi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.CollectInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/4/20
 */
public class CollectAdapter extends BaseAdapter {

    public static final int TYPE_COLLECT_NEWS = 0;
    public static final int TYPE_COLLECT_GALLERY = 1;
    public static final int TYPE_COLLECT_TOPIC = 2;

    private Context mContext;
    private List<CollectInfo> mCollects;
    private boolean isMultiMode;
    private List<CollectInfo> collectInfos = new ArrayList<>();

    public CollectAdapter(Context context, List<CollectInfo> collects) {
        this.mContext = context;
        this.mCollects = collects;
    }

    @Override
    public int getCount() {
        return mCollects != null ? mCollects.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mCollects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final CollectInfo collect = mCollects.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_collect, parent, false);
            viewHolder.cbDelete = convertView.findViewById(R.id.cb_delete);
            viewHolder.ivCover = convertView.findViewById(R.id.iv_cover);
            viewHolder.tvTitle = convertView.findViewById(R.id.tv_title);
            viewHolder.tvSource = convertView.findViewById(R.id.tv_source);
            viewHolder.tvDate = convertView.findViewById(R.id.tv_date);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.cbDelete.setVisibility(isMultiMode ? View.VISIBLE : View.GONE);
        viewHolder.cbDelete.setChecked(collectInfos.contains(collect));
        GlideApp.with(mContext).load(collect.image)
                .error(R.drawable.image_bg)
                .into(viewHolder.ivCover);
        viewHolder.tvTitle.setText(collect.title);
        viewHolder.tvSource.setText(collect.source);
        viewHolder.tvDate.setText(formatDate(collect.timestamp));

        return convertView;
    }

    private String formatDate(String timestamp) {
        String[] dates = timestamp.split(" ");
        if (dates.length > 0) {
            return dates[0];
        }
        return timestamp;
    }

    public void setMultiMode(boolean isMultiMode) {
        this.isMultiMode = isMultiMode;
        notifyDataSetChanged();
    }

    public void setSelectedCollect(CollectInfo collect) {
        if (collectInfos.contains(collect)) {
            collectInfos.remove(collect);
        } else {
            collectInfos.add(collect);
        }
        notifyDataSetChanged();
    }

    public void clearSelectedCollects() {
        collectInfos.clear();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        CheckBox cbDelete;
        ImageView ivCover;
        TextView tvTitle;
        TextView tvSource;
        TextView tvDate;
    }
}

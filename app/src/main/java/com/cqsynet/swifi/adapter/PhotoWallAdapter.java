/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：PhotoWall中GridView的适配器
 *
 *
 * 创建标识： br 20150210
 */
package com.cqsynet.swifi.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.PhotoWallActivity;

import java.util.ArrayList;

public class PhotoWallAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> imagePathList = null;

    public PhotoWallAdapter(Activity mActivity, ArrayList<String> imagePathList) {
        this.context = mActivity.getApplicationContext();
        this.imagePathList = imagePathList;
    }

    @Override
    public int getCount() {
        return imagePathList == null ? 0 : imagePathList.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePathList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String filePath = (String) getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.photo_wall_item, null);
            holder.imageView = convertView.findViewById(R.id.photo_wall_item_photo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoWallActivity.introduction.chused(filePath);
            }
        });

        GlideApp.with(context)
                .load(filePath)
                .centerCrop()
                .error(R.drawable.image_bg)
                .into(holder.imageView);
        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
    }
}

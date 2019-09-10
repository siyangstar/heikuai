/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：选择相册页面,ListView的adapter
 *
 *
 * 创建标识： br 20150210
 */
package com.cqsynet.swifi.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.PhotoAlbumLVItem;

import java.io.File;
import java.util.ArrayList;

public class PhotoAlbumLVAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PhotoAlbumLVItem> list;

    public PhotoAlbumLVAdapter(Activity mActivity, ArrayList<PhotoAlbumLVItem> list) {
        this.context = mActivity.getApplicationContext();
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.photo_album_lv_item, null);
            holder = new ViewHolder();

            holder.firstImageIV = convertView.findViewById(R.id.select_img_gridView_img);
            holder.pathNameTV = convertView.findViewById(R.id.select_img_gridView_path);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //图片（缩略图）
        String filePath = list.get(position).getFirstImagePath();
        GlideApp.with(context)
                .load(filePath)
                .centerCrop()
                .error(R.drawable.image_bg)
                .into(holder.firstImageIV);
        //文字
        holder.pathNameTV.setText(getPathNameToShow(list.get(position)));

        return convertView;
    }

    private class ViewHolder {
        ImageView firstImageIV;
        TextView pathNameTV;
    }

    /**根据完整路径，获取最后一级路径，并拼上文件数用以显示。*/
    private String getPathNameToShow(PhotoAlbumLVItem item) {
        String absolutePath = item.getPathName();
        int lastSeparator = absolutePath.lastIndexOf(File.separator);
        return absolutePath.substring(lastSeparator + 1) + "(" + item.getFileCount() + ")";
    }

}

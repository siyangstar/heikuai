/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：资讯列表导航栏适配器
 *
 *
 * 创建标识：zhaosy 20161116
 */
package com.cqsynet.swifi.adapter;

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
import com.cqsynet.swifi.model.NavItemInfo;

import java.util.ArrayList;

public class NavItemsAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<NavItemInfo> mList;

	public NavItemsAdapter(Context context, ArrayList<NavItemInfo> list) {
		mContext = context;
		mList = list;
	}

	public ArrayList<NavItemInfo> getData() {
		return mList;
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		}
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		if (mList.get(position) == null) {
			return new NavItemInfo();
		} else {
			return mList.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.navitem_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.ivImage = convertView.findViewById(R.id.ivImage_navitem);
            viewHolder.tvTitle = convertView.findViewById(R.id.tvTitle_navitem);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        NavItemInfo itemObj = mList.get(position);
        if (itemObj != null) {
            if (!TextUtils.isEmpty(itemObj.navImage)) {
				GlideApp.with(mContext)
						.load(itemObj.navImage)
						.centerCrop()
						.error(R.drawable.image_bg)
						.into(viewHolder.ivImage);
            }
            viewHolder.tvTitle.setText(itemObj.navTitle);
        }
        if(mList.size() % 5 == 0) { //5列时隐藏分割线
            convertView.setBackgroundResource(R.color.white);
        } else { //显示分割线
            convertView.setBackgroundResource(R.drawable.nav_bg);
        }
        return convertView;
	}

	/**
	 * 缓存NavItem的View。
	 */
	private class ViewHolder {
		TextView tvTitle;
		ImageView ivImage;
	}

}
/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：自定义搜索框
 *
 *
 * 创建标识：xy 20160321
 */
package com.cqsynet.swifi.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.SearchHistoryDao;
import com.cqsynet.swifi.model.SearchHistoryInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

public class SearchHistoryAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<SearchHistoryInfo> mList;
	private AdapterListener mAdapterListener;
	
	public SearchHistoryAdapter(Context context, ArrayList<SearchHistoryInfo> list) {
		mContext = context;
		mList = list;
	}
	
	public SearchHistoryAdapter(Context context, ArrayList<SearchHistoryInfo> list, 
			AdapterListener listener) {
		mContext = context;
		mList = list;
		mAdapterListener = listener;
	}
	
	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		}
		return mList.size();
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if (mAdapterListener != null && mList.isEmpty()) {
			mAdapterListener.setVisibility();
		} 
	}
	
	@Override
	public Object getItem(int position) {
		if (mList.get(position) == null) {
			return new SearchHistoryInfo();
		} else {
			return mList.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		SearchHistoryViewHolder viewHolder = null;
		final String userAccount = SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.PHONE_NUM);
		if (convertView == null) {
			viewHolder = new SearchHistoryViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_search_history, parent,false);
			viewHolder.tv_history = convertView.findViewById(R.id.search_history_item);
			viewHolder.iv_del = convertView.findViewById(R.id.history_del);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (SearchHistoryViewHolder) convertView.getTag();
		}
		viewHolder.tv_history.setText(mList.get(position).content);
		viewHolder.iv_del.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (position > 0) {
					SearchHistoryDao.getInstance(mContext).deleteSearch(mList.get(position).content, userAccount);
				} else {
					String text = mList.get(0).content;
					SearchHistoryDao.getInstance(mContext).deleteSearch(text, userAccount);
				}
				if (!TextUtils.isEmpty(userAccount)) {
					mList.clear();
					mList.addAll(SearchHistoryDao.getInstance(mContext).getSearchHistory(userAccount));
					notifyDataSetChanged();
		        }
			}
		});
		return convertView;
	}
	
	class SearchHistoryViewHolder {
		TextView tv_history;
		ImageView iv_del;
	}
	
	/**
	 * 是否隐藏搜索历史回掉接口
	 */
	public interface AdapterListener {
		void setVisibility();
	}
}

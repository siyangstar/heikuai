/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：WiFi使用历史列表数据适配器
 *
 *
 * 创建标识：duxl 20150227
 */
package com.cqsynet.swifi.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.WifiUseInfoResponseObject.WiFiUseInfo;

public class WiFiUseHistoryAdapter extends BaseAdapter {

	private Context mContext;
	private List<WiFiUseInfo> mListData;
	
	
	public WiFiUseHistoryAdapter(Context cxt, List<WiFiUseInfo> data) {
		super();
		this.mContext = cxt;
		this.mListData = data;
	}

	@Override
	public int getCount() {
		return mListData == null ? 0 : mListData.size();
	}

	@Override
	public WiFiUseInfo getItem(int position) {
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HolderView holderView = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.wifi_use_history_listitem_layout, null);
			convertView.setBackgroundColor(Color.WHITE);
			holderView = new HolderView();
			holderView.tvDate = convertView.findViewById(R.id.tv1_wifi_use_history_listitem_layout);
			holderView.tvGetTime = convertView.findViewById(R.id.tv2_wifi_use_history_listitem_layout);
			holderView.tvFlow = convertView.findViewById(R.id.tv4_wifi_use_history_listitem_layout);
			
			holderView.tvDate.setTextColor(Color.parseColor("#999999"));
			holderView.tvGetTime.setTextColor(Color.parseColor("#999999"));
			holderView.tvFlow.setTextColor(Color.parseColor("#999999"));
			
			convertView.setTag(holderView);
		} else {
			holderView = (HolderView) convertView.getTag();
		}
		
		WiFiUseInfo itemObj = getItem(position);
		holderView.tvDate.setText(itemObj.date);
		holderView.tvGetTime.setText(itemObj.totalTime + "分钟");
		holderView.tvFlow.setText(itemObj.totalFlow+"MB");
		
		return convertView;
	}

	private final class HolderView {
		public TextView tvDate;
		public TextView tvGetTime;
		public TextView tvFlow;
		
	}
}

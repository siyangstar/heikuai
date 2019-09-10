/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：wifi使用历史
 *
 *
 * 创建标识：duxl 20150227
 */
package com.cqsynet.swifi.activity;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.WiFiUseHistoryAdapter;
import com.cqsynet.swifi.db.FreeWifiUseLogDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.WifiUseInfoRequestBody;
import com.cqsynet.swifi.model.WifiUseInfoResponseObject;
import com.cqsynet.swifi.model.WifiUseInfoResponseObject.WiFiUseInfo;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WiFiUseHistoryActivity extends HkActivity implements OnClickListener {

	private PullToRefreshListView mPTRListView;
	private TextView mTvUseInfo;
	
	private List<WiFiUseInfo> mListData = new ArrayList<WifiUseInfoResponseObject.WiFiUseInfo>();
	private WiFiUseHistoryAdapter mAdapter;
	
	private String mTotalFlow;
	private String mTotalTime;
	private int mCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wifi_use_history);
		TitleBar titleBar = findViewById(R.id.titlebar_wifiusehistory);
		titleBar.setTitle("WiFi使用记录");
		ImageView ivBack = findViewById(R.id.ivBack_titlebar_layout);
		ivBack.setOnClickListener(this);
		mPTRListView = findViewById(R.id.lvList_activity_wifiusehistory);
		mPTRListView.setPullToRefreshOverScrollEnabled(false);
		mTvUseInfo = findViewById(R.id.tvUseInfo_activity_wifiusehistory);
		
		mAdapter = new WiFiUseHistoryAdapter(this, mListData);
		mPTRListView.setAdapter(mAdapter);
		mPTRListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				loadData("");
			}
		});

		mPTRListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				// 当前显示的总条数
				int curCount = mListData.size();
				if (mCount > 0) {
					if (curCount < mCount) {
						// 如果当前显示的总条数小于当app总条数，继续加载
						loadData(mListData.get(curCount - 1).date);
					} else {
						// 已到最底端，停止加载，提示无更多内容
//						ToastUtil.showToast(AppTopicActivity.this, R.string.no_more_item);
					}
				}
			}
		});
		mPTRListView.setOnItemClickListener(null);
		mPTRListView.setRefreshing(false);
		StatisticsDao.saveStatistics(WiFiUseHistoryActivity.this, "wifiHistory", ""); // wifi点击统计	
	}
	
	private void loadData(final String date) {
		final WifiUseInfoRequestBody requestBody = new WifiUseInfoRequestBody();
		requestBody.date = date;
		
		// 调用接口
        WebServiceIf.getWifiUseInfo(this, requestBody, new IResponseCallback() {
			@Override
			public void onResponse(String response) throws JSONException {
                mPTRListView.onRefreshComplete();
				if (response != null) {
					WifiUseInfoResponseObject responseObj = new Gson().fromJson(response, WifiUseInfoResponseObject.class);
					if(responseObj.header != null) {
						if(AppConstants.RET_OK.equals(responseObj.header.ret)) {
							mTotalFlow = responseObj.body.flow;
							mTotalTime = responseObj.body.time;
							try {
								mCount = Integer.parseInt(responseObj.body.count);
							} catch (Exception e) {
								e.printStackTrace();
							}
							if(!TextUtils.isEmpty(mTotalFlow) && !TextUtils.isEmpty(mTotalTime)) {
								setUseInfo(mTotalTime, mTotalFlow);
							} else {
								mTvUseInfo.setText("暂无数据");
							}
							if(responseObj.body.UsageStatistics != null && responseObj.body.UsageStatistics.size() > 0) {
								//更新数据库中今日使用时长
								int todayUse = Integer.parseInt(responseObj.body.UsageStatistics.get(0).totalTime);
								FreeWifiUseLogDao.getInstance(WiFiUseHistoryActivity.this).updateTodayUse(DateUtil.formatTime(new Date(), "yyyy-MM-dd"), todayUse * 60);
							}
							
							if(date.equals("")) {
								mListData.clear();
								mListData.addAll(responseObj.body.UsageStatistics);
							} else {
								mListData.addAll(responseObj.body.UsageStatistics);
							}
							mAdapter.notifyDataSetChanged();
							mPTRListView.getLoadingLayoutProxy().setLastUpdatedLabel(
									"更新于:" + DateUtil.getRelativeTimeSpanString(System.currentTimeMillis()));
						} else if(!TextUtils.isEmpty(responseObj.header.errMsg)) {
							ToastUtil.showToast(WiFiUseHistoryActivity.this, responseObj.header.errMsg);
							
						} else {
							ToastUtil.showToast(WiFiUseHistoryActivity.this, "获取数据失败");
						}
					} else {
						ToastUtil.showToast(WiFiUseHistoryActivity.this, "获取数据失败");
					}
				} else {
					ToastUtil.showToast(WiFiUseHistoryActivity.this, "获取数据失败");
				}
			}
			
			@Override
			public void onErrorResponse() {
				mPTRListView.onRefreshComplete();
				ToastUtil.showToast(WiFiUseHistoryActivity.this, "获取数据失败");
			}
		});
	
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ivBack_titlebar_layout) {
			finish();
		}
	}
	
	private void setUseInfo(String time, String flow) {
		String formatStr = "您累计使用嘿快WiFi网络<font color='#FCF101'>%s</font>分钟\n累计节约<font color='#FCF101'>%sMB</font>流量";
//		String formatStr = "您累计使用嘿快WiFi网络<font color='#FCF101'>%s</font>分钟";
		mTvUseInfo.setText(Html.fromHtml(String.format(formatStr, time, flow)));
	}
}

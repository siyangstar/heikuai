/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：抽奖结果详情页面。
 *
 *
 * 创建标识：xy 20150921
 */
package com.cqsynet.swifi.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.LotteryDetailRequestBody;
import com.cqsynet.swifi.model.LotteryDetailResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.LoadingDialog;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

public class LotteryDetailActivity extends HkActivity  implements OnClickListener{
	
	private TitleBar mTitleBar;
	private TextView tv_title; // 抽奖结果详情的标题
	private TextView tv_prize_class; //奖项
	private TextView tv_rule; // 领奖规则
	private TextView tv_prize; // 奖品详情
	private Dialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDialog = LoadingDialog.createLoadingDialog(this, "请稍候...");
			getLotteryDetail();
	}

	/**
	 * 接口根据id和类型获取抽奖结果详情
	 */
	private void getLotteryDetail () {
		mDialog.show();
		String prizeId = getIntent().getStringExtra("prizeId");
		String type = getIntent().getStringExtra("type");
		if (TextUtils.isEmpty(prizeId)) { // 如果抽奖id为空，则返回
			ToastUtil.showToast(LotteryDetailActivity.this,"获取抽奖结果出错");
		}
		final LotteryDetailRequestBody body = new LotteryDetailRequestBody();
		body.id = prizeId;
		body.type = type;
		IResponseCallback callbackIf = new IResponseCallback() {

			@Override
			public void onResponse(String response) {

				if (response != null && !TextUtils.isEmpty(response)) {
					Gson gson = new Gson();
					try {
						LotteryDetailResponseObject responseObj = gson.fromJson(response, LotteryDetailResponseObject.class);
						ResponseHeader header = responseObj.header;
						if (AppConstants.RET_OK.equals(header.ret)) {
							mDialog.dismiss();
							initLayout();
							setData(responseObj);
						} else {
							mDialog.dismiss();
							ToastUtil.showToast(LotteryDetailActivity.this, "获取奖品详情失败");
							finish();
						}
					} catch (Exception e) {
						e.printStackTrace();
						mDialog.dismiss();
						ToastUtil.showToast(LotteryDetailActivity.this, "获取奖品详情失败");
						finish();
					}
				}
			}

			@Override
			public void onErrorResponse() {
				mDialog.dismiss();
				ToastUtil.showToast(LotteryDetailActivity.this, R.string.request_fail_warning);
				finish();
			}
		};
		WebServiceIf.getLotteryDetail(LotteryDetailActivity.this, body, callbackIf);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_lottery_detail_back:
			onBackPressed();
			break;

		default:
			break;
		}
	}

	/**
	 * 初始化界面
	 */
	protected void initLayout() {
		setContentView(R.layout.activity_lottery_detail);
		mTitleBar = findViewById(R.id.activity_lottery_detail_back);
		mTitleBar.setTitle("奖品详情");
		mTitleBar.setOnClickListener(this);
		tv_title = findViewById(R.id.lottery_title);
		tv_prize_class = findViewById(R.id.lottery_prize_class);
		tv_rule = findViewById(R.id.lottery_rule);
		tv_prize = findViewById(R.id.lottery_prize);
	}

	/**
	 * 更新数据显示
	 * @param responseObj
	 */
	private void setData (LotteryDetailResponseObject responseObj) {
		tv_title.setText(responseObj.body.title);
		tv_prize_class.setText(responseObj.body.prizeClass);
		tv_prize.setText(responseObj.body.prize);
		tv_rule.setText(responseObj.body.rule);
	}
}

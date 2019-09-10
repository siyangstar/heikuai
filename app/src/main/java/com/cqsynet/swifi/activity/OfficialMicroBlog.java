/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：显示官方微博和微信公众号的Activity。
 *
 *
 * 创建标识：br 20150210
 */
package com.cqsynet.swifi.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;

public class OfficialMicroBlog extends HkActivity implements OnClickListener {
	
	private TitleBar mTitleBar;
	private Button mBtBlog;
	private Button mBtWeChat;

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ivBack_titlebar_layout) { // 返回
			finish();
		} else if(v.getId() == R.id.bt_blog) {
			Uri uri = Uri.parse(AppConstants.WEIBO_URL);  
			Intent it = new Intent(Intent.ACTION_VIEW, uri);  
			startActivity(it);
		} else if(v.getId() == R.id.bt_wechat) {
			ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			cbm.setPrimaryClip(ClipData.newPlainText("wechat", AppConstants.WECHAT_ID));
			ToastUtil.showToast(this, "微信账号" + AppConstants.WECHAT_ID + "已复制到剪切板");
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.official_micro_blog);
		mTitleBar = findViewById(R.id.titlebar_activity_official_micro_blog);
		mTitleBar.setTitle("官方微博微信");
		mTitleBar.setLeftIconClickListener(this);
		mBtBlog = findViewById(R.id.bt_blog);
		mBtWeChat = findViewById(R.id.bt_wechat);
		mBtBlog.setOnClickListener(this);
		mBtWeChat.setOnClickListener(this);
	}
}

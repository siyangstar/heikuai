/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：启动图广告
 *
 *
 * 创建标识：zhaosy 20160330
 */
package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.LaunchImageDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.LaunchImageObject;
import com.cqsynet.swifi.util.WebActivityDispatcher;

public class SplashAdvActivity extends HkActivity {
	
	Handler mHdl = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 0:
				SplashAdvActivity.this.finish();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		ImageView ivSplash = findViewById(R.id.ivAdv_splash);
		setSplash(ivSplash); // 设置splash图片
		
		mHdl.sendEmptyMessageDelayed(0, 3000);
	}
	
	/**
	 * @Description: 显示启动图片
	 * @param view
	 *            需要显示图片的view
	 * 
	 */
	private void setSplash(ImageView view) {
		LaunchImageDao mLaunchImageDao = new LaunchImageDao(this);
		final LaunchImageObject launchImg = mLaunchImageDao.getRandomImage();
		if (launchImg != null) {
			GlideApp.with(this)
					.load(launchImg.url)
					.centerInside()
					.error(R.color.transparent)
					.into(view);
			StatisticsDao.saveStatistics(this, "advView", launchImg.advId); // 启动图广告浏览统计
					
			if(!TextUtils.isEmpty(launchImg.jumpUrl)) {
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						StatisticsDao.saveStatistics(SplashAdvActivity.this, "advClick", launchImg.advId); // 启动图广告点击统计
						Intent webIntent = new Intent();
						webIntent.putExtra("url", launchImg.jumpUrl);
						webIntent.putExtra("mainType", "0");
						webIntent.putExtra("subType", "0");
						webIntent.putExtra("from", "adv");
						WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
						webDispatcher.dispatch(webIntent, SplashAdvActivity.this);
					}
				});
			}
		}
	}
}
/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：关于我们页面
 *
 *
 * 创建标识：duxl 20141223
 */
package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;

/**
 * 关于我们页面
 * @author duxl
 *
 */
public class AboutActivity extends HkActivity implements OnClickListener {

	private TitleBar mTitleBar;
    private long mFirstClickTime;
    private long mNextClickTime;
    private int mClickCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		mTitleBar = findViewById(R.id.titlebar_activity_about);
		mTitleBar.setTitle("关于我们");
		mTitleBar.setLeftIconClickListener(this);
        findViewById(R.id.ivLogo_activity_about).setOnClickListener(this);
		
		TextView tvVersion = findViewById(R.id.tvVersion_activity_about);
		try {
			PackageManager pm = getPackageManager();
    		PackageInfo pi = pm.getPackageInfo(getApplicationContext().getPackageName(), 0);
    		tvVersion.setText(pi.versionName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ivBack_titlebar_layout) { // 返回
            finish();
        } else if(v.getId() == R.id.ivLogo_activity_about) {
            mNextClickTime = System.currentTimeMillis();
            if(mNextClickTime - mFirstClickTime < 500) {
                mFirstClickTime = mNextClickTime;
                mClickCount++;
                if(mClickCount == 5) {
                    ToastUtil.showToast(this, "开发者模式");
                    Intent intent = new Intent(this, DevModeActivity.class);
                    startActivity(intent);
                }
            } else {
                mClickCount = 1;
                mFirstClickTime = System.currentTimeMillis();
            }
        }
	}
	
	/**
	 * 功能介绍
	 */
	public void functionIntroduce(View v) {
		Intent intent = new Intent();
		intent.setClass(AboutActivity.this, UserGuideActivity.class);
		intent.putExtra("from", "about");
		startActivity(intent);
	}
	
	/**
	 * 官方微博
	 */
	public void weibo(View v) {
		Intent intent = new Intent(this, OfficialMicroBlog.class);
		startActivity(intent);
	}
	
	/**
	 * 企业主页
	 * 
	 */
	public void enterPage(View v) {
		Intent intent = new Intent(this, SimpleWebActivity.class);
		intent.putExtra("title", "企业主页");
		intent.putExtra("url", AppConstants.ABOUT_PAGE);
		startActivity(intent);
	}
	
	/**
	 * 用户注册条款
	 */
	public void protocol(View v) {
		Intent intent = new Intent(this, SimpleWebActivity.class);
		intent.putExtra("title", "用户注册及服务协议");
		intent.putExtra("url", AppConstants.AGREEMENT_PAGE);
		startActivity(intent);
	}
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：有赞页面
 *
 *
 * 创建标识：zhaosy 20150420
 */
package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.youzan.sdk.YouzanToken;
import com.youzan.sdk.web.plugin.YouzanBrowser;

public class YouzanWebActivity extends HkActivity {

    private YouzanBrowser mWebView;
	private ImageButton mBtnBack;
	private ImageButton mBtnClose;
	private ProgressBar mProgress;
	private String mUrl;
	private String mFlag; //是不是上网启动图过来的
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_youzanweb);
		mBtnBack = findViewById(R.id.btnBack_youzanweb);
		mBtnClose = findViewById(R.id.btnClose_youzanweb);
		mProgress = findViewById(R.id.progress_youzanweb);
		mBtnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	if(mWebView.canGoBack()) {
            		mWebView.goBack();
            	} else {
            		finish();
            	}
            }
        });
		
		mBtnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	finish();
            }
        });
		
		mWebView = findViewById(R.id.wv_youzanweb);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.getSettings().setTextZoom(100);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWebView.getSettings().setUseWideViewPort(true);

        syncYouzan();

		mFlag = getIntent().getStringExtra("flag");
		mUrl = getIntent().getStringExtra("url");
		mWebView.loadUrl(mUrl);
	}

    private void syncYouzan() {
        String key = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.YOUZAN_COOKIE_KEY);
        String value = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.YOUZAN_COOKIE_VALUE);
        String token = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.YOUZAN_ACCESS_TOKEN);
        YouzanToken youzanToken = new YouzanToken();
        youzanToken.setCookieKey(key);
        youzanToken.setCookieValue(value);
        youzanToken.setAccessToken(token);
        mWebView.sync(youzanToken);
    }

    @Override
	public void onBackPressed() {
		if(mWebView.canGoBack()) {
    		mWebView.goBack();
    	} else {
			if ("fullAdv".equals(mFlag)) {
				Intent fullAdv = new Intent(this, FullAdvActivity.class);
				fullAdv.putExtra("source", "WifiActivity");
				fullAdv.putExtra("accessType", getIntent().getIntExtra("accessType", 0));
				startActivity(fullAdv);
				setResult(RESULT_OK); //splash页可以继续跳转
			}
			finish();
    	}
	}
}
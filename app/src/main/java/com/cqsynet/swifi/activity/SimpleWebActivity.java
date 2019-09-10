/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：所有文本显示页面
 *
 *
 * 创建标识：br 20150210
 */
package com.cqsynet.swifi.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SimpleWebActivity extends HkActivity implements OnClickListener {
	
	private TitleBar mTitleBar;
	private String mTitle;
	private WebView mWebView;
	private ImageView mIvBlankPage;
	private ProgressBar mProgress;
	private boolean mIsError = false;
    private String mUrl;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.ivBack_titlebar_layout) {
			finish();
		}
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_text);
		
		mTitleBar = findViewById(R.id.titlebar_activity_all_text);
		mTitleBar.setLeftIconClickListener(this);
		mIvBlankPage = findViewById(R.id.ivBlank_activity_all_text);
		mProgress = findViewById(R.id.progress_activity_all_text);
		mWebView = findViewById(R.id.webview_activity_all_text);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.getSettings().setTextZoom(100);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.toLowerCase().contains("heikuai.com") || url.toLowerCase().contains("cqsynet.com")) {
                    if (!url.contains("userAccount=")) {
                        if (url.contains("?")) {
                            url = url + "&userAccount=" + SharedPreferencesInfo.getTagString(SimpleWebActivity.this, SharedPreferencesInfo.ACCOUNT);
                        } else {
                            url = url + "?userAccount=" + SharedPreferencesInfo.getTagString(SimpleWebActivity.this, SharedPreferencesInfo.ACCOUNT);
                        }
                    }
                }
        		view.loadUrl(url);
                return true;
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            	super.onPageStarted(view, url, favicon);
            	mProgress.setVisibility(View.VISIBLE);
            	mWebView.setVisibility(View.VISIBLE);
            }
            
		    @Override
		    public void onPageFinished(WebView view, String url) {
		    	super.onPageFinished(view, url);
            	mProgress.setVisibility(View.GONE);
            	if(mIsError) {
            		mIvBlankPage.setVisibility(View.VISIBLE);
            	} else {
            		mIvBlankPage.setVisibility(View.GONE);
            	}
		    }
		    
		    @Override
		    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		    	super.onReceivedError(view, errorCode, description, failingUrl);
            	mProgress.setVisibility(View.GONE);
            	ToastUtil.showToast(SimpleWebActivity.this, R.string.request_fail_warning);
            	mIsError = true;
		    }
		});

		mWebView.addJavascriptInterface(new Object() {
			/**
			 * 向页面发送投诉类型
			 */
			@JavascriptInterface
			public String setComplainType() {
				String complainType = getIntent().getStringExtra("complainType");
				return complainType;
			}

			/**
			 * 向页面发送账号
			 */
			@JavascriptInterface
			public String setAccount() {
				String accountAry = SharedPreferencesInfo.getTagString(SimpleWebActivity.this, SharedPreferencesInfo.ACCOUNT) + "," + getIntent().getStringExtra("friendAccount");
				return accountAry;
			}

            /**
             * 向页面传递用户账号
             */
			@JavascriptInterface
            public String transferAccount() {
                return SharedPreferencesInfo.getTagString(SimpleWebActivity.this, SharedPreferencesInfo.ACCOUNT);
            }

            @JavascriptInterface
            public String setFriendAccount() {
			    return getIntent().getStringExtra("friendAccount");
            }

			/**
			 * 向页面发送chatId
			 */
			@JavascriptInterface
			public String setChatId() {
				String chatId = getIntent().getStringExtra("chatId");
				return chatId;
			}

			/**
			 * 向页面发送投诉的Id
			 */
			@JavascriptInterface
			public String setCommentId() {
				String commentId = getIntent().getStringExtra("commentId");
				return commentId;
			}

			/**
			 * 复制到剪切板
			 * @param content
			 */
			@JavascriptInterface
			public void copyToClipboard(String content) {
				ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				cbm.setPrimaryClip(ClipData.newPlainText("code", content));
				ToastUtil.showToast(SimpleWebActivity.this, "已复制到剪切板");
			}

            /**
             * 关闭窗口
             */
            @JavascriptInterface
            public void close() {
                SimpleWebActivity.this.finish();
            }
		}, "heikuai");
		
		mIvBlankPage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgress.setVisibility(View.VISIBLE);
				new Thread() {
					public void run() {
						//先检测页面是否报错
						mIsError = validStatusCode(mUrl);
						mHdl.sendEmptyMessageDelayed(0, 1000);
					}
				}.start();
			}
		});
		
		mTitle = getIntent().getStringExtra("title");
		mUrl = getIntent().getStringExtra("url");
        if (mUrl.toLowerCase().contains("heikuai.com") || mUrl.toLowerCase().contains("cqsynet.com")) {
            if (!mUrl.contains("userAccount=")) {
                if (mUrl.contains("?")) {
                    mUrl = mUrl + "&userAccount=" + SharedPreferencesInfo.getTagString(SimpleWebActivity.this, SharedPreferencesInfo.ACCOUNT);
                } else {
                    mUrl = mUrl + "?userAccount=" + SharedPreferencesInfo.getTagString(SimpleWebActivity.this, SharedPreferencesInfo.ACCOUNT);
                }
            }
        }
        if (!TextUtils.isEmpty(mTitle)) {
            mTitleBar.setTitle(mTitle);
        }

        new Thread() {
            public void run() {
				mProgress.setVisibility(View.VISIBLE);
                //先检测页面是否报错
                mIsError = validStatusCode(mUrl);
                mHdl.sendEmptyMessage(0);
            }
        }.start();
	}

    Handler mHdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mWebView.loadUrl(mUrl);
                    break;
            }
        }
    };

    /**
     * 检查url返回页面是否成功
     *
     * @param url
     * @return
     */
    private boolean validStatusCode(String url) {
        OkHttpClient mClient = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).build();
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response = mClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }
}

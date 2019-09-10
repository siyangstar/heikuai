package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.AppManager;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.SuggestListAdapter;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.SuggestListItem;
import com.cqsynet.swifi.model.SuggestListRequestBody;
import com.cqsynet.swifi.model.SuggestListResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class SuggestListActivity extends HkActivity implements OnClickListener {
	
	private TitleBar mTitleBar;
	private ListView mLvSuggestList;
	private SuggestListAdapter mSuggestListAdapter;
	private ArrayList<SuggestListItem> mSuggestList;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.ivBack_titlebar_layout) { // 返回
			HashMap<String, Activity> activityMap = AppManager.getInstance().getActivityMap();
        	if(!activityMap.containsKey("HomeActivity")) { // 如果HomeActivity处于销毁状态，app未启动，跳转新闻列表
        		Intent jumpIntent = new Intent();
        		jumpIntent.setClass(SuggestListActivity.this,HomeActivity.class);
        		startActivity(jumpIntent);
        	} else {
        		finish();
        	}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggest_list);
		mTitleBar = findViewById(R.id.titlebar_activity_suggest_list);
		mTitleBar.setTitle("所有意见反馈");
		mTitleBar.setLeftIconClickListener(this);
		mLvSuggestList = findViewById(R.id.suggest_activity_suggest_list);
		mSuggestList = new ArrayList<SuggestListItem>();
		getSuggestList();
		mSuggestListAdapter = new SuggestListAdapter(this,mSuggestList);
		mLvSuggestList.setAdapter(mSuggestListAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
        //去掉意见反馈相关的所有小红点提示
		SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_SUGGEST_LIST, false);
        SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_SUGGEST, false);
        if(!SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.NEW_VERSION, false)) {
            SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_SETTING, false);
        }
	}

	private void getSuggestList(){
		showProgressDialog("获取数据中...");
		SuggestListRequestBody suggestListRequestBody = new SuggestListRequestBody();
        IResponseCallback loginCallbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
            	dismissProgressDialog();
                if (response != null) {
                    Gson gson = new Gson();
                    SuggestListResponseObject responseObj = gson.fromJson(response, SuggestListResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                        	SuggestListRequestBody body = responseObj.body;
                        	mSuggestList = body.suggestionList;
                        	if (mSuggestList.size() != 0) {
                        		mSuggestListAdapter.changeDatas(mSuggestList);
							} else {
								ToastUtil.showToast(getApplicationContext(), "还没有反馈记录哦");
							}
                        } else {
                            ToastUtil.showToast(SuggestListActivity.this, header.errMsg);
                        }
                    }
                } else {
                	ToastUtil.showToast(SuggestListActivity.this, "获取失败");
				}
            }

            @Override
            public void onErrorResponse() {
            	dismissProgressDialog();
                ToastUtil.showToast(SuggestListActivity.this, "获取失败");
            }
        };
        // 调用接口获取反饋列表
        WebServiceIf.getSuggestList(this, suggestListRequestBody, loginCallbackIf);
	}
}

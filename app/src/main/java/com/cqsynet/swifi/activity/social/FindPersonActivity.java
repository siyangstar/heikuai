/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：找人界面
 *
 *
 * 创建标识：sayaki 20171123
 */
package com.cqsynet.swifi.activity.social;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.model.FindPersonInfo;
import com.cqsynet.swifi.model.FindPersonRequestBody;
import com.cqsynet.swifi.model.FindPersonResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.NetworkUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.FilterDialog;
import com.cqsynet.swifi.view.TipDialog;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/11/23
 */
public class FindPersonActivity extends HkActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    public static final String TYPE_TRAIN = "0";
    public static final String TYPE_STATION = "1";
    public static final String TYPE_LINE = "2";
    public static final String TYPE_NEARBY = "3";
    public static final String ACTION_REFRESH = "0";
    public static final String ACTION_LOAD_MORE = "1";

    private ImageView mIvBack;
    private TextView mTvCategory;
    private TextView mTvLocation;
    private TextView mTvFilterPerson;
    private FrameLayout mLayoutCategory;
    private RelativeLayout mRlCategory;
    private ImageView mIvClose;
    private PullToRefreshListView mListView;
    private LinearLayout mLlHint;
    private ImageView mIvHint;
    private TextView mTvHint1;
    private TextView mTvHint2;
    private ImageView mIvTip;
    private ProgressBar mLoadingBar;
    private FilterDialog mFilterDialog;

    private long mFreshTime = 0;
    // 是否有下一页
    private boolean mHasMore = true;

    private String mStation;
    private String mLine;
    private String mType = TYPE_TRAIN;
    private String mAge = "不限";
    private String mSex = "不限";
    private String[] mFindCategoryArray;

    private FindPersonAdapter mAdapter;
    private List<FindPersonInfo> mPersonList = new ArrayList<>();
    private MessageReceiver mMessageReceiver;

    public static void launch(Context context, String type) {
        Intent intent = new Intent();
        intent.setClass(context, FindPersonActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_person);

        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mTvCategory = findViewById(R.id.tv_category);
        mTvCategory.setOnClickListener(this);
        mTvLocation = findViewById(R.id.tv_location);
        mTvLocation.setOnClickListener(this);
        mTvFilterPerson = findViewById(R.id.tv_filter_person);
        mTvFilterPerson.setOnClickListener(this);
        mLayoutCategory = findViewById(R.id.layout_category);
        mRlCategory = findViewById(R.id.rlCategory_layout_filter_find_person);
        findViewById(R.id.cvTrain_layout_filter_find_person).setOnClickListener(this);
        findViewById(R.id.cvLine_layout_filter_find_person).setOnClickListener(this);
        findViewById(R.id.cvStation_layout_filter_find_person).setOnClickListener(this);
        findViewById(R.id.cvNearby_layout_filter_find_person).setOnClickListener(this);
        mIvClose = findViewById(R.id.iv_close);
        mIvClose.setOnClickListener(this);
        mListView = findViewById(R.id.list_view);
        mAdapter = new FindPersonAdapter(this, mPersonList);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mHasMore = true;
                findPerson(ACTION_REFRESH);
            }
        });
        mListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (mHasMore) {
                    mLoadingBar.setVisibility(View.VISIBLE);
                    findPerson(ACTION_LOAD_MORE);
                }
            }
        });
        mFreshTime = System.currentTimeMillis();
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel("更新于:" + DateUtil.getRelativeTimeSpanString(mFreshTime));
        mListView.setOnItemClickListener(this);
        mLlHint = findViewById(R.id.ll_hint);
        mLlHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadingBar.setVisibility(View.VISIBLE);
                findPerson(ACTION_REFRESH);
            }
        });
        mIvHint = findViewById(R.id.iv_hint);
        mTvHint1 = findViewById(R.id.tv_hint_1);
        mTvHint2 = findViewById(R.id.tv_hint_2);
        mIvTip = findViewById(R.id.iv_tip);
        mIvTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TipDialog(FindPersonActivity.this, R.style.round_corner_dialog, getString(R.string.social_find_person_failed_tip)).show();
            }
        });

        mLoadingBar = findViewById(R.id.loading_bar);

        mFindCategoryArray = getResources().getStringArray(R.array.find_category);

        mLoadingBar.setVisibility(View.VISIBLE);

        mType = getIntent().getStringExtra("type");
        mSex = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.SOCIAL_FILTER_SEX);
        if (TextUtils.isEmpty(mSex)) {
            mSex = "不限";
        }
        mAge = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.SOCIAL_FILTER_AGE);
        if (TextUtils.isEmpty(mAge)) {
            mAge = "不限";
        }
        findPerson(ACTION_REFRESH);

        mTvCategory.setText(mFindCategoryArray[Integer.parseInt(mType)]);

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_SOCKET_PUSH);
        filter.addAction(AppConstants.ACTION_DELETE_FRIEND);
        filter.addAction(AppConstants.ACTION_ADD_FRIEND);
        registerReceiver(mMessageReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_category:
                openCategoryLayout();
                break;
            case R.id.tv_location:
                openCategoryLayout();
                break;
            case R.id.tv_filter_person:
                showFilterDialog();
                break;
            case R.id.cvTrain_layout_filter_find_person:
                mTvCategory.setText(mFindCategoryArray[0]);
                mType = TYPE_TRAIN;
                mLoadingBar.setVisibility(View.VISIBLE);
                findPerson(ACTION_REFRESH);
                closeCategoryLayout();
                break;
            case R.id.cvStation_layout_filter_find_person:
                mTvCategory.setText(mFindCategoryArray[1]);
                mType = TYPE_STATION;
                mLoadingBar.setVisibility(View.VISIBLE);
                findPerson(ACTION_REFRESH);
                closeCategoryLayout();
                break;
            case R.id.cvLine_layout_filter_find_person:
                mTvCategory.setText(mFindCategoryArray[2]);
                mType = TYPE_LINE;
                mLoadingBar.setVisibility(View.VISIBLE);
                findPerson(ACTION_REFRESH);
                closeCategoryLayout();
                break;
            case R.id.cvNearby_layout_filter_find_person:
                mTvCategory.setText(mFindCategoryArray[3]);
                mType = TYPE_NEARBY;
                mLoadingBar.setVisibility(View.VISIBLE);
                findPerson(ACTION_REFRESH);
                closeCategoryLayout();
                break;
            case R.id.iv_close:
                closeCategoryLayout();
                break;
        }
    }

    private void openCategoryLayout() {
        mLayoutCategory.setVisibility(View.VISIBLE);
        ObjectAnimator springInAnimator = ObjectAnimator.ofFloat(mRlCategory, "translationY", -1800, 0);
        springInAnimator.setDuration(500);
        springInAnimator.setInterpolator(new SpringInterpolator(0.7f));
        springInAnimator.start();
        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(mLayoutCategory, "alpha", 0f, 1f);
        fadeInAnimator.setDuration(500).start();
        mIvClose.setEnabled(true);
    }

    private void closeCategoryLayout() {
        mIvClose.setEnabled(false);
        ObjectAnimator springOutAnimator = ObjectAnimator.ofFloat(mRlCategory, "translationY", 0, -1800);
        springOutAnimator.setDuration(500).start();
        ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(mLayoutCategory, "alpha", 1f, 0f);
        fadeOutAnimator.setDuration(500).start();
        fadeOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLayoutCategory.setVisibility(View.GONE);
            }
        });
    }

    private void findPerson(final String action) {
        if(action.equals(ACTION_REFRESH)) {
            mTvLocation.setVisibility(View.GONE);
            mTvCategory.setTextSize(16);
        }
        switch (mType) {
            case TYPE_TRAIN:
            case TYPE_LINE:
            case TYPE_STATION:
                if (!isNetworkOk() || !isHeikuaiNetwork()) {
                    return;
//                } else {
//                    mTvLocation.setVisibility(View.VISIBLE);
//                    mTvCategory.setTextSize(14);
                }
                break;
            case TYPE_NEARBY:
                if (!isNetworkOk()) {
                    return;
                }
                break;
        }

        FindPersonRequestBody body = new FindPersonRequestBody();
        body.type = mType;
        body.age = mAge;
        body.sex = mSex;
        body.refresh = action;
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                mLoadingBar.setVisibility(View.GONE);
                mListView.onRefreshComplete();
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    FindPersonResponseObject object = gson.fromJson(response, FindPersonResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        mLine = object.body.line;
                        mStation = object.body.station;
                        if (ACTION_REFRESH.equals(action)) {
                            refreshPerson(object.body);
                        } else {
                            loadMorePerson(object.body);
                        }
                    } else if ("36162".equals(header.errCode) || "38162".equals(header.errCode)) {
                        mLlHint.setVisibility(View.VISIBLE);
                        mIvHint.setImageResource(R.drawable.ic_in_train);
                        mTvHint1.setText(R.string.social_in_train);
                        mTvHint2.setText(R.string.social_refresh);
                        mIvTip.setVisibility(View.VISIBLE);
                    } else if ("36163".equals(header.errCode) || "38163".equals(header.errCode)) {
                        mLlHint.setVisibility(View.VISIBLE);
                        mIvHint.setImageResource(R.drawable.ic_out_train);
                        mTvHint1.setText(R.string.social_out_train);
                        mTvHint2.setText(R.string.social_refresh);
                        mIvTip.setVisibility(View.VISIBLE);
                    } else {
                        ToastUtil.showToast(FindPersonActivity.this, "服务器开小差了，请稍后再试(" + header.errCode + ")");
                        mPersonList.clear();
                        mAdapter.notifyDataSetChanged();
                        updateHint();
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(FindPersonActivity.this, R.string.request_fail_warning);
                mLoadingBar.setVisibility(View.GONE);
                mPersonList.clear();
                mAdapter.notifyDataSetChanged();
                updateHint();
            }
        };
        WebServiceIf.findPerson(this, body, callback);
    }

    private boolean isHeikuaiNetwork() {
        WifiManager wifiManager = (WifiManager) (this.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (!NetworkUtil.isConnectFashionWiFi(wifiInfo)) {
            mLlHint.setVisibility(View.VISIBLE);
            mIvHint.setImageResource(R.drawable.ic_no_wifi);
            mTvHint1.setText(R.string.social_no_wifi);
            mTvHint2.setText("");
            mIvTip.setVisibility(View.GONE);
            mLoadingBar.setVisibility(View.GONE);
            return false;
        }
        return true;
    }

    private boolean isNetworkOk() {
        if (!NetworkUtil.isNetAvailable(this)) {
            mLlHint.setVisibility(View.VISIBLE);
            mIvHint.setImageResource(R.drawable.ic_no_location);
            mTvHint1.setText(R.string.social_no_location_1);
            mTvHint2.setText(R.string.social_no_location_2);
            mIvTip.setVisibility(View.GONE);
            mLoadingBar.setVisibility(View.GONE);
            return false;
        }
        return true;
    }

    private void refreshPerson(FindPersonResponseObject.FindPersonResponseBody body) {
        mFreshTime = System.currentTimeMillis();
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                "更新于：" + DateUtil.getRelativeTimeSpanString(mFreshTime));
        mPersonList.clear();
        if (body.userList != null && body.userList.size() > 0) {
            mPersonList.addAll(body.userList);
        }
        mAdapter.notifyDataSetChanged();
        mListView.getRefreshableView().setSelection(0);

        updateHint();
    }

    private void updateHint() {
        if (mPersonList.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            mLlHint.setVisibility(View.GONE);
        } else {
            mListView.setVisibility(View.GONE);
            mLlHint.setVisibility(View.VISIBLE);
            if (mAge.equals("不限") && mSex.equals("不限")) {
                mIvHint.setImageResource(R.drawable.ic_change_posture);
                mTvHint1.setText(R.string.social_change_posture);
                mTvHint2.setText(R.string.social_refresh);
                mIvTip.setVisibility(View.GONE);
            } else {
                mIvHint.setImageResource(R.drawable.ic_filter);
                mTvHint1.setText(R.string.social_no_person);
                mTvHint2.setText(R.string.social_filter_hint);
                mIvTip.setVisibility(View.GONE);
            }
        }
        switch (mType) {
            case TYPE_STATION:
                if (!TextUtils.isEmpty(mStation)) {
                    mTvLocation.setVisibility(View.VISIBLE);
                    mTvCategory.setTextSize(14);
                    if ("一号线".equals(mLine)) {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line1);
                    } else if ("二号线".equals(mLine)) {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line2);
                    } else if ("三号线".equals(mLine)) {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line3);
                    } else if ("六号线".equals(mLine)) {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line6);
                    } else {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line1);
                    }
                    mTvLocation.setText(mStation);
                }
                break;
            case TYPE_LINE:
                if (!TextUtils.isEmpty(mLine)) {
                    mTvLocation.setVisibility(View.VISIBLE);
                    mTvCategory.setTextSize(14);
                    if ("一号线".equals(mLine)) {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line1);
                    } else if ("二号线".equals(mLine)) {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line2);
                    } else if ("三号线".equals(mLine)) {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line3);
                    } else if ("六号线".equals(mLine)) {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line6);
                    } else {
                        mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line1);
                    }
                    mTvLocation.setText(mLine);
                }
                break;
        }
    }

    private void loadMorePerson(FindPersonResponseObject.FindPersonResponseBody body) {
        if (body.userList != null && body.userList.size() > 0) {
            mPersonList.addAll(body.userList);
            mAdapter.notifyDataSetChanged();
            mHasMore = true;
        } else {
            mHasMore = false;
        }
    }

    private void showFilterDialog() {
        mFilterDialog = new FilterDialog(this, R.style.round_corner_dialog, new FilterDialog.MyDialogListener() {
            @Override
            public void onClick(View view, String sex, String age) {
                switch (view.getId()) {
                    case R.id.tv_confirm:
                        mFilterDialog.dismiss();
                        mSex = sex;
                        mAge = age;
                        mLoadingBar.setVisibility(View.VISIBLE);
                        findPerson(ACTION_REFRESH);
                        SharedPreferencesInfo.setTagString(FindPersonActivity.this, SharedPreferencesInfo.SOCIAL_FILTER_SEX, mSex);
                        SharedPreferencesInfo.setTagString(FindPersonActivity.this, SharedPreferencesInfo.SOCIAL_FILTER_AGE, mAge);
                        break;
                    case R.id.tv_cancel:
                        mFilterDialog.dismiss();
                        break;
                }
            }
        }, mAge, mSex);
        mFilterDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, PersonInfoActivity.class);
        intent.putExtra("person", mPersonList.get(position - 1));
        intent.putExtra("category", "1"); //1表示是社交,0表示是漂流瓶
        intent.putExtra("isFriend", mPersonList.get(position - 1).isFriend);
        startActivity(intent);
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppConstants.ACTION_DELETE_FRIEND.equals(action)) {
                mLoadingBar.setVisibility(View.VISIBLE);
                findPerson(ACTION_REFRESH);
            } else if (AppConstants.ACTION_ADD_FRIEND.equals(action)) {
                mLoadingBar.setVisibility(View.VISIBLE);
                findPerson(ACTION_REFRESH);
            }
        }
    }

    public class SpringInterpolator implements Interpolator {
        //弹性因数
        private float factor;

        public SpringInterpolator(float factor) {
            this.factor = factor;
        }

        @Override
        public float getInterpolation(float input) {

            return (float) (Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);
        }
    }
}

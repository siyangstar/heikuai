/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻，wifi操作引导透明页面。
 *
 *
 * 创建标识：luchaowei 20141020
 */
package com.cqsynet.swifi.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

public class OperateGuideActivity extends HkActivity  implements OnClickListener {
    public static final int INDEX_WIFI = 0;
    public static final int INDEX_NEWS = 1;
    public static final int INDEX_BOTTLE = 3;
    public static final int INDEX_SOCIAL = 4;
    private int mGuideIndex;
    private RelativeLayout mRlGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_operate_guide);
        mRlGuide = findViewById(R.id.rlGuide_activity_operate_guide);
        mRlGuide.setOnClickListener(this);

        mGuideIndex = getIntent().getIntExtra("type", -1);
        int layoutId = -1;
        if (mGuideIndex == INDEX_WIFI) { // WIFI页面需要show 引导层。
            layoutId = R.layout.layout_wifi_guide;
        } else if (mGuideIndex == INDEX_NEWS) { // 新闻页面需要show 引导层。
            layoutId = R.layout.layout_news_guide;
        } else if (mGuideIndex == INDEX_BOTTLE) { // 漂流瓶引导层
            layoutId = R.layout.layout_social_guide;
        } else if (mGuideIndex == INDEX_SOCIAL) { // 社交引导层
            layoutId = R.layout.layout_social_guide;
        }
        if (layoutId != -1) {
            View view = View.inflate(this, layoutId, null);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mRlGuide.addView(view, params);
        } else {
            finish();
        }
    }


    @Override
    public void onClick(View arg0) {
        switch(mGuideIndex) {
            default:
                exitGuide();
                break;
        }
    }


    @Override
    public void onBackPressed() {
        exitGuide();
    }

   private void exitGuide() {
       if (mGuideIndex == INDEX_WIFI) { // 退出wifi引导页面，保存flag。
           SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.WIFI_GUIDE, true);
       } else if (mGuideIndex == INDEX_NEWS) { // 退出新闻引导页面，保存flag。
           SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEWS_GUIDE, true);
       } else if (mGuideIndex == INDEX_BOTTLE) { // 退出漂流瓶引导页面，保存flag。
           SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.BOTTLE_GUIDE, true);
       } else if (mGuideIndex == INDEX_SOCIAL) { // 退出社交引导页面，保存flag。
           SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.SOCIAL_GUIDE, true);
       }
       finish();
   }
}

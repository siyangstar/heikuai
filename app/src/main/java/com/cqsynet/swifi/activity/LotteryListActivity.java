/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：抽奖结果列表页面。
 *
 * 创建标识：sayaki 20170321
 */
package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.fragment.LotteryFragment;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.view.SlidingPagerTabStrip;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/3/21
 */
public class LotteryListActivity extends BasicFragmentActivity {

    private ImageView mIvBack;
    private TextView mTvExchange;
    private SlidingPagerTabStrip mSlidingTabs;
    private ViewPager mViewPager;
    private LotteryFragmentAdapter mAdapter;
    private static final String TYPE_LOTTERY_AVAILABLE = "0";
    private static final String TYPE_LOTTERY_INVALID = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottery_list);

        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvExchange = findViewById(R.id.tv_exchange);
        mTvExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent helpIntent = new Intent(LotteryListActivity.this, SimpleWebActivity.class);
                helpIntent.putExtra("url", AppConstants.REDEEM_PAGE);
                helpIntent.putExtra("title", "兑换卡券");
                startActivity(helpIntent);
            }
        });

        mViewPager = findViewById(R.id.vp_lottery);
        ArrayList<Fragment> fragments = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        fragments.add(LotteryFragment.create(TYPE_LOTTERY_AVAILABLE));
        titles.add("可用");
        fragments.add(LotteryFragment.create(TYPE_LOTTERY_INVALID));
        titles.add("失效");
        mAdapter = new LotteryFragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mAdapter);

        mSlidingTabs = findViewById(R.id.tab_lottery);
        mSlidingTabs.setUnderlineHeight(0);
        mSlidingTabs.setDividerColorResource(R.color.transparent);
        mSlidingTabs.setTabPaddingLeftRight(AppUtil.dp2px(this, 32));
        mSlidingTabs.setIndicatorColorResource(R.color.green);
        mSlidingTabs.setIndicatorHeight(AppUtil.dp2px(this, 2));
        mSlidingTabs.setTextColorResource(R.color.text2);
        mSlidingTabs.setSelectedTextColorResource(R.color.green);
        mSlidingTabs.setTextSize(AppUtil.dp2px(this, 15));
        mSlidingTabs.setSelectedTextSize(AppUtil.dp2px(this, 15));
        mSlidingTabs.setViewPager(mViewPager, mSPTSOnPageChangedListener);
    }

    private SlidingPagerTabStrip.SPTSOnPageChangedListener mSPTSOnPageChangedListener
            = new SlidingPagerTabStrip.SPTSOnPageChangedListener() {
        @Override
        public void onPageScrollStateChanged(int position) {
        }

        @Override
        public void onPageScrolled(int position, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onTabClick(int position) {
        }
    };

    private class LotteryFragmentAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragments;
        private List<String> mTitles;

        public LotteryFragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
            super(fm);
            this.mFragments = fragments;
            this.mTitles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }
    }
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：我的评论
 *
 *
 * 创建标识：zhaosy 20180316
 */
package com.cqsynet.swifi.activity;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.fragment.CommentReplyFragment;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.view.SlidingPagerTabStrip;

import java.util.ArrayList;
import java.util.List;

public class MyCommentActivity extends BasicFragmentActivity {

    private ViewPager mViewPager;
    private CommentFragmentAdapter mAdapter;
    private SlidingPagerTabStrip mSlidingTabs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_comment);

        mViewPager = findViewById(R.id.view_pager_activity_my_comment);
        findViewById(R.id.ivBack_activity_my_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(CommentReplyFragment.newInstance("commentReply"));
        fragments.add(CommentReplyFragment.newInstance("myComment"));
        ArrayList<String> titles = new ArrayList<>();
        titles.add("回复我的");
        titles.add("我的评论");
        mAdapter = new CommentFragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mAdapter);

        mSlidingTabs = findViewById(R.id.tab_activity_my_comment);
        mSlidingTabs.setUnderlineHeight(0);
        mSlidingTabs.setDividerColorResource(R.color.transparent);
        mSlidingTabs.setTabPaddingLeftRight(AppUtil.dp2px(this, 32));
        mSlidingTabs.setIndicatorColorResource(R.color.green);
        mSlidingTabs.setIndicatorHeight(AppUtil.dp2px(this, 2));
        mSlidingTabs.setTextColorResource(R.color.text2);
        mSlidingTabs.setSelectedTextColorResource(R.color.green);
        mSlidingTabs.setTextSize(AppUtil.dp2px(this, 15));
        mSlidingTabs.setSelectedTextSize(AppUtil.dp2px(this, 15));
        mSlidingTabs.setViewPager(mViewPager, new SlidingPagerTabStrip.SPTSOnPageChangedListener() {
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
        });
    }


    private class CommentFragmentAdapter extends FragmentPagerAdapter {

        private List<Fragment> mFragments;
        private List<String> mTitles;

        public CommentFragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
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

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_COMMENT_REPLY, false);
        SharedPreferencesInfo.setTagInt(this, SharedPreferencesInfo.COMMENT_REPLY_COUNT, 0);
    }
}

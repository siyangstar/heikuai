/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：加载在listview顶部的viewPager
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.view;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.cqsynet.swifi.R;

public class XViewPager extends FrameLayout {

    public FrameLayout mXViewPager;

    public XViewPager(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        DisplayMetrics dMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dMetrics);
        int screenWidth = dMetrics.widthPixels;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, screenWidth / 2);
        mXViewPager = (FrameLayout) LayoutInflater.from(context).inflate(
                R.layout.top_pager, null);
        addView(mXViewPager, lp);
    }
}
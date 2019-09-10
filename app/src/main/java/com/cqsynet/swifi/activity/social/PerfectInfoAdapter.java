/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：完善个人信息界面的适配器
 *
 *
 * 创建标识：sayaki 20171228
 */
package com.cqsynet.swifi.activity.social;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/12/28
 */
public class PerfectInfoAdapter extends PagerAdapter {

    private List<View> views;

    public PerfectInfoAdapter(List<View> views) {
        this.views = views;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}

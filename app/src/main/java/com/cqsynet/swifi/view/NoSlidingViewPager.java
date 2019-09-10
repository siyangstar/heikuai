package com.cqsynet.swifi.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Author: Arturia
 * Date: 2018/1/8
 */
public class NoSlidingViewPager extends ViewPager {

    public NoSlidingViewPager(Context context) {
        super(context);
    }

    public NoSlidingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}

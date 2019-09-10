/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：伸缩动画工具
 *
 *
 * 创建标识：zhaosy 20150923
 */
package com.cqsynet.swifi.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.cqsynet.swifi.view.StretchAnimation;

public class StretchAnimationUtil implements StretchAnimation.AnimationListener {

	private Context mContext;
	//View可伸展最长的宽度
	private int mMaxSize;
	//View可伸展最小宽度
	private int mMinSize;
	//当前点击的View
	public View mCurrentView;
	//显示最长的那个View
	public View mPreView;
	//主布局ViewGroup
	private LinearLayout mContainer;
	private StretchAnimation mStretchanimation;

	public StretchAnimationUtil(Context context, LinearLayout container) {
		mContext = context;
		mContainer = container;
	}

	/**
	 * @param index 初始化时哪一个是最大的 从零开始
	 */
	public void initViewData(int index) {
		View child;
		int sizeValue = 0;
		LayoutParams params = null;
		int childCount = mContainer.getChildCount();
		if (index < 0 || index >= childCount) {
			throw new RuntimeException("index 超出范围");
		}

		for (int i = 0; i < childCount; i++) {
			child = mContainer.getChildAt(i);
			params = child.getLayoutParams();
			if (i == index) {
				mPreView = child;
				sizeValue = mMaxSize;
			} else {
				sizeValue = mMinSize;
			}
			if (mStretchanimation.getmType() == StretchAnimation.TYPE.horizontal) {
				params.width = sizeValue;
			} else if (mStretchanimation.getmType() == StretchAnimation.TYPE.vertical) {
				params.height = sizeValue;
			}

			child.setLayoutParams(params);
		}
	}

	/**
	 * 初始化动画参数
	 */
	public void initCommonData() {
		int screentWidth = AppUtil.getScreenW((Activity) mContext);
		int space = AppUtil.dp2px(mContext, 48);
		measureSize(screentWidth - space);
		mStretchanimation = new StretchAnimation(mMaxSize, mMinSize, StretchAnimation.TYPE.horizontal, 200);
//		mStretchanimation.setInterpolator(new BounceInterpolator());
		mStretchanimation.setInterpolator(new DecelerateInterpolator());
		mStretchanimation.setOnAnimationListener(this);
	}

	/**
	 * 测量View 的 max min 长度 这里你可以根据你的要求设置max
	 * 
	 * @param screenSize
	 * @param index 从零开始
	 */
	private void measureSize(int layoutSize) {
		int width = layoutSize / 4;
		mMaxSize = width * 2;
		mMinSize = (layoutSize - mMaxSize) / (mContainer.getChildCount() - 1);
	}

	@Override
	public void animationEnd(View v) {
		onOffClickable(true);
	}

	private void clickEvent(View view) {
		View child;
		int childCount = mContainer.getChildCount();
		LinearLayout.LayoutParams params;
		for (int i = 0; i < childCount; i++) {
			child = mContainer.getChildAt(i);
			if (mPreView == child) {
				params = (android.widget.LinearLayout.LayoutParams) child.getLayoutParams();
				if (mPreView != view) {
					params.weight = 1.0f;
				}
				child.setLayoutParams(params);
			} else {
				params = (android.widget.LinearLayout.LayoutParams) child.getLayoutParams();
				params.weight = 0.0f;
				if (mStretchanimation.getmType() == StretchAnimation.TYPE.horizontal) {
					params.width = mMinSize;
				} else if (mStretchanimation.getmType() == StretchAnimation.TYPE.vertical) {
					params.height = mMinSize;
				}
				child.setLayoutParams(params);
			}
		}
		mPreView = view;

	}

	public void click(View v) {
		View tempView = null;
		for(int i = 0; i < mContainer.getChildCount(); i++) {
			if(v.getId() == mContainer.getChildAt(i).getId()) {
				tempView = mContainer.getChildAt(i);
			}
		}
		if (tempView == mPreView) {
			return;
		} else {
			mCurrentView = tempView;
		}
		clickEvent(mCurrentView);
		onOffClickable(false);
		mStretchanimation.startAnimation(mCurrentView);
	}
	
	
	/**
	 * LinearLayout下所有childView是否可点击的开关,
	 * 当动画在播放时应该设置为不可点击，结束时设置为可点击  
	 * @param isClickable
	 */
    private void onOffClickable(boolean isClickable) {  
        View child;  
        int childCount = mContainer.getChildCount();  
        for (int i = 0; i < childCount; i++) {  
            child = mContainer.getChildAt(i);  
            child.setClickable(isClickable);  
        }  
    } 
}
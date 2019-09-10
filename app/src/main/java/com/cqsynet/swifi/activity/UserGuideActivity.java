/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：尚WIFI引导图Activity
 *
 *
 * 创建标识：zhaosiyang 20141029
 */
package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.DoubleClickExitUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

public class UserGuideActivity extends HkActivity  {

	private final int[] mImageResAry = { R.drawable.guide0, R.drawable.guide1, R.drawable.guide2, R.drawable.guide3 }; // 引导图
	private ViewPager mVpGuide;
	private RadioGroup mRgPoint;
	private RadioButton[] mRbPoint;
	private ImageView mImageView;
	private DoubleClickExitUtil mExitUtil;
	private String mFrom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_guide);

		mExitUtil = new DoubleClickExitUtil(this);

		mVpGuide = findViewById(R.id.vpGuide_guide);
		mRgPoint = findViewById(R.id.rgPoint_guide);
		
		mFrom = getIntent().getStringExtra("from");

		initPoint();
		mVpGuide.setAdapter(mGuidePagerAdapter);

		mVpGuide.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageSelected(int position) {
				for (int i = 0; i < mImageResAry.length; i++) {
					if (i == position) {
						mRgPoint.getChildAt(position).setSelected(true);
						mRbPoint[i].setChecked(true);
					} else {
						mRgPoint.getChildAt(i).setSelected(false);
						mRbPoint[i].setChecked(false);
					}
				}
			}
		});
	}

	/**
	 * 初始化底部小圆点
	 */
	private void initPoint() {
		mRbPoint = new RadioButton[mImageResAry.length];
		for (int i = 0; i < mImageResAry.length; i++) {
			RadioButton rb = new RadioButton(UserGuideActivity.this);
			rb.setBackgroundDrawable(null);
			rb.setClickable(false);
			RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rb.setButtonDrawable(R.drawable.green_point_selector);
			rb.setSelected(false);
			mRgPoint.addView(rb, params);
			mRbPoint[i] = rb;
		}
		mRgPoint.getChildAt(0).setSelected(true);
		mRbPoint[0].setChecked(true);
	}

	/**
	 * 引导图viewPager的适配器
	 */
	private PagerAdapter mGuidePagerAdapter = new PagerAdapter() {
		@Override
		public int getCount() {
			return mImageResAry.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		public Object instantiateItem(ViewGroup container, final int position) {
			mImageView = new ImageView(UserGuideActivity.this);
			mImageView.setImageResource(mImageResAry[position]);
			mImageView.setScaleType(ScaleType.CENTER_INSIDE);
			if (position == mImageResAry.length - 1) {
				mImageView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (position == (mImageResAry.length - 1)) {
							if(mFrom != null && mFrom.equals("about")) {
								UserGuideActivity.this.finish();
							} else {
								SharedPreferencesInfo.setTagInt(UserGuideActivity.this, SharedPreferencesInfo.MAIN_GUIDE, 1);
	                            if (SharedPreferencesInfo.getTagInt(UserGuideActivity.this, SharedPreferencesInfo.IS_LOGIIN) == 0) { // 未登陆
	                                Intent loginIntent = new Intent(UserGuideActivity.this, LoginActivity.class);
	                                startActivity(loginIntent);
	                            } else {
	                                Intent guideIntent = new Intent(UserGuideActivity.this, HomeActivity.class);
	                                startActivity(guideIntent);
	                            }
	                            UserGuideActivity.this.finish();
							}
						}
						mImageView.setClickable(false);
					}
				});
			}
			container.addView(mImageView);
			return mImageView;
		}
	};
	

	@Override
	public void onBackPressed() {
		if(mFrom != null && mFrom.equals("about")) {
			finish();
		} else {
			mExitUtil.exit(true);
		}
	}
}

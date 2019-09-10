package com.cqsynet.swifi.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.AppUtil;

/**
 * Author: sayaki
 * Date: 2018/1/15
 */
public class FilterDialog extends Dialog implements android.view.View.OnClickListener {

    private Context mContext;
    private MyDialogListener mListener;

    private TextView mTvSexAll;
    private LinearLayout mLlMale;
    private ImageView mIvSexMale;
    private TextView mTvSexMale;
    private LinearLayout mLlFemale;
    private ImageView mIvSexFemale;
    private TextView mTvSexFemale;
    private TextView mTvAgeAll;
    private TextView mTvLower18;
    private TextView mTv18To25;
    private TextView mTv26To32;
    private TextView mTv33To40;
    private TextView mTvHigher40;

    private String mAge = "不限";
    private String mSex = "不限";

    public FilterDialog(@NonNull Context context, int themeResId, MyDialogListener listener, String age, String sex) {
        super(context, themeResId);
        this.mContext = context;
        this.mListener = listener;
        this.mAge = age;
        this.mSex = sex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_filter);
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = AppUtil.getScreenW((Activity) mContext) - AppUtil.dp2px(mContext, 48);
        getWindow().setAttributes(lp);
        mTvSexAll = findViewById(R.id.tv_sex_all);
        mTvSexAll.setOnClickListener(mSexListener);
        mLlMale = findViewById(R.id.ll_male);
        mLlMale.setOnClickListener(mSexListener);
        mIvSexMale = findViewById(R.id.iv_sex_male);
        mTvSexMale = findViewById(R.id.tv_sex_male);
        mLlFemale = findViewById(R.id.ll_female);
        mIvSexFemale = findViewById(R.id.iv_sex_female);
        mTvSexFemale = findViewById(R.id.tv_sex_female);
        mLlFemale.setOnClickListener(mSexListener);
        mTvAgeAll = findViewById(R.id.tv_age_all);
        mTvAgeAll.setOnClickListener(mAgeListener);
        mTvLower18 = findViewById(R.id.tv_lower_18);
        mTvLower18.setOnClickListener(mAgeListener);
        mTv18To25 = findViewById(R.id.tv_18to25);
        mTv18To25.setOnClickListener(mAgeListener);
        mTv26To32 = findViewById(R.id.tv_26to32);
        mTv26To32.setOnClickListener(mAgeListener);
        mTv33To40 = findViewById(R.id.tv_33to40);
        mTv33To40.setOnClickListener(mAgeListener);
        mTvHigher40 = findViewById(R.id.tv_higher_40);
        mTvHigher40.setOnClickListener(mAgeListener);

        if ("不限".equals(mSex)) {
            mTvSexAll.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTvSexAll.setTextColor(mContext.getResources().getColor(R.color.white));
        } else if ("男".equals(mSex)) {
            mLlMale.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mIvSexMale.setImageResource(R.drawable.ic_male_white);
            mTvSexMale.setTextColor(mContext.getResources().getColor(R.color.white));
        } else if ("女".equals(mSex)) {
            mLlFemale.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mIvSexFemale.setImageResource(R.drawable.ic_female_white);
            mTvSexFemale.setTextColor(mContext.getResources().getColor(R.color.white));
        }

        if ("不限".equals(mAge)) {
            mTvAgeAll.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTvAgeAll.setTextColor(mContext.getResources().getColor(R.color.white));
        } else if ("18岁以下".equals(mAge)) {
            mTvLower18.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTvLower18.setTextColor(mContext.getResources().getColor(R.color.white));
        } else if ("18-25岁".equals(mAge)) {
            mTv18To25.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTv18To25.setTextColor(mContext.getResources().getColor(R.color.white));
        } else if ("26-32岁".equals(mAge)) {
            mTv26To32.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTv26To32.setTextColor(mContext.getResources().getColor(R.color.white));
        } else if ("33-40岁".equals(mAge)) {
            mTv33To40.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTv33To40.setTextColor(mContext.getResources().getColor(R.color.white));
        } else if ("40岁以上".equals(mAge)) {
            mTvHigher40.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTvHigher40.setTextColor(mContext.getResources().getColor(R.color.white));
        }

        TextView tvConfirm = findViewById(R.id.tv_confirm);
        tvConfirm.setOnClickListener(this);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(this);
    }

    private View.OnClickListener mSexListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_sex_all:
                    mTvSexAll.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTvSexAll.setTextColor(mContext.getResources().getColor(R.color.white));
                    mLlMale.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mIvSexMale.setImageResource(R.drawable.ic_male);
                    mTvSexMale.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mLlFemale.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mIvSexFemale.setImageResource(R.drawable.ic_female);
                    mTvSexFemale.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mSex = "不限";
                    break;
                case R.id.ll_male:
                    mTvSexAll.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvSexAll.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mLlMale.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mIvSexMale.setImageResource(R.drawable.ic_male_white);
                    mTvSexMale.setTextColor(mContext.getResources().getColor(R.color.white));
                    mLlFemale.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mIvSexFemale.setImageResource(R.drawable.ic_female);
                    mTvSexFemale.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mSex = "男";
                    break;
                case R.id.ll_female:
                    mTvSexAll.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvSexAll.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mIvSexMale.setImageResource(R.drawable.ic_male);
                    mTvSexMale.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mLlMale.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mLlFemale.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mIvSexFemale.setImageResource(R.drawable.ic_female_white);
                    mTvSexFemale.setTextColor(mContext.getResources().getColor(R.color.white));
                    mSex = "女";
                    break;
            }
        }
    };

    private View.OnClickListener mAgeListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_age_all:
                    mTvAgeAll.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTvAgeAll.setTextColor(mContext.getResources().getColor(R.color.white));
                    mTvLower18.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvLower18.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv18To25.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv18To25.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv26To32.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv26To32.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv33To40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv33To40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvHigher40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mAge = "不限";
                    break;
                case R.id.tv_lower_18:
                    mTvAgeAll.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvAgeAll.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvLower18.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTvLower18.setTextColor(mContext.getResources().getColor(R.color.white));
                    mTv18To25.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv18To25.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv26To32.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv26To32.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv33To40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv33To40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvHigher40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mAge = "18岁以下";
                    break;
                case R.id.tv_18to25:
                    mTvAgeAll.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvAgeAll.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvLower18.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvLower18.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv18To25.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTv18To25.setTextColor(mContext.getResources().getColor(R.color.white));
                    mTv26To32.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv26To32.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv33To40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv33To40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvHigher40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mAge = "18-25岁";
                    break;
                case R.id.tv_26to32:
                    mTvAgeAll.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvAgeAll.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvLower18.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvLower18.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv18To25.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv18To25.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv26To32.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTv26To32.setTextColor(mContext.getResources().getColor(R.color.white));
                    mTv33To40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv33To40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvHigher40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mAge = "26-32岁";
                    break;
                case R.id.tv_33to40:
                    mTvAgeAll.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvAgeAll.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvLower18.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvLower18.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv18To25.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv18To25.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv26To32.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv26To32.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv33To40.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTv33To40.setTextColor(mContext.getResources().getColor(R.color.white));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvHigher40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mAge = "33-40岁";
                    break;
                case R.id.tv_higher_40:
                    mTvAgeAll.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvAgeAll.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvLower18.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvLower18.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv18To25.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv18To25.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv26To32.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv26To32.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTv33To40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv33To40.setTextColor(mContext.getResources().getColor(R.color.text2));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTvHigher40.setTextColor(mContext.getResources().getColor(R.color.white));
                    mAge = "40岁以上";
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        mListener.onClick(v, mSex, mAge);
    }

    public interface MyDialogListener {
        void onClick(View view, String sex, String age);
    }
}

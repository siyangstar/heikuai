/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：完善个人信息界面
 *
 *
 * 创建标识：sayaki 20171205
 */
package com.cqsynet.swifi.activity.social;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.activity.OperateGuideActivity;
import com.cqsynet.swifi.activity.SelectionPictureActivity;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.NoSlidingViewPager;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/12/5
 */
public class PerfectInfoActivity extends HkActivity {

    private static final int REQUEST_CODE_HEAD = 100;

    private NoSlidingViewPager mViewPager;
    private LinearLayout mLlUploadAvatar;
    private ImageView mIvAvatar;
    private EditText mEtNickname;
    private EditText mEtSign;
    private TextView mTvNext;
    private LinearLayout mLlMale;
    private ImageView mIvSexMale;
    private TextView mTvSexMale;
    private LinearLayout mLlFemale;
    private ImageView mIvSexFemale;
    private TextView mTvSexFemale;
    private TextView mTvLower18;
    private TextView mTv18To25;
    private TextView mTv26To32;
    private TextView mTv33To40;
    private TextView mTvHigher40;
    private TextView mTvComplete;

    // 选择的头像路径
    private String mHeadImagePath;
    // 是否已经选择了头像
    private boolean mHasAvatar;
    private String mSex;
    private String mAge;
    // 是否已经设置了性别
    private boolean mHasSex;
    // 是否已经设置了年龄
    private boolean mHasAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfect_info);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewPager.getCurrentItem() == 0) {
                    finish();
                } else {
                    mViewPager.setCurrentItem(0, true);
                }
            }
        });

        View view1 = getLayoutInflater().inflate(R.layout.layout_info_1, null);
        mLlUploadAvatar = view1.findViewById(R.id.ll_upload_avatar);
        mIvAvatar = view1.findViewById(R.id.iv_avatar);
        mEtNickname = view1.findViewById(R.id.et_nickname);
        mEtNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString().trim()) && mHasAvatar) {
                    mTvNext.setBackgroundResource(R.drawable.bg_green_radius_normal);
                } else {
                    mTvNext.setBackgroundResource(R.drawable.bg_white_radius_disable);
                }
            }
        });
        mEtSign = view1.findViewById(R.id.et_sign);
        mEtSign.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            }
        });
        mLlUploadAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PerfectInfoActivity.this, SelectionPictureActivity.class);
                intent.putExtra("title", "头像修改");
                intent.putExtra("isNeedCut", true);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 720);
                intent.putExtra("outputY", 720);
                startActivityForResult(intent, REQUEST_CODE_HEAD);
            }
        });
        mTvNext = view1.findViewById(R.id.tv_next);
        mTvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });

        View view2 = getLayoutInflater().inflate(R.layout.layout_info_2, null);
        mLlMale = view2.findViewById(R.id.ll_male);
        mLlMale.setOnClickListener(mSexListener);
        mIvSexMale = view2.findViewById(R.id.iv_sex_male);
        mTvSexMale = view2.findViewById(R.id.tv_sex_male);
        mLlFemale = view2.findViewById(R.id.ll_female);
        mIvSexFemale = view2.findViewById(R.id.iv_sex_female);
        mTvSexFemale = view2.findViewById(R.id.tv_sex_female);
        mLlFemale.setOnClickListener(mSexListener);
        mTvLower18 = view2.findViewById(R.id.tv_lower_18);
        mTvLower18.setOnClickListener(mAgeListener);
        mTv18To25 = view2.findViewById(R.id.tv_18to25);
        mTv18To25.setOnClickListener(mAgeListener);
        mTv26To32 = view2.findViewById(R.id.tv_26to32);
        mTv26To32.setOnClickListener(mAgeListener);
        mTv33To40 = view2.findViewById(R.id.tv_33to40);
        mTv33To40.setOnClickListener(mAgeListener);
        mTvHigher40 = view2.findViewById(R.id.tv_higher_40);
        mTvHigher40.setOnClickListener(mAgeListener);
        mTvComplete = view2.findViewById(R.id.tv_complete);
        mTvComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                complete();
            }
        });

        List<View> views = new ArrayList<>();
        views.add(view1);
        views.add(view2);
        PerfectInfoAdapter adapter = new PerfectInfoAdapter(views);

        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(adapter);

        if (!TextUtils.isEmpty(Globals.g_userInfo.headUrl)) {
            GlideApp.with(this)
                    .load(Globals.g_userInfo.headUrl)
                    .centerCrop()
                    .circleCrop()
                    .error(R.drawable.icon_profile_default_round)
                    .into(mIvAvatar);
            mHasAvatar = true;
        }
        mEtNickname.setText(Globals.g_userInfo.nickname);
        mEtNickname.setSelection(Globals.g_userInfo.nickname.length());
        mEtSign.setText(Globals.g_userInfo.sign);
        if (!TextUtils.isEmpty(mEtNickname.getText()) && mHasAvatar) {
            mTvNext.setBackgroundResource(R.drawable.bg_green_radius_normal);
        }
        if ("男".equals(Globals.g_userInfo.sex)) {
            mLlMale.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mIvSexMale.setImageResource(R.drawable.ic_male_white);
            mTvSexMale.setTextColor(getResources().getColor(R.color.white));
            mSex = "男";
            mHasSex = true;
        } else if ("女".equals(Globals.g_userInfo.sex)) {
            mLlFemale.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mIvSexFemale.setImageResource(R.drawable.ic_female_white);
            mTvSexFemale.setTextColor(getResources().getColor(R.color.white));
            mSex = "女";
            mHasSex = true;
        }
        if ("18岁以下".equals(Globals.g_userInfo.age)) {
            mTvLower18.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTvLower18.setTextColor(getResources().getColor(R.color.white));
            mAge = "18岁以下";
            mHasAge = true;
        } else if ("18-25岁".equals(Globals.g_userInfo.age)) {
            mTv18To25.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTv18To25.setTextColor(getResources().getColor(R.color.white));
            mAge = "18-25岁";
            mHasAge = true;
        } else if ("26-32岁".equals(Globals.g_userInfo.age)) {
            mTv26To32.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTv26To32.setTextColor(getResources().getColor(R.color.white));
            mAge = "26-32岁";
            mHasAge = true;
        } else if ("33-40岁".equals(Globals.g_userInfo.age)) {
            mTv33To40.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTv33To40.setTextColor(getResources().getColor(R.color.white));
            mAge = "33-40岁";
            mHasAge = true;
        } else if ("40岁以上".equals(Globals.g_userInfo.age)) {
            mTvHigher40.setBackgroundResource(R.drawable.bg_green_radius_selector);
            mTvHigher40.setTextColor(getResources().getColor(R.color.white));
            mAge = "40岁以上";
            mHasAge = true;
        }
        if (!TextUtils.isEmpty(mSex) && !TextUtils.isEmpty(mAge)) {
            mTvComplete.setBackgroundResource(R.drawable.bg_green_radius_selector);
        }

        // 第一次进入，显示操作引导图层
        if (!SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.SOCIAL_GUIDE, false)) {
            Intent intent = new Intent(this, OperateGuideActivity.class);
            intent.putExtra("type", OperateGuideActivity.INDEX_SOCIAL);
            startActivity(intent);
        }
    }

    private View.OnClickListener mSexListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_male:
                    mLlMale.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mIvSexMale.setImageResource(R.drawable.ic_male_white);
                    mTvSexMale.setTextColor(getResources().getColor(R.color.white));
                    mLlFemale.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mIvSexFemale.setImageResource(R.drawable.ic_female);
                    mTvSexFemale.setTextColor(getResources().getColor(R.color.text2));
                    mSex = "男";
                    break;
                case R.id.ll_female:
                    mLlFemale.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mIvSexMale.setImageResource(R.drawable.ic_male);
                    mTvSexMale.setTextColor(getResources().getColor(R.color.text2));
                    mLlMale.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mIvSexFemale.setImageResource(R.drawable.ic_female_white);
                    mTvSexFemale.setTextColor(getResources().getColor(R.color.white));
                    mSex = "女";
                    break;
            }
            mHasSex = true;
            if (mHasAge) {
                mTvComplete.setBackgroundResource(R.drawable.bg_green_radius_selector);
            } else {
                mTvComplete.setBackgroundResource(R.drawable.bg_white_radius_disable);
            }
        }
    };

    private View.OnClickListener mAgeListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_lower_18:
                    mTvLower18.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTvLower18.setTextColor(getResources().getColor(R.color.white));
                    mTv18To25.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv18To25.setTextColor(getResources().getColor(R.color.text2));
                    mTv26To32.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv26To32.setTextColor(getResources().getColor(R.color.text2));
                    mTv33To40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv33To40.setTextColor(getResources().getColor(R.color.text2));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvHigher40.setTextColor(getResources().getColor(R.color.text2));
                    mAge = "18岁以下";
                    break;
                case R.id.tv_18to25:
                    mTvLower18.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvLower18.setTextColor(getResources().getColor(R.color.text2));
                    mTv18To25.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTv18To25.setTextColor(getResources().getColor(R.color.white));
                    mTv26To32.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv26To32.setTextColor(getResources().getColor(R.color.text2));
                    mTv33To40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv33To40.setTextColor(getResources().getColor(R.color.text2));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvHigher40.setTextColor(getResources().getColor(R.color.text2));
                    mAge = "18-25岁";
                    break;
                case R.id.tv_26to32:
                    mTvLower18.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvLower18.setTextColor(getResources().getColor(R.color.text2));
                    mTv18To25.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv18To25.setTextColor(getResources().getColor(R.color.text2));
                    mTv26To32.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTv26To32.setTextColor(getResources().getColor(R.color.white));
                    mTv33To40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv33To40.setTextColor(getResources().getColor(R.color.text2));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvHigher40.setTextColor(getResources().getColor(R.color.text2));
                    mAge = "26-32岁";
                    break;
                case R.id.tv_33to40:
                    mTvLower18.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvLower18.setTextColor(getResources().getColor(R.color.text2));
                    mTv18To25.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv18To25.setTextColor(getResources().getColor(R.color.text2));
                    mTv26To32.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv26To32.setTextColor(getResources().getColor(R.color.text2));
                    mTv33To40.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTv33To40.setTextColor(getResources().getColor(R.color.white));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvHigher40.setTextColor(getResources().getColor(R.color.text2));
                    mAge = "33-40岁";
                    break;
                case R.id.tv_higher_40:
                    mTvLower18.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTvLower18.setTextColor(getResources().getColor(R.color.text2));
                    mTv18To25.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv18To25.setTextColor(getResources().getColor(R.color.text2));
                    mTv26To32.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv26To32.setTextColor(getResources().getColor(R.color.text2));
                    mTv33To40.setBackgroundResource(R.drawable.bg_gray_wireframe);
                    mTv33To40.setTextColor(getResources().getColor(R.color.text2));
                    mTvHigher40.setBackgroundResource(R.drawable.bg_green_radius_selector);
                    mTvHigher40.setTextColor(getResources().getColor(R.color.white));
                    mAge = "40以上";
                    break;
            }
            mHasAge = true;
            if (mHasSex) {
                mTvComplete.setBackgroundResource(R.drawable.bg_green_radius_selector);
            } else {
                mTvComplete.setBackgroundResource(R.drawable.bg_white_radius_disable);
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mViewPager.setCurrentItem(0, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_HEAD) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("file")) {
                    mHeadImagePath = data.getStringExtra("file");
                    if (!TextUtils.isEmpty(mHeadImagePath)) {
                        GlideApp.with(this)
                                .load(mHeadImagePath)
                                .centerCrop()
                                .circleCrop()
                                .error(R.drawable.icon_profile_default_round)
                                .into(mIvAvatar);

                        mHasAvatar = true;
                    }
                }
                if (!TextUtils.isEmpty(mEtNickname.getText()) && mHasAvatar) {
                    mTvNext.setBackgroundResource(R.drawable.bg_green_radius_selector);
                } else {
                    mTvNext.setBackgroundResource(R.drawable.bg_white_radius_disable);
                }
            }
        }
    }

    private void next() {
        if (!TextUtils.isEmpty(mEtNickname.getText().toString().trim()) && mHasAvatar) {
            mViewPager.setCurrentItem(1, true);
        } else {
            if (TextUtils.isEmpty(mEtNickname.getText().toString().trim())) {
                ToastUtil.showToast(this, "昵称不能为空");
            } else if (!mHasAvatar) {
                ToastUtil.showToast(this, "头像不能为空");
            }
        }
    }

    private void complete() {
        if (TextUtils.isEmpty(mSex)) {
            ToastUtil.showToast(this, "必须选择性别");
            return;
        }
        if (TextUtils.isEmpty(mAge)) {
            ToastUtil.showToast(this, "必须选择年龄");
            return;
        }
        final UserInfo userInfo = new UserInfo();
        userInfo.nickname = mEtNickname.getText().toString();
        userInfo.sign = mEtSign.getText().toString();
        userInfo.headUrl = mHeadImagePath;
        userInfo.sex = mSex;
        userInfo.age = mAge;
        userInfo.setting = "0";
        File avatarFile = null;
        if (!TextUtils.isEmpty(mHeadImagePath)) {
            avatarFile = new File(mHeadImagePath);
        }

        WebServiceIf.updateUserInfo(this, avatarFile, userInfo, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    BaseResponseObject object = new Gson().fromJson(response, BaseResponseObject.class);
                    if (object.header != null) {
                        if (AppConstants.RET_OK.equals(object.header.ret)) {
                            Globals.g_userInfo.nickname = mEtNickname.getText().toString();
                            if (!TextUtils.isEmpty(mHeadImagePath)) {
                                Globals.g_userInfo.headUrl = mHeadImagePath;
                            }
                            Globals.g_userInfo.sex = userInfo.sex;
                            Globals.g_userInfo.age = userInfo.age;
                            Globals.g_userInfo.sign = userInfo.sign;
                            Globals.g_userInfo.setting = "0";

                            SharedPreferencesInfo.setTagString(PerfectInfoActivity.this, SharedPreferencesInfo.USER_INFO, new Gson().toJson(Globals.g_userInfo));
                            if (!TextUtils.isEmpty(mHeadImagePath)) {
                                Intent intent = new Intent(AppConstants.ACTION_REFRESH_HEADER);
                                intent.putExtra("headUrl", mHeadImagePath);
                                sendBroadcast(intent);
                            }

                            ToastUtil.showToast(PerfectInfoActivity.this, "提交个人信息成功");

                            startActivity(new Intent(PerfectInfoActivity.this, SocialActivity.class));
                            PerfectInfoActivity.this.finish();
                        } else if (!TextUtils.isEmpty(object.header.errCode)) {
                            ToastUtil.showToast(PerfectInfoActivity.this, object.header.errMsg + "(" + object
                                    .header.errCode + ")");
                        } else {
                            ToastUtil.showToast(PerfectInfoActivity.this, R.string.update_user_info_fail);
                        }
                    } else {
                        ToastUtil.showToast(PerfectInfoActivity.this, R.string.update_user_info_fail);
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(PerfectInfoActivity.this, R.string.update_user_info_fail);
            }
        });
    }
}

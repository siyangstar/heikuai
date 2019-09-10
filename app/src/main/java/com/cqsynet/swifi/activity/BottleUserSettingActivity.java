/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：漂流瓶用户设置页面
 *
 *
 * 创建标识：zhaosy 20161109
 */
package com.cqsynet.swifi.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;

public class BottleUserSettingActivity extends HkActivity implements View.OnClickListener {
    private ImageView mIvHead; // 头像
    private TextView mTvAge; // 生日
    private TextView mTvSex; // 性别
    private TextView mTvSign; //签名

    private static final int REQUEST_CODE_HEAD = 0x0001;
    private static final int REQUEST_CODE_SEX = 0x0002;
    private static final int REQUEST_CODE_AGE = 0x0003;
    private static final int REQUEST_CODE_SIGN = 0x0004;
    private boolean mIsRefreshHeader = false; //返回发现页面时是否需要刷新头像

    private String mHeadImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottle_user_setting);

        findViewById(R.id.ivBack_activity_bottle_user_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.tvSave_activity_bottle_user_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        mIvHead = findViewById(R.id.ivHead_activity_bottle_user_setting);
        mTvSex = findViewById(R.id.tvSex_activity_bottle_user_setting);
        mTvAge = findViewById(R.id.tvAge_activity_bottle_user_setting);
        mTvSign = findViewById(R.id.tvSign_activity_bottle_user_setting);
        findViewById(R.id.llHead_activity_bottle_user_setting).setOnClickListener(this);
        findViewById(R.id.llSex_activity_bottle_user_setting).setOnClickListener(this);
        findViewById(R.id.llAge_activity_bottle_user_setting).setOnClickListener(this);
        findViewById(R.id.llSign_activity_bottle_user_setting).setOnClickListener(this);

        init();

//        // 第一次进入，显示操作引导图层
//        if (!SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.BOTTLE_GUIDE, false)) {
//            if(Globals.g_userInfo != null && Globals.g_userInfo.setting.equals("1")) {
//                Intent intent = new Intent(this, OperateGuideActivity.class);
//                intent.putExtra("type", OperateGuideActivity.INDEX_BOTTLE);
//                startActivity(intent);
//            }
//        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.llHead_activity_bottle_user_setting) { // 用户头像
            Intent intent = new Intent(this, SelectionPictureActivity.class);
            intent.putExtra("title", "头像修改");
            intent.putExtra("isNeedCut", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 720);
            intent.putExtra("outputY", 720);
            startActivityForResult(intent, REQUEST_CODE_HEAD);
        } else if (v.getId() == R.id.llSex_activity_bottle_user_setting) { // 性别
            String[] items = getResources().getStringArray(R.array.sex);
            Intent intent = new Intent(this, UserCenterChoiceActivity.class);
            intent.putExtra("items", items);
            intent.putExtra("title", "性别");
            intent.putExtra("value", mTvSex.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_SEX);
        } else if (v.getId() == R.id.llAge_activity_bottle_user_setting) { // 年龄
            String[] items = getResources().getStringArray(R.array.age);
            Intent intent = new Intent(this, UserCenterChoiceActivity.class);
            intent.putExtra("items", items);
            intent.putExtra("title", "年龄");
            intent.putExtra("value", mTvAge.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_AGE);
        } else if (v.getId() == R.id.llSign_activity_bottle_user_setting) { // 签名
            Intent intent = new Intent(this, UserSignActivity.class);
            intent.putExtra("title", "个性签名");
            intent.putExtra("value", mTvSign.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_SIGN);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_HEAD) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("file")) {
                    mHeadImagePath = data.getStringExtra("file");
                    if(!TextUtils.isEmpty(mHeadImagePath)) {
                        GlideApp.with(this)
                                .load(mHeadImagePath)
                                .centerCrop()
                                .circleCrop()
                                .error(R.drawable.icon_profile_default_round)
                                .into(mIvHead);
                    }
                }
                mIsRefreshHeader = true;
            }
        } else if (requestCode == REQUEST_CODE_SEX) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("value")) {
                    String content = data.getStringExtra("value");
                    mTvSex.setText(content);
                }
            }
        } else if (requestCode == REQUEST_CODE_AGE) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("value")) {
                    String content = data.getStringExtra("value");
                    mTvAge.setText(content);
                }
            }
        } else if (requestCode == REQUEST_CODE_SIGN) {
            if (resultCode == RESULT_OK) {
                String nick = data.getStringExtra("data");
                mTvSign.setText(nick);
            }
        }
    }


    /**
     * 保存用户信息
     */
    public void save() {
        if (TextUtils.isEmpty(mTvSex.getText().toString())) {
            ToastUtil.showToast(this, R.string.fill_sex);
            return;
        }
        if (TextUtils.isEmpty(mTvAge.getText().toString())) {
            ToastUtil.showToast(this, R.string.fill_age);
            return;
        }
        final UserInfo userInfo = new UserInfo();
        userInfo.sex = mTvSex.getText().toString();
        userInfo.age = mTvAge.getText().toString();
        if (TextUtils.isEmpty(mTvSign.getText().toString())) {
            userInfo.sign = "";
        } else {
            userInfo.sign = mTvSign.getText().toString();
        }
        File headFile = null;
        if (!TextUtils.isEmpty(mHeadImagePath)) {
            headFile = new File(mHeadImagePath);
        }

        // 调用接口
        showProgressDialog(R.string.data_loading);
        WebServiceIf.updateUserInfo(this, headFile, userInfo, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                dismissProgressDialog();
                if (response != null) {
                    BaseResponseObject responseObj = new Gson().fromJson(response, BaseResponseObject.class);
                    if (responseObj.header != null) {
                        if (AppConstants.RET_OK.equals(responseObj.header.ret)) {
                            mIsRefreshHeader = true;
                            Globals.g_userInfo.sex = userInfo.sex;
                            Globals.g_userInfo.age = userInfo.age;
                            Globals.g_userInfo.sign = userInfo.sign;
                            Globals.g_userInfo.setting = userInfo.setting;
                            if(!TextUtils.isEmpty(mHeadImagePath)) {
                                Globals.g_userInfo.headUrl = mHeadImagePath;
                            }
                            SharedPreferencesInfo.setTagString(BottleUserSettingActivity.this, SharedPreferencesInfo.USER_INFO, new Gson().toJson(Globals.g_userInfo));
                            //更新头像
                            if(mIsRefreshHeader) {
                                Intent intent = new Intent(AppConstants.ACTION_REFRESH_HEADER);
                                String headUrl = mHeadImagePath;
                                if (!TextUtils.isEmpty(headUrl)) {
                                    intent.putExtra("headUrl", headUrl);
                                }
                                sendBroadcast(intent);
                            }
                            ToastUtil.showToast(BottleUserSettingActivity.this, "提交个人信息成功");
                            BottleUserSettingActivity.this.finish();
                            Intent intent = new Intent(BottleUserSettingActivity.this, BottleActivity.class);
                            startActivity(intent);
                        } else if (!TextUtils.isEmpty(responseObj.header.errCode)) {
                            ToastUtil.showToast(BottleUserSettingActivity.this, getResources().getString(R.string.update_user_info_fail) + "(" + responseObj
                                    .header.errCode + ")");
                        } else {
                            ToastUtil.showToast(BottleUserSettingActivity.this, R.string.update_user_info_fail);
                        }
                    } else {
                        ToastUtil.showToast(BottleUserSettingActivity.this, R.string.update_user_info_fail);
                    }
                } else {
                    ToastUtil.showToast(BottleUserSettingActivity.this, R.string.update_user_info_fail);
                }
            }

            @Override
            public void onErrorResponse() {
                dismissProgressDialog();
                ToastUtil.showToast(BottleUserSettingActivity.this, R.string.update_user_info_fail);
            }
        });
    }


    private void init() {
        if (Globals.g_userInfo == null) {
            Globals.g_userInfo = new Gson().fromJson(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.USER_INFO), UserInfo.class);
        }
        if (Globals.g_userInfo != null && !TextUtils.isEmpty(Globals.g_userInfo.headUrl)) {
            GlideApp.with(this)
                    .load(Globals.g_userInfo.headUrl)
                    .centerCrop()
                    .circleCrop()
                    .error(R.drawable.icon_profile_default_round)
                    .into(mIvHead);
        }
        if (Globals.g_userInfo.sex != null && Globals.g_userInfo.sex.equals("保密")) {
            mTvSex.setText("");
        } else {
            mTvSex.setText(Globals.g_userInfo.sex);
        }
        mTvAge.setText(Globals.g_userInfo.age);
        mTvSign.setText(Globals.g_userInfo.sign);
    }
}

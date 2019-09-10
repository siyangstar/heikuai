/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：用户中心页面
 *
 *
 * 创建标识：duxl 20141216
 * 
 * 修改内容：更换UI  br 20150210
 */
package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.RegionDao;
import com.cqsynet.swifi.db.RegionDao.KeyValue;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.UserInfoResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.util.List;

/**
 * 用户中心页面
 *
 * @author duxl
 */
public class UserCenterActivity extends HkActivity {

    private TitleBar mTitleBar;
    private ImageView mIvHead; // 头像
    private TextView mTvNick; // 昵称
    private TextView mTvSign; // 个性签名
    private TextView mTvSex; // 性别
    private TextView mTvAge; // 年龄段
    private TextView mTvCareer; // 职业
    private TextView mTvAddress; // 现居地
    private TextView mTvStep; // 人生阶段

    private static final int REQUEST_CODE_NICKNAME = 30001;
    private static final int REQUEST_CODE_ADDRESS = 30002;
    private static final int REQUEST_CODE_STEP = 30003;
    private static final int REQUEST_CODE_HEAD = 30004;
    private static final int REQUEST_CODE_CAREER = 30005;
    private static final int REQUEST_CODE_SIGN = 30006;
    private static final int REQUEST_CODE_AGE = 30007;
    private static final int REQUEST_CODE_SEX = 30008;

    private boolean mCanChooseHead = true;//是否能点击选择头像，避免多次点击打开多个图像选择activity

    private boolean mIsRefreshHeader = false; //返回发现页面时是否需要刷新头像
    private String mHeadImagePath; //头像路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);
        mTitleBar = findViewById(R.id.titlebar_activity_user_center);
        mTitleBar.findViewById(R.id.ivBack_titlebar_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRefreshHeader) {
                    Intent intent = new Intent(AppConstants.ACTION_REFRESH_HEADER);
                    if (!TextUtils.isEmpty(mHeadImagePath)) {
                        intent.putExtra("headUrl", mHeadImagePath);
                    }
                    sendBroadcast(intent);
                }
                finish();
            }
        });
        mTitleBar.setTitle(R.string.people_info);
        mIvHead = findViewById(R.id.ivHead_activity_usr_center);
        mTvNick = findViewById(R.id.tvNick_activity_user_center);
        mTvSign = findViewById(R.id.tvSign_activity_user_center);
        mTvSex = findViewById(R.id.tvSex_activity_user_center);
        mTvAge = findViewById(R.id.tvAge_activity_user_center);
        mTvCareer = findViewById(R.id.tvTrofession_activity_user_center);
        mTvAddress = findViewById(R.id.tvAddress_activity_user_center);
        mTvStep = findViewById(R.id.tvStep_activity_user_center);

        if (Globals.g_userInfo == null) {
            getUserInfo();
        } else {
            showUserInfo();
        }
    }

    public void click(View v) {
        if (v.getId() == R.id.llHead_usercenter) { // 用户头像
            // setHeadZoomParam(1, 1, 100, 100);
            // showDefaultHeadFromDialog();
            if (!mCanChooseHead) {
                return;
            }
            mCanChooseHead = false;
            Intent intent = new Intent(this, SelectionPictureActivity.class);
            intent.putExtra("title", "头像修改");
            intent.putExtra("isNeedCut", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 720);
            intent.putExtra("outputY", 720);
            startActivityForResult(intent, REQUEST_CODE_HEAD);
        } else if (v.getId() == R.id.llNickname_usercenter) { // 昵称
            Intent intent = new Intent(this, UserCenterInputActivity.class);
            intent.putExtra("title", "昵称");
            intent.putExtra("value", mTvNick.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_NICKNAME);
        } else if (v.getId() == R.id.llRemark_usercenter) { // 个性签名
            Intent intent = new Intent(this, UserSignActivity.class);
            intent.putExtra("title", "个性签名");
            intent.putExtra("value", mTvSign.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_SIGN);
        } else if (v.getId() == R.id.llSex_usercenter) { // 性别
            String[] items = getResources().getStringArray(R.array.sex);
            Intent intent = new Intent(this, UserCenterChoiceActivity.class);
            Bundle b = new Bundle();
            b.putString("type", "sex");
            b.putStringArray("items", items);
            if (mTvSex.getText().toString().equals("")) {
                b.putString("value", items[0]);
            } else {
                b.putString("value", mTvSex.getText().toString());
            }
            b.putString("title", "性别修改");
            intent.putExtras(b);
            startActivityForResult(intent, REQUEST_CODE_SEX);
        } else if (v.getId() == R.id.llAge_usercenter) {
            String[] items = getResources().getStringArray(R.array.age);
            Intent intent = new Intent(this, UserCenterChoiceActivity.class);
            Bundle b = new Bundle();
            b.putString("type", "age");
            b.putStringArray("items", items);
            if (mTvAge.getText().toString().equals("")) {
                b.putString("value", items[0]);
            } else {
                b.putString("value", mTvAge.getText().toString());
            }
            b.putString("title", "年龄段修改");
            intent.putExtras(b);
            startActivityForResult(intent, REQUEST_CODE_AGE);
        } else if (v.getId() == R.id.llCareer_usercenter) { // 职业
            final String[] items = getResources().getStringArray(R.array.profession);
            int[] icons = new int[items.length];
            for (int i = 0; i < items.length; i++) {
                if (items[i].contains("计算机")) {
                    icons[i] = R.drawable.it;
                } else if (items[i].contains("制造")) {
                    icons[i] = R.drawable.zz;
                } else if (items[i].contains("医疗")) {
                    icons[i] = R.drawable.yl;
                } else if (items[i].contains("金融")) {
                    icons[i] = R.drawable.jr;
                } else if (items[i].contains("商业")) {
                    icons[i] = R.drawable.sy;
                } else if (items[i].contains("传媒")) {
                    icons[i] = R.drawable.wh;
                } else if (items[i].contains("艺术")) {
                    icons[i] = R.drawable.ys;
                } else if (items[i].contains("法律")) {
                    icons[i] = R.drawable.fl;
                } else if (items[i].contains("教育")) {
                    icons[i] = R.drawable.jy;
                } else if (items[i].contains("行政")) {
                    icons[i] = R.drawable.xz;
                } else if (items[i].contains("模特")) {
                    icons[i] = R.drawable.mt;
                } else if (items[i].contains("空姐")) {
                    icons[i] = R.drawable.kj;
                } else if (items[i].contains("学生")) {
                    icons[i] = R.drawable.xs;
                } else {
                    icons[i] = R.drawable.qt;
                }
            }
            Intent intent = new Intent(this, UserCenterChoiceActivity.class);
            Bundle b = new Bundle();
            b.putBoolean("hasIcon", true);
            b.putStringArray("items", items);
            b.putIntArray("icons", icons);
            if (mTvCareer.getText().toString().equals("")) {
                b.putString("value", items[0]);
            } else {
                b.putString("value", mTvCareer.getText().toString());
            }
            b.putString("title", "职业修改");
            intent.putExtras(b);
            startActivityForResult(intent, REQUEST_CODE_CAREER);
        } else if (v.getId() == R.id.llAddress_usercenter) { // 现居地
            Intent intent = new Intent(this, EditAddressActivity.class);
            if (mTvAddress.getTag() != null) {
                intent.putExtra("areaCode", mTvAddress.getTag().toString());
            }
            startActivityForResult(intent, REQUEST_CODE_ADDRESS);
        } else if (v.getId() == R.id.llStep_usercenter) { // 人生阶段
            final String[] items = getResources().getStringArray(R.array.step);
            Intent intent = new Intent(this, UserCenterChoiceActivity.class);
            Bundle b = new Bundle();
            b.putString("type", "step");
            b.putStringArray("items", items);
            if (mTvStep.getText().toString().equals("")) {
                b.putString("value", items[0]);
            } else {
                b.putString("value", mTvStep.getText().toString());
            }
            b.putString("title", "人生阶段修改");
            intent.putExtras(b);
            startActivityForResult(intent, REQUEST_CODE_STEP);
        }

    }

    /**
     * 获取用户信息
     */
    private void getUserInfo() {
        // 调用接口
        showProgressDialog(R.string.data_loading);
        WebServiceIf.getUserInfo(this, new IResponseCallback() {

            @Override
            public void onResponse(String response) throws JSONException {
                dismissProgressDialog();
                if (response != null) {
                    UserInfoResponseObject responseObj = new Gson().fromJson(response, UserInfoResponseObject.class);
                    if (responseObj.header != null) {
                        if (AppConstants.RET_OK.equals(responseObj.header.ret)) {
                            if (responseObj.body != null) {
                                Globals.g_userInfo = responseObj.body;
                                SharedPreferencesInfo.setTagString(UserCenterActivity.this, SharedPreferencesInfo.USER_INFO, new Gson().toJson(responseObj.body));
                                showUserInfo();
                            } else {
                                ToastUtil.showToast(UserCenterActivity.this, R.string.get_user_info_fail);
                            }

                        } else if (!TextUtils.isEmpty(responseObj.header.errMsg)) {
                            ToastUtil.showToast(UserCenterActivity.this, responseObj.header.errMsg);
                        } else {
                            ToastUtil.showToast(UserCenterActivity.this, R.string.get_user_info_fail);
                        }
                    } else {
                        ToastUtil.showToast(UserCenterActivity.this, R.string.get_user_info_fail);
                    }
                } else {
                    ToastUtil.showToast(UserCenterActivity.this, R.string.get_user_info_fail);
                }
            }

            @Override
            public void onErrorResponse() {
                dismissProgressDialog();
                ToastUtil.showToast(UserCenterActivity.this, R.string.get_user_info_fail);
            }
        });

    }


    /**
     * 保存用户信息
     */
    public void save() {
        final UserInfo userInfo = new UserInfo();
        userInfo.nickname = mTvNick.getText().toString();
        userInfo.sign = mTvSign.getText().toString();
        userInfo.sex = mTvSex.getText().toString();
        userInfo.age = mTvAge.getText().toString();
        userInfo.career = mTvCareer.getText().toString();
        userInfo.step = mTvStep.getText().toString();
        if (mTvAddress.getTag() != null) {
            userInfo.areaCode = mTvAddress.getTag().toString();
        }
        File headFile = null;
        if (mHeadImagePath != null) {
            headFile = new File(mHeadImagePath);
        }

        // 调用接口
        showProgressDialog(R.string.data_loading);
        WebServiceIf.updateUserInfo(this, headFile, userInfo, new IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                dismissProgressDialog();
                if (response != null) {
                    BaseResponseObject responseObj = new Gson().fromJson(response, BaseResponseObject.class);
                    if (responseObj.header != null) {
                        if (AppConstants.RET_OK.equals(responseObj.header.ret)) {
                            Globals.g_userInfo.nickname = userInfo.nickname;
                            Globals.g_userInfo.birthday = userInfo.birthday;
                            Globals.g_userInfo.sex = userInfo.sex;
                            Globals.g_userInfo.age = userInfo.age;
                            Globals.g_userInfo.sign = userInfo.sign;
                            Globals.g_userInfo.career = userInfo.career;
                            Globals.g_userInfo.step = userInfo.step;
                            Globals.g_userInfo.areaCode = userInfo.areaCode;
                            if (!TextUtils.isEmpty(mHeadImagePath)) {
                                Globals.g_userInfo.headUrl = mHeadImagePath;
                            }
                            SharedPreferencesInfo.setTagString(UserCenterActivity.this, SharedPreferencesInfo.USER_INFO, new Gson().toJson(Globals.g_userInfo));
                            ToastUtil.showToast(UserCenterActivity.this, R.string.update_user_info_ok);
                        } else if (!TextUtils.isEmpty(responseObj.header.errMsg)) {
                            ToastUtil.showToast(UserCenterActivity.this, responseObj.header.errMsg);
                        } else {
                            ToastUtil.showToast(UserCenterActivity.this, R.string.update_user_info_fail);
                        }
                    } else {
                        ToastUtil.showToast(UserCenterActivity.this, R.string.update_user_info_fail);
                    }
                } else {
                    ToastUtil.showToast(UserCenterActivity.this, R.string.update_user_info_fail);
                }
            }

            @Override
            public void onErrorResponse() {
                dismissProgressDialog();
                ToastUtil.showToast(UserCenterActivity.this, R.string.update_user_info_fail);
            }
        });

    }

    /**
     * 显示用户信息
     */
    private void showUserInfo() {
        if (!TextUtils.isEmpty(Globals.g_userInfo.headUrl)) {
            GlideApp.with(this)
                    .load(Globals.g_userInfo.headUrl)
                    .centerCrop()
                    .circleCrop()
                    .error(R.drawable.icon_profile_default_round)
                    .into(mIvHead);
        }

        mTvNick.setText(Globals.g_userInfo.nickname); // 昵称
        mTvSign.setText(Globals.g_userInfo.sign); // 个性签名
        if (TextUtils.isEmpty(Globals.g_userInfo.sex)) {// 性别
            mTvSex.setText("保密");
        } else {
            mTvSex.setText(Globals.g_userInfo.sex);
        }
        mTvAge.setText(Globals.g_userInfo.age);
        mTvCareer.setText(Globals.g_userInfo.career); // 职业
        mTvAddress.setTag(Globals.g_userInfo.areaCode);
        // mTvAddress.setTag(R.id.key_3001, mUserInfo.address);
        mTvStep.setText(Globals.g_userInfo.step); // 人生阶段

        if (!TextUtils.isEmpty(Globals.g_userInfo.areaCode)) {
            RegionDao regionDao = new RegionDao(this);
            List<KeyValue> data = regionDao.getRegionByCode(Globals.g_userInfo.areaCode);
            String addr = "";
            for (KeyValue kv : data) {
                if (kv.key.length() == 2) {
                    addr = kv.value;
                } else if (kv.key.length() == 4) {
                    addr = kv.value;
                } else if (kv.key.length() == 6) {
                    if (!addr.equals(kv.value)) {
                        addr += kv.value;
                    }
                }
            }

            if (!TextUtils.isEmpty(addr)) {
                mTvAddress.setText(addr);
            }
            regionDao.closeDB();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NICKNAME) {
            if (resultCode == RESULT_OK) {
                String nick = data.getStringExtra("data");
                mTvNick.setText(nick);
                save();
                mIsRefreshHeader = true;
            }
        } else if (requestCode == REQUEST_CODE_ADDRESS) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("areaCode")) {
                    mTvAddress.setTag(data.getStringExtra("areaCode"));
                    String addr = data.getStringExtra("areaName");
                    mTvAddress.setText(addr);
                    save();
                }
            }
        } else if (requestCode == REQUEST_CODE_STEP) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("value")) {
                    String step = data.getStringExtra("value");
                    mTvStep.setText(step);
                    save();
                }
            }
        } else if (requestCode == REQUEST_CODE_CAREER) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("value")) {
                    String step = data.getStringExtra("value");
                    mTvCareer.setText(step);
                    save();
                }
            }
        } else if (requestCode == REQUEST_CODE_HEAD) {
            mCanChooseHead = true;
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("file")) {
                    mHeadImagePath = data.getStringExtra("file");
                    if (!TextUtils.isEmpty(mHeadImagePath)) {
                        GlideApp.with(this)
                                .load(mHeadImagePath)
                                .centerCrop()
                                .circleCrop()
                                .error(R.drawable.icon_profile_default_round)
                                .into(mIvHead);
                    }
                }
                save();
                mIsRefreshHeader = true;
            }
        } else if (requestCode == REQUEST_CODE_SIGN) {
            if (resultCode == RESULT_OK) {
                String sign = data.getStringExtra("data");
                mTvSign.setText(sign);
                save();
                mIsRefreshHeader = true;
            }
        } else if (requestCode == REQUEST_CODE_AGE) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("value")) {
                    String age = data.getStringExtra("value");
                    mTvAge.setText(age);
                    save();
                }
            }
        } else if (requestCode == REQUEST_CODE_SEX) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("value")) {
                    String sex = data.getStringExtra("value");
                    mTvSex.setText(sex);
                    save();
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (mIsRefreshHeader) {
            Intent intent = new Intent(AppConstants.ACTION_REFRESH_HEADER);
            if (!TextUtils.isEmpty(mHeadImagePath)) {
                intent.putExtra("headUrl", mHeadImagePath);
            }
            sendBroadcast(intent);
        }
        finish();
    }
}

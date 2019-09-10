/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：修改手机号码页面
 *
 *
 * 创建标识：duxl 20141227
 * 
 * 修改内容：更换UI  br 20150210
 */
package com.cqsynet.swifi.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.ResponseObject;
import com.cqsynet.swifi.model.UpdatePhoneRequestBody;
import com.cqsynet.swifi.model.VerifyCodeRequestBody;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.CountDown;
import com.cqsynet.swifi.util.PhoneNumberUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.LoadingDialog;
import com.cqsynet.swifi.view.LoginInputField;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

import org.json.JSONException;

/**
 * 修改手机号码页面
 *
 * @author duxl
 */
public class UpdatePhoneActivity extends HkActivity implements OnClickListener {

    private TitleBar mTitleBar;
    private TextView mTvMsg;
    private EditText mEtPhone;
    private EditText mEtVerifyCode;
    private Button mBtnGetVerify;
    private TextView mTvVerifyCodeError;
    private String mOldPhone;

//    private SmsUtil mSmsObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_phone);
        mTitleBar = findViewById(R.id.titlebar_activity_update_phone);
        mTitleBar.setTitle("手机号修改");
        mTitleBar.setLeftIconClickListener(this);
        mTvMsg = findViewById(R.id.tvMsg_activity_update_phone);
        LoginInputField updatPehone = findViewById(R.id.etPhone_activity_update_phone);
        LoginInputField verifyField = findViewById(R.id.inputFieldVerifyCode_update_phone);
        mEtPhone = updatPehone.getEditText();
        mEtVerifyCode = verifyField.getEditText();
        mBtnGetVerify = findViewById(R.id.btnGetVerify_activity_update_phone);
        mBtnGetVerify.setOnClickListener(this);
        mOldPhone = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.PHONE_NUM);
        mTvMsg.setText(mOldPhone);
        mTvVerifyCodeError = findViewById(R.id.tv_VerifyCode_error);
        mTvVerifyCodeError.setOnClickListener(this);

//        //监听短信
//        mSmsObserver = new SmsUtil(this, smsHandler);
//        getContentResolver().registerContentObserver(SmsUtil.SMS, true, mSmsObserver);
    }

    public Handler smsHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String message = (String) msg.obj;
                    mEtVerifyCode.setText(message);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        getContentResolver().unregisterContentObserver(mSmsObserver);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivBack_titlebar_layout) { // 返回
            finish();
        } else if (v.getId() == R.id.btnGetVerify_activity_update_phone) { // 请求验证码
            String phone = mEtPhone.getText().toString().trim();
            if (phone.equals(mOldPhone)) {
                ToastUtil.showToast(UpdatePhoneActivity.this, "新手机号不可与原手机号一致");
                return;
            } else if (!PhoneNumberUtil.verifyPhoneNum(phone, this)) {
                // 验证输入合法性
                ToastUtil.showToast(UpdatePhoneActivity.this, "请输入合法的手机号");
                return;
            }
            getVerifyCode(phone);
        } else if (v.getId() == R.id.tv_VerifyCode_error) {
            Intent verifyIntent = new Intent(this, SimpleWebActivity.class);
            verifyIntent.putExtra("url", AppConstants.VERIFY_CODE_ERROR);
            verifyIntent.putExtra("title", "没有收到验证码");
            startActivity(verifyIntent);
        }

    }

    /**
     * 确认修改
     *
     * @param v
     */
    public void submit(View v) {
        String phone = mEtPhone.getText().toString().trim();
        String verifyCode = mEtVerifyCode.getText().toString().trim();
        if (phone.length() != 11) {
            ToastUtil.showToast(this, "请输入11位手机号码");
            return;
        }

        if (verifyCode.length() != 6) {
            ToastUtil.showToast(this, "请输入6位验证码");
            return;
        }

        final UpdatePhoneRequestBody requestBody = new UpdatePhoneRequestBody();
        requestBody.phone = phone;
        requestBody.verifyCode = verifyCode;

        // 调用接口
        showProgressDialog("数据提交中...");
        WebServiceIf.updatePhone(this, requestBody, new IResponseCallback() {

            @SuppressWarnings("static-access")
            @Override
            public void onResponse(String response) throws JSONException {
                dismissProgressDialog();
                if (response != null) {
                    BaseResponseObject responseObj = new Gson().fromJson(response, BaseResponseObject.class);
                    if (responseObj.header != null) {
                        if (AppConstants.RET_OK.equals(responseObj.header.ret)) {
                            SharedPreferencesInfo.setTagString(UpdatePhoneActivity.this,
                                    SharedPreferencesInfo.PHONE_NUM, requestBody.phone);
                            ToastUtil.showToast(UpdatePhoneActivity.this, R.string.update_phone_ok);
                            finish();

                        } else if (!TextUtils.isEmpty(responseObj.header.errMsg)) {
                            ToastUtil.showToast(UpdatePhoneActivity.this, responseObj.header.errMsg);
                        } else {
                            ToastUtil.showToast(UpdatePhoneActivity.this, R.string.update_phone_fail);
                        }
                    } else {
                        ToastUtil.showToast(UpdatePhoneActivity.this, R.string.update_phone_fail);
                    }
                } else {
                    ToastUtil.showToast(UpdatePhoneActivity.this, R.string.update_phone_fail);
                }
            }

            @Override
            public void onErrorResponse() {
                dismissProgressDialog();
                ToastUtil.showToast(UpdatePhoneActivity.this, R.string.update_phone_fail);
            }
        });
    }

    /**
     * 找回密码获取验证码
     *
     * @param phoneNum 手机号码
     */
    private void getVerifyCode(String phoneNum) {
        final Dialog dialog = LoadingDialog.createLoadingDialog(this, "请稍候...");
        dialog.show();
        VerifyCodeRequestBody requestBody = new VerifyCodeRequestBody();
        requestBody.phoneNo = phoneNum;
        requestBody.type = "3";
        IResponseCallback getVerifyCodeCallbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                if (response != null) {
                    Gson gson = new Gson();
                    ResponseObject responseObj = gson.fromJson(response, ResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null && header.ret.equals(AppConstants.RET_OK)) {
                        ToastUtil.showToast(UpdatePhoneActivity.this, "验证码已下发,请注意查收");
                        // 开始倒计时
                        CountDown countDown = new CountDown(UpdatePhoneActivity.this, mBtnGetVerify, AppConstants.VERIFY_INTERVAL * 1000, 1000);
                        countDown.start();
                    } else {
                        ToastUtil.showToast(UpdatePhoneActivity.this, header.errMsg);
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                dialog.dismiss();
                ToastUtil.showToast(UpdatePhoneActivity.this, R.string.get_verify_code_fail);
            }
        };
        WebServiceIf.getVerifyCode(this, requestBody, getVerifyCodeCallbackIf);
    }
}
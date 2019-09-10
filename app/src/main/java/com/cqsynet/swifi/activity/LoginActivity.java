/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：尚WIFI登陆Activity。
 *
 *
 * 创建标识：zhaosy 20140922
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.LoginRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.UserInfoResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.Md5Util;
import com.cqsynet.swifi.util.PhoneNumberUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.LoadingDialog;
import com.cqsynet.swifi.view.LoginInputField;
import com.google.gson.Gson;

public class LoginActivity extends Activity implements OnClickListener {
    private EditText mPhoneNum; // 手机号输入框
    private EditText mPassword; // 密码输入框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置样式
        setContentView(R.layout.activity_login);
        Button btnLogin = findViewById(R.id.btnLogin_login);
        LoginInputField phoneNumField = findViewById(R.id.loginFieldPhoneNum_login);
        LoginInputField pswField = findViewById(R.id.loginFieldPsw_login);
        mPhoneNum = phoneNumField.getEditText();
        mPassword = pswField.getEditText();
        String phoneNum = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.PHONE_NUM);
        if (!TextUtils.isEmpty(phoneNum)) {
            mPhoneNum.setText(phoneNum);
        }
        btnLogin.setOnClickListener(this);
        TextView regist = findViewById(R.id.tvRegist_login);
        TextView forgetPsw = findViewById(R.id.tvForget_login);
        regist.setOnClickListener(this);
        forgetPsw.setOnClickListener(this);
        LinearLayout llMain = findViewById(R.id.llMain_login);
        llMain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.tvRegist_login: // 注册
                Intent registIntent = new Intent(this, RegisterActivity.class);
                startActivityForResult(registIntent, 100);
                break;
            case R.id.tvForget_login: // 找回密码
                Intent forgetIntent = new Intent(this, ForgetPswActivity.class);
                startActivity(forgetIntent);
                break;
            case R.id.btnLogin_login: // 登陆
                String phoneNum = mPhoneNum.getText().toString();
                String psw = mPassword.getText().toString();
                // 验证输入合法性
                if (!PhoneNumberUtil.verifyPhoneNum(phoneNum, this)) {
                    return;
                }
                if (TextUtils.isEmpty(psw) || psw.length() < 6) {
                    ToastUtil.showToast(LoginActivity.this, R.string.psw_warning);
                    return;
                }
                login(phoneNum, psw);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == 20) {
            login(data.getStringExtra("phoneNum"), data.getStringExtra("psw"));
        }
    }

    /**
     * @param phoneNum 要登陆的电话号码
     * @param psw      登陆密码
     * @Description: 调用登陆接口发起登陆，并处理服务器返回信息
     * @return: void
     */
    private void login(String phoneNum, String psw) {
        final Dialog dialog = LoadingDialog.createLoadingDialog(this, "请稍候...");
        dialog.show();
        final LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.phoneNo = phoneNum;
        loginRequestBody.password = Md5Util.MD5(psw);
        loginRequestBody.rsaPubKey = "";
        IResponseCallback loginCallbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    UserInfoResponseObject responseObj = gson.fromJson(response, UserInfoResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            try {
                                UserInfo body = responseObj.body;
                                if (!TextUtils.isEmpty(body.userAccount) && !TextUtils.isEmpty(body.rsaPubKey)) {
                                    SharedPreferencesInfo.setTagString(LoginActivity.this,
                                            SharedPreferencesInfo.PHONE_NUM, loginRequestBody.phoneNo);
                                    SharedPreferencesInfo.setTagString(LoginActivity.this,
                                            SharedPreferencesInfo.ACCOUNT, body.userAccount);
                                    SharedPreferencesInfo.setTagString(LoginActivity.this,
                                            SharedPreferencesInfo.RSA_KEY, body.rsaPubKey);
                                    SharedPreferencesInfo.setTagInt(LoginActivity.this,
                                            SharedPreferencesInfo.IS_LOGIIN, 1);
                                    Globals.g_userInfo = body;
                                    SharedPreferencesInfo.setTagString(LoginActivity.this, SharedPreferencesInfo.USER_INFO, gson.toJson(body));
                                    Globals.g_tempPriSign = ""; //清空签名,重新生成
                                    Intent broadcast = new Intent(AppConstants.ACTION_SOCKET_LOGIN);
                                    sendBroadcast(broadcast);
                                    Intent home = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(home);
                                    LoginActivity.this.finish();
                                }
                            } catch (ClassCastException e) {
                                ToastUtil.showToast(LoginActivity.this, R.string.login_fail);
                            }
                        } else {
                            ToastUtil.showToast(LoginActivity.this, header.errMsg);
                        }
                    }
                }
                dialog.dismiss();
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(LoginActivity.this, R.string.login_fail);
                dialog.dismiss();
            }
        };
        // 调用接口发起登陆
        WebServiceIf.login(this, loginRequestBody, loginCallbackIf);
    }

    @Override
    public void onBackPressed() {
        // 不关闭activity,直接退到桌面
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}

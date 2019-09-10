/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：尚WIFI用户注册Activity。
 *
 *
 * 创建标识：luchaowei 20140922
 * 
 * 修改内容：更换UI    br 20150210
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.RegistRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.ResponseObject;
import com.cqsynet.swifi.model.VerifyCodeRequestBody;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.CountDown;
import com.cqsynet.swifi.util.Md5Util;
import com.cqsynet.swifi.util.NetworkUtil;
import com.cqsynet.swifi.util.PhoneNumberUtil;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.LoadingDialog;
import com.cqsynet.swifi.view.LoginInputField;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

public class RegisterActivity extends Activity implements OnClickListener {
    private EditText mEtPhoneNumber; // 注册手机号输入框
    private EditText mEtPsw; // 注册密码输入框
    private EditText mEtVerifyCode; // 验证码输入框
    private CheckBox mCb;
    private TextView mTvSer;//服务条款
    private Button mBtnGetVerifyCode; // 获取验证码按钮
    private TextView mTvVerifyCodeError;
//    private SmsUtil mSmsObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置样式
        // 注册页面和找回密码页面共用一个layout
        setContentView(R.layout.activity_register_forgetpsw);
        LoginInputField phoneNumField = findViewById(R.id.inputFieldPhoneNum_register);
        LoginInputField pswField = findViewById(R.id.inputFieldPsw_register);
        LoginInputField verifyField = findViewById(R.id.inputFieldVerifyCode_register);
        mCb = findViewById(R.id.cb_activity_register);
        mEtPhoneNumber = phoneNumField.getEditText();
        mEtPsw = pswField.getEditText();
        mEtVerifyCode = verifyField.getEditText();
        mTvSer = findViewById(R.id.tvSer_activity_register);
        mTvSer.setOnClickListener(this);
        
        mBtnGetVerifyCode = findViewById(R.id.getVerifyCode_rigister);
        Button btnRigist = findViewById(R.id.btnRegist_register);
        TitleBar titleBar = findViewById(R.id.titleBar_register);
        titleBar.setLeftIconClickListener(this);
        mBtnGetVerifyCode.setOnClickListener(this);
        btnRigist.setOnClickListener(this);
        btnRigist.setText(R.string.regist_complete);
        mTvVerifyCodeError = findViewById(R.id.tv_VerifyCode_error);
        mTvVerifyCodeError.setOnClickListener(this);

//        //监听短信
//        mSmsObserver = new SmsUtil(this, smsHandler);
//      	getContentResolver().registerContentObserver(SmsUtil.SMS, true, mSmsObserver);
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
//    	getContentResolver().unregisterContentObserver(mSmsObserver);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.getVerifyCode_rigister : // 获取验证码
                String phoneNum = mEtPhoneNumber.getText().toString();
                // 验证输入合法性
                if (!PhoneNumberUtil.verifyPhoneNum(phoneNum, this)) {
                    return;
                }
                if(!NetworkUtil.isNetAvailable(this)) {
                	ToastUtil.showToast(this, "请确保网络连接畅通");
                	return;
                }
                getVerifyCode(phoneNum);
                break;
            case R.id.btnRegist_register : // 注册
                // 输入的注册手机号码
                String regPhoneNum = mEtPhoneNumber.getText().toString();
                // 输入的注册密码
                String regPsw = mEtPsw.getText().toString();
                // 输入的注册验证码
                String regVerifyCode = mEtVerifyCode.getText().toString();

                // 验证输入合法性
                if (!PhoneNumberUtil.verifyPhoneNum(regPhoneNum, this)) {
                    return;
                }
                if (TextUtils.isEmpty(regVerifyCode) || regVerifyCode.length() < 6) {
                    ToastUtil.showToast(RegisterActivity.this, R.string.verify_code_warning);
                    return;
                } else if (TextUtils.isEmpty(regPsw) || regPsw.length() < 6) {
                    ToastUtil.showToast(RegisterActivity.this, R.string.psw_warning);
                    return;
                } else if(!mCb.isChecked()) {
                	ToastUtil.showToast(RegisterActivity.this, R.string.agreement_error);
                    return;
                }
                // 注册
                regist(regPhoneNum, regPsw, regVerifyCode);
                break;
            case R.id.ivBack_titlebar_layout : // 返回
                finish();
                break;
            case R.id.tvSer_activity_register://服务条款
            	Intent intent = new Intent(this, SimpleWebActivity.class);
        		intent.putExtra("title", "用户注册及服务协议");
        		intent.putExtra("url", AppConstants.AGREEMENT_PAGE);
        		startActivity(intent);
            	break;
            case R.id.tv_VerifyCode_error:
                Intent verifyIntent = new Intent(this, SimpleWebActivity.class);
                verifyIntent.putExtra("url", AppConstants.VERIFY_CODE_ERROR);
                verifyIntent.putExtra("title", "没有收到验证码");
                startActivity(verifyIntent);
                break;
            default :
                break;
        }

    }

    /**
     * 获取验证码
     * @param phoneNum
     *            手机号码
     */
    private void getVerifyCode(String phoneNum) {
    	final Dialog dialog = LoadingDialog.createLoadingDialog(this, "请稍候...");
    	dialog.show();
        VerifyCodeRequestBody requestBody = new VerifyCodeRequestBody();
        requestBody.phoneNo = phoneNum;
		requestBody.type = "1";
        IResponseCallback callbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                if (response != null) {
                    Gson gson = new Gson();
                    ResponseObject responseObj = gson.fromJson(response, ResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null && header.ret.equals(AppConstants.RET_OK)) {
                        ToastUtil.showToast(RegisterActivity.this, "验证码已下发,请注意查收");
                        // 开始倒计时
                        CountDown countDown = new CountDown(RegisterActivity.this, mBtnGetVerifyCode, AppConstants.VERIFY_INTERVAL * 1000, 1000);
                        countDown.start();
                    } else {
                        ToastUtil.showToast(RegisterActivity.this, header.errMsg);
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                dialog.dismiss();
                ToastUtil.showToast(RegisterActivity.this, R.string.get_verify_code_fail);
            }
        };
        WebServiceIf.getVerifyCode(this, requestBody, callbackIf);
    }

    /**
     * 注册
     * 
     * @param phoneNum
     *            手机号码
     * @param psw
     *            密码
     * @param verifycode
     *            验证码
     */
    private void regist(final String phoneNum, final String psw, String verifycode) {
        RegistRequestBody requestBody = new RegistRequestBody();
        requestBody.phoneNo = phoneNum;
        requestBody.password = Md5Util.MD5(psw);
        requestBody.verifyCode = verifycode;

        IResponseCallback callbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    ResponseObject responseObj = gson.fromJson(response, ResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            ToastUtil.showToast(RegisterActivity.this, R.string.regist_success);
                            Intent data = new Intent();
                            data.putExtra("phoneNum", phoneNum);
                            data.putExtra("psw", psw);
                            setResult(20, data);
                            finish();
                        } else {
                            ToastUtil.showToast(RegisterActivity.this, header.errMsg);
                        }
                    }
                } else {
                    ToastUtil.showToast(RegisterActivity.this, R.string.request_fail_warning);
                }
            }

            @Override
            public void onErrorResponse() {
            	ToastUtil.showToast(RegisterActivity.this, R.string.request_fail_warning);
            }
        };
        WebServiceIf.regist(this, requestBody, callbackIf);
    }
}

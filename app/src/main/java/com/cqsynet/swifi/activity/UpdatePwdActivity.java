/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：修改密码页面
 *
 *
 * 创建标识：duxl 20141223
 * 
 * 修改内容：更换UI  br 20150210
 */
package com.cqsynet.swifi.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.UpdatePwdRequestBody;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.Md5Util;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.LoginInputField;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

import org.json.JSONException;

/**
 * 修改密码页面
 * @author duxl
 *
 */
public class UpdatePwdActivity extends HkActivity implements OnClickListener {

	private TitleBar mTitleBar;
	private EditText mEtOldPwd;
	private EditText mEtNewPwd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_pwd);
		mTitleBar = findViewById(R.id.titlebar_activity_update_pwd);
		mTitleBar.setTitle("密码修改");
		mTitleBar.setLeftIconClickListener(this);
		LoginInputField oldPwd = findViewById(R.id.etOldPsw_activity_update_pwd);
		LoginInputField pwdNew1 = findViewById(R.id.etNewPwd_activity_update_pwd);
		mEtOldPwd = oldPwd.getEditText();
		mEtNewPwd = pwdNew1.getEditText();
		TextView phoneNum = findViewById(R.id.tvPhoneNum_activity_update_pwd);
		String phone = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.PHONE_NUM);
		phoneNum.setText(phone);
	}


	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ivBack_titlebar_layout) { // 返回
			finish();
		}
	}


	/**
	 * 修改密码
	 * @param v
	 */
	public void update(View v) {
		String pwdOld = Md5Util.MD5(mEtOldPwd.getText().toString().trim());		
		String pwdNew = mEtNewPwd.getText().toString().trim();
		
		if(TextUtils.isEmpty(pwdOld)) {
			ToastUtil.showToast(this, "旧密码不能为空");
			return;
		} else if(TextUtils.isEmpty(pwdNew)) {
			ToastUtil.showToast(this, "新密码不能为空");
			mEtNewPwd.requestFocus();
			return;
		} else if(pwdOld.length() < 6) {
			ToastUtil.showToast(this, R.string.psw_warning);
			mEtOldPwd.requestFocus();
			return;
		} else if(pwdNew.length() < 6) {
			ToastUtil.showToast(this, R.string.psw_warning);
			mEtNewPwd.requestFocus();
			return;
		}
		
		final UpdatePwdRequestBody requestBody = new UpdatePwdRequestBody();
		requestBody.oldPwd = pwdOld;
		requestBody.newPwd = Md5Util.MD5(pwdNew);
		
		// 调用接口
		showProgressDialog("数据提交中...");
        WebServiceIf.updatePwd(this, requestBody, new IResponseCallback() {
			
			@Override
			public void onResponse(String response) throws JSONException {
				dismissProgressDialog();
				if (response != null) {
					BaseResponseObject responseObj = new Gson().fromJson(response, BaseResponseObject.class);
					if(responseObj.header != null) {
						if(AppConstants.RET_OK.equals(responseObj.header.ret)) {
//							SharedPreferencesInfo.saveTagString(UpdatePwdActivity.this, SharedPreferencesInfo.USER_PSW, requestBody.newPwd);
							ToastUtil.showToast(UpdatePwdActivity.this, R.string.update_pwd_ok);
							finish();
						} else if(!TextUtils.isEmpty(responseObj.header.errMsg)) {
							ToastUtil.showToast(UpdatePwdActivity.this, responseObj.header.errMsg);
						} else {
							ToastUtil.showToast(UpdatePwdActivity.this, R.string.update_pwd_fail);
						}
					} else {
						ToastUtil.showToast(UpdatePwdActivity.this, R.string.update_pwd_fail);
					}
				} else {
					ToastUtil.showToast(UpdatePwdActivity.this, R.string.update_pwd_fail);
				}
			}
			
			@Override
			public void onErrorResponse() {
				dismissProgressDialog();
				ToastUtil.showToast(UpdatePwdActivity.this, R.string.update_pwd_fail);
			}
		});

	}
}

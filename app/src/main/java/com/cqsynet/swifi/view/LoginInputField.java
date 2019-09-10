/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：尚WIFI自定义登陆账号密码输入框。
 *
 *
 * 创建标识：luchaowei 20141127
 * 
 * 修改内容：增加几个状态，无左侧图标和输入其他信息时用的状态     br 20150206
 */
package com.cqsynet.swifi.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cqsynet.swifi.R;

public class LoginInputField extends RelativeLayout implements TextWatcher, OnClickListener, OnFocusChangeListener {
	private EditText mEtLogin; // 登陆输入框的EditText实例
	private ImageView mIvClear; // 输入框右侧清除输入内容的按钮图标
	private ImageView mIvShowPsw; // 输入框右侧控制密码显示为星号或明文的按钮图标
	private boolean mIsPsw = false; // 当前输入框输入类型是否为密码类型
	private String mInputType = ""; // 当前输入框输入类型为验证码或者再次输入密码
	private boolean mIsHiddenPsw = true; // 当前密码框字符显示状态。true为星号显示，false为明文显示
	private int mInputLength; // 可输入的字符串的长度
	
	private static final String TYPE_PHONE_NUMBER = "0";
	private static final String TYPE_VERIFY_CODE = "1";
	private static final String TYPE_NEW_PW = "2";
	private static final String TYPE_OLD_PW = "3";
	private static final String TYPE_NAME = "4";
	private static final String TYPE_INVITE_CODE = "5";
	private static final String TYPE_PW = "6";

	public LoginInputField(Context context, AttributeSet attrs) {
		super(context, attrs);
		RelativeLayout inputField = (RelativeLayout) LayoutInflater.from(context).inflate(
				R.layout.login_input_field_layout, this, true);
		mEtLogin = inputField.findViewById(R.id.etLogin);
		mIvClear = inputField.findViewById(R.id.ivLoginClear);
		mIvShowPsw = inputField.findViewById(R.id.ivLoginShowPsw);
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoginInputField);
		mIsPsw = array.getBoolean(R.styleable.LoginInputField_inputTypePsw, false); // 获取xml中的定义
		mInputType = array.getString(R.styleable.LoginInputField_inputType); // 获取xml中的定义

		String length = array.getString(R.styleable.LoginInputField_inputLength);// 获取可输入的字符串的长度
		if (length != null) {
			mInputLength = Integer.parseInt(length);
		}

		InitInputField(mIsPsw, mInputType, mInputLength);
	}

	/**
	 * 初始化登陆输入框View。分为密码输入和非密码输入两种类型。
	 * 
	 * @param isPsw
	 *            true 密码输入，字符显示星号; false 非密码输入
	 * @param inputType
	 */
	private void InitInputField(boolean isPsw, String inputType, int length) {
		Drawable[] drawables = mEtLogin.getCompoundDrawables();
		Drawable head = null;
		int hintTextId = 0;
		if (length != 0) {
			mEtLogin.setFilters(new InputFilter[] { new InputFilter.LengthFilter(length) });
		}
//		if (isPsw) {
//			if ("2".equals(inputType)) {
//				hintTextId = R.string.input_new_psw;
//			} else if ("3".equals(inputType)) {
//				hintTextId = R.string.input_new_psw_again;
//			} else {
//				head = getResources().getDrawable(R.drawable.login_password_icon); // 输入框左侧提示图标
//				hintTextId = R.string.input_psw; // 输入框内提示文字内容
//			}
//			mEtLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // 输入框输入类型为密码
//		} else {
//			if ("0".equals(inputType)) {
//				hintTextId = R.string.input_phonenum;
//				mEtLogin.setInputType(InputType.TYPE_CLASS_NUMBER);
//			} else if ("1".equals(inputType)) {
//				hintTextId = R.string.input_verification_code;
//				mEtLogin.setInputType(InputType.TYPE_CLASS_NUMBER);
//			} else if ("4".equals(inputType)) {
//				hintTextId = R.string.input_name;
//			} else if ("5".equals(inputType)) {// 无图标的电话号码
//				hintTextId = R.string.input_phonenum; // 输入框内提示文字内容
//				mEtLogin.setInputType(InputType.TYPE_CLASS_NUMBER);
//			} else {
//				head = getResources().getDrawable(R.drawable.login_phone_num_icon);
//				hintTextId = R.string.input_phonenum; // 输入框内提示文字内容
//				mEtLogin.setInputType(InputType.TYPE_CLASS_NUMBER);
//			}
//		}
		if (inputType.equals(TYPE_PHONE_NUMBER)) {
			head = getResources().getDrawable(R.drawable.icon_phone);
			hintTextId = R.string.input_phonenum;
			mEtLogin.setInputType(InputType.TYPE_CLASS_NUMBER);
		} else if (inputType.equals(TYPE_VERIFY_CODE)) {
			head = getResources().getDrawable(R.drawable.icon_verify);
			hintTextId = R.string.input_verification_code;
			mEtLogin.setInputType(InputType.TYPE_CLASS_NUMBER);
		} else if (inputType.equals(TYPE_NEW_PW)) {
			hintTextId = R.string.input_new_psw;
			head = getResources().getDrawable(R.drawable.icon_pw);
			mEtLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // 输入框输入类型为密码
		} else if (inputType.equals(TYPE_OLD_PW)) {
			hintTextId = R.string.input_old_psw;
			head = getResources().getDrawable(R.drawable.icon_confirm_pw);
			mEtLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // 输入框输入类型为密码
		} else if (inputType.equals(TYPE_INVITE_CODE)) {
			head = getResources().getDrawable(R.drawable.icon_invite);
			hintTextId = R.string.regist_input_invite_code;
		} else if (inputType.equals(TYPE_NAME)) {
			hintTextId = R.string.input_name;
		} else if (inputType.equals(TYPE_PW)) {
			hintTextId = R.string.input_psw;
			head = getResources().getDrawable(R.drawable.icon_pw);
			mEtLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // 输入框输入类型为密码
		} 

		mEtLogin.setCompoundDrawablesWithIntrinsicBounds(head, drawables[1], drawables[2], drawables[3]);
		mEtLogin.setHintTextColor(0xFFCCCCCC);
		mEtLogin.setHint(hintTextId);
		mEtLogin.addTextChangedListener(this);
		mIvClear.setOnClickListener(this);
		mIvShowPsw.setOnClickListener(this);
		mEtLogin.setOnFocusChangeListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		if (!TextUtils.isEmpty(s)) {
			showHideClearIcon(true); // 当输入不为空时，显示右侧图标。
		} else {
			showHideClearIcon(false); // 当输入为空时，隐藏右侧图标。
		}
	}

	private void showHideClearIcon(boolean show) {
		if (show) {
			if (mIsPsw) {
				mIvShowPsw.setVisibility(View.VISIBLE); // 如果为密码输入框，显示隐藏密码控制图标
			}
			mIvClear.setVisibility(View.VISIBLE); // 显示右侧清除内容图标
		} else {
			mIvClear.setVisibility(View.GONE);
			mIvShowPsw.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.ivLoginClear: // 点击清除按钮，清除输入内容
			mEtLogin.setText("");
			break;
		case R.id.ivLoginShowPsw: // 点击显示/隐藏密码按钮，显示隐藏密码
			TransformationMethod method;
			if (mIsHiddenPsw) {
				// 如果当前设置为隐藏密码，则显示密码明文
				method = HideReturnsTransformationMethod.getInstance();
				mIvShowPsw.setImageResource(R.drawable.psw_show);
			} else {
				// 如果当前设置为密码明文，则隐藏密码
				method = PasswordTransformationMethod.getInstance();
				mIvShowPsw.setImageResource(R.drawable.psw_not_show);
			}
			mEtLogin.setTransformationMethod(method);
			Editable content = mEtLogin.getText();
			if (content != null) {
				mEtLogin.setSelection(content.length()); // 重置光标
			}
			mIsHiddenPsw = !mIsHiddenPsw;
			break;

		default:
			break;
		}
	}

	/**
	 * 获得登陆输入框的EditText View实例
	 * 
	 * @return
	 */
	public EditText getEditText() {
		return mEtLogin;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		String content = mEtLogin.getText().toString();
		if (hasFocus && !TextUtils.isEmpty(content)) {
			showHideClearIcon(true); // 聚焦输入框，且输入不为空，显示右侧图标
		} else {
			showHideClearIcon(false); // 否则，隐藏右侧图标
		}
	}

}

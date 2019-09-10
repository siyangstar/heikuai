/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：个人中心文本输入页面
 *
 *
 * 创建标识：duxl 20141222
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.LoginInputField;

public class UserCenterInputActivity extends HkActivity implements OnClickListener {

    private ImageView mIvBack;
    private TextView mTvTitle;
    private TextView mTvSave;
    private EditText mEtValue;
    private TextView mTvHint;
    private String mTitleStr;
    private String mValueStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mTitleStr = getIntent().getStringExtra("title");
        mValueStr = getIntent().getStringExtra("value");
        setContentView(R.layout.activity_user_center_input);
        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mTvTitle = findViewById(R.id.tv_title);
        mTvTitle.setText(mTitleStr + "修改");
        mTvSave = findViewById(R.id.tv_save);
        mTvSave.setOnClickListener(this);
        LoginInputField loginInputField = findViewById(R.id.etValue_activity_user_center_input);
        mEtValue = loginInputField.getEditText();
        mEtValue.setHint("请输入" + mTitleStr);
        mEtValue.setText(mValueStr);
        mEtValue.setSelection(mEtValue.getText().length());
        mTvHint = findViewById(R.id.tv_hint);
        mTvHint.setText("点击当前" + mTitleStr + "，进行修改");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_save:
                String value = mEtValue.getText().toString().trim();
                if(TextUtils.isEmpty(value)) {
                    ToastUtil.showToast(this, "不能为空");
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("data", value);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }

}

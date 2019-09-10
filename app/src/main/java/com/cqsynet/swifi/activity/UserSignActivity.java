/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：个性签名
 *
 *
 * 创建标识：zhaosy 20161109
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.R;

public class UserSignActivity extends HkActivity {

    private ImageView mIvBack;
    private TextView mTvTitle;
    private TextView mTvSave;
    private EditText mEtContent;
    private TextView mTvCount;
    private String mTitleStr;
    private String mValueStr;
    private int mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mTitleStr = getIntent().getStringExtra("title");
        mValueStr = getIntent().getStringExtra("value");
        setContentView(R.layout.activity_user_sign);
        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("data", mEtContent.getText().toString().trim());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        mTvTitle = findViewById(R.id.tv_title);
        mTvTitle.setText(mTitleStr);
        mTvSave = findViewById(R.id.tv_save);
        mTvSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("data", mEtContent.getText().toString().trim());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        mTvCount = findViewById(R.id.tvCount_activity_user_sign);
        mEtContent = findViewById(R.id.etContent_activity_user_sign);
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mCount = editable.length();
                mTvCount.setText(mCount + "/50");
            }
        });
        mEtContent.setText(mValueStr);
        mEtContent.setSelection(mEtContent.getText().length());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("data", mEtContent.getText().toString().trim());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

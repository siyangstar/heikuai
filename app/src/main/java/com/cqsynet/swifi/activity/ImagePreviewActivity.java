/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：全屏预览图片（单张）
 *
 *
 * 创建标识：br 20150319
 */
package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;

public class ImagePreviewActivity extends HkActivity {

    private ImageView mIvPreviewImg;
    private String mImgUrl;
    private int mDefaultResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mImgUrl = intent.getStringExtra("imgUrl");
        mDefaultResId = intent.getIntExtra("defaultResId", 0);

        setContentView(R.layout.activity_image_preview);
        mIvPreviewImg = findViewById(R.id.iv_activity_img_preview);
        showImage();

        mIvPreviewImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePreviewActivity.this.finish();
            }
        });
    }

    private void showImage() {
        if (!TextUtils.isEmpty(mImgUrl)) {
            GlideApp.with(this)
                    .load(mImgUrl)
                    .error(R.drawable.image_bg)
                    .into(mIvPreviewImg);
        } else if (mDefaultResId != 0) {
            GlideApp.with(this)
                    .load(mDefaultResId)
                    .into(mIvPreviewImg);
        }
    }
}

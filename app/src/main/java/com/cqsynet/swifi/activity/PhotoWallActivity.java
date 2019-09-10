/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：显示相片列表的Activity。
 *
 *
 * 创建标识：br 20150210
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.PhotoWallAdapter;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;

import java.util.ArrayList;

public class PhotoWallActivity extends HkActivity implements OnClickListener {

	private TitleBar mTitleBar;
	private ArrayList<String> list;
    private GridView mPhotoWall;
    private PhotoWallAdapter adapter;
    public static PhotoWallActivity introduction = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		setContentView(R.layout.photo_wall);
		mTitleBar = findViewById(R.id.titlebar_activity_selection_picture_from_album);
		mTitleBar.setTitle("选择图片");
		introduction = this;
		mTitleBar.setLeftIconClickListener(this);
		mPhotoWall = findViewById(R.id.photo_wall_grid);
        list = getIntent().getStringArrayListExtra("folderPath");
        adapter = new PhotoWallAdapter(this, list);
        mPhotoWall.setAdapter(adapter);
    }

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ivBack_titlebar_layout) { // 返回
			finish();
		}
	}
	
	public void chused(String path){
		Intent intent = new Intent();
		if (!TextUtils.isEmpty(path)) {
			intent.putExtra("path", path);
			setResult(Activity.RESULT_OK, intent);
			finish();
		} else {
			ToastUtil.showToast(getApplicationContext(), "图片选择出错啦");
		}
	}

    
}

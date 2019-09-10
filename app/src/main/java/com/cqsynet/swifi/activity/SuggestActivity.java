/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：意见反馈页面
 *
 *
 * 创建标识：duxl 20141223
 * 
 * 修改内容：更换UI和图片选择方式  br 20150210
 */
package com.cqsynet.swifi.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.SuggestRequestBody;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

/**
 * 意见反馈页面
 * @author duxl
 *
 */
public class SuggestActivity extends HkActivity implements OnClickListener {

	private TitleBar mTitleBar;
	private EditText mEtContent;
	private ImageView mIvImg1, mIvImg2, mIvImg3;
	private ImageView mIvImg1Del, mIvImg2Del, mIvImg3Del;
	private static final int REQUEST_CODE_IMAGE = 30004;
	private boolean mCanChooseImg = true;//是否能点击选择图片，避免多次点击打开多个图像选择activity
	private int mImgIndex; // 1设置第一个图片，2……，3……
//	private ImageLoader mImageLoader;
    private String mImagePath1 = "";
    private String mImagePath2 = "";
    private String mImagePath3 = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggest);
		mTitleBar = findViewById(R.id.titlebar_activity_suggest);
		mTitleBar.setTitle("意见反馈");
		mTitleBar.setLeftIconClickListener(this);
		mTitleBar.setRightIconClickListener(this);
		mEtContent = findViewById(R.id.etContent_activity_suggest);
		mIvImg1 = findViewById(R.id.ivImg1_activity_suggest);
		mIvImg2 = findViewById(R.id.ivImg2_activity_suggest);
		mIvImg3 = findViewById(R.id.ivImg3_activity_suggest);
		mIvImg1.setOnClickListener(this);
		mIvImg2.setOnClickListener(this);
		mIvImg3.setOnClickListener(this);
		mIvImg1Del = findViewById(R.id.ivImg1Del_activity_suggest);
		mIvImg2Del = findViewById(R.id.ivImg2Del_activity_suggest);
		mIvImg3Del = findViewById(R.id.ivImg3Del_activity_suggest);
		mEtContent.setFilters(new InputFilter[] { new InputFilter.LengthFilter(500) }); //最多输入500字
		initSaveData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_SUGGEST, false); //去掉设置中"意见反馈"上的红点"
		if(SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.NEW_SUGGEST_LIST, false)) {
			mTitleBar.setRightIcon(R.drawable.btn_right_list_true_new);
		} else {
			mTitleBar.setRightIcon(R.drawable.btn_right_list);
		}
	}

	private void initSaveData(){
		String useToSave = SharedPreferencesInfo.getTagString(getApplicationContext(), SharedPreferencesInfo.SUGGEST);
		if ("".equals(useToSave) || "null".equals(useToSave)) {
			return;
		}
		String[] useToSaveArray = useToSave.split(",", 10);
        if (useToSaveArray.length <= 0) {
            return;
        }
		//设置反馈意见
		if (!useToSaveArray[0].equals("")) {
			mEtContent.setText(useToSaveArray[0]);
		}
		//设置图片
		if (useToSaveArray.length <= 1) {
			return;
		}
		if (!TextUtils.isEmpty(useToSaveArray[1])) {
			mIvImg1Del.setVisibility(View.VISIBLE);
            mImagePath1 = useToSaveArray[1];
            GlideApp.with(this)
                    .load(useToSaveArray[1])
                    .centerCrop()
                    .error(R.drawable.addimg)
                    .into(mIvImg1);
		}
		if (!TextUtils.isEmpty(useToSaveArray[2])) {
			mIvImg2Del.setVisibility(View.VISIBLE);
            mImagePath2 = useToSaveArray[2];
            GlideApp.with(this)
                    .load(useToSaveArray[2])
                    .centerCrop()
                    .error(R.drawable.addimg)
                    .into(mIvImg2);
		}
		if (!TextUtils.isEmpty(useToSaveArray[3])) {
			mIvImg3Del.setVisibility(View.VISIBLE);
            mImagePath3 = useToSaveArray[3];
            GlideApp.with(this)
                    .load(useToSaveArray[3])
                    .centerCrop()
                    .error(R.drawable.addimg)
                    .into(mIvImg3);
		}

	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ivBack_titlebar_layout) { // 返回
			finish();
		} else if(v.getId() == R.id.ivMenu_titlebar_layout) {
			mTitleBar.setRightIcon(R.drawable.btn_right_list);
			Intent intent = new Intent(SuggestActivity.this, SuggestListActivity.class);
			startActivity(intent);
		} else if(v.getId() == R.id.ivImg1_activity_suggest) {
			if(TextUtils.isEmpty(mImagePath1)) {
				mImgIndex = 1;
				jumpToChuseImg(1, 1, 150, 150);
			} else {
				showDelImgDialog(mIvImg1, mIvImg1Del);
			}

		} else if(v.getId() == R.id.ivImg2_activity_suggest) {
			if(TextUtils.isEmpty(mImagePath2)) {
				mImgIndex = 2;
				jumpToChuseImg(1, 1, 150, 150);
			} else {
				showDelImgDialog(mIvImg2, mIvImg2Del);
			}

		} else if(v.getId() == R.id.ivImg3_activity_suggest) {
			if(TextUtils.isEmpty(mImagePath3)) {
				mImgIndex = 3;
				jumpToChuseImg(1, 1, 150, 150);
			} else {
				showDelImgDialog(mIvImg3, mIvImg3Del);
			}
		}
	}

	private void jumpToChuseImg(int aspectX, int aspectY, int outputX, int outputY){
		if (!mCanChooseImg) {
			return;
		}
		mCanChooseImg = false;
		Intent intent = new Intent(this, SelectionPictureActivity.class);
		intent.putExtra("title", "选择图片");
		intent.putExtra("isNeedCut", false);
		intent.putExtra("aspectX", aspectX);
		intent.putExtra("aspectY", aspectY);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		startActivityForResult(intent, REQUEST_CODE_IMAGE);
	}

	/**
	 * 删除图片确认对话框
	 * @param v1 显示图片的控件
	 * @param v2 显示删除图标的控件
	 */
	private void showDelImgDialog(final ImageView v1, final ImageView v2) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示");
		builder.setMessage("确定要删除该图？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
                if(v1 == mIvImg1) {
                    mImagePath1 = "";
                } else if(v1 == mIvImg2) {
                    mImagePath2 = "";
                } else if(v1 == mIvImg3) {
                    mImagePath3 = "";
                }
				v1.setImageResource(R.drawable.addimg);
				v2.setVisibility(View.GONE);
			}
		}).setNegativeButton("取消", null).create().show();

	}

	public void saveAndBack(View v){
		String useToSave = mEtContent.getText().toString().trim();//保存文字
		//保存图片路径
		useToSave += "," + mImagePath1;//1
		useToSave += "," + mImagePath2;//2
		useToSave += "," + mImagePath3;//3
		SharedPreferencesInfo.setTagString(getApplicationContext(), SharedPreferencesInfo.SUGGEST, useToSave);
		ToastUtil.showToast(SuggestActivity.this, "已保存");
		finish();
	}

	/**
	 * 提交意见反馈
	 * @param v
	 */
	public void submitSuggest(View v) {
		String content = mEtContent.getText().toString().trim();

		if(TextUtils.isEmpty(content)) {
			ToastUtil.showToast(this, "请输入你宝贵的意见");
			mEtContent.requestFocus();
			return;
		}

		SuggestRequestBody requestBody = new SuggestRequestBody();
		requestBody.content = content;

		ArrayList<File> files = new ArrayList<>();
		if(!TextUtils.isEmpty(mImagePath1)) {
			File file = new File(mImagePath1);
			if(file.exists()) {
				files.add(file);
			}
		}
        if(!TextUtils.isEmpty(mImagePath2)) {
            File file = new File(mImagePath2);
            if(file.exists()) {
                files.add(file);
            }
        }
        if(!TextUtils.isEmpty(mImagePath3)) {
            File file = new File(mImagePath3);
            if(file.exists()) {
                files.add(file);
            }
        }

		// 调用接口
		showProgressDialog("意见提交中...");
        WebServiceIf.submitSuggest(this, files, requestBody, new IResponseCallback() {

			@Override
			public void onResponse(String response) throws JSONException {
				dismissProgressDialog();
				if (response != null) {
					BaseResponseObject responseObj = new Gson().fromJson(response, BaseResponseObject.class);
					if(responseObj.header != null) {
						if(AppConstants.RET_OK.equals(responseObj.header.ret)) {
							SharedPreferencesInfo.setTagString(getApplicationContext(), SharedPreferencesInfo.SUGGEST, "");
							Intent intent = new Intent(SuggestActivity.this, SuggestListActivity.class);
							startActivity(intent);
							finish();
						} else if(!TextUtils.isEmpty(responseObj.header.errMsg)) {
							ToastUtil.showToast(SuggestActivity.this, responseObj.header.errMsg);
						} else {
							ToastUtil.showToast(SuggestActivity.this, R.string.submit_suggest_fail);
						}
					} else {
						ToastUtil.showToast(SuggestActivity.this, R.string.submit_suggest_fail);
					}
				} else {
					ToastUtil.showToast(SuggestActivity.this, R.string.submit_suggest_fail);
				}
			}

			@Override
			public void onErrorResponse() {
				dismissProgressDialog();
				ToastUtil.showToast(SuggestActivity.this, R.string.submit_suggest_fail);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE_IMAGE) {
			mCanChooseImg = true;
			if(resultCode == RESULT_OK) {
				if(data.hasExtra("file")) {
					String filePath = data.getStringExtra("file");
					if (filePath.equals(mImagePath1) || filePath.equals(mImagePath2) || filePath.equals(mImagePath3)) {
						ToastUtil.showToast(SuggestActivity.this, "该图片已添加");
						return;
					}
					if(mImgIndex == 1) {
						mIvImg1Del.setVisibility(View.VISIBLE);
                        mImagePath1 = filePath;
						GlideApp.with(this)
								.load(filePath)
								.centerCrop()
                                .error(R.drawable.addimg)
								.into(mIvImg1);
					} else if(mImgIndex == 2) {
						mIvImg2Del.setVisibility(View.VISIBLE);
                        mImagePath2 = filePath;
                        GlideApp.with(this)
                                .load(filePath)
                                .centerCrop()
                                .error(R.drawable.addimg)
                                .into(mIvImg2);
					} else if(mImgIndex == 3) {
						mIvImg3Del.setVisibility(View.VISIBLE);
                        mImagePath3 = filePath;
                        GlideApp.with(this)
                                .load(filePath)
                                .centerCrop()
                                .error(R.drawable.addimg)
                                .into(mIvImg3);
					}
				}
			}
		}
	}
}

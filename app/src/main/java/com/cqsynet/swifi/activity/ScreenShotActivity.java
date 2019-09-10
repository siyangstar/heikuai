/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：摇一摇意见反馈
 *
 *
 * 创建标识：zhaosy 20151110
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.SuggestRequestBody;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.EditableImageView;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ScreenShotActivity extends Activity implements OnClickListener {
	
	private EditableImageView mEditIV;
	private ImageButton mIBtnText;
	private RadioGroup mRgColor;
	private String mText = "";
	private Animation mAnimFadeOut;
	private ProgressDialog mProgressDialog;
	private Dialog mGuideDialog;
	
	private MyHandler mHdl = new MyHandler(this);
	static class MyHandler extends Handler {
		WeakReference<ScreenShotActivity> mWeakRef;

		public MyHandler(ScreenShotActivity activity) {
			mWeakRef = new WeakReference<ScreenShotActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ScreenShotActivity activity = mWeakRef.get();
			switch (msg.what) {
			case 0:
				activity.mGuideDialog.dismiss();
				break;
			}
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screenshot);
		
		mRgColor = findViewById(R.id.rgColor_screenshot);
		findViewById(R.id.btnBack_screenshot).setOnClickListener(this);
		findViewById(R.id.btnSend_screenshot).setOnClickListener(this);
		findViewById(R.id.btnPen_screenshot).setOnClickListener(this);
		findViewById(R.id.btnClear_screenshot).setOnClickListener(this);
		mIBtnText = findViewById(R.id.btnText_screenshot);
		mIBtnText.setOnClickListener(this);
		mEditIV = findViewById(R.id.ivScreenShot_screenshot);
		if(Globals.g_screenshot != null && !Globals.g_screenshot.isRecycled()) {
			mEditIV.setImage(Globals.g_screenshot);
		}
		
		mRgColor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
				case R.id.rbRed_screenshot:
					mEditIV.setColor(0xffff0000);
					break;
				case R.id.rbGreen_screenshot:
					mEditIV.setColor(0xff08951c);
					break;
				case R.id.rbBlue1_screenshot:
					mEditIV.setColor(0xff005cda);
					break;
				case R.id.rbYellow_screenshot:
					mEditIV.setColor(0xffedb200);
					break;
				case R.id.rbBlue2_screenshot:
					mEditIV.setColor(0xff00d4dc);
					break;
				case R.id.rbBlack_screenshot:
					mEditIV.setColor(0xff313d3d);
					break;
				}
			}
		});

		mAnimFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);		
		mAnimFadeOut.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mRgColor.setVisibility(View.GONE);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}			
		});
		
		if(Globals.g_showGuide) {
			mGuideDialog = new CustomDialog(this, R.style.round_corner_dialog, R.layout.dialog_screenshot_guide);
			mGuideDialog.setCancelable(false);
			mGuideDialog.show();
			mHdl.sendEmptyMessageDelayed(0, 3000);
			Globals.g_showGuide = false;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(Globals.g_screenshot != null) {
			Globals.g_screenshot.recycle();
			Globals.g_screenshot = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnBack_screenshot:
			finish();
			break;
		case R.id.btnSend_screenshot:
			submitSuggest();
			break;
		case R.id.btnPen_screenshot:
			if(mRgColor.getVisibility() == View.GONE) {
				mRgColor.setVisibility(View.VISIBLE);
			} else {
				mRgColor.startAnimation(mAnimFadeOut);
			}
			break;
		case R.id.btnText_screenshot:
			final Dialog dialog = new Dialog(this, R.style.round_corner_dialog);
			View view = View.inflate(this, R.layout.dialog_screenshot_input, null);
			dialog.setContentView(view);
			dialog.setCanceledOnTouchOutside(false);
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			lp.width = AppUtil.getScreenW(this) - 80; // 设置宽度
			lp.height = lp.width * 2 / 3;
			dialog.getWindow().setAttributes(lp);
			dialog.show();
			final EditText etInput = view.findViewById(R.id.etInput_dialog_screenshot);
			etInput.setText(mText);
			Selection.setSelection(etInput.getEditableText(), etInput.getText().length());
			TextView tvFinish = view.findViewById(R.id.tvFinish_dialog_screenshot);
			TextView tvCancel = view.findViewById(R.id.tvCancel_dialog_screenshot);
			tvFinish.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mText = etInput.getText().toString();
					if(TextUtils.isEmpty(mText)) {
						mIBtnText.setImageResource(R.drawable.btn_text_selector);
					} else {
						mIBtnText.setImageResource(R.drawable.btn_texted_selector);
					}
					dialog.dismiss();
				}
			});
			tvCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dialog.dismiss();
				}
			});
			etInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					}
				}
			});
			break;
		case R.id.btnClear_screenshot:
			mEditIV.clear();
			break;
		}		
	}
	
	/**
	 * 提交意见反馈
	 * 
	 */
	public void submitSuggest() {
		SuggestRequestBody requestBody = new SuggestRequestBody();
		if(TextUtils.isEmpty(mText)) {
			requestBody.content = "摇一摇反馈";
		} else {
			requestBody.content = mText;
		}
		File file = saveBitmap(mEditIV.getImage());
		ArrayList<File> files = new ArrayList<>();
		files.add(file);
		
		// 调用接口
		mProgressDialog = ProgressDialog.show(this, null, "意见提交中...");
        WebServiceIf.submitSuggest(this, files, requestBody, new IResponseCallback() {
			@Override
			public void onResponse(String response) throws JSONException {
				mProgressDialog.dismiss();
				if (response != null) {
					BaseResponseObject responseObj = new Gson().fromJson(response, BaseResponseObject.class);
					if(responseObj.header != null) {
						if(AppConstants.RET_OK.equals(responseObj.header.ret)) {
							ToastUtil.showToast(ScreenShotActivity.this, R.string.submit_suggest_success);
							finish();					
						} else if(!TextUtils.isEmpty(responseObj.header.errMsg)) {
							ToastUtil.showToast(ScreenShotActivity.this, responseObj.header.errMsg);
						} else {
							ToastUtil.showToast(ScreenShotActivity.this, R.string.submit_suggest_fail);
						}
					} else {
						ToastUtil.showToast(ScreenShotActivity.this, R.string.submit_suggest_fail);
					}
				} else {
					ToastUtil.showToast(ScreenShotActivity.this, R.string.submit_suggest_fail);
				}
			}
			
			@Override
			public void onErrorResponse() {
				mProgressDialog.dismiss();
				ToastUtil.showToast(ScreenShotActivity.this, R.string.submit_suggest_fail);
			}
		});

	}
	
	/**
	 * 保存bitmap到文件
	 * @param bm
	 * @return
	 */
	private File saveBitmap(Bitmap bm) {
		File f = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/",
				"screenshot.jpg");
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.JPEG, 80, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}
}
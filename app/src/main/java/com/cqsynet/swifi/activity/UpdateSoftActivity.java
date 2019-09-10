/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：应用升级模块
 *
 *
 * 创建标识：zhaosy 20161128
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateSoftActivity extends Activity {

	private TextView mTvNoti;
	private TextView mTvProgress;
	private String mUpdateURL;
	private ProgressBar mPb;
	private static final int REFRESHPROGRESS = 0x0000;
	private static final int ERROR = 0x0001;
	private Button mBtnReDownLoad;
	private Button mBtnQuit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.updateactivity);
		setFinishOnTouchOutside(false);

		mTvNoti = findViewById(R.id.tvNoti_update);
		mTvProgress = findViewById(R.id.tvProgress_update);
		mPb = findViewById(R.id.pb_update);
		mBtnReDownLoad = findViewById(R.id.btnReDownLoad_update);
		mBtnQuit = findViewById(R.id.btnQuit_update);
		
		Bundle bundle = getIntent().getExtras();
		mUpdateURL = bundle.getString("softAddress");

		mBtnReDownLoad.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread() {
					@Override
					public void run() {
						doUpdate(mUpdateURL);
					}
				}.start();
			}
		});
		
		mBtnQuit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		new Thread() {
			@Override
			public void run() {
				doUpdate(mUpdateURL);
			}
		}.start();

	}

	public void doUpdate(String updateURL) {
		File apkFile = null;
		File updateDir = null;
		try {
			URL url = new URL(updateURL);
			HttpURLConnection hc = (HttpURLConnection) url.openConnection();
			InputStream in = hc.getInputStream();
			hc.setConnectTimeout(15000);
			int totalSize = hc.getContentLength();
			int curSize = 0;
			int progress = 0;

			if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
				updateDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/");
			} else {
				updateDir = new File("/data/data/com.cqsynet.swifi/apk/");
			}

			if (!updateDir.exists()) {
				updateDir.mkdirs();
			}
			apkFile = new File(updateDir.getPath(), getResources().getString(R.string.app_name_en) + ".apk");
			if (apkFile.exists()) {
				apkFile.delete();
			}
			apkFile.createNewFile();

			// 修改文件夹及安装包的权限,供第三方应用访问
			try {
				Runtime.getRuntime().exec("chmod 705 " + updateDir.getPath());
				Runtime.getRuntime().exec("chmod 604 " + apkFile.getPath());
			} catch (Exception e) {
				e.printStackTrace();
			}

			FileOutputStream out = new FileOutputStream(apkFile);
			byte[] bytes = new byte[1024];
			int c;
			while ((c = in.read(bytes)) != -1) {
				out.write(bytes, 0, c);

				curSize += c;
				progress = curSize * 100 / totalSize;
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt("progress", progress);
				msg.what = REFRESHPROGRESS;
				msg.setData(bundle);
				UpdateSoftActivity.this.hdl.sendMessage(msg);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			UpdateSoftActivity.this.hdl.sendEmptyMessage(ERROR);
			return;
		}

		String apkUpdatePath = apkFile.getPath();
		if (apkFile != null) {
			// APK更新成功
			SharedPreferencesInfo.setTagBoolean(UpdateSoftActivity.this, SharedPreferencesInfo.NEW_VERSION, false);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //android N之后的版本
                Uri contentUri = FileProvider.getUriForFile(UpdateSoftActivity.this, "com.cqsynet.swifi.fileprovider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
			} else { //android N之前的版本
                intent.setDataAndType(Uri.parse("file://" + apkUpdatePath), "application/vnd.android.package-archive");
			}
			startActivity(intent);
			UpdateSoftActivity.this.finish();
		} else {
			// 未更新
			SharedPreferencesInfo.setTagBoolean(UpdateSoftActivity.this, SharedPreferencesInfo.NEW_VERSION, true);
		}
	}

	Handler hdl = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESHPROGRESS:
				mTvNoti.setText("正在下载更新，请稍候...");
				mBtnReDownLoad.setVisibility(View.GONE);
				mBtnQuit.setVisibility(View.GONE);
				int p = msg.getData().getInt("progress");
				mPb.setProgress(p);
				mTvProgress.setText(p + "%");
				break;
			case ERROR:
				mBtnReDownLoad.setVisibility(View.VISIBLE);
				mBtnQuit.setVisibility(View.VISIBLE);
				mTvNoti.setText("下载失败,请重试");
				break;
			default:
				break;
			}
		}
	};
	
	public void onBackPressed() {
		//屏蔽
	}
}
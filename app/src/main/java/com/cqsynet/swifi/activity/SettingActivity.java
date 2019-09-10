package com.cqsynet.swifi.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.CheckVersionResponseObject;
import com.cqsynet.swifi.model.LastVerInfo;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.CacheCleanManager;
import com.cqsynet.swifi.util.LogoutUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SettingActivity extends HkActivity implements OnClickListener {

	private TitleBar mTitleBar;
	private TextView mTvCacheSize;
	private ImageView mIvUpdate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mTitleBar = findViewById(R.id.titlebar_activity_setting);
		mTitleBar.setTitle("设置");
		mTitleBar.setLeftIconClickListener(this);
		findViewById(R.id.btnLogout_setting).setOnClickListener(this);
		mTvCacheSize = findViewById(R.id.tvCacheSize_activity_setting);
		getCacheSize();
		mIvUpdate = findViewById(R.id.ivNewUpdate_setting);
		findViewById(R.id.ll_setting_update).setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_SETTING, false);
		if (SharedPreferencesInfo.getTagBoolean(SettingActivity.this, SharedPreferencesInfo.NEW_VERSION, false)) {
			mIvUpdate.setVisibility(View.VISIBLE); // 如果是最新版本，则不提示红点
		} else {
			mIvUpdate.setVisibility(View.INVISIBLE);
		}
	}

	private void getCacheSize(){
		try {
			ArrayList<File> cacheFolderList = new ArrayList<File>();
//			cacheFolderList.add(getApplicationContext().getCacheDir());
			cacheFolderList.add(new File("/data/data/package_name/database/webview.db"));
			cacheFolderList.add(new File("/data/data/package_name/database/webviewCache.db"));
//			cacheFolderList.add(getApplicationContext().getDatabasePath(DBHelper.DATABASE_NAME));
			cacheFolderList.add(new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR));
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
					|| !Environment.isExternalStorageRemovable()) {
				cacheFolderList.add(new File(getExternalCacheDir().getPath() + "/" + AppConstants.CACHE_DIR));
			}
			String cacheSize = CacheCleanManager.getCacheSize(cacheFolderList);
			mTvCacheSize.setText(cacheSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ivBack_titlebar_layout) { // 返回
			finish();
		} else if (v.getId() == R.id.btnLogout_setting) { // 注销
			final CustomDialog dialog = new CustomDialog(SettingActivity.this, R.style.round_corner_dialog, R.layout.dialog_quit);
			dialog.setContentView(R.layout.dialog_quit);
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			lp.width = AppUtil.getScreenW(this) - 80; // 设置宽度
			lp.height = lp.width * 2 / 3;
			dialog.getWindow().setAttributes(lp);
			dialog.show();
			View view = dialog.getCustomView();
			view.findViewById(R.id.btnOk_dialog_quit).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LogoutUtil.disconnectWifi(SettingActivity.this);
					dialog.cancel();
				    setResult(10);
					SettingActivity.this.finish();
				}
			});
			view.findViewById(R.id.btnCancel_dialog_quit).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			});	
		} else if (v.getId() == R.id.ll_setting_update) { // 检查更新
			SharedPreferencesInfo.setTagBoolean(SettingActivity.this, SharedPreferencesInfo.NEW_VERSION, false);
			mIvUpdate.setVisibility(View.INVISIBLE);
			checkVersion();
		}
	}
	
	/**
	 * 修改手机号码
	 * @param v
	 */
	public void updatePhone(View v) {
		Intent intent = new Intent(this, UpdatePhoneActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 清除缓存
	 * @param v
	 */
	public void cleanCache(View v) {
		deleteDatabase("webview.db");
		deleteDatabase("webviewCache.db");
//		CacheCleanManager.deleteFolderFile(getApplicationContext().getCacheDir().getPath(), false);
//		CacheCleanManager.deleteFolderFile(getApplicationContext().getDatabasePath(DBHelper.DATABASE_NAME).getPath(), true);
		CacheCleanManager.deleteFolderFile(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR, false);
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			CacheCleanManager.deleteFolderFile(getExternalCacheDir().getPath(), false);
		}
		SharedPreferencesInfo.setTagString(getApplicationContext(), SharedPreferencesInfo.SUGGEST, "");
		
		SharedPreferences preferences = getSharedPreferences(SharedPreferencesInfo.SWIFI_PREFERENCES, Context.MODE_PRIVATE);
		Map<String, ?> map = preferences.getAll();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			if(key.startsWith(SharedPreferencesInfo.READED)) {
				SharedPreferencesInfo.setTagBoolean(getApplicationContext(), key, false);
			}
		}
		
		getCacheSize();
		ToastUtil.showToast(this, "您已成功清理缓存");
	}
	
	/**
	 * 修改密码
	 * @param v
	 */
	public void updatePwd(View v) {
		Intent intent = new Intent(this, UpdatePwdActivity.class);
		startActivity(intent);
	}

	
	/**
	 * 关于我们
	 * @param v
	 */
	public void about(View v) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	/**
	 * 招商热线
	 * @param v
	 */
	public void hotLine(View v) {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		Uri data = Uri.parse("tel:023-81988876");
		intent.setData(data);
		startActivity(intent);
	}
	
	/**
	 * 检查版本更新
	 */
	public void checkVersion() {
		showProgressDialog("版本检测中");
		// 调用接口
        WebServiceIf.getVersionInfo(this, new WebServiceIf.IResponseCallback() {
			@Override
			public void onResponse(String response) throws JSONException {
				dismissProgressDialog();
				if (response != null) {
                    Gson gson = new Gson();
                    CheckVersionResponseObject responseObj = gson.fromJson(response, CheckVersionResponseObject.class);                                       
                    ResponseHeader header = responseObj.header;
                    if (header != null && AppConstants.RET_OK.equals(header.ret) 
                    		&& responseObj.body != null && responseObj.body.verInfo != null) {
                    	try {
                    		showUpdateVerionDialogLogic(SettingActivity.this, responseObj.body.verInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                    	ToastUtil.showToast(SettingActivity.this, "暂无更新");
                    }
                }
			}
			
			@Override
			public void onErrorResponse() {
				dismissProgressDialog();
				ToastUtil.showToast(SettingActivity.this, "暂无更新");
			}
		});
	}
	
	/**
	 * 显示版本更新对话框逻辑
	 * 
	 * @param lastVerInfo
	 *            服务端最新版本
	 */
	public void showUpdateVerionDialogLogic(final Context context, final LastVerInfo lastVerInfo) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
			int currentVer = pi.versionCode;
			if (currentVer < lastVerInfo.verCode) {
				final CustomDialog dialog = new CustomDialog(this, R.style.round_corner_dialog, R.layout.dialog_update);
				WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
				lp.width = AppUtil.getScreenW(this) - 80; // 设置宽度
				lp.height = lp.width * 2 / 3;
				dialog.getWindow().setAttributes(lp);
				dialog.setCancelable(false);
				View view = dialog.getCustomView();
				TextView tvTitle = view.findViewById(R.id.tvTitle_dialog_update);
				TextView tvContent = view.findViewById(R.id.tvContent_dialog_update);
				final CheckBox cb = view.findViewById(R.id.cbRemind_dialog_update);
				cb.setVisibility(View.GONE);
				Button btnCancel = view.findViewById(R.id.btnCancel_dialog_update);
				Button btnOk = view.findViewById(R.id.btnOk_dialog_update);

				tvTitle.setText("版本更新 " + lastVerInfo.verName);
				tvContent.setText(lastVerInfo.des);
				btnOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(SettingActivity.this, UpdateSoftActivity.class);
						intent.putExtra("softAddress", lastVerInfo.downloadUrl);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						dialog.dismiss();
					}
				});
				btnCancel.setText("下次再说");
				btnCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
                        SharedPreferencesInfo.setTagBoolean(SettingActivity.this, SharedPreferencesInfo.NEW_VERSION, true);
                        mIvUpdate.setVisibility(View.VISIBLE);
						dialog.dismiss();
					}
				});
				dialog.show();
			} else {
				ToastUtil.showToast(SettingActivity.this, "暂无更新");
				SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_VERSION, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

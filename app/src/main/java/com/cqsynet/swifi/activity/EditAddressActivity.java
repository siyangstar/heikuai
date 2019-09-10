/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：个人中心居住地输入页面
 *
 *
 * 创建标识：duxl 20141225
 * 
 * 修改内容：更改选择地区的UI
 * 
 * 修改标识：br 20150206
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.RegionDao;
import com.cqsynet.swifi.db.RegionDao.KeyValue;

import java.util.List;

/**
 * 个人中心居住地编辑页面
 * 
 * @param areaCode
 *            - 地区编码
 * @param address
 *            - 详细地址
 * 
 * @return areaCode - String 地区code；<br />
 *         address - 详细地址；<br />
 *         areaName - 地区名称
 * @author duxl
 *
 */
public class EditAddressActivity extends HkActivity implements OnClickListener {

	private ImageView mIvBack;
	private TextView mTvTitle;
	private TextView mTvSave;
	private TextView mTvProvince;
	private TextView mTvCity;
	private TextView mTvCounty;

	private String mOldAreaCode;

	private RegionDao mRegionDao;

	List<RegionDao.KeyValue> mData;

	private final int mRequestCodeForProvince = 40001;
	private final int mRequestCodeForCity = 40002;
	private final int mRequestCodeForCounty = 40003;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_edit_addres);
        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mTvTitle = findViewById(R.id.tv_title);
        mTvTitle.setText("选择地区");
        mTvSave = findViewById(R.id.tv_save);
        mTvSave.setOnClickListener(this);
		mTvProvince = findViewById(R.id.tvProvince_activity_edit_address);
		mTvCity = findViewById(R.id.tvCity_activity_edit_address);
		mTvCounty = findViewById(R.id.tvCounty_activity_edit_address);
		mTvProvince.setOnClickListener(this);
		mTvCity.setOnClickListener(this);
		mTvCounty.setOnClickListener(this);

		mOldAreaCode = getIntent().getStringExtra("areaCode");
		mRegionDao = new RegionDao(this);
		if (!TextUtils.isEmpty(mOldAreaCode)) {
			List<KeyValue> data = mRegionDao.getRegionByCode(mOldAreaCode);
			if (data.size() >= 1) {
				mTvProvince.setText(data.get(0).value);
				mTvProvince.setTag(data.get(0).key);
				mTvCity.setClickable(true);
			}

			if (data.size() >= 2) {
				mTvCity.setText(data.get(1).value);
				mTvCity.setTag(data.get(1).key);
				mTvCounty.setClickable(true);
			}

			if (data.size() >= 3) {
				mTvCounty.setText(data.get(2).value);
				mTvCounty.setTag(data.get(2).key);
			}
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.iv_back) { // 返回
			finish();

		} else if (v.getId() == R.id.tv_save) { // 确定
			String areaCode = "";
			String areaName = "";
			if (mTvProvince.getTag() != null) {
				areaCode = mTvProvince.getTag().toString();
				areaName = mTvProvince.getText().toString();
			}

			if (mTvCity.getTag() != null) {
				areaCode = mTvCity.getTag().toString();
				areaName = mTvCity.getText().toString();
			}

			if (mTvCounty.getTag() != null) {
				areaCode = mTvCounty.getTag().toString();
				if (!mTvCounty.getText().toString().equals(areaName)) { // 避免出现: 香港香港, 澳门澳门
					areaName += mTvCounty.getText().toString();
				}
			}

			Intent intent = new Intent();
			if (!TextUtils.isEmpty(areaCode)) {
				intent.putExtra("areaCode", areaCode);
				intent.putExtra("areaName", areaName);
			}
			setResult(Activity.RESULT_OK, intent);
			finish();
		} else if (v.getId() == R.id.tvProvince_activity_edit_address) { // 选择省份
			choseRegion(mTvProvince, "选择省份", "''", mRequestCodeForProvince);

		} else if (v.getId() == R.id.tvCity_activity_edit_address) { // 选择城市
			if (mTvProvince.getTag() != null && !TextUtils.isEmpty(mTvProvince.getText())) {
				choseRegion(mTvCity, "选择城市", mTvProvince.getTag().toString(), mRequestCodeForCity);
			}

		} else if (v.getId() == R.id.tvCounty_activity_edit_address) { // 选择区县
			if (mTvCity.getTag() != null && !TextUtils.isEmpty(mTvCity.getText())) {
				choseRegion(mTvCounty, "选择区县", mTvCity.getTag().toString(), mRequestCodeForCounty);
			}
		}
	}

	/**
	 * 选择地区
	 * 
	 * @param v
	 * @param title
	 */
	private void choseRegion(final TextView v, String title, String parentCode, final int requestCode) {
		mData = mRegionDao.getRegionByParentCode(parentCode);
		String[] items = new String[mData.size()];
		for (int i = 0; i < items.length; i++) {
			items[i] = mData.get(i).value;
		}
		Intent intent = new Intent(this, UserCenterChoiceActivity.class);
		Bundle b = new Bundle();
		b.putString("type", "address");
		b.putStringArray("items", items);
		if (v.getText().toString().equals("")) {
			b.putString("value", items[0]);
		} else {
			b.putString("value", v.getText().toString());
		}
		b.putString("title", title);
		intent.putExtras(b);
		startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onDestroy() {
		mRegionDao.closeDB();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == mRequestCodeForProvince) {
			if (resultCode == RESULT_OK) {
				if (data.hasExtra("value")) {
					String value = data.getStringExtra("value");
					mTvProvince.setText(value);
					mTvProvince.setTag(mData.get(data.getIntExtra("index", 0)).key);
					mTvCity.setText("");
					mTvCity.setTag(null);
					mTvCity.setClickable(true);
					mTvCounty.setText("");
					mTvCounty.setTag(null);
					mTvCounty.setClickable(false);
				}
			}
		} else if (requestCode == mRequestCodeForCity) {
			if (resultCode == RESULT_OK) {
				if (data.hasExtra("value")) {
					String value = data.getStringExtra("value");
					mTvCity.setText(value);
					mTvCity.setTag(mData.get(data.getIntExtra("index", 0)).key);
					mTvCounty.setText("");
					mTvCounty.setTag(null);
					mTvCounty.setClickable(true);
				}
			}
		} else if (requestCode == mRequestCodeForCounty) {
			if (resultCode == RESULT_OK) {
				if (data.hasExtra("value")) {
					String value = data.getStringExtra("value");
					mTvCounty.setText(value);
					mTvCounty.setTag(mData.get(data.getIntExtra("index", 0)).key);
				}
			}
		}
	}
}

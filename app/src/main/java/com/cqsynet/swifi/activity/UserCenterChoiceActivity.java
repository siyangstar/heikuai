/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：个人中心选择页面
 *
 *
 * 创建标识：br 20150204
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.view.ChoiceView;
import com.cqsynet.swifi.view.TitleBar;

public class UserCenterChoiceActivity extends HkActivity implements OnClickListener {

	private TitleBar mTitleBar;
	private ListView mListView;
	private String mTitle;
	private String[] mItems;
	private int[] mIcons;
    private String mValue;
	private ListViewAdapter mAdapter;
	private boolean mHasIcon;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle b = this.getIntent().getExtras();
		mHasIcon = b.getBoolean("hasIcon", false);
		mTitle = b.getString("title");
		mItems = b.getStringArray("items");
        mValue = b.getString("value");

		setContentView(R.layout.activity_user_center_choice);
		mTitleBar = findViewById(R.id.titlebar_activity_user_center_choice);
		mTitleBar.setTitle(mTitle);
		mTitleBar.setLeftIconClickListener(this);
		mListView = findViewById(R.id.lvChoiceList);
		if (mHasIcon) {
			mIcons = b.getIntArray("icons");
			mAdapter = new ListViewAdapter(UserCenterChoiceActivity.this, mItems, mIcons);
		} else {
			mAdapter = new ListViewAdapter(UserCenterChoiceActivity.this, mItems);
		}
		mListView.setAdapter(mAdapter);
        if(!TextUtils.isEmpty(mValue)) {
            for (int i = 0; i < mItems.length; i++) {
                if (mItems[i].equals(mValue)) {
                    mListView.setItemChecked(i, true);
                    break;
                }
            }
        }
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					Intent intent = new Intent();
					intent.putExtra("value", mItems[arg2]);
                    intent.putExtra("index", arg2);
					setResult(Activity.RESULT_OK, intent);
					finish();
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ivBack_titlebar_layout) { // 返回
			finish();
		}
	}

	public class ListViewAdapter extends BaseAdapter {

		private Context context;
		private String[] items;
		private int[] icons;

		class ViewHolder {
			ImageView imageView;
			TextView tvName;
            RadioButton radioButton;
		}

		public ListViewAdapter(Context context, String[] items, int[] icons) {
			this.items = items;
			this.context = context;
			this.icons = icons;
		}

		public ListViewAdapter(Context context, String[] beans) {
			this.items = beans;
			this.context = context;
		}

		@Override
		public int getCount() {
			return items.length;
		}

		@Override
		public Object getItem(int position) {
			return items[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			String str = items[position];
			if (convertView == null) {
				convertView = new ChoiceView(UserCenterChoiceActivity.this);
				holder = new ViewHolder();
				holder.tvName = convertView.findViewById(R.id.tvName_choiceView);
				holder.imageView = convertView.findViewById(R.id.ivIcon_choiceView);
                holder.radioButton = convertView.findViewById(R.id.rb_choiceView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tvName.setText(str);
			if (icons != null && icons.length > position) {
				holder.imageView.setBackgroundResource(icons[position]);
			}

			return convertView;
		}
	}

}

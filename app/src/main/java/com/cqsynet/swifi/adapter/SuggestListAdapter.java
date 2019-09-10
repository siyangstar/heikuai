package com.cqsynet.swifi.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.ImagePreviewActivity;
import com.cqsynet.swifi.activity.SuggestDetailsActivity;
import com.cqsynet.swifi.model.SuggestListItem;

import java.util.ArrayList;

public class SuggestListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<SuggestListItem> mList;

	public SuggestListAdapter(Context context, ArrayList<SuggestListItem> list) {
		this.mContext = context;
		this.mList = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList == null ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_suggest_list_item, null);
			holder = new ViewHolder();
			holder.mIvIsRe = convertView.findViewById(R.id.ivIsRe_suggest_item);
			holder.mTvCenter = convertView.findViewById(R.id.tvContent_suggest_item);
			holder.mTvTime = convertView.findViewById(R.id.tvTime_suggest_item);
			holder.mImageView0 = convertView.findViewById(R.id.iv1_list_item);
			holder.mImageView1 = convertView.findViewById(R.id.iv2_list_item);
			holder.mImageView2 = convertView.findViewById(R.id.iv3_list_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.mImageView0.setImageResource(R.drawable.image_bg);
			holder.mImageView1.setImageResource(R.drawable.image_bg);
			holder.mImageView2.setImageResource(R.drawable.image_bg);
		}
		
		final SuggestListItem suggestListItem = mList.get(position);
		if (!TextUtils.isEmpty(suggestListItem.reply)) {
			holder.mIvIsRe.setBackgroundResource(R.drawable.reply_true);
		} else {
			holder.mIvIsRe.setBackgroundResource(R.drawable.reply_false);
		}
//		holder.mIvIsRe.setAlpha(0.3f);
		holder.mTvCenter.setText(suggestListItem.content);
		holder.mTvTime.setText(suggestListItem.commitDate);

		if (suggestListItem.img.length == 0) {
			return convertView;
		}

		for (int i = 0; i < suggestListItem.img.length; i++) {
			final int imgIndex = i;
			if (i == 0) {
				if (suggestListItem.img[i] != null && !suggestListItem.img[i].isEmpty()) {
					GlideApp.with(mContext)
							.load(suggestListItem.img[i])
							.centerCrop()
							.error(R.drawable.image_bg)
							.into(holder.mImageView0);
					holder.mImageView0.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setClass(mContext, ImagePreviewActivity.class);
							intent.putExtra("imgUrl", suggestListItem.img[imgIndex]);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mContext.startActivity(intent);
						}
					});
					holder.mImageView0.setVisibility(View.VISIBLE);
				} else {
					holder.mImageView0.setVisibility(View.GONE);
				}
			} else if (i == 1) {
				if (suggestListItem.img[i] != null && !suggestListItem.img[i].isEmpty()) {
					GlideApp.with(mContext)
							.load(suggestListItem.img[i])
							.centerCrop()
							.error(R.drawable.image_bg)
							.into(holder.mImageView1);
					holder.mImageView1.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setClass(mContext, ImagePreviewActivity.class);
							intent.putExtra("imgUrl", suggestListItem.img[imgIndex]);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mContext.startActivity(intent);
						}
					});
					holder.mImageView1.setVisibility(View.VISIBLE);
				} else {
					holder.mImageView1.setVisibility(View.GONE);
				}
			} else if (i == 2) {
				if (suggestListItem.img[i] != null && !suggestListItem.img[i].isEmpty()) {
					GlideApp.with(mContext)
							.load(suggestListItem.img[i])
							.centerCrop()
							.error(R.drawable.image_bg)
							.into(holder.mImageView2);
					holder.mImageView2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setClass(mContext, ImagePreviewActivity.class);
							intent.putExtra("imgUrl", suggestListItem.img[imgIndex]);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mContext.startActivity(intent);
						}
					});
					holder.mImageView2.setVisibility(View.VISIBLE);
				} else {
					holder.mImageView2.setVisibility(View.GONE);
				}
			}
				
		}

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mContext, SuggestDetailsActivity.class);
				Bundle bundle = new Bundle();
				bundle.putStringArray("img", mList.get(position).img);
				bundle.putString("commitDate", mList.get(position).getCommitDate());
				bundle.putString("content", mList.get(position).getContent());
				bundle.putString("reply", mList.get(position).getReply());
				bundle.putString("replyDate", mList.get(position).getReplyDate());
				intent.putExtra("var", bundle);
				mContext.startActivity(intent);
			}
		});
		return convertView;
	}

	private class ViewHolder {
		ImageView mIvIsRe;
		TextView mTvCenter;
		TextView mTvTime;
		ImageView mImageView0;
		ImageView mImageView1;
		ImageView mImageView2;
	}

	public void changeDatas(ArrayList<SuggestListItem> datas) {
		mList = datas;
		notifyDataSetChanged();
	}

}

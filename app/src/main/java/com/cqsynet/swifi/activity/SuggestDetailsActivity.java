package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.view.TitleBar;

public class SuggestDetailsActivity extends HkActivity implements OnClickListener {
	
	private TitleBar mTitleBar;
	private LinearLayout mLlReply;
	private TextView mTvReplyContent;
	private TextView mTvReplyData;
	private TextView mTvSuggestContent;
	private TextView mTvSuggestData;
	private ImageView[] mImageView;
	
	private String commitDate;
	private String content;
	private String[] img;
	private String reply;//回复内容
	private String replyDate;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.ivBack_titlebar_layout) { // 返回
			finish();
		} else if (v.getId() == R.id.iv1_activity_suggest_details) {
			jump(img[0]);
		} else if (v.getId() == R.id.iv2_activity_suggest_details) {
			jump(img[1]);
		} else if (v.getId() == R.id.iv3_activity_suggest_details) {
			jump(img[2]);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getBundleExtra("var");
		img = bundle.getStringArray("img");
		commitDate = bundle.getString("commitDate");
		content = bundle.getString("content");
		reply = bundle.getString("reply");
		replyDate = bundle.getString("replyDate");

		setContentView(R.layout.activity_suggest_details);
		mTitleBar = findViewById(R.id.titlebar_activity_suggest_details);
		mTitleBar.setTitle("意见反馈详情");
		mTitleBar.setLeftIconClickListener(this);
		
		mLlReply = findViewById(R.id.llReply_activity_suggest_details);
		mTvReplyContent = findViewById(R.id.tvReplyContent_activity_suggest_details);
		mTvReplyData = findViewById(R.id.tvReplyData_activity_suggest_details);
		mTvSuggestContent = findViewById(R.id.tvSuggestContent_activity_suggest_details);
		mTvSuggestData = findViewById(R.id.tvSuggestData_activity_suggest_details);
		mImageView = new ImageView[3];
		mImageView[0] = findViewById(R.id.iv1_activity_suggest_details);
		mImageView[1] = findViewById(R.id.iv2_activity_suggest_details);
		mImageView[2] = findViewById(R.id.iv3_activity_suggest_details);
		mImageView[0].setOnClickListener(this);
		mImageView[1].setOnClickListener(this);
		mImageView[2].setOnClickListener(this);
		if (img != null) {
			for (int i = 0; i < img.length; i++) {
				if (!"".equals(img[i])) {
					mImageView[i].setVisibility(View.VISIBLE);
					loadImg(mImageView[i], img[i]);
					mImageView[i].setOnClickListener(this);
				}
			}
		}
		mTvSuggestContent.setText(content);
		mTvSuggestData.setText(commitDate);
		if (reply == null || "".equals(reply)) {
			mLlReply.setVisibility(View.GONE);
		} else {
			mTvReplyContent.setText(reply);
			mTvReplyData.setText(replyDate);
		}
	}

	private void loadImg(ImageView imageView, String url){
		GlideApp.with(this)
				.load(url)
				.centerCrop()
				.error(R.drawable.image_bg)
				.into(imageView);
	}
	
	private void jump(String imgUrl){
		Intent intent = new Intent(SuggestDetailsActivity.this, ImagePreviewActivity.class);
        intent.putExtra("imgUrl", imgUrl);
        startActivity(intent);
	}
}

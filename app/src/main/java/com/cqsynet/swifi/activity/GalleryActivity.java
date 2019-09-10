/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：图集
 *
 *
 * 创建标识：zhaosy 20150420
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.target.Target;
import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.AppManager;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.CollectCacheDao;
import com.cqsynet.swifi.db.MessageDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.CollectRemoveInfo;
import com.cqsynet.swifi.model.CollectRemoveRequestBody;
import com.cqsynet.swifi.model.CollectRequestBody;
import com.cqsynet.swifi.model.CommentInfo;
import com.cqsynet.swifi.model.CommentRequestBody;
import com.cqsynet.swifi.model.CommentSubmitResponseObject;
import com.cqsynet.swifi.model.GalleryInfo;
import com.cqsynet.swifi.model.GalleryRequestBody;
import com.cqsynet.swifi.model.GalleryResponseBody;
import com.cqsynet.swifi.model.GalleryResponseObject;
import com.cqsynet.swifi.model.RecommendInfo;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.ResponseObject;
import com.cqsynet.swifi.model.ShareObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.InputManagerUtil;
import com.cqsynet.swifi.util.SoftKeyboardStateHelper;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.CommentDialog;
import com.cqsynet.swifi.view.ShareDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GalleryActivity extends HkActivity {

    public static final int MSG_DISMISS_DIALOG = 0;

    private ViewPager mViewPager;
    private LinearLayout mLlText;
    private TextView mTvTitle;
    private TextView mTvPage;
    private TextView mTvContent;
    private LinearLayout mLlComment;
    private ImageView mIvWrite;
    private EditText mEtComment;
    private TextView mTvSend;
    private FrameLayout mFlCommentCount;
    private TextView mTvCommentCount;
    private View mPlaceHolderView;
    private TextView mTvCommentHint;
    private TextView mTvCommentDisable;
    private RelativeLayout mRootLayout;
    private View mMarkView;
    private ArrayList<GalleryInfo> mImgList;
    private ArrayList<RecommendInfo> mRecommend;
    private PagerAdapter mAdapter;
    private String mId;
    private String mChannelId;
    private String mShareUrl;
    private String mSharePic;
    private String mShareTitle;
    private String mShareContent;
    private String mCanComment; // 是否可以评论
    private String mCommentMsg; // 不能评论时的提示信息
    private String mCommentCount; // 评论数量
    private String mFrom;
    private int mIndex = 0;
    private String mImgJson;
    private ProgressBar mProgress;
    private boolean mIsCollect = false;// 是否已被收藏
    private ImageView mIvCollect;
    private boolean mIsTextShow = true;
    private Dialog mDialog;
    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<GalleryActivity> mWeakRef;

        public MyHandler(GalleryActivity galleryActivity) {
            mWeakRef = new WeakReference<>(galleryActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            GalleryActivity activity = mWeakRef.get();
            switch (msg.what) {
                case MSG_DISMISS_DIALOG:
                    activity.dismissProgressDialog();
                    if (activity.mDialog != null && activity.mDialog.isShowing()) {
                        activity.mDialog.dismiss();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this); // 加入Activity容器
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置样式

        setContentView(R.layout.activity_gallery);
        findViewById(R.id.ivBack_galleryactivity).setOnClickListener(mOnClickListener);
        findViewById(R.id.ivSave_galleryactivity).setOnClickListener(mOnClickListener);
        findViewById(R.id.ivShare_galleryactivity).setOnClickListener(mOnClickListener);
        findViewById(R.id.ivCollect_galleryactivity).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btnComment_galleryactivity).setOnClickListener(mOnClickListener);
        mViewPager = findViewById(R.id.vpImage_galleryactivity);
        mLlText = findViewById(R.id.llText_activity_gallery);
        mTvTitle = findViewById(R.id.tvTitle_galleryactivity);
        mTvPage = findViewById(R.id.tvPage_galleryactivity);
        mTvContent = findViewById(R.id.tvContent_galleryactivity);
        mProgress = findViewById(R.id.progress_galleryactivity);
        mIvCollect = findViewById(R.id.ivCollect_galleryactivity);
        mLlComment = findViewById(R.id.comment_layout);
        mIvWrite = findViewById(R.id.iv_write);
        mEtComment = findViewById(R.id.et_comment);
        mTvSend = findViewById(R.id.tv_send);
        mFlCommentCount = findViewById(R.id.fl_comment_count);
        mTvCommentCount = findViewById(R.id.tv_comment_count);
        mPlaceHolderView = findViewById(R.id.place_holder_view);
        mTvCommentHint = findViewById(R.id.tv_comment_hint);
        mTvCommentDisable = findViewById(R.id.tv_comment_disable);
        mRootLayout = findViewById(R.id.root_layout);
        mMarkView = findViewById(R.id.mark_view);
        mTvSend.setOnClickListener(mOnClickListener);
        mFlCommentCount.setOnClickListener(mOnClickListener);
        mEtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    mFlCommentCount.setVisibility(View.VISIBLE);
                    mTvSend.setVisibility(View.GONE);
                    mIvWrite.setVisibility(View.VISIBLE);
                    mTvCommentHint.setVisibility(View.VISIBLE);
                } else {
                    mFlCommentCount.setVisibility(View.GONE);
                    mTvSend.setVisibility(View.VISIBLE);
                    mIvWrite.setVisibility(View.GONE);
                    mTvCommentHint.setVisibility(View.GONE);
                }
            }
        });
        mMarkView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputManagerUtil.toggleKeyboard(GalleryActivity.this);
            }
        });

        mId = getIntent().getExtras().getString("id");
        mFrom = getIntent().getExtras().getString("from");
        mIndex = getIntent().getExtras().getInt("index");
        mImgJson = getIntent().getExtras().getString("json");
        mChannelId = getIntent().getExtras().getString("channelId");

        //图集浏览统计
        if(!"web".equals(mFrom)) {
            StatisticsDao.saveStatistics(this, "gallery", mId + WebActivity.initFrom(mFrom));
        }

        //在消息中设置为已读
        String msgId = getIntent().getStringExtra("msgId");
        MessageDao.getInstance(GalleryActivity.this).updateMsgStatus(msgId); // 修改消息为已读状态
        Intent action = new Intent();
        action.setAction(AppConstants.ACTION_REFRESH_RED_POINT);
        sendBroadcast(action); // 发送广播提示此消息已读，并更新未读提示

        if (!TextUtils.isEmpty(mId)) {
            mIsCollect = CollectCacheDao.queryById(GalleryActivity.this, mId);
            if (mIsCollect) {
                mIvCollect.setBackgroundResource(R.drawable.btn_collect_on);// 如果此资讯已被收藏，显示已被收藏的图标
            } else {
                mIvCollect.setBackgroundResource(R.drawable.btn_collect_off);
            }
        }

        mImgList = new ArrayList<GalleryInfo>();
        mAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return mImgList.size();
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View view = container.findViewWithTag(position);
                if (view != null) {
                    container.removeView(view);
                }
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                if (mImgList.get(position).title == null || !mImgList.get(position).title.equals("recommend")) { // 非推荐页
                    ImageView view = container.findViewWithTag(position);
                    if (view == null) {
                        view = new ImageView(GalleryActivity.this);
                        view.setScaleType(ScaleType.FIT_CENTER);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mIsTextShow) {
                                    mLlText.setVisibility(View.GONE);
                                } else {
                                    mLlText.setVisibility(View.VISIBLE);
                                }
                                mIsTextShow = !mIsTextShow;
                            }
                        });
                        GlideApp.with(GalleryActivity.this)
                                .load(mImgList.get(position).img)
                                .fitCenter()
                                .error(R.color.transparent)
                                .into(view);
                    }
                    container.addView(view);
                    return view;
                } else { // 最后一页推荐
                    FrameLayout frame = (FrameLayout) View.inflate(GalleryActivity.this, R.layout.view_recommend, null);
                    RelativeLayout rlRecommend1 = frame.findViewById(R.id.rlRecommend1_recommend);
                    RelativeLayout rlRecommend2 = frame.findViewById(R.id.rlRecommend2_recommend);
                    ImageView imageView1 = frame.findViewById(R.id.iv1_recommend);
                    ImageView imageView2 = frame.findViewById(R.id.iv2_recommend);
                    TextView tvTitle1 = frame.findViewById(R.id.tvTitle1_recommend);
                    TextView tvTitle2 = frame.findViewById(R.id.tvTitle2_recommend);

                    int screenWidth = AppUtil.getScreenW(GalleryActivity.this);
                    int imgWidth = screenWidth - AppUtil.dp2px(GalleryActivity.this, 8);
                    int imgHeight = imgWidth * 2 / 3;
                    LinearLayout.LayoutParams recommendParams = new LinearLayout.LayoutParams(imgWidth, imgHeight);
                    rlRecommend1.setLayoutParams(recommendParams);
                    recommendParams.topMargin = AppUtil.dp2px(GalleryActivity.this, 10);
                    rlRecommend2.setLayoutParams(recommendParams);
                    container.addView(frame);

                    rlRecommend1.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mRecommend.get(0).type.equals("1")) {  //跳转图集
                                Intent intent = new Intent();
                                intent.setClass(GalleryActivity.this, GalleryActivity.class);
                                intent.putExtra("id", mRecommend.get(0).id);
                                startActivity(intent);
                            } else {  //跳转网页
                                Intent advIntent = new Intent();
                                advIntent.putExtra("url", mRecommend.get(0).url);
                                advIntent.putExtra("type", "0");
                                advIntent.putExtra("from", "newsList");
                                WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                                webDispatcher.dispatch(advIntent, GalleryActivity.this);
                            }
                        }
                    });
                    rlRecommend2.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mRecommend.size() > 1 && !TextUtils.isEmpty(mRecommend.get(1).id)) {
                                if (mRecommend.get(1).type.equals("1")) {  //跳转图集
                                    Intent intent = new Intent();
                                    intent.setClass(GalleryActivity.this, GalleryActivity.class);
                                    intent.putExtra("id", mRecommend.get(1).id);
                                    startActivity(intent);
                                } else {  //跳转网页
                                    Intent advIntent = new Intent();
                                    advIntent.putExtra("url", mRecommend.get(1).url);
                                    advIntent.putExtra("type", "0");
                                    advIntent.putExtra("from", "newsList");
                                    WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                                    webDispatcher.dispatch(advIntent, GalleryActivity.this);
                                }
                            }
                        }
                    });

                    if (!TextUtils.isEmpty(mRecommend.get(0).img)) {
                        tvTitle1.setText(mRecommend.get(0).title);
                        GlideApp.with(GalleryActivity.this)
                                .load(mRecommend.get(0).img)
                                .centerCrop()
                                .error(R.drawable.image_bg)
                                .into(imageView1);
                    }
                    if (mRecommend.size() > 1 && !TextUtils.isEmpty(mRecommend.get(1).img)) {
                        tvTitle2.setText(mRecommend.get(1).title);
                        GlideApp.with(GalleryActivity.this)
                                .load(mRecommend.get(1).img)
                                .centerCrop()
                                .error(R.drawable.image_bg)
                                .into(imageView2);
                    } else {
                        rlRecommend2.setVisibility(View.GONE);
                    }

                    return frame;
                }
            }
        };

        mViewPager.setAdapter(mAdapter);

        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int arg0) {
                GalleryInfo galleryInfo = mImgList.get(arg0);
                if (galleryInfo.title != null && galleryInfo.title.equals("recommend")) {
                    mLlText.setVisibility(View.GONE);
                    findViewById(R.id.ivSave_galleryactivity).setVisibility(View.GONE);
                    findViewById(R.id.ivShare_galleryactivity).setVisibility(View.GONE);
                    findViewById(R.id.ivCollect_galleryactivity).setVisibility(View.GONE);
                    findViewById(R.id.tvMainTitle_galleryactivity).setVisibility(View.VISIBLE);
                } else if (mFrom != null && mFrom.equals("web")) {
                    if (mIsTextShow) {
                        mLlText.setVisibility(View.VISIBLE);
                    } else {
                        mLlText.setVisibility(View.GONE);
                    }
                    findViewById(R.id.ivShare_galleryactivity).setVisibility(View.INVISIBLE);
                    findViewById(R.id.ivCollect_galleryactivity).setVisibility(View.INVISIBLE);
                    findViewById(R.id.ivSave_galleryactivity).setVisibility(View.VISIBLE);
                    findViewById(R.id.tvMainTitle_galleryactivity).setVisibility(View.GONE);
                } else {
                    if (mIsTextShow) {
                        mLlText.setVisibility(View.VISIBLE);
                    } else {
                        mLlText.setVisibility(View.GONE);
                    }
                    findViewById(R.id.ivSave_galleryactivity).setVisibility(View.VISIBLE);
                    findViewById(R.id.ivCollect_galleryactivity).setVisibility(View.VISIBLE);
                    findViewById(R.id.ivShare_galleryactivity).setVisibility(View.VISIBLE);
                    findViewById(R.id.tvMainTitle_galleryactivity).setVisibility(View.GONE);
                }
                mTvTitle.setText(galleryInfo.title);
                mTvContent.setText(galleryInfo.content);
                if (mRecommend != null && mRecommend.size() != 0) {
                    mTvPage.setText((arg0 + 1) + "/" + (mImgList.size() - 1));
                } else {
                    mTvPage.setText((arg0 + 1) + "/" + mImgList.size());
                }
            }
        });

        if (mFrom == null || !mFrom.equals("web")) { // 不是来自于图文混排
            mProgress.setVisibility(View.VISIBLE);
            GalleryRequestBody requestBody = new GalleryRequestBody();
            requestBody.id = mId;
            requestBody.channelId = mChannelId;
            IResponseCallback galleryCallbackIf = new IResponseCallback() {
                @Override
                public void onResponse(String response) {
                    mProgress.setVisibility(View.GONE);
                    if (response != null) {
                        Gson gson = new Gson();
                        GalleryResponseObject responseObj = gson.fromJson(response, GalleryResponseObject.class);
                        ResponseHeader header = responseObj.header;
                        if (header != null) {
                            if (AppConstants.RET_OK.equals(header.ret)) {
                                GalleryResponseBody body = responseObj.body;
                                if (body.imgList != null) {
                                    mImgList.clear();
                                    mImgList.addAll(body.imgList);
                                    if (body.recommend != null && body.recommend.size() > 0) {
                                        mRecommend = body.recommend;
                                        GalleryInfo temp = new GalleryInfo();
                                        temp.title = "recommend";
                                        mImgList.add(temp);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                    if (mImgList.size() > 0) {
                                        GalleryInfo galleryInfo = mImgList.get(0);
                                        mTvTitle.setText(galleryInfo.title);
                                        mTvContent.setText(galleryInfo.content);
                                        if (mRecommend != null && mRecommend.size() != 0) {
                                            mTvPage.setText(1 + "/" + (mImgList.size() - 1));
                                        } else {
                                            mTvPage.setText(1 + "/" + mImgList.size());
                                        }
                                    }

                                    mShareUrl = body.shareUrl;
                                    mSharePic = body.sharePic;
                                    mShareTitle = body.shareTitle;
                                    mShareContent = body.shareContent;
                                    mCanComment = body.commentStatus;
                                    mCommentMsg = body.commentMsg;
                                    mCommentCount = body.commentCount;

                                    if (!TextUtils.isEmpty(mCommentCount)) {
                                        int count = Integer.parseInt(mCommentCount);
                                        if (count > 0) {
                                            mPlaceHolderView.setVisibility(View.VISIBLE);
                                            mTvCommentCount.setVisibility(View.VISIBLE);
                                            mTvCommentCount.setText(mCommentCount);
                                        } else {
                                            mPlaceHolderView.setVisibility(View.GONE);
                                            mTvCommentCount.setVisibility(View.GONE);
                                        }
                                    } else {
                                        mPlaceHolderView.setVisibility(View.GONE);
                                        mTvCommentCount.setVisibility(View.GONE);
                                    }
                                    updateCommentLayout();
                                } else {
                                    ToastUtil.showToast(GalleryActivity.this, header.errMsg);
                                }
                            } else {
                                ToastUtil.showToast(GalleryActivity.this, header.errMsg);
                            }
                        }
                    }
                }

                @Override
                public void onErrorResponse() {
                    mProgress.setVisibility(View.GONE);
                    ToastUtil.showToast(GalleryActivity.this, R.string.request_fail_warning);
                }
            };
            // 调用接口获取图集
            WebServiceIf.getGallery(this, requestBody, galleryCallbackIf);
        } else { // 来自于图文混排
            findViewById(R.id.ivShare_galleryactivity).setVisibility(View.INVISIBLE);
            findViewById(R.id.ivCollect_galleryactivity).setVisibility(View.INVISIBLE);
            try {
                mImgList.clear();
                String str = URLDecoder.decode(mImgJson, "utf-8");
                Gson gson = new Gson();
                ArrayList<String> list = gson.fromJson(str, new TypeToken<ArrayList<String>>() {
                }.getType());
                for (String s : list) {
                    GalleryInfo info = new GalleryInfo();
                    info.img = s;
                    mImgList.add(info);
                }
                mAdapter.notifyDataSetChanged();
                if (mImgList.size() > 0) {
                    mTvPage.setText((mIndex + 1) + "/" + mImgList.size());
                    mViewPager.setCurrentItem(mIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SoftKeyboardStateHelper helper = new SoftKeyboardStateHelper(mRootLayout);
        helper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                mMarkView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSoftKeyboardClosed() {
                mMarkView.setVisibility(View.GONE);
            }
        });
    }

    private void updateCommentLayout() {
        if ("0".equals(mCanComment)) {
            mLlComment.setVisibility(View.VISIBLE);
            mEtComment.setEnabled(true);
            mIvWrite.setVisibility(View.VISIBLE);
            mTvCommentHint.setVisibility(View.VISIBLE);
            mTvCommentDisable.setVisibility(View.GONE);
        } else if ("2".equals(mCanComment)) {
            mLlComment.setVisibility(View.VISIBLE);
            mEtComment.setEnabled(false);
            mIvWrite.setVisibility(View.GONE);
            mTvCommentHint.setVisibility(View.GONE);
            mTvCommentDisable.setVisibility(View.VISIBLE);
            mTvCommentDisable.setText(mCommentMsg);
        } else {
            mLlComment.setVisibility(View.GONE);
        }

        int commentCount = !TextUtils.isEmpty(mCommentCount) ? Integer.parseInt(mCommentCount) : 0;
        if (commentCount == 0) {
            mPlaceHolderView.setVisibility(View.GONE);
            mTvCommentCount.setVisibility(View.GONE);
        } else if (commentCount % 10000 == 0) {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount / 10000 + "W"));
        } else if (commentCount % 1000 == 0) {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount / 1000 + "K"));
        } else if (commentCount > 10000) {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount / 10000 + "." + commentCount % 10000 / 1000 + "W"));
        } else if (commentCount > 1000) {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount / 1000 + "." + commentCount % 1000 / 100 + 'K'));
        } else {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount));
        }
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ivBack_galleryactivity:
                    HashMap<String, Activity> activityMap = AppManager.getInstance().getActivityMap();
                    if (!activityMap.containsKey("HomeActivity")) { // 如果HomeActivity处于销毁状态，app未启动，跳转新闻列表
                        Intent jumpIntent = new Intent();
                        jumpIntent.setClass(GalleryActivity.this, HomeActivity.class);
                        startActivity(jumpIntent);
                    } else {
                        finish();
                    }
                    break;
                case R.id.ivSave_galleryactivity:
                    StatisticsDao.saveStatistics(GalleryActivity.this, "imgDown", mId); // 图集下载统计
                    int i = mViewPager.getCurrentItem();
                    String imageUrl = mImgList.get(i).img;
                    String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                    try {
                        Bitmap bitmap = GlideApp.with(GalleryActivity.this)
                                .asBitmap()
                                .load(imageUrl)
                                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .get();
                        saveImage(GalleryActivity.this, bitmap, imageName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.ivShare_galleryactivity:
                    ShareObject shareObj = new ShareObject();
                    shareObj.setId(mId);
                    shareObj.setText(mShareContent);
                    shareObj.setTitle(mShareTitle);
                    shareObj.setTitleUrl(mShareUrl);
                    shareObj.setImagePath(mSharePic);
                    shareObj.setImageUrl(mSharePic);
                    shareObj.setUrl(mShareUrl);
                    shareObj.setSite("嘿快");
                    shareObj.setSiteUrl("www.heikuai.com");
                    ShareDialog dialog = new ShareDialog(GalleryActivity.this, shareObj);
                    dialog.show();
                    break;
                case R.id.ivCollect_galleryactivity:
                    if (!mIsCollect) {
                        collect();
                    } else {
                        removeCollect();
                    }
                    break;
//                case R.id.btnComment_galleryactivity:
//                    Intent newsIntent = new Intent();
//                    newsIntent.setClass(GalleryActivity.this, CommentWebActivity.class);
//                    newsIntent.putExtra("url", AppConstants.COMMENT_PAGE + "?userAccount="
//                            + SharedPreferencesInfo.getTagString(GalleryActivity.this, SharedPreferencesInfo.ACCOUNT)
//                            + "&id=" + mId);
//                    startActivity(newsIntent);
//                    break;
                case R.id.tv_send:
                    submitComment();
                    break;
                case R.id.fl_comment_count:
                    CommentActivity.launch(GalleryActivity.this, mId, mCanComment, mCommentMsg);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 从Activity容器中删除该activity
        AppManager.getInstance().removeActivity(this);
    }

    /**
     * @Description: 调用收藏接口，并处理服务器返回信息
     */
    private void collect() {
        final CollectRequestBody collectRequestBody = new CollectRequestBody();
        collectRequestBody.id = mId;
        collectRequestBody.type = "1";
        collectRequestBody.title = mShareTitle;
        collectRequestBody.url = "";
        collectRequestBody.image = mSharePic;
        collectRequestBody.source = "图集";
        IResponseCallback collectCallbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    ResponseObject responseObj = gson.fromJson(response, ResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            mIsCollect = true;
                            mIvCollect.setBackgroundResource(R.drawable.btn_collect_on);
                            ToastUtil.showToast(GalleryActivity.this, R.string.collect_success);
                            CollectCacheDao.insertData(GalleryActivity.this, "1", mId, mShareTitle, "",
                                    mSharePic, "图集", DateUtil.formatTime(System.currentTimeMillis(), "yyyy/MM/dd HH:mm"));
                        } else {
                            ToastUtil.showToast(GalleryActivity.this, R.string.request_fail_warning);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(GalleryActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.collect(this, collectRequestBody, collectCallbackIf);
    }

    private void removeCollect() {
        CollectRemoveRequestBody body = new CollectRemoveRequestBody();
        List<CollectRemoveInfo> infos = new ArrayList<>();
        CollectRemoveInfo info = new CollectRemoveInfo();
        info.type = "1";
        info.id = mId;
        info.url = "";
        info.title = mShareTitle;
        infos.add(info);
        body.favorList = infos;
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    BaseResponseObject responseObj = gson.fromJson(response, BaseResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            mIsCollect = false;
                            mIvCollect.setBackgroundResource(R.drawable.btn_collect_off);
                            ToastUtil.showToast(GalleryActivity.this, R.string.remove_collect_success);
                            CollectCacheDao.deleteData(GalleryActivity.this, mId);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(GalleryActivity.this, R.string.request_fail_warning);
            }
        };
        WebServiceIf.removeCollect(this, body, callback);
    }

    /**
     * 保存图片至本地
     *
     * @param context
     * @param bmp
     * @param fileName
     */
    public static void saveImage(Context context, Bitmap bmp, String fileName) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR,
                "downloads");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, fileName);
        if (file.exists()) {
            ToastUtil.showToast(context, "图片已保存");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // // 其次把文件插入到系统图库
        // try {
        // MediaStore.Images.Media.insertImage(context.getContentResolver(),
        // file.getAbsolutePath(), fileName, null);
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }
        // 最后通知图库更新
        MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
        ToastUtil.showToast(context, "成功保存图片至" + appDir.getAbsolutePath());
    }

    private void submitComment() {
        if (TextUtils.isEmpty(mEtComment.getText().toString().trim())) {
            return;
        }

        showProgressDialog(R.string.comment_submitting);

        CommentRequestBody body = new CommentRequestBody();
        body.type = "0";
        body.newsId = mId;
        body.levelOneId = "";
        body.levelTwoId = "";
        body.content = mEtComment.getText().toString();

        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                dismissProgressDialog();
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    CommentSubmitResponseObject object = gson.fromJson(response, CommentSubmitResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        mDialog = CommentDialog.createDialog(GalleryActivity.this, R.drawable.ic_success, getString(R.string.comment_success));
                        mDialog.show();
                        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 1000);
                        if (!TextUtils.isEmpty(object.body.id)) {
                            CommentInfo commentInfo = new CommentInfo();
                            commentInfo.id = object.body.id;
                            commentInfo.userAccount = Globals.g_userInfo.userAccount;
                            commentInfo.nickname = Globals.g_userInfo.nickname;
                            commentInfo.headUrl = Globals.g_userInfo.headUrl;
                            commentInfo.userLevel = "0";
                            commentInfo.content = mEtComment.getText().toString();
                            commentInfo.date = String.valueOf(System.currentTimeMillis());
                            commentInfo.like = "0";
                            commentInfo.likeCount = "0";
                            commentInfo.replyCount = "0";

                            mLlComment.setVisibility(View.VISIBLE);
                            if (!mCommentCount.isEmpty()) {
                                mCommentCount = String.valueOf(Integer.parseInt(mCommentCount) + 1);
                            } else {
                                mCommentCount = "1";
                            }
                            mPlaceHolderView.setVisibility(View.VISIBLE);
                            mTvCommentCount.setVisibility(View.VISIBLE);
                            mTvCommentCount.setText(mCommentCount);
                        }
                    }
                }

                mEtComment.setText("");
            }

            @Override
            public void onErrorResponse() {
                mDialog = CommentDialog.createDialog(GalleryActivity.this, R.drawable.ic_failure, getString(R.string.comment_fail));
                mDialog.show();
                mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 1000);
            }
        };
        WebServiceIf.submitComment(GalleryActivity.this, body, callback);
    }
}
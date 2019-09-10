package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.AppManager;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.NewsItemsAdapter;
import com.cqsynet.swifi.db.CollectCacheDao;
import com.cqsynet.swifi.db.MessageDao;
import com.cqsynet.swifi.db.NewsCacheDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.AdvInfoObject;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.CollectRemoveInfo;
import com.cqsynet.swifi.model.CollectRemoveRequestBody;
import com.cqsynet.swifi.model.CollectRequestBody;
import com.cqsynet.swifi.model.NewsCacheObject;
import com.cqsynet.swifi.model.NewsItemInfo;
import com.cqsynet.swifi.model.NewsListRequestBody;
import com.cqsynet.swifi.model.NewsTopicResponseBody;
import com.cqsynet.swifi.model.NewsTopicResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.ResponseObject;
import com.cqsynet.swifi.model.ShareObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.ShareDialog;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TopicActivity extends HkActivity implements OnClickListener {

    private TextView mTvTitle;
    private ImageView mIvBack;
    private ImageView mIvCollect;
    private ImageView mIvShare;
    private PullToRefreshListView mPTRListView;
    private LinearLayout mLlBanner; // banner头图的容器layout
    private ImageView mIvBanner;
    private TextView mTvBannerTitle; // 头图标题
    private NewsItemsAdapter mAdapter;
    private ArrayList<NewsItemInfo> mList = new ArrayList<NewsItemInfo>(); // 专题列表数据
    private static String mTopicId; // 当前的专题id
    private int mNewsCount = 0; // 当前专题新闻总条数
    private NewsCacheDao mCacheDao;
    private ProgressBar mProgressBar;
    private Animation mAnim;
    private boolean mIsShowAnim = true;
    private long mFreshTime = 0; // 刷新时间
    private boolean mIsRefreshing = false; //是否正在刷新
    private boolean mIsCollect = false;
    private String mImage; // 封面缩略图
    private String mShareTitle;
    private String mShareContent;
    private String mShareUrl;
    private String mSharePic;
    private String mFrom; // 从何处打开

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_news_topic);

        mTvTitle = findViewById(R.id.tv_title);
        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mIvCollect = findViewById(R.id.iv_collect);
        mIvCollect.setOnClickListener(this);
        mIvShare = findViewById(R.id.iv_share);
        mIvShare.setOnClickListener(this);
        mPTRListView = findViewById(R.id.listview_news_topic);
        mPTRListView.setPullToRefreshOverScrollEnabled(false);
        mLlBanner = (LinearLayout) View.inflate(this, R.layout.topic_header, null);
        mIvBanner = mLlBanner.findViewById(R.id.iv_topic_header);
        mTvBannerTitle = mLlBanner.findViewById(R.id.tv_topic_header);
        setAdvHeight(mIvBanner);
        mPTRListView.getRefreshableView().addHeaderView(mLlBanner);

        mProgressBar = findViewById(R.id.progress_topic);
        mAnim = AnimationUtils.loadAnimation(this, R.anim.alpha);

        mTopicId = getIntent().getStringExtra("id");
        mFrom = getIntent().getStringExtra("from");

        //专题浏览统计
        StatisticsDao.saveStatistics(this, "topic", mTopicId + WebActivity.initFrom(mFrom));

        //在消息中设置为已读
        String msgId = getIntent().getStringExtra("msgId");
        MessageDao.getInstance(TopicActivity.this).updateMsgStatus(msgId); // 修改消息为已读状态
        Intent action = new Intent();
        action.setAction(AppConstants.ACTION_REFRESH_RED_POINT);
        sendBroadcast(action); // 发送广播提示此消息已读，并更新未读提示

        mPTRListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                getTopicList("");
            }
        });

        mPTRListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                int curCount = mAdapter.getCount();
                if (mNewsCount != 0) {
                    if (curCount < mNewsCount && !mIsRefreshing) {
                        // 如果当前显示的总条数小雨当前频道新闻总条数，继续加载
                        NewsItemInfo info = (NewsItemInfo) mAdapter.getItem(curCount - 1);
                        getTopicList(info.id);
                        mIsRefreshing = true;
                    } else if (!mIsRefreshing) {
                        // 已到最底端，停止加载，提示无更多内容
                        ToastUtil.showToast(TopicActivity.this, R.string.no_more_item);
                    }
                }

            }
        });

        mPTRListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id < 0) {
                    return;
                }

                mPTRListView.getRefreshableView().setEnabled(false);
                hdl.sendEmptyMessageDelayed(0, 200);

                NewsItemInfo info = mList.get((int) id);
                mAdapter.setNewsClickJump(TopicActivity.this, info, null, mFrom);
            }
        });

        mCacheDao = NewsCacheDao.getInstance(this);
        NewsCacheObject cacheObject = mCacheDao.getNews(mTopicId, true);
        if (cacheObject != null) {
            if (!TextUtils.isEmpty(cacheObject.topicTitle)) {
                mTvTitle.setText(cacheObject.topicTitle);
                mShareTitle = cacheObject.topicTitle;
                mShareContent = cacheObject.shareContent;
                mShareUrl = cacheObject.shareUrl;
                mSharePic = cacheObject.sharePic;
            }
            mFreshTime = cacheObject.date;
            if (!DateUtil.overOneHour(mFreshTime)) {
                mList = cacheObject.newsList;
                mTvBannerTitle.setText(cacheObject.summary);
                if (!TextUtils.isEmpty(cacheObject.imgUrl)) {
                    mImage = cacheObject.imgUrl;
                    GlideApp.with(this)
                            .load(cacheObject.imgUrl)
                            .centerCrop()
                            .error(R.drawable.image_bg)
                            .into(mIvBanner);
                } else {
                    mLlBanner.setVisibility(View.GONE);
                }
                mProgressBar.setVisibility(View.GONE);
            } else {
                getTopicList("");
            }
        } else {
            getTopicList("");
        }

        mAdapter = new NewsItemsAdapter(this, mList);
        mPTRListView.setAdapter(mAdapter);

        if (!TextUtils.isEmpty(mTopicId)) {
            mIsCollect = CollectCacheDao.queryById(TopicActivity.this, mTopicId);
            if (mIsCollect) {
                mIvCollect.setImageResource(R.drawable.btn_collect_green_on);
            } else {
                mIvCollect.setImageResource(R.drawable.btn_collect_green_off);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mTopicId = getIntent().getStringExtra("id");
        NewsCacheObject cacheObject = mCacheDao.getNews(mTopicId, true);
        if (cacheObject != null) {
            if (!TextUtils.isEmpty(cacheObject.topicTitle)) {
                mTvTitle.setText(cacheObject.topicTitle);
            }
            mFreshTime = cacheObject.date;
            if (!DateUtil.overOneHour(mFreshTime)) {
                mList = cacheObject.newsList;
                mTvBannerTitle.setText(cacheObject.summary);
                if (!TextUtils.isEmpty(cacheObject.imgUrl)) {
                    mImage = cacheObject.imgUrl;
                    GlideApp.with(this)
                            .load(cacheObject.imgUrl)
                            .centerCrop()
                            .error(R.drawable.image_bg)
                            .into(mIvBanner);
                } else {
                    mLlBanner.setVisibility(View.GONE);
                }
                mProgressBar.setVisibility(View.GONE);
            } else {
                getTopicList("");
            }
        } else {
            getTopicList("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            HashMap<String, Activity> activityMap = AppManager.getInstance().getActivityMap();
            if (!activityMap.containsKey("HomeActivity")) { // 如果HomeActivity处于销毁状态，app未启动，跳转新闻列表
                Intent jumpIntent = new Intent();
                jumpIntent.setClass(TopicActivity.this, HomeActivity.class);
                startActivity(jumpIntent);
            } else {
                finish();
            }
        } else if (v.getId() == R.id.iv_collect) {
            if (!mIsCollect) {
                collect();
            } else {
                removeCollect();
            }
        } else if (v.getId() == R.id.iv_share) {
            share();
        }
    }

    private void share() {
        ShareObject shareObj = new ShareObject();
        shareObj.setId(mTopicId);
        shareObj.setText(mShareContent);
        shareObj.setTitle(mShareTitle);
        shareObj.setTitleUrl(mShareUrl);
        shareObj.setImagePath(mSharePic);
        shareObj.setImageUrl(mSharePic);
        shareObj.setUrl(mShareUrl);
        shareObj.setSite("嘿快");
        shareObj.setSiteUrl("www.heikuai.com");
        ShareDialog dialog = new ShareDialog(this, shareObj);
        dialog.show();
    }

    private void collect() {
        final CollectRequestBody collectRequestBody = new CollectRequestBody();
        collectRequestBody.id = mTopicId;
        collectRequestBody.type = "2";
        collectRequestBody.title = mTvTitle.getText().toString();
        collectRequestBody.url = "";
        collectRequestBody.image = mImage;
        collectRequestBody.source = "专题";
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
                            mIvCollect.setImageResource(R.drawable.btn_collect_green_on);
                            ToastUtil.showToast(TopicActivity.this, R.string.collect_success);
                            CollectCacheDao.insertData(TopicActivity.this, "2", mTopicId, mTvTitle.getText().toString(),
                                    "", mImage, "专题", DateUtil.formatTime(System.currentTimeMillis(), "yyyy/MM/dd HH:mm"));
                        } else {
                            ToastUtil.showToast(TopicActivity.this, R.string.request_fail_warning);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(TopicActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.collect(this, collectRequestBody, collectCallbackIf);
    }

    private void removeCollect() {
        CollectRemoveRequestBody body = new CollectRemoveRequestBody();
        List<CollectRemoveInfo> infos = new ArrayList<>();
        CollectRemoveInfo info = new CollectRemoveInfo();
        info.type = "2";
        info.id = mTopicId;
        info.url = "";
        info.title = mTvTitle.getText().toString();
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
                            CollectCacheDao.deleteData(TopicActivity.this, mTopicId);
                            mIsCollect = false;
                            mIvCollect.setImageResource(R.drawable.btn_collect_green_off);
                            ToastUtil.showToast(TopicActivity.this, R.string.remove_collect_success);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(TopicActivity.this, R.string.request_fail_warning);
            }
        };
        WebServiceIf.removeCollect(this, body, callback);
    }

    /**
     * @param startId 从startId这条新闻获取接下来的10条
     * @Description: 获取专题列表
     * @return: void
     */
    private void getTopicList(final String startId) {
        final NewsListRequestBody body = new NewsListRequestBody();
        body.id = mTopicId;
        body.start = startId;
        IResponseCallback callbackIf = new IResponseCallback() {

            @Override
            public void onResponse(String response) {
                mPTRListView.onRefreshComplete();
                mIsRefreshing = false;
                if (response != null && !TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    try {
                        NewsTopicResponseObject responseObj = gson.fromJson(response, NewsTopicResponseObject.class);
                        ResponseHeader header = responseObj.header;
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            // 保存广告信息
                            mShareTitle = responseObj.body.shareTitle;
                            mShareContent = responseObj.body.shareContent;
                            mShareUrl = responseObj.body.shareUrl;
                            mSharePic = responseObj.body.sharePic;
                            for (NewsItemInfo newsInfo : responseObj.body.newsList) {
                                if (newsInfo.type.equals(NewsItemsAdapter.NEWS_TYPE_AD + "")) {
                                    Globals.g_advMap.put(newsInfo.id, newsInfo);
                                    int index = 0;
                                    if (!TextUtils.isEmpty(newsInfo.plan)) {
                                        index = new Random().nextInt(newsInfo.plan.split(AdvInfoObject.PLAN_SPLIT_CHAR).length);
                                    }
                                    Globals.g_advIndexMap.put(newsInfo.id, index);
                                    Globals.g_advTimeMap.put(newsInfo.id, 0L);
                                }
                            }
                            boolean isRefresh = TextUtils.isEmpty(startId);
                            refreshNewsList(responseObj.body, isRefresh);
                        } else {
                            ToastUtil.showToast(TopicActivity.this, "获取专题失败");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToast(TopicActivity.this, "获取专题报错");
                    }
                }
                mProgressBar.setVisibility(View.GONE);
                if (mIsShowAnim) {
                    mPTRListView.startAnimation(mAnim);
                    mIsShowAnim = false;
                }
            }

            @Override
            public void onErrorResponse() {
                mPTRListView.onRefreshComplete();
                mIsRefreshing = false;
                ToastUtil.showToast(TopicActivity.this, R.string.request_fail_warning);
                mProgressBar.setVisibility(View.GONE);
                if (mIsShowAnim) {
                    mPTRListView.startAnimation(mAnim);
                    mIsShowAnim = false;
                }
            }
        };
        WebServiceIf.getTopicList(this, body, callbackIf);
    }

    /**
     * 设置数据，刷新新闻列表
     *
     * @param body    新闻列表的数据对象
     * @param refresh true: 刷新列表 false: 加载更多
     */
    private void refreshNewsList(NewsTopicResponseBody body, boolean refresh) {
        if (!TextUtils.isEmpty(body.topicTitle)) {
            mTvTitle.setText(body.topicTitle);
        }
        if (refresh) {
            mFreshTime = System.currentTimeMillis();
            mPTRListView.getLoadingLayoutProxy().setLastUpdatedLabel("更新于:" + DateUtil.getRelativeTimeSpanString(mFreshTime));
            if (!TextUtils.isEmpty(body.imgUrl)) {
                mImage = body.imgUrl;
                GlideApp.with(this)
                        .load(body.imgUrl)
                        .centerCrop()
                        .error(R.drawable.image_bg)
                        .into(mIvBanner);
            }
            if (!TextUtils.isEmpty(body.summary)) {
                mTvBannerTitle.setText(body.summary);
            }
            mList.clear();
            mList.addAll(body.newsList);
        } else {
            mList.addAll(body.newsList);
        }
        mAdapter.notifyDataSetChanged();
        mNewsCount = body.newsListCount;
        body.newsList = mList;
        // 保存到数据库
        mCacheDao.saveNews(mFreshTime, mTopicId, body);
    }

    Handler hdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mPTRListView.getRefreshableView().setEnabled(true);
                    break;
            }
        }
    };

    /**
     * 设置头图高度
     *
     * @param view
     */
    private void setAdvHeight(View view) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int newHeight = screenWidth / 3;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, newHeight);
        view.setLayoutParams(params);
    }
}
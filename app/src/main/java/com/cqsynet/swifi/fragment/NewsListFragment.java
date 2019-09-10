/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻焦点页面。
 *
 *
 * 创建标识：luchaowei 20140922
 */
package com.cqsynet.swifi.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.GalleryActivity;
import com.cqsynet.swifi.activity.TopicActivity;
import com.cqsynet.swifi.adapter.NavItemsAdapter;
import com.cqsynet.swifi.adapter.NewsItemsAdapter;
import com.cqsynet.swifi.db.NewsCacheDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.AdvInfoObject;
import com.cqsynet.swifi.model.NavItemInfo;
import com.cqsynet.swifi.model.NewsCacheObject;
import com.cqsynet.swifi.model.NewsItemInfo;
import com.cqsynet.swifi.model.NewsListRequestBody;
import com.cqsynet.swifi.model.NewsListResponseBody;
import com.cqsynet.swifi.model.NewsListResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.AdvDataHelper;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.NewsAdUtil;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.NoScrollGridView;
import com.cqsynet.swifi.view.XViewPager;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NewsListFragment extends Fragment implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private PullToRefreshListView mPTRListView;
    private XViewPager mTopView;
    private ViewPager mTopViewPager; // 显示头图图片的viewpager
    private TextView mTvBannerTitle; // 新闻头图标题
    private TextView mTvNewsType; // 头图类型
    private RadioGroup mRadioGroup; // 新闻头图的指示小圆点
    private NewsItemsAdapter mNewsItemsAdapter; // 新闻列表adapter
    private TopPagerAdapter mTopPagerAdapter; // 新闻头图adapter
    public String mChannelId = ""; // 当前新闻页面对应的频道id
    private ArrayList<NewsItemInfo> mNewsList = new ArrayList<NewsItemInfo>(); // 新闻列表数据
    private ArrayList<NewsItemInfo> mTopList = new ArrayList<NewsItemInfo>(); // 新闻头图数据
    private ArrayList<NavItemInfo> mNavList = new ArrayList<NavItemInfo>(); //导航
    private int mNewsCount = 0; // 当前新闻频道的新闻总条数
    private long mFreshLocalTime = 0; // 刷新时间
    private NewsCacheDao mCacheDao; // 新闻缓存数据库
    private boolean mNeedRefresh; // 是否需要刷新
    private boolean mIsRefreshing = false; // 是否正在刷新
    private int mAdvCount; // 内容列表里面的广告数量(不包含幻灯)
    private String mTopPullAdvId;
    private ImageView mTopPullAdView;
    private List<AdvInfoObject> mAdvData; // 下拉刷新广告位数据
    private TextView mNewsAddView; // 更新资讯条数提醒view
    private boolean mIsTimeNull = false; // 如果上次更新时间为空，则为true。并解决首次安装app无上次更新时间问题
    private int mViewPagerPosition; //viewpager当前显示的位置
    private String mFreshServerTime; // cms服务器更新时间
    private ImageView mIvFloatingAdv; //浮层广告位
    private NewsItemInfo mFloatingAdv; //浮层广告
    private boolean mIsTopViewEnable = false; //是否有幻灯
    private boolean mIsNavEnable = false; //是否有导航
    private NavItemsAdapter mNavListAdapter; //导航栏适配器
    private NoScrollGridView mNavGrid; //导航
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TextUtils.isEmpty(mChannelId)) {
            initData();
        }
        mAdvData = new AdvDataHelper(mContext, null).getAdvData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = initLayout(inflater, container);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        //解决girdview不能点击的bug
        mNavGrid.setVisibility(View.GONE);
        mNavGrid.setVisibility(View.VISIBLE);

        if ((!TextUtils.isEmpty(mChannelId) && DateUtil.overOneHour(mFreshLocalTime) && mFreshLocalTime != 0) || mNeedRefresh) {
            mPTRListView.setRefreshing(false);
        } else {
            refreshAd();
            startViewPager();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopViewPager();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }


    /**
     * 刷新广告(轮播)
     */
    public void refreshAd() {
        mNewsItemsAdapter.notifyDataSetChanged();
        if (NewsMainFragment.mCurrentFragment == null || this == null || this == NewsMainFragment.mCurrentFragment) {
            showFloatingAdv();
        }
    }

    /**
     * 设置Fragment新闻列表对应的频道ID ，一定要在创建Fragment或id变化时指定此id，否则将显示空白
     *
     * @param id
     */
    public void setChannelId(String id) {
        if (!TextUtils.isEmpty(mChannelId) && !mChannelId.equals(id)) {
            // 如果当前页面的频道id发生改变，置起强制刷新标志
            mNeedRefresh = true;
        }
        mChannelId = id;
    }

    /**
     * 初始化数据
     */
    public void initData() {
        // 从数据库获取普通新闻列表
        mCacheDao = NewsCacheDao.getInstance(mContext);
        NewsCacheObject cacheObject = mCacheDao.getNews(mChannelId, false);
        mNeedRefresh = false;
        if (cacheObject != null) {
            // 如果有数据，使用缓存的数据
            mFreshLocalTime = cacheObject.date;
            mFreshServerTime = cacheObject.serverDate;
            mTopList.clear();
            mNewsList.clear();
            mNavList.clear();
            if (cacheObject.topList != null) {
                mTopList.addAll(cacheObject.topList);
            }
            if (cacheObject.navList != null) {
                mNavList.addAll(cacheObject.navList);
            }
            mNewsList.addAll(cacheObject.newsList);
            mNewsCount = cacheObject.newsListCount;

            // 初始化广告
            for (NewsItemInfo newsInfo : mNewsList) {
                if (newsInfo.type.equals(NewsItemsAdapter.NEWS_TYPE_AD + "")) {
                    if (Globals.g_advMap.get(newsInfo.id) == null || Globals.g_advIndexMap.get(newsInfo.id) == null) {
                        Globals.g_advMap.put(newsInfo.id, newsInfo);
                        int index = 0;
                        if (!TextUtils.isEmpty(newsInfo.plan)) {
                            index = new Random().nextInt(newsInfo.plan.split(AdvInfoObject.PLAN_SPLIT_CHAR).length);
                        }
                        Globals.g_advIndexMap.put(newsInfo.id, index);
                        Globals.g_advTimeMap.put(newsInfo.id, 0L);
                    }
                }
            }
            for (NewsItemInfo newsInfo : mTopList) {
                if (newsInfo.type.equals(NewsItemsAdapter.NEWS_TYPE_AD + "")) {
                    if (Globals.g_advMap.get(newsInfo.id) == null || Globals.g_advIndexMap.get(newsInfo.id) == null) {
                        Globals.g_advMap.put(newsInfo.id, newsInfo);
                        int index = 0;
                        if (!TextUtils.isEmpty(newsInfo.plan)) {
                            index = new Random().nextInt(newsInfo.plan.split(AdvInfoObject.PLAN_SPLIT_CHAR).length);
                        }
                        Globals.g_advIndexMap.put(newsInfo.id, index);
                        Globals.g_advTimeMap.put(newsInfo.id, 0L);
                    }
                }
            }
            mFloatingAdv = cacheObject.floatingAdv;
            if (mFloatingAdv != null) {
                Globals.g_advMap.put(mFloatingAdv.id, mFloatingAdv);
                int index = 0;
                if (!TextUtils.isEmpty(mFloatingAdv.plan)) {
                    index = new Random().nextInt(mFloatingAdv.plan.split(AdvInfoObject.PLAN_SPLIT_CHAR).length);
                }
                Globals.g_advIndexMap.put(mFloatingAdv.id, index);
                Globals.g_advTimeMap.put(mFloatingAdv.id, 0L);
            }
        } else {
            // 无缓存数据，强制刷新
            mNeedRefresh = true;
        }
    }

    /**
     * @return void
     * @Title: initLayout
     * @Description: 初始化listView，Viewpager等。
     */
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);
        mPTRListView = view.findViewById(R.id.listview_fragment_newslist);
        mPTRListView.setPullToRefreshOverScrollEnabled(false);
        //幻灯
        mTopView = new XViewPager(mContext);
        mTopViewPager = mTopView.findViewById(R.id.viewPager_xviewpager);
        mTvBannerTitle = mTopView.findViewById(R.id.tvImageTitle_xviewpager);
        mTvNewsType = mTopView.findViewById(R.id.tvNewsType_xviewpager);
        mRadioGroup = mTopView.findViewById(R.id.rgSelectPoint_xviewpager);
        mNavGrid = (NoScrollGridView) View.inflate(mContext, R.layout.nav_gridview, null);
        mNewsAddView = view.findViewById(R.id.tvNewsReminder_fragment_newslist);
        mPTRListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mHdl.sendEmptyMessageDelayed(5, 300); //延迟300毫秒防止卡顿
            }
        });

        mPTRListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                // 当前显示的广告数量
                mAdvCount = 0;
                for (int i = 0; i < mNewsList.size(); i++) {
                    if (mNewsList.get(i).type.equals(String.valueOf(NewsItemsAdapter.NEWS_TYPE_AD))) {
                        mAdvCount++;
                    }
                }

                // 当前显示的总条数
                int curCount = mNewsItemsAdapter.getCount();
                if (mNewsCount != 0) {
                    if (curCount - mAdvCount < mNewsCount && !mIsRefreshing) {
                        // 如果当前显示的总条数小于当前频道新闻总条数，继续加载
                        NewsItemInfo info = (NewsItemInfo) mNewsItemsAdapter.getItem(curCount - 1);
                        getNewsList(info.id);
                        mIsRefreshing = true;
                    } else if (!mIsRefreshing) {
                        // 已到最底端，停止加载，提示无更多内容
                        ToastUtil.showToast(mContext, R.string.no_more_item);
                    }
                }
            }
        });

        mNavGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NavItemInfo navItemInfo = mNavList.get(i);
                StatisticsDao.saveStatistics(mContext, "nav", navItemInfo.type + "," + navItemInfo.navValue);
                Intent intent = new Intent();
                switch (Integer.parseInt(navItemInfo.type)) {
                    case NewsItemsAdapter.NEWS_TYPE_TOPIC: // 专题
                        intent.setClass(mContext, TopicActivity.class);
                        intent.putExtra("from", "newsList");
                        intent.putExtra("id", navItemInfo.navValue);
                        startActivity(intent);
                        break;
                    case NewsItemsAdapter.NEWS_TYPE_AD: // 广告
                        if (!TextUtils.isEmpty(navItemInfo.navValue)) {
                            Intent advIntent = new Intent();
                            advIntent.putExtra("url", navItemInfo.navValue);
                            advIntent.putExtra("type", "0");
                            advIntent.putExtra("channelId", mChannelId);
                            advIntent.putExtra("from", "newsList");
                            WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                            webDispatcher.dispatch(advIntent, mContext);
                        }
                        break;
                    case NewsItemsAdapter.NEWS_TYPE_GALLERY: // 图集
                        intent.setClass(mContext, GalleryActivity.class);
                        intent.putExtra("id", navItemInfo.navValue);
                        intent.putExtra("channelId", mChannelId);
                        intent.putExtra("from", "newsList");
                        startActivity(intent);
                        break;
                    default: // 普通页面
                        if (!TextUtils.isEmpty(navItemInfo.navValue)) {
                            Intent newsIntent = new Intent();
                            newsIntent.putExtra("url", navItemInfo.navValue);
                            newsIntent.putExtra("type", "0");
                            newsIntent.putExtra("channelId", mChannelId);
                            newsIntent.putExtra("from", "newsList");
                            newsIntent.putExtra("source", "资讯");
                            WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                            webDispatcher.dispatch(newsIntent, mContext);
                        }
                        break;
                }
            }
        });

        //浮层广告位
        mIvFloatingAdv = view.findViewById(R.id.ivFloatingAdv_fragment_news);
        showFloatingAdv();
        //下拉广告位
        mTopPullAdView = view.findViewById(R.id.ivPullAdv_fragment_newslist);
        if (mAdvData != null && mAdvData.size() != 0) {
            for (AdvInfoObject advInfo : mAdvData) {
                if ("ad0019".equals(advInfo.id) && mTopPullAdView != null) {
                    showAdv(advInfo, mTopPullAdView);
                }
            }
        }

        if (mTopList != null && !mTopList.isEmpty()) {
            mPTRListView.getRefreshableView().addHeaderView(mTopView);
            mIsTopViewEnable = true;
        }
        mTopPagerAdapter = new TopPagerAdapter(mTopList);
        mTopViewPager.setAdapter(mTopPagerAdapter);
        mTopViewPager.addOnPageChangeListener(this);
        if (mTopList != null && mTopList.size() != 0) {
            mTopPagerAdapter.initPagerIndicator(mTopList);
        }
//		mTopViewPager.setCurrentItem(mTopList.size() * 10);
        //采用反射的方式设置初始化显示哪一页,用setCurrentItem可能会出现卡死
        try {
            Field mFirstLayout = ViewPager.class.getDeclaredField("mFirstLayout");
            mFirstLayout.setAccessible(true);
            mFirstLayout.set(mTopViewPager, true);
            mTopPagerAdapter.notifyDataSetChanged();
            mTopViewPager.setCurrentItem(mTopList.size() * 10);
            mViewPagerPosition = mTopList.size() * 10;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //导航
        if (mNavList != null && !mNavList.isEmpty()) {
            if (mNavList.size() % 3 == 0) {
                mNavGrid.setNumColumns(3);
            } else if (mNavList.size() % 4 == 0) {
                mNavGrid.setNumColumns(4);
            } else if (mNavList.size() % 5 == 0) {
                mNavGrid.setNumColumns(5);
            } else {
                mNavGrid.setNumColumns(5);
            }
            mPTRListView.getRefreshableView().addHeaderView(mNavGrid);
            mIsNavEnable = true;
        }
        mNavListAdapter = new NavItemsAdapter(mContext, mNavList);
        mNavGrid.setAdapter(mNavListAdapter);

        // 再次进入Fragment时，如果之前的实例还在，用之前的实例。
        if (mNewsItemsAdapter == null) {
            mNewsItemsAdapter = new NewsItemsAdapter(mContext, mNewsList, this);
        }
        mPTRListView.setAdapter(mNewsItemsAdapter);
        mHdl.sendEmptyMessageDelayed(1, 10);

        mPTRListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //防止频繁重复点击
                mPTRListView.getRefreshableView().setEnabled(false);
                mHdl.sendEmptyMessageDelayed(0, 200);

                NewsItemInfo info = (NewsItemInfo) mNewsItemsAdapter.getItem((int) id);
                if (info == null) {
                    return;
                }
                //跳转
                mNewsItemsAdapter.setNewsClickJump(mContext, info, mChannelId);
                //点击的如果是广告,返回列表时不轮播该广告,所以index-1
                if (!TextUtils.isEmpty(info.type)) {
                    if (Integer.parseInt(info.type) == NewsItemsAdapter.NEWS_TYPE_AD) {
                        int index = Globals.g_advIndexMap.get(info.id);
                        int size = info.advId.size();
                        index = (index + size - 1) % size;
                        Globals.g_advIndexMap.put(info.id, index);
                    }
                }
            }
        });

        if (mFreshLocalTime != 0) {
            mPTRListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                    "更新于:" + DateUtil.getRelativeTimeSpanString(mFreshLocalTime));
        }

        return view;
    }

    /**
     * 新闻页面，图片滚动栏Viewpager的adapter。
     */
    public class TopPagerAdapter extends PagerAdapter implements OnClickListener {
        List<NewsItemInfo> mList;

        public TopPagerAdapter(List<NewsItemInfo> list) {
            mList = list;
            mTvBannerTitle.setOnClickListener(this);
        }

        /**
         * 获取adapter数据
         *
         * @return
         */
        public List<NewsItemInfo> getData() {
            return mList;
        }

        /**
         * 初始化头图ViewPager的指示小圆点
         *
         * @param list
         */
        public void initPagerIndicator(List<NewsItemInfo> list) {
            RadioButton rbtn;
            int dotCount = mRadioGroup.getChildCount();
            if (dotCount > 0) {
                mRadioGroup.removeAllViews();
            }
            dotCount = mList.size();
            if (dotCount == 1) {
                setTitleAndLabel(0);
            } else if (dotCount > 1) {
                for (int i = 0; i < dotCount; i++) {
                    rbtn = (RadioButton) LayoutInflater.from(mContext).inflate(R.layout.news_toplist_radio_button,
                            null);
                    rbtn.setClickable(false);
                    rbtn.setId(i);
                    if (i == mTopViewPager.getCurrentItem() % mList.size()) {
                        setTitleAndLabel(i);
                        rbtn.setChecked(true); // 选中第一个button
                    }
                    mRadioGroup.addView(rbtn);
                }
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position = position % mList.size();
            int newsType = NewsItemsAdapter.NEWS_TYPE_NORMAL;
            NewsItemInfo info = mList.get(position);
            String type = info.type;

            if (type != null && !TextUtils.isEmpty(type)) {
                newsType = Integer.valueOf(type);
            }

            ImageView view = new ImageView(mContext);
            view.setScaleType(ScaleType.FIT_XY);
            // 如果数据不合法，不去load 网络图片
            if (info != null && info.img != null && info.img.size() > 0) {
                String imgUrl;
                if (newsType == NewsItemsAdapter.NEWS_TYPE_AD && info.img.size() > 1) { // 如果为广告类型
                    imgUrl = info.img.get(Globals.g_advIndexMap.get(info.id));
                } else {
                    imgUrl = info.img.get(NewsItemsAdapter.FIRST_IMAGE);
                }
                GlideApp.with(mContext)
                        .load(imgUrl)
                        .centerCrop()
                        .error(R.drawable.image_bg)
                        .into(view);
            }
            container.addView(view, 0);
            view.setOnClickListener(this);
            return view;
        }

        @Override
        public void destroyItem(View container, int position, Object arg2) {
            ((ViewPager) container).removeView(container.findViewWithTag(position % mList.size()));
        }

        @Override
        public int getCount() {
            if (mList.size() <= 1) {
                return mList.size();
            } else {
                return Integer.MAX_VALUE;
            }
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
        public void onClick(View v) {
            // 获得当前点击的是第几个item
            int position = mTopViewPager.getCurrentItem();
            int newsType = 0;
            NewsItemInfo info = mList.get(position % mList.size());
            String type = info.type;
            if (type != null && !TextUtils.isEmpty(type)) {
                newsType = Integer.valueOf(type);
            }

            String url = "";
            Intent intent = new Intent();
            switch (newsType) {
                case NewsItemsAdapter.NEWS_TYPE_TOPIC: // 专题
                    intent.setClass(mContext, TopicActivity.class);
                    intent.putExtra("id", info.id);
                    intent.putExtra("from", "newsList");
                    startActivity(intent);
                    break;
                case NewsItemsAdapter.NEWS_TYPE_AD: // 广告
                    try {
                        String advId = info.advId.get(Globals.g_advIndexMap.get(info.id));
                        StatisticsDao.saveStatistics(mContext, "advClick", advId); // 广告点击统计
                    } catch (NullPointerException e) {
                        break;
                    }
                    if (info.img.size() > 1) {
                        // 如果为广告类型，需要根据plan返回跳转url
                        url = info.url.get(Globals.g_advIndexMap.get(info.id));
                    } else if (info.url != null) {
                        url = info.url.get(0);
                    }
                    if (!TextUtils.isEmpty(url)) {
                        Integer index = Globals.g_advIndexMap.get(info.id);
                        if (index != null) {
                            Intent advIntent = new Intent();
                            advIntent.putExtra("url", url);
                            advIntent.putExtra("type", "0");
                            advIntent.putExtra("channelId", mChannelId);
                            advIntent.putExtra("from", "newsList");
                            advIntent.putExtra("source", "广告");
                            WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                            webDispatcher.dispatch(advIntent, mContext);
                        }
                    }
                    break;
                case NewsItemsAdapter.NEWS_TYPE_GALLERY: // 图集
                    intent.setClass(mContext, GalleryActivity.class);
                    intent.putExtra("id", info.id);
                    intent.putExtra("channelId", mChannelId);
                    intent.putExtra("from", "newsList");
                    startActivity(intent);
                    break;
                default: // 其它
                    url = "";
                    if (info.url != null) {
                        // 其他类型 默认使用第一个url
                        url = info.url.get(0);
                    }
                    if (!TextUtils.isEmpty(url)) {
                        Intent newsIntent = new Intent();
                        newsIntent.putExtra("url", url);
                        newsIntent.putExtra("type", "0");
                        newsIntent.putExtra("channelId", mChannelId);
                        newsIntent.putExtra("from", "newsList");
                        newsIntent.putExtra("source", "资讯");
                        WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                        webDispatcher.dispatch(newsIntent, mContext);
                    }
                    break;
            }
        }
    }

    private void setTitleAndLabel(final int position) {
        String suitableString = mTopList.get(position).title;
        if (TextUtils.isEmpty(suitableString)) {
            mTvBannerTitle.setVisibility(View.GONE);
            mTvNewsType.setVisibility(View.GONE);
        } else {
            float width = mTvBannerTitle.getPaint().measureText(suitableString);
            final float charWidth = width / suitableString.length();
            final int tvWidth = AppUtil.getScreenW((Activity)mContext) - AppUtil.dp2px(mContext, 50);
            if (width > tvWidth) {
                suitableString = suitableString.substring(0, (int) (tvWidth / charWidth));
            }
            mTvBannerTitle.setText(suitableString);
            mTvBannerTitle.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(suitableString) || TextUtils.isEmpty(mTopList.get(position).label)) {
                mTvNewsType.setVisibility(View.GONE);
            } else {
                mTvNewsType.setText(mTopList.get(position).label);
                mTvNewsType.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        mViewPagerPosition = position;
        startViewPager();
        try {
            position = position % mTopList.size();
            mRadioGroup.check(position);
            setTitleAndLabel(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final ArrayList<String> arrayList = mTopList.get(position).advId;
        if (arrayList != null && arrayList.size() > 0) {
            StatisticsDao.saveStatistics(mContext, "advView", arrayList.get(0));
        }
    }

    @Override
    public void onClick(View view) {
    }

    /**
     * 获取最新新闻列表
     */
    private void getNewsList(final String startId) {
        final NewsListRequestBody body = new NewsListRequestBody();
        body.id = mChannelId;
        body.start = startId;
        if (TextUtils.isEmpty(startId)) {
            if (!TextUtils.isEmpty(mFreshServerTime)) {
                mIsTimeNull = true;
                body.updateTime = mFreshServerTime;
            } else {
                body.updateTime = ""; // 首次登陆或无上次更新时间时提交空字符串
            }
        } else {
            body.updateTime = "";
        }

        IResponseCallback callbackIf = new IResponseCallback() {

            @Override
            public void onResponse(String response) {
                mPTRListView.onRefreshComplete();
                mNeedRefresh = false;
                mIsRefreshing = false;

                if (response != null && !TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    try {
                        NewsListResponseObject responseObj = gson.fromJson(response, NewsListResponseObject.class);
                        ResponseHeader header = responseObj.header;
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            // 保存列表广告信息
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
                            //保存幻灯广告信息
                            if (responseObj.body.topList != null) {
                                for (NewsItemInfo newsInfo : responseObj.body.topList) {
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
                            }
                            //保存浮层广告信息
                            mFloatingAdv = responseObj.body.floatingAdv;
                            if (responseObj.body.floatingAdv != null) {
                                NewsItemInfo newsInfo = responseObj.body.floatingAdv;
                                Globals.g_advMap.put(newsInfo.id, newsInfo);
                                int index = 0;
                                if (!TextUtils.isEmpty(newsInfo.plan)) {
                                    index = new Random().nextInt(newsInfo.plan.split(AdvInfoObject.PLAN_SPLIT_CHAR).length);
                                }
                                Globals.g_advIndexMap.put(newsInfo.id, index);
                                Globals.g_advTimeMap.put(newsInfo.id, 0L);
                            }
                            boolean isRefresh = TextUtils.isEmpty(startId);
                            refreshNewsList(responseObj.body, isRefresh);
                        } else {
                            ToastUtil.showToast(mContext, "获取新闻列表失败");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToast(mContext, "获取新闻列表报错");
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                mPTRListView.onRefreshComplete();
                mNeedRefresh = false;
                mIsRefreshing = false;
                ToastUtil.showToast(mContext, R.string.request_fail_warning);
            }
        };
        WebServiceIf.getNewsList(this.mContext, body, callbackIf);
    }

    /**
     * 设置数据，刷新新闻列表
     *
     * @param body    新闻列表的数据对象
     * @param refresh true: 刷新列表 false: 加载更多
     */
    private void refreshNewsList(NewsListResponseBody body, boolean refresh) {
        if (refresh) {
            mFreshLocalTime = System.currentTimeMillis();
            mPTRListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                    "更新于:" + DateUtil.getRelativeTimeSpanString(mFreshLocalTime));
            mTopList.clear();
            mNavList.clear();
            mTopViewPager.removeAllViews();
            if (body.topList == null || body.topList.isEmpty()) {
                // 如果头图无数据，隐藏头图框体
                mPTRListView.getRefreshableView().removeHeaderView(mTopView);
                mIsTopViewEnable = false;
            } else {
                // 如果头图有数据，显示头图框体
                if (!mIsTopViewEnable) {
                    mPTRListView.getRefreshableView().addHeaderView(mTopView);
                    mIsTopViewEnable = true;
                }
                mTopList.addAll(body.topList);
                mTopPagerAdapter.initPagerIndicator(mTopList);
                mTopPagerAdapter.notifyDataSetChanged();
//				mTopViewPager.setCurrentItem(mTopList.size() * 10); //这种方式可能会出现卡死
                //采用反射来设置显示的页,防止卡死
                try {
                    Field mFirstLayout = ViewPager.class.getDeclaredField("mFirstLayout");
                    mFirstLayout.setAccessible(true);
                    mFirstLayout.set(mTopViewPager, true);
                    mTopPagerAdapter.notifyDataSetChanged();
                    mTopViewPager.setCurrentItem(mTopList.size() * 10);
                    mViewPagerPosition = mTopList.size() * 10;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //导航
            if (body.navList == null || body.navList.isEmpty()) {
                mPTRListView.getRefreshableView().removeHeaderView(mNavGrid);
                mIsNavEnable = false;
            } else {
                if (!mIsNavEnable) {
                    mPTRListView.getRefreshableView().addHeaderView(mNavGrid);
                    mIsNavEnable = true;
                }
                mNavList.addAll(body.navList);
                if (mNavList.size() % 3 == 0) {
                    mNavGrid.setNumColumns(3);
                } else if (mNavList.size() % 4 == 0) {
                    mNavGrid.setNumColumns(4);
                } else if (mNavList.size() % 5 == 0) {
                    mNavGrid.setNumColumns(5);
                } else {
                    mNavGrid.setNumColumns(5);
                }
                mNavListAdapter.notifyDataSetChanged();
            }

            mNewsList.clear();
            mNewsList.addAll(body.newsList);
            String tempNum = String.valueOf(body.updateNum);
            if (mIsTimeNull) {
                if (!TextUtils.isEmpty(tempNum) && body.updateNum > 0) { // 如果更新条数大于0则提示
                    mNewsAddView.setText("为您更新了" + body.updateNum + "条资讯");
                    mHdl.sendEmptyMessage(3);
                }
            }
            mFreshServerTime = body.updateTime;
            showFloatingAdv();
            mHdl.sendEmptyMessage(1);
        } else if (body.newsList != null && !body.newsList.isEmpty()) {
            mNewsList.addAll(body.newsList);
        }
        mNewsItemsAdapter.notifyDataSetChanged();
        mNewsCount = body.newsListCount;
        body.newsList = mNewsList;
        body.topList = mTopList;
        body.navList = mNavList;
        body.updateTime = mFreshServerTime;
        // 保存到数据库
        mCacheDao.saveNews(mFreshLocalTime, mChannelId, body);
    }

    public void moveToTop() {
//        int headerViewCount = mPTRListView.getRefreshableView().getHeaderViewsCount();
//        mPTRListView.getRefreshableView().setSelection(headerViewCount - 2);
        mPTRListView.getRefreshableView().setSelection(1);
    }

    Handler mHdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mPTRListView.getRefreshableView().setEnabled(true);
                    break;
                case 1:
                    moveToTop();
                    break;
                case 2:
                    //滑动幻灯
                    mTopViewPager.setCurrentItem(mViewPagerPosition + 1, true);
                    break;
                case 3:
                    Animation mHiddenAction = new AlphaAnimation(1.0f, 0.0f);
                    mHiddenAction.setDuration(3000);
                    mHiddenAction.setFillAfter(true);
                    mNewsAddView.setVisibility(View.VISIBLE);
                    mNewsAddView.startAnimation(mHiddenAction);
                    break;
                case 4:
                    if (mAdvData != null && mAdvData.size() != 0) {
                        for (AdvInfoObject advInfo : mAdvData) {
                            if ("ad0019".equals(advInfo.id) && mTopPullAdView != null) {
                                showAdv(advInfo, mTopPullAdView);
                            }
                        }
                    }
                    break;
                case 5:
                    //刷新资讯
                    getNewsList("");
                    //刷新广告
                    new AdvDataHelper(mContext, null).loadAdvData();
                    mHdl.sendEmptyMessageDelayed(4, 1000);
                    break;
            }
        }
    };

    /**
     * 显示广告
     *
     * @param advInfo
     */
    private void showAdv(AdvInfoObject advInfo, ImageView imgView) {
        int index = advInfo.getSortIndex(advInfo.getCurrentIndex());
        mTopPullAdvId = advInfo.advId[index];
        String imgUrl = advInfo.adUrl[index];
        if (mContext != null) {
            GlideApp.with(mContext)
                    .load(imgUrl)
                    .centerInside()
                    .into(imgView);
            if (!TextUtils.isEmpty(mTopPullAdvId)) {
                StatisticsDao.saveStatistics(mContext, "advView", mTopPullAdvId); // 下拉顶部广告显示统计
            }
        }
    }

    /**
     * 开始自动滚动viewpager
     */
    public void startViewPager() {
        mHdl.removeMessages(2);
        mHdl.sendEmptyMessageDelayed(2, 4500); //4.5秒滚动一次
    }

    /**
     * 停止自动滚动viewpager
     */
    public void stopViewPager() {
        mHdl.removeMessages(2);
    }

    /**
     * 显示浮层广告
     *
     */
    public void showFloatingAdv() {
        if (mContext != null) {
            if (mFloatingAdv != null && mFloatingAdv.img != null && mFloatingAdv.img.size() > 0) {
                NewsAdUtil adUtil = new NewsAdUtil();
                final int index = adUtil.getCurrentIndex(mFloatingAdv.id);
                GlideApp.with(mContext)
                        .load(mFloatingAdv.img.get(index))
                        .centerInside()
                        .into(mIvFloatingAdv);
                mIvFloatingAdv.setVisibility(View.VISIBLE);
                mIvFloatingAdv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mFloatingAdv.url != null && mFloatingAdv.url.size() > 0 && !TextUtils.isEmpty(mFloatingAdv.url.get(index))) {
                            StatisticsDao.saveStatistics(mContext, "advClick", mFloatingAdv.advId.get(index)); // 启动图广告点击统计
                            Intent webIntent = new Intent();
                            webIntent.putExtra("url", mFloatingAdv.url.get(index));
                            webIntent.putExtra("type", "0");
                            webIntent.putExtra("source", "广告");
                            webIntent.putExtra("from", "newsList");
                            WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                            webDispatcher.dispatch(webIntent, mContext);
                        }
                    }
                });
            } else {
                mIvFloatingAdv.setVisibility(View.GONE);
            }
        } else {
            mIvFloatingAdv.setVisibility(View.GONE);
        }
    }

}

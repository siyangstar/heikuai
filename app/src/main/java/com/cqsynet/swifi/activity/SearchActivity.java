/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：搜索界面Activity
 *
 *
 * 创建标识：xy 20160331
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.NewsItemsAdapter;
import com.cqsynet.swifi.adapter.SearchHistoryAdapter;
import com.cqsynet.swifi.db.SearchHistoryDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.AdvInfoObject;
import com.cqsynet.swifi.model.NewsItemInfo;
import com.cqsynet.swifi.model.NewsSearchOrHotResponseObject;
import com.cqsynet.swifi.model.NewsSearchOrHotResponseObject.NewsSearchResponseBody;
import com.cqsynet.swifi.model.NewsSearchRequestBody;
import com.cqsynet.swifi.model.RequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.SearchHistoryInfo;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.DeleteDialog;
import com.cqsynet.swifi.view.LoadingDialog;
import com.cqsynet.swifi.view.SearchView;
import com.cqsynet.swifi.view.SearchView.SearchViewListener;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Random;

public class SearchActivity extends Activity implements SearchViewListener, OnClickListener, SearchHistoryAdapter.AdapterListener {

    private Context mContext;
    private SearchView mSearchView;

    private TextView mBack;
    private TextView tv_search_about;
    private TextView tv_del; // 清除历史记录

    private DeleteDialog mDeleteDialog;
    private LinearLayout ll_history_region; // 显示历史搜索的整个view,包括清除历史记录的TextView
    private RelativeLayout rl_search_before;
    private RelativeLayout rl_search_result;

    private SearchHistoryAdapter mSearchHistoryAdapter;
    private HotNewsAdapter mHotNewsAdapter;
    private NewsItemsAdapter mNewsItemsAdapter; // 搜索列表adapter

    private ArrayList<SearchHistoryInfo> historyList = new ArrayList<SearchHistoryInfo>();
    private ArrayList<NewsItemInfo> mNewsSearchList = new ArrayList<NewsItemInfo>(); // 搜索列表数据
    private ArrayList<NewsItemInfo> hotList;

    private String userAccount;

    private String searchText; // 搜索关键字

    private ListView hotArticleListView; // 近期热门
    private ListView mHistoryListView; // 搜索历史
    private PullToRefreshListView mPTRListView;

    private int mNewsCount = 0; // 当前搜索总条数
    private long mFreshTime = 0; // 刷新时间
    private boolean mIsRefreshing = false; // 是否正在刷新


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search);
        mContext = SearchActivity.this;
        userAccount = SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.PHONE_NUM);
        getSearchHistoryAll();
        initLayout();
        getHotArticle();
    }

    /**
     * 初始化布局和设置监听
     */
    private void initLayout() {
        mSearchView = findViewById(R.id.search_view);
        mSearchView.clearFocus();
        mPTRListView = findViewById(R.id.listview_search_list);
        mPTRListView.setPullToRefreshOverScrollEnabled(false);
        mBack = mSearchView.findViewById(R.id.search_btn_back);
        tv_del = mSearchView.findViewById(R.id.tv_del_search_all);
        mHistoryListView = mSearchView.findViewById(R.id.lv_search_tips);
        ll_history_region = mSearchView.findViewById(R.id.region_search_history);
        hotArticleListView = findViewById(R.id.hot_article);
        tv_search_about = findViewById(R.id.search_about);
        rl_search_before = findViewById(R.id.rl_search_before);
        rl_search_result = findViewById(R.id.rl_search_result);
        mSearchView.setSearchViewListener(this);
        mBack.setOnClickListener(this);
        tv_del.setOnClickListener(this);

        mHistoryListView.setAdapter(mSearchHistoryAdapter);
        if (!historyList.isEmpty()) {
            tv_search_about.setVisibility(View.GONE);
            ll_history_region.setVisibility(View.VISIBLE);
        }
        mNewsItemsAdapter = new NewsItemsAdapter(mContext, mNewsSearchList);
        mPTRListView.setAdapter(mNewsItemsAdapter);

        hotArticleListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                NewsItemInfo info = hotList.get(position);
                if (info == null) {
                    return;
                }

                // 点击后保存状态,下次进来后标题置灰
                SharedPreferencesInfo.setTagBoolean(mContext, SharedPreferencesInfo.READED + info.id, true);

                int newsType = 0;
                String type = info.type;
                if (type != null && !TextUtils.isEmpty(type)) {
                    newsType = Integer.valueOf(type);
                }

                String url = "";
                Intent intent = new Intent();
                switch (newsType) {
                    case NewsItemsAdapter.NEWS_TYPE_TOPIC: // 专题
                        intent.setClass(mContext, TopicActivity.class);
                        intent.putExtra("from", "newsList");
                        intent.putExtra("id", info.id);
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
                                advIntent.putExtra("mainType", "0");
                                advIntent.putExtra("subType", "0");
                                advIntent.putExtra("from", "newsList");
                                WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                                webDispatcher.dispatch(advIntent, mContext);
                            }
                        }
                        break;
                    case NewsItemsAdapter.NEWS_TYPE_GALLERY: // 图集
                        intent.setClass(mContext, GalleryActivity.class);
                        intent.putExtra("id", info.id);
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
                            Intent webIntent = new Intent();
                            webIntent.putExtra("url", url);
                            webIntent.putExtra("mainType", "0");
                            webIntent.putExtra("subType", "0");
                            webIntent.putExtra("from", "newsList");
                            WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                            webDispatcher.dispatch(webIntent, mContext);
                        }
                        break;
                }
            }
        });
        mPTRListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                getNewsSearchList("", searchText);
            }
        });

        mPTRListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                // 当前显示的总条数
                int curCount = mNewsItemsAdapter.getCount();
                if (mNewsCount != 0) {
                    if (curCount < mNewsCount && !mIsRefreshing) {
                        // 如果当前显示的总条数小于当前频道新闻总条数，继续加载
                        NewsItemInfo info = (NewsItemInfo) mNewsItemsAdapter.getItem(curCount - 1);
                        getNewsSearchList(info.id, searchText);
                        mIsRefreshing = true;
                    } else if (!mIsRefreshing) {
                        // 已到最底端，停止加载，提示无更多内容
                        ToastUtil.showToast(mContext, R.string.no_more_item);
                    }
                }
            }
        });

        mPTRListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsItemInfo info = (NewsItemInfo) mNewsItemsAdapter.getItem((int) id);
                mNewsItemsAdapter.setNewsClickJump(mContext, info, null);
            }
        });
        setRefreshLable();
    }

    /**
     * 获取4条最近搜索历史
     */
    private void getSearchHistoryAll() {
        if (!historyList.isEmpty()) {
            historyList.clear();
        }

        if (!TextUtils.isEmpty(userAccount)) {
            ArrayList<SearchHistoryInfo> mHistoryList = SearchHistoryDao.getInstance(mContext).getSearchHistory(userAccount);
            historyList.addAll(mHistoryList);
            mSearchHistoryAdapter = new SearchHistoryAdapter(mContext, historyList, this);
            if (!historyList.isEmpty() && mSearchView != null) {
                mSearchView.findViewById(R.id.region_search_history).setVisibility(View.VISIBLE);
            }
            if (mHistoryListView != null) {
                mHistoryListView.setAdapter(mSearchHistoryAdapter);
            }
        }
    }

    @Override
    public void onSearch(String text) {
        if (!TextUtils.isEmpty(text)) {
            searchText = text;
            if (mNewsItemsAdapter != null) {
                mNewsItemsAdapter.setText(searchText);
            }
            getNewsSearchList("", searchText);
        }
    }

    @Override
    public void onSearchClick() {
        getSearchHistoryAll();
        if (!historyList.isEmpty()) {
            ll_history_region.setVisibility(View.VISIBLE);
        } else {
            ll_history_region.setVisibility(View.GONE);
        }
        if (rl_search_result != null && rl_search_result.getVisibility() == View.VISIBLE) {
            rl_search_result.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 获取近期热门文章
     */
    private void getHotArticle() {
        final RequestBody body = new RequestBody();
        IResponseCallback callbackIf = new IResponseCallback() {

            @Override
            public void onResponse(String response) {
                if (response != null && !TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    try {
                        NewsSearchOrHotResponseObject responseObj = gson.fromJson(response, NewsSearchOrHotResponseObject.class);
                        ResponseHeader header = responseObj.header;
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            hotList = responseObj.body.newsList;
                            if (hotList != null && hotArticleListView != null) {
                                mHotNewsAdapter = new HotNewsAdapter(mContext, hotList);
                                hotArticleListView.setAdapter(mHotNewsAdapter);
                            }
                        } else {
                            ToastUtil.showToast(mContext, "获取热门文章失败");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToast(mContext, "获取热门文章失败");
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(mContext, R.string.request_fail_warning);
            }
        };
        WebServiceIf.getNewsHotList(mContext, body, callbackIf);
    }

    /**
     * 获取搜索结果列表
     *
     * @throws InterruptedException
     */
    private void getNewsSearchList(final String startId, final String text) {
        final Dialog dialog = LoadingDialog.createLoadingDialog(this, "请稍候...");
        if (TextUtils.isEmpty(startId)) { // startId为空，代表不是分页请求
            dialog.show();
        }
        final NewsSearchRequestBody body = new NewsSearchRequestBody();
        body.key = text;
        body.start = startId;

        IResponseCallback callbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                mIsRefreshing = false;
                mPTRListView.onRefreshComplete();
                if (response != null && !TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    try {
                        NewsSearchOrHotResponseObject responseObj = gson.fromJson(response, NewsSearchOrHotResponseObject.class);
                        ResponseHeader header = responseObj.header;
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            // 保存广告信息
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
                            if (rl_search_result.getVisibility() == View.INVISIBLE) {
                                rl_search_result.setVisibility(View.VISIBLE);
                                rl_search_before.setVisibility(View.INVISIBLE);
                            }
                            if (responseObj.body.newsList.isEmpty()) {
                                TextView mTextView = findViewById(R.id.search_no_result);
                                mPTRListView.setEmptyView(mTextView);
                            }
                            boolean isRefresh = TextUtils.isEmpty(startId);
                            refreshNewsList(responseObj.body, isRefresh);
                        } else {
                            ToastUtil.showToast(SearchActivity.this, "获取搜索列表失败");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToast(SearchActivity.this, "获取搜索列表失败");
                    }
                }
                dialog.dismiss();
            }

            @Override
            public void onErrorResponse() {
                dialog.dismiss();
                mIsRefreshing = false;
                mPTRListView.onRefreshComplete();
                ToastUtil.showToast(SearchActivity.this, R.string.request_fail_warning);
            }
        };
        WebServiceIf.getNewsSearchList(SearchActivity.this, body, callbackIf);
    }

    /**
     * 设置数据，刷新搜索列表
     *
     * @param body    搜索列表的数据对象
     * @param refresh true: 刷新列表 false: 加载更多
     */
    private void refreshNewsList(NewsSearchResponseBody body, boolean refresh) {
        if (refresh) {
            setRefreshLable();
            mNewsSearchList.clear();
            mNewsSearchList.addAll(body.newsList);
        } else if (body.newsList != null && !body.newsList.isEmpty()) {
            mNewsSearchList.addAll(body.newsList);
        }
        mNewsCount = body.newsListCount;
        if (mNewsItemsAdapter != null) {
            mNewsItemsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置刷新时间显示
     */
    private void setRefreshLable() {
        mFreshTime = System.currentTimeMillis();
        mPTRListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                "更新于:" + DateUtil.getRelativeTimeSpanString(mFreshTime));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPTRListView != null) {
            mPTRListView.getRefreshableView().setEnabled(true);
        }
        mNewsItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_btn_back:
                finish();
                break;

            case R.id.tv_del_search_all:
                String title = "确定清空搜索历史吗？";
                mDeleteDialog = new DeleteDialog(mContext, R.style.round_corner_dialog, title, new DeleteDialog.MyDialogListener() {

                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.tv_confirm_collect: // 确定
                                mDeleteDialog.dismiss();
                                if (!TextUtils.isEmpty(userAccount)) {
                                    historyList.clear();
                                    mSearchHistoryAdapter.notifyDataSetChanged();
                                    SearchHistoryDao.getInstance(mContext).deleteHistoryAll(userAccount);
                                    ll_history_region.setVisibility(View.GONE);
                                    tv_search_about.setVisibility(View.VISIBLE);
                                }
                                break;

                            case R.id.tv_cancel_collect: // 取消
                                mDeleteDialog.dismiss();
                                break;

                            default:
                                break;
                        }
                    }
                });
                mDeleteDialog.show();
                break;

            default:
                break;
        }
    }

    /**
     * 热门新闻adapter
     */
    class HotNewsAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<NewsItemInfo> mList;

        public HotNewsAdapter(Context context, ArrayList<NewsItemInfo> list) {
            mContext = context;
            mList = list;
        }

        @Override
        public int getCount() {
            if (mList == null) {
                return 0;
            }
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            if (mList.get(position) == null) {
                return new NewsItemInfo();
            } else {
                return mList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            HotNewsViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new HotNewsViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_hot_news, parent, false);
                viewHolder.tv_hot = convertView.findViewById(R.id.hot_news_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (HotNewsViewHolder) convertView.getTag();
            }
            viewHolder.tv_hot.setText(mList.get(position).title);
            return convertView;
        }

        class HotNewsViewHolder {
            TextView tv_hot;
        }
    }

    @Override
    public void setVisibility() {
        mSearchView.findViewById(R.id.region_search_history).setVisibility(View.GONE);
        tv_search_about.setVisibility(View.VISIBLE);
    }

    /**
     * 点击空白位置 隐藏软键盘
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.getCurrentFocus() != null) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

}
package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.CollectAdapter;
import com.cqsynet.swifi.adapter.SearchHistoryAdapter;
import com.cqsynet.swifi.db.CollectCacheDao;
import com.cqsynet.swifi.db.SearchHistoryDao;
import com.cqsynet.swifi.model.CollectInfo;
import com.cqsynet.swifi.model.SearchHistoryInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.DeleteDialog;
import com.cqsynet.swifi.view.SearchView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/6/21
 */
public class CollectSearchActivity extends HkActivity implements
        SearchView.SearchViewListener, View.OnClickListener,
        AdapterView.OnItemClickListener, SearchHistoryAdapter.AdapterListener {

    private SearchView mSearchView;
    private LinearLayout mLlHistoryRegion;
    private ListView mLvSearchResult;
    private ListView mLvSearchHistory;
    private TextView mTvBack;
    private TextView mTvDeleteAllHistory;
    private TextView mTvSearchNoResult;
    private DeleteDialog mDeleteDialog;

    private List<CollectInfo> mCollects = new ArrayList<>();
    private ArrayList<SearchHistoryInfo> mHistoryList = new ArrayList<>();
    private CollectAdapter mAdapter;
    private SearchHistoryAdapter mSearchHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_search);

        mSearchView = findViewById(R.id.search_view);
        mSearchView.clearFocus();
        mSearchView.setSearchViewListener(this);
        mLlHistoryRegion = mSearchView.findViewById(R.id.region_search_history);
        mLvSearchHistory = mSearchView.findViewById(R.id.lv_search_tips);

        mTvBack = mSearchView.findViewById(R.id.search_btn_back);
        mTvBack.setOnClickListener(this);
        mTvDeleteAllHistory = mSearchView.findViewById(R.id.tv_del_search_all);
        mTvDeleteAllHistory.setOnClickListener(this);

        mAdapter = new CollectAdapter(this, mCollects);
        mLvSearchResult = findViewById(R.id.lv_search_result);
        mLvSearchResult.setAdapter(mAdapter);
        mLvSearchResult.setOnItemClickListener(this);
        mTvSearchNoResult = findViewById(R.id.tv_search_no_result);

        getSearchHistoryAll();
    }

    @Override
    public void onSearch(String text) {
        if (!TextUtils.isEmpty(text)) {
            getCollectsOfSearch(text);
        }
    }

    private void getCollectsOfSearch(String text) {
        mCollects.clear();
        mCollects.addAll(CollectCacheDao.queryByTitle(this, text));
        mAdapter.notifyDataSetChanged();
        if (mCollects.size() > 0 ) {
            mLvSearchResult.setVisibility(View.VISIBLE);
            mTvSearchNoResult.setVisibility(View.GONE);
        } else {
            mLvSearchResult.setVisibility(View.GONE);
            mTvSearchNoResult.setVisibility(View.VISIBLE);
        }
    }

    private void getSearchHistoryAll () {
        if (!mHistoryList.isEmpty()) {
            mHistoryList.clear();
        }

        String userAccount = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.PHONE_NUM);
        if (!TextUtils.isEmpty(userAccount)) {
            ArrayList<SearchHistoryInfo> historyList = SearchHistoryDao.getInstance(this).getSearchHistory(userAccount);
            mHistoryList.addAll(historyList);
            mSearchHistoryAdapter = new SearchHistoryAdapter(this, mHistoryList, this);
            if (!mHistoryList.isEmpty() && mSearchView != null) {
                mLlHistoryRegion.setVisibility(View.VISIBLE);
            }
            if (mLvSearchHistory != null) {
                mLvSearchHistory.setAdapter(mSearchHistoryAdapter);
            }
        }
    }

    @Override
    public void onSearchClick() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_btn_back:
                finish();
                break;
            case R.id.tv_del_search_all:
                String title = "确定清空搜索历史吗？";
                mDeleteDialog = new DeleteDialog(CollectSearchActivity.this, R.style.round_corner_dialog,
                        title, new DeleteDialog.MyDialogListener() {

                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.tv_confirm_collect:
                                mDeleteDialog.dismiss();
                                String userAccount = SharedPreferencesInfo.getTagString(CollectSearchActivity.this,
                                        SharedPreferencesInfo.PHONE_NUM);
                                if (!TextUtils.isEmpty(userAccount)) {
                                    mHistoryList.clear();
                                    mSearchHistoryAdapter.notifyDataSetChanged();
                                    SearchHistoryDao.getInstance(CollectSearchActivity.this).deleteHistoryAll(userAccount);
                                    mLlHistoryRegion.setVisibility(View.GONE);
                                }
                                break;
                            case R.id.tv_cancel_collect:
                                mDeleteDialog.dismiss();
                                break;
                            default:
                                break;
                        }
                    }
                });
                mDeleteDialog.show();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CollectInfo collect = mCollects.get(position);
        int type = Integer.valueOf(collect.type);
        switch (type) {
            case CollectAdapter.TYPE_COLLECT_NEWS:
                Intent newsIntent = new Intent();
                newsIntent.putExtra("url", collect.url);
                newsIntent.putExtra("image", collect.image);
                newsIntent.putExtra("type", collect.type);
                newsIntent.putExtra("from", "collect");
                newsIntent.putExtra("source", "资讯");
                WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                webDispatcher.dispatch(newsIntent, this);
                break;
            case CollectAdapter.TYPE_COLLECT_GALLERY:
                Intent galleryIntent = new Intent();
                galleryIntent.putExtra("id", collect.id);
                galleryIntent.putExtra("title", collect.title);	
                galleryIntent.putExtra("from", "collect");
                galleryIntent.setClass(this, GalleryActivity.class);
                startActivity(galleryIntent);
                break;
            case CollectAdapter.TYPE_COLLECT_TOPIC:
                Intent topicIntent = new Intent();
                topicIntent.putExtra("id", collect.id);
                topicIntent.putExtra("from", "collect");
                topicIntent.setClass(this, TopicActivity.class);
                startActivity(topicIntent);
                break;
        }
    }

    @Override
    public void setVisibility() {
        mSearchView.findViewById(R.id.region_search_history).setVisibility(View.GONE);
    }
}

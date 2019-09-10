/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：分类新闻页面v2.0
 *
 *
 * 创建标识：zhaosy 20150501
 */
package com.cqsynet.swifi.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.CustomDialog;
import com.cqsynet.swifi.activity.OperateGuideActivity;
import com.cqsynet.swifi.activity.SearchActivity;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.AdvInfoObject;
import com.cqsynet.swifi.model.ChannelInfo;
import com.cqsynet.swifi.model.ChannelListResponseBody;
import com.cqsynet.swifi.model.ChannelListResponseObject;
import com.cqsynet.swifi.model.ChannnelListRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.AdvDataHelper;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.LogUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.SlidingPagerTabStrip;
import com.cqsynet.swifi.view.SlidingPagerTabStrip.SPTSOnPageChangedListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewsMainFragment extends Fragment implements OnClickListener {
    private Context mContext;
    private ViewPager mViewPager;
    private NewsFragmentAdapter mAdapter;
    private ArrayList<ChannelInfo> mChannels = new ArrayList<ChannelInfo>();
    private long mRefreshTime;
    private SlidingPagerTabStrip mSlidingTabs;
    private SPTSOnPageChangedListener mSPTSOnPageChangedListener; //viewPager滑动监听
    private String mCurrentChannelId;
    public static NewsListFragment mCurrentFragment;
    private TextView mTvCity;
    private String mCity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 从ShareInfo里面拿到之前保存的Json格式的频道列表字符串
        mChannels = SharedPreferencesInfo.getNewsChannel(getActivity());
        if(mChannels == null) {
            mChannels = new ArrayList<>();
        }

        // 从服务器获取最新频道列表
        getNewsChannel(getActivity());

        //初始化广告
        Globals.g_advMap = new HashMap<>();
        Globals.g_advIndexMap = new HashMap<>();
        Globals.g_advTimeMap = new HashMap<>();

        String cityCode = SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.CITY_CODE);
        setCityNameViaCode(cityCode);

        AdvDataHelper advDataHelper = new AdvDataHelper(mContext, null);
        List<AdvInfoObject> data = advDataHelper.getAdvData();
        if (data != null && !data.isEmpty()) {
            for (AdvInfoObject advInfo : data) {
                if ("ad11059".equals(advInfo.id)) {
                    showAdvDialog(advInfo);
                }
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View newsView = inflater.inflate(R.layout.fragment_news_main, container, false);
        mViewPager = newsView.findViewById(R.id.pager);
        newsView.findViewById(R.id.ibtnSearch_news).setOnClickListener(this);
        mTvCity = newsView.findViewById(R.id.tvCity_fragment_news_main);
        mTvCity.setOnClickListener(this);
        mTvCity.setText(mCity);
        mAdapter = new NewsFragmentAdapter(getChildFragmentManager(), mChannels);
        mViewPager.setAdapter(mAdapter);
        mSlidingTabs = newsView.findViewById(R.id.tabs);
        mSlidingTabs.setUnderlineHeight(0);
        mSlidingTabs.setDividerColorResource(R.color.transparent);
        mSlidingTabs.setTabPaddingLeftRight(AppUtil.dp2px(mContext, 12));
        mSlidingTabs.setIndicatorColorResource(R.color.green);
        mSlidingTabs.setIndicatorHeight(AppUtil.dp2px(mContext, 4));
        mSlidingTabs.setTextColorResource(R.color.text1);
        mSlidingTabs.setSelectedTextColorResource(R.color.green);
        mSlidingTabs.setTextSize(AppUtil.dp2px(mContext, 16));
        mSlidingTabs.setSelectedTextSize(AppUtil.dp2px(mContext, 16));
        mSlidingTabs.setIndicatorRadius(AppUtil.dp2px(mContext, 4));
        mSlidingTabs.setIndicatorWidth(AppUtil.dp2px(mContext, 14));
        mSPTSOnPageChangedListener = new SPTSOnPageChangedListener() {
            @Override
            public void onPageScrollStateChanged(int position) {
            }

            @Override
            public void onPageScrolled(int position, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentChannelId = mChannels.get(position).id;
                //切换栏目时统计栏目阅读量
                StatisticsDao.saveStatistics(getActivity(), "channelView", mCurrentChannelId);
                mCurrentFragment = mAdapter.getCurItem(position);
                // 选中新闻频道页面时，切换广告
                if (System.currentTimeMillis() - mRefreshTime > AppConstants.AD_REFRESH_INTEVAL && mCurrentFragment != null) {
                    mCurrentFragment.refreshAd();
                }
                mRefreshTime = System.currentTimeMillis();
            }

            @Override
            public void onTabClick(int position) {
                if (mViewPager.getCurrentItem() == position) {
                    NewsListFragment fragment = mAdapter.getCurItem(position);
                    fragment.moveToTop();
                }
            }
        };

        //设置tab
        mSlidingTabs.setViewPager(mViewPager, mSPTSOnPageChangedListener);

        // 第一次进入，显示半透明操作引导图层。
        if (!SharedPreferencesInfo.getTagBoolean(getActivity(), SharedPreferencesInfo.NEWS_GUIDE, false)) {
            Intent intent = new Intent(getActivity(), OperateGuideActivity.class);
            intent.putExtra("type", OperateGuideActivity.INDEX_NEWS);
            startActivity(intent);
        }

        return newsView;
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        super.onAttach(activity);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (mChannels.size() == 0) {
            return;
        }
        int position = mViewPager.getCurrentItem();
        NewsListFragment fragment = mAdapter.getCurItem(position);
        // 当页面显示时，切换广告
        if (!hidden && System.currentTimeMillis() - mRefreshTime > AppConstants.AD_REFRESH_INTEVAL && fragment != null) {
            fragment.refreshAd();
            //切换底部tab时统计栏目浏览数
            StatisticsDao.saveStatistics(getActivity(), "channelView", mCurrentChannelId);
        }
        mRefreshTime = System.currentTimeMillis();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.ibtnSearch_news:
                //点击搜索
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                break;
            case R.id.tvCity_fragment_news_main:
                //点击切换城市
                final CustomDialog dialog = new CustomDialog(mContext, R.style.round_corner_dialog, R.layout.dialog_select_city);
                View dialogView = dialog.getCustomView();
                TextView tvCity = dialogView.findViewById(R.id.tvCity_dialog_select_city);
                LinearLayout llChongqing = dialogView.findViewById(R.id.llChongqing_dialog_select_city);
                LinearLayout llJinan = dialogView.findViewById(R.id.llJinan_dialog_select_city);
                ImageView ivClose = dialogView.findViewById(R.id.ivClose_dialog_select_city);

                if (!TextUtils.isEmpty(SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.REAL_CITY))) {
                    tvCity.setText(SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.REAL_CITY));
                } else {
                    tvCity.setText("重庆");
                }
                ivClose.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                llChongqing.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.CITY_CODE).equals("132")) {
                            resetChannelData("132");
                        }
                        dialog.dismiss();
                    }
                });
                llJinan.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.CITY_CODE).equals("288")) {
                            resetChannelData("288");
                        }
                        dialog.dismiss();
                    }
                });

                dialog.show();
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.width = (AppUtil.getScreenW(getActivity())) - 80; // 设置宽度
                dialog.getWindow().setAttributes(lp);
                break;
            default:
                break;
        }
    }


    /**
     * 获取频道列表
     */
    private void getNewsChannel(final Context ctx) {
        ChannnelListRequestBody requestBody = new ChannnelListRequestBody();
        IResponseCallback callbackIf = new IResponseCallback() {

            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                try {
                    ChannelListResponseObject responseObject = gson.fromJson(response, ChannelListResponseObject.class);
                    ResponseHeader header = responseObject.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        ChannelListResponseBody body = responseObject.body;
                        if (body == null || body.columnList == null) {
                            ToastUtil.showToast(ctx, "当前无频道");
                            return;
                        }
                        //有变化时刷新界面
                        if(!compareChannel(body.columnList, mChannels)) {
                            if(Globals.DEBUG) {
                                System.out.println("栏目列表已刷新");
                            }
                            mChannels.clear();
                            mChannels.addAll(body.columnList);
                            //设置tab
                            mSlidingTabs.setViewPager(mViewPager, mSPTSOnPageChangedListener);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            if(Globals.DEBUG) {
                                System.out.println("栏目列表保持不变");
                            }
                        }

                        SharedPreferencesInfo.setTagString(ctx, SharedPreferencesInfo.CHANNELS, gson.toJson(body));
                        SharedPreferencesInfo.setTagBoolean(ctx, SharedPreferencesInfo.GET_CHANNEL_FAIL, false);

                        //增加第一个页面的栏目阅读量
                        if (mChannels.size() > 0) {
                            mCurrentChannelId = mChannels.get(0).id;
                            StatisticsDao.saveStatistics(getActivity(), "channelView", mCurrentChannelId);
                        }
                    } else {
                        ToastUtil.showToast(ctx, R.string.get_channel_list_fail);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToast(mContext, "刷新频道出错" + e.getMessage());
                }
            }

            @Override
            public void onErrorResponse() {
                if (Globals.DEBUG) {
                    LogUtil.e(getActivity(), "HeiKuai", R.string.get_channel_list_fail);
                }
            }
        };
        WebServiceIf.getNewsChannels(getActivity(), requestBody, callbackIf);
    }

    /**
     * 对比两个频道列表的内容是否相同
     *
     * @param a
     * @param b
     * @return
     */
    private boolean compareChannel(ArrayList<ChannelInfo> a, ArrayList<ChannelInfo> b) {
        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            ChannelInfo infoA = a.get(i);
            for (int j = 0; j < b.size(); j++) {
                ChannelInfo infoB = b.get(j);
                if (infoA.id.equals(infoB.id)) {
                    break;
                }
                if (j == b.size() - 1) {
                    return false;
                }
            }
        }

        for (int i = 0; i < b.size(); i++) {
            ChannelInfo infoB = b.get(i);
            for (int j = 0; j < a.size(); j++) {
                ChannelInfo infoA = a.get(j);
                if (infoA.id.equals(infoB.id)) {
                    break;
                }
                if (j == a.size() - 1) {
                    return false;
                }
            }
        }


        return true;
    }

    class NewsFragmentAdapter extends FragmentStatePagerAdapter {
        private ArrayList<ChannelInfo> mChannelList;
        // 存储当前已加载fragment的map
        private HashMap<String, NewsListFragment> mFragments = new HashMap<String, NewsListFragment>();

        public NewsFragmentAdapter(FragmentManager fm, ArrayList<ChannelInfo> channels) {
            super(fm);
            mChannelList = channels;
        }

        @Override
        public void notifyDataSetChanged() {
            // 数据改变时，清除之前的fragment map
            mFragments.clear();
            super.notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments.get(mChannelList.get(position).id) == null) {
                NewsListFragment fragment = new NewsListFragment();
                return fragment;
            } else {
                return mFragments.get(mChannelList.get(position).id);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mChannelList.get(position).name;
        }

        @Override
        public int getCount() {
            int count = 0;
            if (mChannelList != null) {
                count = mChannelList.size();
            }
            return count;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            NewsListFragment fragment = (NewsListFragment) super.instantiateItem(container, position);
            fragment.setChannelId(mChannelList.get(position).id);
            // 将当前fragment实例保存到map中
            mFragments.put(mChannelList.get(position).id, fragment);
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        // 获得当前fragment实例
        public NewsListFragment getCurItem(int position) {
            return mFragments.get(mChannelList.get(position).id);
        }
    }

    /**
     * 通过cityCode获取城市名称
     * @param cityCode
     */
    private void setCityNameViaCode(String cityCode) {
        String[] cityCodeAry = getResources().getStringArray(R.array.city_code);
        String[] cityAry = getResources().getStringArray(R.array.city);
        for(int i = 0; i < cityCodeAry.length; i++) {
            if (cityCodeAry[i].equals(cityCode)) {
                mCity = cityAry[i];
                break;
            }
        }
    }

    /**
     * 刷新频道数据
     * @param cityCode
     */
    private void resetChannelData(String cityCode) {
        mViewPager.setCurrentItem(0);
        SharedPreferencesInfo.setTagString(mContext, SharedPreferencesInfo.CITY_CODE, cityCode);
        setCityNameViaCode(cityCode);
        mTvCity.setText(mCity);
        getNewsChannel(getActivity());
    }

    /**
     * 弹出广告对话框
     *
     * @param advInfo
     */
    private void showAdvDialog(final AdvInfoObject advInfo) {
        final CustomDialog dialog = new CustomDialog(mContext, R.style.adv_dialog, R.layout.dialog_adv);
        dialog.setCanceledOnTouchOutside(false);
        View dialogView = dialog.getCustomView();
        ImageView ivClose = dialogView.findViewById(R.id.ivClose_dialog_adv);
        ImageView ivAdv = dialogView.findViewById(R.id.ivAdv_dialog_adv);

        final int index = advInfo.getSortIndex(advInfo.getCurrentIndex());
        try {
            GlideApp.with(this)
                    .load(advInfo.adUrl[index])
                    .error(R.drawable.image_bg)
                    .into(ivAdv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ivAdv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(advInfo.jumpUrl[index])) {
                    StatisticsDao.saveStatistics(mContext, "advClick", advInfo.advId[index]); // 广告点击统计
                    Intent topIntent = new Intent();
                    topIntent.putExtra("url", advInfo.jumpUrl[index]);
                    topIntent.putExtra("from", "adv");
                    topIntent.putExtra("type", "0");
                    topIntent.putExtra("source", "广告");
                    WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                    webDispatcher.dispatch(topIntent, mContext);
                }
            }
        });

        dialog.show();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
//        lp.width = (AppUtil.getScreenW(getActivity())) * 2 / 3; // 设置宽度
        lp.y = -(AppUtil.dp2px(mContext,24));
        dialog.getWindow().setAttributes(lp);
    }
}

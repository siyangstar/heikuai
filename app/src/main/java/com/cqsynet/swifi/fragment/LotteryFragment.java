/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获奖界面和奖品失效界面
 *
 * 创建标识：sayaki 20170321
 */
package com.cqsynet.swifi.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.LotteryDetailActivity;
import com.cqsynet.swifi.activity.SimpleWebActivity;
import com.cqsynet.swifi.model.LotteryInfo;
import com.cqsynet.swifi.model.LotteryListRequestBody;
import com.cqsynet.swifi.model.LotteryListResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Author: sayaki
 * Date: 2017/3/21
 */
public class LotteryFragment extends Fragment implements AdapterView.OnItemClickListener {

    private PullToRefreshListView mListView;
    private TextView mTvRemind;
    private ArrayList<LotteryInfo> mLotteries = new ArrayList<>();
    private LotteryAdapter mAdapter;
    private int mTotalCount = 0; // 抽奖结果列表的总条数
    private long mFreshTime = 0; // 刷新时间
    private String mType = "0"; // 奖券类型
    private static final String TYPE_LOTTERY_AVAILABLE = "0";
    private static final String TYPE_LOTTERY_INVALID = "1";

    public static LotteryFragment create(String type) {
        LotteryFragment fragment = new LotteryFragment();
        Bundle bundle = new Bundle();
        bundle.putString("lottery_type", type);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mType = getArguments().getString("lottery_type");

        View view = inflater.inflate(R.layout.fragment_lottery, container, false);
        mListView = view.findViewById(R.id.lottery_available_list);
        mTvRemind = view.findViewById(R.id.no_data_remind);
        mListView.setPullToRefreshOverScrollEnabled(false);
        mAdapter = new LotteryAdapter(getActivity(), mLotteries);
        mListView.setAdapter(mAdapter);
        mFreshTime = System.currentTimeMillis();

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                getLotteries("", mType);
            }
        });
        mListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (mTotalCount != 0) {
                    if (mAdapter.getCount() < mTotalCount && !mListView.isRefreshing()) {
                        LotteryInfo lottery = (LotteryInfo) mAdapter.getItem(mAdapter.getCount() - 1);
                        getLotteries(lottery.getId(), mType);
                    } else {
                        ToastUtil.showToast(LotteryFragment.this.getActivity(), R.string.no_more_item);
                    }
                }
            }
        });
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                "更新于:" + DateUtil.getRelativeTimeSpanString(mFreshTime));
        mListView.setOnItemClickListener(this);

        getLotteries("", mType);

        return view;
    }

    private void getLotteries(final String startId, String type) {
        LotteryListRequestBody body = new LotteryListRequestBody();
        body.start = startId;
        body.type = type;

        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                mListView.onRefreshComplete();
                if (response != null && !TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    LotteryListResponseObject responseObject = gson.fromJson(response, LotteryListResponseObject.class);
                    ResponseHeader header = responseObject.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        if ("".equals(startId)) {
                            refreshLottery(responseObject.body);
                        } else {
                            loadMoreLottery(responseObject.body);
                        }
                    } else {
                        ToastUtil.showToast(LotteryFragment.this.getActivity(), R.string.get_lottery_fail);
                    }
                }
                noDataRemind();
            }

            @Override
            public void onErrorResponse() {
                mListView.onRefreshComplete();
                noDataRemind();
                ToastUtil.showToast(LotteryFragment.this.getActivity(), R.string.request_fail_warning);
            }
        };
        WebServiceIf.getLotteryList(LotteryFragment.this.getActivity(), body, callback);
    }

    /**
     * 无数据提示
     */
    private void noDataRemind() {
        if (mLotteries.isEmpty()) {
            if (mType.equals("0")) {
                mTvRemind.setText(R.string.no_lottery_available);
            } else {
                mTvRemind.setText(R.string.no_lottery_invalid);
            }
            mTvRemind.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
    }

    private void refreshLottery(LotteryListResponseObject.LotteryListResponseBody body) {
        if (body.myLotteryList == null || body.myLotteryList.size() == 0) {
            mTvRemind.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mTvRemind.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            mFreshTime = System.currentTimeMillis();
            mListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                    "更新于：" + DateUtil.getRelativeTimeSpanString(mFreshTime));
            mLotteries.clear();

            mLotteries.addAll(body.myLotteryList);
            mAdapter.notifyDataSetChanged();
            mTotalCount = body.myLotteryCount;
        }
    }

    private void loadMoreLottery(LotteryListResponseObject.LotteryListResponseBody body) {
        if (body.myLotteryList == null || body.myLotteryList.size() == 0) {
            ToastUtil.showToast(getActivity(), R.string.no_more_item);
        } else {
            mLotteries.addAll(body.myLotteryList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (id < 0 || id >= mLotteries.size()) {
            return;
        }
        LotteryInfo lottery = mLotteries.get((int) id);
        String url = lottery.getUrl();
        if (!TextUtils.isEmpty(url)) {
            String userAccount = SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.ACCOUNT);
            Intent urlIntent = new Intent(getActivity(), SimpleWebActivity.class);
            urlIntent.putExtra("url", url + "?userAccount=" + userAccount + "&win_pk_id=" + lottery.getId());
            startActivity(urlIntent);
        } else {
            Intent intent = new Intent(getActivity(), LotteryDetailActivity.class);
            intent.putExtra("prizeId", lottery.getId());
            intent.putExtra("type", TYPE_LOTTERY_AVAILABLE);
            startActivity(intent);
        }
    }

    private class LotteryAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<LotteryInfo> mInfos;

        public LotteryAdapter(Context context, ArrayList<LotteryInfo> infos) {
            this.mContext = context;
            this.mInfos = infos;
        }

        @Override
        public int getCount() {
            return mInfos != null ? mInfos.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LotteryInfo info = mInfos.get(position);
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_lottery, parent, false);
                holder = new ViewHolder();
                holder.tvLotteryTitle = convertView.findViewById(R.id.tv_lottery_title);
                holder.tvLotteryPrize = convertView.findViewById(R.id.tv_lottery_prize);
                holder.tvLotteryPersonCount = convertView.findViewById(R.id.tv_lottery_person_count);
                holder.ibtnLotteryInvalid = convertView.findViewById(R.id.ibtn_lottery_invalid);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvLotteryTitle.setText(info.getTitle());
            holder.tvLotteryPrize.setText(info.getPrizeClass());
            holder.tvLotteryPersonCount.setText(String.format("%s人参与", info.getPersonCount()));
            holder.ibtnLotteryInvalid.setVisibility(
                    mType.equals(TYPE_LOTTERY_AVAILABLE) ? View.GONE : View.VISIBLE);

            return convertView;
        }
    }

    private class ViewHolder {
        TextView tvLotteryTitle;
        TextView tvLotteryPrize;
        TextView tvLotteryPersonCount;
        ImageView ibtnLotteryInvalid;
    }
}

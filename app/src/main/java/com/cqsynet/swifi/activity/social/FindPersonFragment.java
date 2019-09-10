/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：找人主界面
 *
 *
 * 创建标识：sayaki 20171123
 */
package com.cqsynet.swifi.activity.social;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.BottleActivity;
import com.cqsynet.swifi.util.ToastUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/11/23
 */
public class FindPersonFragment extends Fragment implements View.OnClickListener {

    private CardView mCvTrain;
    private CardView mCvStation;
    private CardView mCvNearby;
    private CardView mCvLine;
    private CardView mCvBottle;
    private Iterator<CardView> mCardViewIterator;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_person, container, false);

        mCvTrain = view.findViewById(R.id.cvTrain_fragment_find_person);
        mCvStation = view.findViewById(R.id.cvStation_fragment_find_person);
        mCvNearby = view.findViewById(R.id.cvNearby_fragment_find_person);
        mCvLine = view.findViewById(R.id.cvLine_fragment_find_person);
        mCvBottle = view.findViewById(R.id.cvBottle_fragment_find_person);

        mCvTrain.setOnClickListener(this);
        mCvStation.setOnClickListener(this);
        mCvNearby.setOnClickListener(this);
        mCvLine.setOnClickListener(this);
        mCvBottle.setOnClickListener(this);

        List<CardView> cardList = new LinkedList<>();
        cardList.add(mCvStation);
        cardList.add(mCvNearby);
        cardList.add(mCvLine);
        cardList.add(mCvBottle);
        Collections.shuffle(cardList);
        mCardViewIterator = cardList.iterator();

        //先显示同列车
        Message msg = new Message();
        msg.obj = mCvTrain;
        msg.what = 0;
        mHdl.sendMessageDelayed(msg, 200);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //解决getActivity()为null的问题
        mContext = context;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cvTrain_fragment_find_person:
                FindPersonActivity.launch(mContext, FindPersonActivity.TYPE_TRAIN);
                break;
            case R.id.cvLine_fragment_find_person:
                FindPersonActivity.launch(mContext, FindPersonActivity.TYPE_LINE);
                break;
            case R.id.cvStation_fragment_find_person:
                FindPersonActivity.launch(mContext, FindPersonActivity.TYPE_STATION);
                break;
            case R.id.cvNearby_fragment_find_person:
                FindPersonActivity.launch(mContext, FindPersonActivity.TYPE_NEARBY);
                break;
            case R.id.cvBottle_fragment_find_person:
                //用户是否被冻结
                if (Globals.g_userInfo.lock.equals("1")) {
                    ToastUtil.showToast(mContext, Globals.g_userInfo.lockMsg);
                } else {
                    Intent intent = new Intent(mContext, BottleActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private Handler mHdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    CardView cardView = (CardView) msg.obj;
                    cardView.setVisibility(View.VISIBLE);
                    cardView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.social_btn_fade_in));
                    showCardView();
                    break;
            }
        }
    };

    /**
     * 逐个显示item
     */
    private void showCardView() {
        if(mCardViewIterator.hasNext()) {
            Message msg = new Message();
            msg.obj = mCardViewIterator.next();
            msg.what = 0;
            mHdl.sendMessageDelayed(msg, 400);
        }
    }
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：尚WIFI消息中心Activity。
 *
 *
 * 创建标识：XY 20160302
 */
package com.cqsynet.swifi.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.PushAdapter;
import com.cqsynet.swifi.db.MessageDao;
import com.cqsynet.swifi.model.MessageInfo;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.DeleteDialog;
import com.cqsynet.swifi.view.SwipeMenu;
import com.cqsynet.swifi.view.SwipeMenuCreator;
import com.cqsynet.swifi.view.SwipeMenuItem;
import com.cqsynet.swifi.view.SwipeMenuListView;
import com.cqsynet.swifi.view.TitleBar;

import java.util.ArrayList;

public class MessageCenterActivity extends HkActivity implements OnClickListener {

    private ArrayList<MessageInfo> mMessageList;
    private SwipeMenuListView mSwipeListView;
    private TitleBar mTitleBar;
    private PushAdapter mPushAdapter;
    private String mUseraccount;
    private MessageReceiver mMessageReceiver;
    private DeleteDialog mDialog;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        mContext = MessageCenterActivity.this;

        mUseraccount = SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.PHONE_NUM);
        mMessageList = MessageDao.getInstance(mContext).getMsgAll(mUseraccount);

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(AppConstants.ACTION_REFRESH_RED_POINT);
        registerReceiver(mMessageReceiver, filter);

        mTitleBar = findViewById(R.id.back_titlebar_push);
        mTitleBar.setTitle("消息中心");
        mTitleBar.setLeftIconClickListener(this);
        mTitleBar.setRightIconClickListener(this);

        mPushAdapter = new PushAdapter(mContext, mMessageList);
        mSwipeListView = findViewById(R.id.show_pushmessage);
        TextView emptyView = findViewById(R.id.empty_view);
        mSwipeListView.setAdapter(mPushAdapter);
        mSwipeListView.setEmptyView(emptyView);
        // step 1. 创建一个MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(AppUtil.dp2px(MessageCenterActivity.this, 80));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mSwipeListView.setMenuCreator(creator);

        // step 2. 添加监听事件
        mSwipeListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                MessageInfo info = mMessageList.get(position);
                switch (index) {
                    case 0: // 删除
                        mMessageList.remove(position);
                        mPushAdapter.notifyDataSetChanged();
                        MessageDao.getInstance(mContext).deleteMsg(info.msgId);
                        ToastUtil.showToast(getApplicationContext(), "删除成功");
                        break;
                }
            }
        });

        // 设置监听
        mSwipeListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
            }

            @Override
            public void onSwipeEnd(int position) {
            }
        });

        mSwipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 跳转到指定activity查看消息内容，同时修改标记为已读
                MessageInfo info = mMessageList.get(position);
                clearReadPoint(info);
                if (info.type.equals(AppConstants.PUSH_ADV) || info.type.equals(AppConstants.PUSH_NEWS) || info.type.equals(AppConstants.PUSH_H5)) { // 营销类信息, 资讯, H5
                    Intent intent = new Intent();
                    intent.putExtra("url", info.url);
                    intent.putExtra("msgId", info.msgId);
                    intent.putExtra("from", "messageCenter");
                    intent.putExtra("type", "0");
                    intent.putExtra("source", "资讯");
                    WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                    webDispatcher.dispatch(intent, MessageCenterActivity.this);
                } else if (info.type.equals(AppConstants.PUSH_SYS_MESSAGE)) { // 系统消息跳转
                    Intent intent = new Intent(getApplicationContext(), ReadMessageActivity.class);
                    intent.putExtra("msgId", info.msgId);
                    startActivity(intent);
                } else if (info.type.equals(AppConstants.PUSH_GALLERY)) { // 资讯图集
                    Intent intent = new Intent();
                    intent.setClass(MessageCenterActivity.this, GalleryActivity.class);
                    intent.putExtra("id", info.contentId);
                    intent.putExtra("type", "1");
                    intent.putExtra("from", "messageCenter");
                    intent.putExtra("source", "图集");
                    intent.putExtra("msgId", info.msgId);
                    startActivity(intent);
                } else if (info.type.equals(AppConstants.PUSH_TOPIC)) { // 资讯专题
                    Intent intent = new Intent();
                    intent.setClass(MessageCenterActivity.this, TopicActivity.class);
                    intent.putExtra("id", info.contentId);
                    intent.putExtra("msgId", info.msgId);
                    intent.putExtra("from", "messageCenter");
                    intent.putExtra("type", "2");
                    intent.putExtra("source", "专题");
                    startActivity(intent);
                }
            }
        });
    }

    private void clearReadPoint(MessageInfo info) {
        MessageDao.getInstance(MessageCenterActivity.this).updateMsgStatus(info.msgId);
        mMessageList.clear();
        mMessageList.addAll(MessageDao.getInstance(mContext).getMsgAll(mUseraccount));
        mPushAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivBack_titlebar_layout) { // 返回
            finish();
        } else if (v.getId() == R.id.ivMenu_titlebar_layout) {// 清除所有信息
            int num = MessageDao.getInstance(mContext).getMsgNum(mUseraccount);
            String title = "删除所有消息";
            if (num > 0) {
                mDialog = new DeleteDialog(MessageCenterActivity.this,
                        R.style.round_corner_dialog, title, new DeleteDialog.MyDialogListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.tv_confirm_collect: // 确定:
                                mMessageList.clear();
                                mPushAdapter.notifyDataSetChanged();
                                MessageDao.getInstance(mContext).deleteMsgAll(mUseraccount);
                                mDialog.dismiss();
                                break;
                            case R.id.tv_cancel_collect: // 取消
                                mDialog.dismiss();
                                break;

                            default:
                                break;
                        }
                    }
                });
                mDialog.show();
            } else {
                return;
            }
        }
    }


    /**
     * 广播接收消息内容
     */
    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mMessageList.clear();
            mMessageList.addAll(MessageDao.getInstance(mContext).getMsgAll(mUseraccount));
            mPushAdapter.notifyDataSetChanged();
        }
    }
}

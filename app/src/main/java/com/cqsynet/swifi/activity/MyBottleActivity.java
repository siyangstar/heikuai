/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：我的瓶子
 *
 *
 * 创建标识：zhaosy 20161111
 */
package com.cqsynet.swifi.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.ChatListAdapter;
import com.cqsynet.swifi.db.BottleListDao;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.model.ChatListItemInfo;
import com.cqsynet.swifi.model.DeleteBottleRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.ResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.DeleteDialog;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MyBottleActivity extends HkActivity implements OnClickListener {

    private TitleBar mTitleBar;
    private ListView mListView;
    private TextView mTvNoBottleHint;
    private ChatListAdapter mAdapter;
    private DeleteDialog mDeleteDialog;
    private ArrayList<ChatListItemInfo> mChatList = new ArrayList<>();
    private MessageReceiver mMessageReceiver; //监听推送消息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bottle);

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_SOCKET_PUSH);
        filter.addAction(AppConstants.ACTION_UPDATE_DRAFT);
        registerReceiver(mMessageReceiver, filter);

        mTitleBar = findViewById(R.id.titlebar_activity_my_bottle);
        mTitleBar.setTitle("我的瓶子");
        mTitleBar.setLeftIconClickListener(this);
        mListView = findViewById(R.id.listView_activity_my_bottle);
        mTvNoBottleHint = findViewById(R.id.tvNoBottleHint_activity_my_bottle);
        mAdapter = new ChatListAdapter(this, mChatList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MyBottleActivity.this, ChatActivity.class);
                intent.putExtra("chatId", mAdapter.getItem(i).chatId);
                intent.putExtra("userAccount", mAdapter.getItem(i).userAccount);
                intent.putExtra("position", mAdapter.getItem(i).position);
                intent.putExtra("category", "0");
                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                showPopupDialog(i);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivBack_titlebar_layout) { // 返回
            finish();
        }
    }

    /**
     * 删除瓶子
     *
     * @param chatId 瓶子id
     */
    private void deleteBottle(final String chatId, final int position) {
        final ChatListItemInfo chatListItemInfo = mChatList.get(position);
        DeleteBottleRequestBody requestBody = new DeleteBottleRequestBody();
        requestBody.chatId = chatId;
        WebServiceIf.IResponseCallback callbackIf = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                dismissProgressDialog();
                if (response != null) {
                    Gson gson = new Gson();
                    ResponseObject responseObj = gson.fromJson(response, ResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(MyBottleActivity.this);
                            chatMsgDao.delAllChatMsgFromChatId(chatId);
                            BottleListDao bottleListDao = BottleListDao.getInstance(MyBottleActivity.this);
                            bottleListDao.delBottleListItem(chatListItemInfo);
                            mChatList.remove(chatListItemInfo);
                            mAdapter.notifyDataSetChanged();
                            if (mChatList.isEmpty()) {
                                mTvNoBottleHint.setVisibility(View.VISIBLE);
                                mListView.setVisibility(View.INVISIBLE);
                            }
                            Intent intent = new Intent(AppConstants.ACTION_UPDATE_MSG);
                            sendBroadcast(intent);
                        } else {
                            ToastUtil.showToast(MyBottleActivity.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(MyBottleActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.deleteBottle(this, requestBody, callbackIf);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottleListDao bottleListDao = BottleListDao.getInstance(this);
        mChatList.clear();
        mChatList.addAll(bottleListDao.queryBottleList(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT)));
        mAdapter.notifyDataSetChanged();

        //没有聊天数据时显示提示语
        if (mChatList.size() == 0) {
            mTvNoBottleHint.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
        } else {
            mTvNoBottleHint.setVisibility(View.INVISIBLE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMessageReceiver != null) {
            unregisterReceiver(mMessageReceiver);
        }
    }


    /**
     * 监听到新信息刷新界面
     */
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if ((!TextUtils.isEmpty(type) && type.equals(AppConstants.PUSH_BOTTLE))
                    || AppConstants.ACTION_UPDATE_DRAFT.equals(intent.getAction())) {
                BottleListDao bottleListDao = BottleListDao.getInstance(MyBottleActivity.this);
                mChatList.clear();
                mChatList.addAll(bottleListDao.queryBottleList(SharedPreferencesInfo.getTagString(MyBottleActivity.this, SharedPreferencesInfo.ACCOUNT)));
                mAdapter.notifyDataSetChanged();

                //没有聊天数据时显示提示语
                if (mChatList.size() == 0) {
                    mTvNoBottleHint.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.INVISIBLE);
                } else {
                    mTvNoBottleHint.setVisibility(View.INVISIBLE);
                    mListView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * 长按弹出框
     *
     * @param position
     */
    private void showPopupDialog(final int position) {
        final String[] itemAray = getResources().getStringArray(R.array.chat_popup_3);
        final CustomDialog dialog = new CustomDialog(this, R.style.round_corner_dialog, R.layout.listview_chat_popup);
        final String chatId = mChatList.get(position).chatId;
        ListView listview = dialog.getCustomView().findViewById(R.id.lv_listview_chat_popup);
        listview.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return itemAray.length;
            }

            @Override
            public Object getItem(int i) {
                return itemAray[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                view = View.inflate(MyBottleActivity.this, R.layout.chat_popup, null);
                TextView tvTitle = view.findViewById(R.id.tvTitle_chat_popup);
                tvTitle.setText(itemAray[i]);
                return view;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (itemAray[i].equals("删除")) {
                    mDeleteDialog = new DeleteDialog(MyBottleActivity.this, R.style.round_corner_dialog, "删除瓶子后对方将无法再向您发送消息", new DeleteDialog.MyDialogListener() {
                        @Override
                        public void onClick(View view) {
                            switch (view.getId()) {
                                case R.id.tv_confirm_collect://确定
                                    mDeleteDialog.dismiss();
                                    showProgressDialog("删除瓶子中...");
                                    deleteBottle(chatId, position);
                                    break;
                                case R.id.tv_cancel_collect: // 取消
                                    mDeleteDialog.dismiss();
                                    break;
                            }
                        }
                    });
                    mDeleteDialog.show();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

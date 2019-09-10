/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：个人中心页面。
 *
 *
 * 创建标识：zhaosy 20140922
 * 
 * 修改内容：更新UI    br 20150210
 */
package com.cqsynet.swifi.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.CollectActivity;
import com.cqsynet.swifi.activity.LoginActivity;
import com.cqsynet.swifi.activity.LotteryListActivity;
import com.cqsynet.swifi.activity.MessageCenterActivity;
import com.cqsynet.swifi.activity.MyCommentActivity;
import com.cqsynet.swifi.activity.SettingActivity;
import com.cqsynet.swifi.activity.SuggestActivity;
import com.cqsynet.swifi.activity.UserCenterActivity;
import com.cqsynet.swifi.activity.YouzanWebActivity;
import com.cqsynet.swifi.db.MessageDao;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.UserInfoResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.google.gson.Gson;

import org.json.JSONException;

public class FindFragment extends Fragment implements OnClickListener {

    private ImageView mIvHeadImg;
    private TextView mTvNickname;
    private TextView mTvMessageRemind;
    private TextView mTvCommentRemind;
    private RemindReceiver mRemindReceiver;
    private RefreshHeaderReceiver mRefreshHeaderReceiver;
    private LinearLayout mLlShakeGuild;
    private ImageView mIvShake;
    private ImageView mIvSettingRemind;
    private ImageView mIvSuggestionRemind;
    private Handler mHdl = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mLlShakeGuild.setVisibility(View.GONE);
                    break;
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 注册广播接收器
        mRemindReceiver = new RemindReceiver();
        IntentFilter filter = new IntentFilter(AppConstants.ACTION_REFRESH_RED_POINT);
        getActivity().registerReceiver(mRemindReceiver, filter);
        mRefreshHeaderReceiver = new RefreshHeaderReceiver();
        IntentFilter refreshHeaderfilter = new IntentFilter(AppConstants.ACTION_REFRESH_HEADER);
        getActivity().registerReceiver(mRefreshHeaderReceiver, refreshHeaderfilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mRemindReceiver);
        getActivity().unregisterReceiver(mRefreshHeaderReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        v.findViewById(R.id.llHead_find).setOnClickListener(this); // 编辑个人资料
        v.findViewById(R.id.llSetting_find).setOnClickListener(this);
        mTvMessageRemind = v.findViewById(R.id.tvMessageRemind_find);
        mTvCommentRemind = v.findViewById(R.id.tvCommentRemind_find);
        mIvHeadImg = v.findViewById(R.id.ivHeadImg_find);
        mTvNickname = v.findViewById(R.id.tvName_find);
        mIvSettingRemind = v.findViewById(R.id.ivSettingRemind_find);
        mIvSuggestionRemind = v.findViewById(R.id.ivSuggestRemind_find);
        v.findViewById(R.id.llJianPaHuo_find).setOnClickListener(this);
        v.findViewById(R.id.llMyLottery_find).setOnClickListener(this);
        v.findViewById(R.id.llCollection_find).setOnClickListener(this);
        v.findViewById(R.id.llMessage_find).setOnClickListener(this);
        v.findViewById(R.id.llComment_find).setOnClickListener(this);
        v.findViewById(R.id.llSuggestiong_find).setOnClickListener(this);

        // 第一次进入，显示半透明操作引导图层。
        if (!SharedPreferencesInfo.getTagBoolean(getActivity(), SharedPreferencesInfo.SHAKE_GUIDE, false)) {
            mLlShakeGuild = v.findViewById(R.id.llShakeGuild_find);
            mIvShake = v.findViewById(R.id.ivShake_find);
            mLlShakeGuild.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_suggest);
            mIvShake.startAnimation(anim);
            mHdl.sendEmptyMessageDelayed(0, 3000);
            SharedPreferencesInfo.setTagBoolean(getActivity(), SharedPreferencesInfo.SHAKE_GUIDE, true);
        }

        // 显示头像昵称
        if (Globals.g_userInfo == null) {
            Globals.g_userInfo = new Gson().fromJson(SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.USER_INFO), UserInfo.class);
        }
        if (Globals.g_userInfo != null) {
            refreshHeader(Globals.g_userInfo.headUrl);
            mTvNickname.setText(Globals.g_userInfo.nickname);
        }

        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.llHead_find) { // 编辑个人资料
            Intent intent = new Intent(getActivity(), UserCenterActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.llSetting_find) {// 设置
            startActivityForResult(new Intent(getActivity(), SettingActivity.class), 101);
        } else if (v.getId() == R.id.llJianPaHuo_find) { //捡耙活
            Intent intent = new Intent(getActivity(), YouzanWebActivity.class);
            intent.putExtra("url", AppConstants.YOUZAN_URL);
            startActivity(intent);
            //敬请期待
//			Intent intent = new Intent(getActivity(),WaitingActivity.class);
//			getActivity().startActivity(intent);
            //小程序
//            IWXAPI api = WXAPIFactory.createWXAPI(getContext(), AppConstants.WECHAT_APP_ID);
//            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
//            req.userName = "gh_10f9d45f9501"; // 填小程序原始id
////            req.path = "pages/goods/detail/index?alias=2olc5dbtwkt4k";                  //拉起小程序页面的可带参路径，不填默认拉起小程序首页
////            req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;// 可选打开 开发版，体验版和正式版
//            api.sendReq(req);
        } else if (v.getId() == R.id.llMyLottery_find) { //我的抽奖
            // 正式跳转
            Intent intent = new Intent(getActivity(), LotteryListActivity.class);
            //敬请期待跳转
//			Intent intent = new Intent(getActivity(), WaitingActivity.class);
            intent.putExtra("title", getString(R.string.my_account));
            getActivity().startActivity(intent);
        } else if (v.getId() == R.id.llMessage_find) { //消息
            mTvMessageRemind.setVisibility(View.GONE);
            Intent intent = new Intent(getActivity(), MessageCenterActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.llCollection_find) { //收藏
            CollectActivity.launch(getActivity());
        } else if (v.getId() == R.id.llComment_find) {  //评论
            mTvCommentRemind.setVisibility(View.GONE);
            Intent intent = new Intent(getActivity(), MyCommentActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.llSuggestiong_find) { //意见反馈
            Intent intent = new Intent(getActivity(), SuggestActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showNewSetting();
        showNewMessage();
        showNewSuggestion();
        showNewCommentReply();

        //显示头像
        if (Globals.g_userInfo == null) {
            Globals.g_userInfo = new Gson().fromJson(SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.USER_INFO), UserInfo.class);
        }
        if (mTvNickname.getText().toString().length() == 0 && Globals.g_userInfo != null) {
            refreshHeader(Globals.g_userInfo.headUrl);
            mTvNickname.setText(Globals.g_userInfo.nickname);
        }
        if (Globals.g_userInfo == null) {
            getUserInfo();
        }

    }

    /**
     * 刷新头像
     */
    private void refreshHeader(String path) {
        if (!TextUtils.isEmpty(path)) {
            GlideApp.with(this)
                    .load(path)
                    .circleCrop()
                    .error(R.drawable.icon_profile_default_round)
                    .into(mIvHeadImg);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            if (resultCode == 10) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        }
    }

    /**
     * 是否显示新消息的红点
     */
    public void showNewMessage() {
        String useraccount = SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.PHONE_NUM);
        int num = MessageDao.getInstance(getActivity()).getUnreadNum(useraccount);
        if (num != 0) {
            mTvMessageRemind.setVisibility(View.VISIBLE);
            mTvMessageRemind.setText(String.valueOf(num));
        } else {
            mTvMessageRemind.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 是否显示评论回复的红点
     */
    public void showNewCommentReply() {
        if (SharedPreferencesInfo.getTagBoolean(getActivity(), SharedPreferencesInfo.NEW_COMMENT_REPLY, false)) {
            int count = SharedPreferencesInfo.getTagInt(getActivity(), SharedPreferencesInfo.COMMENT_REPLY_COUNT);
            if(count != 0) {
                mTvCommentRemind.setVisibility(View.VISIBLE);
                mTvCommentRemind.setText(String.valueOf(count));
            } else {
                mTvCommentRemind.setVisibility(View.INVISIBLE);
            }
        } else {
            mTvCommentRemind.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 是否显示意见反馈的红点
     */
    public void showNewSuggestion() {
        if(SharedPreferencesInfo.getTagBoolean(getActivity(), SharedPreferencesInfo.NEW_SUGGEST, false)) {
            mIvSuggestionRemind.setVisibility(View.VISIBLE);
        } else {
            mIvSuggestionRemind.setVisibility(View.GONE);
        }
    }

    /**
     * 是否显示"设置"的红点
     */
    public void showNewSetting() {
        if (SharedPreferencesInfo.getTagBoolean(getActivity(), SharedPreferencesInfo.NEW_SETTING, false)
                && SharedPreferencesInfo.getTagBoolean(getActivity(), SharedPreferencesInfo.NEW_VERSION, false)) {
            mIvSettingRemind.setVisibility(View.VISIBLE);
        } else {
            mIvSettingRemind.setVisibility(View.GONE);
            SharedPreferencesInfo.setTagBoolean(getActivity(), SharedPreferencesInfo.NEW_SETTING, false);
        }
    }

    /**
     * 获取用户信息
     */
    private void getUserInfo() {
        // 调用接口
        WebServiceIf.getUserInfo(getActivity(), new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    UserInfoResponseObject responseObj = new Gson().fromJson(response, UserInfoResponseObject.class);
                    if (responseObj.header != null) {
                        if (AppConstants.RET_OK.equals(responseObj.header.ret)) {
                            if (responseObj.body != null) {
                                Globals.g_userInfo = responseObj.body;
                                SharedPreferencesInfo.setTagString(getActivity(), SharedPreferencesInfo.USER_INFO, new Gson().toJson(responseObj.body));
                                //显示头像
                                if (Globals.g_userInfo == null) {
                                    Globals.g_userInfo = new Gson().fromJson(SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.USER_INFO), UserInfo.class);
                                }
                                if (mTvNickname.getText().toString().length() == 0 && Globals.g_userInfo != null) {
                                    refreshHeader(Globals.g_userInfo.headUrl);
                                    mTvNickname.setText(Globals.g_userInfo.nickname);
                                }
                            } else {
                                ToastUtil.showToast(getActivity(), R.string.get_user_info_fail);
                            }
                        } else if (!TextUtils.isEmpty(responseObj.header.errMsg)) {
                            ToastUtil.showToast(getActivity(), responseObj.header.errMsg);
                        } else {
                            ToastUtil.showToast(getActivity(), R.string.get_user_info_fail);
                        }
                    } else {
                        ToastUtil.showToast(getActivity(), R.string.get_user_info_fail);
                    }
                } else {
                    ToastUtil.showToast(getActivity(), R.string.get_user_info_fail);
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(getActivity(), R.string.get_user_info_fail);
            }
        });

    }

    /**
     * 收到推送消息显示红点
     */
    private class RemindReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showNewSetting();
            showNewMessage();
            showNewSuggestion();
            showNewCommentReply();
        }
    }

    /**
     * 监听头像刷新广播
     */
    private class RefreshHeaderReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Globals.g_userInfo == null) {
                Globals.g_userInfo = new Gson().fromJson(SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.USER_INFO), UserInfo.class);
            }
            if (Globals.g_userInfo != null) {
                mTvNickname.setText(Globals.g_userInfo.nickname);
            }
            String headUrl = intent.getStringExtra("headUrl");
            if (!TextUtils.isEmpty(headUrl)) {
                refreshHeader(headUrl);
            }
        }
    }
}
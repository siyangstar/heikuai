/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：尚WIFI各Fragment的容器Activity。
 *
 *
 * 创建标识：luchaowei 20140922
 */
package com.cqsynet.swifi.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.social.PerfectInfoActivity;
import com.cqsynet.swifi.activity.social.SocialActivity;
import com.cqsynet.swifi.broadcast.StatisticsReceiver;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.FriendApplyDao;
import com.cqsynet.swifi.db.MessageDao;
import com.cqsynet.swifi.fragment.FindFragment;
import com.cqsynet.swifi.fragment.NewsMainFragment;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.YouzanLoginRequestBody;
import com.cqsynet.swifi.model.YouzanTokenResponse;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.service.PushService;
import com.cqsynet.swifi.util.CollectUtil;
import com.cqsynet.swifi.util.NetworkUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.StatisticsUtil;
import com.cqsynet.swifi.util.ToastUtil;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.PostRequest;

import org.json.JSONException;

public class HomeActivity extends BasicFragmentActivity implements OnClickListener {

    private StatisticsReceiver mStatisticsReceiver;
    private NewsMainFragment mNewsMainFragment;
    private FindFragment mFindFragment;
    private ImageView mIvWifi;
    private ImageView mIvNews;
    private ImageView mIvSns;
    private ImageView mIvFind;
    private ImageView mIvFindHint;
    private TextView mTvSnsHint; //社交未读消息红点
    private MessageReceiver mMessageReceiver;

    private BroadcastReceiver mWifiStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean status = intent.getBooleanExtra("wifi_status", false);
            if (status) {
                mIvWifi.setImageResource(R.drawable.btn_wifi_on_pressed);
            } else {
                mIvWifi.setImageResource(R.drawable.btn_wifi_home_selector);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction("action_wifi_status");
        registerReceiver(mWifiStatusReceiver, wifiFilter);

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_REFRESH_RED_POINT);
        filter.addAction(AppConstants.ACTION_SOCKET_PUSH);
        registerReceiver(mMessageReceiver, filter);

        mStatisticsReceiver = new StatisticsReceiver();
        IntentFilter statFilter = new IntentFilter();
        statFilter.addAction(AppConstants.ACTION_STATISTICS);
        statFilter.setPriority(2147483647);
        registerReceiver(mStatisticsReceiver, statFilter);

        StatisticsUtil.startSubmitStatistics(this);
        initYouZan();

        mIvFindHint = findViewById(R.id.iv_find_hint);
        mTvSnsHint = findViewById(R.id.tv_sns_hint);
        mIvNews = findViewById(R.id.iv_news);
        mIvWifi = findViewById(R.id.iv_wifi);
        mIvSns = findViewById(R.id.iv_sns);
        mIvFind = findViewById(R.id.iv_find);
        mIvNews.setOnClickListener(this);
        mIvWifi.setOnClickListener(this);
        mIvFind.setOnClickListener(this);
        mIvSns.setOnClickListener(this);
        mIvNews.setSelected(true);
        mIvNews.performClick();

        CollectUtil.getCollectData(HomeActivity.this);

//      if(!isIgnoringBatteryOptimizations()) {
//          gotoSettingIgnoringBatteryOptimizations();
//      }
    }

    private void checkWifi() {
        Long passedTime = System.currentTimeMillis()
                - SharedPreferencesInfo.getTagLong(this, SharedPreferencesInfo.FREE_WIFI_START_TIME);
        int freeTime = (int) ((SharedPreferencesInfo.getTagLong(this,
                SharedPreferencesInfo.FREE_WIFI_TIME) - passedTime) / 1000);
        Globals.mHasFreeAuthority = freeTime > 0;
        WifiManager wifiManager = (WifiManager) (this.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (NetworkUtil.isConnectFashionWiFi(wifiInfo)) {
            int ip = wifiInfo.getIpAddress();
            Globals.mIsConnectFreeWifi = ip != 0;
        } else {
            Globals.mIsConnectFreeWifi = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //开启守护进程
        Intent intent = new Intent(this, PushService.class);
        startService(intent);

        refreshBottleRedPoint();
        refreshFindRedPoint();

        // 防止缓存被清理
        if (Globals.g_userInfo == null) {
            Globals.g_userInfo = new Gson().fromJson(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.USER_INFO), UserInfo.class);
        }

        checkWifi();

        if (Globals.mHasFreeAuthority && Globals.mIsConnectFreeWifi) {
            mIvWifi.setImageResource(R.drawable.btn_wifi_on_home_selector);
        } else {
            mIvWifi.setImageResource(R.drawable.btn_wifi_home_selector);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//    	super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        mIvNews.setEnabled(false);
        mIvWifi.setEnabled(false);
        mIvSns.setEnabled(false);
        mIvFind.setEnabled(false);

        int id = view.getId();
        switch (id) {
            case R.id.iv_wifi: // 点击进入wifi管理页，
                WifiActivity.launch(this);
                break;
            case R.id.iv_news: // 点击进入新闻列表页。默认进入新闻列表页。
                if (mNewsMainFragment == null) {
                    mNewsMainFragment = new NewsMainFragment();
                }
                setFragment(mNewsMainFragment);
                mIvWifi.setSelected(false);
                mIvNews.setSelected(true);
                mIvSns.setSelected(false);
                mIvFind.setSelected(false);
                break;
            case R.id.iv_sns: // 点击进入社交
                if (Globals.g_userInfo == null) {
                    break;
                }
                //判断社交模块是否可用
                if (!TextUtils.isEmpty(Globals.g_userInfo.socialStatus) && Globals.g_userInfo.socialStatus.equals("1")) {
                    Intent intent = new Intent();
                    intent.setClass(this, SimpleWebActivity.class);
                    intent.putExtra("url", Globals.g_userInfo.socialErrorPage);
                    startActivity(intent);
                    break;
                }
                //判断用户是否被冻结
                if (!TextUtils.isEmpty(Globals.g_userInfo.lock) && Globals.g_userInfo.lock.equals("2")) {
                    ToastUtil.showToast(this, Globals.g_userInfo.lockMsg);
                    break;
                }
                //用户是否需要进入个人设置
                if (!TextUtils.isEmpty(Globals.g_userInfo.setting) && Globals.g_userInfo.setting.equals("1")) {
                    startActivity(new Intent(this, PerfectInfoActivity.class));
                } else {
                    startActivity(new Intent(this, SocialActivity.class));
                }
                //隐藏社交红点
                findViewById(R.id.ivSnsMore_home).setVisibility(View.GONE);
                SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.BOTTLE_NOTIFY_IN_HOME, false);
                break;
            case R.id.iv_find: // 点击进入更多页面。
                if (mFindFragment == null) {
                    mFindFragment = new FindFragment();
                }
                setFragment(mFindFragment);
                mIvWifi.setSelected(false);
                mIvNews.setSelected(false);
                mIvSns.setSelected(false);
                mIvFind.setSelected(true);
                break;
            default:
                break;
        }

        mIvNews.setEnabled(true);
        mIvWifi.setEnabled(true);
        mIvSns.setEnabled(true);
        mIvFind.setEnabled(true);
    }

    /**
     * @param targetFragment 需要显示的Fragment
     *                       Description: 切换Fragment
     */
    private void setFragment(Fragment targetFragment) {
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        if (targetFragment.isAdded()) {
            fm.show(targetFragment);
        } else {
            fm.add(R.id.flContainer_home, targetFragment);
        }

        if (mNewsMainFragment != null && !mNewsMainFragment.isHidden() && targetFragment != mNewsMainFragment) {
            fm.hide(mNewsMainFragment);
        } else if (mFindFragment != null && !mFindFragment.isHidden() && targetFragment != mFindFragment) {
            fm.hide(mFindFragment);
        }

        fm.commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        StatisticsUtil.stopSubmitStatistics(this);
        if (mStatisticsReceiver != null) {
            unregisterReceiver(mStatisticsReceiver);
            mStatisticsReceiver = null;
        }
        if (mMessageReceiver != null) {
            unregisterReceiver(mMessageReceiver);
            mMessageReceiver = null;
        }
        if (mWifiStatusReceiver != null) {
            unregisterReceiver(mWifiStatusReceiver);
            mWifiStatusReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // 不关闭activity,直接退到桌面
//        Intent setIntent = new Intent(Intent.ACTION_MAIN);
//        setIntent.addCategory(Intent.CATEGORY_HOME);
//        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(setIntent);
        moveTaskToBack(true);
    }

    /**
     * 在发现按钮上显示小红点
     */
    private void refreshFindRedPoint() {
        boolean unreadMsg = false; //是否有未读消息
        String useraccount = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.PHONE_NUM);
        int num = MessageDao.getInstance(this).getUnreadNum(useraccount);
        if (num != 0) {
            unreadMsg = true;
        }
        if (unreadMsg ||SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.NEW_SETTING, false)
                || SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.NEW_COMMENT_REPLY, false)) {
            mIvFindHint.setVisibility(View.VISIBLE);
        } else {
            mIvFindHint.setVisibility(View.GONE);
        }
    }

    /**
     * 刷新漂流瓶红点
     */
    private void refreshBottleRedPoint() {
        if (SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.BOTTLE_NOTIFY_IN_HOME, true)) {
            findViewById(R.id.ivSnsMore_home).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ivSnsMore_home).setVisibility(View.GONE);
        }
        //设置红点未读数量
        int count = ChatMsgDao.getInstance(this).queryAllUnReadMsgCount("friend");
        if(SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.MSG_BOTTLE, true)) {
            count += ChatMsgDao.getInstance(this).queryAllUnReadMsgCount("bottle");
        }
        if(SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.MSG_FRIEND_APPLY, true)) {
            count += FriendApplyDao.getInstance(this).queryUnReadApplyCount();
        }
        if (count < 100) {
            mTvSnsHint.setText(count + "");
        } else {
            mTvSnsHint.setText("···");
        }
        if (count != 0) {
            mTvSnsHint.setVisibility(View.VISIBLE);
        } else {
            mTvSnsHint.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化有赞用户
     */
    private void initYouZan() {
        if (Globals.g_userInfo != null) {
            removeYouzanToken();

            YouzanLoginRequestBody requestBody = new YouzanLoginRequestBody();
            requestBody.clientId = AppConstants.YOUZAN_CLIENT_ID;
            requestBody.clientSecret = AppConstants.YOUZAN_CLIENT_SECRET;
            requestBody.openUserId = Globals.g_userInfo.userAccount;
            requestBody.kdtId = AppConstants.YOUZAN_KDT_ID;
            loginYouzan(this, AppConstants.YOUZAN_LOGIN_URL, requestBody, new WebServiceIf.IResponseCallback() {

                @Override
                public void onResponse(String response) throws JSONException {
                    Gson gson = new Gson();
                    YouzanTokenResponse resp = gson.fromJson(response, YouzanTokenResponse.class);
                    YouzanTokenResponse.Token token = resp.getData();
                    if (token != null) {
                        SharedPreferencesInfo.setTagString(HomeActivity.this,
                                SharedPreferencesInfo.YOUZAN_COOKIE_KEY, token.getCookieKey());
                        SharedPreferencesInfo.setTagString(HomeActivity.this,
                                SharedPreferencesInfo.YOUZAN_COOKIE_VALUE, token.getCookieValue());
                        SharedPreferencesInfo.setTagString(HomeActivity.this,
                                SharedPreferencesInfo.YOUZAN_ACCESS_TOKEN, token.getAccessToken());
                    }
                }

                @Override
                public void onErrorResponse() {

                }
            });
        }
    }

    private void removeYouzanToken() {
        SharedPreferencesInfo.removeData(this, SharedPreferencesInfo.YOUZAN_COOKIE_KEY);
        SharedPreferencesInfo.removeData(this, SharedPreferencesInfo.YOUZAN_COOKIE_VALUE);
        SharedPreferencesInfo.removeData(this, SharedPreferencesInfo.YOUZAN_ACCESS_TOKEN);
    }

    private void loginYouzan(Context ctx, String url, YouzanLoginRequestBody requestBody,
                             final WebServiceIf.IResponseCallback callback) {
        PostRequest post = OkGo.post(url);
        post.isMultipart(false);
        post.tag(ctx)
                .params("client_id", requestBody.clientId)
                .params("client_secret", requestBody.clientSecret)
                .params("open_user_id", requestBody.openUserId)
                .params("kdt_id", requestBody.kdtId)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String res = response.body();
                            callback.onResponse(res);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        // 调用UI端注册的错误处理回调
                        callback.onErrorResponse();
                        if (Globals.DEBUG) {
                            response.getException().printStackTrace();
                        }
                    }
                });
    }

    /**
     * 广播接收消息内容
     */
    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(AppConstants.ACTION_SOCKET_PUSH)) {
                String type = intent.getStringExtra("type");
                if (!TextUtils.isEmpty(type) && (type.equals(AppConstants.PUSH_BOTTLE) || type.equals(AppConstants.PUSH_CHAT) || type.equals(AppConstants.PUSH_FRIEND_APPLY))) {
                    refreshBottleRedPoint();
                }
                refreshFindRedPoint();
            }
        }
    }


//  private final static int REQUEST_IGNORE_BATTERY_CODE = 1001;
//
//    /**
//     * 判断是否加入省电白名单
//     * @return
//     */
//    private boolean isIgnoringBatteryOptimizations(){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            String packageName = getPackageName();
//            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//            return pm.isIgnoringBatteryOptimizations(packageName);
//        }
//        return false;
//    }
//
//    /**
//     * 打开省电白名单询问框
//     */
//    private void gotoSettingIgnoringBatteryOptimizations() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            try {
//                Intent intent = new Intent();
//                String packageName = getPackageName();
//                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                intent.setData(Uri.parse("package:" + packageName));
//                startActivityForResult(intent, REQUEST_IGNORE_BATTERY_CODE);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == RESULT_OK){
//            if (requestCode == REQUEST_IGNORE_BATTERY_CODE) {
//                Log.d("Hello World!","忽略省电模式成功");
//            }
//        }else if (resultCode == RESULT_CANCELED) {
//            if (requestCode == REQUEST_IGNORE_BATTERY_CODE) {
//                Toast.makeText(this, "请忽略省电模式后才能后台接收消息~", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}

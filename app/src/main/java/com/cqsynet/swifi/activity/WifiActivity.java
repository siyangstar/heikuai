/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：Wifi管理界面
 *
 * 创建标识：sayaki 20170418
 */
package com.cqsynet.swifi.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.FreeWifiUseLogDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.AdvInfoObject;
import com.cqsynet.swifi.model.CloseFreeWifiRequestBody;
import com.cqsynet.swifi.model.OpenFreeWifiRequestBody;
import com.cqsynet.swifi.model.OpenFreeWifiResponseObject;
import com.cqsynet.swifi.model.RequestHeader;
import com.cqsynet.swifi.model.TodayWifiTotalUseTimeResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.service.TimerService;
import com.cqsynet.swifi.util.AdvDataHelper;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.NetworkUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.google.gson.Gson;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Author: sayaki
 * Date: 2017/4/18
 */
public class WifiActivity extends SwipeBackActivity implements View.OnClickListener, TimerService.TimerCallback {

    public static int REQUEST_CODE_FULL_AD = 1001;
    public static int REQUEST_CODE_WEB = 1002;

    private static final int MSG_SET_CURRENT_WIFI_INFO = 0;
    //    private static final int MSG_UPDATE_USER_GROUP = 1;
    private static final int MSG_OPEN_WIFI = 2;
    //    private static final int MSG_REFRESH_STATE = 3;
    private static final int MSG_START_SCALE_ANIMATION = 4;
    private static final int MSG_CLOSE_WIFI = 5;
    private static final int MSG_SEND_CLOSE_WIFI_REQUEST = 6;
    private static final int MSG_LOAD_WEB = 7;
    private static final int MSG_CLOSE_WEB = 8;
    private static final int MSG_FINISH_ACTIVITY = 10;

    private LinearLayout mLlSurplusTime;
    private LinearLayout mLlTodayUsedTime;
    private RelativeLayout mRlSwitcher;
    private ImageView mIvFreeOnOff;
    private TextView mTvSurplusTime;
    private TextView mTvTodayUsedTime;
    private TextView mTvTips;
    private TextView mTvTips2;
    private ImageView mIvGreenHalo;
    private ImageView mIvGreenHalo2;
    private ImageView mIvWhiteHalo;
    private ImageView mIvCircle;
    private LinearLayout mLlRenewWifiTime;
    private ImageView mIvRenewWifiTime;
    private TextView mTvRenewWifiTime;
    private FrameLayout mFlTopAdv;
    private ImageView mIvClose;
    private ImageView mIvTopAdv;
    private ImageView mIvBottomAdv;
    private TextView mTvHelp;
    private TextView mTvBulletin;
    private WebView mWvLink;

    private ArrayList<String> mLinkTimeList; //自动打开的页面需要显示的时间
    private Iterator<String> mLinkTimeIterator;
    // 本次剩余时长
    private int mFreeTime = 0;
    // 续时按钮是否可点击
    private boolean mCanRenew = false;
    private boolean mHasAdv = true;

    private String mTopAdvUrl;
    private String mBottomAdvUrl;

    private Animation mShakeAnim;
    private Animation mScaleFadeAnim;

    private FreeWifiUseLogDao mWifiUseDao;

    private String mTopAdvId; //顶部广告id
    private String mBottomAdvId; //底部广告id

    private Iterator<String> mLinkIterator; //需要后台访问的页面迭代器

    private String mSSID1 = "";
    private String mSSID2 = "";

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {// wifi连接上与否
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    Globals.mIsConnectFreeWifi = false;
                    System.out.println("wifi网络连接断开");
                    mHdl.sendEmptyMessage(MSG_SET_CURRENT_WIFI_INFO);
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    mHdl.sendEmptyMessageDelayed(MSG_SET_CURRENT_WIFI_INFO, 500);
                    WifiManager wifiManager = (WifiManager) (getApplicationContext().getSystemService(Context.WIFI_SERVICE));
                    wifiManager.startScan();
                }
            } else if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    mHdl.sendEmptyMessage(MSG_SET_CURRENT_WIFI_INFO);
                }
            }
        }
    };

    private Handler mHdl = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_CURRENT_WIFI_INFO:
                    setUseWifiInfo();
                    setCurrentWifiInfo();
                    break;
                case MSG_OPEN_WIFI:
                    //@@@@预留测试账号
                    if (SharedPreferencesInfo.getTagString(WifiActivity.this, SharedPreferencesInfo.PHONE_NUM).startsWith("170000")) {
                        Globals.g_getTime = 600; // 假数据,用于测试
                        Globals.mHasFreeAuthority = true;
                        mFreeTime = Globals.g_getTime - 5; //减少5秒钟,缓冲和服务器时间的误差
                        SharedPreferencesInfo.setTagLong(WifiActivity.this, SharedPreferencesInfo.FREE_WIFI_START_TIME,
                                System.currentTimeMillis());
                        SharedPreferencesInfo.setTagLong(WifiActivity.this, SharedPreferencesInfo.FREE_WIFI_TIME,
                                mFreeTime * 1000);
                        mWifiUseDao.initTodayUse(DateUtil.formatTime(new Date(), "yyyy-MM-dd"));
                        startCircleAnim();
                        setUseWifiInfo();
                        setCurrentWifiInfo();

                        Intent i = new Intent();
                        i.putExtra("wifi_status", true);
                        i.setAction("action_wifi_status");
                        sendBroadcast(i);

                        bindTimerService();

                        // 连接成功后退出wifi界面
                        mHdl.sendEmptyMessageDelayed(MSG_FINISH_ACTIVITY, 1700);
                        break;
                    } else {
                        openWifi(msg.arg1);
                    }
                    break;
                case MSG_START_SCALE_ANIMATION:
                    mIvGreenHalo2.startAnimation(AnimationUtils.loadAnimation(WifiActivity.this, R.anim.scale_fade2));
                    break;
                case MSG_CLOSE_WIFI:
                    Globals.g_isUpdateUserGroup = false;
                    mIvFreeOnOff.setEnabled(true);
                    Globals.mHasFreeAuthority = false;
                    mFreeTime = 0;
                    setUseWifiInfo();
                    setCurrentWifiInfo();
                    stopCircleAnim();
                    mIvWhiteHalo.clearAnimation();
                    SharedPreferencesInfo.setTagLong(WifiActivity.this, SharedPreferencesInfo.FREE_WIFI_START_TIME, 0L);
                    Intent i = new Intent();
                    i.putExtra("wifi_status", false);
                    i.setAction("action_wifi_status");
                    sendBroadcast(i);
                    break;
                case MSG_SEND_CLOSE_WIFI_REQUEST:
                    sendCloseWiFiRequest();
                    break;
                case MSG_LOAD_WEB: //加载网页
                    if (Globals.DEBUG) {
                        System.out.println("start WifiFragment link visit = " + msg.obj);
                    }
                    mWvLink.loadUrl((String) msg.obj);
                    break;
                case MSG_CLOSE_WEB: //关闭浏览器页面
                    mWvLink.loadUrl("about:blank");
                    break;
                case MSG_FINISH_ACTIVITY:
                    setResult(100); // 成功
                    finish();
                    overridePendingTransition(0, R.anim.tranlate_out);
                    break;
            }
        }
    };

    private TimerService mTimerService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTimerService = ((TimerService.TimerBinder) service).getService();
            mTimerService.setTimerCallback(WifiActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimerService.setTimerCallback(null);
        }
    };

    public static void launch(Activity context) {
        Intent intent = new Intent();
        intent.setClass(context, WifiActivity.class);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.tranlate_in, 0);
    }

    public static void launchForResult(Activity context, boolean hasAdv) {
        Intent intent = new Intent();
        intent.setClass(context, WifiActivity.class);
        intent.putExtra("hasAdv", hasAdv);
        context.startActivityForResult(intent, REQUEST_CODE_WEB);
        context.overridePendingTransition(R.anim.tranlate_in, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_BOTTOM);
        getSwipeBackLayout().setEdgeSize(AppUtil.getScreenH(this));
        getSwipeBackLayout().setScrollThresHold(0.02f);
        getSwipeBackLayout().addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {

            }

            @Override
            public void onEdgeTouch(int edgeFlag) {

            }

            @Override
            public void onScrollOverThreshold() {
                if (Globals.mHasFreeAuthority) {
                    setResult(100);
                } else {
                    setResult(200);
                }
            }
        });
        initView();

        // 第一次进入，显示半透明操作引导图层。
        if (!SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.WIFI_GUIDE, false)) {
            Intent intent = new Intent(this, OperateGuideActivity.class);
            intent.putExtra("type", OperateGuideActivity.INDEX_WIFI);
            startActivity(intent);
        }

        mHasAdv = getIntent().getBooleanExtra("hasAdv", true);

        if (savedInstanceState != null) {
            Globals.g_getTime = savedInstanceState.getInt("getTime", 1800); //默认每次上网1800秒
        }
        mWifiUseDao = FreeWifiUseLogDao.getInstance(this);

        registerReceiver();

        mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_clock);
        mScaleFadeAnim = AnimationUtils.loadAnimation(this, R.anim.scale_fade);
        StatisticsDao.saveStatistics(this, "wifi", ""); // 切换到wifi页面统计

        String source = getIntent().getStringExtra("flag");
        if ("fullAdv".equals(source)) {
            int accessType = getIntent().getIntExtra("accessType", 0);
            boolean hasAdv = getIntent().getBooleanExtra("hasAdv", true);
            Message msg = new Message();
            msg.what = MSG_OPEN_WIFI;
            msg.arg1 = accessType;
            //若没有广告,提权后要延时2秒才发送上线请求
            if (!hasAdv) {
                mHdl.sendMessageDelayed(msg, 2000);
            } else {
                mHdl.sendMessage(msg);
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        mRlSwitcher = (RelativeLayout) findViewById(R.id.rl_switcher);
        mIvFreeOnOff = (ImageView) findViewById(R.id.iv_free_on_off);
        mIvFreeOnOff.setOnClickListener(this);
        mLlSurplusTime = (LinearLayout) findViewById(R.id.ll_surplus_time);
        mLlTodayUsedTime = (LinearLayout) findViewById(R.id.ll_today_used_time);
        mLlSurplusTime.setOnClickListener(this);
        mLlTodayUsedTime.setOnClickListener(this);
        mTvSurplusTime = (TextView) findViewById(R.id.tv_surplus_time);
        mTvTodayUsedTime = (TextView) findViewById(R.id.tv_today_used_time);
        mTvTips = (TextView) findViewById(R.id.tv_tips);
        mTvTips2 = (TextView) findViewById(R.id.tv_tips_2);
        mIvCircle = (ImageView) findViewById(R.id.iv_circle);
        mIvGreenHalo = (ImageView) findViewById(R.id.iv_green_halo);
        mIvGreenHalo2 = (ImageView) findViewById(R.id.iv_green_halo2);
        mIvWhiteHalo = (ImageView) findViewById(R.id.iv_white_halo);

        mLlRenewWifiTime = (LinearLayout) findViewById(R.id.ll_renew);
        mLlRenewWifiTime.setOnClickListener(this);
        mIvRenewWifiTime = (ImageView) findViewById(R.id.iv_renew_wifi_time);
        mTvRenewWifiTime = (TextView) findViewById(R.id.tv_renew);
        mFlTopAdv = (FrameLayout) findViewById(R.id.fl_top_adv);
        mIvClose = (ImageView) findViewById(R.id.iv_close_adv);
        mTvHelp = (TextView) findViewById(R.id.tv_help);
        mTvHelp.setOnClickListener(this);
        mTvBulletin = (TextView) findViewById(R.id.tv_bulletin);
        mTvBulletin.setOnClickListener(this);

        mIvTopAdv = (ImageView) findViewById(R.id.iv_top_adv);
        mIvBottomAdv = (ImageView) findViewById(R.id.iv_bottom_adv);
        setAdvHeight(mIvTopAdv);
        setAdvHeight(mIvBottomAdv);

        mIvTopAdv.setOnClickListener(this);
        mIvBottomAdv.setOnClickListener(this);

        if (!Globals.mIsShowTopAdv) {
            mFlTopAdv.setVisibility(View.GONE);
        }
        mIvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlTopAdv.setVisibility(View.GONE);
                Globals.mIsShowTopAdv = false;
            }
        });

        mWvLink = (WebView) findViewById(R.id.wv_link);
        mWvLink.getSettings().setJavaScriptEnabled(true);
        mWvLink.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (Globals.DEBUG) {
                    System.out.println("end WifiActivity link visit = " + url);
                }
                if (mLinkIterator.hasNext()) {
                    int time = 10000; //默认10秒
                    if (mLinkTimeIterator != null && mLinkTimeIterator.hasNext()) {
                        time = Integer.parseInt(mLinkTimeIterator.next());
                    }
                    Message msg = new Message();
                    msg.what = MSG_LOAD_WEB;
                    msg.obj = mLinkIterator.next();
                    mHdl.sendMessageDelayed(msg, time);
                } else {
                    if (!url.equals("about:blank")) {
                        int time = 10000; //默认10秒
                        if (mLinkTimeIterator != null && mLinkTimeIterator.hasNext()) {
                            time = Integer.parseInt(mLinkTimeIterator.next());
                        }
                        mHdl.sendEmptyMessageDelayed(MSG_CLOSE_WEB, time);
                    }
                }
            }
        });
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.setPriority(2147483647);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AdvDataHelper advDataHelper = new AdvDataHelper(this, null);
        advDataHelper.loadAdvData();
        refreshAdv(advDataHelper.getAdvData());
        getTodayWifiUseTime(); //从服务器获取当日使用时长,避免本地计时不准
        StatisticsDao.saveStatistics(this, "wifi", ""); // 切换到wifi页面统计

        Long passedTime = System.currentTimeMillis()
                - SharedPreferencesInfo.getTagLong(this, SharedPreferencesInfo.FREE_WIFI_START_TIME);
        int freeTime = (int) ((SharedPreferencesInfo.getTagLong(this,
                SharedPreferencesInfo.FREE_WIFI_TIME) - passedTime) / 1000);
        if (freeTime > 0) {
            Globals.mHasFreeAuthority = true;
            startCircleAnim();
            mFreeTime = freeTime;
            bindTimerService();
        } else {
            mFreeTime = 0;
            Globals.mHasFreeAuthority = false;
            stopCircleAnim();
        }

        WifiManager wifiManager = (WifiManager) (getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        wifiManager.startScan();
        setUseWifiInfo();
        setCurrentWifiInfo();
    }

    @Override
    public void onBackPressed() {
        if (Globals.mHasFreeAuthority) {
            setResult(100);
        } else {
            setResult(200);
        }
        super.onBackPressed();
        overridePendingTransition(0, R.anim.tranlate_out);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimerService != null) {
            mTimerService.setTimerCallback(null);
        }
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("getTime", Globals.g_getTime);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String source = intent.getStringExtra("flag");
        if ("fullAdv".equals(source)) {
            int accessType = intent.getIntExtra("accessType", 0);
            boolean hasAdv = intent.getBooleanExtra("hasAdv", true);
            Message msg = new Message();
            msg.what = MSG_OPEN_WIFI;
            msg.arg1 = accessType;
            //若没有广告,提权后要延时2秒才发送上线请求
            if (!hasAdv) {
                mHdl.sendMessageDelayed(msg, 2000);
            } else {
                mHdl.sendMessage(msg);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 20) {
            if (requestCode == REQUEST_CODE_FULL_AD) { // 开始联网
                int flag = data.getIntExtra("flag", 0);
                boolean hasAdv = data.getBooleanExtra("hasAdv", true);
                Message msg = new Message();
                msg.what = MSG_OPEN_WIFI;
                msg.arg1 = flag;
                //若没有广告,提权后要延时2秒才发送上线请求
                if (!hasAdv) {
                    mHdl.sendMessageDelayed(msg, 2000);
                } else {
                    mHdl.sendMessage(msg);
                }
            }
        }
    }

    public void refreshAdv(List<AdvInfoObject> data) {
        if (data != null && !data.isEmpty()) {
            for (AdvInfoObject advInfo : data) {
                if ("ad0002".equals(advInfo.id) && mIvTopAdv != null) {
                    showAdv(advInfo, mIvTopAdv);
                } else if ("ad0003".equals(advInfo.id) && mIvBottomAdv != null) {
                    showAdv(advInfo, mIvBottomAdv);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_renew:
                if (mCanRenew) {
                    mLlRenewWifiTime.setEnabled(false);
                    Intent aIntent = new Intent(this, FullAdvActivity.class);
                    aIntent.putExtra("accessType", 1);
                    aIntent.putExtra("source", "WifiActivity");
                    startActivity(aIntent);
                    finish();
                }
                break;
            case R.id.iv_top_adv:
                if (!TextUtils.isEmpty(mTopAdvUrl)) {
                    StatisticsDao.saveStatistics(this, "advClick", mTopAdvId); // 顶部广告点击统计
                    Intent topIntent = new Intent();
                    topIntent.putExtra("url", mTopAdvUrl);
                    topIntent.putExtra("from", "adv");
                    topIntent.putExtra("type", "0");
                    topIntent.putExtra("source", "广告");
                    WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                    webDispatcher.dispatch(topIntent, this);
                }
                break;
            case R.id.iv_bottom_adv:
                if (!TextUtils.isEmpty(mBottomAdvUrl)) {
                    StatisticsDao.saveStatistics(this, "advClick", mBottomAdvId); // 底部广告点击统计
                    Intent bottomIntent = new Intent();
                    bottomIntent.putExtra("url", mBottomAdvUrl);
                    bottomIntent.putExtra("from", "adv");
                    bottomIntent.putExtra("type", "0");
                    bottomIntent.putExtra("source", "广告");
                    WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                    webDispatcher.dispatch(bottomIntent, this);
                }
                break;
            case R.id.ll_surplus_time:
                Intent historyIntent = new Intent(this, WiFiUseHistoryActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.ll_today_used_time:
                Intent historyIntent2 = new Intent(this, WiFiUseHistoryActivity.class);
                startActivity(historyIntent2);
                break;
            case R.id.iv_free_on_off:
                final WifiManager wifiManager = (WifiManager) (this.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
                if (!wifiManager.isWifiEnabled()) {
                    if (SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.WIFI_TIP, false)) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    } else {
                        startActivity(new Intent(this, WifiTipActivity.class));
                    }
                } else {
                    mSSID1 = "";
                    mSSID2 = "";
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if(wifiInfo != null) {
                        mSSID1 = wifiInfo.getSSID();
                    }
                    ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if(connManager != null) {
                        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (networkInfo != null && networkInfo.isConnected()) {
                            if (networkInfo.getExtraInfo() != null) {
                                mSSID2 = networkInfo.getExtraInfo();
                            }
                        }
                    }
                    if (NetworkUtil.formatSSID(mSSID1).startsWith(AppConstants.WIFI_SSID) || NetworkUtil.formatSSID(mSSID1).toUpperCase().contains(AppConstants.WIFI_SSID2)
                            || NetworkUtil.formatSSID(mSSID2).startsWith(AppConstants.WIFI_SSID) || NetworkUtil.formatSSID(mSSID2).toUpperCase().contains(AppConstants.WIFI_SSID2)
                            || SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.PHONE_NUM).startsWith("170000")) {
                        //@@@@预留测试账号
                        int ip = wifiInfo.getIpAddress();
                        if (ip != 0) {
                            mIvFreeOnOff.setEnabled(false);
                            if (Globals.mHasFreeAuthority) {// 关闭内部wifi
                                mIvWhiteHalo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_fade));
                                mTvTips.setText("正在关闭免费网络");
                                mHdl.sendEmptyMessageDelayed(MSG_SEND_CLOSE_WIFI_REQUEST, 1000);
                            } else { // 打开内部wifi
                                if (mHasAdv) {
                                    // 显示全屏广告
                                    Intent adIntent = new Intent(this, FullAdvActivity.class);
                                    adIntent.putExtra("accessType", 0);
                                    adIntent.putExtra("source", "WifiActivity");
                                    startActivity(adIntent);
                                    finish();
                                } else {
                                    Message msg = new Message();
                                    msg.what = MSG_OPEN_WIFI;
                                    msg.arg1 = 0;
                                    //若没有广告,提权后要延时2秒才发送上线请求
                                    mHdl.sendMessageDelayed(msg, 2000);
                                }
                            }
                        }
                    } else {
                        if (SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.WIFI_TIP, false)) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        } else {
                            startActivity(new Intent(this, WifiTipActivity.class));
                        }
                    }
                }
                break;
            case R.id.tv_help:
                Intent helpIntent = new Intent(this, SimpleWebActivity.class);
                helpIntent.putExtra("url", AppConstants.HELP_PAGE);
                helpIntent.putExtra("title", "上网必看");
                startActivity(helpIntent);
                break;
            case R.id.tv_bulletin:
                Intent intent = new Intent();
                intent.setClass(this, TopicActivity.class);
                intent.putExtra("id", "89B57743-36F3-4B9A-9FB6-6B44801D0EC2");
                startActivity(intent);
                break;
        }
    }

    private void showAdv(AdvInfoObject advInfo, ImageView imgView) {
        try {
            int index = advInfo.getSortIndex(advInfo.getCurrentIndex());
            String imgUrl = advInfo.adUrl[index];
            if (imgView.getId() == R.id.iv_top_adv) {
                GlideApp.with(this)
                        .load(imgUrl)
                        .centerCrop()
                        .error(R.drawable.ad0002)
                        .into(imgView);
                mTopAdvUrl = advInfo.jumpUrl[index];
                mTopAdvId = advInfo.advId[index];
                StatisticsDao.saveStatistics(this, "advView", mTopAdvId); // 顶部广告显示统计
            } else if (imgView.getId() == R.id.iv_bottom_adv) {
                GlideApp.with(this)
                        .load(imgUrl)
                        .centerCrop()
                        .error(R.drawable.ad0003)
                        .into(imgView);
                mBottomAdvUrl = advInfo.jumpUrl[index];
                mBottomAdvId = advInfo.advId[index];
                StatisticsDao.saveStatistics(this, "advView", mBottomAdvId); // 底部广告显示统计
            } else {
                GlideApp.with(this)
                        .load(imgUrl)
                        .centerCrop()
                        .error(R.drawable.image_bg)
                        .into(imgView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAdvHeight(View adv) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        android.view.ViewGroup.LayoutParams params;
        if (adv == mIvTopAdv) {
            int newHeight = dm.widthPixels * 162 / 1080;
            params = new android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    newHeight);
        } else {
            int newHeight = dm.widthPixels * 360 / 1080;
            params = new android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, newHeight);
            ((RelativeLayout.LayoutParams) params).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        }
        adv.setLayoutParams(params);
    }

    public void openWifi(final int flag) {
        mIvGreenHalo.startAnimation(mScaleFadeAnim);
        mHdl.sendEmptyMessageDelayed(MSG_START_SCALE_ANIMATION, 300);

        final OpenFreeWifiRequestBody requestBody = new OpenFreeWifiRequestBody();
        requestBody.type = flag + "";

        // 调用接口
        WebServiceIf.openFreeWifi(this, requestBody, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    OpenFreeWifiResponseObject responseObj = new Gson().fromJson(response,
                            OpenFreeWifiResponseObject.class);
                    if (responseObj.header != null) {
                        if (AppConstants.RET_OK.equals(responseObj.header.ret)) {
                            SharedPreferencesInfo.setTagString(WifiActivity.this, SharedPreferencesInfo.MAC, responseObj.body.mac);
                            RequestHeader.mRequestHead.mac = responseObj.body.mac;
                            ArrayList linkList = responseObj.body.link;
                            if (linkList != null && linkList.size() > 0) {
                                mLinkTimeList = responseObj.body.linkTime;
                                mLinkTimeIterator = mLinkTimeList.iterator();
                                applyLinks(linkList);
                            }

                            Globals.g_getTime = Integer.parseInt(responseObj.body.time);
//							Globals.g_getTime = 90; // 假数据,用于测试
                            Globals.mHasFreeAuthority = true;
//							mFreeTime = Globals.g_getTime;
                            mFreeTime = Globals.g_getTime - 5; //减少5秒钟,缓冲和服务器时间的误差

                            SharedPreferencesInfo.setTagLong(WifiActivity.this, SharedPreferencesInfo.FREE_WIFI_START_TIME,
                                    System.currentTimeMillis());
                            SharedPreferencesInfo.setTagLong(WifiActivity.this, SharedPreferencesInfo.FREE_WIFI_TIME,
                                    mFreeTime * 1000);
                            mWifiUseDao.initTodayUse(DateUtil.formatTime(new Date(), "yyyy-MM-dd"));
                            startCircleAnim();
                            setUseWifiInfo();
                            setCurrentWifiInfo();

                            Intent i = new Intent();
                            i.putExtra("wifi_status", true);
                            i.setAction("action_wifi_status");
                            sendBroadcast(i);

                            bindTimerService();

                            // 连接成功后退出wifi界面
                            mHdl.sendEmptyMessageDelayed(MSG_FINISH_ACTIVITY, 1700);
                        } else {
                            ToastUtil.showToast(WifiActivity.this, "开启失败，请确保WiFi正常连接后再开启上网" + "(" + responseObj.header.errCode + ")");
                            mTvTips.setText(R.string.no_free_wifi);
                        }
                    } else {
                        ToastUtil.showToast(WifiActivity.this, R.string.request_fail_warning);
                        mTvTips.setText(R.string.no_free_wifi);
                    }
                } else {
                    ToastUtil.showToast(WifiActivity.this, R.string.request_fail_warning);
                    mTvTips.setText(R.string.no_free_wifi);
                }
                mIvGreenHalo.clearAnimation();
                mIvGreenHalo2.clearAnimation();
                mHdl.removeMessages(MSG_START_SCALE_ANIMATION);
                mIvFreeOnOff.setEnabled(true);
                mLlRenewWifiTime.setEnabled(true);
            }

            @Override
            public void onErrorResponse() {
                setUseWifiInfo();
                setCurrentWifiInfo();
                mIvGreenHalo.clearAnimation();
                mIvGreenHalo2.clearAnimation();
                mHdl.removeMessages(MSG_START_SCALE_ANIMATION);
                mIvFreeOnOff.setEnabled(true);
                mLlRenewWifiTime.setEnabled(true);
                ToastUtil.showToast(WifiActivity.this, R.string.request_fail_warning);
            }
        });
    }

    private void bindTimerService() {
        Intent intent = new Intent(this, TimerService.class);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private void setCurrentWifiInfo() {
        final WifiManager wifiManager = (WifiManager) (this.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        if (!wifiManager.isWifiEnabled()) { // 未打开wifi
            mTvTips.setText("您需要打开WiFi后才能使用此功能");
            mTvTips2.setText("连接嘿快专用热点即可开启免费上网");
            mTvTips2.setVisibility(View.VISIBLE);
            mIvFreeOnOff.setImageResource(R.drawable.btn_wifi_off_selector);
            mRlSwitcher.setBackgroundResource(R.drawable.wifi_grey_bg);
            mTvHelp.setTextColor(0xFF999999);
            Drawable helpDrawable = getResources().getDrawable(R.drawable.ic_wifi_help_green);
            helpDrawable.setBounds(0, 0, helpDrawable.getMinimumWidth(), helpDrawable.getMinimumHeight());
            mTvHelp.setCompoundDrawables(helpDrawable, null, null, null);
            mTvBulletin.setTextColor(0xFF999999);
            Drawable bulletinDrawable = getResources().getDrawable(R.drawable.ic_bulletin_green);
            bulletinDrawable.setBounds(0, 0, bulletinDrawable.getMinimumWidth(), bulletinDrawable.getMinimumHeight());
            mTvBulletin.setCompoundDrawables(bulletinDrawable, null, null, null);
            mTvTips.setTextColor(0xFF999999);
            mIvRenewWifiTime.setVisibility(View.INVISIBLE);
            mTvRenewWifiTime.setVisibility(View.INVISIBLE);
            stopCircleAnim();
        } else { // 已打开WiFi
            mSSID1 = "";
            mSSID2 = "";
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if(wifiInfo != null) {
                mSSID1 = wifiInfo.getSSID();
            }
            ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connManager != null) {
                NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (networkInfo.getExtraInfo() != null) {
                        mSSID2 = networkInfo.getExtraInfo();
                    }
                }
            }
            //@@@@预留测试账号
            if (NetworkUtil.formatSSID(mSSID1).startsWith(AppConstants.WIFI_SSID) || NetworkUtil.formatSSID(mSSID1).toUpperCase().contains(AppConstants.WIFI_SSID2)
                    || NetworkUtil.formatSSID(mSSID2).startsWith(AppConstants.WIFI_SSID) || NetworkUtil.formatSSID(mSSID2).toUpperCase().contains(AppConstants.WIFI_SSID2)
                    || SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.PHONE_NUM).startsWith("170000")) {
                // 连接的是尚WiFi
                int ip = wifiInfo.getIpAddress();
                if (ip != 0) {
                    Globals.mIsConnectFreeWifi = true;
                    mTvTips2.setVisibility(View.GONE);
                    if (Globals.mHasFreeAuthority) {
                        mIvFreeOnOff.setImageResource(R.drawable.btn_wifi_on_selector);
                        mRlSwitcher.setBackgroundResource(R.drawable.wifi_green_bg);
                        mTvHelp.setTextColor(0xFFFFFFFF);
                        Drawable helpDrawable = getResources().getDrawable(R.drawable.ic_wifi_help_white);
                        helpDrawable.setBounds(0, 0, helpDrawable.getMinimumWidth(), helpDrawable.getMinimumHeight());
                        mTvHelp.setCompoundDrawables(helpDrawable, null, null, null);
                        mTvBulletin.setTextColor(0xFFFFFFFF);
                        Drawable bulletinDrawable = getResources().getDrawable(R.drawable.ic_bulletin_white);
                        bulletinDrawable.setBounds(0, 0, bulletinDrawable.getMinimumWidth(), bulletinDrawable.getMinimumHeight());
                        mTvBulletin.setCompoundDrawables(bulletinDrawable, null, null, null);
                        mTvTips.setTextColor(0xFFFFFFFF);
                        mIvRenewWifiTime.setVisibility(View.VISIBLE);
                        mTvRenewWifiTime.setVisibility(View.VISIBLE);
                        startCircleAnim();
                    } else {
                        mRlSwitcher.setBackgroundResource(R.drawable.wifi_grey_bg);
                        mTvHelp.setTextColor(0xFF999999);
                        Drawable helpDrawable = getResources().getDrawable(R.drawable.ic_wifi_help_green);
                        helpDrawable.setBounds(0, 0, helpDrawable.getMinimumWidth(), helpDrawable.getMinimumHeight());
                        mTvHelp.setCompoundDrawables(helpDrawable, null, null, null);
                        mTvBulletin.setTextColor(0xFF999999);
                        Drawable bulletinDrawable = getResources().getDrawable(R.drawable.ic_bulletin_green);
                        bulletinDrawable.setBounds(0, 0, bulletinDrawable.getMinimumWidth(), bulletinDrawable.getMinimumHeight());
                        mTvBulletin.setCompoundDrawables(bulletinDrawable, null, null, null);
                        mTvTips.setTextColor(0xFF999999);
                        mIvRenewWifiTime.setVisibility(View.INVISIBLE);
                        mTvRenewWifiTime.setVisibility(View.INVISIBLE);
                        stopCircleAnim();
                    }
                } else {
                    mHdl.sendEmptyMessageDelayed(MSG_SET_CURRENT_WIFI_INFO, 1000);
                }
            } else { // 连接的不是尚WiFi或是没有连接wifi
                Globals.mIsConnectFreeWifi = false;
                AndPermission.with(this)
                        .requestCode(100)
                        .permission(Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                        .callback(new PermissionListener() {
                            @Override
                            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                if (requestCode == 100) {
                                    try {
                                        scanWifi(wifiManager);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ToastUtil.showToast(WifiActivity.this, "扫描Wifi需要位置权限，请在权限设置中打开");
                                    }
                                }
                            }

                            @Override
                            public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                                ToastUtil.showToast(WifiActivity.this, "扫描Wifi需要位置权限，请在权限设置中打开");
                            }
                        })
                        .start();
            }
        }
    }

    private void scanWifi(WifiManager wifiManager) {
        boolean hasFreeWiFi = false; // 判断附近是否有轨道免费WiFi
        List<ScanResult> scanList = wifiManager.getScanResults();
        for (ScanResult scan : scanList) {
            if (!TextUtils.isEmpty(scan.SSID)) {
                if (NetworkUtil.formatSSID(scan.SSID).startsWith(AppConstants.WIFI_SSID) || NetworkUtil.formatSSID(scan.SSID).toUpperCase().contains(AppConstants.WIFI_SSID2)) {
                    hasFreeWiFi = true;
                    break;
                }
            }
        }

        mTvTips2.setVisibility(View.VISIBLE);
        mTvTips2.setText("连接嘿快专用热点即可开启免费上网");
//        mTvTips2.setText("当前连接:\n" + "ssid1=" + mSSID1 + "\n" + "ssid2=" + mSSID2);
        mTvTips.setText(hasFreeWiFi ? "附近有可用的轨道WIFI" : "附近没有可用的轨道WIFI");
        mIvFreeOnOff.setImageResource(R.drawable.btn_wifi_off_selector);
        mRlSwitcher.setBackgroundResource(R.drawable.wifi_grey_bg);
        mTvHelp.setTextColor(0xFF999999);
        Drawable helpDrawable = getResources().getDrawable(R.drawable.ic_wifi_help_green);
        helpDrawable.setBounds(0, 0, helpDrawable.getMinimumWidth(), helpDrawable.getMinimumHeight());
        mTvHelp.setCompoundDrawables(helpDrawable, null, null, null);
        mTvBulletin.setTextColor(0xFF999999);
        Drawable bulletinDrawable = getResources().getDrawable(R.drawable.ic_bulletin_green);
        bulletinDrawable.setBounds(0, 0, bulletinDrawable.getMinimumWidth(), bulletinDrawable.getMinimumHeight());
        mTvBulletin.setCompoundDrawables(bulletinDrawable, null, null, null);
        mTvTips.setTextColor(0xFF999999);
        mIvRenewWifiTime.setVisibility(View.INVISIBLE);
        mTvRenewWifiTime.setVisibility(View.INVISIBLE);
        stopCircleAnim();
    }

    private void setUseWifiInfo() {
        int todayUse = 0;
        Map<String, Integer> map = mWifiUseDao.getLog(DateUtil.formatTime(new Date(), "yyyy-MM-dd"));
        if (map != null && map.get("todayUse") != null) {
            todayUse = map.get("todayUse");
            if (todayUse < 0) {
                todayUse = 0;
            }
        }
        mTvSurplusTime.setText(String.valueOf((mFreeTime + 60 - 1) / 60)); // 今日剩余
        mTvTodayUsedTime.setText(String.valueOf((todayUse / 60))); // 今日累计使用

        if (!Globals.mHasFreeAuthority) {
            mIvFreeOnOff.setImageResource(R.drawable.btn_wifi_off_selector);
            mTvTips.setText(R.string.no_free_wifi);
            mIvRenewWifiTime.setBackgroundResource(R.drawable.clock_off);
            mIvRenewWifiTime.clearAnimation();
            mTvRenewWifiTime.setTextColor(0xFF7bcc87);
            mCanRenew = false;
        } else if (mFreeTime <= AppConstants.NEAR_CLOSE_FREE_WIFI_TIME) {
            mTvTips.setText("您现在可以续时了");
            mIvRenewWifiTime.setBackgroundResource(R.drawable.clock_on);
            if (mIvRenewWifiTime == null || mIvRenewWifiTime.getAnimation() == null || !mIvRenewWifiTime.getAnimation().hasStarted()) {
                mIvRenewWifiTime.startAnimation(mShakeAnim);
            }
            mTvRenewWifiTime.setTextColor(0xFFFFFFFF);
            mCanRenew = true;
            mIvFreeOnOff.setImageResource(R.drawable.btn_wifi_on_selector);
        } else {
            String txtInfo = "WiFi已成功连接，%s分钟后可续时";
            mTvTips.setText(String.format(txtInfo, (mFreeTime + 60 - 1) / 60 - AppConstants.NEAR_CLOSE_FREE_WIFI_TIME
                    / 60));
            mIvRenewWifiTime.setBackgroundResource(R.drawable.clock_off);
            mIvRenewWifiTime.clearAnimation();
            mTvRenewWifiTime.setTextColor(0xFF7bcc87);
            mCanRenew = false;
            mIvFreeOnOff.setImageResource(R.drawable.btn_wifi_on_selector);
        }
    }

    public void sendCloseWiFiRequest() {
        CloseFreeWifiRequestBody requestBody = new CloseFreeWifiRequestBody();
        WebServiceIf.closeFreeWifi(this, requestBody, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                mHdl.sendEmptyMessageDelayed(MSG_CLOSE_WIFI, 1000);
            }

            @Override
            public void onErrorResponse() {
                mHdl.sendEmptyMessageDelayed(MSG_CLOSE_WIFI, 1000);
            }
        });
    }

    public void startCircleAnim() {
        if (mIvCircle.getAnimation() == null) {
            Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
            mIvCircle.startAnimation(rotateAnimation);
            mIvCircle.setVisibility(View.VISIBLE);
        }
    }

    public void stopCircleAnim() {
        mIvCircle.clearAnimation();
        mIvCircle.setVisibility(View.INVISIBLE);
    }

    /**
     * 查询当日使用时长
     */
    private void getTodayWifiUseTime() {
        // 调用接口
        WebServiceIf.getTodayWifiUseTime(this, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    TodayWifiTotalUseTimeResponseObject responseObj = new Gson().fromJson(response, TodayWifiTotalUseTimeResponseObject.class);
                    if (responseObj.header != null) {
                        if (AppConstants.RET_OK.equals(responseObj.header.ret)) {
                            String totalTime = responseObj.body.totalTime;
                            if (!TextUtils.isEmpty(totalTime)) {
                                int todayUse = Integer.parseInt(totalTime);
                                mWifiUseDao.updateTodayUse(DateUtil.formatTime(new Date(), "yyyy-MM-dd"), todayUse * 60);
                                setUseWifiInfo();
                                setCurrentWifiInfo();
                            }
                        } else if (!TextUtils.isEmpty(responseObj.header.errMsg)) {
                            ToastUtil.showToast(WifiActivity.this, responseObj.header.errMsg);
                        } else {
                            ToastUtil.showToast(WifiActivity.this, "暂时无法查询当日使用时长");
                        }
                    } else {
                        ToastUtil.showToast(WifiActivity.this, "暂时无法查询当日使用时长");
                    }
                } else {
                    ToastUtil.showToast(WifiActivity.this, "暂时无法查询当日使用时长");
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(WifiActivity.this, "暂时无法查询当日使用时长");
            }
        });
    }

    /**
     * 访问后台返回的链接(用于运营)
     */
    private void applyLinks(ArrayList<String> linkList) {
        mLinkIterator = linkList.iterator();
        Message msg = new Message();
        msg.what = MSG_LOAD_WEB;
        msg.obj = mLinkIterator.next();
        mHdl.sendMessage(msg);
    }

    @Override
    public void onTick(int minute) {
        mFreeTime = minute;
        setUseWifiInfo();
        setCurrentWifiInfo();
    }

    @Override
    public void onFinish() {
        mFreeTime = 0;
        Globals.mHasFreeAuthority = false;
        setUseWifiInfo();
        setCurrentWifiInfo();
        stopCircleAnim();
        Globals.g_isUpdateUserGroup = false;
    }
}

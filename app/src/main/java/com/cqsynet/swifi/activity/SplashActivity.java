/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：尚WIFI欢迎页面Activity。
 *
 *
 * 创建标识：zhaosy 20150317
 */
package com.cqsynet.swifi.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.AppManager;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.BlackWhiteUrlDao;
import com.cqsynet.swifi.db.LaunchImageDao;
import com.cqsynet.swifi.db.NewsCacheDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.CheckVersionResponseObject;
import com.cqsynet.swifi.model.LastVerInfo;
import com.cqsynet.swifi.model.LaunchImageObject;
import com.cqsynet.swifi.model.LaunchImgResponseObject;
import com.cqsynet.swifi.model.LoginRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.UpdateUserGroupResponseObject;
import com.cqsynet.swifi.model.UrlRuleResponseObject;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.UserInfoResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.AdvDataHelper;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.CrashUtil;
import com.cqsynet.swifi.util.LogUtil;
import com.cqsynet.swifi.util.LogoutUtil;
import com.cqsynet.swifi.util.NetworkUtil;
import com.cqsynet.swifi.util.PermissionUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.util.List;

public class SplashActivity extends Activity {

    private static final int MSG_TO_PAGE = 0x0001;
    private static final int MSG_START_REQUEST = 0x0002;
    private static final int MSG_INIT = 0x0003;
    private static final int MSG_LOGIN = 0x0004;
    private static final int LOGIN_MIN_DURATION = 3500; //整个登录流程的最短持续时间
    private static final int IMAGE_MIN_DURATION = 3000; //splash图片显示出来的最短时间
    //常量字符串数组，将需要申请的权限写进去，同时必须要在Androidmanifest.xml中声明。
    private static String[] permissions = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    private LaunchImageDao mLaunchImageDao;
    private boolean mIsCheckVerFinished = false;
    private boolean mIsNotInSplashDetail = true; //是否没跳转到启动图详情页
    private boolean mIsLogin = false; //是否已登录
    private WifiManager mWifiManager;
    private BroadcastReceiver mBroadcastReceiver;
    private long mStartAppTime; //打开app的时间
    private long mStartSplashTime; //开始显示splash图片的时间
    private boolean mIsSplashShown = false; // splash图是否已显示
    private ImageView mIvSplash;

    private Handler mHdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TO_PAGE:
                    //判断是否从启动页广告的详情中返回
                    if (mIsNotInSplashDetail && mIsLogin) {
                        mIvSplash.setOnClickListener(null);
                        toPage();
                    } else {
                        mHdl.sendEmptyMessageDelayed(MSG_TO_PAGE, 200);
                    }
                    break;
                case MSG_INIT: //提前,获取广告,获取启动图,获取网址黑白名单等操作
                    //发起修改用户组到00组的请求
                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                    if (NetworkUtil.isConnectFashionWiFi(wifiInfo)) {
                        Globals.mIsConnectFreeWifi = true;
                        if (!Globals.g_isUpdateUserGroup) {
                            updateUserGroup(SplashActivity.this);
                        }
                    }
                    //注册wifi监听,在连上heikuai后,若没提权,则发起修改用户组到00组的请求
                    mBroadcastReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                                if (info != null && info.getState().equals(NetworkInfo.State.CONNECTED)) {
                                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                                    if (wifiInfo != null && (NetworkUtil.formatSSID(wifiInfo.getSSID()).startsWith(AppConstants.WIFI_SSID))) {
                                        Globals.mIsConnectFreeWifi = true;
                                        if (!Globals.g_isUpdateUserGroup) {
                                            updateUserGroup(SplashActivity.this);
                                        }
                                    }
                                }
                            }
                        }
                    };
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                    registerReceiver(mBroadcastReceiver, filter);

                    getUrlRule(); // 获取网址黑白名单规则
                    getLaunchImg(); // 获取启动图
                    new AdvDataHelper(SplashActivity.this, null).loadAdvData(); // 获取广告位数据

                    if (SharedPreferencesInfo.getTagInt(SplashActivity.this, SharedPreferencesInfo.IS_LOGIIN) == 1) { //app之前已登录过,有登录信息
                        loginRequest(); //登录
                        mHdl.sendEmptyMessage(MSG_TO_PAGE);
                    } else { //app没有登录过
                        mIvSplash.setOnClickListener(null);
                        toPage();
                    }
                    break;
                case MSG_START_REQUEST:
                    checkVersion(); // 检查版本更新
                    new Thread() {
                        public void run() {
                            while (!mIsCheckVerFinished) {
                                try {
                                    sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            mHdl.sendEmptyMessage(MSG_INIT);
                        }
                    }.start();
                    break;
                case MSG_LOGIN:
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    break;
//			case MSG_CONNECT_WIFI:
//				connectWifi();
//				break;
//            case MSG_UPDATE_USER_GROUP:
//                updateUserGroup();
//                break;
            }
        }
    };

    /**
     * 更新用户分组(提权到00组)
     */
    public static void updateUserGroup(Context context) {
        WebServiceIf.updateUserGroup(context, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    Gson gson = new Gson();
                    UpdateUserGroupResponseObject responseObj = gson.fromJson(response, UpdateUserGroupResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            Globals.g_isUpdateUserGroup = true;
                        } else {
                            LogUtil.writeToFile("提权接口异常\n" + responseObj.header.errMsg + "(" + responseObj.header.errCode + ")");
                            if (Globals.DEBUG) {
                                System.out.println("提权到00组失败: " + header.errMsg);
                            }
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //以下代码防止第一次安装后点击图标每次都会重新打开app
        if (!isTaskRoot() || (getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        CrashUtil.getInstance().init(getApplicationContext());
        ((AppManager)getApplication()).mLocationService.start(); //启动定位服务
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mStartAppTime = System.currentTimeMillis();

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);

        setContentView(R.layout.activity_splash);

        mLaunchImageDao = new LaunchImageDao(this);
        // 创建缓存文件夹
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }

        //初始化引导图配置
        if (SharedPreferencesInfo.getTagInt(this, SharedPreferencesInfo.VERSION) < AppUtil.getVersionCode(this)) {
            SharedPreferencesInfo.setTagInt(this, SharedPreferencesInfo.MAIN_GUIDE, 0);
            SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEWS_GUIDE, false);
            SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.WIFI_GUIDE, false);
            SharedPreferencesInfo.setTagInt(this, SharedPreferencesInfo.VERSION, AppUtil.getVersionCode(this));
            //清空资讯缓存
            NewsCacheDao.getInstance(this.getApplicationContext()).clearCache();
        }

        //初始化城市
        if (TextUtils.isEmpty(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.CITY_CODE))) {
            SharedPreferencesInfo.setTagString(this, SharedPreferencesInfo.CITY_CODE, "132"); //默认为重庆
        }

        //设置splash图片
        mIvSplash = findViewById(R.id.ivAdv_splash);
//        setImageHeight(mIvSplash);
        setSplash();
        //开始splash动画
        doAnimation();

        //验证app的手机权限
        boolean result = PermissionUtil.checkAndRequestPermissions(this, permissions);
        if (!result) {
            mHdl.sendEmptyMessageDelayed(MSG_START_REQUEST, 2000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            //启动图详情返回
            if (resultCode == RESULT_OK) {
                mIsNotInSplashDetail = true;
            }
        } else if (requestCode == 100) {
            //权限设置返回
            boolean result = PermissionUtil.checkAndRequestPermissions(this, permissions);
            if (!result) {
                mHdl.sendEmptyMessageDelayed(MSG_START_REQUEST, 2000);
            }
        }
    }

    /**
     * @Description: 显示启动图片
     */
    private void setSplash() {
        final LaunchImageObject launchImg = mLaunchImageDao.getRandomImage();
        if (launchImg != null) {
            GlideApp.with(this)
                    .load(launchImg.url)
                    .centerInside()
                    .error(R.color.transparent)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            mIsSplashShown = true;
                            mStartSplashTime = System.currentTimeMillis();
                            return false;
                        }
                    })
                    .into(mIvSplash);
            StatisticsDao.saveStatistics(SplashActivity.this, "advView", launchImg.advId); // 启动图广告浏览统计

            if (!TextUtils.isEmpty(launchImg.jumpUrl)) {
                mIvSplash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //如果连上了heikuai,提权没有成功,则不能点击跳转到外链
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        if (NetworkUtil.isConnectFashionWiFi(wifiInfo)) { //连着heikuai
                            if (!Globals.g_isUpdateUserGroup) {  //没有提权成功
                                if (!launchImg.jumpUrl.toLowerCase().contains("heikuai.com") && !launchImg.jumpUrl.toLowerCase().contains("cqsynet.com")) { //外网链接
                                    return; //不能点击跳转
                                }
                            }
                        }
                        //没有用户信息是不能点击
                        if (TextUtils.isEmpty(SharedPreferencesInfo.getTagString(SplashActivity.this, SharedPreferencesInfo.ACCOUNT))) {
                            return;
                        }
                        mIsNotInSplashDetail = false;
                        StatisticsDao.saveStatistics(SplashActivity.this, "advClick", launchImg.advId); // 启动图广告点击统计
                        Intent webIntent = new Intent();
                        webIntent.putExtra("url", launchImg.jumpUrl);
                        webIntent.putExtra("type", "0");
                        webIntent.putExtra("source", "广告");
                        webIntent.putExtra("from", "adv");
                        WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                        webDispatcher.dispatch(webIntent, SplashActivity.this, 101);
                    }
                });
            }
        }
    }

    /**
     * 检查版本
     */
    private void checkVersion() {
        WebServiceIf.getVersionInfo(this, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    Gson gson = new Gson();
                    CheckVersionResponseObject responseObj = gson.fromJson(response, CheckVersionResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null && AppConstants.RET_OK.equals(header.ret)) {
                        if (responseObj.body != null && responseObj.body.verInfo != null) {
                            try {
                                showUpdateVerionDialogLogic(SplashActivity.this, responseObj.body.verInfo);
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                mIsCheckVerFinished = true;
            }

            @Override
            public void onErrorResponse() {
                mIsCheckVerFinished = true;
            }
        });
    }

    /**
     * 显示版本更新对话框逻辑
     *
     * @param lastVerInfo 服务端最新版本
     */
    public void showUpdateVerionDialogLogic(final Context context, final LastVerInfo lastVerInfo) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
            int currentVer = pi.versionCode;
            if (currentVer < lastVerInfo.forceMiniVer || currentVer < lastVerInfo.verCode) {
                if (currentVer < lastVerInfo.forceMiniVer) { // 强制更新
                    showUpdateDialog(true, lastVerInfo);
                } else { // 选择更新
                    if (lastVerInfo.verCode != SharedPreferencesInfo.getTagInt(context,
                            SharedPreferencesInfo.IGNORE_VERSION)) {
                        showUpdateDialog(false, lastVerInfo);
                    } else {
                        mIsCheckVerFinished = true;
                    }
                }
            } else {
                mIsCheckVerFinished = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mIsCheckVerFinished = true;
        }
    }

    /**
     * 显示更新提示框
     *
     * @param isForceUpdate 是否强制升级
     * @param lastVerInfo   版本信息
     */
    private void showUpdateDialog(boolean isForceUpdate, final LastVerInfo lastVerInfo) {
        final CustomDialog dialog = new CustomDialog(SplashActivity.this, R.style.round_corner_dialog, R.layout.dialog_update);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (AppUtil.getScreenW(this)) - 80; // 设置宽度
        lp.height = lp.width * 2 / 3;
        dialog.getWindow().setAttributes(lp);
        dialog.setCancelable(false);
        View view = dialog.getCustomView();
        TextView tvTitle = view.findViewById(R.id.tvTitle_dialog_update);
        TextView tvContent = view.findViewById(R.id.tvContent_dialog_update);
        final CheckBox cb = view.findViewById(R.id.cbRemind_dialog_update);
        Button btnCancel = view.findViewById(R.id.btnCancel_dialog_update);
        Button btnOk = view.findViewById(R.id.btnOk_dialog_update);

        tvTitle.setText("版本更新 " + lastVerInfo.verName);
        tvContent.setText(lastVerInfo.des);
        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, UpdateSoftActivity.class);
                intent.putExtra("softAddress", lastVerInfo.downloadUrl);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        if (isForceUpdate) {
            cb.setVisibility(View.GONE);
            btnCancel.setText("退出程序");
            btnCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    SplashActivity.this.finish();
                }
            });
        } else {
            cb.setVisibility(View.VISIBLE);
            btnCancel.setText("下次再说");
            btnCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点此说明用户没有更新到最新版本
                    SharedPreferencesInfo.setTagBoolean(SplashActivity.this, SharedPreferencesInfo.NEW_VERSION, true);
                    SharedPreferencesInfo.setTagBoolean(SplashActivity.this, SharedPreferencesInfo.NEW_SETTING, true);
                    Intent intent = new Intent();
                    intent.setAction(AppConstants.ACTION_REFRESH_RED_POINT);
                    SplashActivity.this.sendBroadcast(intent); // 发送广播提示新消息到达

                    if (cb.isChecked()) {
                        SharedPreferencesInfo.setTagInt(SplashActivity.this, SharedPreferencesInfo.IGNORE_VERSION,
                                lastVerInfo.verCode);
                    }
                    mIsCheckVerFinished = true;
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    /**
     * 设置启动图渐变动画效果
     */
    private void doAnimation() {
        AnimationSet animation = new AnimationSet(true);
        AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
        alpha.setInterpolator(AnimationUtils.loadInterpolator(SplashActivity.this, android.R.anim.decelerate_interpolator));
        alpha.setDuration(1500);
        animation.addAnimation(alpha);
        mIvSplash.startAnimation(animation);
    }

    /**
     * 跳转页面
     */
    private void toPage() {
        if (SharedPreferencesInfo.getTagInt(getApplicationContext(), SharedPreferencesInfo.MAIN_GUIDE) == 0) { // 未展示过引导页
            Intent guideIntent = new Intent(SplashActivity.this, UserGuideActivity.class);
            guideIntent.putExtra("from", "splash");
            startActivity(guideIntent);
            finish();
        } else if (SharedPreferencesInfo.getTagInt(this, SharedPreferencesInfo.IS_LOGIIN) == 0) { // 未登陆
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        } else {
            //进入app主界面
            new Thread() {
                public void run() {
                    while(true) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - mStartAppTime > LOGIN_MIN_DURATION) {
                            if (!mIsSplashShown || currentTime - mStartSplashTime > IMAGE_MIN_DURATION) {
                                break;
                            }
                        }
                        try {
                            sleep(200);
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mHdl.sendEmptyMessage(MSG_LOGIN);
                }
            }.start();
        }
    }

    /**
     * 调用登陆接口发起登陆，并处理服务器返回信息
     */
    private void loginRequest() {
        final LoginRequestBody loginRequestBody = new LoginRequestBody();
        loginRequestBody.phoneNo = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.PHONE_NUM);
        loginRequestBody.password = "";
        loginRequestBody.rsaPubKey = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.RSA_KEY);
        IResponseCallback loginCallbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    UserInfoResponseObject responseObj = gson.fromJson(response, UserInfoResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            try {
                                UserInfo body = responseObj.body;
                                if (!TextUtils.isEmpty(body.userAccount) && !TextUtils.isEmpty(body.rsaPubKey)) {
                                    SharedPreferencesInfo.setTagString(SplashActivity.this, SharedPreferencesInfo.RSA_KEY, body.rsaPubKey);
                                    SharedPreferencesInfo.setTagString(SplashActivity.this, SharedPreferencesInfo.ACCOUNT, body.userAccount);
                                }
                                Globals.g_userInfo = body;
                                SharedPreferencesInfo.setTagString(SplashActivity.this, SharedPreferencesInfo.USER_INFO, gson.toJson(body));
                                Globals.g_tempPriSign = ""; //清空签名,重新生成
                            } catch (ClassCastException e) {
                                ToastUtil.showToast(SplashActivity.this, R.string.login_fail);
                            }
                        } else if (header.errCode.equals("02064")) {
                            // 账号在其它地方登陆,弹出登陆界面
                            LogoutUtil.cleanLoginInfo(SplashActivity.this);
                            ToastUtil.showToast(SplashActivity.this, header.errMsg);
                            Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(loginIntent);
                            finish();
                            return;
                        }
                    }
                }
                mIsLogin = true;
            }

            @Override
            public void onErrorResponse() {
                mIsLogin = true;
            }
        };
        // 调用接口发起登陆
        WebServiceIf.login(this, loginRequestBody, loginCallbackIf);
    }

    /**
     * 获取放通网址的黑白名单
     */
    private void getUrlRule() {
        // 调用接口
        WebServiceIf.getUrlRule(this, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    Gson gson = new Gson();
                    UrlRuleResponseObject responseObj = gson.fromJson(response, UrlRuleResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            try {
                                if (responseObj.body != null) {
                                    Globals.g_whiteList = responseObj.body.whiteList;
                                    Globals.g_blackList = responseObj.body.blackList;
                                    // 更新数据库数据
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            BlackWhiteUrlDao.clear(SplashActivity.this);
                                            BlackWhiteUrlDao.saveWhiteList(SplashActivity.this, Globals.g_whiteList);
                                            BlackWhiteUrlDao.saveBlackList(SplashActivity.this, Globals.g_blackList);
                                        }
                                    }).start();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (Globals.DEBUG) {
                                System.out.println("获取网址黑白名单失败");
                            }
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                if (Globals.DEBUG) {
                    System.out.println("获取网址黑白名单失败");
                }
            }
        });
    }

    /**
     * 从服务器回去启动图片
     */
    private void getLaunchImg() {
        // 调用接口
        WebServiceIf.getLaunchImg(this, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    Gson gson = new Gson();
                    LaunchImgResponseObject responseObj = gson.fromJson(response, LaunchImgResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            try {
                                if (responseObj.body != null && responseObj.body.adList != null) {
                                    // 这里需要单独实例化dao，因为回调可能是在当前页面已经销毁掉之后
                                    LaunchImageDao dao = new LaunchImageDao(AppManager.getInstance());
                                    dao.clear();
                                    dao.save(responseObj.body.adList);
                                    dao.closeDB();

                                    if(!mIsSplashShown) {
                                        setSplash();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (Globals.DEBUG) {
                                    System.out.println("获取启动图片失败：" + e.getMessage());
                                }
                            }
                        } else {
                            if (Globals.DEBUG) {
                                System.out.println("获取启动图片失败");
                            }
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                if (Globals.DEBUG) {
                    System.out.println("获取启动图片失败");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHdl.removeMessages(MSG_START_REQUEST);
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                final CustomDialog dialog = new CustomDialog(SplashActivity.this, R.style.round_corner_dialog, R.layout.dialog_permission);
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.width = AppUtil.getScreenW(this) - 80; // 设置宽度
                lp.height = lp.width * 2 / 3;
                dialog.getWindow().setAttributes(lp);
                dialog.setCancelable(false);
                View view = dialog.getCustomView();
                TextView tvTitle = view.findViewById(R.id.tvTitle_dialog_permission);
                TextView tvContent = view.findViewById(R.id.tvContent_dialog_permission);
                Button btnCancel = view.findViewById(R.id.btnCancel_dialog_permission);
                Button btnOk = view.findViewById(R.id.btnOk_dialog_permission);

                tvTitle.setText("权限提示");
                tvContent.setText("您需要允许权限才可以正常使用嘿快的全部功能");
                btnOk.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 进入App设置页面
                        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                        intent.putExtra("extra_pkgname", SplashActivity.this.getPackageName());
                        if (Build.MANUFACTURER.equals("Xiaomi") && isIntentAvailable(SplashActivity.this, intent)) { // 小米系统跳转
                            startActivityForResult(intent, 100);
                        } else { // 其他系统跳转
                            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", SplashActivity.this.getPackageName(), null);
                            intent.setData(uri);
                            SplashActivity.this.startActivityForResult(intent, 100);
                        }
                        dialog.dismiss();
                    }
                });
                btnCancel.setText("退出");
                btnCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        SplashActivity.this.finish();
                    }
                });
                dialog.show();

                return;
            }
        }
        mHdl.sendEmptyMessageDelayed(MSG_START_REQUEST, 2000);
    }

    /**
     * 检查是否有intent对应的activity
     */
    private boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * 设置启动图图高度
     */
    private void setImageHeight(View view) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int newHeight = screenWidth * 3 / 2;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, newHeight);
        view.setLayoutParams(params);
    }
}
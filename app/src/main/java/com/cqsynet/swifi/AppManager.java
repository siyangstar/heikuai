/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：app管理器
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.cqsynet.swifi.activity.SplashAdvActivity;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.service.LocationService;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.mob.MobSDK;
import com.youzan.sdk.YouzanSDK;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class AppManager extends Application {

    private HashMap<String, Activity> mActivityMap = new HashMap<String, Activity>();
    private static AppManager instance;
    private int mActivityCount = 0;
    public LocationService mLocationService;

    @Override
    public void onCreate() {
        super.onCreate();
//        if(Globals.DEBUG) {
//            Bugtags.start("d47ecd69081bbf35932270350b16f5cc", this, Bugtags.BTGInvocationEventBubble);
//        } else {
//            Bugtags.start("d47ecd69081bbf35932270350b16f5cc", this, Bugtags.BTGInvocationEventNone);
//        }
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {
                SharedPreferencesInfo.setTagLong(getApplicationContext(), SharedPreferencesInfo.APP_USE_TIME, System
                        .currentTimeMillis());
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (activity.getLocalClassName().equals("activity.SplashActivity")) {
                    StatisticsDao.saveStatistics(getApplicationContext(), "appUse", "");
                } else {
                    //从后台回到前台时,间隔35超过分钟,算增加1次appUse
                    long lastUseTime = SharedPreferencesInfo.getTagLong(getApplicationContext(),
                            SharedPreferencesInfo.APP_USE_TIME);
                    if (System.currentTimeMillis() - lastUseTime > AppConstants.APP_USE_INTERVAL) {
                        StatisticsDao.saveStatistics(getApplicationContext(), "appUse", "");
                        Intent intent = new Intent(activity, SplashAdvActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActivityCount++;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityCount--;
                if (mActivityCount == 0) {
                    SharedPreferencesInfo.setTagLong(getApplicationContext(), SharedPreferencesInfo.APP_USE_TIME,
                            System.currentTimeMillis());
                }
            }

        });

        instance = this;
        YouzanSDK.init(this, AppConstants.YOUZAN_CLIENT_ID);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(25000, TimeUnit.MILLISECONDS); //全局的读取超时时间
        builder.writeTimeout(25000, TimeUnit.MILLISECONDS); //全局的写入超时时间
        builder.connectTimeout(25000, TimeUnit.MILLISECONDS); //全局的连接超时时间
        //配置cookie 3选1
//        builder.cookieJar(new CookieJarImpl(new SPCookieStore(this))); //使用sp保持cookie，如果cookie不过期，则一直有效
//        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this))); //使用数据库保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new MemoryCookieStore())); //使用内存保持cookie，app退出后，cookie消失
//        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
//        HttpHeaders headers = new HttpHeaders();
//        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
//        headers.put("commonHeaderKey2", "commonHeaderValue2");
//        HttpParams params = new HttpParams();
//        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
//        params.put("commonParamsKey2", "这里支持中文参数");
        OkGo.getInstance().init(this)                       //必须调用初始化
                .setOkHttpClient(builder.build())               //设置OkHttpClient
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(0);//全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
//                .addCommonHeaders(headers)                      //全局公共头
//                .addCommonParams(params);                       //全局公共参数


        //百度定位
        mLocationService = new LocationService(getApplicationContext());

        MobSDK.init(this);
    }

    /**
     * 单例模式中获取唯一的ExitApplication实例
     *
     * @return
     */
    public static AppManager getInstance() {
        return instance;
    }

    /**
     * 添加Activity到容器中
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        mActivityMap.put(activity.getClass().getSimpleName(), activity);
    }

    /**
     * 从容器中删除activity
     *
     * @param activity
     */
    public void removeActivity(Activity activity) {
        mActivityMap.remove(activity.getClass().getSimpleName());
    }

    /**
     * 遍历所有Activity并finish
     */
    public void exit() {
        Iterator<Activity> it = mActivityMap.values().iterator();
        while (it.hasNext()) {
            it.next().finish();
        }
        System.exit(0);
    }

    /**
     * 清空activity容器
     */
    public void clearActivityMap() {
        mActivityMap.clear();
    }

    /**
     * 获取activity容器
     *
     * @return
     */
    public HashMap<String, Activity> getActivityMap() {
        return mActivityMap;
    }

}

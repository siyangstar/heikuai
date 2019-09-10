/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：sharinfo工具类。
 *
 *
 * 创建标识：luchaowei 20141020
 */
package com.cqsynet.swifi.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.cqsynet.swifi.model.ChannelInfo;
import com.cqsynet.swifi.model.ChannelListResponseBody;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Set;

public class SharedPreferencesInfo {
    public static final String SWIFI_PREFERENCES = "swifi_data"; // shareinfo 名称
    public static final String VERSION = "version"; // 上次退出时的版本号,用于判读是否新升级后的第一次打开app
    public static final String MAIN_GUIDE = "activate"; // 程序是否第一次运行，未激活。需要显示APP的用户引导页，
    public static final String IS_LOGIIN = "login"; // 是否有成功登陆过。
    public static final String NEWS_GUIDE = "news_guide"; // 是否第一次进入新闻列表页面，如果是第一次，需要显示引导浮层。
    public static final String WIFI_GUIDE = "wifi_guide"; // 是否第一次进入wifi管理页面。如果是第一次，需要显示引导浮层。
    public static final String WIFI_TIP = "wifi_tip"; // 是否需要引导用户进行wifi连接
    public static final String BOTTLE_GUIDE = "bottle_guide";	//是否第一次进入漂流瓶页面.如果第一次,需要显示引导浮层.
    public static final String SHAKE_GUIDE = "shake_guide"; //是否显示摇一摇引导提示
    public static final String ACCOUNT = "swifi_account"; // 登陆成功后服务器返回的账户名
    public static final String PHONE_NUM = "swifi_phone_num"; // 登陆成功的手机号
//    public static final String USER_PSW = "swifi_psw"; // 登陆成功的MD5加密密码
    public static final String RSA_KEY = "public_rsa_key"; // 登陆成功服务器返回的rsa公钥
    public static final String CHANNELS = "channel_info"; // 新闻频道列表的Json 字符串
    public static final String SAVE_CHANNEL_FAIL = "save_channel_fail"; // 保存频道列表到服务器失败状态
    public static final String GET_CHANNEL_FAIL = "get_channel_fail"; // 从服务器获取频道列表失败状态
    public static final String IGNORE_VERSION = "ignoreVersion"; // 忽略升级的版本号
    public static final String SHOP_DB_USER_ID = "shop_user_id";
    public static final String IS_PUSHMESSAGE_RECEIVE = "is_pushmessage_receive";//是否接收推送消息
    public static final String READED = "readed"; //已读内容标识
    public static final String COLLECT_LIST_NEWS = "collect_list_news";
    public static final String SUGGEST = "suggest";//意见反馈，一段文字三张图，","隔开
    public static final String FREE_WIFI_START_TIME = "free_wifi_start_time";//最后一次点击我要上网或者续时的系统时间
    public static final String FREE_WIFI_TIME = "free_wifi_time";//最后一次点击我要上网或者续时后,还剩余的可用上网时间
    public static final String SHOP_AUTH = "shop_auth";//判断是否可访问电商,三个参数：ret&time&user
    public static final String NEW_COMMENT_REPLY = "new_comment_reply"; //发现中"评论"是否有红点提示
    public static final String COMMENT_REPLY_COUNT = "comment_reply_count"; //评论回复的新消息条数
    public static final String NEW_SETTING = "new_setting"; //发现中"设置"是否有小红点
    public static final String NEW_VERSION = "new_version"; //应用更新是否有小红点
    public static final String NEW_SUGGEST = "new_suggest"; //设置中"意见反馈"是否有小红点
    public static final String BOTTLE_NOTIFY_IN_HOME = "bottle_notify_in_home"; //首页中漂流瓶是否显示红点
    public static final String NEW_SUGGEST_LIST = "new_suggest_list"; //意见反馈"列表"按钮是否有小红点
    public static final String APP_USE_TIME = "app_use"; //应用打开次数
    public static final String MAC = "mac";//本机mac
    public static final String USER_INFO = "userInfo"; //用户信息
    public static final String YOUZAN_COOKIE_KEY = "youzan_cookie_key"; // 有赞返回的登录token
    public static final String YOUZAN_COOKIE_VALUE = "youzan_cookie_value";
    public static final String YOUZAN_ACCESS_TOKEN = "youzan_access_token";
    public static final String MSG_FRIEND_APPLY = "msg_friend_apply"; //是否在消息列表中显示好友申请
    public static final String MSG_BOTTLE = "msg_bottle"; // 是否在消息列表中显示漂流瓶
    public static final String SOCIAL_FILTER_SEX = "social_filter_sex"; // 社交找人筛选性别
    public static final String SOCIAL_FILTER_AGE = "social_filter_age"; // 社交找人筛选年龄
    public static final String SOCIAL_GUIDE = "social_guide"; // 是否第一次进入社交
    public static final String INPUT_TYPE = "input_type"; // 是否第一次进入社交
    public static final String CITY_CODE = "city_code"; // 城市代码
    public static final String REAL_CITY = "real_city"; // 定位所得的城市名
    
    /**
     * @Description: 保存String类型的字符串到shareinfo。
     * @param context
     * @param key 存储的名称
     * @param values   要存储的字符串
     * @return: void
     */
    public static void setTagString(Context context, String key, String values) {
        SharedPreferences preferences = context.getSharedPreferences(
                SWIFI_PREFERENCES, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(key, values);
        editor.commit();
    }

    /**
     * @Description: 从shareinfo里面获取字符串。
     * @param context
     * @param key 要获取的key名称。
     * @return   
     * @return: String 得到的字符串。获取失败返回null。
     */
    public static String getTagString(Context context, String key) {
        return context.getSharedPreferences(SWIFI_PREFERENCES,
                Context.MODE_PRIVATE).getString(key, "");
    }
    
    /**
     * @Description: 保存long类型到shareinfo。
     * @param context
     * @param key 存储的名称
     * @param values   要存储long值
     * @return: void
     */
    public static void setTagLong(Context context, String key, long values) {
        SharedPreferences preferences = context.getSharedPreferences(
                SWIFI_PREFERENCES, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putLong(key, values);
        editor.commit();
    }
    
    /**
     * @Description: 获取long数据
     * @param context
     * @param key 要获取的key
     * @return   
     * @return: long 返回获取到的整形值。获取失败返回0.
     */
    public static long getTagLong(Context context, String key) {
        return context.getSharedPreferences(SWIFI_PREFERENCES,
                Context.MODE_PRIVATE).getLong(key, 0L);
    }

    /**
     * @Description: 保存整形数据到shareinfo
     * @param context
     * @param key 要保存的key。
     * @param values   要保存的整形值
     * @return: void
     */
    public static void setTagInt(Context context, String key, int values) {
        SharedPreferences preferences = context.getSharedPreferences(
                SWIFI_PREFERENCES, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(key, values);
        editor.commit();
    }

    /**
     * @Description: 获取整形数据
     * @param context
     * @param key 要获取的key
     * @return   
     * @return: int 返回获取到的整形值。获取失败返回0.
     */
    public static int getTagInt(Context context, String key) {
        return context.getSharedPreferences(SWIFI_PREFERENCES,
                Context.MODE_PRIVATE).getInt(key, 0);
    }

    /**
     *
     * @Description: 获取整形数据
     * @param context
     * @param key 要获取的key
     *@param defaultValue 默认值
     * @return: int 返回获取到的整形值。获取失败返回0.
     */
    public static int getTagInt(Context context, String key, int defaultValue) {
        return context.getSharedPreferences(SWIFI_PREFERENCES,
                Context.MODE_PRIVATE).getInt(key, defaultValue);
    }

    /**
     * @Description: 保存布尔类型的数据到shareinfo
     * @param context
     * @param key 要保存的key。
     * @param values   要保存的布尔值。
     * @return: void
     */
    public static void setTagBoolean(Context context, String key, boolean values) {
        SharedPreferences preferences = context.getSharedPreferences(
                SWIFI_PREFERENCES, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(key, values);
        editor.commit();
    }

    /**
     * @Description: 从shareinfo获取布尔值。
     * @param context
     * @param key 要获取的key。
     * @return   
     * @return: boolean 获取到的布尔值。获取失败返回false。
     */
    public static boolean getTagBoolean(Context context, String key, boolean defaultValue) {
        return context.getSharedPreferences(SWIFI_PREFERENCES,
                Context.MODE_PRIVATE).getBoolean(key, defaultValue);
    }
    
    /**
     * @Description: 保存set<String>集合类型数据
     * @param context
     * @param key 要保存的key
     * @param values 要保存的值
     * @return   
     * @return: set<String> 返回获取到的set集合。获取失败返回null.
     */
	public static void setTagSet(Context context,String key,Set<String> values){
    	SharedPreferences preferences = context.getSharedPreferences(
                SWIFI_PREFERENCES, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putStringSet(key, values);
        editor.commit();
    }
    
    /**
     * @Description: 获取set<String>集合类型数据
     * @param context
     * @param key 要获取的key
     * @return   
     * @return: set<String> 返回获取到的set集合。获取失败返回null.
     */
	public static Set<String> getTagSet(Context context,String key){
    	return context.getSharedPreferences(SWIFI_PREFERENCES,
                Context.MODE_PRIVATE).getStringSet(key, null);
    }

	/**
     * @Description: 删除对应Key的数据
     * @param context
     * @param key 要删除的key
     * @return   
     * @return: void
     */
    public static void removeData(Context context,String key){
    	SharedPreferences preferences = context.getSharedPreferences(
                SWIFI_PREFERENCES, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
    }
    
    public static ArrayList<ChannelInfo> getNewsChannel(Context context) {
        String channels = getTagString(context, CHANNELS);
        if (!TextUtils.isEmpty(channels)) {
            Gson gson = new Gson();
            ChannelListResponseBody body = gson.fromJson(channels, ChannelListResponseBody.class);
             return body.columnList;
        }
        return new ArrayList<ChannelInfo>();
    }
}

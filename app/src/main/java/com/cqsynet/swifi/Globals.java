/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：全局变量
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi;

import android.graphics.Bitmap;

import com.cqsynet.swifi.model.BlackUrlObject;
import com.cqsynet.swifi.model.NewsItemInfo;
import com.cqsynet.swifi.model.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class Globals {
    public static final boolean DEBUG = BuildConfig.DEBUG;
    
    //本次获取免费时长
    public static int g_getTime;
    
    //记录每个固定广告位当前是第几次显示广告（值从0开始）
	public static int index_ad0001 = -1;
	public static int index_ad0002 = -1;
	public static int index_ad0003 = -1;
	public static int index_ad0004 = -1;
	public static int index_ad0005 = -1;
	public static int index_ad0006 = -1;
	public static int index_ad0007 = -1;
	public static int index_ad0008 = -1;
	public static int index_ad0009 = -1;
	public static int index_ad0019 = -1;
	public static int index_ad11059 = -1;
	
	//记录每个固定广告位的刷新时间,防止短时间内频繁刷新
	public static long refreshTime_ad0001 = 0;
	public static long refreshTime_ad0002 = 0;
	public static long refreshTime_ad0003 = 0;
	public static long refreshTime_ad0004 = 0;
	public static long refreshTime_ad0005 = 0;
	public static long refreshTime_ad0006 = 0;
	public static long refreshTime_ad0007 = 0;
	public static long refreshTime_ad0008 = 0;
	public static long refreshTime_ad0009 = 0;
	public static long refreshTime_ad0019 = 0;
	public static long refreshTime_ad11059 = 0;
	
	//保存cms里面的广告数据:string代表排期id
	public static HashMap<String, NewsItemInfo> g_advMap;
	//保存cms里面广告的轮播index
	public static HashMap<String, Integer> g_advIndexMap;
	//保存cms里面广告的轮播显示时间,防止一次点击多次切换
	public static HashMap<String, Long> g_advTimeMap;
	
	//生成des密钥的时间
	public static long g_encrypTime = 0;
	//des密钥
	public static String g_tempKey = "";
	//私有签名
	public static String g_tempPriSign = "";
	//公共签名
	public static String g_tempPubSign = "";	

	//屏幕截图
	public static Bitmap g_screenshot;
	
	//是否显示涂鸦界面的引导
	public static boolean g_showGuide = true;
	
	//用户信息
	public static UserInfo g_userInfo;

	//提权到00组是否成功
	public static boolean g_isUpdateUserGroup = false;

    //通知列表
    public static ArrayList<Integer> g_notificationList;

	//是否连接socket
	public static boolean g_isSocketConnected = false;

	//放通的域名白名单
	public static ArrayList<String> g_whiteList;
	//未放通的网址黑名单
	public static ArrayList<BlackUrlObject> g_blackList;

	// 是否有权限使用内部wifi
	public static boolean mHasFreeAuthority = false;
	// 是否连接着HeiKuai
	public static boolean mIsConnectFreeWifi = false;
	// 是否显示wifi界面的顶部广告
	public static boolean mIsShowTopAdv = true;

	// 全屏广告剩余的时间
	public static long g_advRestTime = -1;
}
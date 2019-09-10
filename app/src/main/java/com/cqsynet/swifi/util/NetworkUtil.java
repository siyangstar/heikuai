/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：判断网络连接状态
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cqsynet.swifi.AppConstants;

import java.util.List;

public class NetworkUtil {
    /**
     * 判断网络是否连接
     * 
     * @param context
     *            Context
     * @return true ---连接 false --无连接
     */
    public static boolean isNetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo networkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
                    return true;
            }
            return false;
        }
    }
    
    /**
     * 获取当前连接的WiFi信息
     * @param context
     */
    public static void getCurrentConnectionWifi(Context context) {
    	
    }
    
    /**
	 * 格式化SSID（有些Android系统返回的SSID会加双引号）
	 * @param ssid
	 * @return
	 */
	public static String formatSSID(String ssid) {
		if(!TextUtils.isEmpty(ssid)) {
			if(ssid.startsWith("\"") && ssid.endsWith("\"")) {
				ssid = ssid.substring(1);
				ssid = ssid.substring(0, ssid.length() - 1);
			}
		}
		return ssid;
	}
	
	/**
	 * 获取当前已连接WiFi信息
	 * @return
	 */
	public static WifiInfo getConnectionInfo(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
			return wifiInfo;
		}
		return null;
	}
	
	/**
	 * 是否连接了WiFi
	 * @param wifiInfo
	 * @return
	 */
	public static boolean isConnectWiFi(WifiInfo wifiInfo) {
        return wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED;
    }
	
	/**
	 * 断开尚WiFi，在退出系统的时候调用
	 */
	public static void disableSWiFi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = getConnectionInfo(context);
		if(isConnectFashionWiFi(wifiInfo)) {
//			mWifiManager.disableNetwork(wifiInfo.getNetworkId());  //使用此方法将无法重新自动连接wifi
			wifiManager.removeNetwork(wifiInfo.getNetworkId()); // 暂时注释掉 方便开发
		}
	}
	
	/**
	 * 是否已连接尚WiFi
	 * @param wifiInfo
	 * @return
	 */
	public static boolean isConnectFashionWiFi(WifiInfo wifiInfo) {
		if(wifiInfo != null) {
			String ssid = NetworkUtil.formatSSID(wifiInfo.getSSID());
			if(ssid.startsWith(AppConstants.WIFI_SSID) || ssid.toUpperCase().contains(AppConstants.WIFI_SSID2)) {
				return true;
			}
		}
        return false;
    }
	
	/**
	 * 判断配置文件中是否已存在该网络
	 * @param SSID
	 * @return
	 */
	public static WifiConfiguration isExsits(Context context, String SSID) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (formatSSID(existingConfig.SSID).equals(SSID)) {
				return existingConfig;
			}
		}
		return null;
	}
}

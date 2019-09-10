/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：WiFi实体信息
 *
 *
 * 创建标识：duxl 20140924
 */
package com.cqsynet.swifi.model;

import android.net.wifi.WifiConfiguration.Status;

public class WiFiObject {

	/**
	 * 排序，数字大的排在前面
	 */
	public int order = -99999;
	
	public int networkId = -99999;
	
	public String ssid;

	public String bssid;
	
	/**
	 * 描述
	 */
	public String desc;
	
	/**
	 * 信号强度
	 */
	public int ss = -99999;
	
	/**
	 * 状态
	 * @see Status
	 */
	public int status = -99999;
	
	/**
	 * Describes the authentication, key management, and encryption schemes supported by the access point.
	 */
	public String capabilities;

	@Override
	public String toString() {
		return "WiFiObject [order=" + order + ", networkId=" + networkId + ", SSID=" + ssid + ", desc=" + desc + ", level=" + ss + ", status=" + status + ", capabilities=" + capabilities + "]";
	}
	
	
}

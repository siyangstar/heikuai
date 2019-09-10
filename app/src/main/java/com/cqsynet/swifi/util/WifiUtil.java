package com.cqsynet.swifi.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.cqsynet.swifi.model.WiFiObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/8/30
 */
public class WifiUtil {

    private WifiManager wifiManager;

    public WifiUtil(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public List<WiFiObject> scanWifi() {
        wifiManager.startScan();
        List<WiFiObject> wifiList = new ArrayList<>();
        List<ScanResult> results = wifiManager.getScanResults();
        for (ScanResult scanResult : results) {
            WiFiObject wifi = new WiFiObject();
            wifi.ssid = scanResult.SSID;
            wifi.bssid = scanResult.BSSID;
            wifi.ss = scanResult.level;
            if ("".equals(scanResult.capabilities) || "[ESS]".equals(scanResult.capabilities)) {
                wifiList.add(wifi);
            }
        }
        return wifiList;
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals(SSID)) {
                return existingConfig;
            }
        }
        return null;
    }
}

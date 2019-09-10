package com.cqsynet.swifi.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.ToastUtil;

public class DevModeActivity extends HkActivity {

    private Button mBtnRefresh;
    private Button mBtnQuit;
    private TextView mTv1;
    private TextView mTv2;
    private TextView mTv3;
    private TextView mTv4;
    private WifiManager mWifiManager;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_mode);

        mBtnRefresh = findViewById(R.id.btnRefresh_activity_dev_mode);
        mBtnQuit = findViewById(R.id.btnQuit_activity_dev_mode);
        mTv1 = findViewById(R.id.tv1_activity_dev_mode);
        mTv2 = findViewById(R.id.tv2_activity_dev_mode);
        mTv3 = findViewById(R.id.tv3_activity_dev_mode);
        mTv4 = findViewById(R.id.tv4_activity_dev_mode);
        mWebView = findViewById(R.id.wv_activity_dev_mode);

        mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.getSettings().setTextZoom(100);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setGeolocationDatabasePath(getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath());
        mWebView.setWebViewClient(new WebViewClient() {});

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });
        mBtnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.destroy();
                DevModeActivity.this.finish();
            }
        });

        refresh();
    }

    private void refresh() {
        if(Globals.g_isSocketConnected) {
            mTv1.setText("长连接已连接");
        } else {
            mTv1.setText("长连接已断开");
        }

        mTv2.setText("IMEI+IMSI:  " + AppUtil.getIMEI(this) + "  " + AppUtil.getIMSI(this));
        mTv3.setText("分辨率:  " + AppUtil.getScreenResolution(this));

        ///////////////////////////////////测试ssid
        ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager != null) {
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo() != null) {
                    ToastUtil.showToast(this, "@@@@@@ssid = " + networkInfo.getExtraInfo().replace("\"", ""));
                }
            }
        }
        ////////////////////////////////////

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String wifiStr = "WIFI信息:\n    " +
                "BSSID: " + wifiInfo.getBSSID()
                + "\n    " + "SSID: " + wifiInfo.getSSID()
                + "\n    " + "IP: " + wifiInfo.getIpAddress()
                + "\n    " + "MAC: " + wifiInfo.getMacAddress()
                + "\n    " + "NetworkId: " + wifiInfo.getNetworkId()
                + "\n    " + "LinkSpeed: " + wifiInfo.getLinkSpeed()
                + "\n    " + "Rssi: " + wifiInfo.getRssi()
                + "\n    " + "IsHiddenSSID(): " + wifiInfo.getHiddenSSID()
                + "\n    " + "DescribeContents: " + wifiInfo.describeContents();
//                + "\n    " + "Frequency: " + wifiInfo.getFrequency();
        mTv4.setText(wifiStr);

        mWebView.loadUrl("http://app.heikuai.com/dev/devmode.html");
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

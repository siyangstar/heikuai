package com.cqsynet.swifi.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.DisconnectPromptActivity;
import com.cqsynet.swifi.activity.HomeActivity;
import com.cqsynet.swifi.activity.TimerPromptActivity;
import com.cqsynet.swifi.db.FreeWifiUseLogDao;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.SubmitWifiListRequestBody;
import com.cqsynet.swifi.model.UpdateUserGroupResponseObject;
import com.cqsynet.swifi.model.WiFiObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.CountDownTimer;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.LogUtil;
import com.cqsynet.swifi.util.NetworkUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.WifiUtil;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.Date;
import java.util.List;

import static android.R.attr.id;

public class TimerService extends Service {

    public static final int NOTIFICATION_ID_CLOSE_WIFI = 77;

    private static final int MSG_UPDATE_USER_GROUP = 1;
    private static final int MSG_SHOW_DISCONNECTED_DIALOG = 2;

    // 本次剩余时长
    private int mFreeTime = 0;

    public interface TimerCallback {
        void onTick(int minute);

        void onFinish();
    }

    private TimerCallback mTimerCallback;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_USER_GROUP:
                    updateUserGroup();
                    break;
                case MSG_SHOW_DISCONNECTED_DIALOG:
                    if (!Globals.mIsConnectFreeWifi) {
                        DisconnectPromptActivity.launch(TimerService.this, getString(R.string.wifi_disconnect));
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {// wifi连接上与否
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info != null && info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                            .getSystemService(Context.WIFI_SERVICE);
                    try {
                        wifiManager.startScan();
                    } catch (SecurityException e) {
                        LogUtil.writeToFile("TimerService -> wifiManager.startScan -> SecurityException: \n" + e.getMessage());
                    }
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (NetworkUtil.isConnectFashionWiFi(wifiInfo)) {
                        Globals.mIsConnectFreeWifi = true;
                        if (!Globals.g_isUpdateUserGroup && !Globals.mHasFreeAuthority) {
                            mHandler.removeMessages(MSG_UPDATE_USER_GROUP);
                            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_USER_GROUP, 1000); //延迟2秒发送提权接口,防止ip地址未获取成功
                        }
                        sendBroadcast(new Intent("ACTION_CLOSE"));
                    }
                } else if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    if(Globals.mIsConnectFreeWifi) {
                        mHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCONNECTED_DIALOG , 5000);
                    }
                    Globals.mIsConnectFreeWifi = false;
                    sendBroadcast(false);
                }
            }
        }
    };

    private CountDownTimer mCDTimer = new CountDownTimer(mFreeTime * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mFreeTime = (int) (millisUntilFinished / 1000) + 1;  //若剩10秒,那么millisUntilFinished的返回值约为9900毫秒,所以+1平衡误差
            if (mFreeTime % 60 == 0) {
                FreeWifiUseLogDao.getInstance(TimerService.this)
                        .updateTodayUse(DateUtil.formatTime(new Date(), "yyyy-MM-dd"));
                if (Globals.mHasFreeAuthority && mTimerCallback != null) {
                    mTimerCallback.onTick(mFreeTime);
                }
            }
            if (mFreeTime == AppConstants.NEAR_CLOSE_FREE_WIFI_TIME) { // 提示续时
                //实时判断是否已断开heikuai网络(之前偶尔会出现断开后,Globals.mIsConnectFreeWifi的值依然为true的情况)
                WifiManager wifiManager = (WifiManager) getApplicationContext() .getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (NetworkUtil.isConnectFashionWiFi(wifiInfo)) {
                    Globals.mIsConnectFreeWifi = true;
                } else {
                    Globals.mIsConnectFreeWifi = false;
                }
                if (Globals.mHasFreeAuthority && Globals.mIsConnectFreeWifi) {
                    TimerPromptActivity.launch(TimerService.this);
                }
            }
        }

        @Override
        public void onFinish() {
            mFreeTime = 0;
            FreeWifiUseLogDao.getInstance(TimerService.this)
                    .updateTodayUse(DateUtil.formatTime(new Date(), "yyyy-MM-dd"));
            Globals.mHasFreeAuthority = false;
            if (Globals.mIsConnectFreeWifi) {
                sendNotify(TimerService.this, "免费WiFi已关闭", "请重新打开网络");
                DisconnectPromptActivity.launch(TimerService.this, getString(R.string.wifi_free_finish));
                SharedPreferencesInfo.setTagLong(TimerService.this, SharedPreferencesInfo.FREE_WIFI_START_TIME, 0L);
            }
            Intent i = new Intent();
            i.putExtra("wifi_status", false);
            i.setAction("action_wifi_status");
            sendBroadcast(i);
            if (mTimerCallback != null) {
                mTimerCallback.onFinish();
            }
        }
    };

    private CountDownTimer mWifiScanTimer = new CountDownTimer(30 * 60 * 1000, 60 * 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (Globals.mIsConnectFreeWifi) {
                WifiUtil wifiUtil = new WifiUtil(TimerService.this);
                sendWifiList(wifiUtil.scanWifi());
                mWifiScanTimer.setRestTime(30 * 60 * 1000);
                mWifiScanTimer.cancel();
                mWifiScanTimer.start();
            }
        }
    };

    public TimerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new TimerBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver();
        Long passedTime = System.currentTimeMillis()
                - SharedPreferencesInfo.getTagLong(this, SharedPreferencesInfo.FREE_WIFI_START_TIME);
        mFreeTime = (int) ((SharedPreferencesInfo.getTagLong(this,
                SharedPreferencesInfo.FREE_WIFI_TIME) - passedTime) / 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Long passedTime = System.currentTimeMillis()
                - SharedPreferencesInfo.getTagLong(this, SharedPreferencesInfo.FREE_WIFI_START_TIME);
        mFreeTime = (int) ((SharedPreferencesInfo.getTagLong(this,
                SharedPreferencesInfo.FREE_WIFI_TIME) - passedTime) / 1000);
        if (mFreeTime > 0 && Globals.mHasFreeAuthority) {
            mCDTimer.setRestTime(mFreeTime * 1000);
            mCDTimer.cancel();
            mCDTimer.start();
        } else {
            mCDTimer.cancel();
        }

        if (Globals.mIsConnectFreeWifi) {
            mWifiScanTimer.setRestTime(2 * 60 * 1000);
            mWifiScanTimer.cancel();
            mWifiScanTimer.start();
        } else {
            mWifiScanTimer.cancel();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        mHandler.removeMessages(MSG_UPDATE_USER_GROUP);
        mHandler.removeMessages(MSG_SHOW_DISCONNECTED_DIALOG);
        mCDTimer.cancel();
        mCDTimer = null;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.setPriority(2147483647);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void updateUserGroup() {
        WebServiceIf.updateUserGroup(this, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    Gson gson = new Gson();
                    UpdateUserGroupResponseObject responseObj = gson.fromJson(response, UpdateUserGroupResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            Globals.g_isUpdateUserGroup = true;
                            if (responseObj.body.status.equals("on")) {
                                Globals.mHasFreeAuthority = true;
                                sendBroadcast(true);
                            } else if (responseObj.body.status.equals("off")) {
                                Globals.mHasFreeAuthority = false;
                                SharedPreferencesInfo.setTagLong(TimerService.this, SharedPreferencesInfo.FREE_WIFI_TIME, 0L);
                                sendBroadcast(false);
                            }
                        } else if (header.errCode.equals("")) {
                            if (Globals.DEBUG) {
                                System.out.println("提权到00组失败: " + header.errMsg);
                            }
                            sendBroadcast(false);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                sendBroadcast(false);
            }
        });
    }

    private void sendBroadcast(boolean isWifi) {
        Intent i = new Intent();
        i.putExtra("wifi_status", isWifi);
        i.setAction("action_wifi_status");
        sendBroadcast(i);
    }

    private void sendNotify(Context context, String title, String content) {
        Intent jumpIntent = new Intent(context, HomeActivity.class);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pi = PendingIntent.getActivity(context, id, jumpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.noti_icon);
            builder.setColor(0xFF39B44A);
        } else {
            builder.setSmallIcon(R.drawable.ic_launcher);
        }
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true);
        builder.setContentIntent(pi);
        nm.notify(NOTIFICATION_ID_CLOSE_WIFI, builder.build());
    }

    /**
     * 发送wifi热线统计信息
     * @param wifiList
     */
    public void sendWifiList(List<WiFiObject> wifiList) {
        if (wifiList.isEmpty()) {
            return;
        }
        SubmitWifiListRequestBody body = new SubmitWifiListRequestBody();
        body.wifiList = wifiList;
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
            }

            @Override
            public void onErrorResponse() {
            }
        };
        WebServiceIf.sendWifiList(this, body, callback);
    }

    public void setTimerCallback(TimerCallback timerCallback) {
        this.mTimerCallback = timerCallback;
    }

    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
}

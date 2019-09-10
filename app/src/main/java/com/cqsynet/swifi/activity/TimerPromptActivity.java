package com.cqsynet.swifi.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.cqsynet.swifi.model.OpenFreeWifiRequestBody;
import com.cqsynet.swifi.model.OpenFreeWifiResponseObject;
import com.cqsynet.swifi.model.RequestHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.service.TimerService;
import com.cqsynet.swifi.util.AdvDataHelper;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.GlideRoundRectTransform;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.Date;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/4/19
 */
public class TimerPromptActivity extends HkActivity implements View.OnClickListener {

    private static final int NOTIFICATION_ID = 1001;

    private Animation mAnim;
    private ImageView mIvAdvBanner;
    private TextView mTvTitle;

    private AdvInfoObject mAdvInfo;

    private long mStartFullAdTime = 0L;

    private BroadcastReceiver mWifiStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean status = intent.getBooleanExtra("wifi_status", false);
            if (!status) {
                finish();
            }
        }
    };

    public static void launch(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, TimerPromptActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_casttotime_remind);

        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction("action_wifi_status");
        registerReceiver(mWifiStatusReceiver, wifiFilter);

        LinearLayout llRoot = findViewById(R.id.ll_root);
        llRoot.setOnClickListener(this);
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        FrameLayout flRenew = findViewById(R.id.fl_renew);
        flRenew.setOnClickListener(this);
        ImageView mIvClock = findViewById(R.id.iv_clock);
        mIvAdvBanner = findViewById(R.id.iv_adv_banner);
        mTvTitle = findViewById(R.id.tv_title);
        mAnim = AnimationUtils.loadAnimation(this, R.anim.shake_clock);

        mIvAdvBanner.postDelayed(new Runnable() {
            @Override
            public void run() {
                View v = findViewById(R.id.iv_adv_banner);
                Drawable drawable = getResources().getDrawable(R.drawable.free_wifi_img);
                int newHeight = drawable.getIntrinsicHeight();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
                params.height = newHeight;
                v.setLayoutParams(params);
            }
        }, 100);

        String formatTime = getString(R.string.casttotime_remind);
        TextView tvMsg = findViewById(R.id.tv_msg);
        tvMsg.setText(String.format(formatTime, AppConstants.NEAR_CLOSE_FREE_WIFI_TIME / 60));
        TextView tvTime = findViewById(R.id.tv_ok);
        if (Globals.g_getTime == 0) {
            Globals.g_getTime = 1800;
        }
        tvTime.setText("继续免费上网" + (Globals.g_getTime / 60) + "分钟");

        loadAdv();

        mIvClock.startAnimation(mAnim);
        sendNotification();

        String source = getIntent().getStringExtra("flag");
        if ("fullAdv".equals(source)) {
            openWifi();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAnim.cancel();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.ll_root:
            case R.id.iv_back:
                finish();
                overridePendingTransition(0, 0);
                break;
            case R.id.fl_renew:
                if (System.currentTimeMillis() - mStartFullAdTime > 60 * 1000) {
                    Intent intent = new Intent(this, FullAdvActivity.class);
                    intent.putExtra("accessType", 1);
                    intent.putExtra("source", "TimerPromptActivity");
                    startActivityForResult(intent, WifiActivity.REQUEST_CODE_FULL_AD);
                    finish();
                }
                mStartFullAdTime = System.currentTimeMillis();
                if (!Globals.mIsConnectFreeWifi) {
                    ToastUtil.showToast(this, "网络已断开，请先链接嘿快wifi");
                }
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String source = intent.getStringExtra("flag");
        if ("fullAdv".equals(source)) {
            openWifi();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    private void openWifi() {
        final OpenFreeWifiRequestBody requestBody = new OpenFreeWifiRequestBody();
        requestBody.type = "1";
        WebServiceIf.openFreeWifi(this, requestBody, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (response != null) {
                    OpenFreeWifiResponseObject responseObj = new Gson().fromJson(response,
                            OpenFreeWifiResponseObject.class);
                    if (responseObj.header != null) {
                        if (AppConstants.RET_OK.equals(responseObj.header.ret)) {
                            SharedPreferencesInfo.setTagString(TimerPromptActivity.this,
                                    SharedPreferencesInfo.MAC, responseObj.body.mac);
                            RequestHeader.mRequestHead.mac = responseObj.body.mac;

                            Globals.g_getTime = Integer.parseInt(responseObj.body.time);
                            int freeTime = Globals.g_getTime - 5; //减少5秒钟,缓冲和服务器时间的误差

                            SharedPreferencesInfo.setTagLong(TimerPromptActivity.this,
                                    SharedPreferencesInfo.FREE_WIFI_START_TIME, System.currentTimeMillis());
                            SharedPreferencesInfo.setTagLong(TimerPromptActivity.this,
                                    SharedPreferencesInfo.FREE_WIFI_TIME, freeTime * 1000);
                            FreeWifiUseLogDao.getInstance(TimerPromptActivity.this)
                                    .initTodayUse(DateUtil.formatTime(new Date(), "yyyy-MM-dd"));
                            startService(new Intent(TimerPromptActivity.this, TimerService.class));
                        } else {
                            ToastUtil.showToast(TimerPromptActivity.this, "加载失败,请检查网络后重试" + "(" + responseObj.header.errCode + ")");
                        }
                    } else {
                        ToastUtil.showToast(TimerPromptActivity.this, R.string.request_fail_warning);
                    }
                } else {
                    ToastUtil.showToast(TimerPromptActivity.this, R.string.request_fail_warning);
                }
                finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(TimerPromptActivity.this, R.string.request_fail_warning);
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    private void loadAdv() {
        List<AdvInfoObject> advData = new AdvDataHelper(this, null).getAdvData();
        if (advData != null && !advData.isEmpty()) {
            for (AdvInfoObject advInfo : advData) {
                if ("ad0004".equals(advInfo.id)) {
                    mAdvInfo = advInfo;
                    break;
                }
            }
        }

        if (mAdvInfo != null) {
            try {
                final int index = mAdvInfo.getSortIndex(mAdvInfo.getCurrentIndex());
                String imgUrl = mAdvInfo.adUrl[index];
                GlideApp.with(this)
                        .load(imgUrl)
                        .transform(new GlideRoundRectTransform(this, 5, GlideRoundRectTransform.TOP))
                        .into(mIvAdvBanner);
                String tagPath = mAdvInfo.jumpUrl[index];
                mIvAdvBanner.setTag(tagPath);
                mTvTitle.setText(mAdvInfo.adName[index]);
                if (!TextUtils.isEmpty(mAdvInfo.advId[index])) {
                    StatisticsDao.saveStatistics(this, "advView", mAdvInfo.advId[index]); // 上网续时提醒广告位统计
                }

                //点击打开广告详情
                if (mAdvInfo.jumpUrl.length > 0 && !TextUtils.isEmpty(mAdvInfo.jumpUrl[index])) {
                    mIvAdvBanner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StatisticsDao.saveStatistics(TimerPromptActivity.this, "advClick", mAdvInfo.advId[index]); // 顶部广告点击统计
                            Intent webIntent = new Intent();
                            webIntent.putExtra("url", mAdvInfo.jumpUrl[index]);
                            webIntent.putExtra("mainType", "0");
                            webIntent.putExtra("subType", "0");
                            WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                            webDispatcher.dispatch(webIntent, TimerPromptActivity.this);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent jumpIntent = new Intent(this, HomeActivity.class);
        jumpIntent.putExtra("toIndex", 0);
        PendingIntent pi = PendingIntent.getActivity(this, 1, jumpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.noti_icon);
            builder.setColor(0xFF39B44A);
        } else {
            builder.setSmallIcon(R.drawable.ic_launcher);
        }
        builder.setContentTitle("免费上网到时提醒");
        builder.setContentText(String.format("您的免费上网时间即将在%d分钟后结束，请及时\"加油\"", AppConstants.NEAR_CLOSE_FREE_WIFI_TIME / 60));
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true);
        builder.setContentIntent(pi);
        nm.notify(NOTIFICATION_ID, builder.build());
    }
}

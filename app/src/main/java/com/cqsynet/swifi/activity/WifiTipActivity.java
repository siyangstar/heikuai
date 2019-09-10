package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

/**
 * Author: sayaki
 * Date: 2017/7/24
 */
public class WifiTipActivity extends HkActivity {

    private static final int TIP_1 = 0;
    private static final int TIP_2 = 1;
    private static final int TIP_3 = 2;
    private static final int TIP_TIME = 2000;

    private TextView mTvTip1;
    private TextView mTvTipContent1;
    private TextView mTvTip2;
    private TextView mTvTipContent2;
    private TextView mTvTip3;
    private TextView mTvTipContent3;
    private ImageView mIvTip;
    private TextView mTvBack;
    private TextView mTvConn;
    private CheckBox checkBox;

    private Animation animation;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIP_1:
                    mTvTip1.setBackgroundResource(R.drawable.bg_green_round);
                    mTvTipContent1.setTextColor(getResources().getColor(R.color.text1));
                    mTvTip2.setBackgroundResource(R.drawable.bg_light_green_round);
                    mTvTipContent2.setTextColor(getResources().getColor(R.color.text4));
                    mTvTip3.setBackgroundResource(R.drawable.bg_light_green_round);
                    mTvTipContent3.setTextColor(getResources().getColor(R.color.text4));
                    mIvTip.setImageResource(R.drawable.wifi_tip_1);
                    mIvTip.startAnimation(animation);
                    handler.sendEmptyMessageDelayed(TIP_2, TIP_TIME);
                    break;
                case TIP_2:
                    mTvTip1.setBackgroundResource(R.drawable.bg_light_green_round);
                    mTvTipContent1.setTextColor(getResources().getColor(R.color.text4));
                    mTvTip2.setBackgroundResource(R.drawable.bg_green_round);
                    mTvTipContent2.setTextColor(getResources().getColor(R.color.text1));
                    mTvTip3.setBackgroundResource(R.drawable.bg_light_green_round);
                    mTvTipContent3.setTextColor(getResources().getColor(R.color.text4));
                    mIvTip.setImageResource(R.drawable.wifi_tip_2);
                    mIvTip.startAnimation(animation);
                    handler.sendEmptyMessageDelayed(TIP_3, TIP_TIME);
                    break;
                case TIP_3:
                    mTvTip1.setBackgroundResource(R.drawable.bg_light_green_round);
                    mTvTipContent1.setTextColor(getResources().getColor(R.color.text4));
                    mTvTip2.setBackgroundResource(R.drawable.bg_light_green_round);
                    mTvTipContent2.setTextColor(getResources().getColor(R.color.text4));
                    mTvTip3.setBackgroundResource(R.drawable.bg_green_round);
                    mTvTipContent3.setTextColor(getResources().getColor(R.color.text1));
                    mIvTip.setImageResource(R.drawable.wifi_tip_3);
                    mIvTip.startAnimation(animation);
                    handler.sendEmptyMessageDelayed(TIP_1, TIP_TIME);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_tip);

        mTvTip1 = findViewById(R.id.tv_tip_1);
        mTvTipContent1 = findViewById(R.id.tv_tip_content_1);
        mTvTip2 = findViewById(R.id.tv_tip_2);
        mTvTipContent2 = findViewById(R.id.tv_tip_content_2);
        mTvTip3 = findViewById(R.id.tv_tip_3);
        mTvTipContent3 = findViewById(R.id.tv_tip_content_3);
        mIvTip = findViewById(R.id.iv_tip);

        mTvBack = findViewById(R.id.tv_back);
        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvConn = findViewById(R.id.tv_conn);
        mTvConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                finish();
            }
        });
        checkBox = findViewById(R.id.check_box);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesInfo.setTagBoolean(WifiTipActivity.this, SharedPreferencesInfo.WIFI_TIP, checkBox.isChecked());
            }
        });

        animation = AnimationUtils.loadAnimation(this, R.anim.wifi_in);
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.sendEmptyMessage(TIP_1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeMessages(TIP_1);
        handler.removeMessages(TIP_2);
        handler.removeMessages(TIP_3);
    }
}

package com.cqsynet.swifi.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.service.TimerService;

/**
 * Author: sayaki
 * Date: 2017/4/19
 */
public class DisconnectPromptActivity extends HkActivity implements View.OnClickListener {

    public static void launch(Context context, String content) {
        Intent intent = new Intent();
        intent.setClass(context, DisconnectPromptActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("content", content);
        context.startActivity(intent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (("ACTION_CLOSE".equals(intent.getAction()))) {
                finish();
                overridePendingTransition(0, 0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disconnect_dialog_layout);

        LinearLayout llRoot = findViewById(R.id.ll_root);
        llRoot.setOnClickListener(this);
        Button btnKnow = findViewById(R.id.btn_know);
        btnKnow.setOnClickListener(this);
        TextView tvMsg = findViewById(R.id.tv_msg);
        tvMsg.setText(getIntent().getStringExtra("content"));

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_CLOSE");
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.ll_root:
            case R.id.btn_know:
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(TimerService.NOTIFICATION_ID_CLOSE_WIFI);
                finish();
                overridePendingTransition(0, 0);
                break;
        }
    }
}

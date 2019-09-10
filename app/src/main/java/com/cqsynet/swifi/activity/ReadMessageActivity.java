package com.cqsynet.swifi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.MessageDao;
import com.cqsynet.swifi.model.MessageInfo;
import com.cqsynet.swifi.view.TitleBar;

public class ReadMessageActivity extends HkActivity implements OnClickListener {

    private final String TAG = "ReadMessageActivity";
    private TitleBar mrTitleBar;
    private TextView mtTextView;
    private TextView mcTextView;
    private TextView mtTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_read);
        mrTitleBar = findViewById(R.id.back_titlebar_read);
        mrTitleBar.setTitle("系统消息");
        mrTitleBar.setLeftIconClickListener(this);
        mtTextView = findViewById(R.id.read_title);
        mcTextView = findViewById(R.id.read_content);
        mtTime = findViewById(R.id.message_detail_time);
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initData();
    }

    /**
     * 加载数据
     */
    private void initData() {
        String msgId = getIntent().getStringExtra("msgId");
        MessageDao.getInstance(this).updateMsgStatus(msgId);
        MessageInfo info = MessageDao.getInstance(this).queryMsg(msgId);
        mtTextView.setText(info.title);
        mcTextView.setText(info.content);
        mtTime.setText(info.createTime);
        Intent remindIntent = new Intent();
        remindIntent.setAction(AppConstants.ACTION_REFRESH_RED_POINT);
        sendBroadcast(remindIntent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivBack_titlebar_layout) { // 返回
            finish();
        }
    }

    //监控返回键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

package com.cqsynet.swifi.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.AppUtil;

/**
 * Author: sayaki
 * Date: 2018/2/28
 */
public class TipDialog extends Dialog {

    private Context mContext;
    private String mMessage;

    public TipDialog(Context context, int theme, String message) {
        super(context, theme);
        mContext = context;
        mMessage = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_tip);
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = AppUtil.getScreenW((Activity) mContext) - AppUtil.dp2px(mContext, 120);
        getWindow().setAttributes(lp);
        TextView tvMessage = findViewById(R.id.tv_message);
        tvMessage.setText(mMessage);
        TextView tvClose = findViewById(R.id.tv_close);
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TipDialog.this.dismiss();
            }
        });
    }
}

/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：自定义删除dialog
 *
 *
 * 创建标识：xy 20150622
 */
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

public class DeleteDialog extends Dialog implements android.view.View.OnClickListener {

    private Context mContext;
    private TextView tv_cancel;//取消
    private TextView tv_confirm;//确定
    private TextView tv_title;//标题
    private String mTitle;
    private MyDialogListener listener;//dialog的控件监听接口

    public DeleteDialog(Context context, int theme, String title, MyDialogListener listener) {
        super(context, theme);
        this.mContext = context;
        this.listener = listener;
        this.mTitle = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete);
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = AppUtil.getScreenW((Activity) mContext) - AppUtil.dp2px(mContext, 48);
        getWindow().setAttributes(lp);
        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(mTitle);
        tv_confirm = findViewById(R.id.tv_confirm_collect);
        tv_cancel = findViewById(R.id.tv_cancel_collect);
        tv_confirm.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }

    public interface MyDialogListener {
        void onClick(View view);
    }
}

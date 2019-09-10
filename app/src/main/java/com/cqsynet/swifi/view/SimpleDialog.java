package com.cqsynet.swifi.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.AppUtil;

public class SimpleDialog extends Dialog implements android.view.View.OnClickListener {

	private TextView mTvMsg;
    private TextView mTvOk;
    private ImageView mIvClose;
	
	private Context mContext;
	private CharSequence mMsg;
	private MyDialogListener mListener; //dialog的控件监听接口
	
	public SimpleDialog(Context context, String msg, MyDialogListener listener) {
		super(context, R.style.dialog);
		this.mContext = context;
		this.mMsg = msg;
        this.mListener = listener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_simple);
		setCanceledOnTouchOutside(false);
		setCancelable(false);
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.width = AppUtil.getScreenW((Activity) mContext) - AppUtil.dp2px(mContext, 48);
		getWindow().setAttributes(lp);
		mTvMsg = findViewById(R.id.tv_msg);
		mTvMsg.setText(mMsg);
        mTvOk = findViewById(R.id.tv_ok);
        mTvOk.setOnClickListener(this);
        mIvClose = findViewById(R.id.iv_close);
        mIvClose.setOnClickListener(this);
    }


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_ok:
				mListener.onOkClick(v);
				break;
			case R.id.iv_close:
				mListener.onCloseClick(v);
				break;
		}
	}

	public interface MyDialogListener {
		void onOkClick(View view);

		void onCloseClick(View view);
	}
}

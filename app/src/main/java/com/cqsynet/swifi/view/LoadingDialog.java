/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：loading框
 *
 *
 * 创建标识：zhaosy 20150416
 */
package com.cqsynet.swifi.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.R;

public class LoadingDialog extends Dialog {

	public LoadingDialog(Context context) {
		super(context);
	}

	/**
	 * 得到自定义的progressDialog
	 * 
	 * @param context
	 * @return
	 */
	public static Dialog createLoadingDialog(Context context, String content) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.dialog_loading, null);
		LinearLayout layout = v.findViewById(R.id.dialog_view);
		TextView tipTextView = v.findViewById(R.id.tipTextView);

		tipTextView.setText(content);

		Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);
		loadingDialog.setCancelable(false);
		loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		return loadingDialog;
	}

}
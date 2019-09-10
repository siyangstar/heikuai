package com.cqsynet.swifi.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class CustomDialog extends Dialog {
	Context context;
	private View customView;

	public CustomDialog(Context context) {
		super(context);
		this.context = context;
	}
	
	public CustomDialog(Context context,int theme){
        super(context, theme);
        this.context = context;
    }

	public CustomDialog(Context context, int theme, int layout) {
		super(context, theme);
		this.context = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		customView = inflater.inflate(layout, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(customView);
	}

	@Override
	public View findViewById(int id) {
		// TODO Auto-generated method stub
		return super.findViewById(id);
	}

	public View getCustomView() {
		return customView;
	}
}
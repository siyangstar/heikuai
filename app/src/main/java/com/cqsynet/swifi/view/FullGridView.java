/*
 * Copyright (C) 2014 尚wifi
 * 版权所有
 *
 * 功能描述：全屏GridView，ScollView中嵌套GridView显示UI不全时使用
 *
 *
 * 创建标识：duxl 20141221
 */
package com.cqsynet.swifi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class FullGridView extends GridView {

	public FullGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FullGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FullGridView(Context context) {
		super(context);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}

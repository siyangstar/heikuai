/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：可涂鸦的ImageView
 *
 *
 * 创建标识：zhaosy 20151110
 */
package com.cqsynet.swifi.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class EditableImageView extends ImageView {

	private Paint mPaint;
	private Bitmap originalBitmap;
	private Bitmap newBitmap;
	private float mStartX = 0;
	private float mStartY = 0;
	private float mEndX = 0;
	private float mEndY = 0;
	private boolean mIsMoving = true;
	private float mScale;

	public EditableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		mPaint.setStyle(Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(4.0f);
	}
	
	public void setImage(Bitmap bm) {
		originalBitmap = bm;
		newBitmap = originalBitmap;
		mScale = (float)newBitmap.getWidth() / (float)newBitmap.getHeight();
	}
	
	public Bitmap getImage() {
		return newBitmap;
	}

	/**
	 * 清除所有编辑
	 */
	public void clear() {
		newBitmap = originalBitmap;
		invalidate();
	}

	/**
	 * 设置画笔粗细
	 * @param strokeWidth
	 */
	public void setstyle(float strokeWidth) {
		mPaint.setStrokeWidth(strokeWidth);
	}
	
	/**
	 * 设置画笔颜色
	 * @param color
	 */
	public void setColor(int color) {
		mPaint.setColor(color);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (newBitmap == null) {
			return;
		}
		if(mScale == 0) {
			mScale = (float)newBitmap.getWidth() / (float)newBitmap.getHeight();
		}
		int width = (int) (canvas.getHeight() * mScale);
		canvas.drawBitmap(EditBitmap(width, canvas.getHeight(), (canvas.getWidth() - width) / 2), (canvas.getWidth() - width) / 2, 0, null);
	}

	public Bitmap EditBitmap(int width, int height, int offsetX) {
		if(newBitmap.getWidth() != width) {
			newBitmap = Bitmap.createScaledBitmap(newBitmap, width, height, true);
		}
		Canvas canvas = new Canvas(newBitmap);

		if (mIsMoving) {
			canvas.drawLine(mStartX - offsetX, mStartY, mEndX - offsetX, mEndY, mPaint);
			mStartX = mEndX;
			mStartY = mEndY;
			mIsMoving = false;
		}
		return newBitmap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mStartX = event.getX();
			mStartY = event.getY();
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			mEndX = event.getX();
			mEndY = event.getY();
			mIsMoving = true;
			invalidate();
			return true;
		}
		return super.onTouchEvent(event);
	}
}
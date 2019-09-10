/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：图形扫描界面蒙层
 *
 *
 * 创建标识：zhaosiyang 20170516
 */
package com.cqsynet.swifi.scaner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.AppUtil;

import static android.R.attr.scaleHeight;
import static android.R.attr.scaleWidth;

public class MaskView extends ImageView {
    private static final String TAG = "YanZi";
    private Paint mCornerPaint;
    private Paint mLinePaint;
    private Paint mShadowPaint;
    private Paint mAnimPaint;
    private Rect mCenterRect = null;
    private Context mContext;
    int mScreenWidth, mScreenHeight;
    int mSideLength;
    int mAnimLine;
    Bitmap mLineBmp;

    public MaskView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPaint();
        mContext = context;

        mScreenWidth = AppUtil.getScreenW((Activity) mContext);
        mScreenHeight = AppUtil.getScreenH((Activity) mContext);
        mSideLength = mScreenWidth * 3 / 5;

        mLineBmp = getScaleBitmap(R.drawable.scan_line, mSideLength);

        int x1 = (mScreenWidth - mSideLength) / 2;
        int y1 = (mScreenHeight - mSideLength) / 2;
        int x2 = x1 + mSideLength;
        int y2 = y1 + mSideLength;
        setCenterRect(new Rect(x1, y1, x2, y2));
    }

    private Bitmap getScaleBitmap(int resourceId, int width) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();

        float scaleRatio = width * 1.0f / srcWidth;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleRatio, scaleRatio);

        return Bitmap.createBitmap(bitmap, 0, 0, srcWidth, srcHeight, matrix, true);
    }

    private void initPaint(){
        //绘制边角的Paint
        mCornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCornerPaint.setColor(Color.WHITE);
        mCornerPaint.setStyle(Style.STROKE);
        mCornerPaint.setStrokeWidth(4f);
        mCornerPaint.setAlpha(255);

        //绘制中间网格线的Paint
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setStrokeWidth(1f);
        mLinePaint.setAlpha(80);

        //绘制四周阴影区域
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setStyle(Style.FILL);
        mShadowPaint.setAlpha(100);

        //绘制扫描动画的Paint
        mAnimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimPaint.setColor(Color.RED);
        mAnimPaint.setStyle(Style.STROKE);
        mAnimPaint.setStrokeWidth(2f);
        mAnimPaint.setAlpha(100);

    }

    public void setCenterRect(Rect r){
        Log.i(TAG, "setCenterRect...");
        this.mCenterRect = r;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onDraw...");
        if(mCenterRect == null) {
            return;
        }

        //绘制四周阴影区域
        canvas.drawRect(0, 0, mScreenWidth, mCenterRect.top, mShadowPaint);
        canvas.drawRect(0, mCenterRect.bottom + 1, mScreenWidth, mScreenHeight, mShadowPaint);
        canvas.drawRect(0, mCenterRect.top, mCenterRect.left - 1, mCenterRect.bottom  + 1, mShadowPaint);
        canvas.drawRect(mCenterRect.right + 1, mCenterRect.top, mScreenWidth, mCenterRect.bottom + 1, mShadowPaint);

        //绘制方形边框
        canvas.drawRect(mCenterRect, mLinePaint);

        //绘制四个角
        int cornerLength = mSideLength / 12;
        canvas.drawLine(mCenterRect.left, mCenterRect.top, mCenterRect.left + cornerLength, mCenterRect.top, mCornerPaint);
        canvas.drawLine(mCenterRect.left, mCenterRect.top, mCenterRect.left, mCenterRect.top + cornerLength, mCornerPaint);
        canvas.drawLine(mCenterRect.right, mCenterRect.top, mCenterRect.right - cornerLength, mCenterRect.top, mCornerPaint);
        canvas.drawLine(mCenterRect.right, mCenterRect.top, mCenterRect.right, mCenterRect.top + cornerLength, mCornerPaint);
        canvas.drawLine(mCenterRect.left, mCenterRect.bottom, mCenterRect.left + cornerLength, mCenterRect.bottom, mCornerPaint);
        canvas.drawLine(mCenterRect.left, mCenterRect.bottom, mCenterRect.left, mCenterRect.bottom - cornerLength, mCornerPaint);
        canvas.drawLine(mCenterRect.right, mCenterRect.bottom, mCenterRect.right - cornerLength, mCenterRect.bottom, mCornerPaint);
        canvas.drawLine(mCenterRect.right, mCenterRect.bottom, mCenterRect.right, mCenterRect.bottom - cornerLength, mCornerPaint);

        //绘制网格线
        int lineNum = 12; //网格线的条数
        int sep = mSideLength / lineNum; //网格线之间的间隔距离
        for(int i = 0; i < lineNum; i++) {
            canvas.drawLine(mCenterRect.left, mCenterRect.top + sep * i, mCenterRect.right, mCenterRect.top + sep * i, mLinePaint);
            canvas.drawLine(mCenterRect.left + sep * i, mCenterRect.top, mCenterRect.left + sep * i, mCenterRect.bottom, mLinePaint);
        }

        //绘制动画
        mAnimLine = mAnimLine % mSideLength;
//        canvas.drawLine(mCenterRect.left, mCenterRect.top + mAnimLine, mCenterRect.right, mCenterRect.top + mAnimLine, mAnimPaint);
        canvas.drawBitmap(mLineBmp, mCenterRect.left, mCenterRect.top + mAnimLine, mAnimPaint);
        mAnimLine = mAnimLine + 3;

        invalidate();

        super.onDraw(canvas);
    }
}

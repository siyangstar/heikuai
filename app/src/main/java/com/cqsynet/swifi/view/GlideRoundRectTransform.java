/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：用于Glide的圆角矩形配置
 *
 *
 * 创建标识：zhaosy 20161230
 */
package com.cqsynet.swifi.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class GlideRoundRectTransform extends BitmapTransformation {

    private static float mRadius = 0f;
    public static final int ALL = 0x0001;
    public static final int TOP = 0x0002;
    public static final int LEFT = 0x0003;
    public static final int RIGHT = 0x0004;
    public static final int BOTTOM = 0x0005;
    private static int mType = ALL;

    /**
     * 构造函数 默认圆角半径 4dp
     *
     * @param context Context
     */
    public GlideRoundRectTransform(Context context) {
        this(context, 4, ALL);
    }

    /**
     * 构造函数
     *
     * @param context Context
     * @param dp 圆角半径
     */
    public GlideRoundRectTransform(Context context, int dp, int type) {
        super(context);
        mRadius = Resources.getSystem().getDisplayMetrics().density * dp;
        mType = type;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return roundCrop(pool, toTransform);
    }

    private static Bitmap roundCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        RectF rectF;
        switch (mType) {
            case TOP:
                rectF = new RectF(0, mRadius, source.getWidth(), source.getHeight());
                canvas.drawRect(rectF, paint);
                rectF = new RectF(0f, 0f, source.getWidth(), mRadius * 2);
                canvas.drawRoundRect(rectF, mRadius, mRadius, paint);
                break;
            case LEFT:
                rectF = new RectF(mRadius, 0, source.getWidth(), source.getHeight());
                canvas.drawRect(rectF, paint);
                rectF = new RectF(0, 0, mRadius * 2 , source.getHeight());
                canvas.drawRoundRect(rectF, mRadius, mRadius, paint);
                break;
            case RIGHT:
                rectF = new RectF(0, 0, source.getWidth() - mRadius, source.getHeight());
                canvas.drawRect(rectF, paint);
                rectF = new RectF(source.getWidth() - mRadius * 2, 0f, source.getWidth(), source.getHeight());
                canvas.drawRoundRect(rectF, mRadius, mRadius, paint);
                break;
            case BOTTOM:
                rectF = new RectF(0, 0, source.getWidth(), source.getHeight() - mRadius);
                canvas.drawRect(rectF, paint);
                rectF = new RectF(0, source.getHeight() - mRadius * 2 , source.getWidth() , source.getHeight());
                canvas.drawRoundRect(rectF, mRadius, mRadius, paint);
                break;
            default:
                rectF = new RectF(0, 0, source.getWidth() , source.getHeight());
                canvas.drawRoundRect(rectF, mRadius, mRadius, paint);
                break;
        }
        return result;
    }

//    @Override
//    public String getId() {
//        return getClass().getName() + Math.round(mRadius);
//    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
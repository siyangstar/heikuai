package com.cqsynet.swifi.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * Author: sayaki
 * Date: 2018/1/9
 */
public class BlurTransformation extends BitmapTransformation {

    private Context mContext;

    public BlurTransformation(Context context) {
        this.mContext = context;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return blurBitmap(toTransform, outWidth, outHeight);
    }

    public Bitmap blurBitmap(Bitmap bitmap, int outWidth, int outHeight) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
            RenderScript rs = RenderScript.create(mContext);
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
            // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
            blurScript.setRadius(25.0f);
            // 设置blurScript对象的输入内存
            blurScript.setInput(tmpIn);
            // 将输出数据保存到输出内存中
            blurScript.forEach(tmpOut);
            // 将数据填充到Allocation中
            tmpOut.copyTo(outputBitmap);
            return outputBitmap;
        } else {
            return null;
        }
    }
}

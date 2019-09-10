/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：设置glide内联
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDefaultTransitionOptions(Drawable.class, DrawableTransitionOptions.withCrossFade())
            .setDefaultTransitionOptions(Bitmap.class, BitmapTransitionOptions.withCrossFade());
    }

    /**
     * 清单解析的开启
     *
     * 这里不开启，避免添加相同的modules两次
     * @return
     */
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}

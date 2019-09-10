/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻列表广告工具类
 *
 *
 * 创建标识：zhaosy 2015.5.5
 */

package com.cqsynet.swifi.util;

import java.util.Random;

import android.text.TextUtils;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.model.NewsItemInfo;

public class NewsAdUtil {
    private final String mPlanSplitChar = ",";  //轮播顺序的分隔符
	
	/**
     * 获取当前排序（plan字段）元素的索引
     * @return
     */
    public int getCurrentIndex(String id) {
    	int result = 0;
    	NewsItemInfo info = Globals.g_advMap.get(id);
    	if(TextUtils.isEmpty(info.plan)) {
			return 0;
		}
    	String[] planAry = info.plan.split(mPlanSplitChar);
    	int index = Globals.g_advIndexMap.get(id);
    	long refreshTime = Globals.g_advTimeMap.get(id);
		if (index == -1) {
			index = new Random().nextInt(planAry.length);
		}
		if (System.currentTimeMillis() - refreshTime > AppConstants.AD_REFRESH_INTEVAL) {
			refreshTime = System.currentTimeMillis();
			result = ++index % planAry.length;
		} else {
			result = index;
		}
		Globals.g_advIndexMap.put(id, result);
		Globals.g_advTimeMap.put(id, refreshTime);
        return Integer.parseInt(planAry[result]);
    }
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：邻近上网时间广播接收器
 *
 *
 * 创建标识：duxl 20141001
 */
package com.cqsynet.swifi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.util.StatisticsUtil;

public class StatisticsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(AppConstants.ACTION_STATISTICS.equals(intent.getAction())) {
			StatisticsUtil.submitStatistics(context); // 提交统计信息
			StatisticsUtil.submitLogs(context); // 提交日志
		}
	}
}

/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：统计工具类
 *
 *
 * 创建标识：xy 20150906
 */
package com.cqsynet.swifi.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.LogsRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.ResponseObject;
import com.cqsynet.swifi.model.StatisticsRequestBody;
import com.cqsynet.swifi.model.StatisticsResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.google.gson.Gson;

import java.util.ArrayList;

public class StatisticsUtil {
	/**
	 * @Description: 提交统计信息
	 * @return: void
	 */
	public static void submitStatistics(final Context context) {
		ArrayList<ArrayList<String>> subData = StatisticsDao.getStatistics(context);
		if (subData.size() == 0) {
			return;
		}
		StatisticsRequestBody requestBody = new StatisticsRequestBody();
		requestBody.statistics = subData;
		IResponseCallback callbackIf = new IResponseCallback() {
			@Override
			public void onResponse(String response) {
				if (response != null) {
					Gson gson = new Gson();
					StatisticsResponseObject responseObj = gson.fromJson(response, StatisticsResponseObject.class);
					ResponseHeader header = responseObj.header;
					if (header != null) {
						if (AppConstants.RET_OK.equals(header.ret)) {
							Log.i("submit Statistics", "success");
							StatisticsDao.deleteStatistics(context);
						} else {
							Log.e("submit Statistics", "fail");
						}
					}
				}
			}

			@Override
			public void onErrorResponse() {
				Log.e("submit Statistics", "fail");
			}
		};
		WebServiceIf.submitStatistics(context, requestBody, callbackIf);
	}
	
	/**
	 * @Description: 提交日志信息
	 * @return: void
	 */
	public static void submitLogs(final Context context) {
		//从temp.txt中读取日志
		String tempStr;
		final String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/" + AppConstants.CACHE_TEMP_FILE;
		tempStr = FileUtil.getString(filePath);
		if(TextUtils.isEmpty(tempStr)) {
			return;
		} 
		LogsRequestBody requestBody = new LogsRequestBody();
		requestBody.logs = tempStr;
		IResponseCallback callbackIf = new IResponseCallback() {
			@Override
			public void onResponse(String response) {
				if (response != null) {
					Gson gson = new Gson();
					ResponseObject responseObj = gson.fromJson(response, ResponseObject.class);
					ResponseHeader header = responseObj.header;
					if (header != null) {
						if (AppConstants.RET_OK.equals(header.ret)) {
							Log.i("submit logs", "success");
							//删除temp.txt
							FileUtil.deleteFile(filePath);
						} else {
							Log.e("submit logs", "fail");
						}
					}
				}
			}

			@Override
			public void onErrorResponse() {
                Log.e("submit logs", "fail");
			}
		};
		WebServiceIf.submitLogs(context, requestBody, callbackIf);
	}
	
	/**
	 * 开始提交统计信息
	 */
	public static void startSubmitStatistics(Context context) {
    	Intent intent = new Intent(AppConstants.ACTION_STATISTICS);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        long firstime = SystemClock.elapsedRealtime(); //开始时间
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, AppConstants.SUBMIT_STATISTICS_INTEVAL, pi); //每隔2分钟发送广播
    }
	
	/**
	 * 取消提交统计信息
	 */
	public static void stopSubmitStatistics(Context context) {
		Intent intent = new Intent(AppConstants.ACTION_STATISTICS);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pi);
	}
}

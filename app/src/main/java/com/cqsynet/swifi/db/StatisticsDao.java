/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：统计接口工具类
 *
 *
 * 创建标识：xy 20150813
 */
package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

public class StatisticsDao {

	/**
	 * 保存统计信息(除开app下载统计)
	 * 
	 * @param context
	 * @param type  统计类型
	 * @param value  根据统计类型,值分为id或者url
     *
	 */
	public static void saveStatistics(Context context, String type, String value) {
		if(context == null || value == null || type == null) {
			return;
		}
		StatisticsDBHelper db = new StatisticsDBHelper(context);
		SQLiteDatabase sdb = db.getWritableDatabase();
		// 通过统计类型和id值确定一条统计数据的唯一性
		Cursor c = sdb.query(StatisticsDBHelper.TABLE, null, StatisticsDBHelper.COL_TYPE + "=? and " +StatisticsDBHelper.COL_VALUE + "=?", new String[] { type, value }, null,
				null, null, null);
		ContentValues cv = new ContentValues();
		cv.put(StatisticsDBHelper.COL_TYPE, type);
		cv.put(StatisticsDBHelper.COL_VALUE, value);
		String clickCount = null;
		if (c.getCount() != 0) { // 同一统计类型，修改
			c.moveToFirst();
			clickCount = String.valueOf(Integer.parseInt(c.getString(c.getColumnIndex(StatisticsDBHelper.COL_COUNT))) + 1); // 点击次数加1
			cv.put(StatisticsDBHelper.COL_COUNT, clickCount);
			sdb.update(StatisticsDBHelper.TABLE, cv, StatisticsDBHelper.COL_TYPE + "=? and " +StatisticsDBHelper.COL_VALUE + "=?", new String[] { type, value });
		} else { // 不同统计类型，新增
			cv.put(StatisticsDBHelper.COL_COUNT, "1");
			sdb.insert(StatisticsDBHelper.TABLE, null, cv);
		}
		c.close();
		sdb.close();
		db.close();
	}

    /**
     * 统计页面的访问信息
     * @param context
     * @param value
     * @param startTime
     * @param endTime
     */
    public static void saveWebVisitStatistics(Context context, String value, String startTime, String endTime) {
        if(TextUtils.isEmpty(value)) {
            return;
        }
        StatisticsDBHelper db = new StatisticsDBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        if(!TextUtils.isEmpty(startTime)) {
            ContentValues cv = new ContentValues();
            cv.put(StatisticsDBHelper.COL_TYPE, "url");
            cv.put(StatisticsDBHelper.COL_VALUE, value);
            cv.put(StatisticsDBHelper.COL_START_TIME, startTime);
            cv.put(StatisticsDBHelper.COL_END_TIME, endTime);
            //开始时间不为空,表示这是一次新的访问
            sdb.insert(StatisticsDBHelper.TABLE, null, cv);
        } else if(!TextUtils.isEmpty(endTime)){
            //开始时间为空,结束时间不为空,表示这是一次访问的结束,若能找到该次的访问记录,则更新结束时间.否则说明统计信息已经提交,不做操作
            Cursor c = sdb.query(StatisticsDBHelper.TABLE, null, StatisticsDBHelper.COL_TYPE + "=? and " +StatisticsDBHelper.COL_VALUE + "=?", new String[] { "url", value }, null, null, null, null);
            if(c.getCount() != 0) {
                c.moveToLast();
                String lastStartTime = c.getString(c.getColumnIndex(StatisticsDBHelper.COL_START_TIME));
                if(!TextUtils.isEmpty(lastStartTime)) {
                    ContentValues cv = new ContentValues();
                    cv.put(StatisticsDBHelper.COL_END_TIME, endTime);
                    String id = c.getString(c.getColumnIndex(StatisticsDBHelper.COL_ID));
                    sdb.update(StatisticsDBHelper.TABLE, cv, StatisticsDBHelper.COL_ID + "=?", new String[] { id });
                }
            }
            c.close();
        }
        sdb.close();
        db.close();
//        getStatistics(context);
    }

	/**
	 * 删除表里所有的数据
	 * 
	 */
	public static void deleteStatistics(Context context) {
		StatisticsDBHelper db = new StatisticsDBHelper(context);
		SQLiteDatabase sdb = db.getWritableDatabase();
		sdb.delete(StatisticsDBHelper.TABLE, null, null);
		Log.i("statistic_delete", "success");
		sdb.close();
		db.close();
	}

	/**
	 * 获取统计的数据
	 */
	public static ArrayList<ArrayList<String>> getStatistics(Context context) {
		ArrayList<ArrayList<String>> statistics = new ArrayList<ArrayList<String>>();
		StatisticsDBHelper db = new StatisticsDBHelper(context);
		SQLiteDatabase sdb = db.getReadableDatabase();
		Cursor c = sdb.query(StatisticsDBHelper.TABLE, null, null, null, null, null, null, null);
		while (c.moveToNext()) {
			ArrayList<String> array = new ArrayList<String>();
			String type = c.getString(c.getColumnIndex(StatisticsDBHelper.COL_TYPE));
			if(type.equals("url")) {
				array.add(type);
				array.add(c.getString(c.getColumnIndex(StatisticsDBHelper.COL_VALUE)));
				array.add(c.getString(c.getColumnIndex(StatisticsDBHelper.COL_START_TIME)));
				array.add(c.getString(c.getColumnIndex(StatisticsDBHelper.COL_END_TIME)));
			} else {
				array.add(c.getString(c.getColumnIndex(StatisticsDBHelper.COL_TYPE)));
				array.add(c.getString(c.getColumnIndex(StatisticsDBHelper.COL_VALUE)));
				array.add(c.getString(c.getColumnIndex(StatisticsDBHelper.COL_COUNT)));
			}
			statistics.add(array);
		}
		c.close();
		sdb.close();
		db.close();
		
		return statistics;
	}
}

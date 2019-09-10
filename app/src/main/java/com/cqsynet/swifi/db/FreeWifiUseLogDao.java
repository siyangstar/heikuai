/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：免费WiFi使用分钟记录数据访问Dao
 *
 *
 * 创建标识：duxl 20140929
 */
package com.cqsynet.swifi.db;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cqsynet.swifi.Globals;

public class FreeWifiUseLogDao {

	private static FreeWifiUseLogDao mWifiUseLogDao;
	private Context mContext;

	public FreeWifiUseLogDao(Context context) {
		mContext = context;
	}
	
	public static FreeWifiUseLogDao getInstance(Context context) {
		if(mWifiUseLogDao == null) {
			mWifiUseLogDao = new FreeWifiUseLogDao(context);
		}
		return mWifiUseLogDao;
	}

//	/**
//	 * 保存记录
//	 * @param date 日期
//	 * @param appendTime 累计秒数
//	 */
//	public void saveLog(String date, int appendTime) {
//		DBHelper db = new DBHelper(mContext);
//		Map<String, Integer> map = getLog(date);
//		if(map != null) { // 修改记录
//			String sql = String.format("update freeWifiUseLog set todayUse=todayUse+%d where date='%s'", appendTime, date);
//			db.getWritableDatabase().execSQL(sql);
//			
//		} else { // 创建记录
//			ContentValues contentValues = new ContentValues();
//			contentValues.put("date", date);
//			contentValues.put("todayUse", appendTime);
//			contentValues.put("totalFree", Globals.sGetTime);
//			db.getWritableDatabase().insert("freeWifiUseLog", null, contentValues);
//		}
//		db.close();
//	}
	
	/**
	 * 初始化某天的使用时间
	 * @param date
	 */
	public void initTodayUse(String date) {
		DBHelper db = new DBHelper(mContext);
		Map<String, Integer> map = getLog(date);
		if(map == null) { // 新建记录
			ContentValues contentValues = new ContentValues();
			contentValues.put("date", date);
			contentValues.put("todayUse", 0);
			contentValues.put("totalFree", Globals.g_getTime);
			db.getWritableDatabase().insert("freeWifiUseLog", null, contentValues);
		}
		db.close();
	}
	
	
	/**
	 * 更新某天的使用时间
	 * @param date
	 */
	public void updateTodayUse(String date) {
		DBHelper db = new DBHelper(mContext);
		Map<String, Integer> map = getLog(date);
		if(map == null) { // 新建记录
			ContentValues contentValues = new ContentValues();
			contentValues.put("date", date);
			contentValues.put("todayUse", 0);
			contentValues.put("totalFree", Globals.g_getTime);
			db.getWritableDatabase().insert("freeWifiUseLog", null, contentValues);
		} else { // 修改
			ContentValues contentValues = new ContentValues();
			contentValues.put("todayUse", map.get("todayUse") + 60);
			db.getWritableDatabase().update("freeWifiUseLog", contentValues, "date=?", new String[]{ date });
		}
		db.close();
	}
	
	/**
	 * 更新某天的使用时间
	 * @param date
	 */
	public void updateTodayUse(String date, int todayUse) {
		DBHelper db = new DBHelper(mContext);
		Map<String, Integer> map = getLog(date);
		if(map == null) { // 新建记录
			ContentValues contentValues = new ContentValues();
			contentValues.put("date", date);
			contentValues.put("todayUse", todayUse);
			contentValues.put("totalFree", Globals.g_getTime);
			db.getWritableDatabase().insert("freeWifiUseLog", null, contentValues);
		} else { // 修改
			ContentValues contentValues = new ContentValues();
			contentValues.put("todayUse", todayUse);
			db.getWritableDatabase().update("freeWifiUseLog", contentValues, "date=?", new String[]{ date });
		}
		db.close();
	}
	
//	/**
//	 * 重置本次可用时间
//	 * @param date
//	 */
//	public void resetTotalFree(String date) {
//		DBHelper db = new DBHelper(mContext);
//		Map<String, Integer> map = getLog(date);
//		if(map != null) {
//			ContentValues contentValues = new ContentValues();
//			contentValues.put("totalFree", 0);
//			db.getWritableDatabase().update("freeWifiUseLog", contentValues, "date=?", new String[] {date});
//		}
//		db.close();
//	}
	
	/**
	 * 查询记录
	 * @param date 日期
	 * @return
	 */
	public Map<String, Integer> getLog(String date) {
		DBHelper db = new DBHelper(mContext);
		Map<String, Integer> map = null;
		String sql = "select todayUse, totalFree from freeWifiUseLog where date=?";
		Cursor cur = null;
		
		try {
			cur = db.getReadableDatabase().rawQuery(sql, new String[]{date});
			if(cur.moveToFirst()) {
				map = new HashMap<String, Integer>();
				for(String col : cur.getColumnNames()) {
					map.put(col, cur.getInt(cur.getColumnIndex(col)));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(cur != null) {
				cur.close();
			}
			db.close();
		}
		
		return map;
	}
	
//	/**
//	 * 查询记录
//	 * @return 返回Map集合，key=日期 value=对应key当日所使用的Wifi秒数
//	 */
//	public Map<String, Integer> getAllLog() {
//		DBHelper db = new DBHelper(mContext);
//		Map<String, Integer> map = null;
//		String sql = "select date, todayUse from freeWifiUseLog";
//		Cursor cur = null;
//		
//		try {
//			cur = db.getReadableDatabase().rawQuery(sql, null);
//			map = new HashMap<String, Integer>();
//			while(cur.moveToNext()) {
//				map.put(cur.getString(0), cur.getInt(1));
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if(cur != null) {
//				cur.close();
//			}
//			db.close();
//		}
//		
//		return map;
//	}
	
//	/**
//	 * 获取累计使用时长（单位秒）
//	 * @return
//	 */
//	public int getTotalUseTime() {
//		DBHelper db = new DBHelper(mContext);
//		int minute = 0;
//		String sql = "select sum(todayUse) from freeWifiUseLog";
//		Cursor cur = null;
//		
//		try {
//			cur = db.getReadableDatabase().rawQuery(sql, null);
//			if(cur.moveToFirst()) {
//				minute = cur.getInt(0);
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if(cur != null) {
//				cur.close();
//			}
//			db.close();
//		}
//		return minute;
//	}
	
//	/**
//	 * 关闭数据库
//	 */
//	public void closeDB() {
////		if(mReadableDB != null && mReadableDB.isOpen()) {
////			mReadableDB.close();
////			mReadableDB = null;
////		}
//		
//		if(mWritableDB != null && mWritableDB.isOpen()) {
//			mWritableDB.close();
//			mWritableDB = null;
//		}
//		
//		if(mOpenHelper != null) {
//			mOpenHelper.close();
//		}
//	}
}

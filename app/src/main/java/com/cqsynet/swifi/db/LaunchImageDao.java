/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：启动图片数据访问Dao
 *
 *
 * 创建标识：duxl 2015-1-8
 */
package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqsynet.swifi.model.LaunchImageObject;
import com.cqsynet.swifi.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LaunchImageDao {

	private DBHelper mOpenHelper;
	private SQLiteDatabase mReadableDB;
	private SQLiteDatabase mWritableDB;
	private Context mContext;

	public LaunchImageDao(Context context) {
		mContext = context;
		mOpenHelper = new DBHelper(context);
		mReadableDB = mOpenHelper.getReadableDatabase();
		mWritableDB = mOpenHelper.getWritableDatabase();
	}
	
	/**
	 * 清空所有数据
	 */
	public void clear() {
		mWritableDB.execSQL("delete from launchImg");
	}

	/**
	 * 保存记录
	 * @param launchImgs
   */
	public void save(List<LaunchImageObject> launchImgs) {
		clear();
		for(LaunchImageObject launchImg : launchImgs) {
			ContentValues contentValues = new ContentValues();
			launchImg.localFileName = "launch-"+String.valueOf(launchImg.url.hashCode());
			if(!launchImg.exists(mContext)) {
				contentValues.put("localFileName", launchImg.localFileName);
				contentValues.put("advId", launchImg.advId);
				contentValues.put("url", launchImg.url);
				contentValues.put("jumpUrl", launchImg.jumpUrl);
				contentValues.put("startDate", launchImg.startDate);
				contentValues.put("endDate", launchImg.endDate);
				mWritableDB.insert("launchImg", null, contentValues);
			}
		}
	}
	
	/**
	 * 获取当前需要显示的图片(随机获取)
	 * @return
	 */
	public LaunchImageObject getRandomImage() {
		List<LaunchImageObject> list = new ArrayList<LaunchImageObject>();
		String currentTime = DateUtil.formatTime(System.currentTimeMillis(), "yyyy-MM-dd HH");
		String where = String.format("startDate <= '%s' and endDate > '%s'", currentTime, currentTime);
		Cursor cur = mReadableDB.query("launchImg", null, where, null, null, null, "_id asc");
		if(cur != null) {
			while(cur.moveToNext()) {
				LaunchImageObject imageObj = new LaunchImageObject();
				imageObj.localFileName = cur.getString(cur.getColumnIndex("localFileName"));
				imageObj.advId = cur.getString(cur.getColumnIndex("advId"));
				imageObj.jumpUrl = cur.getString(cur.getColumnIndex("jumpUrl"));
				imageObj.url = cur.getString(cur.getColumnIndex("url"));
				imageObj.startDate = cur.getString(cur.getColumnIndex("startDate"));
				imageObj.endDate = cur.getString(cur.getColumnIndex("endDate"));
				list.add(imageObj);
			}
			cur.close();
		}
		int size = list.size();
		if(size == 0) {
			return null;
		} else {
			Random random = new Random();
			int index = random.nextInt(size);
			return list.get(index);
		}
	}
	
	/**
	 * 关闭数据库
	 */
	public void closeDB() {
		if(mReadableDB != null && mReadableDB.isOpen()) {
			mReadableDB.close();
			mReadableDB = null;
		}
		
		if(mWritableDB != null && mWritableDB.isOpen()) {
			mWritableDB.close();
			mWritableDB = null;
		}
		
		if(mOpenHelper != null) {
			mOpenHelper.close();
		}
	}
}

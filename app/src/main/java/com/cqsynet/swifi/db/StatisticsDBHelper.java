/*
 * Copyright (C) 2016 重庆尚渝
 * 版权所有
 *
 * 功能描述：数据库工具类
 *
 *
 * 创建标识：xy 20160217
 */
package com.cqsynet.swifi.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StatisticsDBHelper extends SQLiteOpenHelper {

	// 统计数据库名
	public static final String DATABASE_NAME = "statistics.db";
	// 统计数据库版本
	private final static int DATABASE_VERSION = 5;

	// 统计数据表名
	public static final String TABLE = "statisticsCache";

	// 统计数据字段
	public static final String COL_ID = "id";
	public static final String COL_TYPE = "type";
	public static final String COL_VALUE = "value";
	public static final String COL_COUNT = "count";
	public static final String COL_START_TIME = "startTime";
	public static final String COL_END_TIME = "endTime";
	

	// 创建统计数据表的语句
	public static final String CREATE_TABLE_STATISTICS = "CREATE TABLE IF NOT EXISTS " + TABLE + "("
			+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ COL_TYPE + " TEXT,"
			+ COL_VALUE + " TEXT,"
			+ COL_COUNT + " TEXT,"
			+ COL_START_TIME + " TEXT,"
			+ COL_END_TIME + " TEXT)";
		
	public static final String DROP_TABLE_STATISTICS = "DROP TABLE IF EXISTS " + TABLE;
	
	public StatisticsDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DROP_TABLE_STATISTICS);
		createTable(db);
	}

	public void createTable(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_STATISTICS);
	}
}
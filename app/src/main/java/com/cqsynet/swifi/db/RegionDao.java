/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：地区数据库Dao
 *
 *
 * 创建标识：duxl 20141225
 */
package com.cqsynet.swifi.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RegionDao {

	SQLiteDatabase db;
	
	/**
	 * 记得关闭DB {@link #closeDB()}
	 * @param context
	 */
	public RegionDao(Context context) {
		this.db = RegionDBHelper.getInstance(context);
	}
	
	public  void closeDB() {
		if(db != null && db.isOpen()) {
			db.close();
		}
	}
	
	/**
	 * 地区编码
	 * @param code 地区编码
	 * @return 
	 */
	public List<KeyValue> getRegionByCode(String code) {
		// sql = select * from region where id in('45', '4501', '450105');
		List<KeyValue> resultData = new ArrayList<RegionDao.KeyValue>();
		String data = "";
		String sql = "select * from region where id in('province', 'city', 'county')";
		String provinceCode = code.substring(0, 2);
		sql = sql.replace("province", provinceCode);
		String cityCode = null;
		if(code.length() >= 4) {
			cityCode = code.substring(0, 4);
			sql = sql.replace("city", cityCode);
		}
		String countyCode = null;
		if(code.length() == 6) {
			countyCode = code.substring(0, 6);
			sql = sql.replace("county", countyCode);
		}
		
		Cursor cur = db.rawQuery(sql, null);
		while(cur.moveToNext()) {
			KeyValue keyValue = new KeyValue();
			keyValue.key = cur.getString(cur.getColumnIndex("id"));
			keyValue.value = cur.getString(cur.getColumnIndex("name"));
			resultData.add(keyValue);
		}
		cur.close();
		
		return resultData;
	}
	
	/**
	 * 获取地区列表
	 * @param code 父级地区code
	 * @return
	 */
	public List<KeyValue> getRegionByParentCode(String code) {
		List<KeyValue> resultData = new ArrayList<KeyValue>();
		Cursor cur = db.rawQuery("select * from region where parentId=" + code, null);
		while(cur.moveToNext()) {
			KeyValue keyValue = new KeyValue();
			keyValue.key = cur.getString(cur.getColumnIndex("id"));
			keyValue.value = cur.getString(cur.getColumnIndex("name"));
			resultData.add(keyValue);
		}
		
		return resultData;
	}
	
	public static class KeyValue {
		public String key;
		public String value;
	}
}

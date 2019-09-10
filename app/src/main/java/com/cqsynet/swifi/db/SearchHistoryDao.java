package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqsynet.swifi.model.SearchHistoryInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SearchHistoryDao {
	private static SearchHistoryDao mSearchHistoryDao;
	private static DBHelper mDbHelper;
	private static SQLiteDatabase mReadableDB;
	private static SQLiteDatabase mWriteableDB;
	private Context mContext;

	public SearchHistoryDao(Context context) {
		mContext = context;
		mDbHelper = new DBHelper(context);
		mReadableDB = mDbHelper.getReadableDatabase();
		mWriteableDB = mDbHelper.getWritableDatabase();
	}

	public static SearchHistoryDao getInstance(Context context) {
		if (mSearchHistoryDao == null) {
			mSearchHistoryDao = new SearchHistoryDao(context);
		} else {
			if (mDbHelper == null) {
				mDbHelper = new DBHelper(context);
			}
			if (mReadableDB == null || !mReadableDB.isOpen()) {
				mReadableDB = mDbHelper.getReadableDatabase();
			}
			if(mWriteableDB == null || !mWriteableDB.isOpen()) {
				mWriteableDB = mDbHelper.getWritableDatabase();
			}
		}
		return mSearchHistoryDao;
	}


	/**
	 * 获取指定用户的所有消息
	 * 
	 * @param account
	 * @return
	 */
	public ArrayList<SearchHistoryInfo> getSearchHistory (String account) {
		ArrayList<SearchHistoryInfo> mArrayList = new ArrayList<SearchHistoryInfo>();
		Cursor c = mReadableDB.query(DBHelper.SEARCH_TABLE, null, " userAccount=? ", new String[] { account }, null, null,
				"_id desc limit 0,4");
		SearchHistoryInfo info = null;
		if (c.getCount() > 0) {
			while (c.moveToNext()) {
				info = new SearchHistoryInfo();
				info.id = c.getInt(c.getColumnIndex(DBHelper.SEARCH_COL_ID));
				info.account = account;
				info.content = c.getString(c.getColumnIndex(DBHelper.SEARCH_COL_CONTENT));
				info.creatTime = c.getString(c.getColumnIndex(DBHelper.SEARCH_COL_TIME));
				mArrayList.add(info);
			}
		}
		c.close();
		closeDb();
		return mArrayList;
	}

	/**
	 * 删除用户指定搜索历史
	 * 
	 * @param content 搜索内容
	 * @param account 账户
	 */
	public void deleteSearch(String content, String account) {
		mWriteableDB.delete(DBHelper.SEARCH_TABLE, "userAccount=? and searchContent=?", new String[] { account, content });
		closeDb();
	}

	/**
	 * 查询是否存在此条搜索内容
	 * @param content
	 * @return
	 */
	public boolean isSearchSave (String content) {
		Cursor c = mReadableDB.query(DBHelper.SEARCH_TABLE, null, "searchContent=?", new String[] {content}, null, null, null);
		if (c.getCount() > 0) {
			c.close();
			closeDb();
			return true;
		}
		c.close();
		closeDb();
		return false;
	}
	
	/**
	 * 清空搜索历史
	 * 
	 * @param account
	 */
	public void deleteHistoryAll(String account) {
		mWriteableDB.delete(DBHelper.SEARCH_TABLE, "userAccount=?", new String[] { account });
		closeDb();
	}

	/**
	 * 插入一条搜索历史
	 * 
	 * @param content 内容
	 */
	public void insertSearch(String content) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String createTime = sDateFormat.format(new Date()).toString();
		String useraccount = SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.PHONE_NUM);
		if (useraccount != null) {
			ContentValues cv = new ContentValues();
			cv.put(DBHelper.SEARCH_COL_ACCOUNT, useraccount);
			cv.put(DBHelper.SEARCH_COL_CONTENT, content);
			cv.put(DBHelper.SEARCH_COL_TIME, createTime);
			mWriteableDB.insert(DBHelper.SEARCH_TABLE, null, cv);
		}
		closeDb();
	}

	/**
	 * 关闭数据库
	 */
	private void closeDb() {
		if (mReadableDB != null && mReadableDB.isOpen()) {
			mReadableDB.close();
		}
		if (mWriteableDB != null && mWriteableDB.isOpen()) {
			mWriteableDB.close();
		}
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}
}

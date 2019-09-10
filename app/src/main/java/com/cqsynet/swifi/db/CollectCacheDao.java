/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：收藏接口Dao
 *
 *
 * 创建标识：xy 20150813
 */
package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqsynet.swifi.model.CollectInfo;
import com.cqsynet.swifi.model.CollectRemoveInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

import java.util.ArrayList;
import java.util.List;

public class CollectCacheDao {

    /**
     * 数据库获取收藏缓存数据
     */
    public static ArrayList<CollectInfo> getCollect(Context context) {
        String useraccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
        ArrayList<CollectInfo> collectList = new ArrayList<>();
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLLECT_COL_ACCOUNT, useraccount);
        Cursor c = sdb.query(DBHelper.COLLECT_TABLE, null, "account=?",
                new String[]{useraccount}, null, null, "_id desc");
        if (c.getCount() != 0) {
            CollectInfo info;
            while (c.moveToNext()) {
                info = new CollectInfo();
                info.type = c.getString(c.getColumnIndex(DBHelper.COLLECT_COL_TYPE));
                info.id = c.getString(c.getColumnIndex(DBHelper.COLLECT_COL_NEWS_ID));
                info.title = c.getString(c.getColumnIndex(DBHelper.COLLECT_COL_TITLE));
                info.url = c.getString(c.getColumnIndex(DBHelper.COLLECT_COL_URL));
                info.image = c.getString(c.getColumnIndex(DBHelper.COLLECT_COL_IMAGE));
                info.source = c.getString(c.getColumnIndex(DBHelper.COLLECT_COL_SOURCE));
                info.timestamp = c.getString(c.getColumnIndex(DBHelper.COLLECT_COL_TIMESTAMP));
                collectList.add(info);
            }
        }
        c.close();
        sdb.close();
        db.close();
        return collectList;
    }

    /**
     * 添加收藏数据
     */
    public static void insertData(Context context, String type, String id, String title,
                                  String url, String image, String source, String timestamp) {
        String useraccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLLECT_COL_TYPE, type);
        cv.put(DBHelper.COLLECT_COL_NEWS_ID, id);
        cv.put(DBHelper.COLLECT_COL_TITLE, title);
        cv.put(DBHelper.COLLECT_COL_URL, url);
        cv.put(DBHelper.COLLECT_COL_IMAGE, image);
        cv.put(DBHelper.COLLECT_COL_SOURCE, source);
        cv.put(DBHelper.COLLECT_COL_TIMESTAMP, timestamp);
        cv.put(DBHelper.COLLECT_COL_ACCOUNT, useraccount);
        sdb.insert(DBHelper.COLLECT_TABLE, null, cv);
        sdb.close();
        db.close();
    }

    public static void insertData(Context context, CollectInfo collectInfo) {
        String useraccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLLECT_COL_TYPE, collectInfo.type);
        cv.put(DBHelper.COLLECT_COL_NEWS_ID, collectInfo.id);
        cv.put(DBHelper.COLLECT_COL_TITLE, collectInfo.title);
        cv.put(DBHelper.COLLECT_COL_URL, collectInfo.url);
        cv.put(DBHelper.COLLECT_COL_IMAGE, collectInfo.image);
        cv.put(DBHelper.COLLECT_COL_SOURCE, collectInfo.source);
        cv.put(DBHelper.COLLECT_COL_TIMESTAMP, collectInfo.timestamp);
        cv.put(DBHelper.COLLECT_COL_ACCOUNT, useraccount);
        sdb.insert(DBHelper.COLLECT_TABLE, null, cv);
        sdb.close();
        db.close();
    }

    /**
     * 根据用户名和资讯id删除某条收藏数据
     */
    public static void deleteData(Context context, String id) {
        String useraccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLLECT_COL_NEWS_ID, id);
        cv.put(DBHelper.COLLECT_COL_ACCOUNT, useraccount);
        sdb.delete(DBHelper.COLLECT_TABLE, "id=? and account=?", new String[]{id, useraccount});
        sdb.close();
        db.close();
    }

    public static void deleteDataByUrl(Context context, String url) {
        String userAccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        sdb.delete(DBHelper.COLLECT_TABLE, "account=? and url=?", new String[]{userAccount, url});
        sdb.close();
        db.close();
    }

    public static void deleteCollects(Context context, List<CollectRemoveInfo> collectRemoveInfos) {
        String userAccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        for (CollectRemoveInfo info : collectRemoveInfos) {
            sdb.delete(DBHelper.COLLECT_TABLE, "account=? and url=?", new String[]{userAccount, info.url});
        }
        sdb.close();
        db.close();
    }

    /**
     * 删除表里所有数据
     */
    public static void deleteAll(Context context) {
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        sdb.delete(DBHelper.COLLECT_TABLE, null, null);
        sdb.close();
        db.close();
    }

    /**
     * 根据id查找对应收藏数据
     *
     * @return true:表示此条数据存在，false:不存在
     */
    public static boolean queryById(Context context, String id) {
        String useraccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLLECT_COL_NEWS_ID, id);
        cv.put(DBHelper.COLLECT_COL_ACCOUNT, useraccount);
        Cursor c = sdb.query(DBHelper.COLLECT_TABLE, null, "account=? and id=?", new String[]{useraccount, id}, null, null, null);
        int num = c.getCount();
        c.close();
        sdb.close();
        db.close();
        return num != 0;
    }

    public static boolean queryByUrl(Context context, String url) {
        String userAccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sdb = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLLECT_COL_ACCOUNT, userAccount);
        cv.put(DBHelper.COLLECT_COL_URL, url);
        Cursor cursor = sdb.query(DBHelper.COLLECT_TABLE, null, "account=? and url=?", new String[]{userAccount, url}, null, null, null);
        int num = cursor.getCount();
        cursor.close();
        sdb.close();
        db.close();
        return num != 0;
    }

    public static ArrayList<CollectInfo> queryByTitle(Context context, String title) {
        ArrayList<CollectInfo> collectList = new ArrayList<>();
        String userAccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLLECT_COL_TITLE, title);
        cv.put(DBHelper.COLLECT_COL_ACCOUNT, userAccount);
        Cursor cursor = db.query(DBHelper.COLLECT_TABLE, null, "title LIKE ? ",
                new String[]{"%" + title + "%"}, null, null, "_id desc");
        if (cursor.getCount() != 0) {
            CollectInfo info;
            while (cursor.moveToNext()) {
                info = new CollectInfo();
                info.type = cursor.getString(cursor.getColumnIndex(DBHelper.COLLECT_COL_TYPE));
                info.id = cursor.getString(cursor.getColumnIndex(DBHelper.COLLECT_COL_NEWS_ID));
                info.title = cursor.getString(cursor.getColumnIndex(DBHelper.COLLECT_COL_TITLE));
                info.url = cursor.getString(cursor.getColumnIndex(DBHelper.COLLECT_COL_URL));
                info.image = cursor.getString(cursor.getColumnIndex(DBHelper.COLLECT_COL_IMAGE));
                info.source = cursor.getString(cursor.getColumnIndex(DBHelper.COLLECT_COL_SOURCE));
                info.timestamp = cursor.getString(cursor.getColumnIndex(DBHelper.COLLECT_COL_TIMESTAMP));
                collectList.add(info);
            }
        }
        cursor.close();
        db.close();
        helper.close();
        return collectList;
    }
}

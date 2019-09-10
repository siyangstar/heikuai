/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：网址黑白名单数据访问Dao
 *
 *
 * 创建标识：zhaosy 20170623
 */
package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqsynet.swifi.model.BlackUrlObject;

import java.util.ArrayList;

public class BlackWhiteUrlDao {

    /**
     * 清空所有数据
     */
    public static void clear(Context context) {
        DBHelper db = new DBHelper(context);
        db.getWritableDatabase().execSQL("delete from " + DBHelper.BLACK_WHITE_URL_TABLE);
        db.close();
    }

    /**
     * 保存白名单记录
     *
     * @param whiteList
     */
    public static void saveWhiteList(Context context, ArrayList<String> whiteList) {
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        for (String domain : whiteList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.BWURL_COL_TYPE, "white");
            contentValues.put(DBHelper.BWURL_COL_URL, domain);
            sqLiteDatabase.insert(DBHelper.BLACK_WHITE_URL_TABLE, null, contentValues);
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        db.close();
    }

    /**
     * 保存黑名单记录
     *
     * @param blackList
     */
    public static void saveBlackList(Context context, ArrayList<BlackUrlObject> blackList) {
        DBHelper db = new DBHelper(context);
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        for (BlackUrlObject blackUrlObject : blackList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.BWURL_COL_TYPE, "black");
            contentValues.put(DBHelper.BWURL_COL_URL, blackUrlObject.url);
            contentValues.put(DBHelper.BWURL_COL_ADV, blackUrlObject.showAdv);
            sqLiteDatabase.insert(DBHelper.BLACK_WHITE_URL_TABLE, null, contentValues);
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        db.close();
    }

    /**
     * 获取白名单记录
     *
     * @return
     */
    public static ArrayList<String> getWhiteList(Context context) {
        DBHelper db = new DBHelper(context);
        ArrayList<String> list = new ArrayList<>();
        Cursor cur = db.getReadableDatabase().query(DBHelper.BLACK_WHITE_URL_TABLE, null, DBHelper.BWURL_COL_TYPE + "=?", new String[]{"white"}, null, null, null);
        if (cur != null) {
            while (cur.moveToNext()) {
                list.add(cur.getString(cur.getColumnIndex(DBHelper.BWURL_COL_URL)));
            }
            cur.close();
        }
        db.close();
        return list;
    }

    /**
     * 获取黑名单记录
     *
     * @return
     */
    public static ArrayList<BlackUrlObject> getBlackList(Context context) {
        DBHelper db = new DBHelper(context);
        ArrayList<BlackUrlObject> blackUrlObjects = new ArrayList<>();
        Cursor cur = db.getReadableDatabase().query(DBHelper.BLACK_WHITE_URL_TABLE, null, DBHelper.BWURL_COL_TYPE + "=?", new String[]{"black"}, null, null, null);
        if (cur != null) {
            while (cur.moveToNext()) {
                BlackUrlObject blackUrlObject = new BlackUrlObject();
                blackUrlObject.url = cur.getString(cur.getColumnIndex(DBHelper.BWURL_COL_URL));
                blackUrlObject.showAdv = cur.getString(cur.getColumnIndex(DBHelper.BWURL_COL_ADV));
                blackUrlObjects.add(blackUrlObject);
            }
            cur.close();
        }
        db.close();
        return blackUrlObjects;
    }
}

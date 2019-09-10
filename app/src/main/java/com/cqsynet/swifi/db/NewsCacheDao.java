/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻列表缓存数据存取对象
 *
 *
 * 创建标识：luchaowei 20150125
 */

package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cqsynet.swifi.model.NewsCacheObject;
import com.cqsynet.swifi.model.NewsListResponseBody;
import com.cqsynet.swifi.model.NewsTopicResponseBody;
import com.google.gson.Gson;

public class NewsCacheDao {
    private static DBHelper mDbHelper;
    private static SQLiteDatabase mReadableDB;
    private static SQLiteDatabase mWriteableDB;
    private static NewsCacheDao mInstance ;

    public NewsCacheDao(Context ctx) {
        mDbHelper = new DBHelper(ctx);
        mReadableDB = mDbHelper.getReadableDatabase();
        mWriteableDB = mDbHelper.getWritableDatabase();
    }

    /**
     * 获取缓存对象
     * @param ctx 
     * @return
     */
    public static NewsCacheDao getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new NewsCacheDao(ctx);
        }
        return mInstance;
    }

    /**
     * 保存当前频道的新闻列表到数据库
     * @param date 刷新时间
     * @param channelId 频道id
     * @param body 列表内容
     */
    public void saveNews(long date, String channelId, Object body) {
        String content;
        String sqlString  = "select * from newsCache where channelId = ?";
        Cursor cursor =  mReadableDB.rawQuery(sqlString, new String[]{channelId});
        Gson gson = new Gson();
        content = gson.toJson(body);
        ContentValues values = new ContentValues();
        values.put("channelId", channelId);
        values.put("date", date);
        values.put("content", content);
      if (cursor.getCount() > 0) {
          mWriteableDB.update("newsCache", values, "channelId = ?", new String[]{channelId});
      } else {
          mWriteableDB.insert("newsCache", null, values);
      }
      cursor.close();
}

    /**
     * 根据频道id获取缓存的新闻列表
     * @param channelId 频道id
     *   * @param isTopicNews true 获取频道新闻列表 ，false 获取普通新闻列表
     * @return NewsCacheObject 新闻列表的缓存数据对象
     */
    public NewsCacheObject getNews(String channelId, boolean isTopicNews) {
        String sql = "select * from newsCache where channelId=?";
        if (TextUtils.isEmpty(channelId)) {
            return null;
        }
        Cursor cursor = mReadableDB.rawQuery(sql, new String[]{channelId});
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
 
        NewsCacheObject object = new NewsCacheObject();
        cursor.moveToFirst();
        object.channelId = channelId;
        int index = cursor.getColumnIndex("date");
        object.date = cursor.getLong(index);
        index = cursor.getColumnIndex("content");
        String jsonContent = cursor.getString(index);
        Gson gson = new Gson();
        if (!isTopicNews) {
            // 反序列化普通新闻列表数据
            NewsListResponseBody body = gson.fromJson(jsonContent, NewsListResponseBody.class);
            object.topList = body.topList;
            object.newsList = body.newsList;
            object.newsListCount = body.newsListCount;
            object.serverDate = body.updateTime;
            object.floatingAdv = body.floatingAdv;
            object.navList = body.navList;
        } else {
            // 反序列化专题列表数据
            NewsTopicResponseBody body = gson.fromJson(jsonContent, NewsTopicResponseBody.class);
            object.imgUrl = body.imgUrl;
            object.summary = body.summary;
            object.newsList = body.newsList;
            object.newsListCount = body.newsListCount;
            object.topicTitle = body.topicTitle;
            object.shareContent = body.shareContent;
            object.shareUrl = body.shareUrl;
            object.sharePic = body.sharePic;
        }
        cursor.close();
        return object;
    }

    /**
     * 清除所有数据
     */
    public void clearCache() {
        mWriteableDB.execSQL("DELETE FROM newsCache;");
    }

    /**
     * 关闭数据库连接，关闭db
     */
    public void closeDb () {
        if (mReadableDB != null && mReadableDB.isOpen()) {
            mReadableDB.close();
            mReadableDB = null;
        }

        if (mWriteableDB != null && mWriteableDB.isOpen()) {
            mWriteableDB.close();
            mWriteableDB = null;
        }
 
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }
}

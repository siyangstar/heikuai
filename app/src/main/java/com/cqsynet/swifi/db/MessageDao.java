/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：消息的数据库操作。
 *
 *
 * 创建标识：XY 20160302
 */
package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqsynet.swifi.model.MessageInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageDao {

    private static MessageDao mMessageDao;
    private Context mContext;

    public MessageDao(Context context) {
        mContext = context;
    }

    public static MessageDao getInstance(Context context) {
        if (mMessageDao == null) {
            mMessageDao = new MessageDao(context);
        }
        return mMessageDao;
    }


    /**
     * 向数据库插入一条消息
     *
     * @param title   标题
     * @param content 内容
     */
    public void insertMsg(String msgId, String type, String title, String content, String url, String contentId) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String createTime = sDateFormat.format(new Date()).toString();
        String useraccount = SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.PHONE_NUM);
        if (useraccount == null) {
            useraccount = "";
        }
        if (url == null) {
            url = "";
        }
        DBHelper db = new DBHelper(mContext);
        SQLiteDatabase sdb = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.MESSAGE_COL_ACCOUNT, useraccount);
        cv.put(DBHelper.MESSAGE_COL_TITLE, title);
        cv.put(DBHelper.MESSAGE_COL_CONTENT, content);
        cv.put(DBHelper.MESSAGE_COL_TIME, createTime);
        cv.put(DBHelper.MESSAGE_COL_STATUS, "0");
        cv.put(DBHelper.MESSAGE_COL_URL, url);
        cv.put(DBHelper.MESSAGE_COL_TYPE, type);
        cv.put(DBHelper.MESSAGE_COL_MSGID, msgId);
        cv.put(DBHelper.MESSAGE_COL_CONTENTID, contentId);
        sdb.insert(DBHelper.MESSAGE_TABLE, null, cv);
        sdb.close();
        db.close();
    }

    /**
     * 获取消息总的数量
     *
     * @param account 用户账户(手机号)
     * @return 默认返回-1，表示查询失败
     */
    public int getMsgNum(String account) {
        DBHelper db = new DBHelper(mContext);
        SQLiteDatabase sdb = db.getReadableDatabase();
        Cursor c = sdb.query(DBHelper.MESSAGE_TABLE, null, " account=? ", new String[]{account}, null, null,
                null);
        if (c != null) {
            int num = c.getCount();
            return num;
        }
        c.close();
        sdb.close();
        db.close();
        return -1;
    }

    /**
     * 获取未读消息总数
     *
     * @param account
     * @return
     */
    public int getUnreadNum(String account) {
        DBHelper db = new DBHelper(mContext);
        SQLiteDatabase sdb = db.getReadableDatabase();
        String unRead = "0";
        Cursor c = sdb.query(DBHelper.MESSAGE_TABLE, null, " account=? and isRead=?", new String[]{account,
                unRead}, null, null, null);
        if (c != null) {
            int num = c.getCount();
            return num;
        }
        c.close();
        sdb.close();
        db.close();
        return -1;
    }

    /**
     * 获取指定用户的所有消息
     *
     * @param account
     * @return
     */
    public ArrayList<MessageInfo> getMsgAll(String account) {
        ArrayList<MessageInfo> mArrayList = new ArrayList<MessageInfo>();
        DBHelper db = new DBHelper(mContext);
        SQLiteDatabase sdb = db.getReadableDatabase();
        Cursor c = sdb.query(DBHelper.MESSAGE_TABLE, null, " account=? ", new String[]{account}, null, null, "id desc");
        MessageInfo message = null;
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                message = new MessageInfo();
                message.id = c.getInt(c.getColumnIndex(DBHelper.MESSAGE_COL_ID));
                message.account = c.getString(c.getColumnIndex(DBHelper.MESSAGE_COL_ACCOUNT));
                message.title = c.getString(c.getColumnIndex(DBHelper.MESSAGE_COL_TITLE));
                message.content = c.getString(c.getColumnIndex(DBHelper.MESSAGE_COL_CONTENT));
                message.createTime = c.getString(c.getColumnIndex(DBHelper.MESSAGE_COL_TIME));
                message.isRead = c.getString(c.getColumnIndex(DBHelper.MESSAGE_COL_STATUS));
                message.url = c.getString(c.getColumnIndex(DBHelper.MESSAGE_COL_URL));
                message.type = c.getString(c.getColumnIndex(DBHelper.MESSAGE_COL_TYPE));
                message.msgId = c.getString(c.getColumnIndex(DBHelper.MESSAGE_COL_MSGID));
                message.contentId = c.getString(c.getColumnIndex(DBHelper.MESSAGE_COL_CONTENTID));
                mArrayList.add(message);
            }
        }
        c.close();
        sdb.close();
        db.close();
        return mArrayList;
    }

    /**
     * 查询指定消息
     *
     * @param msgId
     */
    public MessageInfo queryMsg(String msgId) {
        String account = SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.PHONE_NUM);
        DBHelper db = new DBHelper(mContext);
        SQLiteDatabase sdb = db.getWritableDatabase();
        Cursor cursor = sdb.query(DBHelper.MESSAGE_TABLE, null, "msgId=? and account=?", new String[]{msgId, account}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            MessageInfo info = new MessageInfo();
            info.title = cursor.getString(cursor.getColumnIndex(DBHelper.MESSAGE_COL_TITLE));
            info.content = cursor.getString(cursor.getColumnIndex(DBHelper.MESSAGE_COL_CONTENT));
            info.createTime = cursor.getString(cursor.getColumnIndex(DBHelper.MESSAGE_COL_TIME));
            return info;
        } else {
            return null;
        }
    }

    /**
     * 删除用户指定的消息
     *
     * @param msgId
     */
    public void deleteMsg(String msgId) {
        DBHelper db = new DBHelper(mContext);
        SQLiteDatabase sdb = db.getWritableDatabase();
        sdb.delete(DBHelper.MESSAGE_TABLE, "msgId=?", new String[]{msgId});
        sdb.close();
        db.close();
    }

    /**
     * 删除用户所有消息
     *
     * @param account
     */
    public void deleteMsgAll(String account) {
        DBHelper db = new DBHelper(mContext);
        SQLiteDatabase sdb = db.getWritableDatabase();
        sdb.delete(DBHelper.MESSAGE_TABLE, "account=?", new String[]{account});
        sdb.close();
        db.close();
    }

    /**
     * 修改消息状态为已读
     *
     * @param msgId
     */
    public void updateMsgStatus(String msgId) {
        String useraccount = SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.PHONE_NUM);
        DBHelper db = new DBHelper(mContext);
        SQLiteDatabase sdb = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.MESSAGE_COL_STATUS, "1");
        sdb.update(DBHelper.MESSAGE_TABLE, cv, "msgId=? and account=?", new String[]{msgId, useraccount});
        sdb.close();
        db.close();
    }
}

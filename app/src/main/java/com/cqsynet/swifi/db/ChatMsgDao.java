/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：社交聊天列表项的数据访问Dao
 *
 *
 * 创建标识：zhaosy	20161111
 */
package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cqsynet.swifi.model.ChatMsgInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

import java.util.ArrayList;
import java.util.List;

public class ChatMsgDao {

    public static ChatMsgDao mChatMsgDao;
    private Context mContext;

    public ChatMsgDao(Context context) {
        mContext = context;
    }

    public static ChatMsgDao getInstance(Context context) {
        if (mChatMsgDao == null) {
            mChatMsgDao = new ChatMsgDao(context);
        }
        return mChatMsgDao;
    }

    /**
     * 保存聊天消息
     *
     * @param chatMsgInfo
     */
    public void saveChatMsgItem(ChatMsgInfo chatMsgInfo, String category) {
        DBHelper db = new DBHelper(mContext);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.CHAT_COL_ID, chatMsgInfo.msgId);
        contentValues.put(DBHelper.CHAT_COL_CHATID, chatMsgInfo.chatId);
        contentValues.put(DBHelper.CHAT_COL_ACCOUNT, chatMsgInfo.userAccount);
        contentValues.put(DBHelper.CHAT_COL_RECEIVE_ACCOUNT, chatMsgInfo.receiveAccount);
        contentValues.put(DBHelper.CHAT_COL_TYPE, chatMsgInfo.type);
        contentValues.put(DBHelper.CHAT_COL_CONTENT, chatMsgInfo.content);
        contentValues.put(DBHelper.CHAT_COL_DATE, chatMsgInfo.date);
        contentValues.put(DBHelper.CHAT_COL_SEND_STATUS, chatMsgInfo.sendStatus);
        contentValues.put(DBHelper.CHAT_COL_READ_STATUS, chatMsgInfo.readStatus);
        contentValues.put(DBHelper.CHAT_COL_OWNER, SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT));
        contentValues.put(DBHelper.CHAT_COL_CATEGORY, category);
        Cursor cur = db.getWritableDatabase().query(DBHelper.CHAT_TABLE, null, DBHelper.CHAT_COL_ID + "=?", new String[]{chatMsgInfo.msgId}, null, null, null);
        if (cur.getCount() > 0) {
            db.getWritableDatabase().update(DBHelper.CHAT_TABLE, contentValues, DBHelper.CHAT_COL_ID + "=?", new String[]{chatMsgInfo.msgId});
        } else {
            db.getWritableDatabase().insert(DBHelper.CHAT_TABLE, null, contentValues);
        }
        cur.close();
        db.close();
    }

    /**
     * 删除某个聊天的所有消息
     *
     * @param chatId
     */
    public void delAllChatMsgFromChatId(String chatId) {
        DBHelper db = new DBHelper(mContext);
        db.getWritableDatabase().delete(DBHelper.CHAT_TABLE, DBHelper.CHAT_COL_CHATID + "=?", new String[]{chatId});
        db.close();
    }

    /**
     * 删除和某人的所有聊天记录
     * @param userAccount
     * @param owner
     */
    public void delAllChatMsgFromAccount(String userAccount, String owner) {
        DBHelper db = new DBHelper(mContext);
        db.getWritableDatabase().delete(DBHelper.CHAT_TABLE,
                DBHelper.CHAT_COL_ACCOUNT + "=? or " + DBHelper.CHAT_COL_RECEIVE_ACCOUNT + "=? and " + DBHelper.CHAT_COL_OWNER + "=?",
                new String[]{userAccount, userAccount, owner});
        db.close();
    }

    /**
     * 删除单条消息
     *
     * @param msgId
     */
    public void delChatMsg(String msgId) {
        DBHelper db = new DBHelper(mContext);
        db.getWritableDatabase().delete(DBHelper.CHAT_TABLE, DBHelper.CHAT_COL_ID + "=?", new String[]{msgId});
    }

    /**
     * 查询聊天消息记录
     *
     * @param chatId 聊天id
     * @return
     */
    public ArrayList<ChatMsgInfo> queryFromChatId(String chatId, String owner) {
        ArrayList<ChatMsgInfo> list = new ArrayList<>();
        DBHelper db = new DBHelper(mContext);
        Cursor cur = db.getWritableDatabase().query(DBHelper.CHAT_TABLE, null, DBHelper.CHAT_COL_CHATID + "=? and " + DBHelper.CHAT_COL_OWNER + "=?", new String[]{chatId, owner}, null, null, DBHelper.CHAT_COL_DATE);
        if (cur != null) {
            while (cur.moveToNext()) {
                ChatMsgInfo chatMsgInfo = new ChatMsgInfo();
                chatMsgInfo.msgId = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_ID));
                chatMsgInfo.chatId = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_CHATID));
                chatMsgInfo.userAccount = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_ACCOUNT));
                chatMsgInfo.receiveAccount = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_RECEIVE_ACCOUNT));
                chatMsgInfo.type = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_TYPE));
                chatMsgInfo.content = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_CONTENT));
                chatMsgInfo.date = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_DATE));
                chatMsgInfo.sendStatus = cur.getInt(cur.getColumnIndex(DBHelper.CHAT_COL_SEND_STATUS));
                chatMsgInfo.readStatus = cur.getInt(cur.getColumnIndex(DBHelper.CHAT_COL_READ_STATUS));
                chatMsgInfo.owner = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_OWNER));
                chatMsgInfo.category = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_CATEGORY));
                list.add(chatMsgInfo);
            }
        }
        cur.close();
        db.close();
        return list;
    }

    public List<ChatMsgInfo> queryFromFriendAccount(String userAccount, String owner) {
        List<ChatMsgInfo> list = new ArrayList<>();
        DBHelper db = new DBHelper(mContext);
        Cursor cur = db.getWritableDatabase().query(DBHelper.CHAT_TABLE, null,
                DBHelper.CHAT_COL_ACCOUNT + "=? and " + DBHelper.CHAT_COL_CATEGORY + "=? and " + DBHelper.CHAT_COL_OWNER + "=? or "
                        + DBHelper.CHAT_COL_RECEIVE_ACCOUNT + "=? and " + DBHelper.CHAT_COL_CATEGORY + "=? and " + DBHelper.CHAT_COL_OWNER + "=?",
                new String[]{userAccount, "friend", owner, userAccount, "friend", owner}, null, null, DBHelper.CHAT_COL_DATE);
        if (cur.moveToFirst()) {
            do {
                ChatMsgInfo chatMsgInfo = new ChatMsgInfo();
                chatMsgInfo.msgId = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_ID));
                chatMsgInfo.chatId = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_CHATID));
                chatMsgInfo.userAccount = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_ACCOUNT));
                chatMsgInfo.receiveAccount = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_RECEIVE_ACCOUNT));
                chatMsgInfo.type = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_TYPE));
                chatMsgInfo.content = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_CONTENT));
                chatMsgInfo.date = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_DATE));
                chatMsgInfo.sendStatus = cur.getInt(cur.getColumnIndex(DBHelper.CHAT_COL_SEND_STATUS));
                chatMsgInfo.readStatus = cur.getInt(cur.getColumnIndex(DBHelper.CHAT_COL_READ_STATUS));
                chatMsgInfo.owner = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_OWNER));
                chatMsgInfo.category = cur.getString(cur.getColumnIndex(DBHelper.CHAT_COL_CATEGORY));
                list.add(chatMsgInfo);
            } while (cur.moveToNext());
        }
        cur.close();
        db.close();
        return list;
    }

    /**
     * 查询和某个朋友的所有未读聊天记录
     * @param userAccount
     * @return
     */
    public int queryUnReadChatMsgCount(String userAccount) {
        int count = 0;
        DBHelper db = new DBHelper(mContext);
        String sql = "select count(*) from " + DBHelper.CHAT_TABLE + " where " + DBHelper.CHAT_COL_ACCOUNT + "=\"" + userAccount
                + "\" and " + DBHelper.CHAT_COL_READ_STATUS + "=0 and " + DBHelper.CHAT_COL_CATEGORY + "=\"friend\" and "
                + DBHelper.CHAT_COL_OWNER + "=\"" + SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT) + "\"";
        Cursor cur = db.getWritableDatabase().rawQuery(sql, null);
        if (cur.moveToFirst()) {
            count = (int) cur.getLong(0);
        }
        cur.close();
        db.close();
        return count;
    }

    /**
     * 查询某个漂流瓶的所有未读聊天记录
     * @param chatId
     * @return
     */
    public int queryUnReadBottleMsgCount(String chatId) {
        int count = 0;
        DBHelper db = new DBHelper(mContext);
        String sql = "select count(*) from " + DBHelper.CHAT_TABLE + " where " + DBHelper.CHAT_COL_CHATID + "=\"" + chatId
                + "\" and " + DBHelper.CHAT_COL_READ_STATUS + "=0 and " + DBHelper.CHAT_COL_CATEGORY + "=\"bottle\" and "
                + DBHelper.CHAT_COL_OWNER + "=\"" + SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT) + "\"";
        Cursor cur = db.getWritableDatabase().rawQuery(sql, null);
        if (cur.moveToFirst()) {
            count = (int) cur.getLong(0);
        }
        cur.close();
        db.close();
        return count;
    }

    /**
     * 查询所有未读信息
     * @param category 类型 0:漂流瓶 1:好友聊天
     * @return
     */
    public int queryAllUnReadMsgCount(String category) {
        int count = 0;
        DBHelper db = new DBHelper(mContext);
        String sql = "select count(*) from " + DBHelper.CHAT_TABLE + " where " + DBHelper.CHAT_COL_READ_STATUS + "=0 and " + DBHelper.CHAT_COL_CATEGORY + "=\"" + category + "\" and "
                + DBHelper.CHAT_COL_OWNER + "=\"" + SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT) + "\"";
        Cursor cur = db.getReadableDatabase().rawQuery(sql, null);
        if (cur.moveToFirst()) {
            count = (int) cur.getLong(0);
        }
        cur.close();
        db.close();
        return count;
    }
}

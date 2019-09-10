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
import android.text.TextUtils;

import com.cqsynet.swifi.model.ChatListItemInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

import java.util.ArrayList;

public class BottleListDao {

    public static BottleListDao mChatListItemDao;
    private Context mContext;

    public BottleListDao(Context context) {
        mContext = context;
    }

    public static BottleListDao getInstance(Context context) {
        if (mChatListItemDao == null) {
            mChatListItemDao = new BottleListDao(context);
        }
        return mChatListItemDao;
    }

    /**
     * 保存聊天列表
     *
     * @param chatListItemInfo
     */
    public void saveBottleListItem(ChatListItemInfo chatListItemInfo) {
        ChatListItemInfo info = queryBottleListItem(chatListItemInfo.chatId);
        if (info != null && !TextUtils.isEmpty(info.draft)) {
            return;
        }
        DBHelper db = new DBHelper(mContext);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.CHAT_LIST_COL_ACCOUNT, chatListItemInfo.userAccount);
        contentValues.put(DBHelper.CHAT_LIST_COL_OWNER, chatListItemInfo.myAccount);
        contentValues.put(DBHelper.CHAT_LIST_COL_CHATID, chatListItemInfo.chatId);
        contentValues.put(DBHelper.CHAT_LIST_COL_TYPE, chatListItemInfo.type);
        contentValues.put(DBHelper.CHAT_LIST_COL_CONTENT, chatListItemInfo.content);
        contentValues.put(DBHelper.CHAT_LIST_COL_UPDATETIME, chatListItemInfo.updateTime);
        contentValues.put(DBHelper.CHAT_LIST_COL_POSITION, chatListItemInfo.position);
        contentValues.put(DBHelper.CHAT_LIST_COL_DRAFT, chatListItemInfo.draft);
        Cursor cur = db.getWritableDatabase().query(DBHelper.BOTTLE_LIST_TABLE, null, DBHelper.CHAT_LIST_COL_CHATID + "=? and " + DBHelper.CHAT_COL_OWNER + "=?", new String[]{chatListItemInfo.chatId, chatListItemInfo.myAccount}, null, null, null);
        if (cur.getCount() > 0) {
            db.getWritableDatabase().update(DBHelper.BOTTLE_LIST_TABLE, contentValues, DBHelper.CHAT_LIST_COL_CHATID + "=? and " + DBHelper.CHAT_COL_OWNER + "=?", new String[]{chatListItemInfo.chatId, chatListItemInfo.myAccount});
        } else {
            db.getWritableDatabase().insert(DBHelper.BOTTLE_LIST_TABLE, null, contentValues);
        }

        cur.close();
        db.close();
    }


    /**
     * 删除聊天
     *
     * @param chatListItemInfo
     */
    public void delBottleListItem(ChatListItemInfo chatListItemInfo) {
        DBHelper db = new DBHelper(mContext);
        db.getWritableDatabase().delete(DBHelper.BOTTLE_LIST_TABLE, DBHelper.CHAT_LIST_COL_CHATID + "=? and " + DBHelper.CHAT_COL_OWNER + "=?", new String[]{chatListItemInfo.chatId, chatListItemInfo.myAccount});
        db.close();
    }

    /**
     * 查询聊天列表中的单个会话项
     *
     * @param chatId
     * @return
     */
    public ChatListItemInfo queryBottleListItem(String chatId) {
        ChatListItemInfo chatListItemInfo = new ChatListItemInfo();
        DBHelper db = new DBHelper(mContext);
        Cursor cur = db.getWritableDatabase().query(DBHelper.BOTTLE_LIST_TABLE, null, DBHelper.CHAT_LIST_COL_CHATID + "=?", new String[]{chatId}, null, null, null);
        if (cur.moveToFirst()) {
            do {
                chatListItemInfo.chatId = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_CHATID));
                chatListItemInfo.myAccount = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_OWNER));
                chatListItemInfo.userAccount = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_ACCOUNT));
                chatListItemInfo.type = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_TYPE));
                chatListItemInfo.content = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_CONTENT));
                chatListItemInfo.updateTime = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_UPDATETIME));
                chatListItemInfo.position = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_POSITION));
                chatListItemInfo.draft = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_DRAFT));
            } while (cur.moveToNext());
        }
        cur.close();
        db.close();
        return chatListItemInfo;
    }


    /**
     * 查询聊天列表
     *
     * @param myAccount 我的用户名
     * @return
     */
    public ArrayList<ChatListItemInfo> queryBottleList(String myAccount) {
        ArrayList<ChatListItemInfo> list = new ArrayList<ChatListItemInfo>();
        DBHelper db = new DBHelper(mContext);
        Cursor cur = db.getWritableDatabase().query(DBHelper.BOTTLE_LIST_TABLE, null, DBHelper.CHAT_LIST_COL_OWNER + "=?", new String[]{myAccount}, null,
                null, DBHelper.CHAT_LIST_COL_UPDATETIME + " desc");
        if (cur.moveToFirst()) {
            do {
                ChatListItemInfo chatListItemInfo = new ChatListItemInfo();
                chatListItemInfo.chatId = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_CHATID));
                chatListItemInfo.myAccount = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_OWNER));
                chatListItemInfo.userAccount = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_ACCOUNT));
                chatListItemInfo.type = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_TYPE));
                chatListItemInfo.content = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_CONTENT));
                chatListItemInfo.updateTime = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_UPDATETIME));
                chatListItemInfo.position = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_POSITION));
                chatListItemInfo.draft = cur.getString(cur.getColumnIndex(DBHelper.CHAT_LIST_COL_DRAFT));
                list.add(chatListItemInfo);
            } while (cur.moveToNext());
        }
        cur.close();
        db.close();
        return list;
    }

    public int queryUnReadMsgCount() {
        int count = 0;
        DBHelper db = new DBHelper(mContext);
        String sql = "select count(*) from " + DBHelper.CHAT_TABLE + " where " + DBHelper.CHAT_COL_READ_STATUS + "=0 and " + DBHelper.CHAT_COL_OWNER + "=\"" + SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT) + "\" and " + DBHelper.CHAT_COL_CATEGORY + "=\"bottle\"";
        Cursor cur = db.getWritableDatabase().rawQuery(sql, null);
        if (cur.moveToFirst()) {
            count = (int) cur.getLong(0);
        }
        cur.close();
        db.close();
        return count;
    }
}

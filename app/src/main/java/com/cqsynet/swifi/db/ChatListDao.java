/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：社交聊天列表项的数据访问Dao
 *
 *
 * 创建标识：sayaki	20180105
 */
package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.cqsynet.swifi.model.ChatListItemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2018/1/5
 */
public class ChatListDao {

    private static ChatListDao mInstance;

    public static ChatListDao getInstance(Context context) {
        if (mInstance == null) {
            synchronized (FriendApplyDao.class) {
                if (mInstance == null) {
                    mInstance = new ChatListDao(context);
                }
            }
        }
        return mInstance;
    }

    private ChatListDao(Context context) {
        this.mContext = context;
    }

    private Context mContext;

    public void insert(ChatListItemInfo chat) {
        ChatListItemInfo chatListItemInfo = query(chat.userAccount, chat.myAccount);
        if (chatListItemInfo != null && !TextUtils.isEmpty(chatListItemInfo.draft)) {
            return;
        }
        DBHelper db = new DBHelper(mContext);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.CHAT_LIST_COL_ACCOUNT, chat.userAccount);
        contentValues.put(DBHelper.CHAT_LIST_COL_OWNER, chat.myAccount);
        contentValues.put(DBHelper.CHAT_LIST_COL_CHATID, chat.chatId);
        contentValues.put(DBHelper.CHAT_LIST_COL_TYPE, chat.type);
        contentValues.put(DBHelper.CHAT_LIST_COL_CONTENT, chat.content);
        contentValues.put(DBHelper.CHAT_LIST_COL_UPDATETIME, chat.updateTime);
        contentValues.put(DBHelper.CHAT_LIST_COL_POSITION, chat.position);
        contentValues.put(DBHelper.CHAT_LIST_COL_DRAFT, chat.draft);
        Cursor cursor = db.getWritableDatabase().query(DBHelper.CHAT_LIST_TABLE, null,
                DBHelper.CHAT_LIST_COL_ACCOUNT + "=? and " + DBHelper.CHAT_LIST_COL_OWNER + "=?",
                new String[]{chat.userAccount, chat.myAccount}, null, null, null);
        if (cursor.getCount() > 0) {
            db.getWritableDatabase().update(DBHelper.CHAT_LIST_TABLE, contentValues,
                    DBHelper.CHAT_LIST_COL_ACCOUNT + "=? and " + DBHelper.CHAT_LIST_COL_OWNER + "=?",
                    new String[]{chat.userAccount, chat.myAccount});
        } else {
            db.getWritableDatabase().insert(DBHelper.CHAT_LIST_TABLE, null, contentValues);
        }

        cursor.close();
        db.close();
    }

    public void delete(ChatListItemInfo chat) {
        DBHelper db = new DBHelper(mContext);
        db.getWritableDatabase().delete(DBHelper.CHAT_LIST_TABLE, DBHelper.CHAT_LIST_COL_ACCOUNT + "=? and " +
                DBHelper.CHAT_COL_OWNER + "=?", new String[]{chat.userAccount, chat.myAccount});
        db.close();
    }

    public void deleteWithAccount(String userAccount, String owner) {
        DBHelper db = new DBHelper(mContext);
        db.getWritableDatabase().delete(DBHelper.CHAT_LIST_TABLE, DBHelper.CHAT_LIST_COL_ACCOUNT + "=? and " +
                DBHelper.CHAT_COL_OWNER + "=?", new String[]{userAccount, owner});
        db.close();
    }

    public ChatListItemInfo query(String userAccount, String owner) {
        DBHelper db = new DBHelper(mContext);
        Cursor cursor = db.getWritableDatabase().query(DBHelper.CHAT_LIST_TABLE, null,
                DBHelper.CHAT_LIST_COL_ACCOUNT + "=? and " + DBHelper.CHAT_LIST_COL_OWNER + "=?",
                new String[]{userAccount, owner}, null, null, null);
        if (cursor.moveToFirst()) {
            ChatListItemInfo chatListItemInfo = new ChatListItemInfo();
            chatListItemInfo.chatId = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_CHATID));
            chatListItemInfo.myAccount = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_OWNER));
            chatListItemInfo.userAccount = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_ACCOUNT));
            chatListItemInfo.type = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_TYPE));
            chatListItemInfo.content = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_CONTENT));
            chatListItemInfo.updateTime = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_UPDATETIME));
            chatListItemInfo.position = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_POSITION));
            chatListItemInfo.draft = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_DRAFT));
            cursor.close();
            db.close();
            return chatListItemInfo;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }

    public List<ChatListItemInfo> queryList(String myAccount) {
        List<ChatListItemInfo> list = new ArrayList<>();
        DBHelper db = new DBHelper(mContext);
        Cursor cursor = db.getWritableDatabase().query(DBHelper.CHAT_LIST_TABLE, null,
                DBHelper.CHAT_LIST_COL_OWNER + "=?", new String[]{myAccount}, null,
                null, DBHelper.CHAT_LIST_COL_UPDATETIME + " desc");
        if (cursor.moveToFirst()) {
            do {
                ChatListItemInfo chatListItemInfo = new ChatListItemInfo();
                chatListItemInfo.chatId = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_CHATID));
                chatListItemInfo.myAccount = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_OWNER));
                chatListItemInfo.userAccount = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_ACCOUNT));
                chatListItemInfo.type = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_TYPE));
                chatListItemInfo.content = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_CONTENT));
                chatListItemInfo.updateTime = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_UPDATETIME));
                chatListItemInfo.position = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_POSITION));
                chatListItemInfo.draft = cursor.getString(cursor.getColumnIndex(DBHelper.CHAT_LIST_COL_DRAFT));
                list.add(chatListItemInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}

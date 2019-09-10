package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cqsynet.swifi.model.FriendApplyInfo;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2018/1/4
 */
public class FriendApplyDao {

    private static FriendApplyDao mInstance;

    public static FriendApplyDao getInstance(Context context) {
        if (mInstance == null) {
            synchronized (FriendApplyDao.class) {
                if (mInstance == null) {
                    mInstance = new FriendApplyDao(context);
                }
            }
        }
        return mInstance;
    }

    private FriendApplyDao(Context context) {
        mContext = context;
    }

    private Context mContext;

    public void insert(FriendApplyInfo friendApplyInfo, String owner) {
        DBHelper db = new DBHelper(mContext);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.APPLY_COL_MSG_ID, friendApplyInfo.messageId);
        contentValues.put(DBHelper.APPLY_COL_ACCOUNT, friendApplyInfo.userAccount);
        contentValues.put(DBHelper.APPLY_COL_NAME, friendApplyInfo.nickname);
        contentValues.put(DBHelper.APPLY_COL_AVATAR, friendApplyInfo.avatar);
        contentValues.put(DBHelper.APPLY_COL_AGE, friendApplyInfo.age);
        contentValues.put(DBHelper.APPLY_COL_SEX, friendApplyInfo.sex);
        contentValues.put(DBHelper.APPLY_COL_SIGN, friendApplyInfo.sign);
        contentValues.put(DBHelper.APPLY_COL_MESSAGE, friendApplyInfo.content);
        contentValues.put(DBHelper.APPLY_COL_DATE, friendApplyInfo.date);
        contentValues.put(DBHelper.APPLY_COL_READ_STATUS, friendApplyInfo.readStatus);
        contentValues.put(DBHelper.APPLY_COL_REPLY_STATUS, friendApplyInfo.replyStatus);
        contentValues.put(DBHelper.APPLY_COL_OWNER, owner);
        Cursor cursor = db.getWritableDatabase().query(DBHelper.FRIEND_APPLY_TABLE, null,
                DBHelper.APPLY_COL_ACCOUNT + "=? and " + DBHelper.APPLY_COL_OWNER + "=?",
                new String[]{friendApplyInfo.userAccount, owner},
                null, null, null);
        if (cursor.getCount() > 0) {
            db.getWritableDatabase().update(DBHelper.FRIEND_APPLY_TABLE, contentValues,
                    DBHelper.APPLY_COL_ACCOUNT + "=? and " + DBHelper.APPLY_COL_OWNER + "=?",
                    new String[]{friendApplyInfo.userAccount, owner});
        } else {
            db.getWritableDatabase().insert(DBHelper.FRIEND_APPLY_TABLE, null, contentValues);
        }
        cursor.close();
        db.close();
    }

    public void delete(String userAccount, String owner) {
        DBHelper db = new DBHelper(mContext);
        db.getWritableDatabase().delete(DBHelper.FRIEND_APPLY_TABLE, DBHelper.APPLY_COL_ACCOUNT + "=? and " + DBHelper.APPLY_COL_OWNER + "=?", new String[]{userAccount, owner});
        db.close();
    }

    public void deleteAll(String owner) {
        DBHelper db = new DBHelper((mContext));
        db.getWritableDatabase().delete(DBHelper.FRIEND_APPLY_TABLE, DBHelper.FRIENDS_COL_OWNER + "=?", new String[]{owner});
        db.close();
    }

    public FriendApplyInfo query(String userAccount, String owner) {
        DBHelper db = new DBHelper(mContext);
        Cursor cursor = db.getWritableDatabase().query(DBHelper.FRIEND_APPLY_TABLE, null,
                DBHelper.APPLY_COL_ACCOUNT + "=? and " + DBHelper.APPLY_COL_OWNER + "=?",
                new String[]{userAccount, owner},
                null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            FriendApplyInfo friendApplyInfo = new FriendApplyInfo();
            friendApplyInfo.userAccount = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_ACCOUNT));
            friendApplyInfo.messageId = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_MSG_ID));
            friendApplyInfo.nickname = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_NAME));
            friendApplyInfo.avatar = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_AVATAR));
            friendApplyInfo.age = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_AGE));
            friendApplyInfo.sex = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_SEX));
            friendApplyInfo.sign = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_SIGN));
            friendApplyInfo.content = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_MESSAGE));
            friendApplyInfo.date = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_DATE));
            friendApplyInfo.readStatus = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_READ_STATUS));
            friendApplyInfo.replyStatus = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_REPLY_STATUS));
            cursor.close();
            db.close();
            return friendApplyInfo;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }

    public List<FriendApplyInfo> queryList(String owner) {
        List<FriendApplyInfo> friendApplyInfos = new ArrayList<>();
        DBHelper db = new DBHelper(mContext);
        Cursor cursor = db.getWritableDatabase().query(DBHelper.FRIEND_APPLY_TABLE, null,
                DBHelper.APPLY_COL_OWNER + "=?", new String[]{owner},
                null, null, DBHelper.APPLY_COL_DATE + " desc");
        if (cursor.moveToFirst()) {
            do {
                FriendApplyInfo friendApplyInfo = new FriendApplyInfo();
                friendApplyInfo.userAccount = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_ACCOUNT));
                friendApplyInfo.messageId = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_MSG_ID));
                friendApplyInfo.nickname = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_NAME));
                friendApplyInfo.avatar = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_AVATAR));
                friendApplyInfo.age = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_AGE));
                friendApplyInfo.sex = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_SEX));
                friendApplyInfo.sign = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_SIGN));
                friendApplyInfo.content = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_MESSAGE));
                friendApplyInfo.date = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_DATE));
                friendApplyInfo.readStatus = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_READ_STATUS));
                friendApplyInfo.replyStatus = cursor.getString(cursor.getColumnIndex(DBHelper.APPLY_COL_REPLY_STATUS));
                friendApplyInfos.add(friendApplyInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return friendApplyInfos;
    }

    public int queryUnReadApplyCount() {
        int count = 0;
        DBHelper db = new DBHelper(mContext);
        String sql;
        sql = "select count(*) from " + DBHelper.FRIEND_APPLY_TABLE + " where " + DBHelper.APPLY_COL_READ_STATUS+ "=0 and "
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

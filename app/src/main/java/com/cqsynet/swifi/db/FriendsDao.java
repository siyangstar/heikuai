package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cqsynet.swifi.model.FriendsInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2018/1/9
 */
public class FriendsDao {

    private static FriendsDao mInstance;

    public static FriendsDao getInstance(Context context) {
        if (mInstance == null) {
            synchronized (FriendApplyDao.class) {
                if (mInstance == null) {
                    mInstance = new FriendsDao(context);
                }
            }
        }
        return mInstance;
    }

    private FriendsDao(Context context) {
        mContext = context;
    }

    private Context mContext;

    public void insert(String userAccount, String owner) {
        DBHelper db = new DBHelper(mContext);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.FRIENDS_COL_GROUP, "0");
        contentValues.put(DBHelper.FRIENDS_COL_ACCOUNT, userAccount);
        contentValues.put(DBHelper.FRIENDS_COL_OWNER, owner);
        Cursor cursor = db.getWritableDatabase().query(DBHelper.FRIENDS_TABLE, null,
                DBHelper.FRIENDS_COL_ACCOUNT + "=? and " + DBHelper.FRIENDS_COL_OWNER + "=?",
                new String[]{userAccount, owner},
                null, null, null);
        if (cursor.getCount() > 0) {
            db.getWritableDatabase().update(DBHelper.FRIENDS_TABLE, contentValues,
                    DBHelper.FRIENDS_COL_ACCOUNT + "=? and " + DBHelper.FRIENDS_COL_OWNER + "=?",
                    new String[]{userAccount, owner});
        } else {
            db.getWritableDatabase().insert(DBHelper.FRIENDS_TABLE, null, contentValues);
        }
        cursor.close();
        db.close();
    }

    public void delete(String userAccount, String owner) {
        DBHelper db = new DBHelper(mContext);
        db.getWritableDatabase().delete(DBHelper.FRIENDS_TABLE,
                DBHelper.FRIENDS_COL_ACCOUNT + "=? and " + DBHelper.FRIENDS_COL_OWNER + "=?",
                new String[]{userAccount, owner});
        db.close();
    }

    public void deleteAll(String owner) {
        DBHelper db = new DBHelper((mContext));
        db.getWritableDatabase().delete(DBHelper.FRIENDS_TABLE, DBHelper.FRIENDS_COL_OWNER + "=?", new String[]{owner});
        db.close();
    }

    public FriendsInfo query(String userAccount, String owner) {
        DBHelper db = new DBHelper(mContext);
        Cursor cursor = db.getWritableDatabase().query(DBHelper.FRIENDS_TABLE, null,
                DBHelper.FRIENDS_COL_ACCOUNT + "=? and " + DBHelper.FRIENDS_COL_OWNER + "=?",
                new String[]{userAccount, owner},
                null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            FriendsInfo friendsInfo = new FriendsInfo();
            friendsInfo.userAccount = cursor.getString(cursor.getColumnIndex(DBHelper.FRIENDS_COL_ACCOUNT));
            cursor.close();
            db.close();
            return friendsInfo;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }

    public List<FriendsInfo> queryList(String owner) {
        List<FriendsInfo> friendsInfos = new ArrayList<>();
        DBHelper db = new DBHelper(mContext);
        Cursor cursor = db.getWritableDatabase().query(DBHelper.FRIENDS_TABLE, null,
                DBHelper.FRIENDS_COL_OWNER + "=?",
                new String[]{owner},
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                FriendsInfo friendsInfo = new FriendsInfo();
                friendsInfo.userAccount = cursor.getString(cursor.getColumnIndex(DBHelper.FRIENDS_COL_ACCOUNT));
                friendsInfos.add(friendsInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return friendsInfos;
    }
}

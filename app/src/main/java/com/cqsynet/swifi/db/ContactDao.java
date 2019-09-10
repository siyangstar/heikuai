/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：社交用户信息数据访问Dao
 *
 *
 * 创建标识：zhaosy	20161111
 */
package com.cqsynet.swifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cqsynet.swifi.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class ContactDao {

    private Context mContext;
    public static ContactDao mContactDao;

    public ContactDao(Context context) {
        mContext = context;
    }

    public static ContactDao getInstance(Context context) {
        if (mContactDao == null) {
            mContactDao = new ContactDao(context);
        }
        return mContactDao;
    }

    /**
     * 保存用户
     *
     * @param userInfo
     */
    public void saveUser(UserInfo userInfo) {
        DBHelper db = new DBHelper(mContext);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.CONTACT_COL_ACCOUNT, userInfo.userAccount);
        contentValues.put(DBHelper.CONTACT_COL_NICKNAME, userInfo.nickname);
        contentValues.put(DBHelper.CONTACT_COL_HEAD_URL, userInfo.headUrl);
        contentValues.put(DBHelper.CONTACT_COL_SEX, userInfo.sex);
        contentValues.put(DBHelper.CONTACT_COL_SIGN, userInfo.sign);
        contentValues.put(DBHelper.CONTACT_COL_REMARK, userInfo.remark);
        Cursor cur = db.getWritableDatabase().query(DBHelper.CONTACT_TABLE, null, DBHelper.CONTACT_COL_ACCOUNT + "=?", new String[]{userInfo.userAccount}, null, null, null);
        if (cur.getCount() > 0) {
            db.getWritableDatabase().update(DBHelper.CONTACT_TABLE, contentValues, DBHelper.CONTACT_COL_ACCOUNT + "=?", new String[]{userInfo.userAccount});
        } else {
            db.getWritableDatabase().insert(DBHelper.CONTACT_TABLE, null, contentValues);
        }

        cur.close();
        db.close();
    }


    /**
     * 删除用户
     *
     * @param userAccount
     */
    public void delUser(String userAccount) {
        DBHelper db = new DBHelper(mContext);
        db.getWritableDatabase().delete(DBHelper.CONTACT_TABLE, DBHelper.CONTACT_COL_ACCOUNT + "=?", new String[]{userAccount});
        db.close();
    }

    /**
     * 查询用户资料
     *
     * @param userAccount
     * @return
     */
    public UserInfo queryUser(String userAccount) {
        DBHelper db = new DBHelper(mContext);
        Cursor cur = db.getWritableDatabase().query(DBHelper.CONTACT_TABLE, null, DBHelper.CONTACT_COL_ACCOUNT + "=?", new String[]{userAccount}, null, null, null);
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            UserInfo userInfo = new UserInfo();
            userInfo.userAccount = userAccount;
            userInfo.headUrl = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_HEAD_URL));
            userInfo.nickname = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_NICKNAME));
            userInfo.sex = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_SEX));
            userInfo.sign = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_SIGN));
            userInfo.remark = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_REMARK));
            cur.close();
            db.close();
            return userInfo;
        } else {
            cur.close();
            db.close();
            return null;
        }
    }

    /**
     * 模糊查询
     * @param nickname 昵称
     * @param remark   备注
     * @return 用户信息
     */
    public List<UserInfo> queryUserForName(String nickname, String remark) {
        List<UserInfo> userInfos = new ArrayList<>();
        DBHelper db = new DBHelper(mContext);
        String sql = "select * from " + DBHelper.CONTACT_TABLE + " where " + DBHelper.CONTACT_COL_REMARK + " like '%" + remark + "%'"
                + " or " + DBHelper.CONTACT_COL_NICKNAME + " like '%" + nickname + "%'";
        Cursor cur = db.getWritableDatabase().rawQuery(sql, null);
        if (cur.moveToFirst()) {
            do {
                UserInfo userInfo = new UserInfo();
                userInfo.userAccount = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_ACCOUNT));
                userInfo.headUrl = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_HEAD_URL));
                userInfo.nickname = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_NICKNAME));
                userInfo.sex = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_SEX));
                userInfo.sign = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_SIGN));
                userInfo.remark = cur.getString(cur.getColumnIndex(DBHelper.CONTACT_COL_REMARK));
                userInfos.add(userInfo);
            } while (cur.moveToNext());
        }
        cur.close();
        db.close();
        return userInfos;
    }
}

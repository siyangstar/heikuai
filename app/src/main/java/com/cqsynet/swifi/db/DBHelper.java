/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：数据库工具类
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // 数据库名
    public static final String DATABASE_NAME = "heikuai.db";
    // 数据库版本
    public final static int DATABASE_VERSION = 21;
    // 消息表名
    public static final String MESSAGE_TABLE = "app_message";
    // 搜索历史表名
    public static final String SEARCH_TABLE = "app_search";
    // 收藏表名
    public static final String COLLECT_TABLE = "collectCache";
    // 联系人信息表名
    public static final String CONTACT_TABLE = "contact";
    public static final String CONTACT_TABLE_OLD = "contactOld";
    // 聊天列表表名
    public static final String CHAT_LIST_TABLE = "chatList";
    public static final String CHAT_LIST_TABLE_OLD = "chatListOld";
    // 漂流瓶列表表名
    public static final String BOTTLE_LIST_TABLE = "bottleList";
    // 好友申请表表名
    public static final String FRIEND_APPLY_TABLE = "friendApply";
    // 好友列表表名
    public static final String FRIENDS_TABLE = "friends";
    // 聊天记录表名
    public static final String CHAT_TABLE = "chat";
    public static final String CHAT_TABLE_OLD = "chatOld";
    // 网址黑白名单表名
    public static final String BLACK_WHITE_URL_TABLE = "blackWhiteUrl";

    // 消息字段
    public static final String MESSAGE_COL_ID = "id";
    public static final String MESSAGE_COL_ACCOUNT = "account";
    public static final String MESSAGE_COL_TITLE = "title";
    public static final String MESSAGE_COL_CONTENT = "content";
    public static final String MESSAGE_COL_TIME = "createTime";
    public static final String MESSAGE_COL_STATUS = "isRead";
    public static final String MESSAGE_COL_URL = "url";
    public static final String MESSAGE_COL_TYPE = "type";
    public static final String MESSAGE_COL_MSGID = "msgId";
    public static final String MESSAGE_COL_CONTENTID = "contentId";
    // 搜索历史字段
    public static final String SEARCH_COL_ID = "_id";
    public static final String SEARCH_COL_ACCOUNT = "userAccount";
    public static final String SEARCH_COL_CONTENT = "searchContent";
    public static final String SEARCH_COL_TIME = "createDate";
    // 收藏表字段
    public static final String COLLECT_COL_ID = "_id";
    public static final String COLLECT_COL_TYPE = "type";
    public static final String COLLECT_COL_TITLE = "title";
    public static final String COLLECT_COL_NEWS_ID = "id";
    public static final String COLLECT_COL_URL = "url";
    public static final String COLLECT_COL_IMAGE = "image";
    public static final String COLLECT_COL_SOURCE = "source";
    public static final String COLLECT_COL_TIMESTAMP = "timestamp";
    public static final String COLLECT_COL_ACCOUNT = "account";
    // 联系人信息表(根据userAccount区分)
    public static final String CONTACT_COL_ID = "_id";
    public static final String CONTACT_COL_ACCOUNT = "userAccount";
    public static final String CONTACT_COL_NICKNAME = "nickname";
    public static final String CONTACT_COL_REMARK = "remark";
    public static final String CONTACT_COL_SEX = "sex";
    public static final String CONTACT_COL_SIGN = "sign";
    public static final String CONTACT_COL_HEAD_URL = "headUrl";
    // 聊天列表(根据owner区分)
    public static final String CHAT_LIST_COL_ID = "_id";
    public static final String CHAT_LIST_COL_OWNER = "owner";
    public static final String CHAT_LIST_COL_ACCOUNT = "userAccount";
    public static final String CHAT_LIST_COL_CHATID = "chatId";
    public static final String CHAT_LIST_COL_TYPE = "type";
    public static final String CHAT_LIST_COL_CONTENT = "content";
    public static final String CHAT_LIST_COL_UPDATETIME = "updateTime";
    public static final String CHAT_LIST_COL_POSITION = "position";
    public static final String CHAT_LIST_COL_DRAFT = "draft";
    // 聊天记录(根据chatId区分)
    public static final String CHAT_COL_ID = "_id";
    public static final String CHAT_COL_CHATID = "chatId";
    public static final String CHAT_COL_ACCOUNT = "userAccount";
    public static final String CHAT_COL_RECEIVE_ACCOUNT = "receiveAccount";
    public static final String CHAT_COL_TYPE = "type";
    public static final String CHAT_COL_CONTENT = "content";
    public static final String CHAT_COL_DATE = "date";
    public static final String CHAT_COL_SEND_STATUS = "sendStatus";
    public static final String CHAT_COL_READ_STATUS = "readStatus";
    public static final String CHAT_COL_OWNER = "owner";
    public static final String CHAT_COL_CATEGORY= "category";
    // 好友请求
    public static final String APPLY_COL_ID = "_id";
    public static final String APPLY_COL_MSG_ID = "msgId";
    public static final String APPLY_COL_ACCOUNT = "userAccount";
    public static final String APPLY_COL_NAME = "name";
    public static final String APPLY_COL_AVATAR = "avatar";
    public static final String APPLY_COL_AGE = "age";
    public static final String APPLY_COL_SEX = "sex";
    public static final String APPLY_COL_SIGN = "sign";
    public static final String APPLY_COL_MESSAGE = "message";
    public static final String APPLY_COL_DATE = "date";
    public static final String APPLY_COL_READ_STATUS = "readStatus";
    public static final String APPLY_COL_REPLY_STATUS = "replyStatus";
    public static final String APPLY_COL_OWNER = "owner";
    // 好友列表
    public static final String FRIENDS_COL_ID = "_id";
    public static final String FRIENDS_COL_GROUP = "friendGroup";
    public static final String FRIENDS_COL_ACCOUNT = "userAccount";
    public static final String FRIENDS_COL_OWNER = "owner";
    // 网址黑白名单
    public static final String BWURL_COL_ID = "_id";
    public static final String BWURL_COL_TYPE = "type";
    public static final String BWURL_COL_URL = "url";
    public static final String BWURL_COL_ADV = "showAdv";


    // 创建消息表的语句
    public static final String CREATE_TABLE_MESSAGE = "CREATE TABLE IF NOT EXISTS " + MESSAGE_TABLE + "("
            + MESSAGE_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + MESSAGE_COL_ACCOUNT + " TEXT,"
            + MESSAGE_COL_TITLE + " TEXT,"
            + MESSAGE_COL_CONTENT + " TEXT,"
            + MESSAGE_COL_TIME + " TEXT,"
            + MESSAGE_COL_STATUS + " TEXT,"
            + MESSAGE_COL_URL + " TEXT,"
            + MESSAGE_COL_TYPE + " TEXT,"
            + MESSAGE_COL_CONTENTID + " TEXT,"
            + MESSAGE_COL_MSGID + " TEXT)";
    // 创建搜索历史表的语句
    public static final String CREATE_TABLE_SEARCH = "CREATE TABLE IF NOT EXISTS " + SEARCH_TABLE + "("
            + SEARCH_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SEARCH_COL_ACCOUNT + " TEXT,"
            + SEARCH_COL_CONTENT + " TEXT,"
            + SEARCH_COL_TIME + " TEXT)";
    // 创建收藏表的语句
    public static final String CREATE_TABLE_COLLECT = "CREATE TABLE IF NOT EXISTS " + COLLECT_TABLE + "("
            + COLLECT_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLLECT_COL_TYPE + " TEXT,"
            + COLLECT_COL_TITLE + " TEXT,"
            + COLLECT_COL_NEWS_ID + " TEXT,"
            + COLLECT_COL_URL + " TEXT,"
            + COLLECT_COL_IMAGE + " TEXT,"
            + COLLECT_COL_SOURCE + " TEXT,"
            + COLLECT_COL_TIMESTAMP + " TEXT,"
            + COLLECT_COL_ACCOUNT + " TEXT)";
    // 创建联系人表的语句
    public static final String CREATE_TABLE_CONTACT = "CREATE TABLE IF NOT EXISTS " + CONTACT_TABLE + "("
            + CONTACT_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CONTACT_COL_ACCOUNT + " TEXT,"
            + CONTACT_COL_NICKNAME + " TEXT,"
            + CONTACT_COL_REMARK + " TEXT,"
            + CONTACT_COL_SEX + " TEXT,"
            + CONTACT_COL_SIGN + " TEXT,"
            + CONTACT_COL_HEAD_URL + " TEXT)";
    // 创建好友聊天列表表的语句
    public static final String CREATE_TABLE_CHAT_LIST = "CREATE TABLE IF NOT EXISTS " + CHAT_LIST_TABLE + "("
            + CHAT_LIST_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CHAT_LIST_COL_OWNER + " TEXT,"
            + CHAT_LIST_COL_ACCOUNT + " TEXT,"
            + CHAT_LIST_COL_CHATID + " TEXT,"
            + CHAT_LIST_COL_CONTENT + " TEXT,"
            + CHAT_LIST_COL_TYPE + " TEXT,"
            + CHAT_LIST_COL_POSITION + " TEXT,"
            + CHAT_LIST_COL_UPDATETIME + " TEXT,"
            + CHAT_LIST_COL_DRAFT + " TEXT)";
    // 创建漂流瓶聊天列表表的语句
    public static final String CREATE_TABLE_BOTTLE_LIST = "CREATE TABLE IF NOT EXISTS " + BOTTLE_LIST_TABLE + "("
            + CHAT_LIST_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CHAT_LIST_COL_OWNER + " TEXT,"
            + CHAT_LIST_COL_ACCOUNT + " TEXT,"
            + CHAT_LIST_COL_CHATID + " TEXT,"
            + CHAT_LIST_COL_CONTENT + " TEXT,"
            + CHAT_LIST_COL_TYPE + " TEXT,"
            + CHAT_LIST_COL_POSITION + " TEXT,"
            + CHAT_LIST_COL_UPDATETIME + " TEXT,"
            + CHAT_LIST_COL_DRAFT + " TEXT)";
    // 创建聊天记录表的语句
    public static final String CREATE_TABLE_CHAT = "CREATE TABLE IF NOT EXISTS " + CHAT_TABLE + "("
            + CHAT_COL_ID + " TEXT,"
            + CHAT_COL_OWNER + " TEXT,"
            + CHAT_COL_CHATID + " TEXT,"
            + CHAT_COL_ACCOUNT + " TEXT,"
            + CHAT_COL_RECEIVE_ACCOUNT + " TEXT,"
            + CHAT_COL_TYPE + " TEXT,"
            + CHAT_COL_CONTENT + " TEXT,"
            + CHAT_COL_SEND_STATUS + " INTEGER,"
            + CHAT_COL_READ_STATUS + " INTEGER,"
            + CHAT_COL_CATEGORY + " TEXT,"
            + CHAT_COL_DATE + " TEXT,PRIMARY KEY(" + CHAT_COL_CHATID + "," + CHAT_COL_DATE + "))";
    // 创建好友申请表的语句
    public static final String CREATE_TABLE_FRIEND_APPLY = "CREATE TABLE IF NOT EXISTS " + FRIEND_APPLY_TABLE + "("
            + APPLY_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + APPLY_COL_MSG_ID + " TEXT,"
            + APPLY_COL_ACCOUNT + " TEXT,"
            + APPLY_COL_NAME + " TEXT,"
            + APPLY_COL_AVATAR + " TEXT,"
            + APPLY_COL_AGE + " TEXT,"
            + APPLY_COL_SEX + " TEXT,"
            + APPLY_COL_SIGN + " TEXT,"
            + APPLY_COL_MESSAGE + " TEXT,"
            + APPLY_COL_DATE + " TEXT,"
            + APPLY_COL_READ_STATUS + " TEXT,"
            + APPLY_COL_REPLY_STATUS + " TEXT,"
            + APPLY_COL_OWNER + " TEXT)";
    // 创建好友列表的语句
    public static final String CREATE_TABLE_FRIENDS = "CREATE TABLE IF NOT EXISTS " + FRIENDS_TABLE + "("
            + FRIENDS_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FRIENDS_COL_GROUP + " TEXT,"
            + FRIENDS_COL_ACCOUNT + " TEXT,"
            + FRIENDS_COL_OWNER + " TEXT)";
    // 创建网址黑白名单表的语句
    public static final String CREATE_TABLE_BLACK_WHITE_URL = "CREATE TABLE IF NOT EXISTS " + BLACK_WHITE_URL_TABLE + "("
            + BWURL_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + BWURL_COL_TYPE + " TEXT,"
            + BWURL_COL_URL + " TEXT,"
            + BWURL_COL_ADV + " TEXT)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("drop table if exists freeWifiUseLog");
//        db.execSQL("drop table if exists launchImg");
//        db.execSQL("drop table if exists newsCache");
//        db.execSQL("drop table if exists app_message");
//        db.execSQL("drop table if exists app_search");
//        db.execSQL("drop table if exists collectCache");
//        db.execSQL("drop table if exists contact");
//        db.execSQL("drop table if exists chatList");
//        db.execSQL("drop table if exists chat");
//        createTable(db);
//
//        db.execSQL("ALTER TABLE " + CHAT_LIST_TABLE + " RENAME TO " + CHAT_LIST_TABLE_OLD);
//        db.execSQL(CREATE_TABLE_CHAT_LIST);
//        db.execSQL("INSERT INTO " + CHAT_LIST_TABLE + "("
//                + CHAT_LIST_COL_ID + ","
//                + CHAT_LIST_COL_OWNER + ","
//                + CHAT_LIST_COL_ACCOUNT + ","
//                + CHAT_LIST_COL_CHATID + ","
//                + CHAT_LIST_COL_CONTENT + ","
//                + CHAT_LIST_COL_TYPE + ","
//                + CHAT_LIST_COL_POSITION + ","
//                + CHAT_LIST_COL_UPDATETIME
//                + ")" + " SELECT "
//                + CHAT_LIST_COL_ID + ","
//                + CHAT_LIST_COL_OWNER + ","
//                + CHAT_LIST_COL_ACCOUNT + ","
//                + CHAT_LIST_COL_CHATID + ","
//                + CHAT_LIST_COL_CONTENT + ","
//                + CHAT_LIST_COL_TYPE + ","
//                + CHAT_LIST_COL_POSITION + ","
//                + CHAT_LIST_COL_UPDATETIME
//                + " FROM " + CHAT_LIST_TABLE_OLD);
//        db.execSQL("DROP TABLE " + CHAT_LIST_TABLE_OLD);
    }

    public void cleanAllTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists freeWifiUseLog");
        db.execSQL("drop table if exists launchImg");
        db.execSQL("drop table if exists newsCache");
        db.execSQL("drop table if exists app_message");
        db.execSQL("drop table if exists app_search");
        db.execSQL("drop table if exists collectCache");
        db.execSQL("drop table if exists contact");
        db.execSQL("drop table if exists chatList");
        db.execSQL("drop table if exists chat");
        db.execSQL("drop table if exists friendApply");
        db.execSQL("drop table if exists blackWhiteUrl");
        createTable(db);
        db.close();
    }

    public void createTable(SQLiteDatabase db) {

        /**
         * 表字段含义 id 自增长id date 日期（格式：2014-09-29） todayUse 今日累计使用秒数 totalFree
         * 累计获取免费上网秒数
         */
        String sql = "create table if not exists freeWifiUseLog (id integer primary key autoincrement, date varchar(100), todayUse integer, totalFree integer)";
        db.execSQL(sql);

        /**
         * 启动图片数据 字段信息请参考LaunchImageObject实体类
         */
        String sql3 = "create table if not exists launchImg(_id integer primary key autoincrement, advId varchar(100), localFileName varchar(100), url " +
                "varchar(100), jumpUrl varchar(100),startDate varchar(100), endDate varchar(100))";
        db.execSQL(sql3);

        /**
         * 新闻列表Json数据 channelId 新闻列表对应的频道id date 新闻列表获取到的实际 content
         * 新闻列表的内容，json格式的字符串
         */
        String sqlNewsCache = "create table if not exists newsCache(_id integer primary key autoincrement, channelId varchar(100), date long, content text)";
        db.execSQL(sqlNewsCache);

        db.execSQL(CREATE_TABLE_MESSAGE);
        db.execSQL(CREATE_TABLE_SEARCH);
        db.execSQL(CREATE_TABLE_COLLECT);
        db.execSQL(CREATE_TABLE_CONTACT);
        db.execSQL(CREATE_TABLE_CHAT_LIST);
        db.execSQL(CREATE_TABLE_BOTTLE_LIST);
        db.execSQL(CREATE_TABLE_CHAT);
        db.execSQL(CREATE_TABLE_FRIEND_APPLY);
        db.execSQL(CREATE_TABLE_FRIENDS);
        db.execSQL(CREATE_TABLE_BLACK_WHITE_URL);
    }
}
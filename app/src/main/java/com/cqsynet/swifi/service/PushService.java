/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 后台服务,用于长连接推送
 *
 * 创建标识：zhaosy 20160302
 */
package com.cqsynet.swifi.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.ChatActivity;
import com.cqsynet.swifi.activity.GalleryActivity;
import com.cqsynet.swifi.activity.MyCommentActivity;
import com.cqsynet.swifi.activity.ReadMessageActivity;
import com.cqsynet.swifi.activity.SuggestListActivity;
import com.cqsynet.swifi.activity.TopicActivity;
import com.cqsynet.swifi.activity.WebActivity;
import com.cqsynet.swifi.activity.social.FriendApplyListActivity;
import com.cqsynet.swifi.db.BottleListDao;
import com.cqsynet.swifi.db.ChatListDao;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendApplyDao;
import com.cqsynet.swifi.db.FriendsDao;
import com.cqsynet.swifi.db.MessageDao;
import com.cqsynet.swifi.model.ChatListItemInfo;
import com.cqsynet.swifi.model.ChatMsgInfo;
import com.cqsynet.swifi.model.FriendApplyInfo;
import com.cqsynet.swifi.model.FriendsInfo;
import com.cqsynet.swifi.model.GetFriendInfoRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.UserInfoResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.CacheCleanManager;
import com.cqsynet.swifi.util.LogUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WakeupUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PushService extends Service {

    private Socket mSocket;
    private SocketReceiver mReceiver;
    private Map<String, String> mMsgidMap = new HashMap<String, String>();
//    /**
//     * 使用aidl 启动SafeService
//     */
//    private IGuardAIDL mIGuardAIDL = new IGuardAIDL.Stub() {
//        @Override
//        public void stopService() throws RemoteException {
//            Intent i = new Intent(getBaseContext(), SafeService.class);
//            getBaseContext().stopService(i);
//        }
//
//        @Override
//        public void startService() throws RemoteException {
//            Intent i = new Intent(getBaseContext(), SafeService.class);
//            getBaseContext().startService(i);
//        }
//    };
    Handler mHdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Map map = (Map) msg.obj;
                    String title = (String) map.get("title");
                    ChatMsgInfo chatMsgInfo = (ChatMsgInfo) map.get("content");
                    String category = (String) map.get("category");
                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
                    //当前正在和此人聊天则不需要弹出通知
                    if (runningActivity.equals(ChatActivity.class.getName()) && chatMsgInfo.userAccount.equals(ChatActivity.mFriendAccount)) {
                        // 不弹通知
                    } else if (runningActivity.equals(ChatActivity.class.getName()) && chatMsgInfo.chatId.equals(ChatActivity.mChatId)) {
                        //不弹通知
                    } else {
                        //显示通知
                        Intent jumpIntent = new Intent();
                        jumpIntent.setClass(PushService.this, ChatActivity.class);
                        jumpIntent.putExtra("chatId", chatMsgInfo.chatId);
                        jumpIntent.putExtra("userAccount", chatMsgInfo.userAccount);
                        jumpIntent.putExtra("position", chatMsgInfo.position);
                        jumpIntent.putExtra("owner", chatMsgInfo.owner);
                        jumpIntent.putExtra("category", category);
                        jumpIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        if (chatMsgInfo.type.equals("0")) {
                            sendNotify(PushService.this, title, chatMsgInfo.content, jumpIntent);
                        } else if (chatMsgInfo.type.equals("1")) {
                            sendNotify(PushService.this, title, "[语音]", jumpIntent);
                        } else if (chatMsgInfo.type.equals("2")) {
                            sendNotify(PushService.this, title, "[图片]", jumpIntent);
                        }
                    }
                    break;
                case 1:
                    ToastUtil.showToast(PushService.this.getApplicationContext(), R.string.error_rsa);
                    break;
//                case 2:
//                    startSafeService();
//                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 收到消息后发送通知
     *
     * @param title
     * @param content
     * @param jumpIntent
     */
    public static void sendNotify(Context context, String title, String content, Intent jumpIntent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("heikuai", "heikuai_msg", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }
        String chatId = jumpIntent.getStringExtra("chatId");
        int id;
        if (!TextUtils.isEmpty(chatId)) {
            id = jumpIntent.getStringExtra("chatId").hashCode();
        } else {
            id = (int) System.currentTimeMillis();
        }
        PendingIntent pi = PendingIntent.getActivity(context, id, jumpIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.noti_icon);
            builder.setColor(0xFF39B44A);
        } else {
            builder.setSmallIcon(R.drawable.ic_launcher);
        }
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true);
        builder.setContentIntent(pi);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("heikuai");
        }
        nm.notify(id, builder.getNotification());

        if (Globals.g_notificationList == null) {
            Globals.g_notificationList = new ArrayList<>();
        }
        Globals.g_notificationList.add(id);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Globals.DEBUG) {
            Log.i(this.getClass().getName(), "PushService onCreate");
        }
        mReceiver = new SocketReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(AppConstants.ACTION_SOCKET_LOGIN);
        filter.addAction(AppConstants.ACTION_SOCKET_LOGOUT);
        filter.addAction(AppConstants.ACTION_SOCKET_CONNECTING);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(mReceiver, filter);

        initSocket();
        startSocketConnecting();

//        //此线程用监听SafeService的状态
//        new Thread() {
//            public void run() {
//                while (true) {
//                    boolean isRun = AppUtil.isServiceWork(PushService.this, SafeService.class.getName());
//                    if (!isRun) {
//                        Message msg = Message.obtain();
//                        msg.what = 2;
//                        mHdl.sendMessage(msg);
//                    }
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Globals.DEBUG) {
            Log.i(this.getClass().getName(), "PushService onStart");
        }
        if (mSocket == null) {
            initSocket();
        } else if (!mSocket.connected()) {
            mSocket.connect();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (Globals.DEBUG) {
            LogUtil.debug(this.getClass().getName(), "PushService onDestroy");
        }
        unregisterReceiver(mReceiver);
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            mSocket.close();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //    @Override
//    public IBinder onBind(Intent intent) {
//        return (IBinder) mIGuardAIDL;
//    }
//
////    /**
////     * 在内存紧张的时候，系统回收内存时，会回调OnTrimMemory， 重写onTrimMemory当系统清理内存时从新启动SafeService
////     */
////    @Override
////    public void onTrimMemory(int level) {
////        startSafeService();
////    }
//
//    /**
//     * 判断SafeService是否还在运行，如果不是则启动SafeService
//     */
//    private void startSafeService() {
//        boolean isRun = AppUtil.isServiceWork(PushService.this, SafeService.class.getName());
//        if (!isRun) {
//            try {
//                mIGuardAIDL.startService();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * 定时开始socket连接
     */
    private void startSocketConnecting() {
        Intent intent = new Intent(AppConstants.ACTION_SOCKET_CONNECTING);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        long firstime = System.currentTimeMillis() + 5000; //开始时间
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        am.setRepeating(AlarmManager.RTC_WAKEUP, firstime, 1000 * 60 * 1, pi); //每隔1分钟发送广播
    }

    /**
     * 初始化socket
     */
    private void initSocket() {
        try {
            Options opt = new Options();
            opt.forceNew = true;
            opt.reconnection = true;
            opt.reconnectionAttempts = 3;
            opt.reconnectionDelay = 5000;
            opt.reconnectionDelayMax = 10000;
            mSocket = IO.socket(AppConstants.SERVER_SOCKET_ADDRESS, opt);
            mSocket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Ack ack = (Ack) args[args.length - 1];
                    ack.call();
                    String msg = args[1].toString(); // 消息
                    String msgId = args[0].toString(); // 消息id
                    if (!mMsgidMap.containsKey(msgId)) {
                        mMsgidMap.put(msgId, msgId);
                        if (!TextUtils.isEmpty(msg)) {
                            handleMessage(msgId, msg);
                        }
                    }

                    if (Globals.DEBUG) {
                        Log.i(this.getClass().getName(), "on socket custom msg = " + msg);
                    }
                }
            }).on("repeat", new Emitter.Listener() { // 在另一个设备重复登陆,断开当前连接
                @Override
                public void call(Object... args) {
                    if (Globals.DEBUG) {
                        Log.i(this.getClass().getName(), "重复登录");
                    }
                    LogUtil.writeToFile("推送导致重复登录");
//                    LogoutUtil.cleanLoginInfo(PushService.this);
//                    Intent intent = new Intent();
//                    intent.setClass(getApplicationContext(), LoginActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    PushService.this.getApplicationContext().startActivity(intent);
//                    mHdl.sendEmptyMessage(1);
                }
            }).on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (Globals.DEBUG) {
                        LogUtil.debug(this.getClass().getName(), "on socket connect");
                    }
                    Globals.g_isSocketConnected = true;
                    String userAccount = SharedPreferencesInfo.getTagString(PushService.this,
                            SharedPreferencesInfo.ACCOUNT);
                    if (!TextUtils.isEmpty(userAccount)) {
                        mSocket.emit("login", userAccount); // 每次连接发送用户账号
                    }
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... arg0) {
                    if (Globals.DEBUG) {
                        LogUtil.debug(this.getClass().getName(), "on socket error = " + arg0.toString() + "/n" + arg0[0].toString());
                    }
                    Globals.g_isSocketConnected = false;
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (Globals.DEBUG) {
                        LogUtil.debug(this.getClass().getName(), "on socket disconnect");
                    }
                    Globals.g_isSocketConnected = false;
                }
            }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (Globals.DEBUG) {
                        LogUtil.debug(this.getClass().getName(), "on socket connect timeout");
                    }
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理不同消息的逻辑
     *
     * @param msg 消息内容,json格式
     */
    private void handleMessage(String msgId, String msg) {
        Intent jumpIntent = new Intent();
        JSONObject customJson;
        String type;
        try {
            customJson = new JSONObject(msg);
            type = customJson.getString("type");
            String title = customJson.getString("title");
            String content = customJson.getString("content");
            //将需要保存的消息保存到消息中心
            String isSave = customJson.getString("isSave");

            if (!TextUtils.isEmpty(isSave) && isSave.equals("1")) {
                String url = customJson.getString("url").replace("＆", "&");
                String contentId = customJson.getString("id");
                MessageDao.getInstance(this).insertMsg(msgId, type, title, content, url, contentId);
            }
            if (type.equals(AppConstants.PUSH_NEWS)) { // 资讯新闻
                String newsUrl = customJson.getString("url").replace("＆", "&");
                jumpIntent.setClass(this, WebActivity.class);
                jumpIntent.putExtra("url", newsUrl);
                jumpIntent.putExtra("from", "notification");
                jumpIntent.putExtra("msgId", msgId);
                jumpIntent.putExtra("type", "0");
                jumpIntent.putExtra("source", "资讯");
                sendNotify(this, title, content, jumpIntent);
                sendBroadcast(type);
            } else if (type.equals(AppConstants.PUSH_GALLERY)) { // 资讯图集
                String galleryId = customJson.getString("id");
                jumpIntent.setClass(this, GalleryActivity.class);
                jumpIntent.putExtra("id", galleryId);
                jumpIntent.putExtra("msgId", msgId);
                jumpIntent.putExtra("from", "notification");
                jumpIntent.putExtra("type", "1");
                jumpIntent.putExtra("source", "图集");
                sendNotify(this, title, content, jumpIntent);
                sendBroadcast(type);
            } else if (type.equals(AppConstants.PUSH_TOPIC)) { // 资讯专题
                String topicId = customJson.getString("id");
                jumpIntent.setClass(this, TopicActivity.class);
                jumpIntent.putExtra("id", topicId);
                jumpIntent.putExtra("msgId", msgId);
                jumpIntent.putExtra("from", "notification");
                jumpIntent.putExtra("type", "2");
                jumpIntent.putExtra("source", "专题");
                sendNotify(this, title, content, jumpIntent);
                sendBroadcast(type);
            } else if (type.equals(AppConstants.PUSH_H5)) { // H5页面(投票、抽奖、内容收集)
                String h5Url = customJson.getString("url").replace("＆", "&");
                jumpIntent.setClass(this, WebActivity.class);
                jumpIntent.putExtra("url", h5Url);
                jumpIntent.putExtra("from", "notification");
                jumpIntent.putExtra("msgId", msgId);
                jumpIntent.putExtra("type", "0");
                jumpIntent.putExtra("source", "抽奖");
                sendNotify(this, title, content, jumpIntent);
                sendBroadcast(type);
            } else if (type.equals(AppConstants.PUSH_ADVICE_FEEDBACK)) { // 意见反馈跳转
                SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_SUGGEST, true);
                SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_SUGGEST_LIST, true);
                SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_SETTING, true);
                jumpIntent = new Intent(this, SuggestListActivity.class);
                sendNotify(this, title, content, jumpIntent);
                sendBroadcast(type);
            } else if (type.equals(AppConstants.PUSH_SYS_MESSAGE)) { // 系统消息
                jumpIntent.setClass(this, ReadMessageActivity.class);
                jumpIntent.putExtra("msgId", msgId);
                sendNotify(this, title, content, jumpIntent);
                sendBroadcast(type);
            } else if (type.equals(AppConstants.PUSH_ADV)) { //营销推广
                String newsUrl = customJson.getString("url").replace("＆", "&");
                jumpIntent.setClass(this, WebActivity.class);
                jumpIntent.putExtra("url", newsUrl);
                jumpIntent.putExtra("type", "0");
                jumpIntent.putExtra("from", "notification");
                jumpIntent.putExtra("msgId", msgId);
                jumpIntent.putExtra("source", "推广");
                sendNotify(this, title, content, jumpIntent);
                sendBroadcast(type);
            } else if (type.equals(AppConstants.PUSH_CLEAR_CACHE)) { // 清理缓存
                CacheCleanManager.deleteFolderFile("/data/data/package_name/database/webview.db", true);
                CacheCleanManager.deleteFolderFile("/data/data/package_name/database/webviewCache.db", true);
            } else if (type.equals(AppConstants.PUSH_BOTTLE) || type.equals(AppConstants.PUSH_CHAT)) {  // 漂流瓶和好友聊天
                Gson gson = new Gson();
                ArrayList<ChatMsgInfo> msgList = gson.fromJson(content, new TypeToken<ArrayList<ChatMsgInfo>>() {
                }.getType());
                if (msgList != null && msgList.size() > 0) {
                    for (ChatMsgInfo chatMsgInfo : msgList) {
                        chatMsgInfo.msgId = UUID.randomUUID().toString(); //生成一个msgId;
                        chatMsgInfo.sendStatus = 0;
                        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
                        //当前正在和此人聊天则标记为已读
                        if (type.equals(AppConstants.PUSH_BOTTLE)) {
                            if (runningActivity.equals(ChatActivity.class.getName()) && chatMsgInfo.chatId.equals(ChatActivity.mChatId)) {
                                chatMsgInfo.readStatus = 1;
                            } else {
                                chatMsgInfo.readStatus = 0;
                            }
                        } else if (type.equals(AppConstants.PUSH_CHAT)) {
                            if ((runningActivity.equals(ChatActivity.class.getName())) && (chatMsgInfo.userAccount.equals(ChatActivity.mFriendAccount))) {
                                chatMsgInfo.readStatus = 1;
                            } else {
                                chatMsgInfo.readStatus = 0;
                            }
                        }
                        //自己的信息标识为已读
                        if (chatMsgInfo.userAccount.equals(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT))) {
                            chatMsgInfo.readStatus = 1;
                        }
                        if (type.equals(AppConstants.PUSH_BOTTLE)) {
                            SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.MSG_BOTTLE, true);
                            handleChatMsg(chatMsgInfo, title, "0");
                        } else if (type.equals(AppConstants.PUSH_CHAT)) {
                            handleChatMsg(chatMsgInfo, title, "1");
                        }
                    }
                }
            } else if (type.equals(AppConstants.PUSH_FRIEND_APPLY)) {  //好友申请
                Gson gson = new Gson();
                ArrayList<FriendApplyInfo> applyInfos = gson.fromJson(content, new TypeToken<ArrayList<FriendApplyInfo>>() {
                }.getType());
                if (applyInfos != null && applyInfos.size() > 0) {
                    for (FriendApplyInfo applyInfo : applyInfos) {
                        handleFriendApplyMsg(applyInfo);
                    }
                }
            } else if (type.equals(AppConstants.PUSH_COMMENT_REPLY)) {  //评论回复
                int commentReplyCount = SharedPreferencesInfo.getTagInt(this, SharedPreferencesInfo.COMMENT_REPLY_COUNT) + 1;
                SharedPreferencesInfo.setTagInt(this, SharedPreferencesInfo.COMMENT_REPLY_COUNT, commentReplyCount);
                SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.NEW_COMMENT_REPLY, true);
                jumpIntent.setClass(this, MyCommentActivity.class);
                jumpIntent.putExtra("from", "notification");
                sendNotify(this, title, content, jumpIntent);
                sendBroadcast(type);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*******************************************以下为处理聊天信息*******************************************************/

    private void sendBroadcast(String type) {
        Intent intent = new Intent();
        intent.setAction(AppConstants.ACTION_REFRESH_RED_POINT);
        this.sendBroadcast(intent); // 发送广播提示新消息到达

        Intent intent2 = new Intent();
        intent2.putExtra("type", type);
        intent2.setAction(AppConstants.ACTION_SOCKET_PUSH);
        sendBroadcast(intent2);
    }

    private void handleFriendApplyMsg(FriendApplyInfo applyInfo) {
        if (TextUtils.isEmpty(applyInfo.content)) {
            applyInfo.content = getString(R.string.social_friend_apply_greetings);
        }
        FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(this);
        FriendApplyInfo friendApplyInfo = friendApplyDao.query(applyInfo.userAccount,
                SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        applyInfo.readStatus = "0";
        applyInfo.replyStatus = "0";
        if (friendApplyInfo != null && !TextUtils.isEmpty(friendApplyInfo.content)) {
            String[] temp = friendApplyInfo.content.split("\n");
            if (temp.length >= 3) {
                applyInfo.content = temp[1] + "\n" + temp[2] + "\n" + applyInfo.content;
            } else {
                applyInfo.content = friendApplyInfo.content + "\n" + applyInfo.content;
            }
        }
        friendApplyDao.insert(applyInfo, SharedPreferencesInfo.getTagString(PushService.this, SharedPreferencesInfo.ACCOUNT));

        SharedPreferencesInfo.setTagBoolean(this, SharedPreferencesInfo.MSG_FRIEND_APPLY, true);

        Intent jumpIntent = new Intent(this, FriendApplyListActivity.class);
        sendNotify(this, applyInfo.nickname, "请求添加你为好友", jumpIntent);

        sendBroadcast(AppConstants.PUSH_FRIEND_APPLY);
    }

    /**
     * 处理接收到的聊天信息
     *
     * @param chatMsgInfo
     * @param title
     * @param category    0:漂流瓶 1:好友聊天
     */
    private void handleChatMsg(ChatMsgInfo chatMsgInfo, String title, String category) {
        //语音消息需要下语音文件
        String fileName;
        //获取联系人信息
        ContactDao contactDao = ContactDao.getInstance(this);
        UserInfo userInfo = contactDao.queryUser(chatMsgInfo.userAccount);
        if (userInfo == null || TextUtils.isEmpty(userInfo.remark) || TextUtils.isEmpty(userInfo.nickname)) {
            getFriendInfo(chatMsgInfo.userAccount, chatMsgInfo, category, title);
        } else {
            //显示通知
            if ("1".equals(category)) {
                if (!TextUtils.isEmpty(userInfo.remark)) {
                    title = userInfo.remark;
                } else if (!TextUtils.isEmpty(userInfo.nickname)) {
                    title = userInfo.nickname;
                }
            }
            sendNotifyMessage(title, chatMsgInfo, category);
        }
        FriendsDao friendsDao = FriendsDao.getInstance(this);
        FriendsInfo friendsInfo = friendsDao.query(chatMsgInfo.userAccount, SharedPreferencesInfo.getTagString(PushService.this, SharedPreferencesInfo.ACCOUNT));
        if (friendsInfo == null) {
            friendsDao.insert(chatMsgInfo.userAccount, SharedPreferencesInfo.getTagString(PushService.this, SharedPreferencesInfo.ACCOUNT));
        }
        if (chatMsgInfo.type.equals("0")) { //文字瓶子
            if (SharedPreferencesInfo.getTagString(PushService.this, SharedPreferencesInfo.ACCOUNT)
                    .equals(chatMsgInfo.userAccount)) {
                return;
            }

            //更新消息列表
            updateMsg(chatMsgInfo, category);
            //保存到消息记录表
            ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(this);
            chatMsgInfo.receiveAccount = SharedPreferencesInfo.getTagString(PushService.this, SharedPreferencesInfo.ACCOUNT);
            if ("0".equals(category)) {
                chatMsgDao.saveChatMsgItem(chatMsgInfo, "bottle");
                sendBroadcast(AppConstants.PUSH_BOTTLE); //消息列表界面和聊天界面需要监听广播
            } else if ("1".equals(category)) {
                chatMsgDao.saveChatMsgItem(chatMsgInfo, "friend");
                sendBroadcast(AppConstants.PUSH_CHAT);
            }
        } else { //语音图片瓶子
            int index = chatMsgInfo.content.lastIndexOf("/");
            if (index < 0) {
                fileName = chatMsgInfo.content;
            } else {
                fileName = chatMsgInfo.content.substring(index);
            }
            downloadVoice(chatMsgInfo, fileName, title, category);
        }
    }

    private void updateMsg(ChatMsgInfo chatMsgInfo, String category) {
        ChatListItemInfo chatItem = new ChatListItemInfo();
        chatItem.chatId = chatMsgInfo.chatId;
        chatItem.type = chatMsgInfo.type;
        chatItem.content = chatMsgInfo.content;
        chatItem.updateTime = chatMsgInfo.date;
        chatItem.userAccount = chatMsgInfo.userAccount;
        chatItem.myAccount = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT);
        chatItem.position = chatMsgInfo.position;
        if ("0".equals(category)) { // 漂流瓶
            BottleListDao bottleListDao = BottleListDao.getInstance(this);
            bottleListDao.saveBottleListItem(chatItem);
        } else if ("1".equals(category)) { // 好友聊天
            ChatListDao chatListDao = ChatListDao.getInstance(this);
            chatListDao.insert(chatItem);
        }
    }

    /**
     * 下载语音文件
     *
     * @param chatMsgInfo
     * @param fileName
     */
    private void downloadVoice(final ChatMsgInfo chatMsgInfo, final String fileName, final String title, final String category) {
        new Thread() {
            public void run() {
                FileOutputStream fos = null;
                InputStream is = null;
                try {
                    URL url = new URL(chatMsgInfo.content);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    is = conn.getInputStream();
                    File file = new File(getCacheDir().getPath() + "/" + fileName);
                    fos = new FileOutputStream(file);
                    byte[] buf = new byte[512];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();

                    if (SharedPreferencesInfo.getTagString(PushService.this, SharedPreferencesInfo.ACCOUNT)
                            .equals(chatMsgInfo.userAccount)) {
                        return;
                    }

                    //更新消息列表
                    updateMsg(chatMsgInfo, category);
                    //保存到消息记录表
                    ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(PushService.this);
                    chatMsgInfo.receiveAccount = SharedPreferencesInfo.getTagString(PushService.this, SharedPreferencesInfo.ACCOUNT);
                    if ("0".equals(category)) {
                        chatMsgDao.saveChatMsgItem(chatMsgInfo, "bottle");
                        sendBroadcast(AppConstants.PUSH_BOTTLE); //消息列表界面和聊天界面需要监听广播
                    } else if ("1".equals(category)) {
                        //保存到消息记录表
                        chatMsgDao.saveChatMsgItem(chatMsgInfo, "friend");
                        sendBroadcast(AppConstants.PUSH_CHAT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void sendNotifyMessage(String title, ChatMsgInfo chatMsgInfo, String category) {
        Message msg = new Message();
        msg.what = 0;
        Map map = new HashMap();
        map.put("title", title);
        map.put("content", chatMsgInfo);
        map.put("category", category);
        msg.obj = map;
        mHdl.sendMessage(msg);
    }

    /**
     * 查询用户信息
     *
     * @param userAccount
     */
    private void getFriendInfo(final String userAccount, final ChatMsgInfo chatMsgInfo, final String category, final String title) {
        final GetFriendInfoRequestBody requestBody = new GetFriendInfoRequestBody();
        requestBody.friendAccount = userAccount;
        WebServiceIf.IResponseCallback getFriendInfoCallbackIf = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    UserInfoResponseObject responseObj = gson.fromJson(response, UserInfoResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            UserInfo userInfo = responseObj.body;
                            userInfo.userAccount = userAccount;

                            String tempTitle = "";
                            if ("1".equals(category)) {
                                if (!TextUtils.isEmpty(userInfo.remark)) {
                                    tempTitle = userInfo.remark;
                                } else if (!TextUtils.isEmpty(userInfo.nickname)) {
                                    tempTitle = userInfo.nickname;
                                }
                                sendNotifyMessage(tempTitle, chatMsgInfo, category);
                            } else {
                                sendNotifyMessage(title, chatMsgInfo, category);
                            }
                            if ("0".equals(category)) {
                                sendBroadcast(AppConstants.PUSH_BOTTLE); //消息列表界面和聊天界面需要监听广播
                            } else if ("1".equals(category)) {
                                sendBroadcast(AppConstants.PUSH_CHAT);
                            }

                            //将联系人数据存数据库
                            try {
                                ContactDao contactDao = ContactDao.getInstance(PushService.this);
                                contactDao.saveUser(userInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastUtil.showToast(PushService.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(PushService.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.getFriendInfo(this, requestBody, getFriendInfoCallbackIf);
    }

    /*********************************************以上为处理聊天信息***********************************************************/


    public class SocketReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AppConstants.ACTION_SOCKET_LOGIN.equals(intent.getAction())) {
                String userAccount = SharedPreferencesInfo.getTagString(context, SharedPreferencesInfo.ACCOUNT);
                if (!TextUtils.isEmpty(userAccount)) {
                    mSocket.emit("login", userAccount); //发送用户账号
                }
            } else if (AppConstants.ACTION_SOCKET_LOGOUT.equals(intent.getAction())) {
                mSocket.emit("logout"); //注销用户,变成匿名
            } else if (AppConstants.ACTION_SOCKET_CONNECTING.equals(intent.getAction())) {
                if (Globals.DEBUG) {
                    LogUtil.debug(PushService.this.getClass().getName(), "on receive ACTION_SOCKET_CONNECTING" + "    " + mSocket.connected());
                }
                if (mSocket == null) {
                    initSocket();
                } else if (!mSocket.connected()) {
                    if (Globals.DEBUG) {
                        LogUtil.debug(PushService.this.getClass().getName(), "try to connect socket");
                    }
                    mSocket.connect();
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                WakeupUtil.getInstance().acquireWakeLock(getApplicationContext());
                if (Globals.DEBUG) {
                    LogUtil.debug(PushService.this.getClass().getName(), "on receive ACTION_SCREEN_ON" + "    " + mSocket.connected());
                }
                if (mSocket == null) {
                    initSocket();
                } else if (!mSocket.connected()) {
                    if (Globals.DEBUG) {
                        LogUtil.debug(PushService.this.getClass().getName(), "try to connect socket");
                    }
                    mSocket.connect();
                }
            }
        }
    }
}
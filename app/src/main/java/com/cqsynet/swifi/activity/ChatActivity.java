/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：聊天界面
 *
 *
 * 创建标识：zhaosy	20161121
 */
package com.cqsynet.swifi.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.social.BottleFriendInfoActivity;
import com.cqsynet.swifi.activity.social.PersonInfoActivity;
import com.cqsynet.swifi.adapter.ChatMsgViewAdapter;
import com.cqsynet.swifi.db.BottleListDao;
import com.cqsynet.swifi.db.ChatListDao;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.model.ChatListItemInfo;
import com.cqsynet.swifi.model.ChatMsgInfo;
import com.cqsynet.swifi.model.GetFriendInfoRequestBody;
import com.cqsynet.swifi.model.MessageResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.SendMessageRequestBody;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.UserInfoResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.SoundMeterUtil;
import com.cqsynet.swifi.util.ToastUtil;
import com.google.gson.Gson;
import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends HkActivity implements View.OnClickListener, SensorEventListener {

    private static final int REQUEST_CODE_IMAGE = 0;
    public static String mFriendAccount; //对方的账号
    public static String mPosition; //对方的位置
    private ImageView mIvBack;
    private ImageView mIvProfile;
    private TextView mTvTitle;
    private ImageButton mIBtnTypeSwitcher;
    private EditText mEtContent;
    private ImageButton mIBtnMore;
    private ListView mLvChat;
    private RelativeLayout mRlHint; //按下录音按钮后的提示框
    private RelativeLayout mRlMic; //录音提示框种的麦克风
    private ImageView mIvCancel; //录音提示框中的取消箭头
    private ImageView mIvBottleCircle1;
    private ImageView mIvBottleCircle2;
    private TextView mTvSendVoice;
    private TextView mTvSendText;
    private TextView mTvHint;
    private ChatMsgViewAdapter mAdapter;
    private List<ChatMsgInfo> mChatList = new ArrayList<ChatMsgInfo>();
    private int mType; //0:文字 1:语音 2:图片
    public static String mChatId;
    private MessageReceiver mMessageReceiver; //监听推送消息
    private boolean mIsVoiceOvertime = false; //语音是否超过时长
    private boolean mIsCountdown = false; //是否显示倒计时(倒计时时不刷新滑动的提示)
    private int mTouchPosition = -1; //手指位置:0表示可以发送区域,1代表取消发送区域, -1是初始状态
    private SoundMeterUtil mSoundMeterUtil; //录音工具类
    private String mVoiceName; //音频名称
    private long mVoiceStartTime; //开始录音的时间
    private long mVoiceEndTime; //结束录音的时间
    private SensorManager mSensorManager; //传感器管理器
    private Sensor mSensor;  //传感器实例
    private float mProximity; //当前传感器距离
    private AudioManager mAudioManager; //声音管理器
    private boolean mIsRecording = false; //是否正在录音
    private HeadsetReceiver mHeadsetReceiver; //监听耳机
    private static final int MODE_SPEAKER = 0; //外放模式
    private static final int MODE_EARPIECE = 1; //听筒模式
    private static final int MODE_HEADSET = 2; //耳机模式
    private int mCurrentAudioMode = MODE_SPEAKER;
    private String mCategory; //聊天分类  0:漂流瓶 1:好友聊天
    private String mDraft = ""; // 草稿

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_SOCKET_PUSH);
        filter.addAction(AppConstants.ACTION_DELETE_FRIEND);
        filter.addAction(AppConstants.ACTION_MODIFY_REMARK);
        registerReceiver(mMessageReceiver, filter);
        //初始化声音管理器
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.setSpeakerphoneOn(true);
        //初始化距离感应
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //耳机插拔监听
        mHeadsetReceiver = new HeadsetReceiver();

        mCategory = getIntent().getStringExtra("category");
        mType = SharedPreferencesInfo.getTagInt(this, SharedPreferencesInfo.INPUT_TYPE, 1);

        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(ChatActivity.this);
        mChatList.clear();
        if ("0".equals(mCategory)) {
            mChatList.addAll(chatMsgDao.queryFromChatId(mChatId, SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT)));
        } else if ("1".equals(mCategory)) {
            mChatList.addAll(chatMsgDao.queryFromFriendAccount(mFriendAccount, SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT)));
        }
        mAdapter.notifyDataSetChanged();
        mLvChat.setSelection(mAdapter.getCount() - 1);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadsetReceiver, filter);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(mChatId.hashCode());

        for (ChatMsgInfo info : mChatList) {
            if (info.readStatus == 0) {
                info.readStatus = 1;
                chatMsgDao.saveChatMsgItem(info, info.category);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.stopVoice();
        }
        unregisterReceiver(mHeadsetReceiver);
        mSensorManager.unregisterListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMessageReceiver != null) {
            unregisterReceiver(mMessageReceiver);
        }
//        mAudioManager.setMode(AudioManager.MODE_NORMAL);

        String draft = "";
        if (!TextUtils.isEmpty(mEtContent.getText().toString())) {
            draft = mEtContent.getText().toString();
        }
        if (!draft.equals(mDraft)) {
            saveDraft(draft);
        }

        for (ChatMsgInfo info : mChatList) {
            if (info.sendStatus == 1) {
                info.sendStatus = 2;
                ChatMsgDao.getInstance(ChatActivity.this).saveChatMsgItem(info, info.category);
            }
        }

        Intent intent = new Intent(AppConstants.ACTION_UPDATE_DRAFT);
        sendBroadcast(intent);
    }

    private void saveDraft(String draft) {
        ChatListItemInfo chatItem = new ChatListItemInfo();
        chatItem.chatId = mChatId;
        chatItem.content = mChatList.size() > 0 ? mChatList.get(mChatList.size() - 1).content : "";
        String date;
        if (!TextUtils.isEmpty(draft) || mChatList.size() <= 0) {
            chatItem.type = "0";
            date = String.valueOf(System.currentTimeMillis());
        } else {
            chatItem.type = mChatList.get(mChatList.size() - 1).type;
            date = mChatList.get(mChatList.size() - 1).date;
        }
        chatItem.updateTime = date;
        chatItem.userAccount = mFriendAccount;
        chatItem.myAccount = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT);
        chatItem.position = mPosition;
        chatItem.draft = draft;
        if ("0".equals(mCategory)) {
            BottleListDao bottleListDao = BottleListDao.getInstance(this);
            bottleListDao.delBottleListItem(chatItem);
            bottleListDao.saveBottleListItem(chatItem);
        } else if ("1".equals(mCategory)) {
            ChatListDao chatListDao = ChatListDao.getInstance(this);
            chatListDao.delete(chatItem);
            chatListDao.insert(chatItem);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initData();
    }

    public void initView() {
        mIvBack = findViewById(R.id.iv_back);
        mIvProfile = findViewById(R.id.iv_mine);
        mTvTitle = findViewById(R.id.tv_title);
        mIBtnTypeSwitcher = findViewById(R.id.btnTypeSwitcher_activity_chat);
        mTvSendText = findViewById(R.id.btnSendText_activity_chat);
        mTvSendVoice = findViewById(R.id.btnSendVoice_activity_chat);
        mEtContent = findViewById(R.id.etContent_activity_chat);
        mIBtnMore = findViewById(R.id.btnMore_activity_chat);
        mLvChat = findViewById(R.id.lvChatList_activity_chat);
        mRlHint = findViewById(R.id.rlHint_activity_chat);
        mRlMic = findViewById(R.id.rlMic_activity_chat);
        mIvCancel = findViewById(R.id.ivCancel_activity_chat);
        mIvBottleCircle1 = findViewById(R.id.ivCircle1_activity_chat);
        mIvBottleCircle2 = findViewById(R.id.ivCircle2_activity_chat);
        mTvHint = findViewById(R.id.tvHint_activity_chat);
        mIvBack.setOnClickListener(this);
        mIvProfile.setOnClickListener(this);
        mIBtnTypeSwitcher.setOnClickListener(this);
        mIBtnMore.setOnClickListener(this);
        mTvSendText.setOnClickListener(this);
        mTvSendVoice.setOnClickListener(this);

        mSoundMeterUtil = new SoundMeterUtil(this, new SoundMeterUtil.OnRecordListener() {
            @Override
            public void onRecordOvertime(int remainTime) {
                if (remainTime > 0 && remainTime <= 10 && mTouchPosition == 0) {
                    mTvHint.setTextColor(getResources().getColor(R.color.text4));
                    mTvHint.setBackgroundColor(getResources().getColor(R.color.transparent));
                    mTvHint.setText("还可以说" + remainTime + "秒");
                    mIsCountdown = true;
                } else if (remainTime <= 0) {
                    mIsVoiceOvertime = true;
                    mIsCountdown = false;
                    mTvSendVoice.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, mTvSendVoice.getX(), mTvSendVoice.getY(), 0));
                }
            }
        });

        //点击发送语音
        mTvSendVoice.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0;
            float startY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!AndPermission.hasPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO)) { //录音权限没打开
                    ToastUtil.showToast(ChatActivity.this, "录音失败,请到手机应用设置内检查是否开启录音权限");
                    return false;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mAdapter.stopVoice();
                    mHdl.sendEmptyMessageDelayed(1, 300);
                    mTouchPosition = -1;
                    mIsRecording = true;
                    mIsVoiceOvertime = false;
                    mIsCountdown = false;
                    startX = motionEvent.getX();
                    startY = motionEvent.getY();
                    mTvSendVoice.setBackgroundResource(R.drawable.round_rect_grey2);
                    mRlHint.setVisibility(View.VISIBLE);
                    mRlMic.setVisibility(View.VISIBLE);
                    mTvHint.setTextColor(getResources().getColor(R.color.text4));
                    mTvHint.setBackgroundColor(getResources().getColor(R.color.transparent));
                    mTvHint.setText(R.string.voice_hint2);
                    mIvCancel.setVisibility(View.INVISIBLE);
                    mVoiceStartTime = System.currentTimeMillis();
                    mVoiceName = mVoiceStartTime + ".amr";
                    mSoundMeterUtil.start(mVoiceName);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mHdl.removeMessages(1);
                    mSoundMeterUtil.abandonAudioFocus();
                    if (mIsRecording == false) { //语音太长,已通过超时机制发送,此时不再发
                        return false;
                    }
                    mSoundMeterUtil.stop();
                    mTvSendVoice.setBackgroundResource(R.drawable.btn_send_msg);
                    mIvBottleCircle1.clearAnimation();
                    mIvBottleCircle2.clearAnimation();
                    mRlHint.setVisibility(View.GONE);
                    mVoiceEndTime = System.currentTimeMillis();
                    if (mTouchPosition == 0 || mIsVoiceOvertime) {
                        int time = (int) ((mVoiceEndTime - mVoiceStartTime) / 1000); //音频时长
                        if (time < 1) {
                            ToastUtil.showToast(ChatActivity.this, R.string.voice_too_short);
                            mTvSendVoice.setText(R.string.voice_hint3);
                            File file = new File(getCacheDir().getPath() + "/" + mVoiceName);
                            if (file.exists()) {
                                file.delete();
                            }
                            return true;
                        } else {
                            //保存发送的消息并刷新界面
                            File file = new File(getCacheDir().getPath() + "/" + mVoiceName);
                            if (file.exists() && file.length() > 100) { //就算未录音成功,某些手机也会生成一个文件,坚果手机生成的文件大小为6
                                saveMessage("1", getCacheDir().getPath() + "/" + mVoiceName);
                            } else {
                                ToastUtil.showToast(ChatActivity.this, "录音失败,请到手机应用设置内检查是否开启录音权限");
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                        }
                    } else if (mTouchPosition == 1) {
                        File file = new File(getCacheDir().getPath() + "/" + mVoiceName);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    mIsRecording = false;
                    mIsCountdown = false;
                    mTvSendVoice.setText(R.string.voice_hint3);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    if (mIsRecording == false) {
                        return false;
                    }
                    if (startY - motionEvent.getY() > AppUtil.dp2px(ChatActivity.this, 120)) {
                        if (mTouchPosition != 1) {
                            mTouchPosition = 1;
                            mTvHint.setText(R.string.voice_hint1);
                            mTvHint.setTextColor(getResources().getColor(R.color.white));
                            mTvHint.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_rect_red2));
                            mTvSendVoice.setText(R.string.voice_hint1);
                            mRlMic.setVisibility(View.INVISIBLE);
                            mIvBottleCircle1.clearAnimation();
                            mIvBottleCircle2.clearAnimation();
                            mIvCancel.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mTouchPosition != 0) {
                            mTouchPosition = 0;
                            Animation animCircle = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.mic_circle);
                            mIvBottleCircle1.startAnimation(animCircle);
                            mHdl.sendEmptyMessageDelayed(0, 500);
                            if (!mIsCountdown) {
                                mTvHint.setTextColor(getResources().getColor(R.color.text4));
                                mTvHint.setBackgroundColor(getResources().getColor(R.color.transparent));
                                mTvHint.setText(R.string.voice_hint2); //倒计时的时候不设置
                            }
                            mTvSendVoice.setText(R.string.voice_hint4);
                            mRlMic.setVisibility(View.VISIBLE);
                            mIvCancel.setVisibility(View.INVISIBLE);
                        }
                    }
                }
                return true;
            }
        });

        //切换显示"发送"按钮和"+"按钮
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    mTvSendText.setVisibility(View.VISIBLE);
                    mIBtnMore.setVisibility(View.GONE);
                } else {
                    mTvSendText.setVisibility(View.GONE);
                    mIBtnMore.setVisibility(View.VISIBLE);
                }
            }
        });

        mLvChat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showPopupDialog(i);
                return true;
            }
        });

        //根据上次的输入习惯切换输入方式
        if(mType == 0) {
            switchToText();
        } else if(mType == 1) {
            switchToVoice();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                InputMethodManager ime = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                ime.hideSoftInputFromWindow(v.getWindowToken(), 0);
                finish();
                break;
            case R.id.iv_mine:
                if ("0".equals(mCategory)) {
                    Intent intent = new Intent();
                    intent.setClass(ChatActivity.this, BottleFriendInfoActivity.class);
                    intent.putExtra("friendAccount", mFriendAccount);
                    intent.putExtra("chatId", mChatId);
                    intent.putExtra("position", mPosition);
                    startActivity(intent);
                } else if ("1".equals(mCategory)) {
                    Intent friendIntent = new Intent();
                    friendIntent.setClass(ChatActivity.this, PersonInfoActivity.class);
                    friendIntent.putExtra("friendAccount", mFriendAccount);
                    friendIntent.putExtra("isFriend", "1");
                    friendIntent.putExtra("category", mCategory);
                    startActivity(friendIntent);
                }
                break;
            case R.id.btnTypeSwitcher_activity_chat: //语音文字切换按钮
                if (mType == 0) { //从文字到语音
                    switchToVoice();
                } else if (mType == 1) { //从语音到文字
                    switchToText();
                }
                break;
            case R.id.btnSendText_activity_chat:
                //保存发送的消息并刷新界面
                if (!TextUtils.isEmpty(mEtContent.getText().toString().trim())) {
                    saveMessage("0", mEtContent.getText().toString());
                } else {
                    ToastUtil.showToast(ChatActivity.this, "不能发送空白消息");
                }
                break;
            case R.id.btnMore_activity_chat:
                //选择图片
                Intent intent = new Intent(this, SelectionPictureActivity.class);
                intent.putExtra("title", "选择图片");
                intent.putExtra("isNeedCut", false);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
                break;
        }
    }

    private void switchToVoice() {
        mType = 1;
        mIBtnTypeSwitcher.setBackgroundResource(R.drawable.btn_keyboard_selector);
        mEtContent.setVisibility(View.GONE);
        mIBtnMore.setVisibility(View.GONE);
        mTvSendText.setVisibility(View.GONE);
        mTvSendVoice.setVisibility(View.VISIBLE);
        mTvSendVoice.setText("按住 说话");
        //收起软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mIBtnTypeSwitcher.getWindowToken(), 0);
        SharedPreferencesInfo.setTagInt(this, SharedPreferencesInfo.INPUT_TYPE, 1);
    }

    private void switchToText() {
        mType = 0;
        mIBtnTypeSwitcher.setBackgroundResource(R.drawable.btn_voice_selector);
        mTvSendVoice.setVisibility(View.GONE);
        mEtContent.setVisibility(View.VISIBLE);
        mEtContent.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEtContent, 0);
        if (mEtContent.getText().length() > 0) {
            mTvSendText.setVisibility(View.VISIBLE);
            mIBtnMore.setVisibility(View.GONE);
        } else {
            mTvSendText.setVisibility(View.GONE);
            mIBtnMore.setVisibility(View.VISIBLE);
        }
        SharedPreferencesInfo.setTagInt(this, SharedPreferencesInfo.INPUT_TYPE, 0);
    }

    Handler mHdl = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Animation animCircle = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.mic_circle);
                    mIvBottleCircle2.startAnimation(animCircle);
                    break;
                case 1:
                    mSoundMeterUtil.requestAudioFocus();
                    break;
            }
        }
    };


    /**
     * 初始化聊天数据
     */
    public void initData() {
        String owner = getIntent().getStringExtra("owner");
        if (!TextUtils.isEmpty(owner) &&
                !owner.equals(SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT))) {
            ToastUtil.showToast(this, R.string.msg_invalid);
            finish();
        }

        mChatId = getIntent().getStringExtra("chatId");
        mPosition = getIntent().getStringExtra("position");
        mFriendAccount = getIntent().getStringExtra("userAccount");
        ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(this);
        if ("0".equals(mCategory)) {
            mChatList = chatMsgDao.queryFromChatId(mChatId, SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        } else if ("1".equals(mCategory)) {
            mChatList = chatMsgDao.queryFromFriendAccount(mFriendAccount, SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        }


        if (!TextUtils.isEmpty(mFriendAccount)) {
            if ("0".equals(mCategory)) {
                mTvTitle.setText("来自" + mPosition + "的瓶子");
            } else if ("1".equals(mCategory)) {
                ContactDao contactDao = ContactDao.getInstance(this);
                UserInfo userInfo = contactDao.queryUser(mFriendAccount);
                if (userInfo != null) {
                    if (!TextUtils.isEmpty(userInfo.remark)) {
                        mTvTitle.setText(userInfo.remark);
                    } else {
                        mTvTitle.setText(userInfo.nickname);
                    }
                }
            }
            mAdapter = new ChatMsgViewAdapter(this, mChatList, mCategory, mPosition);
            mLvChat.setAdapter(mAdapter);
            mLvChat.setSelection(mAdapter.getCount() - 1);
        } else {
            ToastUtil.showToast(this, "该对话不存在或者已被删除");
        }

        ChatListItemInfo itemInfo = null;
        if ("0".equals(mCategory)) {
            BottleListDao bottleListDao = BottleListDao.getInstance(this);
            itemInfo = bottleListDao.queryBottleListItem(mChatId);
        } else if ("1".equals(mCategory)) {
            ChatListDao chatListDao = ChatListDao.getInstance(this);
            itemInfo = chatListDao.query(mFriendAccount, SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT));
        }
        if (itemInfo != null && !TextUtils.isEmpty(itemInfo.draft)
                && !TextUtils.isEmpty(itemInfo.userAccount)
                && itemInfo.userAccount.equals(mFriendAccount)) {
            switchToText();
            mDraft = itemInfo.draft;
            mEtContent.setText(itemInfo.draft);
            mEtContent.setSelection(itemInfo.draft.length());
        }

    }

    /**
     * 发送聊天消息
     *
     * @param type
     * @param content
     */
    private void sendMessage(String type, String content, final int position, String msgId) {
        if (type.equals("0")) {
            mEtContent.setText("");
        }
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.category = mCategory;
        requestBody.type = type;
        requestBody.chatId = mChatId;
        requestBody.content = content;
        requestBody.msgId = msgId;
        requestBody.friendAccount = mFriendAccount;
        ArrayList<File> files = new ArrayList<>();
        //语音或者图片时,添加文件
        if (!type.equals("0")) {
            files.add(new File(content));
        }
        WebServiceIf.IResponseCallback callbackIf = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null && position < mChatList.size()) {
                    Gson gson = new Gson();
                    MessageResponseObject responseObj = gson.fromJson(response, MessageResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            //修改进度条状态为已发送
                            mChatList.get(position).sendStatus = 0;
                            mAdapter.notifyDataSetChanged();

                            ChatMsgInfo chatMsgInfo = mChatList.get(position);
                            chatMsgInfo.date = String.valueOf(responseObj.body.date);
                            chatMsgInfo.userAccount = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);
                            chatMsgInfo.receiveAccount = mFriendAccount;
                            chatMsgInfo.owner = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);

                            //更新数据库中的进度条状态
                            ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(ChatActivity.this);
                            if ("0".equals(mCategory)) {
                                chatMsgDao.saveChatMsgItem(chatMsgInfo, "bottle");
                            } else if ("1".equals(mCategory)){
                                chatMsgDao.saveChatMsgItem(chatMsgInfo, "friend");
                            }

                            updateChatList(chatMsgInfo);
                        } else {
                            //修改进度条为发送失败
                            mChatList.get(position).sendStatus = 2;
                            mAdapter.notifyDataSetChanged();
                            //更新数据库中的进度条状态
                            ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(ChatActivity.this);
                            ChatMsgInfo chatMsgInfo = mChatList.get(position);
                            chatMsgInfo.userAccount = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);
                            chatMsgInfo.receiveAccount = mFriendAccount;
                            chatMsgInfo.owner = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);
                            if ("0".equals(mCategory)) {
                                chatMsgDao.saveChatMsgItem(chatMsgInfo, "bottle");
                            } else if ("1".equals(mCategory)){
                                chatMsgDao.saveChatMsgItem(chatMsgInfo, "friend");
                            }
                            if (header.errCode.equals("36113")) {
                                ToastUtil.showToast(ChatActivity.this, "对方生气了,不想再收到你的消息了");
                            } else if (header.errCode.equals("36116")) {
                                ToastUtil.showToast(ChatActivity.this, "该用户被冻结,无法收到你的消息");
                            } else if (header.errCode.equals("36168")) {
                                ChatMsgInfo tip = new ChatMsgInfo();
                                tip.msgId = UUID.randomUUID().toString();
                                tip.type = "3";
                                tip.userAccount = mFriendAccount;
                                tip.receiveAccount = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);
                                tip.content = "对方已经开启了好友验证，你还不是TA的好友，请先发送验证请求。";
                                tip.readStatus = 1;
                                tip.sendStatus = 2;
                                tip.date = String.valueOf(System.currentTimeMillis());
                                tip.owner = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);
                                mChatList.add(tip);
                                mAdapter.notifyDataSetChanged();
                                mLvChat.setSelection(mLvChat.getCount() - 1);

                                if ("0".equals(mCategory)) {
                                    chatMsgDao.saveChatMsgItem(tip, "bottle");
                                } else if ("1".equals(mCategory)){
                                    chatMsgDao.saveChatMsgItem(tip, "friend");
                                }
                            } else {
                                ToastUtil.showToast(ChatActivity.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                            }
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                if (position >= mChatList.size()) {
                    return;
                }
                //修改进度条为发送失败
                mChatList.get(position).sendStatus = 2;
                mAdapter.notifyDataSetChanged();
                //更新数据库中的进度条状态
                ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(ChatActivity.this);
                ChatMsgInfo chatMsgInfo = mChatList.get(position);
                chatMsgInfo.userAccount = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);
                chatMsgInfo.receiveAccount = mFriendAccount;
                chatMsgInfo.owner = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);
                if ("0".equals(mCategory)) {
                    chatMsgDao.saveChatMsgItem(chatMsgInfo, "bottle");
                } else if ("1".equals(mCategory)){
                    chatMsgDao.saveChatMsgItem(chatMsgInfo, "friend");
                }
                ToastUtil.showToast(ChatActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.sendMessage(this, files, requestBody, callbackIf);
    }

    /**
     * 更新聊天列表数据
     *
     * @param chatMsgInfo
     */
    private void updateChatList(ChatMsgInfo chatMsgInfo) {
        ChatListItemInfo chatItem = new ChatListItemInfo();
        chatItem.chatId = chatMsgInfo.chatId;
        chatItem.type = chatMsgInfo.type;
        chatItem.content = chatMsgInfo.content;
        chatItem.updateTime = chatMsgInfo.date;
        chatItem.userAccount = mFriendAccount;
        chatItem.myAccount = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);
        chatItem.position = mPosition;
        if ("0".equals(mCategory)) { // 漂流瓶
            //更新消息列表
            BottleListDao bottleListDao = BottleListDao.getInstance(ChatActivity.this);
            bottleListDao.saveBottleListItem(chatItem);
        } else if ("1".equals(mCategory)) { // 好友聊天
            ChatListDao chatListDao = ChatListDao.getInstance(ChatActivity.this);
            chatListDao.insert(chatItem);
        }

        //获取联系人信息
        ContactDao contactDao = ContactDao.getInstance(this);
        UserInfo userInfo = contactDao.queryUser(chatMsgInfo.userAccount);
        if (userInfo == null) {
            getFriendInfo(chatMsgInfo.userAccount);
        }
    }

    private void getFriendInfo(final String userAccount) {
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
                            //将联系人数据存数据库
                            try {
                                ContactDao contactDao = ContactDao.getInstance(ChatActivity.this);
                                contactDao.saveUser(userInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastUtil.showToast(ChatActivity.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(ChatActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.getFriendInfo(this, requestBody, getFriendInfoCallbackIf);
    }

    /**
     * 保存消息到数据库并刷新界面,然后发送
     *
     * @param type
     * @param content
     */
    private void saveMessage(String type, String content) {
        //保存发送的消息并刷新界面
        ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(ChatActivity.this);
        ChatMsgInfo chatMsgInfo = new ChatMsgInfo();
        chatMsgInfo.msgId = UUID.randomUUID().toString();
        chatMsgInfo.type = type;
        chatMsgInfo.userAccount = SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT);
        chatMsgInfo.receiveAccount = mFriendAccount;
        chatMsgInfo.chatId = mChatId;
        chatMsgInfo.content = content;
        chatMsgInfo.sendStatus = 1;
        chatMsgInfo.readStatus = 1;
        //初始化时,把date设置为上一条消息+1毫秒,等到信息发送成功后,再更新为服务器返回的时间
        List<ChatMsgInfo> msgList = new ArrayList<>();
        if ("0".equals(mCategory)) {
            msgList = chatMsgDao.queryFromChatId(mChatId, SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        } else if ("1".equals(mCategory)) {
            msgList = chatMsgDao.queryFromFriendAccount(mFriendAccount, SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        }
        if (msgList.size() > 0) {
            chatMsgInfo.date = String.valueOf(Long.parseLong(msgList.get(msgList.size() - 1).date) + 1);
        } else {
            chatMsgInfo.date = System.currentTimeMillis() + "";
        }
        if ("0".equals(mCategory)) {
            chatMsgDao.saveChatMsgItem(chatMsgInfo, "bottle");
        } else if ("1".equals(mCategory)){
            chatMsgDao.saveChatMsgItem(chatMsgInfo, "friend");
        }
        mChatList.add(chatMsgInfo);
        mAdapter.notifyDataSetChanged();
        mLvChat.setSelection(mLvChat.getCount() - 1);
        updateChatList(chatMsgInfo);
        sendMessage(type, content, mChatList.size() - 1, chatMsgInfo.msgId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("file")) {
                    String filePath = data.getStringExtra("file");
                    File file = new File(filePath);
                    if (file.exists() && file.length() > 5 * 1000 * 1000) {
                        ToastUtil.showToast(ChatActivity.this, "只能发送尺寸小于5MB的图片");
                    } else {
                        //保存发送的消息并刷新界面
                        saveMessage("2", filePath);
                    }
                }
            }
        }
    }

    /**
     * 长按弹出框
     *
     * @param position
     */
    private void showPopupDialog(final int position) {
        final ChatMsgInfo chatMsgInfo = mChatList.get(position);
        String[] tempAry = null;
        if (chatMsgInfo.sendStatus == 2) {
            if (chatMsgInfo.type.equals("0")) {
                tempAry = getResources().getStringArray(R.array.chat_popup_1);
            } else {
                tempAry = getResources().getStringArray(R.array.chat_popup_4);
            }
        } else {
            if (chatMsgInfo.type.equals("0")) {
                tempAry = getResources().getStringArray(R.array.chat_popup_2);
            } else {
                tempAry = getResources().getStringArray(R.array.chat_popup_3);
            }
        }
        final String[] itemAray = tempAry;
        final CustomDialog dialog = new CustomDialog(ChatActivity.this, R.style.round_corner_dialog, R.layout.listview_chat_popup);
        ListView listview = dialog.getCustomView().findViewById(R.id.lv_listview_chat_popup);
        listview.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return itemAray.length;
            }

            @Override
            public Object getItem(int i) {
                return itemAray[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                view = View.inflate(ChatActivity.this, R.layout.chat_popup, null);
                TextView tvTitle = view.findViewById(R.id.tvTitle_chat_popup);
                tvTitle.setText(itemAray[i]);
                return view;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (itemAray[i].equals("删除")) {
                    ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(ChatActivity.this);
                    chatMsgDao.delChatMsg(chatMsgInfo.msgId);
                    mChatList.remove(position);
                    mAdapter.notifyDataSetChanged();
                } else if (itemAray[i].equals("重发")) {
                    sendMessage(chatMsgInfo.type, chatMsgInfo.content, position, chatMsgInfo.msgId);
                } else if (itemAray[i].equals("复制")) {
                    copyText(chatMsgInfo.content);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    protected void copyText(String text) {
        Uri uri = Uri.parse(text);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(
                intent, PackageManager.GET_RESOLVED_FILTER);
        if (resolveInfos.size() > 0) {
            startActivity(Intent.createChooser(intent, "复制文字"));
        } else {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("magnet", text);
            clipboard.setPrimaryClip(clip);
            ToastUtil.showToast(this, "文字已复制");
        }
    }

    /**
     * 监听到新信息刷新界面
     */
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppConstants.ACTION_SOCKET_PUSH.equals(action)) {
                String type = intent.getStringExtra("type");
                if (!TextUtils.isEmpty(type) && (type.equals(AppConstants.PUSH_BOTTLE) || type.equals(AppConstants.PUSH_CHAT))) {
                    ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(ChatActivity.this);
                    mChatList.clear();
                    if ("0".equals(mCategory)) {
                        mChatList.addAll(chatMsgDao.queryFromChatId(mChatId, SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT)));
                    } else if ("1".equals(mCategory)) {
                        mChatList.addAll(chatMsgDao.queryFromFriendAccount(mFriendAccount, SharedPreferencesInfo.getTagString(ChatActivity.this, SharedPreferencesInfo.ACCOUNT)));
                    }
                    mAdapter.notifyDataSetChanged();
                    mLvChat.setSelection(mAdapter.getCount() - 1);
                }
            } else if (AppConstants.ACTION_DELETE_FRIEND.equals(action)) {
                finish();
            } else if (AppConstants.ACTION_MODIFY_REMARK.equals(action)) {
                if ("0".equals(mCategory)) {
                    mTvTitle.setText("来自" + mPosition + "的瓶子");
                } else if ("1".equals(mCategory)) {
                    ContactDao contactDao = ContactDao.getInstance(ChatActivity.this);
                    UserInfo userInfo = contactDao.queryUser(mFriendAccount);
                    if (userInfo != null) {
                        if (!TextUtils.isEmpty(userInfo.remark)) {
                            mTvTitle.setText(userInfo.remark);
                        } else {
                            mTvTitle.setText(userInfo.nickname);
                        }
                    }
                }
            }
        }
    }

    /**
     * 距离感应
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (mCurrentAudioMode != MODE_HEADSET) {
            mProximity = sensorEvent.values[0];
            System.out.println("@@@@@@@ proximity = " + mProximity + "         max = " + mSensor.getMaximumRange());
            if (mProximity >= mSensor.getMaximumRange()) {
                mCurrentAudioMode = MODE_SPEAKER;
                mAudioManager.setSpeakerphoneOn(true);
            } else {
                mCurrentAudioMode = MODE_EARPIECE;
                mAudioManager.setSpeakerphoneOn(false);//关闭扬声器
                mAdapter.restartMediaPlayer();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


    /**
     * 拦截系统按键, 控制音量
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            System.out.println("volume down   " + mAudioManager.getMode());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            } else {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            System.out.println("volume up");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            } else {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 接收耳机插拔广播
     */
    private class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                //插入和拔出耳机会触发此广播
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", 0);
                    if (state == 1) {
                        //耳机已插入
                        mCurrentAudioMode = MODE_HEADSET;
                        mAudioManager.setSpeakerphoneOn(false);
                    } else if (state == 0) {
                        //耳机已拔出
                        mCurrentAudioMode = MODE_SPEAKER;
                        mAudioManager.setSpeakerphoneOn(true);
                    }
                    break;
            }
        }
    }
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：漂流瓶首页
 *
 *
 * 创建标识：zhaosy 20161109
 */
package com.cqsynet.swifi.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.BottleListDao;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.ChatListItemInfo;
import com.cqsynet.swifi.model.ChatMsgInfo;
import com.cqsynet.swifi.model.GetFriendInfoRequestBody;
import com.cqsynet.swifi.model.PickBottleResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.ResponseObject;
import com.cqsynet.swifi.model.ReturnBottleRequestBody;
import com.cqsynet.swifi.model.ThrowBottleRequestBody;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class BottleActivity extends HkActivity implements OnClickListener, SensorEventListener {

    private static final int PICK_BOTTLE = 0;
    private static final int DOWNLOAD_VOICE_FINISHED = 1;
    private static final int PICK_BOTTLE_FINISHED = 2;
    private static final int PICK_BOTTLE_FAILED = 3;
    private static final int CIRCLE_ANIMATION = 4;
    private static final int THROW_RIPPE_ANIMATION = 5;
    private static final int PICK_RIPPLE_ANIMATION = 6;
    private static final int PICK_BOTTLE_CLEAR_STATE = 7;
    private RelativeLayout mRlBg; // 背景层
    private ImageView mIvThrowBottle; //丢瓶子
    private ImageView mIvPickBottle; //捡瓶子
    private ImageView mIvMyBottle; //我的瓶子
    private TextView mTvRedPoint; //未读消息红点
    private RelativeLayout mRlCover; //发送消息的图层
    private RelativeLayout mRlBlankView; //发送消息图层的黑色空白处
    private ImageView mIvThrowStart; // 刚开始丟出去的瓶子
    private ImageView mIvThrowEnd; // 落水时的瓶子
    private ImageView mIvPickStart;
    private ImageView mIvPickEnd;
    private ImageView mIvPickSprayLeft; // 渔网入水时的水花
    private ImageView mIvPickSprayRight; // 渔网入水时的水波纹
    private ImageView mIvPickRippleLeft;
    private ImageView mIvPickRippleRight;
    private ImageView mIvFishingRippleLeft; // 渔网晃动时的水波纹
    private ImageView mIvFishingRippleRight;
    private ImageView mIvFishingNetMark;
    private LinearLayout mLlContent; //文字输入框布局
    private EditText mEtContent; //丢瓶子文字信息输入框
    private TextView mTvCharCount; //输入字数统计
    private ImageButton mIBtnTypeSwitcher; //切换文字语音
    private RelativeLayout mRlMic; //话筒
    private TextView mTvCount; //语音倒计时
    private TextView mTvSend; //发送按钮
    private ImageView mIvHint; //提示的图片
    private RelativeLayout mRlBottleLayer; //瓶子层(复用:1.丢瓶子动画层; 2.捡到瓶子后的显示)
    private ImageView mIvLight; //瓶子下面的光圈
    private RelativeLayout mRlNoBottleLayer; //捞瓶子失败后的层
    private ImageView mIvNoBottle;
    private RelativeLayout mRlResponseLayer; //捡到瓶子的回复层
    private ImageView mIvBottle; //动画瓶子
    private ImageView mIvBottleCircle1; //麦克风光圈
    private ImageView mIvBottleCircle2; //麦克风光圈
    private ImageView mIvMic; //麦克风图标
    private TextView mTvHint; //发送语音时的提示语
    private FrameLayout mFlFishingNet; // 渔网和捞到的瓶子或海星层
    private ImageView mIvFishingNet; //渔网
    private RelativeLayout mRlWaterRipple;
    private ImageView mIvDrop1;
    private ImageView mIvDrop2;
    private ImageView mIvDrop3;
    private ImageView mIvRipple1;
    private ImageView mIvRipple2;
    private ImageView mIvRipple3;
    private ImageView mIvFishingBottle; // 网中的瓶子
    private ImageView mIvHead; //头像
    private TextView mTvNickname; //昵称
    private TextView mTvSign; //签名
    private TextView mTvComplain; //投诉
    private TextView mTvTextContent; //捡到瓶子后的文本内容
    private RelativeLayout mRlVoiceContent;  //语音瓶子内容
    private TextView mTvVoiceTime; //语音持续时间
    private ImageView mIvVoice; //语音动画
    private Button mBtnThrowBack; //扔回水里
    private Button mBtnResponse; //回应
    private int mType = 1; //0:文字 1:语音
    private AnimatorSet mAnimatorFishingSet;
    private ChatListItemInfo mChatItem; //瓶子
    private SoundMeterUtil mSoundMeterUtil; //录音工具类
    private String mVoiceName; //音频名称
    private long mVoiceStartTime; //开始录音的时间
    private long mVoiceEndTime; //结束录音的时间
    private MediaPlayer mMediaPlayer;
    private String mFriendAccount; //对方的账号
    private String mFriendHeadUrl; //对方的头像url
    private boolean mIsVoiceOvertime = false; //语音是否超过时长
    private boolean mIsRecording = false; //是否正在录音
    private int mTouchPosition = 0; //手指位置:0表示可以发送区域,1代表取消发送区域
    private float[] mBottlePosition = new float[2]; // 丢出去的瓶子移动轨迹
    private float mBottleRotationRate = 100; // 丢出去的瓶子旋转比率
    private float mBottleScaleRate = 1; // 丢出去的瓶子缩放比率
    private float mFishingNetScaleRate = 0.3f; // 网子的缩放比率
    private boolean mIsEmptyBottle = true; // 是否是空瓶子
    private String errorCode; // 捞到空瓶子时的错误码
    private MessageReceiver mMessageReceiver;

    private SensorManager mSensorManager; //传感器管理器
    private Sensor mSensor;  //传感器实例
    private float mProximity; //当前传感器距离
    private AudioManager mAudioManager; //声音管理器
    private HeadsetReceiver mHeadsetReceiver; //监听耳机
    private static final int MODE_SPEAKER = 0; //外放模式
    private static final int MODE_EARPIECE = 1; //听筒模式
    private static final int MODE_HEADSET = 2; //耳机模式
    private int mCurrentAudioMode = MODE_SPEAKER;

    private Handler mHdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PICK_BOTTLE:
                    pickBottle();
                    break;
                case DOWNLOAD_VOICE_FINISHED:
                    ChatMsgInfo chatMsgInfo = (ChatMsgInfo) msg.obj;
                    //保存到消息记录表
                    ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(BottleActivity.this);
                    chatMsgDao.saveChatMsgItem(chatMsgInfo, "bottle");
                    //更新消息列表
                    BottleListDao bottleListDao = BottleListDao.getInstance(BottleActivity.this);
                    mChatItem = new ChatListItemInfo();
                    mChatItem.chatId = chatMsgInfo.chatId;
                    mChatItem.type = chatMsgInfo.type;
                    mChatItem.content = chatMsgInfo.content;
                    mChatItem.updateTime = chatMsgInfo.date;
                    mChatItem.userAccount = chatMsgInfo.userAccount;
                    mChatItem.myAccount = SharedPreferencesInfo.getTagString(BottleActivity.this, SharedPreferencesInfo.ACCOUNT);
                    mChatItem.position = chatMsgInfo.position;
                    mChatItem.draft = "";
                    bottleListDao.saveBottleListItem(mChatItem);
                    //动画结束
                    mHdl.sendEmptyMessage(PICK_BOTTLE_FINISHED);
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(BottleActivity.this, Uri.fromFile((File) mRlVoiceContent.getTag()));
                        mMediaPlayer.prepare();
                        int time = mMediaPlayer.getDuration() / 1000 + 1;
                        //最长60秒
                        if (time > AppConstants.VOICE_RECORD_OVERTIME) {
                            time = AppConstants.VOICE_RECORD_OVERTIME;
                        }
                        mTvVoiceTime.setText(time + "\"");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case PICK_BOTTLE_FAILED:
                    ToastUtil.showToast(BottleActivity.this, "渔网漏了,再试一次");
                    mHdl.sendEmptyMessage(PICK_BOTTLE_FINISHED);
                    break;
                case PICK_BOTTLE_FINISHED:
                    //动画结束
                    mAnimatorFishingSet.cancel();
                    mAnimatorFishingSet = null;
                    mIvFishingRippleLeft.clearAnimation();
                    mIvFishingRippleRight.clearAnimation();
                    closeFishingNet();
                    break;
                case CIRCLE_ANIMATION:
                    Animation animCircle = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.mic_circle);
                    mIvBottleCircle2.startAnimation(animCircle);
                    break;
                case THROW_RIPPE_ANIMATION:
                    Animation animThrow = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.throw_ripple_zoom_out);
                    mIvFishingRippleRight.startAnimation(animThrow);
                    break;
                case PICK_RIPPLE_ANIMATION:
                    Animation animPick = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.pick_ripple_zoom_out);
                    mIvFishingRippleRight.startAnimation(animPick);
                    break;
                case PICK_BOTTLE_CLEAR_STATE:
                    mAnimatorFishingSet.cancel();
                    mAnimatorFishingSet = null;
                    mIvFishingRippleLeft.clearAnimation();
                    mIvFishingRippleRight.clearAnimation();
                    clearPickBottleState();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottle);

        //初始化声音管理器
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.setSpeakerphoneOn(true);
        //初始化距离感应
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //耳机插拔监听
        mHeadsetReceiver = new HeadsetReceiver();

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_SOCKET_PUSH);
        registerReceiver(mMessageReceiver, filter);

        StatisticsDao.saveStatistics(this, "bottle", ""); // 切换到漂流瓶页面统计
        mSoundMeterUtil = new SoundMeterUtil(this, new SoundMeterUtil.OnRecordListener() {
            @Override
            public void onRecordOvertime(int remainTime) {
                if (remainTime > 0 && remainTime <= 10) {
                    mTvCount.setText(remainTime + "\"");
                    mTvCount.setVisibility(View.VISIBLE);
                } else if (remainTime <= 0) {
                    mIsVoiceOvertime = true;
                    mTvCount.setVisibility(View.INVISIBLE);
                    mTvSend.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, mTvSend.getX(), mTvSend.getY(), 0));
                }
            }
        });
        mMediaPlayer = new MediaPlayer();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } else {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //停止当前的播放的语音动画
                AnimationDrawable anim = (AnimationDrawable) mIvVoice.getBackground();
                if (anim != null && anim.isRunning()) {
                    anim.stop();
                    anim.selectDrawable(0);
                }
            }
        });

        findViewById(R.id.ivMyBottle_activity_bottle).setOnClickListener(this);
        findViewById(R.id.ivThrow_activity_bottle).setOnClickListener(this);
        findViewById(R.id.ivPick_activity_bottle).setOnClickListener(this);
        findViewById(R.id.btnBack_activity_bottle).setOnClickListener(this);
        findViewById(R.id.btnSetting_activity_bottle).setOnClickListener(this);
        mTvRedPoint = findViewById(R.id.tvRedPoint_activity_bottle);
        mRlCover = findViewById(R.id.rlCoverLayer_activity_bottle);
        mRlBlankView = findViewById(R.id.rlBlank_activity_bottle);
        mRlBottleLayer = findViewById(R.id.rlBottleLayer_activity_bottle);
        mIvLight = findViewById(R.id.ivLight_activity_bottle);
        mRlNoBottleLayer = findViewById(R.id.rlNoBottleLayer_activity_bottle);
        mIvNoBottle = findViewById(R.id.ivNoBottle_activity_bottle);
        mIvBottle = findViewById(R.id.ivBottle_activity_bottle);
        mLlContent = findViewById(R.id.llContent_activity_bottle);
        mEtContent = findViewById(R.id.etContent_activity_bottle);
        mTvCharCount = findViewById(R.id.tvCharCount_activity_bottle);
        mRlMic = findViewById(R.id.rlMic_activity_bottle);
        mTvCount = findViewById(R.id.tvCount_activity_bottle);
        mIvBottleCircle1 = findViewById(R.id.ivCircle1_activity_bottle);
        mIvBottleCircle2 = findViewById(R.id.ivCircle2_activity_bottle);
        mIvMic = findViewById(R.id.ivMic_activity_bottle);
        mTvHint = findViewById(R.id.tvHint_activity_bottle);
        mFlFishingNet = findViewById(R.id.flFishingNet_activity_bottle);
        mIvFishingNet = findViewById(R.id.ivFishingNet_activity_bottle);
        mIvFishingBottle = findViewById(R.id.ivFishingBottle_activity_bottle);
        mRlWaterRipple = findViewById(R.id.rlWaterRipple_activity_bottle);
        mIvDrop1 = findViewById(R.id.ivDropWater1_activity_bottle);
        mIvDrop2 = findViewById(R.id.ivDropWater2_activity_bottle);
        mIvDrop3 = findViewById(R.id.ivDropWater3_activity_bottle);
        mIvRipple1 = findViewById(R.id.ivRipple1_activity_bottle);
        mIvRipple2 = findViewById(R.id.ivRipple2_activity_bottle);
        mIvRipple3 = findViewById(R.id.ivRipple3_activity_bottle);
        mRlResponseLayer = findViewById(R.id.rlResponse_activity_bottle);
        mTvSend = findViewById(R.id.tvSend_activity_bottle);
        mIvHint = findViewById(R.id.ivHint_activity_bottle);
        mIBtnTypeSwitcher = findViewById(R.id.btnTypeSwitcher_activity_bottle);
        mIvHead = findViewById(R.id.ivHead_activity_bottle);
        mTvNickname = findViewById(R.id.tvName_activity_bottle);
        mTvSign = findViewById(R.id.tvSign_activity_bottle);
        mTvComplain = findViewById(R.id.tvComplain_activity_bottle);
        mTvTextContent = findViewById(R.id.tvTextContent_activity_bottle);
        mRlVoiceContent = findViewById(R.id.rlVoice_activity_bottle);
        mTvVoiceTime = findViewById(R.id.tvTime_activity_bottle);
        mIvVoice = findViewById(R.id.ivVoice_activity_bottle);
        mBtnThrowBack = findViewById(R.id.btnThrowBack_activity_bottle);
        mBtnResponse = findViewById(R.id.btnResponse_activity_bottle);
        mIvThrowBottle = findViewById(R.id.ivThrow_activity_bottle);
        mIvPickBottle = findViewById(R.id.ivPick_activity_bottle);
        mIvMyBottle = findViewById(R.id.ivMyBottle_activity_bottle);
        mIvThrowStart = findViewById(R.id.ivThrowStart_activity_bottle);
        mIvThrowEnd = findViewById(R.id.ivThrowEnd_activity_bottle);
        mIvPickStart = findViewById(R.id.ivPickStart_activity_bottle);
        mIvPickEnd = findViewById(R.id.ivPickEnd_activity_bottle);
        mIvPickSprayLeft = findViewById(R.id.ivPickSprayLeft_activity_bottle);
        mIvPickSprayRight = findViewById(R.id.ivPickSprayRight_activity_bottle);
        mIvPickRippleLeft = findViewById(R.id.ivPickRippleLeft_activity_bottle);
        mIvPickRippleRight = findViewById(R.id.ivPickRippleRight_activity_bottle);
        mIvFishingRippleLeft = findViewById(R.id.ivFishingRippleLeft_activity_bottle);
        mIvFishingRippleRight = findViewById(R.id.ivFishingRippleRight_activity_bottle);
        mRlBg = findViewById(R.id.rlBg_activity_bottle);
        mIvFishingNetMark = findViewById(R.id.ivFishingNetMark_activity_bottle);

        mIvHead.setOnClickListener(this);
        mTvComplain.setOnClickListener(this);
        mBtnThrowBack.setOnClickListener(this);
        mBtnResponse.setOnClickListener(this);
        mTvSend.setOnClickListener(this);
        mIBtnTypeSwitcher.setOnClickListener(this);
        mRlVoiceContent.setOnClickListener(this);
        mRlNoBottleLayer.setOnClickListener(this);

        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int count = editable.toString().trim().length();
                mTvCharCount.setText(count + "/1000");
            }
        });

        mTvSend.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0;
            float startY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!AndPermission.hasPermission(BottleActivity.this, Manifest.permission.RECORD_AUDIO)) { //录音权限没打开
                    ToastUtil.showToast(BottleActivity.this, "录音失败,请到手机应用设置内检查是否开启录音权限");
                    return true;
                }
                if (mType == 1) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        mIsRecording = true;
                        mIsVoiceOvertime = false;
                        startX = motionEvent.getX();
                        startY = motionEvent.getY();
                        mTvSend.setBackgroundResource(R.drawable.round_rect_grey2);
                        startMicAnimation();
                        mVoiceStartTime = System.currentTimeMillis();
                        mVoiceName = mVoiceStartTime + ".amr";
                        mSoundMeterUtil.start(mVoiceName);
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (mIsRecording == false) { //语音太长,已通过超时机制发送,此时不再发
                            return false;
                        }
                        mTvSend.setBackgroundResource(R.drawable.btn_send_msg);
                        endMicAnimation();
                        mTvCount.setVisibility(View.INVISIBLE);
                        mSoundMeterUtil.stop();
                        mVoiceEndTime = System.currentTimeMillis();
                        if (mTouchPosition == 0 || mIsVoiceOvertime) {
                            int time = (int) ((mVoiceEndTime - mVoiceStartTime) / 1000); //音频时长
                            if (time < 1) {
                                mHdl.removeMessages(CIRCLE_ANIMATION);
                                ToastUtil.showToast(BottleActivity.this, R.string.voice_too_short);
                                mTvSend.setText(R.string.voice_hint3);
                                return true;
                            } else {
                                File file = new File(getCacheDir().getPath() + "/" + mVoiceName);
                                if (file.exists() && file.length() > 100) { //就算未录音成功,某些手机也会生成一个文件,坚果手机生成的文件大小为6
                                    throwBottle("1", getCacheDir().getPath() + "/" + mVoiceName);
                                } else {
                                    ToastUtil.showToast(BottleActivity.this, "录音失败,请到手机应用设置内检查是否开启录音权限");
                                }
                            }
                        } else if (mTouchPosition == 1) {
                            File file = new File(getCacheDir().getPath() + "/" + mVoiceName);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                        mIsRecording = false;
                        mTvSend.setText(R.string.voice_hint3);
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        if (mIsRecording == false) {
                            return false;
                        }
                        if (startY - motionEvent.getY() > AppUtil.dp2px(BottleActivity.this, 120)) {
                            mTouchPosition = 1;
                            mTvSend.setText(R.string.voice_hint1);
                            mTvHint.setText(R.string.voice_hint1);
                        } else {
                            mTouchPosition = 0;
                            mTvHint.setText(R.string.voice_hint2);
                            mTvSend.setText(R.string.voice_hint5);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnBack_activity_bottle) { //返回
            finish();
        } else if (view.getId() == R.id.btnSetting_activity_bottle) { //设置
            Intent intent = new Intent(this, BottleUserSettingActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.ivMyBottle_activity_bottle) { //我的瓶子
            Intent intent = new Intent(this, MyBottleActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.ivThrow_activity_bottle) { //丢瓶子
            mRlCover.setVisibility(View.VISIBLE);
            mRlBlankView.setOnClickListener(this);
        } else if (view.getId() == R.id.ivPick_activity_bottle) { //捡瓶子
            mRlVoiceContent.setTag(null);
            mIvPickBottle.setClickable(false);
            mIvThrowBottle.setClickable(false);
            mIvMyBottle.setClickable(false);
            mFlFishingNet.setVisibility(View.VISIBLE);
            mIvFishingNet.setVisibility(View.VISIBLE);
            mIvFishingBottle.setVisibility(View.INVISIBLE);
            // 先执行捞瓶子的动画
            startPickBottleAnimation();
        } else if (view.getId() == R.id.rlBlank_activity_bottle) { //点击空白处
            mRlCover.setVisibility(View.GONE);
            //收起软键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else if (view.getId() == R.id.btnTypeSwitcher_activity_bottle) { //切换语音文字
            if (mType == 0) { //从文字到语音
                mIvHint.setVisibility(View.VISIBLE);
                mIBtnTypeSwitcher.setBackgroundResource(R.drawable.btn_keyboard_selector);
                mTvSend.setText(R.string.voice_hint3);
                mType = 1;
                mLlContent.setVisibility(View.GONE);
                mRlMic.setVisibility(View.VISIBLE);
                //收起软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } else if (mType == 1) { //从语音到文字
                mIvHint.setVisibility(View.GONE);
                mIBtnTypeSwitcher.setBackgroundResource(R.drawable.btn_voice_selector);
                mTvSend.setText("扔出去");
                mType = 0;
                mRlMic.setVisibility(View.GONE);
                mLlContent.setVisibility(View.VISIBLE);
                mEtContent.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEtContent, 0);
            }
        } else if (view.getId() == R.id.tvSend_activity_bottle) { //发送
            if (mType == 0) { //发送文字
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                throwBottle("0", mEtContent.getText().toString().trim());
            }
        } else if (view.getId() == R.id.tvComplain_activity_bottle) { //投诉
            Intent complainIntent = new Intent(this, SimpleWebActivity.class);
            complainIntent.putExtra("title", "投诉");
            complainIntent.putExtra("url", AppConstants.COMPLAIN_PAGE);
            complainIntent.putExtra("friendAccount", mFriendAccount);
            complainIntent.putExtra("chatId", mChatItem.chatId);
            complainIntent.putExtra("complainType", "chat");
            startActivity(complainIntent);
        } else if (view.getId() == R.id.btnThrowBack_activity_bottle) { //扔回水里
            mRlResponseLayer.setVisibility(View.GONE);
//            mRlBottleLayer.setVisibility(View.VISIBLE);
            startThrowBottleAnimation();
            try {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mSoundMeterUtil.abandonAudioFocus();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            returnBottle();
        } else if (view.getId() == R.id.btnResponse_activity_bottle) { //回应
            Intent intent = new Intent(BottleActivity.this, ChatActivity.class);
            intent.putExtra("chatId", mChatItem.chatId);
            intent.putExtra("userAccount", mChatItem.userAccount);
            intent.putExtra("position", mChatItem.position);
            intent.putExtra("category", "0");
            startActivity(intent);
            mRlResponseLayer.setVisibility(View.GONE);
        } else if (view.getId() == R.id.rlVoice_activity_bottle) { //点击捡到的语音瓶子
            //停止之前的语音动画
            AnimationDrawable anim = (AnimationDrawable) mIvVoice.getBackground();
            if (anim != null && anim.isRunning()) {
                anim.stop();
                anim.selectDrawable(0);
            }
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mSoundMeterUtil.abandonAudioFocus();
            } else {
                File file = (File) mRlVoiceContent.getTag();
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(this, Uri.fromFile(file));
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    mSoundMeterUtil.requestAudioFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (anim != null) {
                    anim.start();
                }
            }
        } else if (view.getId() == R.id.rlNoBottleLayer_activity_bottle) { //海星层
            mRlNoBottleLayer.setVisibility(View.GONE);
        } else if (view.getId() == R.id.ivHead_activity_bottle) {
            Intent intent = new Intent();
            intent.setClass(this, ImagePreviewActivity.class);
            intent.putExtra("imgUrl", mFriendHeadUrl);
            intent.putExtra("defaultResId", R.drawable.icon_profile_default);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRedPoint();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadsetReceiver, filter);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayer.stop();

        unregisterReceiver(mHeadsetReceiver);
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMessageReceiver != null) {
            unregisterReceiver(mMessageReceiver);
            mMessageReceiver = null;
        }
        mMediaPlayer.release();
        if (mAnimatorFishingSet != null && mAnimatorFishingSet.isRunning()) {
            mAnimatorFishingSet.cancel();
        }

        mAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    /**
     * 捡瓶子
     */
    private void pickBottle() {
        WebServiceIf.IResponseCallback pickBottleCallbackIf = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    PickBottleResponseObject responseObj = gson.fromJson(response, PickBottleResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            mIsEmptyBottle = false;

                            final PickBottleResponseObject.PickBottleResponseBody body = responseObj.body;
                            body.bottle.msgId = UUID.randomUUID().toString(); //生成一个msgId;
                            body.bottle.sendStatus = 0;
                            body.bottle.readStatus = 0;
                            mFriendAccount = body.bottle.userAccount;

                            //获取联系人信息
                            ContactDao contactDao = ContactDao.getInstance(BottleActivity.this);
                            UserInfo userInfo = contactDao.queryUser(body.bottle.userAccount);
                            if (userInfo != null) {
                                showFriendInfo(userInfo, body.bottle.position);
                            } else {
                                getFriendInfo(body.bottle.userAccount, body.bottle.position);
                            }

                            //语音消息需要下语音文件
                            String fileName = "";
                            if (body.bottle.type.equals("1")) { //语音瓶子
                                mIvBottle.setImageResource(R.drawable.voice_bottle);//漂流瓶语音图标
                                int index = body.bottle.content.lastIndexOf("/");
                                if (index < 0) {
                                    fileName = body.bottle.content;
                                } else {
                                    fileName = body.bottle.content.substring(index);
                                }
                                downloadVoice(body.bottle, fileName);
                            } else { //文字瓶子
                                mIvBottle.setImageResource(R.drawable.bottle);//漂流瓶文字图标
                                //保存到消息记录表
                                ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(BottleActivity.this);
                                chatMsgDao.saveChatMsgItem(body.bottle, "bottle");
                                //更新消息列表
                                BottleListDao bottleListDao = BottleListDao.getInstance(BottleActivity.this);
                                mChatItem = new ChatListItemInfo();
                                mChatItem.chatId = body.bottle.chatId;
                                mChatItem.type = body.bottle.type;
                                mChatItem.content = body.bottle.content;
                                mChatItem.updateTime = body.bottle.date;
                                mChatItem.userAccount = body.bottle.userAccount;
                                mChatItem.myAccount = SharedPreferencesInfo.getTagString(BottleActivity.this, SharedPreferencesInfo.ACCOUNT);
                                mChatItem.position = body.bottle.position;
                                mChatItem.draft = "";
                                bottleListDao.saveBottleListItem(mChatItem);
                                //动画结束
                                mHdl.sendEmptyMessage(PICK_BOTTLE_FINISHED);
                            }

                            //点击捡到的瓶子的事件
                            mIvBottle.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mRlResponseLayer.setVisibility(View.VISIBLE);
                                    mIvBottle.setOnClickListener(null);
                                    mRlBottleLayer.setVisibility(View.GONE);
                                    if (body.bottle.type.equals("0")) {
                                        mTvTextContent.setVisibility(View.VISIBLE);
                                        mRlVoiceContent.setVisibility(View.GONE);
                                    } else if (body.bottle.type.equals("1")) {
                                        mRlVoiceContent.setVisibility(View.VISIBLE);
                                        mTvTextContent.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            mIsEmptyBottle = true;
                            errorCode = header.errCode;
                            if ("36109".equals(errorCode)) {
                                mHdl.sendEmptyMessage(PICK_BOTTLE_CLEAR_STATE);
                                ToastUtil.showToast(BottleActivity.this, "今天捞瓶子的机会完了，明天再来吧");
                            } else {
                                // 结束捞瓶子动画
                                mHdl.sendEmptyMessage(PICK_BOTTLE_FINISHED);
                            }
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                //动画结束
                mHdl.sendEmptyMessage(PICK_BOTTLE_CLEAR_STATE);
                ToastUtil.showToast(BottleActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.pickBottle(this, pickBottleCallbackIf);
    }

    private void startPickBottleAnimation() {
        int[] parentLocation = new int[2];
        mRlBg.getLocationInWindow(parentLocation);
        int[] startLocation = new int[2];
        mIvPickStart.getLocationInWindow(startLocation);
        int[] endLocation = new int[2];
        mIvPickEnd.getLocationInWindow(endLocation);

        final float startX = startLocation[0] - parentLocation[0];
        final float startY = startLocation[1] - parentLocation[1];
        final float endX = endLocation[0] - parentLocation[0];
        final float endY = endLocation[1] - parentLocation[1];

        final Path path = new Path();
        path.moveTo(startX, startY);
        path.quadTo(startX + AppUtil.dp2px(this, 100), startY - AppUtil.dp2px(this, 150), endX, endY);

        final PathMeasure pathMeasure = new PathMeasure(path, false);

        final ValueAnimator anim1 = ValueAnimator.ofFloat(0, pathMeasure.getLength());
        anim1.setInterpolator(new LinearInterpolator());
        anim1.setDuration(700);
        anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                pathMeasure.getPosTan(value, mBottlePosition, null);
                mFlFishingNet.setTranslationX(mBottlePosition[0]);
                mFlFishingNet.setTranslationY(mBottlePosition[1]);
                mFishingNetScaleRate += 0.02;
                if (mFishingNetScaleRate > 1) {
                    mFishingNetScaleRate = 1;
                }
                mFlFishingNet.setScaleX(mFishingNetScaleRate);
                mFlFishingNet.setScaleY(mFishingNetScaleRate);
            }
        });
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("rotation", 0, -45);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("translationY", endY, endY + AppUtil.dp2px(this, 60));
        ObjectAnimator anim2 = ObjectAnimator.ofPropertyValuesHolder(mFlFishingNet, holder1, holder2);
        anim2.setInterpolator(new DecelerateInterpolator());
        anim2.setDuration(700);

        final PropertyValuesHolder holder3 = PropertyValuesHolder.ofFloat("translationX",
                endX - AppUtil.dp2px(this, 10), endX + AppUtil.dp2px(this, 10));
        PropertyValuesHolder holder4 = PropertyValuesHolder.ofFloat("translationY",
                endY + AppUtil.dp2px(this, 50), endY + AppUtil.dp2px(this, 60));
        ObjectAnimator anim3 = ObjectAnimator.ofPropertyValuesHolder(mFlFishingNet, holder3, holder4);
        anim3.setInterpolator(new LinearInterpolator());
        anim3.setDuration(300);
        anim3.setRepeatCount(ValueAnimator.INFINITE);
        anim3.setRepeatMode(ValueAnimator.REVERSE);

        mAnimatorFishingSet = new AnimatorSet();
        mAnimatorFishingSet.play(anim1).before(anim2).before(anim3);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIvFishingNetMark.setVisibility(View.VISIBLE);
                startPickRippleAnimation();
            }
        });
        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mFishingNetScaleRate = 0.3f;
                mHdl.sendEmptyMessageDelayed(PICK_BOTTLE, 1000);
            }
        });
        mAnimatorFishingSet.start();
    }

    private void startPickRippleAnimation() {
        Animation animRipple = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.pick_ripple_zoom_out);
        Animation animSpray = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.pick_spray_zoom_out);
        animSpray.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation anim = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.fishing_ripple_zoom_out);
                mIvFishingRippleLeft.startAnimation(anim);
                mHdl.sendEmptyMessageDelayed(PICK_RIPPLE_ANIMATION, 400);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mIvPickRippleLeft.startAnimation(animRipple);
        mIvPickRippleRight.startAnimation(animRipple);
        mIvPickSprayLeft.startAnimation(animSpray);
        mIvPickSprayRight.startAnimation(animSpray);
    }

    private void closeFishingNet() {
        int[] parentLocation = new int[2];
        mRlBg.getLocationInWindow(parentLocation);
        int[] startLocation = new int[2];
        mIvPickStart.getLocationInWindow(startLocation);
        int[] endLocation = new int[2];
        mIvPickEnd.getLocationInWindow(endLocation);

        final float positionY = endLocation[1] - parentLocation[1];

        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("rotation", -45, 0);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("translationY",
                positionY + AppUtil.dp2px(this, 60), positionY - AppUtil.dp2px(this, 40));
        final ObjectAnimator anim1 = ObjectAnimator.ofPropertyValuesHolder(mFlFishingNet,holder1, holder2);
        anim1.setInterpolator(new DecelerateInterpolator());
        anim1.setDuration(500);
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRlWaterRipple.setVisibility(View.VISIBLE);
                mIvFishingNetMark.setVisibility(View.GONE);

                // 水波纹动画
                final ScaleAnimation anim1Ripple = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                        mIvRipple1.getWidth() / 2, mIvRipple1.getHeight() / 2);
                anim1Ripple.setDuration(500);
                final ScaleAnimation anim2Ripple = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                        mIvRipple2.getWidth() / 2, mIvRipple2.getHeight() / 2);
                anim2Ripple.setDuration(500);
                final ScaleAnimation anim3Ripple = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                        mIvRipple3.getWidth() / 2, mIvRipple3.getHeight() / 2);
                anim3Ripple.setDuration(500);

                // 水滴动画
                final TranslateAnimation anim1Drop = new TranslateAnimation(0, 0, 0, AppUtil.dp2px(BottleActivity.this, 70));
                anim1Drop.setDuration(500);
                anim1Drop.setInterpolator(new LinearInterpolator());
                anim1Drop.setStartOffset(100);
                anim1Drop.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mIvRipple1.clearAnimation();
                        anim1Ripple.reset();
                        mIvRipple1.startAnimation(anim1Ripple);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mIvDrop1.startAnimation(anim1Drop);
                final TranslateAnimation anim2Drop = new TranslateAnimation(0, 0, 0, AppUtil.dp2px(BottleActivity.this, 70));
                anim2Drop.setDuration(500);
                anim2Drop.setInterpolator(new LinearInterpolator());
                anim2Drop.setStartOffset(300);
                anim2Drop.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mIvRipple2.clearAnimation();
                        anim2Ripple.reset();
                        mIvRipple2.startAnimation(anim2Ripple);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mIvDrop2.startAnimation(anim2Drop);
                final TranslateAnimation anim3Drop = new TranslateAnimation(0, 0, 0, AppUtil.dp2px(BottleActivity.this, 70));
                anim3Drop.setDuration(500);
                anim3Drop.setInterpolator(new LinearInterpolator());
                anim3Drop.setStartOffset(600);
                anim3Drop.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mIvRipple3.clearAnimation();
                        anim3Ripple.reset();
                        mIvRipple3.startAnimation(anim3Ripple);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mIvDrop3.startAnimation(anim3Drop);

                anim1Ripple.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        anim1Drop.setStartOffset(0);
                        mIvDrop1.startAnimation(anim1Drop);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                anim2Ripple.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        anim2Drop.setStartOffset(0);
                        mIvDrop2.startAnimation(anim2Drop);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                anim2Ripple.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        anim3Drop.setStartOffset(0);
                        mIvDrop2.startAnimation(anim3Drop);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

        ObjectAnimator anim2 = ObjectAnimator.ofFloat(mFlFishingNet, "translationY",
                positionY - AppUtil.dp2px(this, 45), positionY - AppUtil.dp2px(this, 35));
        anim2.setDuration(500);
        anim2.setInterpolator(new LinearInterpolator());
        anim2.setRepeatCount(3);
        anim2.setRepeatMode(ValueAnimator.REVERSE);
        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                stopFishingAnimation();

                if (!mIsEmptyBottle) {
                    mRlBottleLayer.setVisibility(View.VISIBLE);
                    ObjectAnimator anim = ObjectAnimator.ofFloat(mIvBottle, "translationY",
                            -AppUtil.getScreenH(BottleActivity.this) * 0.7f, 0);
                    anim.setDuration(500);
                    anim.setInterpolator(new AccelerateInterpolator());
                    anim.start();
                    // 显示瓶子内容
                    mTvTextContent.setText(mChatItem.content);
                } else {
                    mRlNoBottleLayer.setVisibility(View.VISIBLE);
                    ObjectAnimator anim = ObjectAnimator.ofFloat(mIvNoBottle, "translationY",
                            0, AppUtil.getScreenH(BottleActivity.this) * 0.75f - mIvNoBottle.getHeight());
                    anim.setDuration(500);
                    anim.setInterpolator(new AccelerateInterpolator());
                    anim.start();
                    if ("36110".equals(errorCode)) {
                        ToastUtil.showToast(BottleActivity.this, "没有捞到瓶子，请用力");
                    } else if ("36111".equals(errorCode)) {
                        ToastUtil.showToast(BottleActivity.this, "没有捞到瓶子，请用力");
                    } else {
                        ToastUtil.showToast(BottleActivity.this, getResources().getString(
                                R.string.request_fail_warning) + "(" + errorCode + ")");
                    }
                }

                mIvPickBottle.setClickable(true);
                mIvThrowBottle.setClickable(true);
                mIvMyBottle.setClickable(true);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(anim1).before(anim2);
        set.start();

        mIvFishingBottle.setVisibility(View.VISIBLE);
        if (mIsEmptyBottle) {
            mIvFishingBottle.setImageResource(R.drawable.no_bottle);
        } else {
            mIvFishingBottle.setImageDrawable(mIvBottle.getDrawable());
        }
    }

    private void stopFishingAnimation() {
        mIvFishingNet.setVisibility(View.GONE);
        mIvFishingBottle.setVisibility(View.GONE);
        mRlWaterRipple.setVisibility(View.GONE);
        mIvDrop1.clearAnimation();
        mIvDrop2.clearAnimation();
        mIvDrop3.clearAnimation();
        mIvRipple1.clearAnimation();
        mIvRipple2.clearAnimation();
        mIvRipple3.clearAnimation();
    }

    private void clearPickBottleState() {
        mIvFishingNet.setVisibility(View.GONE);
        mIvFishingBottle.setVisibility(View.GONE);
        mRlWaterRipple.setVisibility(View.GONE);
        mIvFishingNetMark.setVisibility(View.GONE);

        // 将渔网还原
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFlFishingNet, "rotation", -45, 0);
        animator.setDuration(10);
        animator.start();

        mIvPickBottle.setClickable(true);
        mIvThrowBottle.setClickable(true);
        mIvMyBottle.setClickable(true);
    }

    /**
     * 丢瓶子
     *
     * @param type    类型
     * @param content 内容
     */
    private void throwBottle(String type, String content) {
        if (type.equals("0") && content.length() < 5) {
            ToastUtil.showToast(BottleActivity.this, R.string.bottle_text_limit);
            return;
        }
        if (type.equals("0")) {
            mIvBottle.setImageResource(R.drawable.bottle);//丢出去的内容为文字显示文字图标
        } else if (type.equals("1")) {
            mIvBottle.setImageResource(R.drawable.voice_bottle);//为语音显示语音图标
        }
        //动画
        mRlCover.setVisibility(View.GONE);

        startThrowBottleAnimation();

        ThrowBottleRequestBody requestBody = new ThrowBottleRequestBody();
        requestBody.type = type;
        requestBody.content = content;
        ArrayList<File> files = new ArrayList<>();
        if (type.equals("1")) {
            File file = new File(content);
            files.add(file);
        }
        WebServiceIf.IResponseCallback throwBottleCallbackIf = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    ResponseObject responseObj = gson.fromJson(response, ResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            mEtContent.setText("");
                            ToastUtil.showToast(BottleActivity.this, "瓶子已丢出");
                        } else {
                            if (header.errCode.equals("36115")) {
                                ToastUtil.showToast(BottleActivity.this, "今天扔瓶子的机会已经用完啦");
                            } else {
                                ToastUtil.showToast(BottleActivity.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                            }
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                dismissProgressDialog();
                ToastUtil.showToast(BottleActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.throwBottle(this, files, requestBody, throwBottleCallbackIf);
    }

    private void startThrowBottleAnimation() {
        final ImageView ivBottle = new ImageView(this);
        ivBottle.setImageDrawable(mIvBottle.getDrawable());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mRlBg.addView(ivBottle, params);

        int[] parentLocation = new int[2];
        mRlBg.getLocationInWindow(parentLocation);
        int[] startLocation = new int[2];
        mIvThrowStart.getLocationInWindow(startLocation);
        int[] endLocation = new int[2];
        mIvThrowEnd.getLocationInWindow(endLocation);

        final float startX = startLocation[0] - parentLocation[0];
        float startY = startLocation[1] - parentLocation[1];
        float endX = endLocation[0] - parentLocation[0];
        float endY = endLocation[1] - parentLocation[1];

        final Path path = new Path();
        path.moveTo(startX, startY);
        path.quadTo(endX - AppUtil.dp2px(this, 100), endY - AppUtil.dp2px(this, 150), endX, endY);

        final PathMeasure pathMeasure = new PathMeasure(path, false);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, pathMeasure.getLength());
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(800);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                pathMeasure.getPosTan(value, mBottlePosition, null);
                ivBottle.setTranslationX(mBottlePosition[0]);
                ivBottle.setTranslationY(mBottlePosition[1]);
                mBottleRotationRate += 15;
                ivBottle.setRotation(mBottleRotationRate);
                mBottleScaleRate -= 0.01;
                ivBottle.setScaleX(mBottleScaleRate);
                ivBottle.setScaleY(mBottleScaleRate);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mBottleRotationRate = 100; // 动画结束后重置比率
                mBottleScaleRate = 1;
                mRlBottleLayer.setVisibility(View.GONE);
                ivBottle.setVisibility(View.INVISIBLE);
                startThrowRippleAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    private void startThrowRippleAnimation() {
        Animation animSprayLeft = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.pick_spray_zoom_out);
        mIvPickSprayLeft.startAnimation(animSprayLeft);
        Animation animSprayRight = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.pick_spray_zoom_out);
        animSprayRight.setStartOffset(300);
        mIvPickSprayRight.startAnimation(animSprayRight);
        Animation anim = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.throw_ripple_zoom_out);
        mIvFishingRippleLeft.startAnimation(anim);
        mHdl.sendEmptyMessageDelayed(THROW_RIPPE_ANIMATION, 400);
    }

    /**
     * 查询用户信息
     */
    private void getFriendInfo(final String userAccount, final String position) {
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
                                ContactDao contactDao = ContactDao.getInstance(BottleActivity.this);
                                contactDao.saveUser(userInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //显示联系人
                            showFriendInfo(userInfo, position);
                        } else {
                            ToastUtil.showToast(BottleActivity.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(BottleActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.getFriendInfo(this, requestBody, getFriendInfoCallbackIf);
    }

    /**
     * 将瓶子丢回水里
     */
    private void returnBottle() {
        ReturnBottleRequestBody requestBody = new ReturnBottleRequestBody();
        requestBody.chatId = mChatItem.chatId;
        WebServiceIf.IResponseCallback callbackIf = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    ResponseObject responseObj = gson.fromJson(response, ResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            BottleListDao bottleListDao = BottleListDao.getInstance(BottleActivity.this);
                            bottleListDao.delBottleListItem(mChatItem);
                            ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(BottleActivity.this);
                            chatMsgDao.delAllChatMsgFromChatId(mChatItem.chatId);
                        } else {
                            ToastUtil.showToast(BottleActivity.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(BottleActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.returnBottle(this, requestBody, callbackIf);
    }

    /**
     * 下载语音文件
     */
    private void downloadVoice(final ChatMsgInfo chatMsgInfo, final String fileName) {
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
                    int len = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    mRlVoiceContent.setTag(file);
                    Message msg = new Message();
                    msg.what = DOWNLOAD_VOICE_FINISHED;
                    msg.obj = chatMsgInfo;
                    mHdl.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    mHdl.sendEmptyMessage(PICK_BOTTLE_FAILED);
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

    /**
     * 显示用户信息(先查询数据库,再从网络获取)
     */
    private void showFriendInfo(UserInfo userInfo, String position) {
        //显示昵称
        mTvNickname.setText("来自" + position);
        //显示签名
        mTvSign.setText(userInfo.sign);
        //显示性别
        if (userInfo.sex.equals("男")) {
            Drawable drawable = getResources().getDrawable(R.drawable.man);
            drawable.setBounds(0, 0, drawable.getMinimumWidth() / 3 * 2, drawable.getMinimumHeight() / 3 * 2);
            mTvNickname.setCompoundDrawables(drawable, null, null, null);
        } else {
            Drawable drawable = getResources().getDrawable(R.drawable.woman);
            drawable.setBounds(0, 0, drawable.getMinimumWidth() / 3 * 2, drawable.getMinimumHeight() / 3 * 2);
            mTvNickname.setCompoundDrawables(drawable, null, null, null);
        }
        //显示头像
        mIvHead.setImageResource(R.drawable.icon_profile_default);
        if (!TextUtils.isEmpty(userInfo.headUrl)) {
            mFriendHeadUrl = userInfo.headUrl;
            GlideApp.with(this)
                    .load(userInfo.headUrl)
                    .centerCrop()
                    .error(R.drawable.image_bg)
                    .into(mIvHead);
        }
    }

    /**
     * 开始麦克风动画
     */
    private void startMicAnimation() {
        Animation animCircle = AnimationUtils.loadAnimation(BottleActivity.this, R.anim.mic_circle);
        mIvBottleCircle1.setVisibility(View.VISIBLE);
        mIvBottleCircle1.startAnimation(animCircle);
        mIvMic.setVisibility(View.VISIBLE);
        mTvHint.setVisibility(View.VISIBLE);
        mTvHint.setText(R.string.voice_hint2);
        mHdl.sendEmptyMessageDelayed(CIRCLE_ANIMATION, 500);
    }

    /**
     * 结束麦克风动画
     */
    private void endMicAnimation() {
        mIvBottleCircle1.clearAnimation();
        mIvBottleCircle1.setVisibility(View.INVISIBLE);
        mIvBottleCircle2.clearAnimation();
        mIvBottleCircle2.setVisibility(View.INVISIBLE);
        mIvMic.setVisibility(View.INVISIBLE);
        mTvHint.setVisibility(View.INVISIBLE);
    }

    /**
     * 刷新漂流瓶红点
     */
    private void refreshRedPoint() {
        //设置红点未读数量
        ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(BottleActivity.this);
        int count = chatMsgDao.queryAllUnReadMsgCount("bottle");
        if (count < 100) {
            mTvRedPoint.setText(count + "");
        } else {
            mTvRedPoint.setText("···");
        }
        if (count != 0) {
            mTvRedPoint.setVisibility(View.VISIBLE);
        } else {
            mTvRedPoint.setVisibility(View.GONE);
        }
    }

    /**
     * 监听到新信息刷新界面
     */
    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if (!TextUtils.isEmpty(type) && type.equals(AppConstants.PUSH_BOTTLE)) {
                refreshRedPoint();
            }
        }
    }

    /**
     * 距离感应
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(mCurrentAudioMode != MODE_HEADSET) {
            mProximity = sensorEvent.values[0];
            System.out.println("@@@@@@@ proximity = " + mProximity + "         max = " + mSensor.getMaximumRange());
            if (mProximity >= mSensor.getMaximumRange()) {
                mCurrentAudioMode = MODE_SPEAKER;
                mAudioManager.setSpeakerphoneOn(true);
            } else {
                mCurrentAudioMode = MODE_EARPIECE;
                mAudioManager.setSpeakerphoneOn(false);//关闭扬声器
                if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.seekTo(0);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }


    /**
     * 拦截系统按键, 控制音量
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            System.out.println("volume down   " + mAudioManager.getMode());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            } else {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            System.out.println("volume up");
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
    private class HeadsetReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                //插入和拔出耳机会触发此广播
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", 0);
                    if (state == 1){
                        //耳机已插入
                        mCurrentAudioMode = MODE_HEADSET;
                        mAudioManager.setSpeakerphoneOn(false);
                    } else if (state == 0){
                        //耳机已拔出
                        mCurrentAudioMode = MODE_SPEAKER;
                        mAudioManager.setSpeakerphoneOn(true);
                    }
                    break;
            }
        }
    }
}

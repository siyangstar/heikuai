/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：全屏显示广告页面
 *
 *
 * 创建标识：zhaosy 20150318
 */
package com.cqsynet.swifi.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.AdvInfoObject;
import com.cqsynet.swifi.util.AdvDataHelper;
import com.cqsynet.swifi.util.CountDownTimer;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.LoadingDialog;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 迷你视频播放器
 *
 * @author Administrator
 */
public class FullAdvActivity extends HkActivity {

    private ImageView mIvBg;
    private VideoView mVideoView;
    private FrameLayout mFlVideo;
    private TextView mTvTime;
    private ImageView mIvVolume;
    private Button mBtnClose;
    private boolean mIsFirst = true;
    private int mCurrentPosition;
    private AdvInfoObject mAdvObj;
    private CountDownTimer mCDTimer;
    private int mAdvIndex;
    private int mAccessType; //上网类型 0:开始上网  1:续时
    private String mAdType = "0"; //广告类型 0:图片 1:视频
    private Dialog mDialog;
    private AudioManager mAudioManager;
    private int mSystemVolume; //用于保存系统当前媒体音量
    private boolean mIsMute = false; //应用内是否静音
    private String mSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_adv);

        mVideoView = findViewById(R.id.videoView_activity_full_adv);
        mFlVideo = findViewById(R.id.fl_video);
        mBtnClose = findViewById(R.id.btnClose_activity_full_adv);
        mTvTime = findViewById(R.id.tvTime_activity_full_adv);
        mIvVolume = findViewById(R.id.btnVolume_activity_full_adv);
        mIvBg = findViewById(R.id.bg_activity_full_adv);
        mDialog = LoadingDialog.createLoadingDialog(this, "请稍候...");

        mSource = getIntent().getStringExtra("source");
        mAccessType = getIntent().getIntExtra("accessType", 0); //0:开始上网  1:续时
        List<AdvInfoObject> advData = new AdvDataHelper(this, null).getAdvData();
        if (advData != null && advData.size() != 0) {
            for (AdvInfoObject advInfo : advData) {
                if (("ad0009".equals(advInfo.id) && mAccessType == 0) || ("ad0005".equals(advInfo.id) && mAccessType == 1)) {
                    mAdvObj = advInfo;
                    break;
                }
            }
        }

        mCDTimer = new CountDownTimer(Globals.g_advRestTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Globals.g_advRestTime = millisUntilFinished;
                mTvTime.setText(Globals.g_advRestTime / 1000 + 1 + "秒后可关闭,并获得三小时免费上网时间");
                if (Globals.DEBUG) {
                    System.out.println("@@@@@@ ad time = " + mTvTime.getText());
                }
            }

            @Override
            public void onFinish() {
                mTvTime.setText("点击关闭,并获得三小时免费上网时间");
                mBtnClose.setVisibility(View.VISIBLE);
                Globals.g_advRestTime = 0;
            }
        };

        if (mAdvObj != null) {
            mAdvIndex = mAdvObj.getSortIndex(mAdvObj.getCurrentIndex());
            if (Globals.g_advRestTime != -1) {
                mAdvIndex--;
                if (mAdvIndex < 0) {
                    mAdvIndex = mAdvIndex + mAdvObj.plan.split(",").length;
                }
                if(mAccessType == 0) {
                    //点击上网
                    Globals.index_ad0009 = mAdvIndex;
                } else {
                    //续时
                    Globals.index_ad0005 = mAdvIndex;
                }
            }
            showAdv();
        } else {
            Intent data = new Intent();
            data.putExtra("flag", "fullAdv");
            data.putExtra("accessType", mAccessType);
            data.putExtra("hasAdv", false);
            data.setClass(FullAdvActivity.this, WifiActivity.class);
            startActivity(data);
            finish();
        }

        mVideoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        mVideoView.start();
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        if (Globals.g_advRestTime / 1000 > 0) {
                            mCDTimer.setRestTime(Globals.g_advRestTime);
                            mCDTimer.cancel();
                            mCDTimer.start();
                        }
                    }
                });
            }
        });

        mVideoView.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Intent data = new Intent();
                data.putExtra("flag", "fullAdv");
                data.putExtra("accessType", mAccessType);
                data.setClass(FullAdvActivity.this, WifiActivity.class);
                startActivity(data);
                mHdl.sendEmptyMessageDelayed(0, 2000);
                return true;
            }

        });

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("WifiActivity".equals(mSource)) {
                    Intent data = new Intent();
                    data.putExtra("flag", "fullAdv");
                    data.putExtra("accessType", mAccessType);
                    data.setClass(FullAdvActivity.this, WifiActivity.class);
                    startActivity(data);
                } else if ("TimerPromptActivity".equals(mSource)) {
                    Intent data = new Intent();
                    data.putExtra("flag", "fullAdv");
                    data.setClass(FullAdvActivity.this, TimerPromptActivity.class);
                    startActivity(data);
                }
                Globals.g_advRestTime = -1;
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSystemVolume, 0);
                FullAdvActivity.this.finish();
            }
        });

        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mSystemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void gotoAdvDetailActivity() {
        if (mAdvObj.jumpUrl.length > 0 && !TextUtils.isEmpty(mAdvObj.jumpUrl[mAdvIndex])) {
            StatisticsDao.saveStatistics(FullAdvActivity.this, "advClick", mAdvObj.advId[mAdvIndex]); // 广告点击统计
            Intent intent = new Intent();
            intent.putExtra("url", mAdvObj.jumpUrl[mAdvIndex]);
            intent.putExtra("type", "0");
            intent.putExtra("source", "广告");
            intent.putExtra("from", "adv");
            intent.putExtra("flag", "fullAdv");
            intent.putExtra("accessType", mAccessType);
            WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
            webDispatcher.dispatch(intent, FullAdvActivity.this);
            FullAdvActivity.this.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsFirst) {
            if (mAdType.equals("1")) {
                mVideoView.seekTo(mCurrentPosition);
            }
            if (Globals.g_advRestTime / 1000 > 0) {
                mCDTimer.setRestTime(Globals.g_advRestTime);
                mCDTimer.cancel();
                mCDTimer.start();
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            } else {
                mTvTime.setText("点击关闭,并获得三小时免费上网时间");
                mBtnClose.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsFirst = false;
        if (mAdType.equals("1") && mVideoView.isPlaying()) {
            mVideoView.pause();
            mCurrentPosition = mVideoView.getCurrentPosition();
        }
        if (mCDTimer != null) {
            mCDTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCDTimer = null;
        mVideoView.stopPlayback();
    }

    @Override
    public void onBackPressed() {
    }

    private void showAdv() {
        try {
            String img = mAdvObj.adUrl[mAdvIndex];
            mAdType = mAdvObj.type[mAdvIndex];
            if (mAdType.equals("0")) { //图片广告
                mVideoView.setVisibility(View.GONE);
                mFlVideo.setVisibility(View.GONE);
                GlideApp.with(this)
                        .load(img)
                        .centerCrop()
                        .error(R.color.transparent)
                        .into(mIvBg);
                mIvBg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoAdvDetailActivity();
                    }
                });
                if (Globals.g_advRestTime == -1) {
                    Globals.g_advRestTime = AppConstants.AD_IMAGE_DURATION;
                } else if (Globals.g_advRestTime >= 0 && Globals.g_advRestTime < 1000) {
                    Globals.g_advRestTime = 0;
                    mTvTime.setText("点击关闭,并获得三小时免费上网时间");
                    mBtnClose.setVisibility(View.VISIBLE);
                }
                mCDTimer.setRestTime(Globals.g_advRestTime);
                mCDTimer.start();
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                if (Globals.DEBUG) {
                    System.out.println("@@@@ 图片广告 = " + img);
                }
                if (!TextUtils.isEmpty(mAdvObj.advId[mAdvIndex])) {
                    StatisticsDao.saveStatistics(FullAdvActivity.this, "advView", mAdvObj.advId[mAdvIndex]); // 上网续时图片广告显示统计
                }
            } else { //视频广告
                mIvVolume.setVisibility(View.VISIBLE);
                mIvVolume.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mIsMute) {
                            mIsMute = true;
                            mIvVolume.setImageResource(R.drawable.mute_off);
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                        } else {
                            mIsMute = false;
                            mIvVolume.setImageResource(R.drawable.mute_on);
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSystemVolume, 0);
                        }
                    }
                });
                mDialog.show();
                mVideoView.setVisibility(View.VISIBLE);
                mFlVideo.setVisibility(View.VISIBLE);
                mFlVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoAdvDetailActivity();
                    }
                });
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                //视频
                Uri uri = Uri.parse(mAdvObj.adUrl[mAdvIndex]);
                mVideoView.setVideoURI(uri);
                mVideoView.requestFocus();
                if (Globals.g_advRestTime == -1) {
                    Globals.g_advRestTime = AppConstants.AD_VIDEO_DURATION;
                    mVideoView.seekTo(1);
                } else if (Globals.g_advRestTime >= 0 && Globals.g_advRestTime < 1000) {
                    Globals.g_advRestTime = 0;
                    mTvTime.setText("点击关闭,并获得三小时免费上网时间");
                    mBtnClose.setVisibility(View.VISIBLE);
                    mVideoView.seekTo(0);
                } else if (Globals.g_advRestTime >= 1000) {
                    mVideoView.seekTo((int) (AppConstants.AD_VIDEO_DURATION - Globals.g_advRestTime));
                }
                if (Globals.DEBUG) {
                    System.out.println("@@@@@ 视频广告 = " + mAdvObj.adUrl[mAdvIndex]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Intent data = new Intent();
            data.putExtra("flag", "fullAdv");
            data.putExtra("accessType", mAccessType);
            data.setClass(FullAdvActivity.this, WifiActivity.class);
            startActivity(data);
            mHdl.sendEmptyMessageDelayed(0, 2000);
        }
    }

    private MyHandler mHdl = new MyHandler(this);

    static class MyHandler extends Handler {
        WeakReference<FullAdvActivity> mWeakRef;

        public MyHandler(FullAdvActivity fullAdvActivity) {
            mWeakRef = new WeakReference<>(fullAdvActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            FullAdvActivity activity = mWeakRef.get();
            switch (msg.what) {
                case 0:
                    activity.finish();
                    break;
            }
        }
    }
}

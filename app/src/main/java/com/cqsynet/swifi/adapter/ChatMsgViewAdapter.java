/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：聊天界面的适配器
 *
 *
 * 创建标识：zhaosy 20161121
 */
package com.cqsynet.swifi.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.BottleUserSettingActivity;
import com.cqsynet.swifi.activity.ImagePreviewActivity;
import com.cqsynet.swifi.activity.UserCenterActivity;
import com.cqsynet.swifi.activity.social.BottleFriendInfoActivity;
import com.cqsynet.swifi.activity.social.FriendApplyActivity;
import com.cqsynet.swifi.activity.social.PersonInfoActivity;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.model.ChatMsgInfo;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.SoundMeterUtil;
import com.google.gson.Gson;

import java.util.List;

public class ChatMsgViewAdapter extends BaseAdapter {

    private static final String TAG = ChatMsgViewAdapter.class.getSimpleName();
    private List<ChatMsgInfo> mList;
    private Context mContext;
    private String mCategory;
    private String mPosition;
    private LayoutInflater mInflater;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private static final int VIEW_TYPE_COUNT = 7;
    private static final int VIEW_TYPE_VOICE_LEFT = 0;
    private static final int VIEW_TYPE_VOICE_RIGHT = 1;
    private static final int VIEW_TYPE_TEXT_LEFT = 2;
    private static final int VIEW_TYPE_TEXT_RIGHT = 3;
    private static final int VIEW_TYPE_IMAGE_LEFT = 4;
    private static final int VIEW_TYPE_IMAGE_RIGHT = 5;
    private static final int VIEW_TYPE_TIP = 6;
    private String mCurrentPlaying = ""; //当前正在播放的语音文件
    private ImageView mIvVoicePlaying; //当前正在播放语音动画view
    private SoundMeterUtil mSoundMeterUtil;

    public ChatMsgViewAdapter(Context context, List<ChatMsgInfo> list, String category, String position) {
        mContext = context;
        mSoundMeterUtil = new SoundMeterUtil(context, null);
        mList = list;
        mCategory = category;
        mPosition = position;
        mInflater = LayoutInflater.from(context);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        } else {
//            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
//        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mSoundMeterUtil.abandonAudioFocus();
                mCurrentPlaying = "";
                //停止当前的播放的语音动画
                if (mIvVoicePlaying != null) {
                    AnimationDrawable anim = (AnimationDrawable) mIvVoicePlaying.getBackground();
                    if (anim != null && anim.isRunning()) {
                        anim.stop();
                        anim.selectDrawable(0);
                    }
                }
            }
        });
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = VIEW_TYPE_TEXT_LEFT;
        final ChatMsgInfo chatMsgInfo = mList.get(position);
        if ("3".equals(chatMsgInfo.type)) {
            return VIEW_TYPE_TIP;
        }
        if (!chatMsgInfo.userAccount.equals(SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT))) {
            switch (chatMsgInfo.type) {
                case "0":
                    viewType = VIEW_TYPE_TEXT_LEFT;
                    break;
                case "1":
                    viewType = VIEW_TYPE_VOICE_LEFT;
                    break;
                case "2":
                    viewType = VIEW_TYPE_IMAGE_LEFT;
                    break;
            }
        } else {
            switch (chatMsgInfo.type) {
                case "0":
                    viewType = VIEW_TYPE_TEXT_RIGHT;
                    break;
                case "1":
                    viewType = VIEW_TYPE_VOICE_RIGHT;
                    break;
                case "2":
                    viewType = VIEW_TYPE_IMAGE_RIGHT;
                    break;
            }
        }
        return viewType;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ChatMsgInfo chatMsgInfo = mList.get(position);
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            int textWidth = AppUtil.getScreenW((Activity) mContext) * 2 / 3;
            switch (getItemViewType(position)) {
                case VIEW_TYPE_TEXT_LEFT:
                    convertView = mInflater.inflate(R.layout.chat_item_text_left, null);
                    viewHolder.tvDate = convertView.findViewById(R.id.tvDate_chat_item_left);
                    viewHolder.ivHead = convertView.findViewById(R.id.ivHead_chat_item_left);
                    viewHolder.tvContent = convertView.findViewById(R.id.tvContent_chat_item_left);
                    viewHolder.tvContent.setMaxWidth(textWidth);
                    break;
                case VIEW_TYPE_TEXT_RIGHT:
                    convertView = mInflater.inflate(R.layout.chat_item_text_right, null);
                    viewHolder.tvDate = convertView.findViewById(R.id.tvDate_chat_item_right);
                    viewHolder.ivHead = convertView.findViewById(R.id.ivHead_chat_item_right);
                    viewHolder.tvContent = convertView.findViewById(R.id.tvContent_chat_item_right);
                    viewHolder.progress = convertView.findViewById(R.id.progress_chat_item_right);
                    viewHolder.ivFailed = convertView.findViewById(R.id.ivFailed_chat_item_right);
                    viewHolder.tvContent.setMaxWidth(textWidth);
                    break;
                case VIEW_TYPE_VOICE_LEFT:
                    convertView = mInflater.inflate(R.layout.chat_item_voice_left, null);
                    viewHolder.tvDate = convertView.findViewById(R.id.tvDate_chat_item_left);
                    viewHolder.ivHead = convertView.findViewById(R.id.ivHead_chat_item_left);
                    viewHolder.tvTime = convertView.findViewById(R.id.tvTime_chat_item_left);
                    viewHolder.llVoice = convertView.findViewById(R.id.llVoice_chat_item_left);
                    viewHolder.ivVoice = convertView.findViewById(R.id.ivVoice_chat_item_left);
                    break;
                case VIEW_TYPE_VOICE_RIGHT:
                    convertView = mInflater.inflate(R.layout.chat_item_voice_right, null);
                    viewHolder.tvDate = convertView.findViewById(R.id.tvDate_chat_item_right);
                    viewHolder.ivHead = convertView.findViewById(R.id.ivHead_chat_item_right);
                    viewHolder.tvTime = convertView.findViewById(R.id.tvTime_chat_item_right);
                    viewHolder.progress = convertView.findViewById(R.id.progress_chat_item_right);
                    viewHolder.ivFailed = convertView.findViewById(R.id.ivFailed_chat_item_right);
                    viewHolder.llVoice = convertView.findViewById(R.id.llVoice_chat_item_right);
                    viewHolder.ivVoice = convertView.findViewById(R.id.ivVoice_chat_item_right);
                    break;
                case VIEW_TYPE_IMAGE_LEFT:
                    convertView = mInflater.inflate(R.layout.chat_item_image_left, null);
                    viewHolder.tvDate = convertView.findViewById(R.id.tvDate_chat_item_left);
                    viewHolder.ivHead = convertView.findViewById(R.id.ivHead_chat_item_left);
                    viewHolder.ivImage = convertView.findViewById(R.id.ivImage_chat_item_left);
                    break;
                case VIEW_TYPE_IMAGE_RIGHT:
                    convertView = mInflater.inflate(R.layout.chat_item_image_right, null);
                    viewHolder.tvDate = convertView.findViewById(R.id.tvDate_chat_item_right);
                    viewHolder.ivHead = convertView.findViewById(R.id.ivHead_chat_item_right);
                    viewHolder.ivImage = convertView.findViewById(R.id.ivImage_chat_item_right);
                    viewHolder.progress = convertView.findViewById(R.id.progress_chat_item_right);
                    viewHolder.ivFailed = convertView.findViewById(R.id.ivFailed_chat_item_right);
                    break;
                case VIEW_TYPE_TIP:
                    convertView = mInflater.inflate(R.layout.chat_item_tip, null);
                    viewHolder.tvTip = convertView.findViewById(R.id.tv_tip);
                    break;
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (!"3".equals(chatMsgInfo.type)) { // 不是系统提示信息
            //发送时间若:
            //   1.距离上条消息的时间小于5分钟
            //或者
            //   2.大于下一条消息
            //均隐藏时间
            viewHolder.tvDate.setText(DateUtil.getRelativeTimeSpanString(Long.parseLong(chatMsgInfo.date)));
            if (position - 1 > 0 && mList.get(position - 1) != null) {
                long lastTime = Long.parseLong(mList.get(position - 1).date);
                if (Long.parseLong(chatMsgInfo.date) - lastTime < 5 * 60 * 1000) {
                    viewHolder.tvDate.setVisibility(View.GONE);
                } else {
                    viewHolder.tvDate.setVisibility(View.VISIBLE);
                }
            } else if (position + 1 < mList.size()) {
                long nextTime = Long.parseLong(mList.get(position + 1).date);
                if (Long.parseLong(chatMsgInfo.date) > nextTime) {
                    viewHolder.tvDate.setVisibility(View.GONE);
                } else {
                    viewHolder.tvDate.setVisibility(View.VISIBLE);
                }
            } else {
                viewHolder.tvDate.setVisibility(View.VISIBLE);
            }

            //头像
            if (chatMsgInfo.userAccount.equals(SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.ACCOUNT))) {
                if (Globals.g_userInfo == null) {
                    Globals.g_userInfo = new Gson().fromJson(SharedPreferencesInfo.getTagString(mContext, SharedPreferencesInfo.USER_INFO), UserInfo.class);
                }
                if (Globals.g_userInfo != null && !TextUtils.isEmpty(Globals.g_userInfo.headUrl)) {
                    GlideApp.with(mContext)
                            .load(Globals.g_userInfo.headUrl)
                            .centerCrop()
                            .circleCrop()
                            .error(R.drawable.icon_profile_default_round)
                            .into(viewHolder.ivHead);
                }
                viewHolder.ivHead.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ("0".equals(mCategory)) {
                            Intent intent = new Intent(mContext, BottleUserSettingActivity.class);
                            mContext.startActivity(intent);
                        } else if ("1".equals(mCategory)) {
                            Intent intent = new Intent(mContext, UserCenterActivity.class);
                            mContext.startActivity(intent);
                        }
                    }
                });
            } else {
                UserInfo userInfo = ContactDao.getInstance(mContext).queryUser(chatMsgInfo.userAccount);
                if (userInfo != null && !TextUtils.isEmpty(userInfo.headUrl)) {
                    GlideApp.with(mContext)
                            .load(userInfo.headUrl)
                            .centerCrop()
                            .circleCrop()
                            .error(R.drawable.icon_profile_default_round)
                            .into(viewHolder.ivHead);
                }
                viewHolder.ivHead.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ("0".equals(mCategory)) {
                            Intent intent = new Intent();
                            intent.setClass(mContext, BottleFriendInfoActivity.class);
                            intent.putExtra("friendAccount", chatMsgInfo.userAccount);
                            intent.putExtra("position", mPosition);
                            mContext.startActivity(intent);
                        } else if ("1".equals(mCategory)) {
                            Intent friendIntent = new Intent();
                            friendIntent.setClass(mContext, PersonInfoActivity.class);
                            friendIntent.putExtra("friendAccount", chatMsgInfo.userAccount);
                            friendIntent.putExtra("isFriend", mCategory);
                            friendIntent.putExtra("category", mCategory);
                            mContext.startActivity(friendIntent);
                        }
                    }
                });
            }

            //loading圈
            if (viewHolder.progress != null) {
                if (chatMsgInfo.sendStatus == 1) {
                    viewHolder.progress.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.progress.setVisibility(View.GONE);
                }
            }

            //失败感叹号
            if (viewHolder.ivFailed != null) {
                if (chatMsgInfo.sendStatus == 2) {
                    viewHolder.ivFailed.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.ivFailed.setVisibility(View.GONE);
                }
            }
        }

        //内容
        switch (chatMsgInfo.type) {
            case "0": //文字
                viewHolder.tvContent.setText(chatMsgInfo.content);
                break;
            case "1": //语音
                //获取语音长度
                String fileName = "";
                int index = chatMsgInfo.content.lastIndexOf("/");
                if (index < 0) {
                    fileName = chatMsgInfo.content;
                } else {
                    fileName = chatMsgInfo.content.substring(index);
                }
                final String filePath = mContext.getCacheDir().getPath() + "/" + fileName;
                viewHolder.llVoice.setTag(filePath);
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(filePath);
                    mMediaPlayer.prepare();
                    int time = mMediaPlayer.getDuration() / 1000 + 1;
                    //最长60秒
                    if (time > AppConstants.VOICE_RECORD_OVERTIME) {
                        time = AppConstants.VOICE_RECORD_OVERTIME;
                    }
                    viewHolder.tvTime.setText(time + "\"");
                    int width = AppUtil.getScreenW((Activity) mContext) / 2;
                    width = width * time / 60 + AppUtil.dp2px(mContext, 50);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    if (getItemViewType(position) == VIEW_TYPE_VOICE_RIGHT) {
                        params.rightMargin = AppUtil.dp2px(mContext, 10);
                        params.addRule(RelativeLayout.LEFT_OF, R.id.ivHead_chat_item_right);
                    } else if (getItemViewType(position) == VIEW_TYPE_VOICE_LEFT) {
                        params.leftMargin = AppUtil.dp2px(mContext, 10);
                        params.addRule(RelativeLayout.RIGHT_OF, R.id.ivHead_chat_item_left);
                    }
                    viewHolder.llVoice.setLayoutParams(params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                viewHolder.llVoice.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //停止之前的语音动画
                        if (mIvVoicePlaying != null) {
                            AnimationDrawable anim = (AnimationDrawable) mIvVoicePlaying.getBackground();
                            if (anim != null && anim.isRunning()) {
                                anim.stop();
                                anim.selectDrawable(0);
                            }
                        }
                        //若点击的是正在播放的文件,则停止播放;若点击的是新文件,则开始播放新文件
                        String filePath = (String) view.getTag();
                        if (mCurrentPlaying.equals(filePath)) {
                            stopVoice();
                            mCurrentPlaying = "";
                        } else {
                            playVoice(filePath);
                            mCurrentPlaying = filePath;
                            //开始新动画
                            if (getItemViewType(position) == VIEW_TYPE_VOICE_RIGHT) {
                                mIvVoicePlaying = view.findViewById(R.id.ivVoice_chat_item_right);
                            } else if (getItemViewType(position) == VIEW_TYPE_VOICE_LEFT) {
                                mIvVoicePlaying = view.findViewById(R.id.ivVoice_chat_item_left);
                            }
                            AnimationDrawable anim = (AnimationDrawable) mIvVoicePlaying.getBackground();
                            anim.start();
                        }
                    }
                });
                viewHolder.llVoice.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return false;
                    }
                });
                break;
            case "2": //图片
                GlideApp.with(mContext)
                        .load(chatMsgInfo.content)
                        .centerCrop()
                        .error(R.drawable.image_bg)
                        .into(viewHolder.ivImage);
                viewHolder.ivImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setClass(mContext, ImagePreviewActivity.class);
                        intent.putExtra("imgUrl", chatMsgInfo.content);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });
                viewHolder.ivImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return false;
                    }
                });
                break;
            case "3":
                SpannableString spannableString = new SpannableString(chatMsgInfo.content);
                int start = chatMsgInfo.content.length() - 5;
                int end = chatMsgInfo.content.length() - 1;
                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                    }
                }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                viewHolder.tvTip.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, FriendApplyActivity.class);
                        intent.putExtra("friendAccount", chatMsgInfo.userAccount);
                        mContext.startActivity(intent);
                    }
                });
                viewHolder.tvTip.setText(spannableString);
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        public ImageView ivHead; //头像
        public TextView tvDate; //日期
        public TextView tvContent; //内容
        public TextView tvTime; //语音时间
        public ProgressBar progress; //发送进度条
        public ImageView ivFailed; //发送失败状态
        public ImageView ivImage; //图片信息
        public LinearLayout llVoice;//语音信息的布局
        public ImageView ivVoice; //语音播放图标
        public TextView tvTip; // 系统提示
    }


    /**
     * 播放声音
     *
     * @param name
     */
    private void playVoice(String name) {
        mSoundMeterUtil.requestAudioFocus();
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(name);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public void stopVoice() {
        //停止之前的语音动画
        if (mIvVoicePlaying != null) {
            AnimationDrawable anim = (AnimationDrawable) mIvVoicePlaying.getBackground();
            if (anim != null && anim.isRunning()) {
                anim.stop();
                anim.selectDrawable(0);
            }
        }
        //停止播放语音
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCurrentPlaying = "";
        mSoundMeterUtil.abandonAudioFocus();
    }

    /**
     * 重新开始播放声音
     */
    public void restartMediaPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(0);
        }
    }
}

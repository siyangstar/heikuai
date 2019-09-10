package com.cqsynet.swifi.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cqsynet.swifi.AppConstants;

public class SoundMeterUtil {
    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;
    private Context mContext;
    private OnRecordListener mOnRecordListener;
    private int time = 0;
    private AudioManager mAudioManager; //声音管理器
    private boolean mAudioFocus;
    private static String TAG = "SoundMeter";

    public interface OnRecordListener {
        void onRecordOvertime(int remainTime);
    }

    public SoundMeterUtil(Context context, OnRecordListener listener) {
        mContext = context;
        mOnRecordListener = listener;
    }

    public void start(String name) {
        if (mRecorder == null) {
            try {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile(mContext.getCacheDir().getPath() + "/" + name);
                mRecorder.prepare();
                mRecorder.start();
                mEMA = 0.0;
                time = 0;
                mHdl.sendEmptyMessageDelayed(0, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void stop() {
        mHdl.removeCallbacksAndMessages(null);
        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mRecorder = null;
            }
        }
    }

    public void pause() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }

    public void start() {
        if (mRecorder != null) {
            mRecorder.start();
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    Handler mHdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    time++;
                    int remainTime = AppConstants.VOICE_RECORD_OVERTIME - time;
                    if (remainTime > 0) {
                        mHdl.sendEmptyMessageDelayed(0, 1000);
                    }
                    mOnRecordListener.onRecordOvertime(remainTime);
                    break;
            }
        }
    };


    /**
     * 监听声音播放的焦点(是在播放音乐或是语音)
     */
    AudioManager.OnAudioFocusChangeListener mAfChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS");
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT");
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;
                default:
                    Log.i(TAG, "AudioFocusChange focus = " + focusChange);
                    break;

            }

        }

    };

    public void requestAudioFocus() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        Log.v(TAG, "requestAudioFocus mAudioFocus = " + mAudioFocus);
        if (!mAudioFocus) {
            int result = mAudioManager.requestAudioFocus(mAfChangeListener,
                    AudioManager.STREAM_MUSIC, // Use the music stream.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = true;
            } else {
                Log.e(TAG, "AudioManager request Audio Focus result = " + result);
            }
        }
    }

    public void abandonAudioFocus() {
        Log.v(TAG, "abandonAudioFocus mAudioFocus = " + mAudioFocus);
        if (mAudioFocus) {
            mAudioManager.abandonAudioFocus(mAfChangeListener);
            mAudioFocus = false;
        }
    }
}

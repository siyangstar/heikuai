package com.cqsynet.swifi.util;

import android.content.Context;
import android.widget.Button;

import com.cqsynet.swifi.R;

/**
 * 获取验证码计算器
 */
public class CountDown extends CountDownTimer {

        private Button mButton;
        private Context mContext;
        
        /**
         * 构造函数
         * @param button：按钮
         * @param millisInFuture:：倒计时总时间
         * @param countDownInterval：倒计时时间间隔
         */
        public CountDown(Context context, Button button, long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mButton = button;
            mContext = context;
        }

        @Override
        public void onFinish() {
            // 获取验证码按钮可点击
            setButtonEnable();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // 获取验证码按钮不可点击,倒计时开始
            setButtonDisable(millisUntilFinished);
        }

        public void setButtonEnable() {
            mButton.setEnabled(true);
            mButton.setText(R.string.get_verify_code);
            mButton.setTextColor(mContext.getResources().getColor(R.color.green));
        }

        public void setButtonDisable(long millisUntilFinished) {
            mButton.setEnabled(false);
            String countDownString = mContext.getResources().getString(R.string.get_verify_count_down);
            countDownString = String.format(countDownString, (int)millisUntilFinished / 1000);
            mButton.setText(countDownString);
            mButton.setTextColor(mContext.getResources().getColor(R.color.text4));
        }
}

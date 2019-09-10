package com.cqsynet.swifi.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.LotteryListActivity;

/**
 * Author: sayaki
 * Date: 2017/5/19
 */
public class LotteryDialog extends Dialog {

    private ImageView mIvBg;
    private ImageView mTvLogo;
    private TextView mTvDescription;
    private TextView mTvCheckLottery;

    public LotteryDialog(@NonNull final Context context) {
        super(context, R.style.FloatDialog);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_lottery, null);
        setContentView(view);
        setCanceledOnTouchOutside(false);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mIvBg = view.findViewById(R.id.iv_lottery_bg);
        mTvLogo = view.findViewById(R.id.iv_lottery_logo);
        mTvDescription = view.findViewById(R.id.tv_lottery_description);
        mTvCheckLottery = view.findViewById(R.id.tv_check_lottery);
        mTvCheckLottery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context, LotteryListActivity.class);
                context.startActivity(intent);
                dismiss();
            }
        });

        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.CENTER);
            window.setWindowAnimations(R.style.DialogAnimation);
        }
    }

    public void setBackground(int resId) {
        mIvBg.setImageResource(resId);
    }

    public void setBtnStyle(int background, int color) {
        mTvCheckLottery.setBackgroundResource(background);
        mTvCheckLottery.setTextColor(color);
    }

    public void setLogo(String url) {
        if (TextUtils.isEmpty(url)) {
            mTvLogo.setVisibility(View.GONE);
        } else {
            mTvLogo.setVisibility(View.VISIBLE);
            GlideApp.with(getContext())
                    .load(url)
                    .into(mTvLogo);
        }
    }

    public void setDescription(int resId) {
        mTvDescription.setVisibility(View.VISIBLE);
        mTvDescription.setText(resId);
    }

    public void setDescription(CharSequence description) {
        mTvDescription.setVisibility(View.VISIBLE);
        mTvDescription.setText(description);
    }
}

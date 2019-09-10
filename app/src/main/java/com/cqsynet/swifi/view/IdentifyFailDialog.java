package com.cqsynet.swifi.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.WebActivityDispatcher;

/**
 * Author: sayaki
 * Date: 2017/5/16
 */
public class IdentifyFailDialog extends Dialog {

    public IdentifyFailDialog(@NonNull final Context context) {
        super(context, R.style.FloatDialog);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_identify_fail, null);
        setContentView(view);
        setCanceledOnTouchOutside(false);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView tvCheckLottery = view.findViewById(R.id.tv_check_lottery);
        tvCheckLottery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("url", "http://sftpw-img.heikuai.com:8000/zt/web/src/2017/5/cqAR/pages/list.html");
                WebActivityDispatcher dispatcher = new WebActivityDispatcher();
                dispatcher.dispatch(intent, context);
            }
        });

        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.CENTER);
            window.setWindowAnimations(R.style.DialogAnimation);
        }
    }
}

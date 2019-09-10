package com.cqsynet.swifi.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.R;

/**
 * Author: sayaki
 * Date: 2017/11/7
 */
public class CommentDialog extends Dialog {

    public CommentDialog(@NonNull Context context) {
        super(context);
    }

    public static Dialog createDialog(Context context, int iconResId, String msg) {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null);
        LinearLayout layout = v.findViewById(R.id.dialog_view);
        ImageView ivStatus = v.findViewById(R.id.iv_status);
        ivStatus.setImageResource(iconResId);
        TextView tvMsg = v.findViewById(R.id.tv_msg);
        tvMsg.setText(msg);

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);
        loadingDialog.setCancelable(true);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        return loadingDialog;
    }
}

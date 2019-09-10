package com.cqsynet.swifi.view;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cqsynet.swifi.R;

/**
 * Created by USER-PC on 2016/11/10.
 */

public class ChoiceView extends LinearLayout implements Checkable {
    private ImageView mIvIcon;
    private TextView mTvName;
    private RadioButton mRadioButton;

    public ChoiceView(Context context) {
        super(context);
        View.inflate(context, R.layout.choice_list_item, this);
        mIvIcon = findViewById(R.id.ivIcon_choiceView);
        mTvName = findViewById(R.id.tvName_choiceView);
        mRadioButton = findViewById(R.id.rb_choiceView);
    }

    public void setText(String text) {
        mTvName.setText(text);
    }

    @Override
    public void setChecked(boolean checked) {
        mRadioButton.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return mRadioButton.isChecked();
    }

    @Override
    public void toggle() {
        mRadioButton.toggle();
    }
}

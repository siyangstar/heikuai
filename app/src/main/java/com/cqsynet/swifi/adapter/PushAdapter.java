package com.cqsynet.swifi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.MessageInfo;
import com.cqsynet.swifi.util.DateUtil;

import java.util.ArrayList;

public class PushAdapter extends BaseAdapter {

    private ArrayList<MessageInfo> dataList = new ArrayList<MessageInfo>();
    private LayoutInflater mInflater;
    private Context mContext;

    public static final int MESSAGE_UNREAD_ITEM = 0;// 未读
    public static final int MESSAGE_READED_ITEM = 1;// 已读

    public PushAdapter(Context context, ArrayList<MessageInfo> mList) {
        super();
        this.dataList = mList;
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getItemViewType(int position) {
        int type = Integer.parseInt(dataList.get(position).isRead);
        return type;
    }

    @Override
    public int getViewTypeCount() {
        // 因为有两种视图，所以返回2
        return 2;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MessageInfo info = dataList.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.list_show_pushmessage, null);
            viewHolder.mTv_title = convertView.findViewById(R.id.title_push);
            viewHolder.mTv_content = convertView.findViewById(R.id.content_push);
            viewHolder.mMessage_image = convertView.findViewById(R.id.push_icon);
            viewHolder.mMessage_remind = convertView.findViewById(R.id.message_remind);
            viewHolder.mTv_time = convertView.findViewById(R.id.bpush_list_time_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (info.type.equals(AppConstants.PUSH_ADV)) {
            viewHolder.mMessage_image.setBackgroundResource(R.drawable.push_market);
        } else if (info.type.equals(AppConstants.PUSH_SYS_MESSAGE)) {
            viewHolder.mMessage_image.setBackgroundResource(R.drawable.push_sys);
        } else {
            viewHolder.mMessage_image.setBackgroundResource(R.drawable.push_market);
        }
        String create_time;
        long temp = DateUtil.stringToDate(info.createTime);
        if (temp != 0) {
            create_time = DateUtil.getRelativeTimeSpanString(temp);
        } else {
            create_time = "";
        }
        if (getItemViewType(position) == MESSAGE_UNREAD_ITEM) { // 未读
            viewHolder.mMessage_remind.setVisibility(View.VISIBLE);
        } else { // 已读
            viewHolder.mMessage_remind.setVisibility(View.INVISIBLE);
        }
        viewHolder.mTv_title.setText(info.title);
        viewHolder.mTv_content.setText(info.content);
        viewHolder.mTv_time.setText(create_time);
        return convertView;
    }

    static class ViewHolder {
        TextView mTv_title;
        TextView mTv_content;
        ImageView mMessage_image;
        ImageView mMessage_remind;
        TextView mTv_time;
    }
}

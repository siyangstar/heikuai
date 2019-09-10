/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述："评论回复"和"我的评论"
 *
 * 创建标识：zhaosy 20180320
 */
package com.cqsynet.swifi.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.ReplyActivity;
import com.cqsynet.swifi.model.CommentReplyInfo;
import com.cqsynet.swifi.model.CommentReplyRequestBody;
import com.cqsynet.swifi.model.CommentReplyResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class CommentReplyFragment extends Fragment {

    private static final String ARG_TYPE = "type";
    private String mType;
    private PullToRefreshListView mListView;
    private LinearLayout mLlNoCommentRemind;
    private TextView mTvRemind;
    private ArrayList<CommentReplyInfo> mCommentList = new ArrayList<>();
    private CommentReplyAdapter mAdapter;
    private int mTotalCount = 0; // 抽奖结果列表的总条数
    private long mFreshTime = 0; // 刷新时间

    public CommentReplyFragment() {
        // Required empty public constructor
    }


    public static CommentReplyFragment newInstance(String type) {
        CommentReplyFragment fragment = new CommentReplyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_reply, container, false);
        mListView = view.findViewById(R.id.ptrLv_fragment_comment_reply);
        mLlNoCommentRemind = view.findViewById(R.id.ll_noDataRemind_fragment_comment_reply);
        mTvRemind = view.findViewById(R.id.no_data_remind);
        mListView.setPullToRefreshOverScrollEnabled(false);
        mAdapter = new CommentReplyAdapter(getActivity(), mCommentList);
        mListView.setAdapter(mAdapter);
        mFreshTime = System.currentTimeMillis();

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                getComment("", mType);
            }
        });
        mListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (mTotalCount != 0) {
                    if (mAdapter.getCount() < mTotalCount && !mListView.isRefreshing()) {
                        CommentReplyInfo commentReplyInfo = (CommentReplyInfo) mAdapter.getItem(mAdapter.getCount() - 1);
                        getComment(commentReplyInfo.id, mType);
                    } else {
                        ToastUtil.showToast(getActivity(), R.string.no_more_item);
                    }
                }
            }
        });
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel("更新于:" + DateUtil.getRelativeTimeSpanString(mFreshTime));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ReplyActivity.class);
                intent.putExtra("id", mCommentList.get(i - 1).levelOneId);
                intent.putExtra("nickname", mCommentList.get(i - 1).nickname);
                if(mType.equals("commentReply")) {
                    intent.putExtra("type", 2);
                    intent.putExtra("levelTwoId", mCommentList.get(i - 1).id);
                } else if (mType.equals("myComment") && !(mCommentList.get(i - 1).id.equals(mCommentList.get(i - 1).levelOneId))) {
                    intent.putExtra("type", 2);
                    intent.putExtra("levelTwoId", mCommentList.get(i - 1).id);
                } else {
                    intent.putExtra("type", 1);
                }
                startActivity(intent);
            }
        });

        getComment("", mType);

        return view;
    }

    /**
     * 获取评论列表
     * @param startId
     * @param type
     */
    private void getComment(final String startId, String type) {
        CommentReplyRequestBody body = new CommentReplyRequestBody();
        body.start = startId;
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                mListView.onRefreshComplete();
                if (response != null && !TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    CommentReplyResponseObject responseObject = gson.fromJson(response, CommentReplyResponseObject.class);
                    ResponseHeader header = responseObject.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        if ("".equals(startId)) {
                            refreshComment(responseObject.body);
                        } else {
                            loadMoreComment(responseObject.body);
                        }
                    } else {
                        ToastUtil.showToast(getActivity(), getResources().getString(R.string.comment_list_failed) + "(" + header.errCode + ")");
                    }
                    noDataRemind();
                }
            }

            @Override
            public void onErrorResponse() {
                mListView.onRefreshComplete();
                noDataRemind();
                ToastUtil.showToast(getActivity(), R.string.request_fail_warning);
            }
        };

        if(type.equals("commentReply")) {
            WebServiceIf.getCommentReplyList(getActivity(), body, callback);
        } else if (type.equals("myComment")) {
            WebServiceIf.getMyCommentList(getActivity(), body, callback);
        }
    }

    /**
     * 无数据提示
     */
    private void noDataRemind() {
        if (mCommentList.isEmpty()) {
            if (mType.equals("commentReply")) {
                mTvRemind.setText(R.string.no_comment_reply);
            } else if ( mType.equals("myComment")){
                mTvRemind.setText(R.string.no_my_comment);
            }
            mTvRemind.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
    }

    /**
     * 刷新列表
     * @param body
     */
    private void refreshComment(CommentReplyResponseObject.CommentReplyResponseBody body) {
        if (body.commentList == null || body.commentList.size() == 0) {
            mLlNoCommentRemind.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mLlNoCommentRemind.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            mFreshTime = System.currentTimeMillis();
            mListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                    "更新于：" + DateUtil.getRelativeTimeSpanString(mFreshTime));
            mCommentList.clear();
            mCommentList.addAll(body.commentList);
            mAdapter.notifyDataSetChanged();
            mTotalCount = body.commentCount;
        }
    }

    /**
     * 加载更多
     * @param body
     */
    private void loadMoreComment(CommentReplyResponseObject.CommentReplyResponseBody body) {
        if (body.commentList == null || body.commentList.size() == 0) {
            ToastUtil.showToast(getActivity(), R.string.no_more_item);
        } else {
            mCommentList.addAll(body.commentList);
            mAdapter.notifyDataSetChanged();
        }
    }


    class CommentReplyAdapter extends BaseAdapter {

        private Context mContext;
        private List<CommentReplyInfo> mCommentReplyList;


        public CommentReplyAdapter(Context context, List<CommentReplyInfo> commentReplyList) {
            this.mContext = context;
            this.mCommentReplyList = commentReplyList;
        }

        @Override
        public int getCount() {
            return mCommentReplyList != null ? mCommentReplyList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mCommentReplyList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            final CommentReplyInfo commentReply = mCommentReplyList.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_comment_reply, parent, false);
                viewHolder.ivAvatar = convertView.findViewById(R.id.iv_avatar_item_comment_reply);
                viewHolder.tvUserLevel = convertView.findViewById(R.id.tv_userLevel_item_comment_reply);
                viewHolder.tvNickname = convertView.findViewById(R.id.tv_nickname_item_comment_reply);
                viewHolder.tvQuote = convertView.findViewById(R.id.tv_quote_item_comment_reply);
                viewHolder.tvContent = convertView.findViewById(R.id.tv_content_item_comment_reply);
                viewHolder.tvDate = convertView.findViewById(R.id.tv_date_item_comment_reply);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            GlideApp.with(mContext)
                    .load(commentReply.headUrl)
                    .circleCrop()
                    .error(R.drawable.icon_profile_default_round)
                    .into(viewHolder.ivAvatar);
            String nickname;
            if (commentReply.nickname.length() > 16) {
                nickname = commentReply.nickname.substring(0, 16) + "...";
            } else {
                nickname = commentReply.nickname;
            }
            viewHolder.tvNickname.setText(nickname);
            if (TextUtils.isEmpty(commentReply.userLevel)) {
                viewHolder.tvUserLevel.setVisibility(View.GONE);
            } else {
                viewHolder.tvUserLevel.setVisibility(View.VISIBLE);
                viewHolder.tvUserLevel.setText(commentReply.userLevel);
            }

            viewHolder.tvContent.setText(commentReply.content);
            viewHolder.tvDate.setText(DateUtil.getRelativeTimeSpanString(Long.valueOf(commentReply.date)));
            viewHolder.tvQuote.setText("[原文] " + commentReply.quoteContent);
            viewHolder.tvQuote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent newsIntent = new Intent();
                    newsIntent.putExtra("url", commentReply.quoteUrl);
                    newsIntent.putExtra("type", "0");
                    newsIntent.putExtra("from", "commentReply");
                    WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                    webDispatcher.dispatch(newsIntent, getActivity());
                }
            });

            return convertView;
        }
    }

    private class ViewHolder {
        private ImageView ivAvatar;
        private TextView tvNickname;
        private TextView tvUserLevel;
        private TextView tvContent;
        private TextView tvQuote;
        private TextView tvDate;
    }
}

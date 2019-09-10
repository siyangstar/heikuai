/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：二级评论页面
 *
 *
 * 创建标识：zhaosy 20180326
 */
package com.cqsynet.swifi.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.ReplyAdapter;
import com.cqsynet.swifi.model.CommentInfo;
import com.cqsynet.swifi.model.CommentRequestBody;
import com.cqsynet.swifi.model.CommentSubmitResponseObject;
import com.cqsynet.swifi.model.LevelTwoCommentResponseObject;
import com.cqsynet.swifi.model.ReplyListRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.InputManagerUtil;
import com.cqsynet.swifi.util.SoftKeyboardStateHelper;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.CommentDialog;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ReplyActivity extends HkActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        ReplyAdapter.OnItemChildClickListener {

    public static final int DIALOG_SHOW_TIME = 1000;
    private static final int MSG_DISMISS_DIALOG = 0;

    private View mRootLayout;
    private ImageView mIvBack;
    private TextView mTvTitle;
    private PullToRefreshListView mListView;
    private LinearLayout mLlComment;
    private TextView mTvCommentHint;
    private TextView mTvCommentDisable;
    private ImageView mIvWrite;
    private EditText mEtComment;
    private TextView mTvSend;
    private View mMarkView;
    private Dialog mDialog;
    private ReplyAdapter mAdapter;
    private List<CommentInfo> mCommentList = new ArrayList<>(); //评论列表
    private String mNewsId; //评论所在的文章pkId
    private CommentInfo mComment; //一级评论
    private String mCommentStatus; //0可以评论,1文章被冻结,2用户被冻结
    private String mCommentMessage; //冻结信息
    private CommentInfo mLevelTwoComment; //用户在此界面点击的二级评论
    private long mFreshTime = 0;
    private int mType = 1; // 1: 二级评论；2: 二级评论的回复
    private boolean mHasMore = true;   // 是否有下一页
    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {

        private WeakReference<ReplyActivity> weakReference;

        public MyHandler(ReplyActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ReplyActivity activity = weakReference.get();
            if (msg.what == MSG_DISMISS_DIALOG) {
                activity.dismissProgressDialog();
                if (activity.mDialog != null && activity.mDialog.isShowing()) {
                    activity.mDialog.dismiss();
                }
            }
        }
    }

    public static void launch(Context context, String id, CommentInfo comment, String status, String message) {
        Intent intent = new Intent();
        intent.setClass(context, ReplyActivity.class);
        intent.putExtra("newsId", id);
        intent.putExtra("comment", comment);
        intent.putExtra("status", status);
        intent.putExtra("message", message);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mComment = getIntent().getParcelableExtra("comment"); //一级评论
        mCommentStatus = getIntent().getStringExtra("status");
        mCommentMessage = getIntent().getStringExtra("message");
        mType = getIntent().getIntExtra("type", 1);

        mRootLayout = findViewById(R.id.root_layout);
        mIvBack = findViewById(R.id.iv_back);
        mTvTitle = findViewById(R.id.tv_title);
        mListView = findViewById(R.id.lv_comment);
        mLlComment = findViewById(R.id.comment_layout);
        mIvWrite = findViewById(R.id.iv_write);
        mEtComment = findViewById(R.id.et_comment);
        mTvCommentHint = findViewById(R.id.tv_comment_hint);
        mTvSend = findViewById(R.id.tv_send);
        mTvCommentDisable = findViewById(R.id.tv_comment_disable);
        mMarkView = findViewById(R.id.mark_view);

        mFreshTime = System.currentTimeMillis();
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel("更新于:" + DateUtil.getRelativeTimeSpanString(mFreshTime));

        String commentId;
        if (mComment != null) {
            commentId = mComment.id;
            mTvCommentHint.setText("回复" + mComment.nickname + "：");
        } else {
            commentId = getIntent().getStringExtra("id"); //一级评论id, 从发现界面的评论回复里面进入才有此参数
            mLevelTwoComment = new CommentInfo();
            mLevelTwoComment.id = getIntent().getStringExtra("levelTwoId");
            mLevelTwoComment.nickname = getIntent().getStringExtra("nickname");
            mTvCommentHint.setText("回复" + mLevelTwoComment.nickname + "：");
        }

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mAdapter = new ReplyAdapter(this, mCommentList, this);
        mListView.setAdapter(mAdapter);

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if(mComment != null && !TextUtils.isEmpty(mComment.id)) {
                    getReplyList(mComment.id, "");
                }
            }
        });

        mListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (mHasMore && mComment != null && !TextUtils.isEmpty(mComment.id)) {
                    CommentInfo comment = (CommentInfo) mAdapter.getItem(mAdapter.getCount() - 1);
                    getReplyList(mComment.id, comment.id);
                }
            }
        });

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable)) {
                    mIvWrite.setVisibility(View.VISIBLE);
                    mTvCommentHint.setVisibility(View.VISIBLE);
                    mTvSend.setBackgroundResource(R.drawable.btn_send_white_disable);
                } else {
                    mIvWrite.setVisibility(View.GONE);
                    mTvCommentHint.setVisibility(View.GONE);
                    mTvSend.setBackgroundResource(R.drawable.btn_send_green);
                }
            }
        });

        mTvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });

        mMarkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputManagerUtil.toggleKeyboard(ReplyActivity.this);
            }
        });

        SoftKeyboardStateHelper helper = new SoftKeyboardStateHelper(mRootLayout);
        helper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                mMarkView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSoftKeyboardClosed() {
                mMarkView.setVisibility(View.GONE);
            }
        });

        if(!TextUtils.isEmpty(mCommentStatus)) {
            if ("0".equals(mCommentStatus)) {
                mLlComment.setVisibility(View.VISIBLE);
                mEtComment.setEnabled(true);
                mIvWrite.setVisibility(View.VISIBLE);
                mTvCommentHint.setVisibility(View.VISIBLE);
                mTvCommentDisable.setVisibility(View.GONE);
            } else if ("2".equals(mCommentStatus)) {
                mLlComment.setVisibility(View.VISIBLE);
                mEtComment.setEnabled(false);
                mIvWrite.setVisibility(View.GONE);
                mTvCommentHint.setVisibility(View.GONE);
                mTvCommentDisable.setVisibility(View.VISIBLE);
                mTvCommentDisable.setText(mCommentMessage);
                mAdapter.setDisable();
            } else {
                mLlComment.setVisibility(View.GONE);
                mAdapter.setDisable();
            }
        } else {
            mLlComment.setVisibility(View.VISIBLE);
            mEtComment.setEnabled(true);
            mIvWrite.setVisibility(View.VISIBLE);
            mTvCommentHint.setVisibility(View.VISIBLE);
            mTvCommentDisable.setVisibility(View.GONE);
        }

        getReplyList(commentId, "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MSG_DISMISS_DIALOG);
    }

    private void getReplyList(String id, final String start) {
        ReplyListRequestBody body = new ReplyListRequestBody();
        body.id = id;
        body.start = start;

        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                mListView.onRefreshComplete();
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    LevelTwoCommentResponseObject object = gson.fromJson(response, LevelTwoCommentResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        if ("".equals(start)) {
                            refreshComment(object.body);
                        } else {
                            loadMoreComment(object.body);
                        }
                    } else if (!TextUtils.isEmpty(header.errCode)) {
                        ToastUtil.showToast(ReplyActivity.this, getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                dismissProgressDialog();
            }
        };
        WebServiceIf.getReplyList(this, body, callback);
    }

    private void refreshComment(LevelTwoCommentResponseObject.LevelTwoCommentResponseBody body) {
        mFreshTime = System.currentTimeMillis();
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel("更新于：" + DateUtil.getRelativeTimeSpanString(mFreshTime));
        mTvTitle.setText(body.levelOneComment.replyCount + "条回复");
        mNewsId = body.newsId;
        mComment = body.levelOneComment;
        mCommentList.clear();
        mCommentList.add(body.levelOneComment);
        if (body.levelTwoCommentList != null && body.levelTwoCommentList.size() > 0) {
            mCommentList.addAll(body.levelTwoCommentList);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void loadMoreComment(LevelTwoCommentResponseObject.LevelTwoCommentResponseBody body) {
        if (body.levelTwoCommentList != null && body.levelTwoCommentList.size() > 0) {
            mCommentList.addAll(body.levelTwoCommentList);
            mHasMore = true;
        } else {
            mHasMore = false;
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 发送评论
     */
    private void submitComment() {
        if (TextUtils.isEmpty(mEtComment.getText().toString().trim())) {
            return;
        }
        final CommentRequestBody requestBody = new CommentRequestBody();
        if (mType == 1) {
            requestBody.type = mType + "";
            requestBody.newsId = mNewsId;
            requestBody.levelOneId = mComment.id;
            requestBody.levelTwoId = "";
            requestBody.content = mEtComment.getText().toString();
        } else if (mType == 2) {
            requestBody.type = mType + "";
            requestBody.newsId = mNewsId;
            requestBody.levelOneId = mComment.id;
            requestBody.levelTwoId = mLevelTwoComment.id;
            requestBody.content = "回复 " + mLevelTwoComment.nickname + "：" + mEtComment.getText().toString();
        } else {
            return;
        }

        showProgressDialog(R.string.comment_submitting);
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                dismissProgressDialog();
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    CommentSubmitResponseObject object = gson.fromJson(response, CommentSubmitResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_success, getString(R.string.comment_success));
                        mDialog.show();
                        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
                        if (!TextUtils.isEmpty(object.body.id)) {
                            CommentInfo commentInfo = new CommentInfo();
                            commentInfo.id = object.body.id;
                            commentInfo.userAccount = Globals.g_userInfo.userAccount;
                            commentInfo.nickname = Globals.g_userInfo.nickname;
                            commentInfo.headUrl = Globals.g_userInfo.headUrl;
                            commentInfo.content = requestBody.content;
                            commentInfo.likeCount = "0";
                            commentInfo.like = "0";
                            commentInfo.date = String.valueOf(System.currentTimeMillis());
                            commentInfo.replyCount = "0";
                            mCommentList.add(commentInfo);
                            mAdapter.notifyDataSetChanged();

                            if (!TextUtils.isEmpty(mComment.replyCount)) {
                                mTvTitle.setText(Integer.parseInt(mComment.replyCount) + 1 + "条回复");
                            } else {
                                mTvTitle.setText("1条回复");
                            }
                        }
                        mEtComment.setText("");
                    } else {
                        mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_failure, getString(R.string.comment_fail) + "(" + header.errCode + ")");
                        mDialog.show();
                        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                dismissProgressDialog();
                mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_failure, getString(R.string.comment_fail));
                mDialog.show();
                mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
            }
        };
        WebServiceIf.submitComment(this, requestBody, callback);
    }

    @Override
    public void onLikeClick(int position, boolean isSuccess) {
        if (isSuccess) {
            mCommentList.get(position).like = "1";
            int likeCount = Integer.parseInt(mCommentList.get(position).likeCount);
            mCommentList.get(position).likeCount = String.valueOf(likeCount + 1);
            for (int i = 0; i < mCommentList.size(); i++) {
                if (i != position && mCommentList.get(position).id.equals(mCommentList.get(i).id)) {
                    mCommentList.get(i).like = "1";
                    int count = Integer.parseInt(mCommentList.get(i).likeCount);
                    mCommentList.get(i).likeCount = String.valueOf(count + 1);
                    break;
                }
            }
        } else {
            getReplyList(mComment.id, "");
            mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_failure, getString(R.string.comment_fail));
            mDialog.show();
            mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
        }
    }

    @Override
    public void onLikeRepeat() {
        mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_failure, getString(R.string.like_repeat));
        mDialog.show();
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!TextUtils.isEmpty(mCommentStatus) && !"0".equals(mCommentStatus)) {
            if (!TextUtils.isEmpty(mCommentMessage)) {
                ToastUtil.showToast(this, mCommentMessage);
            }
            return;
        }
        if (position > 1) {
            mType = 2;
        } else if (position == 1) {
            mType = 1;
        }
        mLevelTwoComment = mCommentList.get(position - 1);
        mTvCommentHint.setText("回复" + mLevelTwoComment.nickname + "：");
        mEtComment.setFocusable(true);
        mEtComment.setFocusableInTouchMode(true);
        mEtComment.requestFocus();
        InputManagerUtil.showKeyboardDelay(mEtComment, 100);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mLevelTwoComment = mCommentList.get(position - 1);
        showActionDialog(mLevelTwoComment);
        return true;
    }

    private void showActionDialog(final CommentInfo comment) {
        final String[] itemArray = getResources().getStringArray(R.array.comment_action_2);
        final CustomDialog dialog = new CustomDialog(this, R.style.round_corner_dialog, R.layout.listview_chat_popup);
        ListView listview = dialog.getCustomView().findViewById(R.id.lv_listview_chat_popup);
        listview.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return itemArray.length;
            }

            @Override
            public Object getItem(int i) {
                return itemArray[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                view = View.inflate(ReplyActivity.this, R.layout.chat_popup, null);
                TextView tvTitle = view.findViewById(R.id.tvTitle_chat_popup);
                tvTitle.setText(itemArray[i]);
                return view;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (itemArray[i].equals("复制")) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("text", comment.content);
                    clipboardManager.setPrimaryClip(clipData);
                    ToastUtil.showToast(ReplyActivity.this, "评论内容复制成功");
                } else if (itemArray[i].equals("举报")) {
                    Intent complainIntent = new Intent(ReplyActivity.this, SimpleWebActivity.class);
                    complainIntent.putExtra("title", "投诉");
                    complainIntent.putExtra("url", AppConstants.COMPLAIN_PAGE);
                    complainIntent.putExtra("commentId", comment.id);
                    complainIntent.putExtra("complainType", "comment");
                    startActivity(complainIntent);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

package com.cqsynet.swifi.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.CollectAdapter;
import com.cqsynet.swifi.db.CollectCacheDao;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.CollectInfo;
import com.cqsynet.swifi.model.CollectRemoveInfo;
import com.cqsynet.swifi.model.CollectRemoveRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.util.WebActivityDispatcher;
import com.cqsynet.swifi.view.DeleteDialog;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Author: sayaki
 * Date: 2017/4/20
 */
public class CollectActivity extends HkActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ImageView mIvBack;
    private ImageView mIvSearch;
    private TextView mTvEdit;
    private ListView mLvCollect;
    private FrameLayout mFlDelete;
    private TextView mTvDelete;
    private DeleteDialog mDeleteDialog;

    private CollectAdapter mAdapter;
    private ArrayList<CollectInfo> mCollects = new ArrayList<>();
    // 所有可移除的收藏
    private ArrayList<CollectRemoveInfo> mAllCollectRemoveInfos = new ArrayList<>();
    // 选中的需要移除的收藏
    private ArrayList<CollectRemoveInfo> mCollectRemoveInfos = new ArrayList<>();

    public static void launch(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, CollectActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mIvSearch = findViewById(R.id.iv_search);
        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CollectActivity.this, CollectSearchActivity.class);
                startActivity(intent);
            }
        });
        mTvEdit = findViewById(R.id.iv_edit);
        mTvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTvEdit.getText().toString().equals("编辑")) {
                    mTvEdit.setText("取消");
                    mTvDelete.setText("删除(0)");
                    mAdapter.setMultiMode(true);
                    mFlDelete.setVisibility(View.VISIBLE);
                } else {
                    mTvEdit.setText("编辑");
                    mAdapter.setMultiMode(false);
                    mAdapter.clearSelectedCollects();
                    mFlDelete.setVisibility(View.GONE);
                    mCollectRemoveInfos.clear();
                }
            }
        });
        mFlDelete = findViewById(R.id.fl_delete);
        mTvDelete = findViewById(R.id.tv_delete);
        mTvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCollectRemoveInfos.size() > 0) {
                    showDeleteDialog();
                }
            }
        });

        mAdapter = new CollectAdapter(this, mCollects);
        mLvCollect = findViewById(R.id.lv_collect);
        mLvCollect.setAdapter(mAdapter);
        mLvCollect.setOnItemClickListener(this);
        mLvCollect.setOnItemLongClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCollects();
    }

    private void loadCollects() {
        mCollects.clear();
        mAllCollectRemoveInfos.clear();
        mCollects.addAll(CollectCacheDao.getCollect(this));
        for (CollectInfo collect : mCollects) {
            CollectRemoveInfo info = new CollectRemoveInfo();
            info.type = collect.type;
            info.id = collect.id;
            info.url = collect.url;
            info.title = collect.title;
            mAllCollectRemoveInfos.add(info);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CollectInfo collect = mCollects.get(position);
        if (mTvEdit.getText().equals("编辑")) {
            gotoCollectPage(collect);
        } else {
            mAdapter.setSelectedCollect(collect);
            setCollectRemoveList(mAllCollectRemoveInfos.get(position));
        }
    }

    private void setCollectRemoveList(CollectRemoveInfo collect) {
        if (mCollectRemoveInfos.contains(collect)) {
            mCollectRemoveInfos.remove(collect);
        } else {
            mCollectRemoveInfos.add(collect);
        }
        mTvDelete.setText("删除(" + mCollectRemoveInfos.size() + ")");
    }

    public void gotoCollectPage(CollectInfo collect) {
        int type = Integer.valueOf(collect.type);
        switch (type) {
            case CollectAdapter.TYPE_COLLECT_NEWS:
                Intent newsIntent = new Intent();
                newsIntent.putExtra("url", collect.url);
                newsIntent.putExtra("image", collect.image);
                newsIntent.putExtra("type", collect.type);
                newsIntent.putExtra("from", "collect");
                newsIntent.putExtra("source", "资讯");
                WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                webDispatcher.dispatch(newsIntent, this);
                break;
            case CollectAdapter.TYPE_COLLECT_GALLERY:
                Intent galleryIntent = new Intent();
                galleryIntent.putExtra("id", collect.id);
                galleryIntent.putExtra("title", collect.title);
                galleryIntent.putExtra("from", "collect");
                galleryIntent.setClass(this, GalleryActivity.class);
                startActivity(galleryIntent);
                break;
            case CollectAdapter.TYPE_COLLECT_TOPIC:
                Intent topicIntent = new Intent();
                topicIntent.putExtra("id", collect.id);
                topicIntent.putExtra("from", "collect");
                topicIntent.setClass(this, TopicActivity.class);
                startActivity(topicIntent);
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        setCollectRemoveList(mAllCollectRemoveInfos.get(position));
        showDeleteDialog();

        return true;
    }

    private void showDeleteDialog() {
        mDeleteDialog = new DeleteDialog(CollectActivity.this, R.style.round_corner_dialog,
                "确定删除" + mCollectRemoveInfos.size() + "条收藏吗？", new DeleteDialog.MyDialogListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_confirm_collect:
                        mDeleteDialog.dismiss();
                        removeCollectList();
                        break;
                    case R.id.tv_cancel_collect:
                        mDeleteDialog.dismiss();
                        mCollectRemoveInfos.clear();
                        break;
                }
                mTvEdit.setText("编辑");
                mAdapter.setMultiMode(false);
                mAdapter.clearSelectedCollects();
                mFlDelete.setVisibility(View.GONE);
            }
        });
        mDeleteDialog.show();
    }

    private void removeCollectList() {
        CollectRemoveRequestBody body = new CollectRemoveRequestBody();
        body.favorList = mCollectRemoveInfos;
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    BaseResponseObject responseObj = gson.fromJson(response, BaseResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            ToastUtil.showToast(CollectActivity.this, R.string.remove_collect_success);
                            CollectCacheDao.deleteCollects(CollectActivity.this, mCollectRemoveInfos);
                            loadCollects();
                        }
                    }
                }
                mCollectRemoveInfos.clear();
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(CollectActivity.this, R.string.request_fail_warning);
                mCollectRemoveInfos.clear();
            }
        };
        WebServiceIf.removeCollect(this, body, callback);
    }
}

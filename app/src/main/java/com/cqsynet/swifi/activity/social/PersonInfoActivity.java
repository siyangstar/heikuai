/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：人物信息界面
 *
 *
 * 创建标识：sayaki 20180210
 */
package com.cqsynet.swifi.activity.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.ChatActivity;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.activity.ImagePreviewActivity;
import com.cqsynet.swifi.activity.SimpleWebActivity;
import com.cqsynet.swifi.db.ChatListDao;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendApplyDao;
import com.cqsynet.swifi.db.FriendsDao;
import com.cqsynet.swifi.db.RegionDao;
import com.cqsynet.swifi.model.AddOrRemoveFriendRequestBody;
import com.cqsynet.swifi.model.FindPersonInfo;
import com.cqsynet.swifi.model.GetFriendInfoRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.UserInfoResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.DeleteDialog;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.List;

/**
 * Author: sayaki
 * Date: 2018/1/3
 */
public class PersonInfoActivity extends HkActivity implements View.OnClickListener {

    private static final int MODIFY_REMARK_REQUEST = 0;

    private String mFriendAccount;
    private String mIsFriend;
    private String mCategory; //0表示漂流瓶,1表示社交
    private FindPersonInfo mPerson;

    private ImageView mIvAvatar;
    private TextView mTvName;
    private ImageView mIvSex;
    private TextView mTvAge;
    private TextView mTvLocation;
    private TextView mTvNickname;
    private TextView mTvSign;
    private TextView mTvRemark;
    private LinearLayout mLlRemark;
    private TextView mTvAddFriend;
    private TextView mTvComplaint;
    private TextView mTvSendMsg;
    private TextView mTvDelete;
    private TextView mTvComplaintTiny;
    private DeleteDialog mDeleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        mTvAddFriend = findViewById(R.id.tv_add_friend);
        mTvAddFriend.setOnClickListener(this);
        mTvComplaint = findViewById(R.id.tv_complaint);
        mTvComplaint.setOnClickListener(this);
        mTvSendMsg = findViewById(R.id.tv_send_msg);
        mTvSendMsg.setOnClickListener(this);
        mTvDelete = findViewById(R.id.tv_delete);
        mTvDelete.setOnClickListener(this);
        mTvComplaintTiny = findViewById(R.id.tv_complaint_tiny);
        mTvComplaintTiny.setOnClickListener(this);
        mIvAvatar = findViewById(R.id.iv_avatar);
        mIvAvatar.setOnClickListener(this);
        mTvName = findViewById(R.id.tv_name);
        mIvSex = findViewById(R.id.iv_sex);
        mTvAge = findViewById(R.id.tv_age);
        mTvLocation = findViewById(R.id.tv_location);
        mTvNickname = findViewById(R.id.tv_nickname);
        mTvSign = findViewById(R.id.tv_sign);
        mTvRemark = findViewById(R.id.tv_remark);
        mLlRemark = findViewById(R.id.ll_remark);
        mTvRemark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonInfoActivity.this, RemarkActivity.class);
                intent.putExtra("friendAccount", mFriendAccount);
                intent.putExtra("nickname", mPerson.nickname);
                intent.putExtra("remark", mPerson.remark);
                intent.putExtra("action", "modify");
                startActivityForResult(intent, MODIFY_REMARK_REQUEST);
            }
        });

        mFriendAccount = getIntent().getStringExtra("friendAccount");
        mIsFriend = getIntent().getStringExtra("isFriend");
        mCategory = getIntent().getStringExtra("category");
        mPerson = getIntent().getParcelableExtra("person");

        if (mPerson != null) {
            mFriendAccount = mPerson.userAccount;
            mIsFriend = mPerson.isFriend;
            updatePersonInfo();
        } else {
            mPerson = new FindPersonInfo();
            getFriendInfo(mFriendAccount);
        }
    }

    private void getFriendInfoFromDB() {
        ContactDao contactDao = ContactDao.getInstance(this);
        UserInfo userInfo = contactDao.queryUser(mFriendAccount);
        if (userInfo != null) {
            mPerson.userAccount = mFriendAccount;
            mPerson.headUrl = userInfo.headUrl;
            mPerson.nickname = userInfo.nickname;
            mPerson.age = userInfo.age;
            mPerson.sex = userInfo.sex;
            mPerson.sign = userInfo.sign;
            mPerson.remark = userInfo.remark;
            mPerson.isFriend = mIsFriend;
            updatePersonInfo();
        }
    }

    private void updatePersonInfo() {
        if (!TextUtils.isEmpty(mPerson.headUrl)) {
            GlideApp.with(this)
                    .load(mPerson.headUrl)
                    .circleCrop()
                    .into(mIvAvatar);
        }
        if (TextUtils.isEmpty(mPerson.remark)) {
            mTvName.setText(mPerson.nickname);
        } else {
            mTvName.setText(mPerson.remark);
            mTvNickname.setText("昵称：" + mPerson.nickname);
        }
        if ("男".equals(mPerson.sex)) {
            mIvSex.setImageResource(R.drawable.ic_male);
        } else if ("女".equals(mPerson.sex)) {
            mIvSex.setImageResource(R.drawable.ic_female);
        }
        if (!TextUtils.isEmpty(mPerson.age)) {
            mTvAge.setText(mPerson.age);
        }
        if (!TextUtils.isEmpty(mPerson.sign)) {
            mTvSign.setText(mPerson.sign);
        }
        if (!TextUtils.isEmpty(mPerson.remark)) {
            mTvRemark.setText(mPerson.remark);
        }
        if (!TextUtils.isEmpty(mPerson.areaCode)) {
            RegionDao regionDao = new RegionDao(this);
            List<RegionDao.KeyValue> data = regionDao.getRegionByCode(mPerson.areaCode);
            StringBuilder addr = new StringBuilder();
            for (RegionDao.KeyValue kv : data) {
                if (kv.key.length() == 2) {
                    addr = new StringBuilder(kv.value);
                } else if (kv.key.length() == 4) {
                    addr = new StringBuilder(kv.value);
                } else if (kv.key.length() == 6) {
                    if (!addr.toString().equals(kv.value)) {
                        addr.append(kv.value);
                    }
                }
            }
            mTvLocation.setText("地区：" + addr);
        }
        if (!TextUtils.isEmpty(mPerson.isFriend)) {
            if (mPerson.isFriend.equals("0")) {
                mTvAddFriend.setVisibility(View.VISIBLE);
                mTvComplaint.setVisibility(View.VISIBLE);
                mTvSendMsg.setVisibility(View.GONE);
                mTvDelete.setVisibility(View.GONE);
                mTvComplaintTiny.setVisibility(View.GONE);
                mLlRemark.setVisibility(View.GONE);
            } else if (mPerson.isFriend.equals("1")) {
                mTvAddFriend.setVisibility(View.GONE);
                mTvComplaint.setVisibility(View.GONE);
                mTvSendMsg.setVisibility(View.VISIBLE);
                mTvDelete.setVisibility(View.VISIBLE);
                mTvComplaintTiny.setVisibility(View.VISIBLE);
                mLlRemark.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_avatar:
                Intent imagePreview = new Intent();
                imagePreview.setClass(this, ImagePreviewActivity.class);
                imagePreview.putExtra("imgUrl", mPerson.headUrl);
                imagePreview.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(imagePreview);
                break;
            case R.id.tv_add_friend:
                Intent intent = new Intent(this, FriendApplyActivity.class);
                intent.putExtra("friendAccount", mFriendAccount);
                startActivity(intent);
                break;
            case R.id.tv_complaint:
                complaint();
                break;
            case R.id.tv_send_msg:
                Intent jumpIntent = new Intent();
                jumpIntent.setClass(PersonInfoActivity.this, ChatActivity.class);
                jumpIntent.putExtra("chatId", "");
                jumpIntent.putExtra("userAccount", mFriendAccount);
                jumpIntent.putExtra("position", "");
                jumpIntent.putExtra("owner", SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
                jumpIntent.putExtra("category", "1");
                startActivity(jumpIntent);
                break;
            case R.id.tv_delete:
                showDeleteDialog();
                break;
            case R.id.tv_complaint_tiny:
                complaint();
                break;
        }
    }

    private void showDeleteDialog() {
        mDeleteDialog = new DeleteDialog(this, R.style.round_corner_dialog,
                "将好友\"" + mTvName.getText().toString() + "\"删除，同时删除与该联系人的聊天记录", new DeleteDialog.MyDialogListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_confirm_collect:
                        mDeleteDialog.dismiss();
                        deleteFriend();
                        break;
                    case R.id.tv_cancel_collect:
                        mDeleteDialog.dismiss();
                        break;
                }
            }
        });
        mDeleteDialog.show();
    }

    /**
     * 投诉
     */
    private void complaint() {
        Intent complainIntent = new Intent(this, SimpleWebActivity.class);
        complainIntent.putExtra("title", "投诉");
        complainIntent.putExtra("url", AppConstants.COMPLAIN_PAGE);
        complainIntent.putExtra("friendAccount", mFriendAccount);
        complainIntent.putExtra("userAccount", SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        if(!TextUtils.isEmpty(mCategory) && mCategory.equals("0")) {
            complainIntent.putExtra("complainType", "chat");
        } else {
            complainIntent.putExtra("complainType", "social");
        }
        startActivity(complainIntent);
    }

    private void deleteFriend() {
        AddOrRemoveFriendRequestBody body = new AddOrRemoveFriendRequestBody();
        body.type = "1";
        body.friendAccount = mFriendAccount;
        body.message = "";
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                ToastUtil.showToast(PersonInfoActivity.this, "好友已删除");
                FriendsDao friendsDao = FriendsDao.getInstance(PersonInfoActivity.this);
                friendsDao.delete(mFriendAccount, SharedPreferencesInfo.getTagString(PersonInfoActivity.this, SharedPreferencesInfo.ACCOUNT));
                ChatListDao chatListDao = ChatListDao.getInstance(PersonInfoActivity.this);
                chatListDao.deleteWithAccount(mFriendAccount,
                        SharedPreferencesInfo.getTagString(PersonInfoActivity.this, SharedPreferencesInfo.ACCOUNT));
                ChatMsgDao chatMsgDao = ChatMsgDao.getInstance(PersonInfoActivity.this);
                chatMsgDao.delAllChatMsgFromAccount(mFriendAccount,
                        SharedPreferencesInfo.getTagString(PersonInfoActivity.this, SharedPreferencesInfo.ACCOUNT));
                ContactDao contactDao = ContactDao.getInstance(PersonInfoActivity.this);
                contactDao.delUser(mFriendAccount);
                FriendApplyDao.getInstance(PersonInfoActivity.this).delete(mFriendAccount, SharedPreferencesInfo.getTagString(PersonInfoActivity.this, SharedPreferencesInfo.ACCOUNT));

                Intent intent = new Intent(AppConstants.ACTION_DELETE_FRIEND);
                sendBroadcast(intent.putExtra("userAccount", mFriendAccount));
                PersonInfoActivity.this.finish();
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(PersonInfoActivity.this, R.string.social_delete_friend_failed);
            }
        };
        WebServiceIf.addOrRemoveFriend(this, body, callback);
    }

    private void getFriendInfo(final String userAccount) {
        final GetFriendInfoRequestBody requestBody = new GetFriendInfoRequestBody();
        requestBody.friendAccount = userAccount;
        WebServiceIf.IResponseCallback getFriendInfoCallbackIf = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    UserInfoResponseObject responseObj = gson.fromJson(response, UserInfoResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            UserInfo userInfo = responseObj.body;
                            userInfo.userAccount = userAccount;
                            //将联系人数据存数据库
                            try {
                                ContactDao contactDao = ContactDao.getInstance(PersonInfoActivity.this);
                                contactDao.saveUser(userInfo);

                                mPerson.userAccount = mFriendAccount;
                                mPerson.headUrl = userInfo.headUrl;
                                mPerson.nickname = userInfo.nickname;
                                mPerson.age = userInfo.age;
                                mPerson.sex = userInfo.sex;
                                mPerson.sign = userInfo.sign;
                                mPerson.remark = userInfo.remark;
                                mPerson.isFriend = mIsFriend;
                                mPerson.areaCode = userInfo.areaCode;
                                updatePersonInfo();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastUtil.showToast(PersonInfoActivity.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                            getFriendInfoFromDB();
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(PersonInfoActivity.this, R.string.request_fail_warning);
                getFriendInfoFromDB();
            }
        };
        // 调用接口发起登陆
        WebServiceIf.getFriendInfo(this, requestBody, getFriendInfoCallbackIf);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MODIFY_REMARK_REQUEST) {
                String remark = data.getStringExtra("remark");
                mPerson.remark = remark;
                if (!TextUtils.isEmpty(remark)) {
                    mTvName.setText(remark);
                    if (!TextUtils.isEmpty(mPerson.nickname)) {
                        mTvNickname.setText("昵称：" + mPerson.nickname);
                        mTvNickname.setVisibility(View.VISIBLE);
                    }
                } else {
                    mTvName.setText(mPerson.nickname);
                    mTvNickname.setVisibility(View.GONE);
                }
                mTvRemark.setText(remark);
                sendBroadcast(new Intent(AppConstants.ACTION_MODIFY_REMARK));
            }
        }
    }
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：安卓和服务端交互接口
 *
 *
 * 创建标识：luchaowei 20141204
 */
package com.cqsynet.swifi.network;

import android.content.Context;
import android.util.Log;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.model.AddOrRemoveFriendRequestBody;
import com.cqsynet.swifi.model.BindRequestBody;
import com.cqsynet.swifi.model.CloseFreeWifiRequestBody;
import com.cqsynet.swifi.model.CollectRemoveRequestBody;
import com.cqsynet.swifi.model.CollectRequestBody;
import com.cqsynet.swifi.model.CommentListRequestBody;
import com.cqsynet.swifi.model.CommentReplyRequestBody;
import com.cqsynet.swifi.model.CommentRequestBody;
import com.cqsynet.swifi.model.DeleteBottleRequestBody;
import com.cqsynet.swifi.model.FindPersonRequestBody;
import com.cqsynet.swifi.model.FriendsRequestBody;
import com.cqsynet.swifi.model.GalleryRequestBody;
import com.cqsynet.swifi.model.GetFriendInfoRequestBody;
import com.cqsynet.swifi.model.LikeRequestBody;
import com.cqsynet.swifi.model.LoginRequestBody;
import com.cqsynet.swifi.model.LogsRequestBody;
import com.cqsynet.swifi.model.LotteryDetailRequestBody;
import com.cqsynet.swifi.model.LotteryListRequestBody;
import com.cqsynet.swifi.model.ModifyFriendRemarkRequestBody;
import com.cqsynet.swifi.model.NewsSearchRequestBody;
import com.cqsynet.swifi.model.OpenFreeWifiRequestBody;
import com.cqsynet.swifi.model.RegistRequestBody;
import com.cqsynet.swifi.model.ReplyFriendRequestBody;
import com.cqsynet.swifi.model.ReplyListRequestBody;
import com.cqsynet.swifi.model.RequestBody;
import com.cqsynet.swifi.model.RequestObject;
import com.cqsynet.swifi.model.ReturnBottleRequestBody;
import com.cqsynet.swifi.model.SendMessageRequestBody;
import com.cqsynet.swifi.model.SetTagsRequestBody;
import com.cqsynet.swifi.model.StatisticsRequestBody;
import com.cqsynet.swifi.model.SubmitWifiListRequestBody;
import com.cqsynet.swifi.model.SuggestListRequestBody;
import com.cqsynet.swifi.model.SuggestRequestBody;
import com.cqsynet.swifi.model.ThrowBottleRequestBody;
import com.cqsynet.swifi.model.UpdatePhoneRequestBody;
import com.cqsynet.swifi.model.UpdatePwdRequestBody;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.VerifyCodeRequestBody;
import com.cqsynet.swifi.model.WifiUseInfoRequestBody;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

public class WebServiceIf {

    /**
     * 登陆函数
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void login(Context ctx, LoginRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_LOGIN, "v1000", callbackIf);
    }

    /**
     * 获取验证码
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getVerifyCode(Context ctx, VerifyCodeRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_GET_VERIFY_CODE, "v1000", callbackIf);
    }

    /**
     * 注册
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void regist(Context ctx, RegistRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_REGIST, "v1000", callbackIf);
    }

    /**
     * 忘记密码，修改密码
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void changePsw(Context ctx, RequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_FORGET_PSW, "v1000", callbackIf);
    }

    /**
     * 获取频道列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getNewsChannels(Context ctx, RequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.CMS_SYS, AppConstants.IF_GET_CHANNELS, "v1000", callbackIf);
    }

    /**
     * 修改频道列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void setNewsChannels(Context ctx, RequestBody requestBody, IResponseCallback callbackIf) {
        Log.i("chaowei", "chaowei00 setNewsChannels");
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_SET_CHANNELS, "v1000", callbackIf);
    }

    /**
     * 获取新闻列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getNewsList(Context ctx, RequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.CMS_SYS, AppConstants.IF_GET_NEWS_LIST, "v1002", callbackIf);
    }

    /**
     * 获取搜索结果列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getNewsSearchList(Context ctx, NewsSearchRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.CMS_SYS, AppConstants.IF_GET_SEARCH_LIST, "v1000", callbackIf);
    }

    /**
     * 获取热门新闻结果列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getNewsHotList(Context ctx, RequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.CMS_SYS, AppConstants.IF_GET_NEWS_HOT, "v1000", callbackIf);
    }

    /**
     * 获取启动图片
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void getLaunchImg(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.AD_SYS, AppConstants.IF_GET_LAUNCH, "v1000", callbackIf);
    }

    /**
     * 更新用户分组
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void updateUserGroup(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.GATEWAY_SYS, AppConstants.IF_UPDATE_USER_GROUP, "v1000", callbackIf);
    }

    /**
     * 获取广告
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void getAdv(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.AD_SYS, AppConstants.IF_GET_AD, "v1000", callbackIf);
    }


    /**
     * 获取用户信息
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void getUserInfo(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.USER_SYS, AppConstants.IF_GET_USER_INFO, "v1000", callbackIf);
    }

    /**
     * 获取联系人信息
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getFriendInfo(Context ctx, GetFriendInfoRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.SNS_SYS, AppConstants.IF_GET_FRIEND_INFO, "v1000", callbackIf);
    }

    /**
     * 获取反饋列表
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void getSuggestList(Context ctx, SuggestListRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_GET_SUGGEST_LIST, "v1000", callbackIf);

    }

    /**
     * 修改用户信息
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void updateUserInfo(Context ctx, File headFile, UserInfo userInfo, IResponseCallback callbackIf) {
        ArrayList<File> files = new ArrayList<>();
        if (headFile != null) {
            files.add(headFile);
        }
        excuteRequest(ctx, files, userInfo, AppConstants.USER_SYS, AppConstants.IF_UPDATE_USER_INFO, "v1000", callbackIf);
    }

    /**
     * 提交意见反馈
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void submitSuggest(Context ctx, ArrayList<File> imgs, SuggestRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, imgs, requestBody, AppConstants.USER_SYS, AppConstants.IF_SUGGEST, "v1000", callbackIf);
    }

    /**
     * 修改手机号码
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void updatePhone(Context ctx, UpdatePhoneRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_CHANGE_PHONE, "v1000", callbackIf);
    }

    /**
     * 修改登录密码
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void updatePwd(Context ctx, UpdatePwdRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_UPDATE_PWD, "v1000", callbackIf);
    }

    /**
     * 获取上网使用信息
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getWifiUseInfo(Context ctx, WifiUseInfoRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.GATEWAY_SYS, AppConstants.IF_GET_WIFI_USE_INFO, "v1000", callbackIf);
    }

    /**
     * 获取今日上网使用时长（累计使用分钟数）
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void getTodayWifiUseTime(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.GATEWAY_SYS, AppConstants.IF_GET_TODAY_WIFI_USE_TIME, "v1000", callbackIf);
    }

    /**
     * 开启免费上网
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void openFreeWifi(Context ctx, OpenFreeWifiRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.GATEWAY_SYS, AppConstants.IF_OPEN_WIFI, "v1000", callbackIf);
    }

    /**
     * 关闭免费上网
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void closeFreeWifi(Context ctx, CloseFreeWifiRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.GATEWAY_SYS, AppConstants.IF_CLOSE_WIFI, "v1000", callbackIf);
    }

    /**
     * 获取专题列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getTopicList(Context ctx, RequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.CMS_SYS, AppConstants.IF_GET_TOPIC_LIST, "v1000", callbackIf);
    }

    /**
     * 获取版本信息
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void getVersionInfo(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.UPDATE_SYS, AppConstants.IF_GET_VERSION, "v1000", callbackIf);
    }


    /**
     * 获取图集
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getGallery(Context ctx, GalleryRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.CMS_SYS, AppConstants.IF_GET_GALLERY, "v1001", callbackIf);
    }

    /**
     * 收藏
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void collect(Context ctx, CollectRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_COLLECT, "v1001", callbackIf);
    }

    /**
     * 取消收藏
     *
     * @param ctx         调用页面的上下文
     * @param requestObject 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void removeCollect(Context ctx, CollectRemoveRequestBody requestObject, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestObject, AppConstants.USER_SYS, AppConstants.IF_REMOVE_COLLECT, "v1000", callbackIf);
    }

    /**
     * 获取收藏
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void getCollect(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.USER_SYS, AppConstants.IF_GET_COLLECT, "v1001", callbackIf);
    }

    /**
     * 百度云绑定函数
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void bindToServer(Context ctx, BindRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.MSGPUSH_SYS, AppConstants.IF_BINDBD, "v1000", callbackIf);
    }

    /**
     * 获取用户标签
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getTags(Context ctx, SetTagsRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.USER_SYS, AppConstants.IF_GET_TAGS, "v1000", callbackIf);
    }

    /**
     * 获取我的抽奖结果列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getLotteryList(Context ctx, LotteryListRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.CMS_SYS, AppConstants.IF_GET_LOTTERY_LIST, "v1001", callbackIf);
    }

    /**
     * 获取抽奖结果详情
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getLotteryDetail(Context ctx, LotteryDetailRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.CMS_SYS, AppConstants.IF_GET_LOTTERY_DETAIL, "v1001", callbackIf);
    }

    /**
     * 提交统计信息
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void submitStatistics(Context ctx, StatisticsRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.STATISTICS_SYS, AppConstants.IF_SUBMIT_STATISTICS, "v1002", callbackIf);
    }

    /**
     * 提交日志信息
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void submitLogs(Context ctx, LogsRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.STATISTICS_SYS, AppConstants.IF_SUBMIT_LOGS, "v1000", callbackIf);
    }

    /**
     * 捡瓶子
     */
    public static void pickBottle(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.SNS_SYS, AppConstants.IF_PICK_BOTTLE, "v1000", callbackIf);
    }

    /**
     * 丢瓶子
     *
     * @param ctx         调用页面的上下文
     * @param files       语音文件(可选)
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void throwBottle(Context ctx, ArrayList<File> files, ThrowBottleRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, files, requestBody, AppConstants.SNS_SYS, AppConstants.IF_THROW_BOTTLE, "v1000", callbackIf);
    }

    /**
     * 聊天发消息
     *
     * @param ctx         调用页面的上下文
     * @param files       语音或者图片文件(可选)
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void sendMessage(Context ctx, ArrayList<File> files, SendMessageRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, files, requestBody, AppConstants.SNS_SYS, AppConstants.IF_SEND_MSG, "v1001", callbackIf);
    }

    /**
     * 将瓶子扔回水里
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void returnBottle(Context ctx, ReturnBottleRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.SNS_SYS, AppConstants.IF_RETURN_BOTTLE, "v1000", callbackIf);
    }

    /**
     * 删除瓶子
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void deleteBottle(Context ctx, DeleteBottleRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.SNS_SYS, AppConstants.IF_DELETE_BOTTLE, "v1000", callbackIf);
    }

    /**
     * 找人
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void findPerson(Context ctx, FindPersonRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.SNS_SYS, AppConstants.IF_FIND_PERSON, "v1000", callbackIf);
    }

    /**
     * 获取好友列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getFriends(Context ctx, FriendsRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.SNS_SYS, AppConstants.IF_GET_FRIENDS, "v1000", callbackIf);
    }

    /**
     * 添加/删除好友
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void addOrRemoveFriend(Context ctx, AddOrRemoveFriendRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.SNS_SYS, AppConstants.IF_ADD_OR_REMOVE_FRIEND, "v1000", callbackIf);
    }

    /**
     * 响应好友请求
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void replyFriendRequest(Context ctx, ReplyFriendRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.SNS_SYS, AppConstants.IF_REPLY_FRIEND, "v1000", callbackIf);
    }

    public static void modifyFriendRemark(Context ctx, ModifyFriendRemarkRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.SNS_SYS, AppConstants.IF_MODIFY_FRIEND_REMARK, "v1000", callbackIf);
    }

    /**
     * 扫描图片抽奖
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     * @param ctx
     */
    public static void scanLottery(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.LOTTERY_SYS, AppConstants.IF_SCAN_LOTTERY, "v1000", callbackIf);
    }

    /**
     * 获取网址黑白名单规则
     *
     * @param ctx        调用页面的上下文
     * @param callbackIf 请求返回时的回调
     */
    public static void getUrlRule(Context ctx, IResponseCallback callbackIf) {
        excuteRequest(ctx, new RequestBody(), AppConstants.BASE_SYS, AppConstants.IF_GET_URL_RULE, "v1000", callbackIf);
    }

    /**
	 * 提交附近的WIFI信息
	 *
	 * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
	 */
    public static void sendWifiList(Context ctx, SubmitWifiListRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.WIFI_SYS, AppConstants.IF_SUBMIT_WIFI_LIST, "v1000", callbackIf);
    }

    /**
     * 获取一级评论列表
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getCommentList(Context ctx, CommentListRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.COMMENT_SYS, AppConstants.IF_GET_LEVEL_ONE_COMMENT_LIST, "v1000", callbackIf);
    }

    /**
     * 获取二级评论列表
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getReplyList(Context ctx, ReplyListRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.COMMENT_SYS, AppConstants.IF_GET_LEVEL_TWO_COMMENT_LIST, "v1001", callbackIf);
    }

    /**
     * 提交评论
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void submitComment(Context ctx, CommentRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.COMMENT_SYS, AppConstants.IF_SUBMIT_COMMENT, "v1000", callbackIf);
    }

    /**
     * 赞
     * @param ctx
     * @param requestBody
     * @param callbackIf
     */
    public static void submitLike(Context ctx, LikeRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.COMMENT_SYS, AppConstants.IF_LIKE, "v1000", callbackIf);
    }

    /**
     * 获取评论回复列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getCommentReplyList(Context ctx, CommentReplyRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.COMMENT_SYS, AppConstants.IF_COMMENT_REPLY, "v1000", callbackIf);
    }

    /**
     * 获取我的评论列表
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     */
    public static void getMyCommentList(Context ctx, CommentReplyRequestBody requestBody, IResponseCallback callbackIf) {
        excuteRequest(ctx, requestBody, AppConstants.COMMENT_SYS, AppConstants.IF_MY_COMMENT, "v1000", callbackIf);
    }

    /**
     * 带文件上传的Volley请求
     *
     * @param ctx         调用页面的上下文
     * @param files       上传的文件
     * @param requestBody 请求参数的数据类实例
     * @param rId         消息接收服务器id
     * @param tid         接口id
     * @param tVer        接口版本号
     * @param callbackIf  回调
     */
    public static void excuteRequest(Context ctx, ArrayList<File> files, RequestBody requestBody, String rId, String tid, String tVer, IResponseCallback callbackIf) {
        RequestObject requestObj = new RequestObject(ctx, rId, tid, tVer);
        requestObj.data.body = requestBody;
        OkgoRequest.excute(ctx, AppConstants.SERVER_MULTIPART_URL, files, requestObj, callbackIf, true);
    }

    /**
     * 执行Volley请求
     *
     * @param ctx         调用页面的上下文
     * @param requestBody 请求参数的数据类实例
     * @param callbackIf  请求返回时的回调
     * @param ifName      接口名称
     */
    public static void excuteRequest(Context ctx, RequestBody requestBody, String rId, String ifName, String tVer, IResponseCallback callbackIf) {
        RequestObject requestObj = new RequestObject(ctx, rId, ifName, tVer);
        requestObj.data.body = requestBody;
        OkgoRequest.excute(ctx, AppConstants.SERVER_URL, requestObj, callbackIf);
    }

    /*
     * 回调函数接口
     */
    public interface IResponseCallback {

        void onResponse(String response) throws JSONException; // 正常返回时的回调

        void onErrorResponse(); // 错误处理的回调
    }
}

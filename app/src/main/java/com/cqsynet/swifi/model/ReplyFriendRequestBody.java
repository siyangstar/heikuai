package com.cqsynet.swifi.model;

/**
 * Author: sayaki
 * Date: 2017/12/4
 */
public class ReplyFriendRequestBody extends RequestBody {

    public String messageId;
    public String type;   // 0: 同意; 1: 拒绝; 2: 加入黑名单
    public String friendAccount;
    public String remark;
}

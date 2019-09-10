package com.cqsynet.swifi.model;

/**
 * Author: sayaki
 * Date: 2017/12/4
 */
public class AddOrRemoveFriendRequestBody extends RequestBody {

    public String type;   // 0: 添加; 1: 删除
    public String friendAccount;
    public String message;
}

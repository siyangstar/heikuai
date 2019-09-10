package com.cqsynet.swifi.model;

import java.util.ArrayList;

/**
 * Author: sayaki
 * Date: 2017/12/4
 */
public class FriendsResponseObject extends BaseResponseObject {

    public FriendsResponseBody body;

    public class FriendsResponseBody {
        public ArrayList<FriendsInfo> userList;
    }
}

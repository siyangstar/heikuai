package com.cqsynet.swifi.model;

import java.util.ArrayList;

/**
 * Author: Arturia
 * Date: 2017/12/18
 */
public class FindPersonResponseObject extends BaseResponseObject {

    public FindPersonResponseBody body;

    public class FindPersonResponseBody {
        public String station; // 用户所在车站
        public String line;    // 用户所在线路
        public ArrayList<FindPersonInfo> userList;
    }
}

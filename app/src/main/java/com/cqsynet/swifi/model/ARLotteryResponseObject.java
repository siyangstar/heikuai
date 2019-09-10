package com.cqsynet.swifi.model;

/**
 * Author: sayaki
 * Date: 2017/5/18
 */
public class ARLotteryResponseObject extends BaseResponseObject {

    public ARLotteryResponseBody body;

    public class ARLotteryResponseBody {

        public String type;
        public String description;
        public String value;
    }
}

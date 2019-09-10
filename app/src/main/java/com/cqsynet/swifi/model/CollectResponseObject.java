package com.cqsynet.swifi.model;

import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/6/26
 */
public class CollectResponseObject extends BaseResponseObject {

    public CollectResponseBody body;

    public class CollectResponseBody {

        public String favorListCount;
        public List<CollectInfo> favorList;
    }
}

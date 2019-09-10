package com.cqsynet.swifi.model;

import java.util.ArrayList;

/**
 * Author: sayaki
 * Date: 2017/9/7
 */
public class ReplyResponseObject extends BaseResponseObject {

    public ReplyResponseBody body;

    public class ReplyResponseBody {
        public ArrayList<CommentInfo> newComments;
    }
}

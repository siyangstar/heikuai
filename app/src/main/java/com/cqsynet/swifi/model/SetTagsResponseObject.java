package com.cqsynet.swifi.model;

import java.util.List;


public class SetTagsResponseObject extends BaseResponseObject{
	 public SetTagsResponseBody body;

	    public class SetTagsResponseBody {
	        
	        //请求返回用户的标签
	        public List<String> tags;
	    }
}

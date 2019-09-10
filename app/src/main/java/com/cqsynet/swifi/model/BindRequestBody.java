package com.cqsynet.swifi.model;

import java.util.List;

public class BindRequestBody extends RequestBody{
		// 用户userId
		public  String BDUserId;
		// 用户channelId
		public  String BDChannelId;
		// 用户标签是否设置成功
		public  List<String> failedTags;
}

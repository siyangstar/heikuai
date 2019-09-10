package com.cqsynet.swifi.model;


public class UpdateUserGroupResponseObject extends BaseResponseObject {
	public UpdateUserGroupResponseBody body;

	public class UpdateUserGroupResponseBody {
		public String status; //在线状态
	}
}
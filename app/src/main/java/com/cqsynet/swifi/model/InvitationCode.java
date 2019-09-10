/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：我的邀请码数据类
 *
 *
 * 创建标识：br 2015-3-11
 */

package com.cqsynet.swifi.model;

public class InvitationCode {
	
	//邀请码
		public String code;
		//是否使用过
		public boolean isUsed;
		//使用这个邀请码的电话
		public String phoneNo;
		
		public String parentInvitationCode;
		public String userAccount;
		public String userLevel;
		public String registTime;
		
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		
		public boolean getIsUsed() {
			return isUsed;
		}
		public void setUsed(boolean isUsed) {
			this.isUsed = isUsed;
		}
		public String getPhoneNo() {
			return phoneNo;
		}
		public void setPhoneNo(String phoneNo) {
			this.phoneNo = phoneNo;
		}
		public String getParentInvitationCode() {
			return parentInvitationCode;
		}
		public void setParentInvitationCode(String parentInvitationCode) {
			this.parentInvitationCode = parentInvitationCode;
		}
		public String getUserAccount() {
			return userAccount;
		}
		public void setUserAccount(String userAccount) {
			this.userAccount = userAccount;
		}
		public String getUserLevel() {
			return userLevel;
		}
		public void setUserLevel(String userLevel) {
			this.userLevel = userLevel;
		}
		public String getRegistTime() {
			return registTime;
		}
		public void setRegistTime(String registTime) {
			this.registTime = registTime;
		}
		
		
		

}

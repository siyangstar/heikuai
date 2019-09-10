/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取抽奖结果详情返回类
 *
 *
 * 创建标识：xy 20150918
 */
package com.cqsynet.swifi.model;

public class LotteryDetailResponseObject extends BaseResponseObject{
	
	public LotteryDetailResponseBody body;
	
	public class LotteryDetailResponseBody {

		public String title; // 标题
		public String prizeClass; // 获奖奖项
		public String prize; // 奖品
		public String rule; // 领奖规则
		
	}
}

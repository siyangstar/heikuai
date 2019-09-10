/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取抽奖列表返回类
 *
 *
 * 创建标识：xy 20150918
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;


public class LotteryListResponseObject extends BaseResponseObject{

	public LotteryListResponseBody body;
	
	public class LotteryListResponseBody {
		
		public int myLotteryCount; // 返回list抽奖结果总条数
        public ArrayList<LotteryInfo> myLotteryList; // 列表list
	}
}

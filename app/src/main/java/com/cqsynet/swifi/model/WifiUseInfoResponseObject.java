/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：wifi使用情况接口返回数据结构对象
 *
 *
 * 创建标识：duxl 20141223
 */
package com.cqsynet.swifi.model;

import java.util.List;


public class WifiUseInfoResponseObject extends BaseResponseObject {
    
	public Body body;
	
	public static final class Body {
		public String count;
		public String time;
		public String flow;
		public List<WiFiUseInfo> UsageStatistics;
	}
	
	public static final class WiFiUseInfo {
		
		public String date;
		
        public String totalTime;
        
        public String totalFlow;
	}
}

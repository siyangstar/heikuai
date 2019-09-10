/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：广告位接回返回数据结构对象
 *
 *
 * 创建标识：duxl 20150116
 */
package com.cqsynet.swifi.model;

import java.util.List;

public class AdvResponseObject extends BaseResponseObject {
    
	public Body body;
	
	public class Body {
		public List<AdvInfoObject> adList;
	}
}

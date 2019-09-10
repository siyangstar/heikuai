/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：启动图片接口返回数据结构对象
 *
 *
 * 创建标识：duxl 20150108
 */
package com.cqsynet.swifi.model;

import java.util.List;

public class LaunchImgResponseObject extends BaseResponseObject {
	
	public Body body;
    
	public class Body {
		public List<LaunchImageObject> adList;
	}
}

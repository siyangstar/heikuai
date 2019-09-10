/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：版本更新接口返回数据结构对象
 *
 *
 * 创建标识：duxl 20150305
 */
package com.cqsynet.swifi.model;


public class CheckVersionResponseObject extends BaseResponseObject {
	
    public CheckVersionBody body;

    public class CheckVersionBody {
    	public LastVerInfo verInfo;
    }
}

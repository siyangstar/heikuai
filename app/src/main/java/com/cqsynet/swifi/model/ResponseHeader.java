/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：http响应头
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.model;

public class ResponseHeader {
    // 请求结果
    public String ret;
    // 响应代码
    public String errCode;
    // 错误信息描述
    public String errMsg;
    // 版本信息
    public VerInfo verInfo;
    // 响应消息体


    public class VerInfo {
        // 显示版本号
        public String verCode;
        // 版本名称
        public String verName;
        // 版本文件大小
        public String verSize;
        // 版本发布时间
        public String publishDate;
        // 版本简述
        public String des;
        // 强制更新最小版本号
        public String forceMiniVer;
        // 下载地址
        public String downloadUrl;
    }
}
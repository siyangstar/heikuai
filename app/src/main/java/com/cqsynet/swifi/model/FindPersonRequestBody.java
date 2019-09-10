package com.cqsynet.swifi.model;

/**
 * Author: sayaki
 * Date: 2017/11/29
 */
public class FindPersonRequestBody extends RequestBody {

    public String type;  // 0: 同列车; 1: 同站台; 2: 同路线; 3: 附近的人
    public String sex;   // 不限; 男; 女
    public String age;   // 不限; 18岁以下; 18-25; 26-32; 33-40; 40岁以上
    public String refresh; // 0: 重新获取实时数据; 1: 加载更多
}

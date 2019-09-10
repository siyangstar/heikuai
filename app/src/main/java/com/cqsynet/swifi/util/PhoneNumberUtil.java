/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：数据相关。
 *
 *
 * 创建标识：luchaowei 20141008
 */
package com.cqsynet.swifi.util;

import android.content.Context;
import android.text.TextUtils;

import com.cqsynet.swifi.R;

public class PhoneNumberUtil {

    /**
     * @Description: 验证手机号码格式
     * @param phoneNum 手机号码
     * @return boolean true 为手机号格式。 false 为非手机号格式
     */
    public static  boolean verifyPhoneNum(String phoneNum, Context context) {
        if (TextUtils.isEmpty(phoneNum)) {
            ToastUtil.showToast(context, R.string.empty_phone_warning);
            return false;
        } 

        if (!isMobileNum(phoneNum)) {
            ToastUtil.showToast(context, R.string.phone_format_warning);
            return false;
        } 
        return true;

    }

    /**
     * 判断输入是否为电话号码格式
     * @Description:
     * @param phone 输入的电话号码
     * @return   
     * @return: boolean  判断结果
     */
    public static boolean isMobileNum(String phone) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157 (TD)、158、159、184、187、188
         * 联通：130、131、132、152、155、156、185、186 
         * 电信：133、153、180、189、（1349卫通）
         * 苏宁: 170
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */

        // [1] 第一位为1，[3578] 第二位为3,5,7,8； \\d{9} 后面的是0~9的数字
        String matcher = "[1][3456789]\\d{9}";
        if (!TextUtils.isEmpty(phone)) {
            if (phone.startsWith("1") && phone.length() == 11) {
                return phone.matches(matcher);
            }
        }
        return false;
    }
    
    /**
     * 校验密码复杂度,必须是数字,字母(区分大小写),其它符号中的至少两种组合
     * @param pwd
     * @return
     */
    public static boolean verifyPwd(String pwd) {
    	String type = null;
    	char[] pwdAry = pwd.toCharArray();
    	for(int i = 0; i < pwdAry.length; i++) {
    		if((int)pwdAry[i] >= 48 && (int)pwdAry[i] <= 57) { //数字
    			if(type == null) {
    				type = "number";
    			} else if(!type.equals("number")) {
    				return true;
    			}
    		} else if((int)pwdAry[i] >= 65 && (int)pwdAry[i] <= 90) { //大写字母
    			if(type == null) {
    				type = "upCase";
    			} else if(!type.equals("upCase")) {
    				return true;
    			}
    		} else if((int)pwdAry[i] >= 97 && (int)pwdAry[i] <= 122) { //小写字母
    			if(type == null) {
    				type = "lowCase";
    			} else if(!type.equals("lowCase")) {
    				return true;
    			}
    		} else { //其他符号
    			if(type == null) {
    				type = "other";
    			} else if(!type.equals("other")) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：时间工具类
 *
 *
 * 创建标识：duxl 20141219
 */
package com.cqsynet.swifi.util;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 * @author duxl
 *
 */
@SuppressLint("SimpleDateFormat")
public class DateUtil {

	/**
	 * 获取相对时间
	 * @param time
	 * @return 当日<br />一小时内，返回 “**分前”<br />一小时以上，返回“**小时前”<br /> 非当日，返回“月-日”
	 */
	public static String getRelativeTimeSpanString(long time) {
		String result = null;
		long diffTime = System.currentTimeMillis() - time;
		
		if(android.text.format.DateUtils.isToday(time)) { // 当天
			
			long oneHourTimeSpan = 1000 * 60 * 60; // 1小时间隔毫秒数
			
			if(diffTime > oneHourTimeSpan) {
				result = (diffTime / oneHourTimeSpan) + "小时前";
				
			} else {
				long oneMinTimeSpan = 1000 * 60; // 1分钟间隔毫秒数
				long intervalTime = diffTime / oneMinTimeSpan;
				if (intervalTime == 0) {
					result = "1分钟前";
				} else {
					result = intervalTime + "分钟前";
				}
			}
			
		} else { // 非当天
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(time));
			result = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
			result = formatTime(new Date(time), "MM-dd HH:mm");
		}
		
		return result;
	}
	
	   /**
     * 判断和当前系统时间是否相差1小时
     * @param time
     * @return true 已超过一个小时， false 在一小时之内。
     */
    public static boolean overOneHour(long time) {
        boolean result = true;
        long diffTime = System.currentTimeMillis() - time;

        if(android.text.format.DateUtils.isToday(time)) { // 当天
            long oneHourTimeSpan = 1000 * 60 * 60; // 1小时间隔毫秒数

            if(diffTime < oneHourTimeSpan) {
                result = false;
            } 
        } 
        return result;
    }
	
	/**
	 * 解析时间
	 * @param time 时间字符串
	 * @param format 时间格式
	 * @return
	 * @throws ParseException
	 */
	@SuppressLint("SimpleDateFormat")
	public static Date parseTime(String time, String format) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.parse(time);
	}
	
	/**
	 * 格式化时间
	 * @param date 时间
	 * @param format 时间格式
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String formatTime(Date date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	/**
	 * 格式化时间
	 * @param time 时间
	 * @param format 时间格式
	 * @return
	 */
	public static String formatTime(long time, String format) {
		return formatTime(new Date(time), format);
	}
	
	/**
	 * 按指定格式string转Date
	 * @param dateString 时间字符串,格式为yyyy-MM-dd HH:mm
	 * @return 
	 */
	public static long stringToDate (String dateString) {
		Date date = null;
		try {  
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");  
		    date = sdf.parse(dateString);  
		}  catch (ParseException e) {  
		    e.printStackTrace();
		}
		if(date == null) {
			return 0;
		}
		return date.getTime();
	}
	
	public static void main(String[] args) throws ParseException {
		long time = System.currentTimeMillis();
		String format = "yyyy-MM-dd HH:mm:ss";
		String f = formatTime(time, format);
		
	}
}

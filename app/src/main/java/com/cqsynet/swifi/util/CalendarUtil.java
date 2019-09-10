/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：日历相关,包括查询农历,节气,节日
 *
 *
 * 创建标识：zhaosy 20140107
 */
package com.cqsynet.swifi.util;

import java.util.Calendar;
import java.util.Date;

public class CalendarUtil {
    // 农历
    private static final long[] lunarInfo = new long[] { 0x04bd8, 0x04ae0,
            0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0,
            0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540,
            0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5,
            0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3,
            0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0,
            0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0,
            0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8,
            0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570,
            0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5,
            0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0,
            0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50,
            0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0,
            0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7,
            0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50,
            0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954,
            0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260,
            0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0,
            0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0,
            0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20,
            0x0ada0 };

    // 节日
    private static final String[] gregorianHoliday = new String[] { "元旦",
            "情人节", "妇女节", "植树节", "消费者权益日", "愚人节", "劳动节", "青年节", "国际儿童节", "建党节",
            "建军节", "教师节", "国庆节", "平安夜", "圣诞节" };
    private static final String[] lunarHoliday = new String[] { "春节", "元宵",
            "端午", "七夕", "中元", "中秋", "重阳", "腊八", "扫房", "掸尘" };
    private static final int JANUARY = 1;
    private static final int FEBRURAY = 2;
    private static final int MARCH = 3;
    private static final int APRIL = 4;
    private static final int MAY = 5;
    private static final int JUNE = 6;
    private static final int JULY = 7;
    private static final int AUGUST = 8;
    private static final int SEPTEMBER = 9;
    private static final int OCTOBER = 10;
    private static final int NOVEMBER = 11;
    private static final int DECEMBER = 12;

    // 节气
    private static final long[] STermInfo = new long[] { 0, 21208, 42467,
            63836, 85337, 107014, 128867, 150921, 173149, 195551, 218072,
            240693, 263343, 285989, 308563, 331033, 353350, 375494, 397447,
            419210, 440795, 462224, 483532, 504758 };
    private static final String[] SolarTerm = new String[] { "小寒", "大寒", "立春",
            "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
            "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至" };

    /**
     * 传回农历某年的总天数
     * 
     * @param year
     *            年份
     * @return
     */
    final public static int lYearDays(int year) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((lunarInfo[year - 1900] & i) != 0) {
                sum += 1;
            }
        }
        return (sum + leapDays(year));
    }

    /**
     * 传回农历某年闰月的天数
     * 
     * @param year
     * @return
     */
    final public static int leapDays(int year) {
        if (leapMonth(year) != 0) {
            if ((lunarInfo[year - 1900] & 0x10000) != 0) {
                return 30;
            } else {
                return 29;
            }
        } else {
            return 0;
        }
    }

    /**
     * 获取农历某年闰那个月 1-12 , 没闰传回 0
     * 
     * @param year
     * @return
     */
    final public static int leapMonth(int year) {
        return (int) (lunarInfo[year - 1900] & 0xf);
    }

    /**
     * 获取农历 y年m月的总天数
     * 
     * @param y
     * @param m
     * @return
     */
    final public static int monthDays(int y, int m) {
        if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0) {
            return 29;
        } else {
            return 30;
        }
    }

    /**
     * 获取农历 y年的生肖
     * 
     * @param y
     * @return
     */
    final public static String AnimalsYear(int y) {
        final String[] Animals = new String[] { "鼠", "牛", "虎", "兔", "龙", "蛇",
                "马", "羊", "猴", "鸡", "狗", "猪" };
        return Animals[(y - 4) % 12];
    }

    /**
     * 获取月日的offset 传回干支, 0=甲子
     * 
     * @param num
     * @return
     */
    final public static String cyclicalm(int num) {
        final String[] Gan = new String[] { "甲", "乙", "丙", "丁", "戊", "己", "庚",
                "辛", "壬", "癸" };
        final String[] Zhi = new String[] { "子", "丑", "寅", "卯", "辰", "巳", "午",
                "未", "申", "酉", "戌", "亥" };
        return (Gan[num % 10] + Zhi[num % 12]);
    }

    /**
     * 传入年的offset 传回干支, 0=甲子
     * 
     * @param y
     * @return
     */
    final public static String cyclical(int y) {
        int num = y - 1900 + 36;
        return (cyclicalm(num));
    }

    /**
     * 获取农历.year0 .month1 .day2 .yearCyl3 .monCyl4.dayCyl5 .isLeap6
     * 
     * @param y
     * @param m
     * @return
     */
    final public long[] Lunar(int y, int m) {
        final int[] year20 = new int[] { 1, 4, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1 };
        final int[] year19 = new int[] { 0, 3, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0 };
        final int[] year2000 = new int[] { 0, 3, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1 };
        long[] nongDate = new long[7];
        int i = 0, temp = 0, leap = 0;
        Calendar baseCal = Calendar.getInstance();
        baseCal.set(Calendar.YEAR, 1900);
        baseCal.set(Calendar.MONTH, 1);
        baseCal.set(Calendar.DAY_OF_MONTH, 31);
        Date baseDate = baseCal.getTime();

        Calendar objCal = Calendar.getInstance();
        objCal.set(Calendar.YEAR, y);
        objCal.set(Calendar.MONTH, m);
        objCal.set(Calendar.DAY_OF_MONTH, 1);
        Date objDate = objCal.getTime();
        long offset = (objDate.getTime() - baseDate.getTime()) / 86400000L;
        if (y < 2000) {
            offset += year19[m - 1];
        }
        if (y > 2000) {
            offset += year20[m - 1];
        }
        if (y == 2000) {
            offset += year2000[m - 1];
        }
        nongDate[5] = offset + 40;
        nongDate[4] = 14;

        for (i = 1900; i < 2050 && offset > 0; i++) {
            temp = lYearDays(i);
            offset -= temp;
            nongDate[4] += 12;
        }
        if (offset < 0) {
            offset += temp;
            i--;
            nongDate[4] -= 12;
        }
        nongDate[0] = i;
        nongDate[3] = i - 1864;
        leap = leapMonth(i); // 闰那个月
        nongDate[6] = 0;

        for (i = 1; i < 13 && offset > 0; i++) {
            // 闰月
            if (leap > 0 && i == (leap + 1) && nongDate[6] == 0) {
                --i;
                nongDate[6] = 1;
                temp = leapDays((int) nongDate[0]);
            } else {
                temp = monthDays((int) nongDate[0], i);
            }

            // 解除闰月
            if (nongDate[6] == 1 && i == (leap + 1)) {
                nongDate[6] = 0;
            }
            offset -= temp;
            if (nongDate[6] == 0) {
                nongDate[4]++;
            }
        }

        if (offset == 0 && leap > 0 && i == leap + 1) {
            if (nongDate[6] == 1) {
                nongDate[6] = 0;
            } else {
                nongDate[6] = 1;
                --i;
                --nongDate[4];
            }
        }
        if (offset < 0) {
            offset += temp;
            --i;
            --nongDate[4];
        }
        nongDate[1] = i;
        nongDate[2] = offset + 1;
        return nongDate;
    }

    /**
     * 传出y年m月d日对应的农历.year0 .month1 .day2 .yearCyl3 .monCyl4 .dayCyl5 .isLeap6
     * 
     * @param y
     * @param m
     * @param d
     * @return
     */
    final public static long[] calElement(int y, int m, int d) {
        long[] nongDate = new long[7];
        int i = 0, temp = 0, leap = 0;
        Calendar baseCal = Calendar.getInstance();
        baseCal.set(Calendar.YEAR, 0);
        baseCal.set(Calendar.MONTH, 0);
        baseCal.set(Calendar.DAY_OF_MONTH, 31);
        Date baseDate = baseCal.getTime();

        Calendar objCal = Calendar.getInstance();
        objCal.set(Calendar.YEAR, y - 1900);
        objCal.set(Calendar.MONTH, m - 1);
        objCal.set(Calendar.DAY_OF_MONTH, d);
        Date objDate = objCal.getTime();
        long offset = (objDate.getTime() - baseDate.getTime()) / 86400000L;
        nongDate[5] = offset + 40;
        nongDate[4] = 14;

        for (i = 1900; i < 2050 && offset > 0; i++) {
            temp = lYearDays(i);
            offset -= temp;
            nongDate[4] += 12;
        }
        if (offset <= 0) {
            offset += temp;
            i--;
            nongDate[4] -= 12;
        }
        nongDate[0] = i;
        nongDate[3] = i - 1864;
        leap = leapMonth(i); // 闰那个月
        nongDate[6] = 0;

        for (i = 1; i < 13 && offset > 0; i++) {
            // 闰月
            if (leap > 0 && i == (leap + 1) && nongDate[6] == 0) {
                --i;
                nongDate[6] = 1;
                temp = leapDays((int) nongDate[0]);
            } else {
                temp = monthDays((int) nongDate[0], i);
            }

            // 解除闰月
            if (nongDate[6] == 1 && i == (leap + 1)) {
                nongDate[6] = 0;
            }
            offset -= temp;
            if (nongDate[6] == 0) {
                nongDate[4]++;
            }
        }

        if (offset == 0 && leap > 0 && i == leap + 1) {
            if (nongDate[6] == 1) {
                nongDate[6] = 0;
            } else {
                nongDate[6] = 1;
                --i;
                --nongDate[4];
            }
        }
        if (offset < 0) {
            offset += temp;
            --i;
            --nongDate[4];
        } else if (offset == 0) {
            --i;
            if (i == 0) {
                i = 12;
            }
            offset = monthDays((int) nongDate[0], i);
        }
        nongDate[1] = i;
        // nongDate[2] = offset + 1;
        nongDate[2] = offset;
        return nongDate;
    }

    /**
     * 获取农历中的日
     * 
     * @param day
     * @return
     */
    public static String getchina(int day) {
        String a = "";
        if (day == 10) {
            return "初十";
        }
        int two = (day) / 10;
        if (two == 0) {
            a = "初";
        }
        if (two == 1) {
            a = "十";
        }
        if (two == 2) {
            a = "廿";
        }
        if (two == 3) {
            a = "卅";
        }
        int one = day % 10;
        switch (one) {
        case 1:
            a += "一";
            break;
        case 2:
            a += "二";
            break;
        case 3:
            a += "三";
            break;
        case 4:
            a += "四";
            break;
        case 5:
            a += "五";
            break;
        case 6:
            a += "六";
            break;
        case 7:
            a += "七";
            break;
        case 8:
            a += "八";
            break;
        case 9:
            a += "九";
            break;
        }
        return a;
    }

    /**
     * 获取农历中的月
     * 
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static String getLunarInChn(int year, int month, int day) {
        long[] lngLunar = calElement(year, month, day);
        String monthInChn = "";
        switch ((int) lngLunar[1]) {
        case 1:
            monthInChn = "一";
            break;
        case 2:
            monthInChn = "二";
            break;
        case 3:
            monthInChn = "三";
            break;
        case 4:
            monthInChn = "四";
            break;
        case 5:
            monthInChn = "五";
            break;
        case 6:
            monthInChn = "六";
            break;
        case 7:
            monthInChn = "七";
            break;
        case 8:
            monthInChn = "八";
            break;
        case 9:
            monthInChn = "九";
            break;
        case 10:
            monthInChn = "十";
            break;
        case 11:
            monthInChn = "十一";
            break;
        case 12:
            monthInChn = "十二";
            break;

        }
        String strLunar = "农历:" + monthInChn + "月"
                + getchina((int) (lngLunar[2]));
        return strLunar;
    }

    /**
     * 获取公历节日
     * 
     * @param m
     * @param d
     * @return
     */
    public static String getGregorianHoliday(int m, int d) {
        switch (m) {
        case JANUARY:
            if (d == 1) {
                return gregorianHoliday[0];
            }
            break;
        case FEBRURAY:
            if (d == 14) {
                return gregorianHoliday[1];
            }
            break;
        case MARCH:
            if (d == 8) {
                return gregorianHoliday[2];
            } else if (d == 12) {
                return gregorianHoliday[3];
            } else if (d == 15) {
                return gregorianHoliday[4];
            }
            break;
        case APRIL:
            if (d == 1) {
                return gregorianHoliday[5];
            }
            break;
        case MAY:
            if (d == 1) {
                return gregorianHoliday[6];
            } else if (d == 4) {
                return gregorianHoliday[7];
            }
            break;
        case JUNE:
            if (d == 1) {
                return gregorianHoliday[8];
            }
            break;
        case JULY:
            if (d == 1) {
                return gregorianHoliday[9];
            }
            break;
        case AUGUST:
            if (d == 1) {
                return gregorianHoliday[10];
            }
            break;
        case SEPTEMBER:
            if (d == 10) {
                return gregorianHoliday[11];
            }
            break;
        case OCTOBER:
            if (d == 1) {
                return gregorianHoliday[12];
            }
            break;
        case DECEMBER:
            if (d == 24) {
                return gregorianHoliday[13];
            } else if (d == 25) {
                return gregorianHoliday[14];
            }
            break;
        default:
            break;
        }
        return "";
    }

    /**
     * 获取农历节日
     * 
     * @param y
     * @param m
     * @param d
     * @return
     */
    public static String getLunarHoliday(int y, int m, int d) {
        long[] lngLunar = CalendarUtil.calElement(y, m, d);
        int month = (int) lngLunar[1];
        int day = (int) lngLunar[2];
        switch (month) {
        case JANUARY:
            if (day == 1) {
                return lunarHoliday[0];
            } else if (day == 15) {
                return lunarHoliday[1];
            }
            break;
        case MAY:
            if (day == 5) {
                return lunarHoliday[2];
            }
            break;
        case JULY:
            if (day == 7) {
                return lunarHoliday[3];
            } else if (day == 15) {
                return lunarHoliday[4];
            }
            break;
        case SEPTEMBER:
            if (day == 9) {
                return lunarHoliday[5];
            }
            break;
        case DECEMBER:
            if (day == 8) {
                return lunarHoliday[6];
            } else if (day == 23) {
                return lunarHoliday[7];
            } else if (day == 24) {
                return lunarHoliday[8];
            }
            break;
        default:
            break;
        }
        return "";
    }

    /**
     * 根据日期(Date格式)得到节气
     * 
     * @param Date
     * @return
     */
    public static String getSoralTerm(Date Date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(Date);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DAY_OF_MONTH);
        return getSoralTerm(y, m, d);
    }

    /**
     * 根据日期(y年m月d日)得到节气
     * 
     * @param y
     * @param m
     * @param d
     * @return
     */
    public static String getSoralTerm(int y, int m, int d) {
        String solarTerms;
        if (d == sTerm(y, (m - 1) * 2)) {
            solarTerms = SolarTerm[(m - 1) * 2];
        } else if (d == sTerm(y, (m - 1) * 2 + 1)) {
            solarTerms = SolarTerm[(m - 1) * 2 + 1];
        } else {// 到这里说明非节气时间
            solarTerms = "";
        }
        return solarTerms;
    }

    /**
     * y年的第n个节气为几日(从0小寒起算)
     * 
     * @param y
     * @param n
     * @return
     */
    private static int sTerm(int y, int n) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1900);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 6);
        cal.set(Calendar.HOUR_OF_DAY, 2);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 0);

        long temp = cal.getTime().getTime();
        cal.setTime(new Date(
                (long) ((31556925974.7 * (y - 1900) + STermInfo[n] * 60000L) + temp)));
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取日期
     * @param time
     * @return 返回如 2014-09-30 的日期
     */
    public static String getDate(long time) {
    	Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONDAY) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 获取日期
     * @param date
     * @return 返回如 2014-09-30 的日期
     */
    public static String getDate(Date date) {
    	Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONDAY) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }
}
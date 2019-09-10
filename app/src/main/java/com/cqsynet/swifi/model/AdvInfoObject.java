/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：广告位配置实体信息
 *
 *
 * 创建标识：duxl 20150116
 */
package com.cqsynet.swifi.model;

import android.text.TextUtils;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;

import java.io.Serializable;
import java.util.Random;

public class AdvInfoObject implements Serializable {
	
	/**
	 * 广告位id
	 * ad0001=启动图广告位
	 * ad0002=我要上网（顶部）
	 * ad0003=我要上网（底部）
	 * ad0004=上网续时提醒广告位
	 * ad0005=点击上网续时后弹出广告位
	 * ad0006=应用商店（本地精品）
	 * ad0007=应用商店（热门推荐）
	 * ad0008=应用商店（手机必备）
	 * ad11059=资讯首页弹出广告
	 */
	public String id;
	
	/**
	 * 广告的id
	 */
	public String[] advId;
	
	/**
	 * 广告的标题
	 */
	public String[] adName;
	
	/**
	 * 广告类型：0图片、1视频
	 */
    public String[] type;
    
    /**
     * 轮播顺序
     */
    public String plan;
    
    /**
     * 轮播顺序的分隔符
     */
    public final static String PLAN_SPLIT_CHAR = ",";
    
    /**
     * 图片或视频地址
     */
    public String[] adUrl;
    
    /**
     * 跳转地址
     */
    public String[] jumpUrl;
    
    /**
     * 获取当前显示的索引
     * @param index
     * @return
     */
    public int getSortIndex(int index) {
    	String[] sort = plan.split(PLAN_SPLIT_CHAR);
    	try {
    		return Integer.parseInt(sort[index % sort.length].trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    /**
     * 获取当前排序（plan字段）元素的索引，调用此方法后，索引自动自增
     * @return
     */
    public int getCurrentIndex() {
		if(TextUtils.isEmpty(plan)) {
			return 0;
		}
    	int result = 0;
    	if("ad0001".equals(id)) {
    		if(Globals.index_ad0001 == -1) {
    			Globals.index_ad0001 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0001 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0001 = System.currentTimeMillis();
    			result = ++Globals.index_ad0001 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0001;
    		}  		
    	} else if("ad0002".equals(id)) {
    		if(Globals.index_ad0002 == -1) {
    			Globals.index_ad0002 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0002 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0002 = System.currentTimeMillis();
    			result = ++Globals.index_ad0002 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0002;
    		}
    	} else if("ad0003".equals(id)) {
    		if(Globals.index_ad0003 == -1) {
    			Globals.index_ad0003 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0003 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0003 = System.currentTimeMillis();
    			result = ++Globals.index_ad0003 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0003;
    		}  
    	} else if("ad0004".equals(id)) {
    		if(Globals.index_ad0004 == -1) {
    			Globals.index_ad0004 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0004 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0004 = System.currentTimeMillis();
    			result = ++Globals.index_ad0004 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0004;
    		}
    	} else if("ad0005".equals(id)) {
    		if(Globals.index_ad0005 == -1) {
    			Globals.index_ad0005 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0005 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0005 = System.currentTimeMillis();
    			result = ++Globals.index_ad0005 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0005;
    		}
    	} else if("ad0006".equals(id)) {
    		if(Globals.index_ad0006 == -1) {
    			Globals.index_ad0006 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0006 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0006 = System.currentTimeMillis();
    			result = ++Globals.index_ad0006 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0006;
    		}
    	} else if("ad0007".equals(id)) {
    		if(Globals.index_ad0007 == -1) {
    			Globals.index_ad0007 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0007 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0007 = System.currentTimeMillis();
    			result = ++Globals.index_ad0007 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0007;
    		} 
    	} else if("ad0008".equals(id)) {
    		if(Globals.index_ad0008 == -1) {
    			Globals.index_ad0008 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0008 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0008 = System.currentTimeMillis();
    			result = ++Globals.index_ad0008 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0008;
    		} 
    	} else if("ad0009".equals(id)) {
    		if(Globals.index_ad0009 == -1) {
    			Globals.index_ad0009 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0009 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0009 = System.currentTimeMillis();
    			result = ++Globals.index_ad0009 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0009;
    		}
    	} else if("ad0019".equals(id)) {
    		if(Globals.index_ad0019 == -1) {
    			Globals.index_ad0019 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
    		}
    		if(System.currentTimeMillis() - Globals.refreshTime_ad0019 > AppConstants.AD_REFRESH_INTEVAL) {
    			Globals.refreshTime_ad0019 = System.currentTimeMillis();
    			result = ++Globals.index_ad0019 % plan.split(PLAN_SPLIT_CHAR).length;
    		} else {
    			result = Globals.index_ad0019;
    		}
    	} else if("ad11059".equals(id)) {
			if(Globals.index_ad11059 == -1) {
				Globals.index_ad11059 = new Random().nextInt(plan.split(PLAN_SPLIT_CHAR).length);
			}
			if(System.currentTimeMillis() - Globals.refreshTime_ad11059 > AppConstants.AD_REFRESH_INTEVAL) {
				Globals.refreshTime_ad11059 = System.currentTimeMillis();
				result = ++Globals.index_ad11059 % plan.split(PLAN_SPLIT_CHAR).length;
			} else {
				result = Globals.index_ad11059;
			}
		}
        return result;
    }
}

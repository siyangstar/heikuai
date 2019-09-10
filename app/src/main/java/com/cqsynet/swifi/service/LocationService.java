/*
 * Copyright (C) 2017 重庆尚渝
 * 版权所有
 *
 * 后台服务,用于定位
 *
 * 创建标识：zhaosy 20171113
 */
package com.cqsynet.swifi.service;

import android.content.Context;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.SharedPreferencesInfo;

import java.util.Arrays;
import java.util.List;

public class LocationService {
	private LocationClient client = null;
	private LocationClientOption mOption,DIYoption;
	private Object objLock = new Object();

	/***
	 * 
	 * @param locationContext
	 */
	public LocationService(final Context locationContext){
		synchronized (objLock) {
			if(client == null){
				client = new LocationClient(locationContext);
				client.setLocOption(getDefaultLocationClientOption());
				registerListener(new BDAbstractLocationListener() {
					@Override
					public void onReceiveLocation(BDLocation location) {
						if (null != location && location.getLocType() != BDLocation.TypeServerError) {
							if(location.getLatitude() != 4.9E-324 && location.getLongitude() != 4.9E-324) {
								SharedPreferencesInfo.setTagString(locationContext, "latitude", location.getLatitude() + "");
								SharedPreferencesInfo.setTagString(locationContext, "longitude", location.getLongitude() + "");
								SharedPreferencesInfo.setTagString(locationContext, SharedPreferencesInfo.REAL_CITY, location.getCity());
								//判断是否为自有城市
								List<String> cityList = Arrays.asList(locationContext.getResources().getStringArray(R.array.city_code));
								if (cityList.contains(location.getCityCode())) {
									SharedPreferencesInfo.setTagString(locationContext, SharedPreferencesInfo.CITY_CODE, location.getCityCode());
								}
							}

							if(Globals.DEBUG) {
                                StringBuffer sb = new StringBuffer(256);
                                sb.append("time : ");
                                /**
                                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                                 */
                                sb.append(location.getTime());
                                sb.append("\nlocType : ");// 定位类型
                                sb.append(location.getLocType());
                                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
                                sb.append(location.getLocTypeDescription());
                                sb.append("\nlatitude : ");// 纬度
                                sb.append(location.getLatitude());
                                sb.append("\nlongitude : ");// 经度
                                sb.append(location.getLongitude());
                                sb.append("\naddr : ");// 地址信息
                                sb.append(location.getAddrStr());
                                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
                                sb.append(location.getUserIndoorState());
                                sb.append("\nlocationdescribe: ");
                                sb.append(location.getLocationDescribe());// 位置语义化信息
//							sb.append("\nPoi: ");// POI信息
//							if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
//								for (int i = 0; i < location.getPoiList().size(); i++) {
//									Poi poi = (Poi) location.getPoiList().get(i);
//									sb.append(poi.getName() + ";");
//								}
//							}
                                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                                    sb.append("\nspeed : ");
                                    sb.append(location.getSpeed());// 速度 单位：km/h
                                    sb.append("\nsatellite : ");
                                    sb.append(location.getSatelliteNumber());// 卫星数目
                                    sb.append("\nheight : ");
                                    sb.append(location.getAltitude());// 海拔高度 单位：米
                                    sb.append("\ngps status : ");
                                    sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                                    sb.append("\ndescribe : ");
                                    sb.append("gps定位成功");
                                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                                    // 运营商信息
                                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                                        sb.append("\nheight : ");
                                        sb.append(location.getAltitude());// 单位：米
                                    }
                                    sb.append("\noperationers : ");// 运营商信息
                                    sb.append(location.getOperators());
                                    sb.append("\ndescribe : ");
                                    sb.append("网络定位成功");
                                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                                    sb.append("\ndescribe : ");
                                    sb.append("离线定位成功，离线定位结果也是有效的");
                                } else if (location.getLocType() == BDLocation.TypeServerError) {
                                    sb.append("\ndescribe : ");
                                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                                    sb.append("\ndescribe : ");
                                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                                    sb.append("\ndescribe : ");
                                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                                }
                                sb.append("\n@@@\n@@@\n");
                                System.out.println(sb.toString());
                            }
						}
					}
				});
			}
		}
	}
	
	/***
	 * 
	 * @param listener
	 * @return
	 */
	
	public boolean registerListener(BDAbstractLocationListener listener){
		boolean isSuccess = false;
		if(listener != null){
			client.registerLocationListener(listener);
			isSuccess = true;
		}
		return  isSuccess;
	}
	
	public void unregisterListener(BDAbstractLocationListener listener){
		if(listener != null){
			client.unRegisterLocationListener(listener);
		}
	}
	
	/***
	 * 
	 * @param option
	 * @return isSuccessSetOption
	 */
	public boolean setLocationOption(LocationClientOption option){
		boolean isSuccess = false;
		if(option != null){
			if(client.isStarted())
				client.stop();
			DIYoption = option;
			client.setLocOption(option);
		}
		return isSuccess;
	}
	
	public LocationClientOption getOption(){
		return DIYoption;
	}
	/***
	 * 
	 * @return DefaultLocationClientOption
	 */
	public LocationClientOption getDefaultLocationClientOption(){
		if(mOption == null){
			mOption = new LocationClientOption();
			mOption.setLocationMode(LocationMode.Battery_Saving);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
			mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
			mOption.setScanSpan(120000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
		    mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
		    mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
		    mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
		    mOption.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		    mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死   
		    mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		    mOption.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
		    mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
		    mOption.setIsNeedAltitude(false);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
		}
		return mOption;
	}
	
	public void start(){
		synchronized (objLock) {
			if(client != null && !client.isStarted()){
				client.start();
			}
		}
	}
	public void stop(){
		synchronized (objLock) {
			if(client != null && client.isStarted()){
				client.stop();
			}
		}
	}
	
	public boolean requestHotSpotState(){
		
		return client.requestHotSpotState();
		
	}
	
}

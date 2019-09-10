/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取系统信息
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.cqsynet.swifi.AppConstants;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.File;
import java.util.List;

public class AppUtil {

    /**
     * 获取TelephonyManager
     * 
     * @param context
     * @return
     */
    public static TelephonyManager getTelephonyManager(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telMgr;
    }

    /**
     * 获取"渠道号"等在manifest内定义的meta值
     * 
     * @param context
     * @param key
     * @return
     */
    public static String getMetaData(Context context, String key) {
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Object value = ai.metaData.get(key);
            if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取网络连接类型
     * 
     * @param context
     * @return
     */
    public static String getNetWorkType(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String nt = "";
        if (telMgr.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {
            nt = "EDGE";
        } else if (telMgr.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS) {
            nt = "GPRS";
        } else if (telMgr.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS) {
            nt = "UMTS";
        } else if (telMgr.getNetworkType() == 4) {
            nt = "HSDPA";
        } else {
        }
        return nt;
    }

    /**
     * 获取IMSI
     * 
     * @param context
     * @return
     */
    public static String getIMSI(final Context context) {
        final TelephonyManager telMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        final String[] imsi = {"111111111111111"};
        AndPermission.with(context)
                .requestCode(100)
                .permission(Manifest.permission.READ_PHONE_STATE)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        if (requestCode == 100) {
                            if(telMgr.getSubscriberId() != null && telMgr.getSubscriberId().length() != 0) {
                                imsi[0] = telMgr.getSubscriberId();
                            }
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        ToastUtil.showToast(context, "读取设备号权限被拒绝");
                    }
                }).start();
        return imsi[0];
    }
    
    /**
     * 获取IMEI
     * 
     * @param context
     * @return
     */
    public static String getIMEI(final Context context) {
        final TelephonyManager telMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        final String[] imei = {"111111111111111"};
        AndPermission.with(context)
                .requestCode(100)
                .permission(Manifest.permission.READ_PHONE_STATE)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        if (requestCode == 100) {
                            if(telMgr.getDeviceId() != null && telMgr.getDeviceId().length() != 0) {
                                imei[0] = telMgr.getDeviceId();
                            }
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        ToastUtil.showToast(context, "读取设备号权限被拒绝");
                    }
                })
                .start();
        return imei[0];
    }

    /**
     * 获取运营商所在国家
     * 
     * @param context
     * @return
     */
    public static String getCountry(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String country = "";
        country = telMgr.getSimCountryIso();
        return country;
    }

    /**
     * 获取手机品牌
     * @return
     */
    public static String getPhoneModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获得VersionCode，返回值为版本号，例如：5
     * 
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "get versionCode Exception(RuntimeException)");
        }
    }

    /**
     * 获得versionName，返回值为版本名称，例如：1.3.1
     * 
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "get versionCode Exception(RuntimeException)");
        }
    }

    /**
     * 获取分辨率
     * @param context
     * @return
     */
    public static String getScreenResolution(Context context) {
    	String resolution;
    	DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    	((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    	int W = mDisplayMetrics.widthPixels;
    	int H = mDisplayMetrics.heightPixels;
    	resolution = Integer.toString(W) + "*" + Integer.toString(H);
    	return resolution;
    }
    
    /**
     * 获取屏幕宽度
     * @param activity
     * @return
     */
    public static int getScreenW(Activity activity) {
    	DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    	activity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    	int width = mDisplayMetrics.widthPixels;
    	return width;
    }
    
    /**
     * 获取屏幕高度
     * @param activity
     * @return
     */
    public static int getScreenH(Activity activity) {
    	DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    	activity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    	int height = mDisplayMetrics.heightPixels;
    	return height;
    }

    /**
     * 获取 uuid
     * 
     * @param context
     * @return
     */
    public static String getUserUUID(Context context) {
        if (context == null)
            return null;
        String uuid = context.getSharedPreferences(
                AppConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
                .getString(AppConstants.SHARED_PREF_KEY_UUID, "");
        if ("".equals(uuid)) {
            uuid = java.util.UUID.randomUUID().toString();
            context.getSharedPreferences(AppConstants.SHARED_PREF_FILE_NAME,
                    Context.MODE_PRIVATE).edit()
                    .putString(AppConstants.SHARED_PREF_KEY_UUID, uuid)
                    .commit();
        }
        return uuid;
    }

    /**
     * 获取手机Mac地址
     * @param ctx
     * @return Mac地址
     */
    public static String getMac(Context ctx) {
    	String mac = SharedPreferencesInfo.getTagString(ctx, SharedPreferencesInfo.MAC);
    	if(!TextUtils.isEmpty(mac)) {
    		return mac;
    	} else {
	        WifiManager manager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
	        WifiInfo info = manager.getConnectionInfo();
	        return info.getMacAddress();
    	}
    }

    /***************************** 根据ip地址获取mac地址*****************************************************
	public static String getLocalMacAddressFromIp(Context context) {
		String mac_s = "";
		try {
			byte[] mac;
			NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalIpAddress()));
			mac = ne.getHardwareAddress();
			mac_s = byte2hex(mac);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac_s;
	}    
    
	public static String getLocalIpAddress() {
		try {
			for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = (NetworkInterface) en.nextElement();
				for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());
		}
		return null;
	}

	public static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer(b.length);
		String stmp = "";
		int len = b.length;
		for (int n = 0; n < len; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1) {
				hs = hs.append("0").append(stmp);
			} else {
				hs = hs.append(stmp);
			}
		}
		return String.valueOf(hs);
	}
	*****************************根据ip获取mac地址******************************************/
    
    /**
     * 安装apk
     */
    public static void installApk(Context ctx, File apkfile) {
        if (apkfile != null && apkfile.exists()) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                    "application/vnd.android.package-archive");
            ctx.startActivity(i);
        }
    }
    
    /**
     * dp转px
     * @param context
     * @param dipValue
     * @return
     */
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    
    /**
     * 将px值转换为sp值，保证文字大小不变
     * 
     * @param pxValue
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */ 
    public static int px2sp(Context context, float pxValue) { 
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity; 
        return (int) (pxValue / fontScale + 0.5f); 
    } 
   
    /**
     * 将sp值转换为px值，保证文字大小不变
     * 
     * @param spValue
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */ 
    public static int sp2px(Context context, float spValue) { 
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity; 
        return (int) (spValue * fontScale + 0.5f); 
    }

    /**
     * 判断apk是否安装
     * @param context
     * @param packageName 包名
     * @return 
     */
	public static boolean isInstalled(Context context, String packageName, int verCode) {
		if (TextUtils.isEmpty(packageName)) {
			return false;
		}
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(packageName, 0);
//            List<PackageInfo> installedAppList =  manager.getInstalledPackages(0); //获取所有安装的app
//			if(verCode >= info.versionCode) {
				return true;
//			} else {
//				return false; //版本过旧,后期可修改为做升级操作
//			}
		} catch (NameNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * 判断是否连接的wifi网络
	 * @param context
	 * @return
	 */
	public static boolean isConnectWifi(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 判断是否安装微信
     * @param context
     * @return
     */
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param context
     * @param serviceName
     *            是包名+服务的类名（例如：com.beidian.test.service.BasicInfoService ）
     * @return
     */
    public static boolean isServiceWork(Context context, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList == null || myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    /**
     * 判断进程是否运行
     *
     * @return
     */
    public static boolean isProessRunning(Context context, String proessName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : lists) {
            if (info.processName.equals(proessName)) {
                isRunning = true;
            }
        }
        return isRunning;
    }

    /**
     * 底部虚拟按键栏的高度
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getSoftButtonsBarHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        //这个方法获取可能不是真实屏幕的高度
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        //获取当前屏幕的真实高度
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    /**
     * 判断定位服务是否开启
     *
     * @param
     * @return true 表示开启
     */
    public boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

}

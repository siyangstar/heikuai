package com.cqsynet.swifi.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.ShareObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class ShareUtil {

	/**
	 * 从服务器取图片
	 * 
	 * @param url
	 * @return
	 */
	public static Drawable getHttpDrawable(String url) {
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			myFileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
			conn.setConnectTimeout(0);
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BitmapDrawable(bitmap);
	}

	/** 将action转换为String */
	public static String actionToString(int action) {
		switch (action) {
		case Platform.ACTION_AUTHORIZING:
			return "ACTION_AUTHORIZING";
		case Platform.ACTION_GETTING_FRIEND_LIST:
			return "ACTION_GETTING_FRIEND_LIST";
		case Platform.ACTION_FOLLOWING_USER:
			return "ACTION_FOLLOWING_USER";
		case Platform.ACTION_SENDING_DIRECT_MESSAGE:
			return "ACTION_SENDING_DIRECT_MESSAGE";
		case Platform.ACTION_TIMELINE:
			return "ACTION_TIMELINE";
		case Platform.ACTION_USER_INFOR:
			return "ACTION_USER_INFOR";
		case Platform.ACTION_SHARE:
			return "ACTION_SHARE";
		default:
			return "UNKNOWN";
		}
	}

	/**
	 * 分享到具体平台的方法
	 */
	public static void share(final Context context, final ShareObject sObject) {
		saveStatistics(context, sObject);
		Platform platform = null;
		Platform.ShareParams sp = new Platform.ShareParams();
		if (TextUtils.isEmpty(sObject.getImageUrl())) {
			copyToCache(context);
			String filepath = Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/"
					+ "share_icon.png";
			File file = new File(filepath);
			if (file.exists()) {
				sp.setImagePath(filepath);
			}
		} else {
			sp.setImageUrl(sObject.getImageUrl());
		}
		sp.setShareType(Platform.SHARE_WEBPAGE);// 设置分享类型
        if (!sObject.getUrl().contains("?")) {
            sObject.setUrl(sObject.getUrl() + "?f=" + sObject.getTag()); //网址后面增加来源,用于统计
        } else {
            sObject.setUrl(sObject.getUrl() + "&f=" + sObject.getTag());
        }
        if (sObject.getTag().equals("Wechat")) {// 分享到微信
			platform = ShareSDK.getPlatform(Wechat.NAME);
			if (!platform.isClientValid()) {
				ToastUtil.showToast(context, "请先安装微信客户端");
				return;
			}
			sp.setTitle(sObject.getTitle());
			sp.setText(sObject.getText());
			sp.setUrl(sObject.getUrl());
			sp.setShareType(Platform.SHARE_WEBPAGE);
		} else if (sObject.getTag().equals("CircleFriend")) {// 分享到微信朋友圈
			platform = ShareSDK.getPlatform(WechatMoments.NAME);
			if (!platform.isClientValid()) {
				ToastUtil.showToast(context, "请先安装微信客户端");
				return;
			}
			sp.setTitle(sObject.getTitle());
			sp.setText(sObject.getText());
			sp.setUrl(sObject.getUrl());
		} else if (sObject.getTag().equals("QQ")) {// 分享到QQ
			platform = ShareSDK.getPlatform(QQ.NAME);
			if (!platform.isClientValid()) {
				ToastUtil.showToast(context, "请先安装QQ客户端");
				return;
			}
			sp.setTitle(sObject.getTitle());
			sp.setTitleUrl(sObject.getUrl());
			sp.setText(sObject.getText());
		} else if (sObject.getTag().equals("QZone")) {// 分享到QQ空间
			platform = ShareSDK.getPlatform(QZone.NAME);
			if (!platform.isClientValid()) {
				ToastUtil.showToast(context, "请先安装QQ客户端");
				return;
			}
			sp.setTitle(sObject.getTitle());
			sp.setTitleUrl(sObject.getUrl());
			sp.setText(sObject.getText());
			sp.setSite(sObject.getSite());
			sp.setSiteUrl(sObject.getSiteUrl());
		} else if (sObject.getTag().equals("SinaWeibo")) {// 分享到新浪微博
			platform = ShareSDK.getPlatform(SinaWeibo.NAME);
			if (!platform.isClientValid()) {
				ToastUtil.showToast(context, "请先安装新浪微博客户端");
				return;
			}
			sp.setText(sObject.getText() + sObject.getUrl());
		}
		platform.setPlatformActionListener(new PlatformActionListener() {
			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {
				// ToastUtil.showToast(context, "分享失败");
			}

			@Override
			public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
				// ToastUtil.showToast(context, "分享成功");
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				ToastUtil.showToast(context, "分享已取消");
			}
		});
		platform.share(sp);
	}

	/**
	 * 保存统计数据
	 * 
	 * @param context
	 * @param sObject
	 *            分享的实体类
	 */
	public static void saveStatistics(Context context, ShareObject sObject) {
		String id = sObject.getId();
		if (!TextUtils.isEmpty(id)) {
			StatisticsDao.saveStatistics(context, "share", id); // share的分享统计
		}
		String name = null;
		if (sObject.getTag().equals("QQ")) {
			name = "qq";
		} else if (sObject.getTag().equals("Wechat")) {
			name = "weixin";
		} else if (sObject.getTag().equals("CircleFriend")) {
			name = "circle";
		} else if (sObject.getTag().equals("QZone")) {
			name = "qzone";
		} else if (sObject.getTag().equals("SinaWeibo")) {
			name = "weibo";
		}
		StatisticsDao.saveStatistics(context, "shareClient", name); // 平台分享统计
	}

	/**
	 *
	 * 将drawable下的图片存储到sd卡
	 *
	 * @param context
	 */
	public static void copyToCache(Context context) {
		File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String filename = dir.getPath() + "/" + "share_icon.png";
		int resID = context.getResources().getIdentifier("share_icon", "drawable", context.getPackageName());
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resID);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filename);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

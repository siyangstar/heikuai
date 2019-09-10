/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：日志工具类
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.util;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import com.cqsynet.swifi.AppConstants;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

public class LogUtil {

    public static boolean mIsWriteToFile = false;

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
        if (mIsWriteToFile) {
            writeToFile(msg);
        }
    }
    
    public static void d(Context context, String tag, int StringId) {
    	String msg = context.getResources().getString(StringId);
        Log.d(tag, msg);
        if (mIsWriteToFile) {
            writeToFile(msg);
        }
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
        if (mIsWriteToFile) {
            writeToFile(msg);
        }
    }
    
    public static void i(Context context, String tag, int StringId) {
    	String msg = context.getResources().getString(StringId);
        Log.i(tag, msg);
        if (mIsWriteToFile) {
            writeToFile(msg);
        }
    }

    public static void e(Context context, String tag, String msg) {
        Log.e(tag, msg);
        if (mIsWriteToFile) {
            writeToFile(msg);
        }
    }
    
    public static void e(Context context, String tag, int StringId) {
    	String msg = context.getResources().getString(StringId);
        Log.e(tag, msg);
        if (mIsWriteToFile) {
            writeToFile(msg);
        }
    }

	/**
	 * 打印调试日志
	 */
	public static void debug(String tag, String msg) {
        Log.i(tag, msg);
		try {
			String curTime = DateFormat.format("MM-dd hh-mm-ss", new Date()).toString();
			String path = Environment.getExternalStorageDirectory().getPath();
			File saveFile = new File(path + "/" + AppConstants.CACHE_DIR + "/Debug2");
			if (!saveFile.exists()) {
				saveFile.createNewFile();
			}
			FileWriter debugWriter = new FileWriter(saveFile, true);
			debugWriter.write("[" + curTime + "]\n" + msg + "\n");
			debugWriter.write("@@@@@@@@@@\n");
			debugWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 写入文件保存
	public static void writeToFile(String msg) {
		try {
			String curTime = DateFormat.format("yyyy-MM-dd hh-mm-ss", new Date()).toString();
			String path = Environment.getExternalStorageDirectory().getPath();
			File saveFile = new File(path + "/" + AppConstants.CACHE_DIR + "/" + AppConstants.CACHE_FILE); //持久保存在本地
			File tempFile = new File(path + "/" + AppConstants.CACHE_DIR + "/" + AppConstants.CACHE_TEMP_FILE); //用于上传,每次上传后会清空
			if (!saveFile.exists()) {
				saveFile.createNewFile();
			}
			if (!tempFile.exists()) {
				tempFile.createNewFile();
			}
			FileWriter debugWriter = new FileWriter(saveFile, true);
			FileWriter tempWriter = new FileWriter(tempFile, true);
			debugWriter.write("[" + curTime + "]\n" + msg + "\n");
			tempWriter.write("[" + curTime + "]\n" + msg + "\n");
            debugWriter.write("@@@@@@@@@@\n");
            tempWriter.write("@@@@@@@@@@\n");
			debugWriter.close();
			tempWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	// 写入文件保存
	public static void writeToFile(Throwable e) {
		try {
			String curTime = DateFormat.format("yyyy-MM-dd hh-mm-ss", new Date()).toString();
			String path = Environment.getExternalStorageDirectory().getPath();
			File saveFile = new File(path + "/" + AppConstants.CACHE_DIR + "/" + AppConstants.CACHE_FILE); //持久保存在本地
			File tempFile = new File(path + "/" + AppConstants.CACHE_DIR + "/" + AppConstants.CACHE_TEMP_FILE); //用于上传,每次上传后会清空
			if (!saveFile.exists()) {
				saveFile.createNewFile();
			}
			if (!tempFile.exists()) {
				tempFile.createNewFile();
			}
			FileWriter debugWriter = new FileWriter(saveFile, true);
			FileWriter tempWriter = new FileWriter(tempFile, true);
            debugWriter.write("[" + curTime + "]\n");
            debugWriter.write(e + "\n");
            tempWriter.write("[" + curTime + "]\n");
            tempWriter.write(e + "\n");
            for(int i = 0; i < e.getStackTrace().length; i++) {
            	debugWriter.write(e.getStackTrace()[i] + "\n");
            	tempWriter.write(e.getStackTrace()[i] + "\n");
            }
            debugWriter.write("@@@@@@@@@@\n");
            tempWriter.write("@@@@@@@@@@\n");
			debugWriter.close();
			tempWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
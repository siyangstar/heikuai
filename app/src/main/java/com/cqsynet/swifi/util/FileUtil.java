/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：文件工具类
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {
    /**
     * 获得指定文件的byte数组
     */
    public static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
    
    /**
     * 从文件中读取文本内容
     * @param filePath
     * @return
     */
	public static String getString(String filePath) {
		String path = filePath;
		String content = ""; // 文件内容字符串
		File file = new File(path);
		// 如果path是传递过来的参数，可以做一个非目录的判断
		if (file.exists() && !file.isDirectory()) {
			try {
				InputStream instream = new FileInputStream(file);
				if (instream != null) {
					InputStreamReader inputreader = new InputStreamReader(instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					// 分行读取
					while ((line = buffreader.readLine()) != null) {
						content += line + "\n";
					}
					instream.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return content;
	}
	
	/**
	 * 删除文件
	 * @param filePath
	 */
	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		file.delete();
	}
}
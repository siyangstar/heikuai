/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：3DES加密
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi.util;

import android.util.Base64;

import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DESUtil {
	//加解密统一使用的编码方式
	private final static String encoding = "utf-8";
	//向量
	private static byte[] iv = {1,2,3,4,5,6,7,8}; 

	/**
	 * DES加密
	 * @param plainText 明文
	 * @param key 长度不能够小于8位字节
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String plainText, String key) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "DES");
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey,zeroIv);
        byte[] bytes = cipher.doFinal(plainText.getBytes());
    	return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

	/**
	 * DES解密
	 * @param encryptText 密文
	 * @param key 长度不能够小于8位字节
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String encryptText, String key) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "DES");
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey,zeroIv);
        byte[] bytes = cipher.doFinal(Base64.decode(encryptText, Base64.NO_WRAP));
    	return new String(bytes);
	}
	
	/**
	 * 生成8位随机字符串作为des密钥
	 * @return
	 */
	public static final String initDesKey() {
		Random randGen = new Random();
		char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
		char[] randBuffer = new char[8];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
		}
		return new String(randBuffer);
	}
}
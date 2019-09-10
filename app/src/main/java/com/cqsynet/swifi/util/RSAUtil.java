package com.cqsynet.swifi.util;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.util.Base64;

/***
 * RSA算法处理工具类
 * 
 * @author zhaosiyang 20141105
 *
 */
public class RSAUtil {
	public static final String KEY_ALGORITHM = "RSA";



	/** */
	/**
	 * 加密<br>
	 * 用公钥加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String data, String key) {
		try {
			byte[] keyBytes = Base64.decode(key, Base64.DEFAULT);

			// 取得公钥
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			Key publicKey = keyFactory.generatePublic(x509KeySpec);

			// 对数据加密
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return Base64.encodeToString(cipher.doFinal(data.getBytes()), Base64.NO_WRAP);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 16进制转字节数组
	 * @param b
	 * @return
	 */
	public static byte[] hex2byte(byte[] b) {
		if ((b.length % 2) != 0)
			throw new IllegalArgumentException("长度不是偶数");
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	/**
	 * 字节数组转16进制
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}

}
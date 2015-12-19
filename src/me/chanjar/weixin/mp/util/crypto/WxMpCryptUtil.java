/**
 * 对公众平台发送给公众账号的消息加解密示例代码.
 *
 * @copyright Copyright (c) 1998-2014 Tencent Inc.
 */

// ------------------------------------------------------------------------

/**
 * 针对org.apache.commons.codec.binary.Base64，
 * 需要导入架包commons-codec-1.9（或commons-codec-1.8等其他版本）
 * 官方下载地址：http://commons.apache.org/proper/commons-codec/download_codec.cgi
 */
package me.chanjar.weixin.mp.util.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

import me.chanjar.weixin.common.bean.WxJsapiUnifiedOrder;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

public class WxMpCryptUtil extends me.chanjar.weixin.common.util.crypto.WxCryptUtil {

	/**
	 * 构造函数
	 * 
	 * @param wxMpConfigStorage
	 */
	public WxMpCryptUtil(WxMpConfigStorage wxMpConfigStorage) {
		/*
		 * @param token 公众平台上，开发者设置的token
		 * 
		 * @param encodingAesKey 公众平台上，开发者设置的EncodingAESKey
		 * 
		 * @param appId 公众平台appid
		 */
		String encodingAesKey = wxMpConfigStorage.getAesKey();
		String token = wxMpConfigStorage.getToken();
		String appId = wxMpConfigStorage.getAppId();

		this.token = token;
		this.appidOrCorpid = appId;
		this.aesKey = Base64.decodeBase64(encodingAesKey + "=");
	}

	public static void addJsapiPaySign(WxJsapiUnifiedOrder unifiedOrder,String key) {
		String[] filedValus = unifiedOrder.toString().split(",");
		unifiedOrder.setSign(genWithAmple(filedValus,key));
	}

	public static String genWithAmple(Map<String,String> params,String key){
		StringBuffer sb = new StringBuffer();
		for (String  paramKey : params.keySet()) {
			sb.append(paramKey + "=" + params.get(paramKey) + ",");
		}
		String result = sb.toString();
		result = result.substring(0, result.length()-1);
		return genWithAmple(result.split(","),key);
	}
	
	/**
	 * 用&串接arr参数，生成MD5 digest
	 * 
	 * @param arr
	 * @return
	 */
	private static String genWithAmple(String[] arr,String key){
		Arrays.sort(arr);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			String a = arr[i];
			sb.append(a);
			if (i != arr.length - 1) {
				sb.append('&');
			}
		}
		if(StringUtils.isNotEmpty(key)){
			sb.append("&key=");
			sb.append(key);
		}
		return genMD5Str(sb.toString()).toUpperCase();
	}

	private static String genMD5Str(String str) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			byte []bytes=md5.digest(str.getBytes("utf8"));
			StringBuilder ret=new StringBuilder(bytes.length<<1);
			for(int i=0;i<bytes.length;i++){
			  ret.append(Character.forDigit((bytes[i]>>4)&0xf,16));
			  ret.append(Character.forDigit(bytes[i]&0xf,16));
			}
			return ret.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}

package com.track.mytools.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

public class DesUtil {
//	public static void main(String args[]){
//		DesUtil td = new DesUtil();
//		td.desDecrypt(td.desEncrypt("12345"));
//	}
	/**
	 * 加密
	 */
	public static String desEncrypt(Context context, String srcData){
		String mK;
		String baseK;
		String key;
		String desdata = "";

		//SerialNumber
		String SerialNumber = android.os.Build.SERIAL;

		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

		@SuppressLint("MissingPermission")
		String number = telephonyManager.getLine1Number();

		//Log.i("DesUtil_Log","SerialNumber:"+SerialNumber);
		//Log.i("DesUtil_Log","number:"+number);

		try {
			//主机密钥解密
			mK = DESKey.desDecrypt("c26666070451fe81649b6dc5cd2f5aba9943f252a133a04d","1231231212abc324");
			//基础密钥解密
			baseK = DESKey.desDecrypt("db600454dce39e6c62505ea20bf330ea28a725058990155d",mK);
			//运行密钥解密
			key = DESKey.desDecrypt("d3f4420605cc205ceae1384be15ad8caf8efa0b3a6bef7a3", baseK);

			//密钥加密
			desdata = DESKey.desEncrypt(srcData, key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		return desdata;
	}
	/**
	 * 解密
	 */
	public static String desDecrypt(Context context, String srcData){
		String mK;
		String baseK;
		String key;
		String desdata = "";

		//SerialNumber
		String SerialNumber = android.os.Build.SERIAL;

		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

		@SuppressLint("MissingPermission")
		String number = telephonyManager.getLine1Number();

		//Log.i("DesUtil_Log","SerialNumber:"+SerialNumber);
		//Log.i("DesUtil_Log","number:"+number);

		try {
			//主机密钥解密
			mK = DESKey.desDecrypt("c26666070451fe81649b6dc5cd2f5aba9943f252a133a04d","1231231212abc324");
			//基础密钥解密
			baseK = DESKey.desDecrypt("db600454dce39e6c62505ea20bf330ea28a725058990155d",mK);
			//运行密钥解密
			key = DESKey.desDecrypt("d3f4420605cc205ceae1384be15ad8caf8efa0b3a6bef7a3", baseK);

			//密钥加密
			desdata = DESKey.desDecrypt(srcData, key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		return desdata;
	}
}

package com.track.mytools.util;

public class DesUtil {
//	public static void main(String args[]){
//		DesUtil td = new DesUtil();
//		td.desDecrypt(td.desEncrypt("12345"));
//	}
	/**
	 * 加密
	 */
	public static String desEncrypt(String srcData){
		String mK;
		String baseK;
		String key;
		String desdata = "";
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
			e.printStackTrace();
		}

		return desdata;
	}
	/**
	 * 解密
	 */
	public static String desDecrypt(String srcData){
		String mK;
		String baseK;
		String key;
		String desdata = "";
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
			e.printStackTrace();
		}

		return desdata;
	}
}

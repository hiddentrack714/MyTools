package com.track.mytools.util;

import android.annotation.SuppressLint;
import android.telephony.TelephonyManager;

import com.track.mytools.activity.PwdActivity;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {
    public static final String VIPARA = "1269571569321021";
    public static final String bm = "utf-8";



    /**
     * 字节数组转化为大写16进制字符串
     *
     * @param b
     * @return
     */
    private static String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String s = Integer.toHexString(b[i] & 0xFF);
            if (s.length() == 1) {
                sb.append("0");
            }

            sb.append(s.toUpperCase());
        }

        return sb.toString();
    }

    /**
     * 16进制字符串转字节数组
     *
     * @param s
     * @return
     */
    private static byte[] str2ByteArray(String s) {
        int byteArrayLength = s.length() / 2;
        byte[] b = new byte[byteArrayLength];
        for (int i = 0; i < byteArrayLength; i++) {
            byte b0 = (byte) Integer.valueOf(s.substring(i * 2, i * 2 + 2), 16)
                    .intValue();
            b[i] = b0;
        }

        return b;
    }


    /**
     * AES 加密
     *
     * @param content
     *            明文
     * @return
     */

    public static String aesEncrypt(String content) {

        //SerialNumber
        String SerialNumber = android.os.Build.SERIAL;

        TelephonyManager telephonyManager = (TelephonyManager) PwdActivity.pwdActivity.getSystemService(PwdActivity.pwdActivity.TELEPHONY_SERVICE);

        @SuppressLint("MissingPermission")
        String number = telephonyManager.getLine1Number();

        //Log.i("DesUtil_Log","SerialNumber:"+SerialNumber+number);

        String password = addSpace(SerialNumber + number);

        content = content == null || "null".equalsIgnoreCase(content) || "".equals(content) ? "" : content;

        try {
            IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
            byte[] encryptedData = cipher.doFinal(content.getBytes(bm));

            return Base64.encode(encryptedData);
//			return byte2HexStr(encryptedData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    /**
     * AES 解密
     *
     * @param content
     *            密文
     * @return
     */

    public static String aesDecrypt(String content) {

        //SerialNumber
        String SerialNumber = android.os.Build.SERIAL;

        TelephonyManager telephonyManager = (TelephonyManager) PwdActivity.pwdActivity.getSystemService(PwdActivity.pwdActivity.TELEPHONY_SERVICE);

        @SuppressLint("MissingPermission")
        String number = telephonyManager.getLine1Number();

        //Log.i("DesUtil_Log","SerialNumber:"+SerialNumber+number);

        String password = addSpace(SerialNumber + number);

        content = content == null || "null".equalsIgnoreCase(content) || "".equals(content) ? "" : content;

        try {
            byte[] byteMi = Base64.decode(content);
//			byte[] byteMi=	str2ByteArray(content);
            IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            byte[] decryptedData = cipher.doFinal(byteMi);
            return new String(decryptedData, "utf-8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static String addSpace(String str){
        if(str.length()<=16){
            int x = 16-str.length();
            for(int i = 0;i<x;i++){
                str = str + "";
            }
        }else{
            str = str.substring(0,16);
        }
        return str;
    }

//    public static void main(String[] args) throws Exception {
//        String content = "123456";
//        System.out.println("加密前：" + content);
//        String encrypt = aesEncrypt(content);
//        System.out.println("加密后：" + encrypt);
//        String decrypt = aesDecrypt(encrypt);
//        System.out.println("解密后：" + decrypt);
//    }

}

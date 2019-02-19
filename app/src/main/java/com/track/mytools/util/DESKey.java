package com.track.mytools.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DESKey
{

  private SecretKeySpec keySpec = null;

  private SecretKey key = null;

  public String creatDES()
          throws Exception
  {

    SecureRandom sr = new SecureRandom();

    KeyGenerator kg = KeyGenerator.getInstance("DES");

    kg.init(sr);

    SecretKey sk = kg.generateKey();

    byte[] ptext = sk.getEncoded();

    SecretKeySpec desSpec = new SecretKeySpec(ptext, "DES");

    this.key = sk;

    this.keySpec = desSpec;

    return KeyUtil.byteArr2HexStr(ptext);
  }

  public byte[] getSpec()
  {
    byte[] ptext = this.keySpec.getEncoded();

    return ptext;
  }

  public byte[] getKey()
  {
    byte[] ptext = this.key.getEncoded();

    return ptext;
  }

  public static String desEncrypt(String srcData, String dESKey)
          throws Exception
  {
    SecureRandom sr = new SecureRandom();

    byte[] rawKeyData = KeyUtil.hexstr2ByteArr(dESKey);

    DESKeySpec dks = new DESKeySpec(rawKeyData);

    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

    SecretKey key = keyFactory.generateSecret(dks);

    Cipher cipher = Cipher.getInstance("DES");

    cipher.init(1, key, sr);

    byte[] data = srcData.getBytes("UTF8");

    byte[] encryptedData = cipher.doFinal(data);

    String enOut = KeyUtil.byteArr2HexStr(encryptedData);

    return enOut;
  }

  public static String desDecrypt(String srcData, String dESKey)
          throws Exception
  {
    SecureRandom sr = new SecureRandom();

    byte[] rawKeyData = KeyUtil.hexstr2ByteArr(dESKey);

    DESKeySpec dks = new DESKeySpec(rawKeyData);

    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

    SecretKey key = keyFactory.generateSecret(dks);

    Cipher cipher = Cipher.getInstance("DES");

    cipher.init(2, key, sr);

    byte[] data = KeyUtil.hexstr2ByteArr(srcData);

    byte[] decryptedData = cipher.doFinal(data);

    String out = new String(decryptedData, "UTF8");

    return out;
  }

  public static byte[] desEncrypt(byte[] scrData, byte[] dESKey)
          throws Exception
  {
    SecureRandom sr = new SecureRandom();

    DESKeySpec dks = new DESKeySpec(dESKey);

    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

    SecretKey key = keyFactory.generateSecret(dks);

    Cipher cipher = Cipher.getInstance("DES");

    cipher.init(1, key, sr);

    byte[] data = scrData;

    byte[] encryptedData = cipher.doFinal(data);

    return encryptedData;
  }

  public static byte[] desDecrypt(byte[] scrData, byte[] dESKey)
          throws Exception
  {
    SecureRandom sr = new SecureRandom();

    DESKeySpec dks = new DESKeySpec(dESKey);

    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

    SecretKey key = keyFactory.generateSecret(dks);

    Cipher cipher = Cipher.getInstance("DES");

    cipher.init(2, key, sr);

    byte[] decryptedData = cipher.doFinal(scrData);

    return decryptedData;
  }

  public static String desEncryptData(String srcData, String dESKey)
          throws Exception
  {
    SecureRandom sr = new SecureRandom();

    byte[] rawKeyData = KeyUtil.hexstr2ByteArr(dESKey);

    DESKeySpec dks = new DESKeySpec(rawKeyData);

    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

    SecretKey key = keyFactory.generateSecret(dks);

    Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");

    cipher.init(1, key, sr);

    byte[] data = srcData.getBytes("UTF8");

    byte[] encryptedData = cipher.doFinal(data);

    String enOut = KeyUtil.byteArr2HexStr(encryptedData);

    return enOut;
  }

  public static String desDecryptData(String srcData, String dESKey)
          throws Exception
  {
    SecureRandom sr = new SecureRandom();

    byte[] rawKeyData = KeyUtil.hexstr2ByteArr(dESKey);

    DESKeySpec dks = new DESKeySpec(rawKeyData);

    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

    SecretKey key = keyFactory.generateSecret(dks);

    Cipher cipher = Cipher.getInstance("DES/CFB8/NoPadding");

    cipher.init(2, key, sr);

    byte[] data = srcData.getBytes();

    byte[] decryptedData = cipher.doFinal(data);

    String out = new String(decryptedData, "UTF8");

    return out;
  }
}
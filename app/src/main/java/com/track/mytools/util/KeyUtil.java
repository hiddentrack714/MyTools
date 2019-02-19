package com.track.mytools.util;

import java.io.IOException;

public class KeyUtil
{
  public static String byteArr2HexStr(byte[] arrB)
    throws IOException
  {
    int iLen = arrB.length;

    StringBuffer sb = new StringBuffer(iLen * 2);

    for (int i = 0; i < iLen; ++i)
    {
      int intTmp = arrB[i];

      while (intTmp < 0)
      {
        intTmp += 256;
      }

      if (intTmp < 16)
      {
        sb.append("0");
      }

      sb.append(Integer.toString(intTmp, 16));
    }

    return sb.toString();
  }

  public static byte[] hexstr2ByteArr(String strIn)
  {
    byte[] arrB = strIn.getBytes();

    int iLen = arrB.length;

    byte[] arrOut = new byte[iLen / 2];

    for (int i = 0; i < iLen; i += 2)
    {
      String strTmp = new String(arrB, i, 2);

      arrOut[(i / 2)] = (byte)Integer.parseInt(strTmp, 16);
    }

    return arrOut;
  }
}
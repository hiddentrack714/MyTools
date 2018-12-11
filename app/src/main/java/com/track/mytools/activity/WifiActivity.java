package com.track.mytools.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ListView;

import com.track.mytools.R;
import com.track.mytools.adapter.WifiMainAdapter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * 展示WIFI密码
 */
public class WifiActivity extends Activity {

    private static int i = -1;
    private static HashMap<String ,String> map;//密码集合
    private static List<HashMap<String, String>> l;//密码键值对
    private static List<HashMap<String, String>> tempL;//临时密码键值对

    private static WifiMainAdapter wma;

    private ListView lv;

    private static WifiActivity ha;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ha = this;
        setContentView(R.layout.activity_wifi);

        try {
            Process process = null;
            l = new ArrayList<HashMap<String, String>>();
            tempL = new ArrayList<HashMap<String, String>>();
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;
            process = Runtime.getRuntime().exec("su");
            dataOutputStream =new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream.writeBytes("cat /data/misc/wifi/wpa_supplicant.conf\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream, "UTF-8");
            BufferedReader r = new BufferedReader(inputStreamReader);
            String str;
            while ((str = r.readLine()) != null) {
                if ("network={".equals(str)) {
                    i = 0;
                    map = new HashMap<String, String>(); //有效wifi密码
                } else if ("}".equals(str)) {
                    if(null!=map.get("ssid") && null!=map.get("passwrd")){
                        //Log.i("WifiActivity_LOG1","->" +map.get("ssid")+":"+map.get("passwrd")+"<-");
                        l.add(map);
                    }else{
                        tempL.add(map);
                    }
                    i = -1;
                } else if (i >= 0) {
                    if (str.indexOf("ssid=\"") > -1) {
                        str = str.trim().substring(str.indexOf("ssid=\\\"") + 7, str.lastIndexOf("\"") - 1);
                        map.put("ssid", str);
                        //Log.i("ssid",str);
                    } else if (str.indexOf("psk") > -1) {
                        str = str.trim().substring(str.indexOf("ssid=\\\"") + 6, str.lastIndexOf("\"") - 1);
                        map.put("passwrd", str);
                        //Log.i("passwrd",str);
                    }
                    i++;
                }
            }
            r.close();
        }catch(Exception e){
            Log.e("WifiActivity_LOG1",e.getMessage());
        }

        lv = (ListView)findViewById(R.id.wifiList);

        HashMap<String, String> tempMap = new HashMap<String, String>(); //有效和无效密码的分割符
        tempMap.put("ssid","----------------------------");
        tempMap.put("passwrd","---------------无效密码------------------------------------------------");

        l.add(tempMap);

        l.addAll(tempL);

        wma = new WifiMainAdapter(ha,l);

        lv.setAdapter(wma);

        Log.i("WifiActivity_LOG2",l.toString());

        super.onCreate(savedInstanceState);
    }
}

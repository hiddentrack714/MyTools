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

    //listview内容模板
    private static String from [] = {"wifiSSid","wifiPssword"};
    private static int to [] = {R.id.wifiSSid,R.id.wifiPassword};

    private static int i = -1;
    private static HashMap<String ,String> map;//密码集合
    private static List<HashMap<String, String>> l;//密码键值对

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
                    map = new HashMap<String, String>();
                } else if ("}".equals(str)) {
                    l.add(map);
                    i = -1;
                } else if (i >= 0) {
                    if (str.indexOf("ssid=\"") > -1) {
                        str = str.substring(str.indexOf("ssid=\\\"") + 8, str.lastIndexOf("\""));
                        map.put("ssid", str);
                        //Log.i("ssid",str);
                    } else if (str.indexOf("psk") > -1) {
                        str = str.substring(str.indexOf("ssid=\\\"") + 7, str.lastIndexOf("\""));
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

        wma = new WifiMainAdapter(ha,l);

        lv.setAdapter(wma);

        Log.i("WifiActivity_LOG2",l.toString());

        super.onCreate(savedInstanceState);
    }
}

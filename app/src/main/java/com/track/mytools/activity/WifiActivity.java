package com.track.mytools.activity;

import android.app.Activity;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.track.mytools.R;
import com.track.mytools.adapter.WifiMainAdapter;
import com.track.mytools.util.ToolsUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * 展示WIFI密码
 */
public class WifiActivity extends Activity {

    @BindView(R.id.wifiList)
    ListView wifiList;

    private static int i = -1;
    private static HashMap<String ,String> map;//密码集合
    private static List<HashMap<String, String>> l;//密码键值对
    private static List<HashMap<String, String>> tempL;//临时密码键值对

    private static WifiMainAdapter wifiMainAdapter;

    public ClipboardManager cm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);

        l =  getWifigroup();

        wifiMainAdapter = new WifiMainAdapter(this,l);
        wifiList.setAdapter(wifiMainAdapter);

        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiMainAdapter.ViewHolder holder = (WifiMainAdapter.ViewHolder) view.getTag();
                String vhPassword =  holder.vhPassword.getText().toString();
                Log.i("WifiActivity_Log",vhPassword);

                cm.setText(vhPassword);
                ToolsUtil.showToast(WifiActivity.this,"密码已复制到剪切板",1000);
            }
        });
    }

    /**
     * 获取Wifi列表
     * @return
     */
    public static List<HashMap<String, String>> getWifigroup(){
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
            Log.e("WifiActivity_Log",e.getMessage());
        }

        l.addAll(tempL);

        return l;
    }
}

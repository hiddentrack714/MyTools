package com.track.mytools.activity;

import android.app.Activity;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.track.mytools.R;
import com.track.mytools.adapter.WifiMainAdapter;
import com.track.mytools.util.ToolsUtil;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private static HashMap<String, String> map;//密码集合
    private static List<HashMap<String, String>> l;//密码键值对
    private static List<HashMap<String, String>> tempL;//临时密码键值对

    private static WifiMainAdapter wifiMainAdapter;

    public ClipboardManager cm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);

        //由于安卓8.0之后WiFi密码位置该便，需要判断当前设备的版本号是否大于8.0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            l = getWifigroupO();
        } else {
            l = getWifigroup();
        }

        //l = getWifigroup();

        wifiMainAdapter = new WifiMainAdapter(this, l);
        wifiList.setAdapter(wifiMainAdapter);

        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiMainAdapter.ViewHolder holder = (WifiMainAdapter.ViewHolder) view.getTag();
                String vhPassword = holder.vhPassword.getText().toString();
                Log.i("WifiActivity_Log", vhPassword);

                cm.setText(vhPassword);
                ToolsUtil.showToast(WifiActivity.this, "密码已复制到剪切板", 1000);
            }
        });
    }

    /**
     * 获取Wifi列表
     *
     * @return
     */
    public static List<HashMap<String, String>> getWifigroup() {
        try {
            Process process = null;
            l = new ArrayList<HashMap<String, String>>();
            tempL = new ArrayList<HashMap<String, String>>();
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
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
                    if (null != map.get("ssid") && null != map.get("passwrd")) {
                        //Log.i("WifiActivity_LOG1","->" +map.get("ssid")+":"+map.get("passwrd")+"<-");
                        l.add(map);
                    } else {
                        tempL.add(map);
                    }
                    i = -1;
                } else if (i >= 0) {
                    str = str.trim();
                    if (str.indexOf("ssid=") == 0) {
                        //wifi名称有可能是中文名
                        if (str.indexOf("ssid=\"") == 0) {
                            //不带中文
                            str = str.substring(str.indexOf("ssid=\\\"") + 7, str.lastIndexOf("\""));
                        } else {
                            //带中文，中文被保存为16进制
                            str = str.substring(5, str.length());

                            byte[] b = new byte[str.length() / 2];

                            for (int i = 0; i < str.length() / 2; i++) {
                                String finallyStr = str.substring(i * 2, i * 2 + 2);
                                b[i] = (byte) Integer.parseInt(finallyStr, 16);
                            }

                            str = new String(b, "utf-8");
                        }

                        map.put("ssid", str);
                        Log.i("WifiActivity_Log", str);
                    } else if (str.indexOf("psk") > -1) {
                        str = str.trim().substring(str.indexOf("ssid=\\\"") + 6, str.lastIndexOf("\""));
                        map.put("passwrd", str);
                        //Log.i("passwrd",str);
                    }
                    i++;
                }
            }
            r.close();
        } catch (Exception e) {
            Log.e("WifiActivity_Log", e.getMessage());
        }

        l.addAll(tempL);

        return l;
    }

    /**
     * 获取安卓8.0以上的
     *
     * @return
     */
    public static List<HashMap<String, String>> getWifigroupO() {
        //File file = new File("F://WifiConfigStore.xml");
        SAXReader saxReader = new SAXReader();
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        try {

            Process process = null;
            l = new ArrayList<HashMap<String, String>>();
            tempL = new ArrayList<HashMap<String, String>>();
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream.writeBytes("cat /data/misc/wifi/WifiConfigStore.xml\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream, "UTF-8");

            Document document = saxReader.read(inputStreamReader);
            Element employees = document.getRootElement();
            for (Iterator i = employees.elementIterator(); i.hasNext(); ) {
                Element employee = (Element) i.next();
                for (Iterator j = employee.elementIterator(); j.hasNext(); ) { // 遍例节点
                    Element node = (Element) j.next();
                    for (Iterator x = node.elementIterator(); x.hasNext(); ) { // 遍例节点
                        Element nodes = (Element) x.next();

                        if ("WifiConfiguration".equals(nodes.getName())) {
                            HashMap<String, String> map = null;
                            for (Iterator y = nodes.elementIterator(); y.hasNext(); ) { // 遍例节点
                                Element nodess = (Element) y.next();
                                if ("ConfigKey".equals(nodess.attributeValue("name"))) {
                                    map = new HashMap<String, String>();
                                    map.put("ssid", nodess.getText().substring(nodess.getText().indexOf("\"") + 1, nodess.getText().lastIndexOf("\"")));
                                } else if ("PreSharedKey".equals(nodess.attributeValue("name"))) {
                                    if (nodess.getText() != null && !"".equals(nodess.getText())) {
                                        map.put("passwrd", nodess.getText().substring(nodess.getText().indexOf("\"") + 1, nodess.getText().lastIndexOf("\"")));
                                    } else {
                                        map.put("passwrd", nodess.getText());
                                    }
                                    list.add(map);
                                }
                            }
                        }
                    }
                }

            }
        } catch (DocumentException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            Log.e("WifiActivity_Log", e.getMessage());
        }

        return list;
    }

}

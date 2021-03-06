package com.track.mytools.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.track.mytools.R;
import com.track.mytools.adapter.LanMainAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jcifs.netbios.NbtAddress;

/**
 *
 * 展示局域网设备
 *
 */

public class LanActivity extends Activity {

    @BindView(R.id.scanBtn)
    Button scanBtn; //扫描按键

    @BindView(R.id.lanPro)
    ProgressBar lanPro; //扫描按键

    @BindView(R.id.lanList)
    ListView lanList;

    private static LanMainAdapter lanMainAdapter;

    private static LanActivity lanActivity;

    private static Handler lanActivityHandler;

    private static List<HashMap<String, String>> l;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan);
        ButterKnife.bind(this);

        lanActivity = this;

        //扫描按键监听
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LanActivity.l!=null &&  LanActivity.lanMainAdapter!=null){
                    LanActivity.l.clear();
                    LanActivity.lanMainAdapter.notifyDataSetChanged();
                }
                lanPro.setVisibility(View.VISIBLE);
                scanBtn.setEnabled(false);
                startScan();
            }
        });

        lanActivityHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                lanPro.setVisibility(View.GONE);
                scanBtn.setEnabled(true);
                lanList.setAdapter(lanMainAdapter);
            }
        };
    }

    /**
     * 获取局域网中的 存在的ip地址及对应的mac
     */
    public void startScan() {
        //局域网内存在的ip集合
        final List<String> ipList = new ArrayList<>();
        final Map<String, String> map = new HashMap<>();

        //获取本机所在的局域网地址
        String hostIP = getHostIP();
        int lastIndexOf = hostIP.lastIndexOf(".");
        final String substring = hostIP.substring(0, lastIndexOf + 1);
        //创建线程池
        //        final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket dp = new DatagramPacket(new byte[0], 0, 0);
                DatagramSocket socket;
                try {
                    socket = new DatagramSocket();
                    int position = 2;
                    while (position < 255) {
                        Log.e("LanActivity_Log", "run: udp-" + substring + position);
                        dp.setAddress(InetAddress.getByName(substring + String.valueOf(position)));
                        socket.send(dp);
                        position++;
                        if (position == 125) {//分两段掉包，一次性发的话，达到236左右，会耗时3秒左右再往下发
                            socket.close();
                            socket = new DatagramSocket();
                        }
                    }
                    socket.close();
                    execCatForArp(hostIP);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取本机 ip地址
     *
     * @return
     */
    private String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("LanActivity_Log", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }

    /**
     * 获取本机Mac
     * @param context
     * @return
     */
    public String getLocalMacAddressFromIp(Context context) {
        String mac_s = "";
        try {
            byte[] mac;
            NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(getHostIP()));
            mac = ne.getHardwareAddress();
            mac_s = byte2hex(mac);
            if(mac_s.length() == 12){
                String finalMac = "";
                for(int i = 0 ;i<6;i++){
                    finalMac += mac_s.substring(i*2,i*2+2);
                    if(i<5) {
                        finalMac+=":";
                    }
                }
                mac_s = finalMac;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mac_s;
    }

    public String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        int len = b.length;
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1) {
                hs = hs.append("0").append(stmp);
            } else {
                hs = hs.append(stmp);
            }
        }

        return String.valueOf(hs);
    }

    /**
     * 执行 cat命令 查找android 设备arp表
     * arp表 包含ip地址和对应的mac地址
     */
    private void execCatForArp(String hostIP) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    l = new ArrayList<HashMap<String, String>>();
                    Process exec = Runtime.getRuntime().exec("cat proc/net/arp");
                    InputStream is = exec.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.contains("00:00:00:00:00:00")&&!line.contains("IP")) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            String[] split = line.split("\\s+");

                            //路由器
                            if("192.168.1.1".equals(split[0]) || "192.168.0.1".equals(split[0])){

                                map.put("name","Router");//主机名称

                            }else if(hostIP.equals(split[0])){
                                map.put("name","Local");//本机名称
                            }else{

                                NbtAddress nbtAddress = NbtAddress.getByName(split[0]);

                                nbtAddress.firstCalledName();

                                String name = nbtAddress.nextCalledName();

                                map.put("name",name!=null?name:"未知");//主机名称
                            }

                            map.put("ip",split[0]);//IP

                            map.put("mac",split[3]);//Mac

                            l.add(map);
                        }
                    }

                    //添加本机IP和Mac
                    HashMap<String, String> map = new HashMap<String, String>();

                    map.put("name","Local");//本机名称

                    map.put("ip",hostIP);//IP

                    map.put("mac",getLocalMacAddressFromIp(lanActivity));//Mac

                    l.add(map);

                    lanMainAdapter = new LanMainAdapter(lanActivity,l);

                    Message message = LanActivity.lanActivityHandler.obtainMessage();

                    message.obj = lanMainAdapter;

                    LanActivity.lanActivityHandler.sendMessage(message);

                    Log.i("LanActivity_Log",l.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

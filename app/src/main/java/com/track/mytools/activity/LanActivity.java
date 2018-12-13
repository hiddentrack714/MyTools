package com.track.mytools.activity;


import android.app.Activity;
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

import jcifs.netbios.NbtAddress;

/**
 *
 * 展示局域网设备
 *
 */

public class LanActivity extends Activity {

    private Button scanBtn; //扫描按键
    private ProgressBar lanPro; //扫描按键

    private static LanMainAdapter lma;

    private static LanActivity la;

    private ListView lv;

    private static Handler handler;

    private static List<HashMap<String, String>> l;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        la = this;

        setContentView(R.layout.activity_lan);

        scanBtn = (Button)findViewById(R.id.scanBtn);
        lanPro = (ProgressBar) findViewById(R.id.lanPro);

        lv = (ListView)findViewById(R.id.lanList);

        //扫描按键监听
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LanActivity.l!=null &&  LanActivity.lma!=null){
                    LanActivity.l.clear();
                    LanActivity.lma.notifyDataSetChanged();
                }
                lanPro.setVisibility(View.VISIBLE);
                scanBtn.setEnabled(false);
                startScan();
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                lanPro.setVisibility(View.GONE);
                scanBtn.setEnabled(true);
                lv.setAdapter(lma);
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
                        Log.e("kalshen", "run: udp-" + substring + position);
                        dp.setAddress(InetAddress.getByName(substring + String.valueOf(position)));
                        socket.send(dp);
                        position++;
                        if (position == 125) {//分两段掉包，一次性发的话，达到236左右，会耗时3秒左右再往下发
                            socket.close();
                            socket = new DatagramSocket();
                        }
                    }
                    socket.close();
                    execCatForArp();
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
            Log.i("kalshen", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }

    /**
     * 执行 cat命令 查找android 设备arp表
     * arp表 包含ip地址和对应的mac地址
     */
    private void execCatForArp() {
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

                    lma = new LanMainAdapter(la,l);

                    Message message = LanActivity.handler.obtainMessage();

                    message.obj = lma;

                    LanActivity.handler.sendMessage(message);

                    Log.i("LanActivity",l.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

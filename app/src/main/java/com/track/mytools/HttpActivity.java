package com.track.mytools;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpActivity extends Activity{
    /**
     * http多线程下载
     * @param savedInstanceState
     */

    private Button httpDownBtn;//下载按钮

    private EditText httpUrl;//下载链接
    private EditText httpThread;//线程数量
    private EditText httpDir;//下载地址

    private ProgressBar httpPro; //加载动画

    private static int THREAD_NUM;
    private static String URL;
    private static String DIR_NAME;

    private static HttpActivity ha;

    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);

        httpDownBtn = (Button)findViewById(R.id.httpDownBtn);

        httpUrl = (EditText)findViewById(R.id.httpUrl);
        httpThread = (EditText)findViewById(R.id.httpThread);
        httpDir = (EditText)findViewById(R.id.httpDir);

        httpPro = (ProgressBar)findViewById(R.id.httpPro);

        ha = this;

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                if(arg0.arg1 == 1){
                    httpPro.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });

        httpDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                httpPro.setVisibility(View.VISIBLE);

                Log.i("httpUrl",httpUrl.getText().toString());
                Log.i("httpThread",httpThread.getText().toString());
                Log.i("httpDir",httpDir.getText().toString());

                URL = httpUrl.getText().toString();
                THREAD_NUM = Integer.parseInt(httpThread.getText().toString());
                DIR_NAME = httpDir.getText().toString();

                URL = URL.substring(0,URL.lastIndexOf("/")+1);
                String flag = URL;

                //循环初始化多个线程
                List<String> list = new ArrayList<String>();
                ConcurrentHashMap<Integer,Boolean> map = new ConcurrentHashMap<Integer,Boolean>();
                for(int i=1 ;i<THREAD_NUM+1;i++) {
                    String temp = "";
                    if(i<10) {
                        temp = "0" + i;
                    }else {
                        temp = i+"";
                    }
                    flag = flag + temp + ".jpg";
                    //System.out.println(flag);
                    list.add(flag);
                    flag = URL;
                    map.put(i-1, true);
                }

                ExecutorService es = Executors.newCachedThreadPool();

                for(int i=0;i<THREAD_NUM;i++) {
                    es.submit(new MyThread(list.get(i),i,map));
                }

                //下载检测
                new Thread(()->{
                    boolean t = true;
                    while(t) {
                        int i = 0;
                        for(Map.Entry<Integer,Boolean> entry:map.entrySet()) {
                            if(entry.getValue()==false) {
                                i++;
                            }
                        }

                        Log.i("http","下载进度检查");

                        if(i == THREAD_NUM) {
                            t = false;
                            Message msg = HttpActivity.handler.obtainMessage();
                            msg.arg1 = 1;
                            HttpActivity.handler.sendMessage(msg);
                            Log.i("http","下载完成");
                            ToolsUntil.showToast(HttpActivity.ha,"下载完成",2000);
                            es.shutdown();
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } ) .start();
            }
        });
    }


    static class MyThread extends Thread{

        private String url;
        private int index;
        private ConcurrentHashMap<Integer, Boolean> map;

        public MyThread(String url,int index,ConcurrentHashMap<Integer, Boolean> map) {
            this.url = url;
            this.index = index;
            this.map = map;
        }

        @Override
        public void run() {
            try {
                boolean isCon = true;
                while(isCon) {
                    isCon = ToolsUntil.down(DIR_NAME,this.url);
                    map.put(index, isCon);
                    //每个线程均+5继续
                    this.url = plusNum(this.url);
                    //Log.i("httpName",this.url);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * +5返回
         * @param str
         * @return
         */
        public String plusNum(String str) {
            String temp = str.substring(str.lastIndexOf("/")+1,str.lastIndexOf("."));
            int i = Integer.parseInt(temp) + THREAD_NUM;
            String font = str.substring(0,str.lastIndexOf("/")+1);
            String back = str.substring(str.lastIndexOf("."),str.length());

            String num = "";
            if(i<10) {
                num = "0"+i;
            }else {
                num = i+"";
            }

            return font+num+back;
        }

    }
}

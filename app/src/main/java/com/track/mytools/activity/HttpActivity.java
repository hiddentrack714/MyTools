package com.track.mytools.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.track.mytools.R;
import com.track.mytools.adapter.HttpMainAdapter;
import com.track.mytools.entity.HttpThreadEntity;
import com.track.mytools.until.ToolsUntil;

import java.io.IOException;
import java.io.InputStream;
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
    private Button httpCopyBtn;//黏贴按钮

    private EditText httpUrl;//下载链接
    private EditText httpThread;//线程数量
    private EditText httpDir;//下载地址

    private SeekBar httpSeek;//线程数量拉条

    private Switch httpSwitch; // 单一下载选项

    private static int THREAD_NUM;
    private static String URL;
    private static String DIR_NAME;

    private static HttpActivity ha;

    public static Handler handler;

    private static ListActivity la;

    private static HttpMainAdapter hma;

    private static ConcurrentHashMap<String,Object> chm = new ConcurrentHashMap<String,Object>(); //listview专用map

    private static List<ConcurrentHashMap<String,Object>> l = new ArrayList<ConcurrentHashMap<String,Object>>();

    private static Boolean isSingle = false; // 是否采用单文件下载标识，默认为不采用

    private ListView lv;

    //listview内容模板
    private static String from [] = {"httpDownNo","httpDownPro","httpDownSize","httpDownName"};
    private static int to [] = {R.id.httpDownNo,R.id.httpDownPro,R.id.httpDownSize,R.id.httpDownName};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);

        lv = (ListView)findViewById(R.id.httpList);

        httpDownBtn = (Button)findViewById(R.id.httpDownBtn);
        httpCopyBtn = (Button)findViewById(R.id.httpCopyBtn);

        httpUrl = (EditText)findViewById(R.id.httpUrl);
        httpThread = (EditText)findViewById(R.id.httpThread);
        httpDir = (EditText)findViewById(R.id.httpDir);

        httpSwitch = (Switch)findViewById(R.id.httpSwitch);

        httpSeek = (SeekBar)findViewById(R.id.httpSeek);

        ha = this;

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                //下载结束后处理
                if(arg0.arg1 == 1){
                    httpDownBtn.setEnabled(true);
                    httpUrl.setEnabled(true);
                    httpThread.setEnabled(true);
                    httpDir.setEnabled(true);
                    httpCopyBtn.setEnabled(true);

                    ConcurrentHashMap<String,Object> chm = new ConcurrentHashMap<String,Object>();

                    l.add(chm);

                    hma = new HttpMainAdapter(ha,l);

                    lv.setAdapter(hma);

                    hma.notifyDataSetChanged();
                }

                //单一线程下载开始之前，对线程视图的初始化
                if(arg0.arg2 == 1){
                    HttpThreadEntity hte = (HttpThreadEntity)arg0.obj;

                    View view = HttpMainAdapter.viewList.get(hte.getFileIndex());

                    HttpMainAdapter.ViewHolder holder = (HttpMainAdapter.ViewHolder)view.getTag();

                    holder.tvSize.setText( hte.getFileSize());

                    holder.tvName.setText(hte.getFileName());
                }
                return false;
            }
        });

        //下载按钮
        httpDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("httpUrl",httpUrl.getText().toString());
                Log.i("httpThread",httpThread.getText().toString());
                Log.i("httpDir",httpDir.getText().toString());

                Log.i("httpSwitch",isSingle+"");

                if("0".equals(httpThread.getText().toString())){
                    ToolsUntil.showToast(HttpActivity.ha,"下载线程数不能为0",3000);
                    return;
                }

                if("".equals(httpUrl.getText().toString())){
                    ToolsUntil.showToast(HttpActivity.ha,"下载链接不能为空",3000);
                    return;
                }

                URL = httpUrl.getText().toString();
                THREAD_NUM = Integer.parseInt(httpThread.getText().toString());
                DIR_NAME = httpDir.getText().toString();

                URL = URL.substring(0,URL.lastIndexOf("/") + 1);
                String flag = URL;

                //启动单一文件下载，只启动一个线程即可
                boolean threadSigle = true;
                if(isSingle){
                    THREAD_NUM = 1;
                    threadSigle = false;
                }

                //开始下载，固定当前链接和按钮
                httpDownBtn.setEnabled(false);
                httpUrl.setEnabled(false);
                httpThread.setEnabled(false);
                httpDir.setEnabled(false);
                httpCopyBtn.setEnabled(false);

                //循环初始化多个线程
                List<String> list = new ArrayList<String>();
                ConcurrentHashMap<Integer,Boolean> map = new ConcurrentHashMap<Integer,Boolean>();  // 线程下载状态
                for(int i=1 ;i<THREAD_NUM+1;i++) {
                    ConcurrentHashMap<String,Object> listMap = new ConcurrentHashMap<String,Object>();              //线程属性
                    String temp = "";
                    if(i<10) {
                        temp = "0" + i;
                    }else {
                        temp = i+"";
                    }
                    flag = flag + temp + ".jpg";

                    list.add(flag);
                    flag = URL;
                    map.put(i-1, true); // 每个线程的初始状态都是true

                    //初始化线程属性状态
                    listMap.put("httpDownNo",i);
                    listMap.put("httpDownPro",0);
                    listMap.put("httpDownSize","");
                    listMap.put("httpDownName","");
                    l.add(listMap);
                }

                hma = new HttpMainAdapter(ha,l);

                lv.setAdapter(hma);

                Log.i("size",l.size()+"");

                ExecutorService es = Executors.newCachedThreadPool();

                //开启线程池
                for(int i=0;i<THREAD_NUM;i++) {
                    es.submit(new MyThread(list.get(i),i,map,threadSigle));
                }

                //下载进度检测
                new Thread(()->{
                    boolean t = true;
                    while(t) {
                        int i = 0;
                        for(Map.Entry<Integer,Boolean> entry:map.entrySet()) {
                            if(entry.getValue()==false) {
                                i++;
                            }
                        }

                        //Log.i("http","下载进度检查");

                        if(i == THREAD_NUM) {
                            t = false;
                            Message msg = HttpActivity.handler.obtainMessage();
                            msg.arg1 = 1;
                            HttpActivity.handler.sendMessage(msg);
                            Log.i("http","下载完成");

                            //下载完成，清空所有静态参数
                            HttpMainAdapter.viewList.clear();

                            ToolsUntil.showToast(HttpActivity.ha,"下载完成",3000);

                            es.shutdown();
                        }

                    }
                } ).start();

            }
        });

        //黏贴剪贴板内容
        httpCopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从剪贴板获得内容
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData data = cm.getPrimaryClip();
                if(data != null){
                    ClipData.Item item = data.getItemAt(0);
                    String content = item.getText().toString();
                    Log.i("CP",content);
                    //覆盖之前的链接
                    httpUrl.setText(content);
                }else{
                    ToolsUntil.showToast(HttpActivity.ha,"剪贴板暂无内容",3000);
                }

            }
        });

        //线程数量拉条监听器
        httpSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("SEEK",progress+"");
                httpThread.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //单一选项选择器听监听
        httpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //开启
                    isSingle = true;
                }else {
                   //关闭
                    isSingle = false;
                }

            }
        });

    }

    /**
     * 多线程下载类
     * 在多线程下载类中只负责初始化listview，不负责更新
     */
    static class MyThread extends Thread{

        private String url; //下载链接
        private int index; // 线程编号从0开始
        private ConcurrentHashMap<Integer, Boolean> map; //线程同步标记
        private boolean isSingle;

        public MyThread(String url,int index,ConcurrentHashMap<Integer, Boolean> map,boolean isSingle) {
            this.url = url;
            this.index = index;
            this.map = map;
            this.isSingle = isSingle;
        }

        @Override
        public void run() {
            try {
                boolean isCon = true;

                while(isCon) {

                    HashMap<String,Object> downMap = new HashMap<String,Object>();

                    downMap = ToolsUntil.down(this.url);

                    //获得文件大小
                    int fileSize = Integer.parseInt(downMap.get("fileSize").toString());

                    if(fileSize == 0){
                        //无下载文件
                        isCon = false;
                    }else{
                        //有下载文件
                        InputStream inputStream = (InputStream)downMap.get("fileStream");

                        //视图已经在界面初始化渲染完成，可以更新
                        View view = HttpMainAdapter.viewList.get(index);

                        HttpMainAdapter.ViewHolder holder = (HttpMainAdapter.ViewHolder)view.getTag();

                        holder.pb.setMax(fileSize);
                        //BUG设置TEXT完成后，卡住，没办法继续，开线程更新
                        //holder.tvName.setText("111");

                        Message msg = HttpActivity.handler.obtainMessage();

                        HttpThreadEntity hte = new HttpThreadEntity();

                        hte.setFileIndex(index);
                        double fileSizeDouble = Double.parseDouble(fileSize+"");
                        hte.setFileSize(ToolsUntil.takePointTwo((fileSizeDouble / 1024)+""));
                        hte.setFileName(this.url.substring(this.url.lastIndexOf("/") + 1));

                        msg.arg2 = 1;
                        msg.obj = hte;

                        HttpActivity.handler.sendMessage(msg);

                        //下载当前文件
                        Log.i("DOWN",this.url);
                        ToolsUntil.saveFile(inputStream,DIR_NAME,this.url,holder.pb);
                        //对当前线程链接++
                        this.url = plusNum(this.url);

                    }

                    map.put(index, isCon);

                    if(!isSingle){
                        isCon = false;
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * 当前线程增加
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

    static class ViewThread extends Thread{

        private TextView tv;
        private String content;

        public ViewThread(TextView tv,String content){
            this.tv = tv;
            this.content = content;
        }

        @Override
        public void run() {
            Message msg = HttpActivity.handler.obtainMessage();
            msg.arg2 = 1;
        }
    }

}

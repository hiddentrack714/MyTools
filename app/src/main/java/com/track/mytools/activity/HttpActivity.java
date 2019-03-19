package com.track.mytools.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;

import com.track.mytools.R;
import com.track.mytools.adapter.HttpMainAdapter;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.HttpEntity;
import com.track.mytools.entity.HttpThreadEntity;
import com.track.mytools.exception.HttpException;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

/**
 * http多线程下载
 *
 */
public class HttpActivity extends BaseKeyboardActivity{

    @BindView(R.id.httpDownBtn)
    Button httpDownBtn;//下载按钮

    @BindView(R.id.httpCopyBtn)
    Button httpCopyBtn;//黏贴按钮

    @BindView(R.id.httpUpdBtn)
    Button httpUpdBtn; //修改按钮

    @BindView(R.id.httpUrl)
    EditText httpUrl;//下载链接

    @BindView(R.id.httpThread)
    EditText httpThread;//线程数量

    @BindView(R.id.httpPath)
    EditText httpPath;//下载地址

    @BindView(R.id.httpDir)
    EditText httpDir;//下载地址

    @BindView(R.id.httpSuff)
    EditText httpSuff;//下载文件后缀

    @BindView(R.id.httpSeek)
    SeekBar httpSeek;//线程数量拉条

    @BindView(R.id.httpSwitch)
    Switch httpSwitch; // 单一下载选项

    @BindView(R.id.httpList)
    ListView lv;

    private static int THREAD_NUM;
    private static String URL;
    private static String DIR_NAME;

    private static HttpActivity httpActivity;

    public static Handler httpActivityHandler;

    private static HttpMainAdapter httpMainAdapter;

    private static ConcurrentHashMap<String,Object> chm = new ConcurrentHashMap<String,Object>(); //listview专用map

    private static List<ConcurrentHashMap<String,Object>> l = new ArrayList<ConcurrentHashMap<String,Object>>();

    private Boolean isSingle = false; // 是否采用单文件下载标识，默认为不采用

    private boolean isUpd = false;

    private final int EX_FILE_PICKER_RESULT = 0xfa01;
    private String startDirectory = null;// 记忆上一次访问的文件目录路径

    private static boolean isUseDownbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);
        ButterKnife.bind(this);

        BaseKeyboardActivity.rooID = R.id.http;
        attachKeyboardListeners();

        httpActivity = this;

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> map = ToolsDao.qryTable(sdb,HttpEntity.class,HttpActivity.this).get(0);

        httpThread.setText(map.get("httpThread").toString());
        httpPath.setText(map.get("httpPath").toString());
        httpSuff.setText(map.get("httpSuff").toString());
        httpSeek.setProgress(Integer.parseInt(map.get("httpThread").toString()));
        httpSeek.setEnabled(false);

        //线程视图复制更新
        httpActivityHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                //下载结束后处理
                if(arg0.arg1 == 1){
                    httpDownBtn.setEnabled(true);
                    httpUrl.setEnabled(true);
                    httpUpdBtn.setEnabled(true);
                    httpPath.setEnabled(true);
                    httpCopyBtn.setEnabled(true);
                    httpSwitch.setEnabled(true);
                    httpDir.setEnabled(true);

                    //清空Listview视图
                    l.clear();
                    httpMainAdapter.notifyDataSetChanged();
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

                Log.i("HttpActivity_Log",httpUrl.getText().toString());
                Log.i("HttpActivity_Log",httpThread.getText().toString());
                Log.i("HttpActivity_Log",httpPath.getText().toString());

                Log.i("HttpActivity_Log",isSingle+"");

                if("".equals(httpUrl.getText().toString())){
                    ToolsUtil.showToast(HttpActivity.this,"下载链接不能为空",2000);
                    return;
                }

                if("0".equals(httpThread.getText().toString())){
                    ToolsUtil.showToast(HttpActivity.this,"下载线程数不能为0",2000);
                    return;
                }

                if("".equals(httpDir.getText().toString())){
                    ToolsUtil.showToast(HttpActivity.this,"下载目录不能为空",2000);
                    return;
                }

                if(isUseDownbtn == false ){
                    ToolsUtil.showToast(HttpActivity.this,"请先关闭键盘，再下载",2000);
                    return;
                }


                URL = httpUrl.getText().toString();   //http连接
                THREAD_NUM = Integer.parseInt(httpThread.getText().toString());//线程数量
                DIR_NAME = httpPath.getText().toString() +  httpDir.getText().toString();//下载地址

                File file = new File(DIR_NAME);

                //要下载的文件目录存在同名文件,如果是目录的直接覆盖里面的内容
                if(file.exists()){
                    AlertDialog.Builder normalDialog =
                            new AlertDialog.Builder(HttpActivity.this);
                    //normalDialog.setIcon(R.drawable.icon_dialog);
                    normalDialog.setTitle("选择").setMessage("本地存在同名文件或文件夹，请选择?");

                    normalDialog.setPositiveButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // ...To-do
                                }
                            });

                    normalDialog.setNeutralButton("删除本地文件",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(file.delete()){
                                        ToolsUtil.showToast(HttpActivity.this,file.getName() + " 删除成功，继续下载",1000);
                                        dialogRes();
                                    } else{
                                        ToolsUtil.showToast(HttpActivity.this,file.getName() + " 删除失败",1000);
                                    }
                                }
                            });

                    normalDialog.setNegativeButton("修改本地文件名", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(file.renameTo(new File(DIR_NAME+"[1]"))){
                                ToolsUtil.showToast(HttpActivity.this,file.getName() + " 修改成功，继续下载",1000);
                                dialogRes();
                            } else{
                                ToolsUtil.showToast(HttpActivity.this,file.getName() + " 修改失败",1000);
                            }
                        }
                    });
                    // 创建实例并显示
                    normalDialog.show();
                }else{
                    dialogRes();
                }

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
                    Log.i("HttpActivity_Log",content);
                    //覆盖之前的链接
                    httpUrl.setText(content);
                }else{
                    ToolsUtil.showToast(HttpActivity.this,"剪贴板暂无内容",3000);
                }

            }
        });

        //线程数量拉条监听器
        httpSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("HttpActivity_Log",progress+"");
                httpThread.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //监听线程数量
        httpThread.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int a = Integer.parseInt(httpThread.getText().toString());
                    httpSeek.setProgress(a);
                }catch (Exception e){
                    ToolsUtil.showToast(HttpActivity.this,"请输入介于1~20之间的数字",3000);
                }
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

        //修改按钮监听
        httpUpdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isUpd == false){

                    httpThread.setEnabled(true);
                    httpSuff.setEnabled(true);
                    httpSeek.setEnabled(true);
                    httpPath.setEnabled(true);

                    isUpd = true;

                    httpUpdBtn.setText("完成");
                    httpDownBtn.setEnabled(false);
                }else{

                    httpThread.setEnabled(false);
                    httpSuff.setEnabled(false);
                    httpSeek.setEnabled(false);
                    httpPath.setEnabled(false);

                    HashMap<String,Object> dataMap = new HashMap<String,Object>();

                    dataMap.put("httpThread",httpThread.getText().toString());
                    dataMap.put("httpPath",httpPath.getText().toString());
                    dataMap.put("httpSuff",httpSuff.getText().toString());
                    dataMap.put("id",map.get("id"));

                    SQLiteDatabase sdb = ToolsDao.getDatabase();

                    ToolsDao.saveOrUpdIgnoreExsit(sdb,dataMap,HttpEntity.class);

                    isUpd = false;

                    httpUpdBtn.setText("修改参数");
                    httpDownBtn.setEnabled(true);
                }
            }
        });

        httpPath.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    ExFilePicker exFilePicker = new ExFilePicker();
                    exFilePicker.setCanChooseOnlyOneItem(true);// 单选
                    exFilePicker.setQuitButtonEnabled(true);
                    exFilePicker.setChoiceType(ExFilePicker.ChoiceType.DIRECTORIES);

                    if (TextUtils.isEmpty(startDirectory)) {
                        exFilePicker.setStartDirectory(Environment.getExternalStorageDirectory().getPath());
                    } else {
                        exFilePicker.setStartDirectory(startDirectory);
                    }

                    exFilePicker.start(HttpActivity.this, EX_FILE_PICKER_RESULT);
                }
                return false;
            }
        });

    }

    /**
     * 下载流程初始化
     */
    private void dialogRes(){
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
        httpUpdBtn.setEnabled(false);
        httpPath.setEnabled(false);
        httpCopyBtn.setEnabled(false);
        httpSwitch.setEnabled(false);
        httpDir.setEnabled(false);

        //文件后缀名
        String fileSuff = httpSuff.getText() + "";

        //循环初始化多个线程
        List<String> list = new ArrayList<String>();
        ConcurrentHashMap<Integer,Boolean> map = new ConcurrentHashMap<Integer,Boolean>();  // 线程下载状态
        for(int i = 1 ;i < THREAD_NUM + 1 ;i++) {
            ConcurrentHashMap<String,Object> listMap = new ConcurrentHashMap<String,Object>();              //线程属性
            String temp = "";
            if(i<10) {
                temp = "0" + i;
            }else {
                temp = i+"";
            }

            //URL+文件+后缀
            if(isSingle){
                flag = httpUrl.getText().toString();
            }else{
                flag = flag + temp + "." + fileSuff;
            }

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

        httpMainAdapter = new HttpMainAdapter(HttpActivity.this,l);

        lv.setAdapter(httpMainAdapter);

        Log.i("HttpActivity_Log",l.size()+"");

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
                    Message msg = HttpActivity.httpActivityHandler.obtainMessage();
                    msg.arg1 = 1;
                    HttpActivity.httpActivityHandler.sendMessage(msg);
                    Log.i("HttpActivity_Log","下载完成");

                    //下载完成，清空所有静态参数
                    HttpMainAdapter.viewList.clear();

                    ToolsUtil.showToast(HttpActivity.this,"下载完成",3000);

                    es.shutdown();
                }

            }
        } ).start();
    }

    /**
     * 多线程下载类
     * 在多线程下载类中只负责初始化listview，不负责更新
     * 添加该线程的自我销毁操作，超过xx秒还未下载完成，停止下载，转到下一个背书循环
     * 将当前未下载完成的文件，标记到界面显示
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

                    downMap = ToolsUtil.down(this.url);

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

                        Message msg = HttpActivity.httpActivityHandler.obtainMessage();

                        long startTime = System.currentTimeMillis();//文件起始下载时间

                        HttpThreadEntity hte = new HttpThreadEntity();

                        hte.setFileIndex(index);
                        hte.setFileSize(fileSize + "");
                        hte.setFileName(this.url.substring(this.url.lastIndexOf("/") + 1));
                        hte.setFileStartTime(startTime);

                        msg.arg2 = 1;
                        msg.obj = hte;

                        HttpActivity.httpActivityHandler.sendMessage(msg);

                        //下载当前文件
                        try{
                            ToolsUtil.saveFile(inputStream,DIR_NAME,this.url,holder.pb,hte);
                        }catch(HttpException e){
                            ToolsUtil.showToast(HttpActivity.httpActivity,DIR_NAME+"下载失败",3000);
                            //String httpFail =  httpFailName.getText();
                            Message msg1 = HttpActivity.httpActivityHandler.obtainMessage();
                            msg1.arg1 = 2;
                            msg1.obj = DIR_NAME;
                            HttpActivity.httpActivityHandler.sendMessage(msg1);
                        }

                        //对当前线程链接++
                        this.url = plusNum(this.url);
                    }

                    map.put(index, isCon);

                    if(!isSingle){
                        isCon = false;
                        map.put(index, false);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EX_FILE_PICKER_RESULT) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                String path = result.getPath();

                List<String> names = result.getNames();
                for (int i = 0; i < names.size(); i++) {
                    File f = new File(path, names.get(i));
                    try {
                        Uri uri = Uri.fromFile(f); //这里获取了真实可用的文件资源

                        httpPath.setText(uri.getPath() + "/");
                        startDirectory = path;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onShowKeyboard(int keyboardHeight) {
        // do things when keyboard is shown
        Log.i("PwdActivity_Log","显示键盘");
        //httpDownBtn.setEnabled(false);
        isUseDownbtn = false;

    }

    @Override
    protected void onHideKeyboard() {
        // do things when keyboard is hidden
        Log.i("PwdActivity_Log","隐藏键盘");
        //httpDownBtn.setEnabled(true);
        isUseDownbtn = true;
    }

}

package com.track.mytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;

import com.track.mytools.R;
import com.track.mytools.adapter.AppMainAdapter;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.AppExtractEntity;
import com.track.mytools.service.AppExtractService;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * APP提取
 */
public class AppExtractActivity extends Activity {

    private ListView appList;
    private EditText appPath;
    private Button appUpdBtn;
    private Button extractBtn;
    private Switch appSwitch;

    private boolean isUpd = false;

    private static AppMainAdapter ama;

    private ListView lv;

    private static ArrayList<HashMap<String,Object>> systemAppList;
    private static ArrayList<HashMap<String,Object>> normalAppList;

    private static ArrayList<HashMap<String,Object>> tempList;

    public static List<HashMap<String,Object>> finallyList;
    public static String appPathStr;

    public static AppExtractActivity aea;

    public static Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appextract);
        appList = (ListView)findViewById(R.id.appList);
        appPath = (EditText)findViewById(R.id.appPath);
        appUpdBtn = (Button)findViewById(R.id.appUpdBtn);
        extractBtn = (Button)findViewById(R.id.extractBtn);
        appSwitch = (Switch)findViewById(R.id.appSwitch);

        aea = this;

        lv = (ListView)findViewById(R.id.appList);

        //查询app保存位置
        SQLiteDatabase sqd = ToolsDao.getDatabase();
        HashMap<String,Object> appMap  = ToolsDao.qryTable(sqd, AppExtractEntity.class,AppExtractActivity.this).get(0);
        appPath.setText(appMap.get("appPath").toString());

        //获取app应用列表
        PackageManager pm = this.getPackageManager();
        List<ApplicationInfo> appInfos= pm.getInstalledApplications(0);

        systemAppList = new ArrayList<HashMap<String,Object>>();//系统app
        normalAppList = new ArrayList<HashMap<String,Object>>();//普通app

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                extractBtn.setEnabled(true);
                appUpdBtn.setEnabled(true);
            }
        };

        //重新整理app列表，
        int i = 0;
        for(ApplicationInfo appInfo:appInfos){
            HashMap<String,Object> tempMap = new HashMap<String,Object>();
            try{
                PackageInfo packageInfo = pm.getPackageInfo(appInfo.packageName,0);

                String appName = appInfo.loadLabel(pm).toString();              //应用名称
                String appPackageName = appInfo.packageName;                    //应用包名
                String appVersionNamee = packageInfo.versionName;               //应用版本
                int appVersionCode = packageInfo.versionCode;                   //应用小版本
                Drawable appIcon = packageInfo.applicationInfo.loadIcon(pm);    //应用图标
                String appDir = appInfo.sourceDir;

                tempMap.put("appName",appName);
                tempMap.put("appPackageName",appPackageName);
                tempMap.put("appVersionName",appVersionNamee);
                tempMap.put("appVersionCode",appVersionCode);
                tempMap.put("appIcon",appIcon);
                tempMap.put("appSize",countFileSize(appInfo.sourceDir)); // 以MB为单位
                tempMap.put("isCheck",false); // 默认为不选中
                tempMap.put("appDir",appDir); // 默认为不选中

                //判断是否是系统应用，
                int flags = appInfo.flags;
                if((flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
                    //Log.d("app  ","  是系统应用  ");
                    systemAppList.add(tempMap);
                }else{
                    //Log.d("app  ","  不是系统应用  ");
                    normalAppList.add(tempMap);
                }
                i++;
            }catch(Exception e){
                e.getStackTrace();
               // Log.e("AppExtractActivity",e.getMessage());
            }
        }

        tempList = (ArrayList<HashMap<String,Object>>)normalAppList.clone();

        ama = new AppMainAdapter(this,tempList);

        lv.setAdapter(ama);

        //监听修改按钮
        appUpdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpd) {
                    appPath.setEnabled(false);
                    isUpd = false;
                    extractBtn.setEnabled(true);
                    appUpdBtn.setText("修改");

                    HashMap<String,Object> m = new HashMap<String,Object>();
                    SQLiteDatabase sqd = ToolsDao.getDatabase();
                    m.put("appPath",appPath.getText().toString());
                    m.put("id",appMap.get("id"));
                    ToolsDao.saveOrUpdIgnoreExsit(sqd,m,AppExtractEntity.class);
                } else {
                    appPath.setEnabled(true);
                    isUpd = true;
                    extractBtn.setEnabled(false);
                    appUpdBtn.setText("完成");
                }
            }
        });

        //监听提取按钮
        extractBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finallyList = new ArrayList<HashMap<String,Object>>();
                for(HashMap<String,Object> map : tempList){
                    if(((boolean)map.get("isCheck")) == true){
                        finallyList.add(map);
                    }
                }

                Log.i("AppExtractActivity",finallyList.toString());

                if(finallyList.size() == 0){
                    ToolsUtil.showToast(AppExtractActivity.this,"还没有选中要提取的APP",2000);
                }else{
                    ToolsUtil.showToast(AppExtractActivity.this,"开始复制["+finallyList.size()+"]款应用",500);

                    appPathStr = appPath.getText().toString();
                    File file = new File(appPathStr);
                    if(!file.exists() || !file.isDirectory()){
                        file.mkdirs();
                    }

                    extractBtn.setEnabled(false);
                    appUpdBtn.setEnabled(false);

                    Intent intentService = new Intent(AppExtractActivity.this, AppExtractService.class);

                    startService(intentService);
                }
            }
        });

        //监听Listview
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppMainAdapter.ViewHolder holder = (AppMainAdapter.ViewHolder) view.getTag();
                if((boolean)normalAppList.get(position).get("isCheck") == true){
                    holder.appCB.setChecked(false);
                    normalAppList.get(position).put("isCheck",false);
                }else{
                    holder.appCB.setChecked(true);
                    normalAppList.get(position).put("isCheck",true);
                }
            }
        });

        //监听Switch
        appSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //系统
                    Log.i("AppExtractActivity3","展示系统APP");
                    tempList.clear();
                    tempList.addAll(systemAppList);
                    ama.notifyDataSetChanged();
                }else {
                    //普通
                    Log.i("AppExtractActivity4","展示普通APP");

                    tempList.clear();
                    tempList.addAll(normalAppList);
                    ama.notifyDataSetChanged();
                }

            }
        });
    }

    /**
     * 计算app大小
     * @param filePath
     * @return  模板 xx.xx MB(xxxxxx字节)
     */
    private String countFileSize(String filePath) {
        File file = new File(filePath);
        String fileSize = "0";
        try {
            fileSize = ((Double.parseDouble(file.length() + "")) / 1024 / 1024) + "";
            //只保留小数点后两位
            String big = fileSize.split("\\.")[0];
            String little = fileSize.split("\\.")[1];
            if (little.length() > 2) {
                little = little.substring(0, 2);
            }
            fileSize = big + "." + little + "MB (";

            fileSize = fileSize+splitThree(file.length()+"")+" 字节)";

        } catch (Exception e) {
            Log.e("AppExtractActivity2", filePath + "-" + e.getMessage());
        }
        return fileSize;
    }

    /**
     * 将字节干3个单位分割
     * @param str
     * @return
     */
    private String splitThree(String str){
        int bigTime = str.length() / 3;
        int littleTime = str.length() % 3;
        String tempStr = "";

        for(int i = 0;i<bigTime+(littleTime>0?1:0);i++){
            if(littleTime > 0 && i == 0){
                tempStr = str.substring(0,littleTime);
                str = str.substring(littleTime,str.length());
            }else if(littleTime > 0){
                tempStr = tempStr + "," + str.substring((i-1)*3,(i-1)*3+3);
            }else {
                tempStr = str.substring(i*3,i*3+3)+","+tempStr;
            }
        }
        if(littleTime > 0) {
            tempStr = tempStr.substring(0,tempStr.length());
        }else {
            tempStr = tempStr.substring(0,tempStr.length()-1);
        }
        return tempStr;
    }
}

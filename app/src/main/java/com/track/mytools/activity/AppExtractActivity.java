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
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.Map;

/**
 * APP提取
 */
public class AppExtractActivity extends Activity {

    private ListView appList;
    private EditText appPath;
    private Button appUpdBtn;
    private Button extractBtn;
    private Switch appSwitch;
    private Switch sortSwitch;
    private EditText appSearch;

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
        sortSwitch = (Switch)findViewById(R.id.sortSwitch);
        appSearch = (EditText)findViewById(R.id.appSearch);

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

        //重新整理app列表
        for(ApplicationInfo appInfo:appInfos){
            HashMap<String,Object> tempMap = new HashMap<String,Object>();
            try{
                PackageInfo packageInfo = pm.getPackageInfo(appInfo.packageName,0);

                String appName = appInfo.loadLabel(pm).toString();              //应用名称
                String appPackageName = appInfo.packageName;                    //应用包名
                String appVersionNamee = packageInfo.versionName;               //应用版本
                int appVersionCode = packageInfo.versionCode;                   //应用小版本
                Drawable appIcon = packageInfo.applicationInfo.loadIcon(pm);    //应用图标
                String appDir = appInfo.sourceDir;                              //应用位置

                tempMap.put("appName",appName);
                tempMap.put("appPackageName",appPackageName);
                tempMap.put("appVersionName",appVersionNamee);
                tempMap.put("appVersionCode",appVersionCode);
                tempMap.put("appIcon",appIcon);
                tempMap.put("appSize",countFileSize(appInfo.sourceDir)); // 以MB为单位
                tempMap.put("appRealSize",new File(appInfo.sourceDir).length()); // 实际app大小
                tempMap.put("isCheck",false); // 默认为不选中
                tempMap.put("appDir",appDir);

                //判断是否是系统应用，
                int flags = appInfo.flags;
                if((flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
                    //Log.d("app  ","  是系统应用  ");
                    systemAppList.add(tempMap);
                }else{
                    //Log.d("app  ","  不是系统应用  ");
                    normalAppList.add(tempMap);
                }
            }catch(Exception e){
                e.getStackTrace();
               // Log.e("AppExtractActivity",e.getMessage());
            }
        }

        //默认显示普通应用
        try {
            tempList = (ArrayList<HashMap<String, Object>>) deepCopy(normalAppList);
        }catch(Exception e){
            Log.i("AppExtractActivity","深复制失败：" + e.getMessage());
        }

        //默认先按从小到大排序
        tempList = sortBySize(tempList,true);

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
                    ToolsUtil.showToast(AppExtractActivity.this,"还没有选中要提取的APP",1000);
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
                if((boolean)tempList.get(position).get("isCheck") == true){
                    holder.appCB.setChecked(false);
                    tempList.get(position).put("isCheck",false);
                }else{
                    holder.appCB.setChecked(true);
                    tempList.get(position).put("isCheck",true);
                }
            }
        });

        //监听筛选条件
        appSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(int i = 0;i<tempList.size();i++){
                    AppMainAdapter.isSelected.put(i,false);
                }
                ama.notifyDataSetChanged();
                tempList.clear();
                if (isChecked){
                    //系统
                    Log.i("AppExtractActivity3","展示系统APP");
                    if(!sortSwitch.isChecked()){
                        //小->大
                        tempList.addAll(sortBySize(deepCopy(systemAppList),true));
                    }else{
                        //大->小
                        tempList.addAll(sortBySize(deepCopy(systemAppList),false));
                    }
                }else {
                    //普通
                    Log.i("AppExtractActivity4","展示普通APP");

                    if(!sortSwitch.isChecked()){
                        //小->大
                        tempList.addAll(sortBySize(deepCopy(normalAppList),true));
                    }else{
                        //大->小
                        tempList.addAll(sortBySize(deepCopy(normalAppList),false));
                    }
                }
                ama.notifyDataSetChanged();
            }
        });

        //监听排序条件
        sortSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                for(int i = 0;i<tempList.size();i++){
                    AppMainAdapter.isSelected.put(i,false);
                }
                ama.notifyDataSetChanged();
                tempList.clear();
                if (isChecked){
                    //大->小
                    if(appSwitch.isChecked()){
                        tempList.addAll(sortBySize(deepCopy(systemAppList),false));
                    }else{
                        tempList.addAll(sortBySize(deepCopy(normalAppList),false));
                    }
                }else {
                    //小->大
                    if(appSwitch.isChecked()){
                        tempList.addAll(sortBySize(deepCopy(systemAppList),true));
                    }else{
                        tempList.addAll(sortBySize(deepCopy(normalAppList),true));
                    }
                }
                ama.notifyDataSetChanged();
            }
        });

        //监听搜索内容
        appSearch.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                for(int i = 0;i<tempList.size();i++){
                    AppMainAdapter.isSelected.put(i,false);
                }
                ama.notifyDataSetChanged();

                ArrayList<HashMap<String,Object>> ssTempList = null;
                if(appSwitch.isChecked()){
                    //搜索系统应用
                    ssTempList = systemAppList;
                }else{
                    //搜索普通应用
                    ssTempList = normalAppList;
                }

               ArrayList<HashMap<String,Object>> realTempList = new ArrayList<HashMap<String,Object>>();
               for(HashMap<String,Object> map : ssTempList){
                   if(map.get("appName").toString().toUpperCase().indexOf(s.toString().toUpperCase())>-1){
                       realTempList.add(map);
                   }
               }
                tempList.clear();
                tempList.addAll(sortBySize(deepCopy(realTempList),sortSwitch.isChecked()?false:true));
                ama.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

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
     * 将字节按3个单位分割
     * @param str
     * @return
     */
    private static String splitThree(String str){
        int bigTime = str.length() / 3;
        int littleTime = str.length() % 3;
        String tempStr = "";

        if(littleTime >0) {
            //有余数
            for(int i = 0 ;i<bigTime + 1;i++) {
                if(i==0) {
                    tempStr = str.substring(i, i+littleTime);
                }else {
                    tempStr = tempStr +","+ str.substring((i-1)*3+littleTime, (i-1)*3+3+littleTime);
                }
            }
        }else {
            //没有余数，直接截取
            for(int i = 0 ;i<bigTime;i++) {
                tempStr = tempStr+","+ str.substring(i*3, i*3+3);
            }
            tempStr = tempStr.substring(1,tempStr.length());
        }
        return tempStr;
    }

    /**
     * 按APP大小排序
     * @param tempList 待排序集合
     * @param sortMode 排序模式 true:从小到大；false:从大到小
     * @return
     */
    private ArrayList<HashMap<String,Object>> sortBySize(ArrayList<HashMap<String,Object>> tempList,boolean sortMode){
        //默认按从大到小排序一次
        for(int i = 0 ;i<tempList.size();i++) {
            for(int j = i + 1;j<tempList.size();j++) {
                if(sortMode){
                    if(Integer.parseInt(tempList.get(i).get("appRealSize").toString()) > Integer.parseInt(tempList.get(j).get("appRealSize").toString())){
                        HashMap<String,Object> temp = tempList.get(j);
                        tempList.set(j,tempList.get(i));
                        tempList.set(i, temp);
                    }
                }else{
                    if(Integer.parseInt(tempList.get(i).get("appRealSize").toString()) < Integer.parseInt(tempList.get(j).get("appRealSize").toString())){
                        HashMap<String,Object> temp = tempList.get(j);
                        tempList.set(j,tempList.get(i));
                        tempList.set(i, temp);
                    }
                }
            }
        }
        return tempList;
    }

    /**
     * 深复制
     * @param list yuan源集合
     * @return
     */
    public ArrayList<HashMap<String,Object>> deepCopy(ArrayList<HashMap<String,Object>> list){
        ArrayList<HashMap<String,Object>> deepList = new ArrayList<HashMap<String,Object>>(list.size());
        for(HashMap<String,Object> map : list){
            HashMap<String,Object> temp = new HashMap<String,Object>();
            for(Map.Entry<String,Object> entry:map.entrySet()){
                temp.put(entry.getKey(),entry.getValue());
            }
            deepList.add(temp);
        }
        return deepList;
    }

}

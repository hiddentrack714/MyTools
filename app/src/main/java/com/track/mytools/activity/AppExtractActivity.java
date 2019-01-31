package com.track.mytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.track.mytools.R;
import com.track.mytools.adapter.AppMainAdapter;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.AppExtractEntity;
import com.track.mytools.service.AppExtractCopyService;
import com.track.mytools.service.AppExtractLoadService;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * APP提取
 */
public class AppExtractActivity extends Activity {

    @BindView(R.id.appList)
    ListView appList;

    @BindView(R.id.appPath)
    EditText appPath;

    @BindView(R.id.appUpdBtn)
    Button appUpdBtn;

    @BindView(R.id.extractBtn)
    Button extractBtn;

    @BindView(R.id.appSwitch)
    Switch appSwitch;

    @BindView(R.id.sortSwitch)
    Switch sortSwitch;

    @BindView(R.id.appSearch)
    EditText appSearch;

    @BindView(R.id.appPro)
    ProgressBar appPro;

    @BindView(R.id.appCopyPro)
    ProgressBar appCopyPro;

    @BindView(R.id.appCopyVal)
    TextView appCopyVal;

    private boolean isUpd = false;

    public static AppMainAdapter appMainAdapter;

    public static ArrayList<HashMap<String,Object>> systemAppList;
    public static ArrayList<HashMap<String,Object>> normalAppList;

    public static ArrayList<HashMap<String,Object>> tempList;

    public static List<HashMap<String,Object>> finallyList;
    public static String appPathStr;

    public static AppExtractActivity appExtractActivity;

    public static Handler appExtractActivityHandler;

    public static long totalSize = 0;
    public static long nowSize = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appextract);
        ButterKnife.bind(this);

        appExtractActivity = this;

        //查询app保存位置
        SQLiteDatabase sqd = ToolsDao.getDatabase();
        HashMap<String,Object> appMap  = ToolsDao.qryTable(sqd, AppExtractEntity.class,AppExtractActivity.this).get(0);
        appPath.setText(appMap.get("appPath").toString());

        systemAppList = new ArrayList<HashMap<String,Object>>();//系统app
        normalAppList = new ArrayList<HashMap<String,Object>>();//普通app

        appExtractActivityHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                if(msg.arg1 == 0){
                    //提取结束更新按钮状态
                    extractBtn.setEnabled(true);
                    appUpdBtn.setEnabled(true);
                }else if(msg.arg1 == 1){
                    //app listview 加载结束更新视图
                    appPro.setVisibility(View.INVISIBLE);
                    appList.setVisibility(View.VISIBLE);
                    extractBtn.setEnabled(true);
                    appList.setAdapter(appMainAdapter);
                }else if(msg.arg1 == 2){
                    //更新提取进程
                    appCopyPro.setProgress((int)nowSize);
                    appCopyVal.setText(msg.obj+"%");
                }
            }
        };

        //休眠1秒钟，防止无法进入，卡上一界面activity，
         new Thread(){
             @Override
             public void run() {
                 try {
                    Thread.sleep(1000);
                     Intent intentService = new Intent(AppExtractActivity.this, AppExtractLoadService.class);
                     startService(intentService);
                 }catch(Exception e){

                 }
             }
         }.start();

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
                nowSize = 0;
                totalSize = 0;
                finallyList = new ArrayList<HashMap<String,Object>>();
                for(HashMap<String,Object> map : tempList){
                    if(((boolean)map.get("isCheck")) == true){
                        finallyList.add(map);
                        totalSize = totalSize + Integer.parseInt(map.get("appRealSize").toString());
                    }
                }
                appCopyPro.setMax((int)totalSize);

                Log.i("AppExtractActivity_Log",finallyList.toString());

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

                    Intent intentService = new Intent(AppExtractActivity.this, AppExtractCopyService.class);

                    startService(intentService);
                }
            }
        });

        //监听Listview
        appList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

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
                appMainAdapter.notifyDataSetChanged();
                tempList.clear();
                if (isChecked){
                    //系统
                    Log.i("AppExtractActivity_Log","展示系统APP");
                    if(!sortSwitch.isChecked()){
                        //小->大
                        tempList.addAll(sortBySize(deepCopy(systemAppList),true));
                    }else{
                        //大->小
                        tempList.addAll(sortBySize(deepCopy(systemAppList),false));
                    }
                }else {
                    //普通
                    Log.i("AppExtractActivity_Log","展示普通APP");

                    if(!sortSwitch.isChecked()){
                        //小->大
                        tempList.addAll(sortBySize(deepCopy(normalAppList),true));
                    }else{
                        //大->小
                        tempList.addAll(sortBySize(deepCopy(normalAppList),false));
                    }
                }
                appMainAdapter.notifyDataSetChanged();
            }
        });

        //监听排序条件
        sortSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                for(int i = 0;i<tempList.size();i++){
                    AppMainAdapter.isSelected.put(i,false);
                }
                appMainAdapter.notifyDataSetChanged();
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
                appMainAdapter.notifyDataSetChanged();
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
                appMainAdapter.notifyDataSetChanged();

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
                appMainAdapter.notifyDataSetChanged();
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
    public static String countFileSize(String filePath) {
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
    public static String splitThree(String str){
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
    public static ArrayList<HashMap<String,Object>> sortBySize(ArrayList<HashMap<String,Object>> tempList,boolean sortMode){
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
    public static ArrayList<HashMap<String,Object>> deepCopy(ArrayList<HashMap<String,Object>> list){
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

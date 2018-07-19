package com.track.mytools;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.content.Intent;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

public class MainActivity extends Activity {
    public TextView warnTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        warnTitle = (TextView)findViewById(R.id.warnTitle);
        //加载初始文件
        Properties pro = new Properties();

        File proFile = new File("/sdcard/UCdownloads/tools.properties");

        if(!proFile.exists()){
            Log.e("su","参123数文件不存在");
            ToolsUntil.showToast(this, "参数文件不存在", 2000);
            return;
        }

        try{
            pro = loadConfig(this,"/sdcard/UCdownloads/tools.properties");
        }catch(Exception e){
            Log.e("su","获取初始化文件失败...");
            ToolsUntil.showToast(this, "获取初始化文件失败...", 2000);
        }
        //Log.i("su","根目录:类型->" + pro.getProperty("path") + ":" + pro.getProperty("type"));

        ToolsEntiy.path = pro.getProperty("path");

        ToolsEntiy.type = pro.getProperty("type");

        String strFilter = pro.getProperty("suFilter");

        ToolsEntiy.suFilter = new String[strFilter.split(",").length];

        try{
        for (int i = 0; i < strFilter.split(",").length; i++) {
            ToolsEntiy.suFilter[i] = strFilter.split(",")[i];
            Log.i("init",ToolsEntiy.suFilter[i]);
        }}catch(Exception e){
            Log.e("error","过滤后缀解析失败");
            return;
        }

        checkFiger();
    }

    /**
     * 检查指纹
     * @return
     */
    public void checkFiger(){
        FingerprintManager fm = (FingerprintManager)this.getSystemService(Service.FINGERPRINT_SERVICE);

        int fingerGrant = checkSelfPermission(Manifest.permission.USE_FINGERPRINT );
        // Log.i("test", "----------->" + fingerGrant);
        //动态检测权限
        if(PackageManager.PERMISSION_GRANTED != fingerGrant){
            //ToolsUntil.showToast(this,"没有权限访问指纹",1000);
           // return false;
            warnTitle.setText("没有权限访问指纹");
            warnTitle.setVisibility(TextView.VISIBLE);
            return;
        }

        //检查是否有指纹模块和是否有录入
        if(!fm.isHardwareDetected() || !fm.isHardwareDetected()){
            //ToolsUntil.showToast(this,"没有指纹模块，或者至少应该有一个指纹",1000);
            //return false;
            warnTitle.setText("没有指纹模块，或者至少应该有一个指纹");
            warnTitle.setVisibility(TextView.VISIBLE);
            return;
        }

        CancellationSignal mCancellationSignal  = new CancellationSignal();
        //检测指纹是否匹配
        try {
            fm.authenticate(null, mCancellationSignal, 0 /* flags */,new FingerprintUtil(this,warnTitle), null);
        }catch(Exception e){

        }
        //return true;
    }

    /**
     * 加载properties
     * @param context
     * @param file
     * @return
     */
    public Properties loadConfig(Context context, String file) {
        Properties properties = new Properties();
        try {
            //以字符流的返回时读取properties
            Reader s = new FileReader(file);
            properties.load(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

}

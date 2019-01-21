package com.track.mytools.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.track.mytools.activity.AppExtractActivity;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class AppExtractService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("AppExtractService","开启AppExtractService");
        CopyThread ct = new CopyThread();
        ct.start();
    }

    class CopyThread extends Thread{

        @Override
        public void run() {
            for(HashMap<String,Object> map : AppExtractActivity.finallyList){
                String rootPath = AppExtractActivity.appPathStr;
                String appName = map.get("appName").toString();
                String appVersionName = map.get("appVersionName").toString();
                File readFile = new File(map.get("appDir").toString());
                File outFile = new File(rootPath + "/" + appName + "_" + appVersionName + ".apk");
                InputStream is = null;
                OutputStream os = null;
                try{
                    is = new FileInputStream(readFile);
                    os= new FileOutputStream(outFile);
                    byte []Bytes = new byte[512];
                    int flag = 0;
                    while((flag=is.read(Bytes))>-1){
                        os.write(Bytes,0,flag);
                    }
                    os.flush();
                }catch(Exception e){
                    try{
                        if(is!=null){
                            is.close();
                        }
                        if(os!=null){
                            os.close();
                        }
                    }catch(Exception e1){}
                }finally{
                    if(readFile.length()!=outFile.length()){
                        Message msg = AppExtractActivity.handler.obtainMessage();
                        AppExtractActivity.handler.sendMessage(msg);
                        ToolsUtil.showToast(AppExtractActivity.aea,appName + "复制异常,暂停复制!",2000);
                        stopSelf();
                        break;
                    }else{
                        ToolsUtil.showToast(AppExtractActivity.aea,appName + "复制，检测完成",300);
                    }
                }
            }
            Message msg = AppExtractActivity.handler.obtainMessage();
            AppExtractActivity.handler.sendMessage(msg);
            ToolsUtil.showToast(AppExtractActivity.aea,"全部复制完成",1000);
            stopSelf();
        }
    }
}

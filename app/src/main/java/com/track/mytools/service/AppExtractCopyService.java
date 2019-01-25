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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class AppExtractCopyService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("AppExtractCopyService_Log","开启AppExtractService");
        CopyThread ct = new CopyThread();
        ct.start();
    }

    /**
     * App提取线程
     *
     */
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
                        Message msg = AppExtractActivity.appExtractActivityHandler.obtainMessage();
                        msg.arg1=2;
                        AppExtractActivity.nowSize = AppExtractActivity.nowSize + flag;
                        msg.obj=countPercent(String.valueOf(AppExtractActivity.nowSize),String.valueOf(AppExtractActivity.totalSize));
                        AppExtractActivity.appExtractActivityHandler.sendMessage(msg);
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
                        Message msg = AppExtractActivity.appExtractActivityHandler.obtainMessage();
                        msg.arg1=0;
                        AppExtractActivity.appExtractActivityHandler.sendMessage(msg);
                        ToolsUtil.showToast(AppExtractActivity.appExtractActivity,appName + "复制异常,暂停复制!",2000);
                        stopSelf();
                        break;
                    }else{
                        ToolsUtil.showToast(AppExtractActivity.appExtractActivity,appName + "复制，检测完成",300);
                    }
                }
            }
            Message msg = AppExtractActivity.appExtractActivityHandler.obtainMessage();
            msg.arg1=0;
            AppExtractActivity.appExtractActivityHandler.sendMessage(msg);
            ToolsUtil.showToast(AppExtractActivity.appExtractActivity,"全部复制完成",1000);
            stopSelf();
        }
    }

    /**
     * 计算百分比
     * @return
     */
    public String countPercent(String n,String t){
        Double a = Double.parseDouble(n);
        Double b = Double.parseDouble(t);
        BigDecimal bi1 = new BigDecimal(a.toString());
        BigDecimal bi2 = new BigDecimal(b.toString());
        BigDecimal divide = bi1.divide(bi2, 4, RoundingMode.HALF_UP);

        String res =  (divide.doubleValue()*100) + "";

        String big = res.split("\\.")[0] + ".";
        String little = res.split("\\.")[1];

        if(little.length()>2){
            little = little.substring(0,2);
        }

        if(big.indexOf("100")>-1){
            little="";
            big = big.substring(0,big.length() -1);
        }

        return big + little;
    }
}

package com.track.mytools.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.track.mytools.activity.AppExtractActivity;
import com.track.mytools.adapter.AppMainAdapter;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppExtractLoadService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获取app应用列表
        PackageManager pm = this.getPackageManager();
        List<ApplicationInfo> appInfos= pm.getInstalledApplications(0);

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
                tempMap.put("appSize", AppExtractActivity.countFileSize(appInfo.sourceDir)); // 以MB为单位
                tempMap.put("appRealSize",new File(appInfo.sourceDir).length()); // 实际app大小
                tempMap.put("isCheck",false); // 默认为不选中
                tempMap.put("appDir",appDir);

                //判断是否是系统应用，
                int flags = appInfo.flags;
                if((flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
                    //Log.d("app  ","  是系统应用  ");
                    AppExtractActivity.systemAppList.add(tempMap);
                }else{
                    //Log.d("app  ","  不是系统应用  ");
                    AppExtractActivity.normalAppList.add(tempMap);
                }
            }catch(Exception e){
                e.getStackTrace();
                // Log.e("AppExtractActivity",e.getMessage());
            }
        }

        //默认显示普通应用
        try {
            AppExtractActivity.tempList = (ArrayList<HashMap<String, Object>>) ToolsUtil.deepCopy(AppExtractActivity.normalAppList);
        }catch(Exception e){
            Log.i("AppExtractLoadService_Log","深复制失败：" + e.getMessage());
        }

        //默认先按从小到大排序
        AppExtractActivity.tempList = AppExtractActivity.sortBySize(AppExtractActivity.tempList,true);

        AppExtractActivity.appMainAdapter = new AppMainAdapter(this,AppExtractActivity.tempList);

        Message msg = AppExtractActivity.appExtractActivityHandler.obtainMessage();

        msg.arg1=1;

        AppExtractActivity.appExtractActivityHandler.sendMessage(msg);

        stopSelf();
    }
}

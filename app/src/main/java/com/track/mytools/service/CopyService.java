package com.track.mytools.service;

import android.app.Service;
import android.content.ClipData;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.track.mytools.activity.CopyActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class CopyService extends Service {

    /**
     * 复制服务
     *
     */

    private static ClipData data; // 剪贴板
    private static String oldStr; // 剪贴板旧内容

    public static boolean firstCopy = true; //是否第一次复制
    private boolean isWhile = true;  //是否继续循环检测剪贴板

    @Override
    public void onCreate() {
        super.onCreate();
        //启动线程，每隔1秒检测复制的内容是否有变化
        Log.i("CopyService_Log","开启多线程...");
        new Thread(new CopyThread()).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isWhile = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class CopyThread extends Thread{
        @Override
        public void run() {
            while(isWhile) {
                try {
                    Thread.sleep(100);
                    } catch (Exception e) {
                        e.getStackTrace();
                        Log.e("CopyService_Log", e.getMessage());
                    }
                    Log.i("CopyService_Log", "检查...");

                    ClipData data = CopyActivity.cm.getPrimaryClip();
                    if (data != null) {
                        ClipData.Item item = data.getItemAt(0);
                        String content = item.getText().toString();
                        Log.i("CopyService_Log",content);
                        //第一次复制
                        if (firstCopy == true) {
                            oldStr = content;
                            firstCopy = false;
                        }

                        if (!oldStr.equals(content)) {
                            saveCopyFile(CopyActivity.saveFile, content);
                            oldStr = content;
                        }
                    }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * 保存内容到本地
     * @param saveFile
     * @param content
     */
    private void saveCopyFile(String saveFile,String content){
        File file = new File(saveFile);
        if(file.exists()){
            file.mkdir();
        }

        //内容为空，跳过输出
        if("".equals(content.trim())){
            return;
        }

        try{
            Writer w  = new FileWriter(file,true);
            w.append(content);
            w.append("\r\n");
            w.flush();
            w.close();
        }catch(Exception e){
            e.getStackTrace();
            Log.e("CopyService_Log",e.getMessage());
        }
    }
}

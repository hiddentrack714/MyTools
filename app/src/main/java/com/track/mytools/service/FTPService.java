package com.track.mytools.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.track.mytools.R;
import com.track.mytools.activity.FTPActivity;
import com.track.mytools.util.FTPUtil;
import com.track.mytools.util.ToolsUtil;

import java.io.File;

public class FTPService extends Service {

    private String ip;
    private int port;
    private String user;
    private String password;
    private String fileName;
    private String localPath;
    private String serverPath;

    public static boolean isFinish = true;

    public static FTPUtil fTPUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        ip = FTPActivity.ftpVal[0];
        port = Integer.parseInt(FTPActivity.ftpVal[1]);
        user = FTPActivity.ftpVal[2];
        password = FTPActivity.ftpVal[3];
        serverPath = FTPActivity.ftpVal[4];
        localPath = FTPActivity.ftpVal[5];
        fileName = FTPActivity.ftpVal[6];

        //ftp文件下载
        new Thread(){
            @Override
            public void run() {
                 fTPUtil = new FTPUtil(ip,port,user,password);
                if(fTPUtil.ftpLogin()){
                    FTPActivity.remoteFileSize = fTPUtil.getFileSeize(serverPath,fileName);
                    Message msg = FTPActivity.handler.obtainMessage();
                    msg.arg1 = 0;
                    FTPActivity.handler.sendMessage(msg);
                    //开启通知
                    FTPActivity.mBuilder = new NotificationCompat.Builder(FTPActivity.fTPActivity);

                    FTPActivity.mBuilder.setSmallIcon(R.mipmap.ic_launcher);

                    FTPActivity.mBuilder.setContentTitle(fileName);

                    FTPActivity.mBuilder.setContentText("正在下载");

                    FTPActivity.notificationManager.notify(0x3, FTPActivity.mBuilder.build());

                    FTPActivity.mBuilder.setProgress(Integer.parseInt(FTPActivity.remoteFileSize),0,false);

                    MyThread t = new MyThread();
                    t.start();
                    Log.i("FTPSERVICE1","文件大小:" + FTPActivity.remoteFileSize);

                    boolean sucess = fTPUtil.downloadFile(fileName, localPath, serverPath);

                    if(sucess){
                        ToolsUtil.showToast(FTPActivity.fTPActivity,"下载成功",2000);
                    }else{
                        ToolsUtil.showToast(FTPActivity.fTPActivity,"下载失败",2000);
                    }

                }else{
                    Log.e("FTPSERVICE2","FTP Login Fail:");
                    ToolsUtil.showToast(FTPActivity.fTPActivity,"FTP登陆失败",2000);
                }
            }
        }.start();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MyThread extends Thread{
        @Override
        public void run() {
            Log.i("FTPSERVICE3","开启下载检测");
            try{
                Thread.sleep(1000);
                while(isFinish){
                    File file = new File(localPath + fileName);
                    if(file.exists()){
                        Long fileSize = file.length();
                        Message msg = FTPActivity.handler.obtainMessage();

                        if(Long.parseLong(FTPActivity.remoteFileSize) == fileSize){
                            isFinish = false;
                            msg.arg1 = 1;

                            fTPUtil.ftpLogOut();
                        }else{
                            msg.arg1 = 2;
                            msg.obj = fileSize;
                        }

                        FTPActivity.handler.sendMessage(msg);
                    }
                }
            }catch(Exception e){
                e.getStackTrace();
                Log.e("FTPSERVICE4",e.getMessage());
            }
        }
    }

}

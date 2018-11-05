package com.track.mytools.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.track.mytools.activity.FTPActivity;
import com.track.mytools.until.FTPUtil;
import com.track.mytools.until.ToolsUntil;

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
                FTPUtil fTPUtil = new FTPUtil(ip,port,user,password);
                if(fTPUtil.ftpLogin()){
                    FTPActivity.remoteFileSize = fTPUtil.getFileSeize(serverPath,fileName);
                    Message msg = FTPActivity.handler.obtainMessage();
                    msg.arg1 = 0;
                    FTPActivity.handler.sendMessage(msg);
                    MyThread t = new MyThread();
                    t.start();
                    Log.i("FTPSERVICE1","文件大小:" + FTPActivity.remoteFileSize);
                    boolean sucess = fTPUtil.downloadFile(fileName, localPath, serverPath);
                    if(sucess){
                        ToolsUntil.showToast(FTPActivity.fTPActivity,"下载成功",2000);
                    }else{
                        ToolsUntil.showToast(FTPActivity.fTPActivity,"下载失败",2000);
                    }
                    isFinish = false; // 下载完成，停止进度检测
                    msg = FTPActivity.handler.obtainMessage();
                    msg.arg1 = 1;
                    FTPActivity.handler.sendMessage(msg);
                    fTPUtil.ftpLogOut();
                }else{
                    Log.e("FTPSERVICE2","FTP Login Fail:");
                    ToolsUntil.showToast(FTPActivity.fTPActivity,"FTP登陆失败",2000);
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
                Thread.sleep(500);
                while(isFinish){
                    File file = new File(localPath + fileName);
                    if(file.exists()){
                        Long fileSize = file.length();
                        Message msg = FTPActivity.handler.obtainMessage();
                        msg.arg1 = 2;
                        msg.obj = fileSize;
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

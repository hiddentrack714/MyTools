package com.track.mytools.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.track.mytools.R;
import com.track.mytools.service.FTPService;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.FTPEntity;
import com.track.mytools.util.ToolsUtil;

import java.util.HashMap;

/**
 * FTP下载
 */
public class FTPActivity extends Activity {

    private EditText ftpIP;
    private EditText ftpPORT;
    private EditText ftpUser;
    private EditText ftpPassword;
    private EditText ftpServerPath;
    private EditText ftpLocalPath;
    private EditText ftpFileName;

    private ProgressBar ftpPro;

    private Button ftpDownBtn;  //下载
    private Button ftpUpdBtn;   //修改

    public static Handler handler;

    private TextView ftpProText;

    public static String remoteFileSize; // ftp远端文件大小;

    public static FTPActivity fTPActivity;

    public static String ftpVal[] = new String[7];

    public static NotificationManager notificationManager;

    public static NotificationCompat.Builder mBuilder;

    private static boolean isUpd = false; // 是否修改中

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);

        ftpIP = (EditText)findViewById(R.id.ftpIP);
        ftpPORT = (EditText)findViewById(R.id.ftpPORT);
        ftpUser = (EditText)findViewById(R.id.ftpUser);
        ftpServerPath = (EditText)findViewById(R.id.ftpServerPath);
        ftpLocalPath = (EditText)findViewById(R.id.ftpLocalPath);
        ftpPassword = (EditText)findViewById(R.id.ftpPassword);
        ftpFileName = (EditText)findViewById(R.id.ftpFileName);

        ftpDownBtn = (Button)findViewById(R.id.ftpDownBtn);
        ftpUpdBtn = (Button)findViewById(R.id.ftpUpdBtn);

        ftpPro = (ProgressBar)findViewById(R.id.ftpPro);

        ftpProText = (TextView)findViewById(R.id.ftpProText);

        fTPActivity = this;

        SQLiteDatabase sdb = ToolsDao.getDatabase();

        HashMap<String,Object> map = ToolsDao.qryTable(sdb,FTPEntity.class,FTPActivity.this).get(0);

        ftpIP.setText(map.get("ftpIP").toString());
        ftpPORT.setText(map.get("ftpPORT").toString());
        ftpUser.setText(map.get("ftpUser").toString());
        ftpServerPath.setText(map.get("ftpServerPath").toString());
        ftpLocalPath.setText(map.get("ftpLocalPath").toString());
        ftpPassword.setText(map.get("ftpPassword").toString());

        Intent intent = new Intent(FTPActivity.this,FTPService.class);

        //运行在主线程的Handler,它将监听所有的消息（Message）
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                String viewText = arg0.obj +"/"+ remoteFileSize;
                //设置进度条最大值
                if(arg0.arg1 == 0){
                    ftpPro.setMax(Integer.parseInt(remoteFileSize));
                }
                //下载完成，停止服务
                if(arg0.arg1 == 1){
                    Log.i("FTPActivity1","下载完成");
                    ftpDownBtn.setEnabled(true);
                    ftpUpdBtn.setEnabled(true);

                    ftpPro.setProgress(Integer.parseInt(remoteFileSize));
                    ftpProText.setText(remoteFileSize+"/"+remoteFileSize);

                    mBuilder.setProgress(Integer.parseInt(remoteFileSize),Integer.parseInt(remoteFileSize),false);
                    mBuilder.setContentText("下载完成");
                    notificationManager.notify(0x3,mBuilder.build());
                    stopService(intent);
                }
                //进度条更新
                if(arg0.arg1 == 2){
                    ftpPro.setProgress(Integer.parseInt(arg0.obj+""));
                    ftpProText.setText(viewText);
                    Log.i("FTPActivity1","下载进度更新:"+viewText);
                    mBuilder.setProgress(Integer.parseInt(remoteFileSize),Integer.parseInt(arg0.obj+""),false);
                    notificationManager.notify(0x3,mBuilder.build());
                }

                //初始化
                if(arg0.arg1 == 4){
                    ftpPro.setProgress(0);
                    FTPService.isFinish = true;
                }
                return false;
            }
        });

        //ftp下载案件监听
        ftpDownBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String ip = ftpIP.getText().toString();
                int port = Integer.parseInt(ftpPORT.getText().toString());
                String user = ftpUser.getText().toString();
                String password = ftpPassword.getText().toString();

                String serverPath = ftpServerPath.getText().toString();
                String localPath = ftpLocalPath.getText().toString();
                String fileName = ftpFileName.getText().toString();

                if("".equals(fileName.trim())){
                    ToolsUtil.showToast(fTPActivity,"下载文件不能为空",3000);
                    return;
                }

                ftpVal[0] = ip;
                ftpVal[1] = port + "";
                ftpVal[2] = user;
                ftpVal[3] = password;
                ftpVal[4] = serverPath;
                ftpVal[5] = localPath;
                ftpVal[6] = fileName;

                Message msg = FTPActivity.handler.obtainMessage();
                msg.arg1 = 4;
                FTPActivity.handler.sendMessage(msg);

                startService(intent);

                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                ftpDownBtn.setEnabled(false);
                ftpUpdBtn.setEnabled(false);
            }
        });

        //点击修改按钮
        ftpUpdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isUpd == false){
                    //修改中
                    ftpIP.setEnabled(true);
                    ftpPORT.setEnabled(true);
                    ftpUser.setEnabled(true);
                    ftpServerPath.setEnabled(true);
                    ftpLocalPath.setEnabled(true);
                    ftpPassword.setEnabled(true);

                    isUpd = true;

                    ftpUpdBtn.setText("完成");

                }else{
                    //完成修改
                    ftpIP.setEnabled(false);
                    ftpPORT.setEnabled(false);
                    ftpUser.setEnabled(false);
                    ftpServerPath.setEnabled(false);
                    ftpLocalPath.setEnabled(false);
                    ftpPassword.setEnabled(false);

                    isUpd = false;

                    SQLiteDatabase sdb = ToolsDao.getDatabase();

                    HashMap<String,Object> dataMap = new HashMap<String,Object>();

                    dataMap.put("ftpIP",ftpIP.getText().toString());
                    dataMap.put("ftpPORT",ftpPORT.getText().toString());
                    dataMap.put("ftpUser",ftpUser.getText().toString());
                    dataMap.put("ftpServerPath",ftpServerPath.getText().toString());
                    dataMap.put("ftpLocalPath",ftpLocalPath.getText().toString());
                    dataMap.put("ftpPassword",ftpPassword.getText().toString());
                    dataMap.put("id",map.get("id"));

                    ToolsDao.saveOrUpdIgnoreExsit(sdb,dataMap,FTPEntity.class);
                    ftpUpdBtn.setText("修改");
                }
            }
        });
    }
}

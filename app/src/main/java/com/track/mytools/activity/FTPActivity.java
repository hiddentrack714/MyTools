package com.track.mytools.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.FTPEntity;
import com.track.mytools.service.FTPService;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

/**
 * FTP下载
 */
public class FTPActivity extends Activity {

    @BindView(R.id.ftpIP)
    EditText ftpIP;

    @BindView(R.id.ftpPORT)
    EditText ftpPORT;

    @BindView(R.id.ftpUser)
    EditText ftpUser;

    @BindView(R.id.ftpPassword)
    EditText ftpPassword;

    @BindView(R.id.ftpServerPath)
    EditText ftpServerPath;

    @BindView(R.id.ftpLocalPath)
    EditText ftpLocalPath;

    @BindView(R.id.ftpFileName)
    EditText ftpFileName;

    @BindView(R.id.ftpPro)
    ProgressBar ftpPro;

    @BindView(R.id.ftpDownBtn)
    Button ftpDownBtn;  //下载

    @BindView(R.id.ftpUpdBtn)
    Button ftpUpdBtn;   //修改

    @BindView(R.id.ftpProText)
    TextView ftpProText;

    public static Handler ftpActivityhandler;

    public static String remoteFileSize; // ftp远端文件大小;

    public static FTPActivity fTPActivity;

    public static String ftpVal[] = new String[7];

    public static NotificationManager notificationManager;

    public static NotificationCompat.Builder mBuilder;

    private static boolean isUpd = false; // 是否修改中

    private final int EX_FILE_PICKER_RESULT = 0xfa01;
    private String startDirectory = null;// 记忆上一次访问的文件目录路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);
        ButterKnife.bind(this);

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
        ftpActivityhandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                String viewText = arg0.obj +"/"+ remoteFileSize;

                //设置进度条最大值
                if(arg0.arg1 == 0){
                    ftpPro.setMax(Integer.parseInt(remoteFileSize));
                }

                //下载完成，停止服务
                if(arg0.arg1 == 1){
                    Log.i("FTPActivity_Log","下载完成");
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

                    mBuilder.setProgress(Integer.parseInt(remoteFileSize),Integer.parseInt(arg0.obj+""),false);
                    notificationManager.notify(0x3,mBuilder.build());
                }

                //初始化
                if(arg0.arg1 == 4){
                    ftpPro.setProgress(0);
                    FTPService.isFinish = true;
                }

                //ftp登陆失败，重新启用下载按键
                if(arg0.arg1 == 3){
                    ftpDownBtn.setEnabled(true);
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

                ftpVal[0] = ip;
                ftpVal[1] = port + "";
                ftpVal[2] = user;
                ftpVal[3] = password;
                ftpVal[4] = serverPath;
                ftpVal[5] = localPath;
                ftpVal[6] = fileName;

                if("".equals(fileName.trim())){
                    ToolsUtil.showToast(FTPActivity.this,"下载文件不能为空",3000);
                    return;
                }

                File file = new File(localPath+fileName);

                //如果要下载的文件本地已经存在，则弹出，删除，修改名称，还是取消
                if(file.exists()){
                    AlertDialog.Builder normalDialog =
                            new AlertDialog.Builder(FTPActivity.this);
                    normalDialog.setTitle("选择").setMessage("本地存在同名文件或文件夹，请选择?");

                    normalDialog.setPositiveButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                    normalDialog.setNeutralButton("删除本地文件",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(file.delete()){
                                        ToolsUtil.showToast(FTPActivity.this,fileName + " 删除成功，继续下载",1000);
                                        dialogRes(intent);
                                    } else{
                                        ToolsUtil.showToast(FTPActivity.this,fileName + " 删除失败",1000);
                                    }
                                }
                            });

                    normalDialog.setNegativeButton("修改本地文件名", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(file.renameTo(new File(localPath+fileName+"[1]"))){
                                ToolsUtil.showToast(FTPActivity.this,fileName + " 修改成功，继续下载",1000);
                                dialogRes(intent);
                            } else{
                                ToolsUtil.showToast(FTPActivity.this,fileName + " 修改失败",1000);
                            }
                        }
                    });
                    // 创建实例并显示
                    normalDialog.show();
                }else{
                    dialogRes(intent);
                }

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
                    ftpDownBtn.setEnabled(false);

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
                    ftpDownBtn.setEnabled(true);

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
                    ftpUpdBtn.setText("修改参数");
                }
            }
        });

        ftpLocalPath.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    ExFilePicker exFilePicker = new ExFilePicker();
                    exFilePicker.setCanChooseOnlyOneItem(true);// 单选
                    exFilePicker.setQuitButtonEnabled(true);
                    exFilePicker.setChoiceType(ExFilePicker.ChoiceType.DIRECTORIES);

                    if (TextUtils.isEmpty(startDirectory)) {
                        exFilePicker.setStartDirectory(Environment.getExternalStorageDirectory().getPath());
                    } else {
                        exFilePicker.setStartDirectory(startDirectory);
                    }

                    exFilePicker.start(FTPActivity.this, EX_FILE_PICKER_RESULT);
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EX_FILE_PICKER_RESULT) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                String path = result.getPath();

                List<String> names = result.getNames();
                for (int i = 0; i < names.size(); i++) {
                    File f = new File(path, names.get(i));
                    try {
                        Uri uri = Uri.fromFile(f); //这里获取了真实可用的文件资源

                        ftpLocalPath.setText(uri.getPath() + "/");
                        startDirectory = path;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 选择框
     * @param intent
     */
    private void dialogRes(Intent intent){

        Message msg = FTPActivity.ftpActivityhandler.obtainMessage();
        msg.arg1 = 4;
        FTPActivity.ftpActivityhandler.sendMessage(msg);

        startService(intent);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        ftpDownBtn.setEnabled(false);
        ftpUpdBtn.setEnabled(false);
    }
}

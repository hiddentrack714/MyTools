package com.track.mytools.activity;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.CopyEntity;
import com.track.mytools.service.CopyService;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 快捷复制
 */
public class CopyActivity extends Activity {

    @BindView(R.id.copyUseBtn)
    Button copyUseBtn; // 服务启动/关闭按钮

    @BindView(R.id.copyUpdBtn)
    Button copyUpdBtn; //修改

    @BindView(R.id.copyPhoneFile)
    EditText copyPhoneFile; //手机端保存文件

    public static String saveFile;//文件保存位置

    private static boolean isStart = false; //是否开始监听服务

    public static ClipboardManager cm;

    private static boolean isUpd = false; //判断是否修改中

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy);
        ButterKnife.bind(this);

        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> map =  ToolsDao.qryTable(sdb,CopyEntity.class,CopyActivity.this).get(0);
        copyPhoneFile.setText(map.get("copyPhoneFile").toString());

        //服务启动按钮监听
        copyUseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CopyActivity.this,CopyService.class);
                if(isStart == false){
                    isStart = true;
                    saveFile = copyPhoneFile.getText().toString();
                    startService(intent);
                    copyUseBtn.setText("关闭服务");
                }else{
                    isStart = false;
                    CopyService.firstCopy = true;
                    stopService(intent);
                    copyUseBtn.setText("开启服务");
                }
            }
        });

        //修改按钮监听
        copyUpdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isUpd ==false){
                    //修改中
                    copyPhoneFile.setEnabled(true);

                    isUpd = true;
                    copyUpdBtn.setText("完成");
                }else{
                   //修改完成
                    copyPhoneFile.setEnabled(false);

                    SQLiteDatabase sdb =  ToolsDao.getDatabase();
                    HashMap<String,Object> dataMap = new HashMap<String,Object>();
                    dataMap.put("copyPhoneFile",copyPhoneFile.getText().toString());

                    Log.i("CopyActivity_Log",(String)map.get("id"));

                    dataMap.put("id",map.get("id"));
                    ToolsDao.saveOrUpdIgnoreExsit(sdb,dataMap,CopyEntity.class);

                    isUpd = false;
                    copyUpdBtn.setText("修改");
                }
            }
        });
    }
}

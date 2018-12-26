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
import android.widget.LinearLayout;

import com.track.mytools.R;
import com.track.mytools.Service.CopyService;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.CopyEntity;

import java.util.HashMap;

/**
 * 快捷复制
 */
public class CopyActivity extends Activity {

    /**
     * 手机端多次复制保存
     * @param savedInstanceState
     */

    private Button copyUseBtn; // 服务启动/关闭按钮
    private Button copyUpdBtn; //修改

    public static EditText copyPhoneFile; //手机端保存文件

    private LinearLayout copyPhoneLayout; //手机模式

    public static String saveFile;//文件保存位置

    private static boolean isStart = false; //是否开始监听服务

    public static CopyActivity copyActivity;

    public static ClipboardManager cm;

    private static boolean isUpd = false; //判断是否修改中

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy);

        copyPhoneFile = (EditText)findViewById(R.id.copyPhoneFile);
        copyPhoneLayout = (LinearLayout)findViewById(R.id.copyPhoneLayout);
        copyUseBtn = (Button)findViewById(R.id.copyUseBtn);
        copyUpdBtn = (Button)findViewById(R.id.copyUpdBtn);

        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> map =  ToolsDao.qryTable(sdb,CopyEntity.class).get(0);
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
                    Log.i("1111111111",(String)map.get("id"));
                    dataMap.put("id",map.get("id"));
                    ToolsDao.saveOrUpdIgnoreExsit(sdb,dataMap,CopyEntity.class);

                    isUpd = false;
                    copyUpdBtn.setText("修改");
                }
            }
        });
    }
}

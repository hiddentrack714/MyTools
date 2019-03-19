package com.track.mytools.activity;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.CopyEntity;
import com.track.mytools.service.CopyService;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

/**
 * 快捷复制
 */
public class CopyActivity extends Activity {

    @BindView(R.id.copyUseBtn)
    Button copyUseBtn; // 服务启动/关闭按钮

    @BindView(R.id.copyUpdBtn)
    Button copyUpdBtn; //修改

    @BindView(R.id.copyFile)
    EditText copyFile; //保存名称

    @BindView(R.id.copyPath)
    EditText copyPath; //保存路径

    public static String saveFile;//文件保存位置

    private boolean isStart = false; //是否开始监听服务

    public static ClipboardManager cm;

    private boolean isUpd = false; //判断是否修改中

    private final int EX_FILE_PICKER_RESULT = 0xfa01;
    private String startDirectory = null;// 记忆上一次访问的文件目录路径

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy);
        ButterKnife.bind(this);

        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> map =  ToolsDao.qryTable(sdb,CopyEntity.class,CopyActivity.this).get(0);
        copyFile.setText(map.get("copyFile").toString());
        copyPath.setText(map.get("copyPath").toString());

        //服务启动按钮监听
        copyUseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CopyActivity.this,CopyService.class);
                if(isStart == false){
                    isStart = true;
                    saveFile = copyPath.getText().toString() + copyFile.getText().toString();
                    ToolsUtil.showToast(CopyActivity.this,"保存路径:" + saveFile,2000);
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
                    copyFile.setEnabled(true);
                    copyPath.setEnabled(true);

                    isUpd = true;
                    copyUpdBtn.setText("完成");
                    copyUseBtn.setEnabled(false);
                }else{
                   //修改完成
                    copyFile.setEnabled(false);
                    copyPath.setEnabled(false);

                    SQLiteDatabase sdb =  ToolsDao.getDatabase();
                    HashMap<String,Object> dataMap = new HashMap<String,Object>();
                    dataMap.put("copyFile",copyFile.getText().toString());
                    dataMap.put("copyPath",copyPath.getText().toString());

                    Log.i("CopyActivity_Log",(String)map.get("id"));

                    dataMap.put("id",map.get("id"));
                    ToolsDao.saveOrUpdIgnoreExsit(sdb,dataMap,CopyEntity.class);

                    isUpd = false;
                    copyUpdBtn.setText("修改参数");
                    copyUseBtn.setEnabled(true);
                }
            }
        });

        //监听
        copyPath.setOnTouchListener(new View.OnTouchListener(){

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

                    exFilePicker.start(CopyActivity.this, EX_FILE_PICKER_RESULT);
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

                        copyPath.setText(uri.getPath() + "/");
                        startDirectory = path;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

package com.track.mytools.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.track.mytools.R;
import com.track.mytools.Service.CopyService;

public class CopyActivity extends Activity {

    /**
     * 手机端多次复制保存
     * @param savedInstanceState
     */

    private Switch copySwitch; // 单一下载选项

    private Button copyUseBtn; // 服务启动/关闭按钮

    public static EditText copyPhoneFile; //手机端保存文件
    public static EditText copyPCIP; // PC端IP
    public static EditText copyPCPort; //PC端Port

    private LinearLayout copyPhoneLayout; //手机模式
    private LinearLayout copyIPLayout;    //PC模式IP
    private LinearLayout copyPortLayout;  //PC模式Port

    public static Boolean isSingle = false; // 复制模式，默认为保存到手机

    public static String saveFile;

    private static boolean isStart = false;

    public static CopyActivity copyActivity;

    public static ClipboardManager cm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy);

        copySwitch = (Switch)findViewById(R.id.copySwitch);

        copyPhoneFile = (EditText)findViewById(R.id.copyPhoneFile);
        copyPCIP = (EditText)findViewById(R.id.copyPCIP);
        copyPCPort = (EditText)findViewById(R.id.copyPCPort);

        copyPhoneLayout = (LinearLayout)findViewById(R.id.copyPhoneLayout);
        copyIPLayout = (LinearLayout)findViewById(R.id.copyIPLayout);
        copyPortLayout = (LinearLayout)findViewById(R.id.copyPortLayout);

        copyUseBtn = (Button)findViewById(R.id.copyUseBtn);

        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        //复制模式监听
        copySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //PC
                    isSingle = true;
                    Log.i("COPY_ACTIVITY","PC");
                    copyPhoneLayout.setVisibility(View.GONE); //隐藏手机端音信

                    copyIPLayout.setVisibility(View.VISIBLE); //显示PC信息
                    copyPortLayout.setVisibility(View.VISIBLE); //显示PC信息
                }else {
                    //手机
                    isSingle = false;
                    Log.i("COPY_ACTIVITY","手机");
                    copyPhoneLayout.setVisibility(View.VISIBLE); //显示手机端音信

                    copyIPLayout.setVisibility(View.GONE); //隐藏PC信息
                    copyPortLayout.setVisibility(View.GONE); //隐藏PC信息
                }

            }
        });

        //服务启动按钮监听
        copyUseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CopyActivity.this,CopyService.class);
                if(isStart == false){
                    isStart = true;
                    saveFile = copyPhoneFile.getText().toString();
                    intent.putExtra("a","");
                    startService(intent);
                    copyUseBtn.setText("关闭服务");
                }else{
                    isStart = false;
                    stopService(intent);
                    copyUseBtn.setText("开启服务");
                }
            }
        });

    }

}

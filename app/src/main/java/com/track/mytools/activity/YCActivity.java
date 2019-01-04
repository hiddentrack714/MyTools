package com.track.mytools.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.track.mytools.R;
import com.track.mytools.util.ToolsUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * YC调度切换模式
 *
 */
public class YCActivity extends Activity {

    private RadioButton ycSD;
    private RadioButton ycPH;
    private RadioButton ycXN;
    private RadioButton ycDYC;
    private RadioButton ycTY;
    private RadioGroup ycRG;
    private Button ycBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_yc);

        ycSD = findViewById(R.id.ycSD);
        ycPH = findViewById(R.id.ycPH);
        ycXN = findViewById(R.id.ycXN);
        ycDYC = findViewById(R.id.ycDYC);
        ycTY = findViewById(R.id.ycTY);
        ycRG = findViewById(R.id.ycRG);
        ycBtn = findViewById(R.id.ycBtn);

        //读取当当前模式
        Process process = null;
        InputStream is = null;
        try {
            process = Runtime.getRuntime().exec("cat /data/wipe_mode");
            is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            Log.e("YCActivity","当前模式:" + line);

            if("powersave".equals(line)){
                //省电
                ycRG.check(R.id.ycSD);
            }else if("balance".equals(line)){
                //平衡(默认)
                ycRG.check(R.id.ycPH);
            }else if("performance".equals(line)){
                //性能
                ycRG.check(R.id.ycXN);
            }else if("fast".equals(line)){
                //低延迟
                ycRG.check(R.id.ycDYC);
            }else if("disabled".equals(line)){
                //停用
                ycRG.check(R.id.ycTY);
            }
        } catch (Exception e) {
            // return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }

        //设置按钮监听
        ycBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String mode="";
                String commod[] = new String[1];
               if(R.id.ycSD == ycRG.getCheckedRadioButtonId()){
                   mode = "powersave";
                   commod[0] = "echo \"powersave\" > /data/wipe_mode";
               }else if(R.id.ycPH == ycRG.getCheckedRadioButtonId()){
                   mode = "balance";
                   commod[0] = "echo \"balance\" > /data/wipe_mode";
               }else if(R.id.ycXN == ycRG.getCheckedRadioButtonId()){
                   mode = "performance";
                   commod[0] = "echo \"performance\" > /data/wipe_mode";
               }else if(R.id.ycDYC == ycRG.getCheckedRadioButtonId()){
                   mode = "fast";
                   commod[0] = "echo \"fast\" > /data/wipe_mode";
               }else if(R.id.ycTY == ycRG.getCheckedRadioButtonId()){
                   mode = "disabled";
                   commod[0] = "echo \"disabled\" > /data/wipe_mode";
               }

               Log.i("YCActivity","设置电量为:" + mode);

               ToolsUtil.exeCommod(commod);
            }
        });

        super.onCreate(savedInstanceState);
    }
}


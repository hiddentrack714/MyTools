package com.track.mytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.track.mytools.util.ToolsUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

/**
 * 无界面activity ，为shortcut服务
 *
 */

public class YCTempActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Intent intent = getIntent();
        String tempMode = intent.getData().toString();
        Log.e("YCTempActivity","临时电量模式:" + tempMode);

        String commod[] = {"powercfg "+tempMode};
        //ToolsUtil.exeCommod(commod);

        Process process = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
            try {
                process = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(process.getOutputStream());
                dos.writeBytes(commod[0] + "\n");
                dos.writeBytes("exit\n");
                dos.flush();
                process.waitFor();

                dis =  new DataInputStream(process.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(dis));
                ToolsUtil.showToast(this,br.readLine(),3000);
                //Log.i("YCTempActivity",br.readLine());
            } catch (Exception e) {
                // return false;
            } finally {
                try {
                    if (dos != null) {
                        dos.close();
                    }
                    if (dis != null) {
                        dis.close();
                    }
                    process.destroy();
                } catch (Exception e) {
                }
            }
        finish();
        super.onCreate(savedInstanceState);
    }
}

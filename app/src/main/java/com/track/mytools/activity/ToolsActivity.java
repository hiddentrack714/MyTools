package com.track.mytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.track.mytools.R;
import com.track.mytools.util.ToolsUtil;

/**
 * 功能主界面
 *
 */
public class ToolsActivity extends Activity implements OnClickListener{

    private Button suffixBtn;
    private Button qrySuBtn;
    private Button httpBtn;
    private Button copyBtn;
    private Button ftpBtn;
    private Button wifiBtn;
    private Button lanBtn;
    private Button nlBtn;
    private Button ycBtn;
    private Button ipBtn;
    private Button appExtractBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        suffixBtn = (Button)findViewById(R.id.suffixBtn);
        qrySuBtn = (Button)findViewById(R.id.qrySuBtn);
        httpBtn = (Button)findViewById(R.id.httpBtn);
        copyBtn = (Button)findViewById(R.id.copyBtn);
        ftpBtn = (Button)findViewById(R.id.ftpBtn);
        wifiBtn = (Button)findViewById(R.id.wifiBtn);
        lanBtn = (Button)findViewById(R.id.lanBtn);
        nlBtn = (Button)findViewById(R.id.nlBtn);
        ycBtn = (Button)findViewById(R.id.ycBtn);
        ipBtn = (Button)findViewById(R.id.ipBtn);
        appExtractBtn = (Button)findViewById(R.id.appExtractBtn);

        suffixBtn.setOnClickListener(this);
        qrySuBtn.setOnClickListener(this);
        httpBtn.setOnClickListener(this);
        copyBtn.setOnClickListener(this);
        ftpBtn.setOnClickListener(this);
        wifiBtn.setOnClickListener(this);
        lanBtn.setOnClickListener(this);
        nlBtn.setOnClickListener(this);
        ycBtn.setOnClickListener(this);
        ipBtn.setOnClickListener(this);
        appExtractBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        if(v.getId() == R.id.suffixBtn){
            Log.i("su","后缀删添");
            intent.setClass(ToolsActivity.this, SuffixActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.qrySuBtn){
            Log.i("su","后缀列表");
            intent.setClass(ToolsActivity.this, QrySuffixActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.httpBtn){
            Log.i("su","http下载");
            intent.setClass(ToolsActivity.this, HttpActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.copyBtn){
            Log.i("su","快捷复制");
            intent.setClass(ToolsActivity.this, CopyActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.ftpBtn){
            Log.i("su","FTP下载");
            intent.setClass(ToolsActivity.this, FTPActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.wifiBtn){
            Log.i("su","Wifi密码");
            intent.setClass(ToolsActivity.this, WifiActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.lanBtn){
            Log.i("su","局域网设备");
            intent.setClass(ToolsActivity.this, LanActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.nlBtn){
            Log.i("su","支付宝获取能量");
            intent.setClass(ToolsActivity.this,NLActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.ycBtn){
            Log.i("su","yc调度模式切换");
            intent.setClass(ToolsActivity.this,YCActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.ipBtn){
            //判断是否开启定位服务
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(ok){
                Log.i("su","WifiIP静态/DHCP切换");
                intent.setClass(ToolsActivity.this,IPActivity.class);
                this.startActivity(intent);
            }else{
                Log.i("IPActivity","还未开启定位，请先开启服务!");
                ToolsUtil.showToast(this,"还未开启定位，请先开启服务!",3000);
            }
        }else if(v.getId() == R.id.appExtractBtn){
            Log.i("su","App提取");
            intent.setClass(ToolsActivity.this,AppExtractActivity.class);
            this.startActivity(intent);
        }
    }
}

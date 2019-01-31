package com.track.mytools.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.track.mytools.R;
import com.track.mytools.enums.AssetsEnum;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 功能主界面
 *
 */
public class ToolsActivity extends Activity implements OnClickListener{

    @BindView(R.id.suffixBtn)
    Button suffixBtn;

    @BindView(R.id.qrySuBtn)
    Button qrySuBtn;

    @BindView(R.id.httpBtn)
    Button httpBtn;

    @BindView(R.id.copyBtn)
    Button copyBtn;

    @BindView(R.id.ftpBtn)
    Button ftpBtn;

    @BindView(R.id.wifiBtn)
    Button wifiBtn;

    @BindView(R.id.lanBtn)
    Button lanBtn;

    @BindView(R.id.nlBtn)
    Button nlBtn;

    @BindView(R.id.ycBtn)
    Button ycBtn;

    @BindView(R.id.ipBtn)
    Button ipBtn;

    @BindView(R.id.appExtractBtn)
    Button appExtractBtn;

    @BindView(R.id.toolsFP)
    Switch toolsFP;

    public static boolean useFP;

    //两个危险权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean mHasPermission;

    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        ButterKnife.bind(this);

        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

        if(fingerprintManager.isHardwareDetected()){
            toolsFP.setChecked(useFP);
        }else{
            toolsFP.setEnabled(false);
        }

        if(!ToolsUtil.hasRoot()){
            wifiBtn.setEnabled(false);
            wifiBtn.setText(wifiBtn.getText()+"-未获取Root，无法正常使用");
        }

        if(!ToolsUtil.hasYC()){
            ycBtn.setEnabled(false);
            ycBtn.setText(ycBtn.getText()+"-未刷入YC调度，无法正常使用");
        }

        suffixBtn.setOnClickListener(this);//删除添加后缀
        qrySuBtn.setOnClickListener(this);//查询类型数量
        httpBtn.setOnClickListener(this);//http下载
        copyBtn.setOnClickListener(this);//快捷复制
        ftpBtn.setOnClickListener(this);//ftp下载
        wifiBtn.setOnClickListener(this);//WiFi密码
        lanBtn.setOnClickListener(this);//局域网搜索
        nlBtn.setOnClickListener(this);//支付宝获取能量
        ycBtn.setOnClickListener(this);//yc调度
        ipBtn.setOnClickListener(this);//切换ip
        appExtractBtn.setOnClickListener(this);//app提取

        toolsFP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String strVal = "";
                if (isChecked){
                    //开启
                    useFP = true;
                    strVal = "y";
                }else {
                    //关闭
                    useFP = false;
                    strVal = "n";
                }
                FileOutputStream oFile = null;
                try{
                    oFile = new FileOutputStream(new File(String.valueOf(AssetsEnum.ASSETS_PROPERTIES_PATH)));
                    Properties p = new Properties();
                    p.setProperty("isUseFinIdMou", strVal);
                    p.store(oFile, "");
                }catch(Exception e){

                }finally {
                    if(oFile!=null){
                        try{
                            oFile.close();
                        }catch(Exception e1){

                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        if(v.getId() == R.id.suffixBtn){
            Log.i("ToolsActivity_Log","后缀删添");
            intent.setClass(ToolsActivity.this, SuffixActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.qrySuBtn){
            Log.i("ToolsActivity_Log","后缀列表");
            intent.setClass(ToolsActivity.this, QrySuffixActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.httpBtn){
            Log.i("ToolsActivity_Log","http下载");
            intent.setClass(ToolsActivity.this, HttpActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.copyBtn){
            Log.i("ToolsActivity_Log","快捷复制");
            intent.setClass(ToolsActivity.this, CopyActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.ftpBtn){
            Log.i("ToolsActivity_Log","FTP下载");
            intent.setClass(ToolsActivity.this, FTPActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.wifiBtn){
            Log.i("ToolsActivity_Log","Wifi密码");
            intent.setClass(ToolsActivity.this, WifiActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.lanBtn){
            Log.i("ToolsActivity_Log","局域网设备");
            intent.setClass(ToolsActivity.this, LanActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.nlBtn){
            Log.i("ToolsActivity_Log","支付宝获取能量");
            intent.setClass(ToolsActivity.this,NLActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.ycBtn){
            Log.i("ToolsActivity_Log","yc调度模式切换");
            intent.setClass(ToolsActivity.this,YCActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.ipBtn){
            //判断有没有获取定位权限
            mHasPermission = checkPermission();
            if (!mHasPermission) {
                Log.i("ToolsActivity_Log","没有权限");
                requestLocationPermission();
            }else{
                Log.i("ToolsActivity_Log","拥有权限");
                checkLocation();
            }
        }else if(v.getId() == R.id.appExtractBtn){
            Log.i("ToolsActivity_Log","App提取");
            intent.setClass(ToolsActivity.this,AppExtractActivity.class);
            this.startActivity(intent);
        }
    }

    /**
     * 检查是否开启定位服务
     */
    private void checkLocation(){
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Intent intent = new Intent();
        if(ok){
            Log.i("ToolsActivity_Log","WifiIP静态/DHCP切换");
            intent.setClass(ToolsActivity.this,IPActivity.class);
            this.startActivity(intent);
        }else{
            Log.i("ToolsActivity_Log","还未开启定位，请先开启服务!");
            ToolsUtil.showToast(this,"还未开启定位，请先开启服务!",3000);
        }
    }

    /**
     * 检查是否已经授予定位权限
     * @return
     */
    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 请求定位权限
     *
     */
    public void requestLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(ToolsActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(ToolsActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(ToolsActivity.this, "自Android 6.0开始需要打开位置权限才可以搜索到WIFI设备", Toast.LENGTH_SHORT);

                }
                //请求权限
                ActivityCompat.requestPermissions(ToolsActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    //权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("ToolsActivity_Log","定位权限回调");
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意权限,执行我们的操作
            checkLocation();
        }else{//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
            ToolsUtil.showToast(ToolsActivity.this,"未开启定位权限,请手动到设置去开启权限",2000);
        }
    }
}

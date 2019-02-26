package com.track.mytools.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.fingerprint.FingerprintManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.track.mytools.R;
import com.track.mytools.adapter.ToolsMainAdapter;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.ToolsEntity;
import com.track.mytools.util.ToolsUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 功能主界面
 *
 */
public class ToolsActivity extends Activity{

    @BindView(R.id.toolsFP)
    Switch toolsFP;

    @BindView(R.id.toolsList)
    ListView toolsList;

    public static boolean useFP;//是否开启指纹

    public static boolean passFP = false;//是否通过指纹验证

    //两个危险权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean mHasPermission;

    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;

    public static Handler toolsActivityHandler = null;

    public static ToolsMainAdapter toolsMainAdapter;

    public static HashMap<String,HashMap<String,Object>> resMap = null;

    static{
        resMap = new HashMap<String,HashMap<String,Object>>();

        HashMap<String,Object> rMap1 = new HashMap<String,Object>();
        rMap1.put("btnValue",R.string.t1);
        rMap1.put("btnId",R.id.suffixBtn);
        resMap.put("suffixBtn",rMap1);

        HashMap<String,Object> rMap2 = new HashMap<String,Object>();
        rMap2.put("btnValue",R.string.t2);
        rMap2.put("btnId",R.id.qrySuBtn);
        resMap.put("qrySuBtn",rMap2);

        HashMap<String,Object> rMap3 = new HashMap<String,Object>();
        rMap3.put("btnValue",R.string.t3);
        rMap3.put("btnId",R.id.httpBtn);
        resMap.put("httpBtn",rMap3);

        HashMap<String,Object> rMap4 = new HashMap<String,Object>();
        rMap4.put("btnValue",R.string.t4);
        rMap4.put("btnId",R.id.copyBtn);
        resMap.put("copyBtn",rMap4);

        HashMap<String,Object> rMap5 = new HashMap<String,Object>();
        rMap5.put("btnValue",R.string.t5);
        rMap5.put("btnId",R.id.ftpBtn);
        resMap.put("ftpBtn",rMap5);

        HashMap<String,Object> rMap6 = new HashMap<String,Object>();
        rMap6.put("btnValue",R.string.t6);
        rMap6.put("btnId",R.id.wifiBtn);
        resMap.put("wifiBtn",rMap6);

        HashMap<String,Object> rMap7 = new HashMap<String,Object>();
        rMap7.put("btnValue",R.string.t7);
        rMap7.put("btnId",R.id.lanBtn);
        resMap.put("lanBtn",rMap7);

        HashMap<String,Object> rMap8 = new HashMap<String,Object>();
        rMap8.put("btnValue",R.string.t8);
        rMap8.put("btnId",R.id.nlBtn);
        resMap.put("nlBtn",rMap8);

        HashMap<String,Object> rMap9 = new HashMap<String,Object>();
        rMap9.put("btnValue",R.string.t9);
        rMap9.put("btnId",R.id.ycBtn);
        resMap.put("ycBtn",rMap9);

        HashMap<String,Object> rMap10 = new HashMap<String,Object>();
        rMap10.put("btnValue",R.string.t10);
        rMap10.put("btnId",R.id.ipBtn);
        resMap.put("ipBtn",rMap10);

        HashMap<String,Object> rMap12 = new HashMap<String,Object>();
        rMap12.put("btnValue",R.string.t12);
        rMap12.put("btnId",R.id.appExtractBtn);
        resMap.put("appExtractBtn",rMap12);

        HashMap<String,Object> rMap13 = new HashMap<String,Object>();
        rMap13.put("btnValue",R.string.t13);
        rMap13.put("btnId",R.id.pwdBtn);
        resMap.put("pwdBtn",rMap13);

        HashMap<String,Object> rMap14 = new HashMap<String,Object>();
        rMap14.put("btnValue",R.string.t14);
        rMap14.put("btnId",R.id.qrCodeBtn);
        resMap.put("qrCodeBtn",rMap14);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        ButterKnife.bind(this);

        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

        //检测是否存在指纹模块
        if(fingerprintManager.isHardwareDetected()){
            toolsFP.setChecked(useFP);
        }else{
            toolsFP.setEnabled(false);
        }

        //检测是否是非法页面跳转
        if(!ToolsUtil.isLegal()){
            ToolsUtil.showToast(ToolsActivity.this,"非法页面跳转",5000);
            finish();
        }

        toolsActivityHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(msg.obj == IPActivity.class){
                    mHasPermission = checkPermission();
                    if (!mHasPermission) {
                        Log.i("ToolsActivity_Log","没有权限");
                        requestLocationPermission();
                    }else{
                        Log.i("ToolsActivity_Log","拥有权限");
                        checkLocation();
                    }
                }else{
                    Intent intent = new Intent();
                    intent.setClass(ToolsActivity.this, (Class)msg.obj);
                    startActivity(intent);
                }
            }
        };

        SQLiteDatabase sqd = ToolsDao.getDatabase();
        List<HashMap<String,Object>> list = ToolsDao.qryTable(sqd, ToolsEntity.class,this);
        int i = 0;
        List<Integer> passList = new ArrayList<Integer>();
        for (int j = 0; j < list.size(); j++) {
            HashMap<String,Object> map = list.get(j);
            String btnName = map.get("btnName").toString();
            String btnUse = map.get("btnUse").toString();

            map.put("btnValue",resMap.get(btnName).get("btnValue"));
            map.put("btnId",resMap.get(btnName).get("btnId"));
            map.put("id",i);
            if("n".equalsIgnoreCase(btnUse)){
                passList.add(j);
                Log.i("ToolsActivity_Log","去除功能:" + btnName);
            }else {
                i++;
            }
        }

        Collections.reverse(passList);

        for(Integer x:passList){
            list.remove(x.intValue());
        }

        Log.i("ToolsActivity_Log","最终功能:" + list);

        toolsMainAdapter = new ToolsMainAdapter(this,list);

        toolsList.setAdapter(toolsMainAdapter);

        //监听是否使用指纹首屏
        toolsFP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String strVal = "";
                if (isChecked){
                    //开启
                    useFP = true;
                    strVal = "y" ;
                }else {
                    //关闭
                    useFP = false;
                    strVal = "n";
                }
                ToolsUtil.setProperties(strVal);
            }
        });
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
            intent.setClass(ToolsActivity.this, IPActivity.class);
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

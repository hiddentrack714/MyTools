package com.track.mytools.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Icon;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.PwdEntity;
import com.track.mytools.entity.ShortCutEntity;
import com.track.mytools.enums.AssetsEnum;
import com.track.mytools.util.FingerprintUtil;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    private static String[] PERMISSIONS_CAMERA_AND_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @BindView(R.id.warnTitle)
    TextView warnTitle;

    private static boolean dbReady = false;
    private static boolean proReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //首先动态申请存储访问权限
        if(isGrantExternalRW(this, 0xfa02)){
            mainActivityMission();
        }
    }

    /**
     * 分析pro文件，判断跳转还是验证指纹
     */
    private void analysispro(){
        //加载初始文件
        Properties pro = new Properties();
        try{
            pro = loadConfig(this, String.valueOf(AssetsEnum.ASSETS_PROPERTIES_PATH));

            String isUseFinIdMou = pro.getProperty("isUseFinIdMou");

            Log.i("MainActivity_Log","是否开启指纹:" + isUseFinIdMou);

            ToolsActivity.useFP = "y".equalsIgnoreCase(isUseFinIdMou) ? true : false;

            //是否需要开启指纹识别
            if("y".equalsIgnoreCase(isUseFinIdMou)){
                //开启指纹识别
                ToolsActivity.useFP = true;
                setContentView(R.layout.activity_main);
                ButterKnife.bind(this);
                checkFiger();
            }else{
                //检查密码本是否有密码存储
                SQLiteDatabase sqd = ToolsDao.getDatabase();
                if(ToolsDao.qryTable(sqd, PwdEntity.class,this).size() > 0){

                    ToolsActivity.useFP = true;
                    setContentView(R.layout.activity_main);
                    ButterKnife.bind(this);
                    checkFiger();
                    ToolsUtil.setProperties("y");

                    ToolsUtil.showToast(this,"密码本存有密码,为保障安全,需要进行指纹识别!",2000);
                }else{

                    //关闭指纹识别,直接跳转到下一级
                    ToolsActivity.useFP = false;
                    Intent intent = new Intent();
                    intent.setClass(this, ToolsActivity.class);
                    this.startActivity(intent);
                    this.finish();
                }
            }

        }catch(Exception e){
            Log.e("MainActivity_Log","获取初始化文件失败");
            ToolsUtil.showToast(this, "获取初始化文件失败", 2000);
        }

    }

    /**
     * 动态添加shortcut
     */
    private void dynamicAddShortCut(){
        List<ShortCutEntity> list = new ArrayList<ShortCutEntity>();

        list.add(new ShortCutEntity("powersave","省电-powersave",YCTempActivity.class,R.drawable.mode4));

        list.add(new ShortCutEntity("balance","平衡-balance",YCTempActivity.class,R.drawable.mode3));

        list.add(new ShortCutEntity("performance","性能-performance",YCTempActivity.class,R.drawable.mode2));

        list.add(new ShortCutEntity("fast","极速-fast",YCTempActivity.class,R.drawable.mode1));

        dynamicAddShortCut(list);
    }

    /**
     * 检查指纹
     * @return
     */
    public void checkFiger(){
        FingerprintManager fm = (FingerprintManager)this.getSystemService(Service.FINGERPRINT_SERVICE);

        int fingerGrant = checkSelfPermission(Manifest.permission.USE_FINGERPRINT );

        //动态检测权限
        if(PackageManager.PERMISSION_GRANTED != fingerGrant){

            warnTitle.setText("没有权限访问指纹");
            warnTitle.setVisibility(TextView.VISIBLE);
            return;
        }

        //检查是否有指纹模块和是否有录入
        if(!fm.isHardwareDetected() || !fm.isHardwareDetected()){

            warnTitle.setText("没有指纹模块，或者至少应该有一个指纹");
            warnTitle.setVisibility(TextView.VISIBLE);
            return;
        }

        CancellationSignal mCancellationSignal  = new CancellationSignal();
        //检测指纹是否匹配
        try {
            fm.authenticate(null, mCancellationSignal, 0 /* flags */,new FingerprintUtil(this,warnTitle), null);
        }catch(Exception e){

        }

    }

    /**
     * 加载properties
     * @param context
     * @param file
     * @return
     */
    public Properties loadConfig(Context context, String file) {
        Properties properties = new Properties();
        try {
            //以字符流的返回时读取properties
            Reader s = new FileReader(file);
            properties.load(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * 动态添加shortcut
     * @param list shortcut属性
     */
    public void dynamicAddShortCut(List<ShortCutEntity> list){
        List<ShortcutInfo> li = new ArrayList<ShortcutInfo>();
        int i = 0;
        for (ShortCutEntity sce : list){
            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(this, "shortcut_id_like" + i)
                    .setShortLabel(sce.getShortName())
                    .setLongLabel(sce.getLongName())
                    .setIcon(Icon.createWithResource(this, sce.getIconName()))  //默认图片
                    //.setIntent(new Intent(this, HttpActivity.class))   error
                    .setIntent(new Intent(Intent.ACTION_VIEW,Uri.parse(sce.getShortName())).setClass(this, sce.getCla()))//intent必须设置action
                    .build();
            li.add(shortcutInfo);
            i++;
        }

        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        //这样就可以通过长按图标显示出快捷方式了
        shortcutManager.setDynamicShortcuts(li);
    }

    /**
     * 初始化复制文件到手机磁盘
     * @param filePath 文件所在全路径
     * @param fileName 文件名
     */
    public static HashMap<String,Object> initCopyFile(String filePath , String fileName, Context context) {
        FileOutputStream out = null;
        InputStream in = null;

        HashMap<String,Object> map = new HashMap<String,Object>();

        String fileDirStr = filePath.substring(0,filePath.lastIndexOf("/"));
        File fileDir = new File(fileDirStr);
        try {
            //检查文件目录是否存在
            if(!fileDir.exists()){
                Log.i("MainActivity_Log","创建目录:" + fileDirStr);
                boolean isSuccess = fileDir.mkdirs();
                Log.i("MainActivity_Log","创建目录:" + (isSuccess==true?"成功":"失败"));
                if(isSuccess == false){
                    map.put("b",false);
                    map.put("i","创建目录["+fileDir.getAbsolutePath()+"],失败");
                    return map;
                }
            }

            out = new FileOutputStream(filePath);
            in = context.getAssets().open(fileName);
            byte[] buffer = new byte[512];
            int readBytes = 0;
            while ((readBytes = in.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
            }
            out.flush();

            if(new File(filePath).exists()){
                map.put("b",true);
                return map;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity_Log",e.getMessage());
            map.put("b",false);
            map.put("i",e.getMessage());
            return map;
        }finally {
            try{
                if(in!=null){
                    in.close();
                }
                if(out!=null){
                    out.close();
                }
            }catch(Exception e){
            }
        }
        return map;
    }

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int storagePermission = activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            //检测是否有权限，如果没有权限，就需要申请
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                //申请权限
                activity.requestPermissions(PERMISSIONS_CAMERA_AND_STORAGE, requestCode);
                //返回false。说明没有授权
                return false;
            }
        }
        //说明已经授权
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0xfa02:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mainActivityMission();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(MainActivity.this, "buxing", Toast.LENGTH_SHORT).show();
                            ToolsUtil.showToast(MainActivity.this,"请授予存储访问权限,再使用该应用",5000);
                            finish();
                        }
                    });
                }
                break;
        }
    }


    public void mainActivityMission(){
        //检验是否获取权限，如果获取权限，外部存储会处于开放状态，会弹出一个toast提示获得授权
        String sdCard = Environment.getExternalStorageState();
        if (sdCard.equals(Environment.MEDIA_MOUNTED)){
            //添加ShortCut
            dynamicAddShortCut();
            //检测是否把db和properties文件复制到手机磁盘
            File dbFile = new File(String.valueOf(AssetsEnum.ASSETS_DB_PATH));
            File proFile = new File(String.valueOf(AssetsEnum.ASSETS_PROPERTIES_PATH));

            if(dbFile.exists() && proFile.exists()){
                //db和 pro文件都存在，直接分析
                Log.i("MainActivity_Log","双文件都在，可以分析" );
                analysispro();
            }else{
                if(!dbFile.exists()){
                    Log.i("MainActivity_Log","db文件不在，开始复制" );
                    HashMap<String,Object> isSuccess = initCopyFile(String.valueOf(AssetsEnum.ASSETS_DB_PATH),"mytools.db",MainActivity.this);
                    if(isSuccess.size()>=1){
                        if((boolean)isSuccess.get("b") == false){
                            ToolsUtil.showToast(this,"数据库复制失败!-" + isSuccess.get("i"),5000);
                            finish();
                        }else{
                            Log.i("MainActivity_Log","db复制成功" );
                            dbReady = true;
                        }
                    }else{
                        ToolsUtil.showToast(this,"数据库复制失败!",5000);
                    }
                }else{
                    dbReady = true;
                }

                if (!proFile.exists()) {
                    Log.i("MainActivity_Log","pro文件不在，开始复制" );
                    HashMap<String,Object> isSuccess = initCopyFile(String.valueOf(AssetsEnum.ASSETS_PROPERTIES_PATH), "mytools.properties",MainActivity.this);
                    if(isSuccess.size()>=1){
                        if((boolean)isSuccess.get("b") == false){
                            ToolsUtil.showToast(this, "参数文件复制失败!-" + isSuccess.get("i"), 5000);
                            finish();
                        }else{
                            Log.i("MainActivity_Log","pro复制成功" );
                            proReady = true;
                        }
                    }else{
                        ToolsUtil.showToast(this, "参数文件复制失败!", 5000);
                    }

                }else{
                    proReady = true;
                }

                if(dbReady && proReady){
                    Log.i("MainActivity_Log","文件复制完成，可以分析" );
                    analysispro();
                }else{
                    ToolsUtil.showToast(this,"文件复制失败，请删除后重试!",5000);
                    finish();
                }
            }

        }
    }

}

package com.track.mytools.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.TextView;

import com.track.mytools.R;
import com.track.mytools.entity.ShortCutEntity;
import com.track.mytools.util.FingerprintUtil;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MainActivity extends Activity {

    public static String ASSETS_DB_PATH = "/sdcard/Android/data/com.track.mytools/databases/mytools.db";

    public static String ASSETS_PROPERTIES_PATH = "/sdcard/Android/data/com.track.mytools/properties/mytools.properties";

    public TextView warnTitle;
    //shortcut模块
    private ShortcutManager shortcutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //检测是否把db和properties文件复制到手机磁盘
        if(!new File(ASSETS_DB_PATH).exists()){
            if(!initCopyFile(ASSETS_DB_PATH,"mytools.db")){
                ToolsUtil.showToast(this,"数据库复制失败!",5000);
                finish();
            }
        }

        if(!new File(ASSETS_PROPERTIES_PATH).exists()){
            if(!initCopyFile(ASSETS_PROPERTIES_PATH,"mytools.properties")){
                ToolsUtil.showToast(this,"参数文件复制失败!",5000);
                finish();
            }
        }

        warnTitle = (TextView)findViewById(R.id.warnTitle);

        List<ShortCutEntity> list = new ArrayList<ShortCutEntity>();

        //list.add(new ShortCutEntity("http下载","http下载",HttpActivity.class));

        list.add(new ShortCutEntity("powersave","省电-powersave",YCTempActivity.class,R.drawable.mode4));

        list.add(new ShortCutEntity("balance","平衡-balance",YCTempActivity.class,R.drawable.mode3));

        list.add(new ShortCutEntity("performance","性能-performance",YCTempActivity.class,R.drawable.mode2));

        list.add(new ShortCutEntity("fast","极速-fast",YCTempActivity.class,R.drawable.mode1));

        dynamicAddShortCut(list);

        //加载初始文件
        Properties pro = new Properties();

        String proFilePath = ASSETS_PROPERTIES_PATH;

        File proFile = new File(proFilePath);

        if(!proFile.exists()){
            Log.e("su","参数文件不存在");
            ToolsUtil.showToast(this, "参数文件不存在", 2000);
        }else{
            try{
                pro = loadConfig(this,proFilePath);

                String isUseFinIdMou = pro.getProperty("isUseFinIdMou");

                //是否需要开启指纹识别
                if("y".equalsIgnoreCase(isUseFinIdMou)){
                    //开启指纹识别
                    checkFiger();
                }else{
                    //关闭指纹识别,直接跳转到下一级
                    Intent intent = new Intent();
                    intent.setClass(this, ToolsActivity.class);
                    this.startActivity(intent);
                    this.finish();
                }
            }catch(Exception e){
                Log.e("su","获取初始化文件失败...");
                ToolsUtil.showToast(this, "获取初始化文件失败...", 2000);
            }
        }
    }

    /**
     * 检查指纹
     * @return
     */
    public void checkFiger(){
        FingerprintManager fm = (FingerprintManager)this.getSystemService(Service.FINGERPRINT_SERVICE);

        int fingerGrant = checkSelfPermission(Manifest.permission.USE_FINGERPRINT );
        // Log.i("test", "----------->" + fingerGrant);
        //动态检测权限
        if(PackageManager.PERMISSION_GRANTED != fingerGrant){
            //ToolsUtil.showToast(this,"没有权限访问指纹",1000);
           // return false;
            warnTitle.setText("没有权限访问指纹");
            warnTitle.setVisibility(TextView.VISIBLE);
            return;
        }

        //检查是否有指纹模块和是否有录入
        if(!fm.isHardwareDetected() || !fm.isHardwareDetected()){
            //ToolsUtil.showToast(this,"没有指纹模块，或者至少应该有一个指纹",1000);
            //return false;
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
        //return true;
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
     * @param filePath
     * @param fileName
     */
    public boolean initCopyFile(String filePath ,String fileName) {
        FileOutputStream out = null;
        InputStream in = null;

        String fileDirStr = filePath.substring(0,filePath.lastIndexOf("/"));
        File fileDir = new File(fileDirStr);
        if(!fileDir.exists()){
            Log.i("MainActivity","创建目录:" + fileDirStr);
            boolean isSuccess = fileDir.mkdirs();
            Log.i("MainActivity","创建目录:" + (isSuccess==true?"成功":"失败"));
            return isSuccess;
        }

        try {
            out = new FileOutputStream(filePath);
            in = getAssets().open(fileName);
            byte[] buffer = new byte[1024];
            int readBytes = 0;
            while ((readBytes = in.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
            }
            out.flush();
            if(new File(filePath).exists()){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity",e.getMessage());
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
        return false;
    }

}

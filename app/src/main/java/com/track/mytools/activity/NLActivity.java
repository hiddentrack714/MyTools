package com.track.mytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.track.mytools.R;
import com.track.mytools.Service.NLService;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.NLEntity;
import com.track.mytools.until.ToolsUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 *
 *
 * 获取支付宝能量
 *
 */

public class NLActivity extends Activity {

    private EditText nlX;  //蚂蚁森林图标x轴位置
    private EditText nlY;  //蚂蚁森林图标y线位置
    private EditText ballX;  //能量球x轴位置
    private EditText ballY;  //能量球y线位置
    private Button nlStartBtn;   //开启服务
    private EditText nlHour;     //时
    private EditText nlMinute;   //分
    private CheckBox nlCheck;    //是否是收获日当天
    private EditText nlClick;    //能量球点击次数
    private Button nlUpdBtn;  //修改按钮

    private EditText nlDiffHour;
    private EditText nlDiffMinute;
    private EditText nlDiffSecond;

    private static boolean isStart = false; // 服务是否启动
    private static boolean isNow;   // 是否是成熟日当天
    private static boolean isUpd= false;   // 是否修改中

    private int hourInt;
    private int minuteInt;

    public static Long diffTime; //时间差值

    public static NLActivity nl;

    public static NLEntity nle;

    private static Intent intentService;

    private PowerManager.WakeLock wakeLock;

    public static Handler handler;

    private static int lightNum;  //屏幕亮度

    public static int clickTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        nl = this;

        setContentView(R.layout.activity_nl);

        lightNum = getScreenBrightness(); //获取屏幕亮度

        nlX = (EditText)findViewById(R.id.nlX);
        nlY = (EditText)findViewById(R.id.nlY);
        nlHour = (EditText)findViewById(R.id.nlHour);
        nlMinute = (EditText)findViewById(R.id.nlMinute);
        nlStartBtn = (Button)findViewById(R.id.nlStartBtn);
        nlCheck  = (CheckBox)findViewById(R.id.nlCheck);
        ballX = (EditText)findViewById(R.id.ballX);
        ballY = (EditText)findViewById(R.id.ballY);
        nlClick = (EditText)findViewById(R.id.nlClick);
        nlUpdBtn = (Button)findViewById(R.id.nlUpdBtn);

        nlDiffHour = (EditText)findViewById(R.id.nlDiffHour);
        nlDiffMinute = (EditText)findViewById(R.id.nlDiffMinute);
        nlDiffSecond = (EditText)findViewById(R.id.nlDiffSecond);

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> map =  ToolsDao.qryTable(sdb,NLEntity.class).get(0);

        nlX.setText(map.get("nlX").toString());
        nlY.setText(map.get("nlY").toString());
        ballX.setText(map.get("ballX").toString());
        ballY.setText(map.get("ballY").toString());
        nlHour.setText(map.get("nlHour").toString());
        nlMinute.setText(map.get("nlMinute").toString());
        nlClick.setText(map.get("nlClick").toString());

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                nlDiffHour.setText(((NLService.NLTime)msg.obj).getHour());
                nlDiffMinute.setText(((NLService.NLTime)msg.obj).getMinute());
                nlDiffSecond.setText(((NLService.NLTime)msg.obj).getSecond());
            }
        };

        //开启服务按钮监听
        nlStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nle = new NLEntity(nlX,nlY,ballX,ballY);

                Log.i("NLActivity2",lightNum+"");

                if(isStart == false){
                    //1,计算到达时间
                    isNow = nlCheck.isChecked();

                    clickTime = Integer.parseInt(nlClick.getText().toString());

                    hourInt = Integer.parseInt(nlHour.getText().toString());

                    minuteInt = Integer.parseInt(nlMinute.getText().toString());

                    Calendar cal =  Calendar.getInstance();
                    Date d = new Date();

                    Long growTime; //成熟日

                    int year = cal.getWeekYear(); // 年份
                    int month = d.getMonth(); // 月份
                    int date = d.getDate();  //日

                    if(isNow){
                        //成熟日当天
                        cal.set(year, month, date, hourInt, minuteInt,00);

                        growTime = cal.getTime().getTime();
                    }else{
                        //成熟前一天
                        cal.add(cal.DAY_OF_WEEK, 1);

                        Date countDate = cal.getTime();

                        year = cal.getWeekYear(); // 年份
                        month = countDate.getMonth(); // 月
                        date = countDate.getDate();  //日

                        cal.set(year, month, date, hourInt, minuteInt);

                        growTime = cal.getTime().getTime();
                    }

                    diffTime = growTime - System.currentTimeMillis();

                    Log.i("NLActivity1",diffTime + "");

                    if(diffTime <= 0){
                        //差值计算错误；
                        ToolsUtil.showToast(NLActivity.this,"时间差值计算错误",3000);
                    }else{
                        //2,开启服务
                        intentService = new Intent(NLActivity.this,NLService.class);
                        Log.i("NLActivity","开启服务");
                        startService(intentService);

                        nlStartBtn.setText("服务启动中，点击停止");

                        isStart = true;

                        //4,降低屏幕亮度
                        setScreenBrightness(0);
                    }

                    //3，点击后程序进入后台
//                    Intent intent= new Intent(Intent.ACTION_MAIN);
//
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //如果是服务里调用，必须加入new task标识    
//
//                    intent.addCategory(Intent.CATEGORY_HOME);
//
//                    startActivity(intent);

                }else{
                    nlStartBtn.setText("开启服务");

                    isStart = false;

                    stopService(intentService);

                    NLService.handlerTime.removeCallbacks(NLService.myThread);

                    setScreenBrightness(lightNum);
                }
            }
        });

        //修改按键监听
        nlUpdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isUpd == false){
                    //准备修改
                    nlX.setEnabled(true); //取消只读模式

                    nlY.setEnabled(true); //取消只读模式

                    ballX.setEnabled(true); //取消只读模式

                    ballY.setEnabled(true); //取消只读模式

                    nlHour.setEnabled(true); //取消只读模式

                    nlMinute.setEnabled(true); //取消只读模式

                    nlClick.setEnabled(true); //取消只读模式

                    nlUpdBtn.setText("完成");

                    isUpd = true;
                }else{
                    //修改完成
                    nlX.setEnabled(false); //只读模式

                    nlY.setEnabled(false); //只读模式

                    ballX.setEnabled(false); //只读模式

                    ballY.setEnabled(false); //只读模式

                    nlHour.setEnabled(false); //只读模式

                    nlMinute.setEnabled(false); //只读模式

                    nlClick.setEnabled(false); //只读模式

                    nlUpdBtn.setText("修改");

                    isUpd = false;

                    //更新数据库
                    SQLiteDatabase sdb = ToolsDao.getDatabase();

                    HashMap<String,Object> dataMap = new HashMap<String,Object>();

                    dataMap.put("nlX",nlX.getText().toString());
                    dataMap.put("nlY",nlY.getText().toString());
                    dataMap.put("ballX",ballX.getText().toString());
                    dataMap.put("ballY",ballY.getText().toString());
                    dataMap.put("nlHour",nlHour.getText().toString());
                    dataMap.put("nlMinute",nlMinute.getText().toString());
                    dataMap.put("nlClick",nlClick.getText().toString());
                    dataMap.put("id",map.get("id"));

                    ToolsDao.saveOrUpdIgnoreExsit(sdb,dataMap,NLEntity.class);
                }
            }
        });

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ON_AFTER_RELEASE, this.getClass().getName());
        wakeLock.acquire();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (wakeLock != null) {
            wakeLock.release();
        }
        super.onPause();
    }

    /**
     * 设置当前屏幕亮度值  0--255
     */
    private void saveScreenBrightness(int paramInt){
        try{
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, paramInt);
        }
        catch (Exception localException){
            localException.printStackTrace();
        }
    }

    /**
     * 保存当前的屏幕亮度值，并使之生效
     */
    private void setScreenBrightness(int paramInt){
        Window localWindow = getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        float f = paramInt / 255.0F;
        localLayoutParams.screenBrightness = f;
        localWindow.setAttributes(localLayoutParams);
    }

    /**
     * 获得当前屏幕亮度值  0--255
     */
    private int getScreenBrightness(){
        int screenBrightness=255;
        try{
            screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        }
        catch (Exception localException){

        }
        return screenBrightness;
    }

}

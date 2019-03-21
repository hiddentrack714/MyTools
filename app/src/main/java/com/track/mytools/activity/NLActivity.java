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
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.NLEntity;
import com.track.mytools.service.NLService;
import com.track.mytools.util.ToolsUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 *
 * 获取支付宝能量
 *
 */

public class NLActivity extends Activity {

    @BindView(R.id.nlX)
    EditText nlX;  //蚂蚁森林图标x轴位置

    @BindView(R.id.nlY)
    EditText nlY;  //蚂蚁森林图标y线位置

    @BindView(R.id.ballX)
    EditText ballX;  //能量球x轴位置

    @BindView(R.id.ballY)
    EditText ballY;  //能量球y线位置

    @BindView(R.id.nlStartBtn)
    Button nlStartBtn;   //开启服务

    @BindView(R.id.nlHour)
    EditText nlHour;     //时

    @BindView(R.id.nlMinute)
    EditText nlMinute;   //分

    @BindView(R.id.nlCheck)
    CheckBox nlCheck;    //是否是收获日当天

    @BindView(R.id.nlClick)
    EditText nlClick;    //能量球点击次数

    @BindView(R.id.nlUpdBtn)
    Button nlUpdBtn;  //修改按钮

    @BindView(R.id.nlDiffHour)
    EditText nlDiffHour;

    @BindView(R.id.nlDiffMinute)
    EditText nlDiffMinute;

    @BindView(R.id.nlDiffSecond)
    EditText nlDiffSecond;

    private static boolean isStart = false; // 服务是否启动
    private static boolean isNow;   // 是否是成熟日当天
    private boolean isUpd= false;   // 是否修改中

    private int hourInt;
    private int minuteInt;

    public static Long diffTime; //时间差值

    public static NLEntity []nleArray;

    private static Intent intentService;

    private PowerManager.WakeLock wakeLock;

    public static Handler nlActivityHandler;

    private static int lightNum;  //屏幕亮度

    public static int clickTime; //点击数次

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nl);
        ButterKnife.bind(this);

        lightNum = getScreenBrightness(); //获取屏幕亮度

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> map =  ToolsDao.qryTable(sdb,NLEntity.class,NLActivity.this).get(0);

        nlX.setText(map.get("nlX").toString());
        nlY.setText(map.get("nlY").toString());
        ballX.setText(map.get("ballX").toString());
        ballY.setText(map.get("ballY").toString());
        nlHour.setText(map.get("nlHour").toString());
        nlMinute.setText(map.get("nlMinute").toString());
        nlClick.setText(map.get("nlClick").toString());

        nlActivityHandler = new Handler(){
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

                String nlXStr = nlX.getText().toString();
                String nlYStr = nlY.getText().toString();
                String ballXStr = ballX.getText().toString();
                String ballYStr = ballY.getText().toString();
                String nlHourStr = nlHour.getText().toString();
                String nlMinuteStr = nlMinute.getText().toString();
                String nlClickStr = nlClick.getText().toString();

                if("".equals(nlXStr)){
                    ToolsUtil.showToast(NLActivity.this,"蚂蚁森林X不能为空",2000);
                    return;
                }

                if("".equals(nlYStr)){
                    ToolsUtil.showToast(NLActivity.this,"蚂蚁森林Y不能为空",2000);
                    return;
                }

                if("".equals(ballXStr)){
                    ToolsUtil.showToast(NLActivity.this,"能量球X不能为空",2000);
                    return;
                }

                if("".equals(ballYStr)){
                    ToolsUtil.showToast(NLActivity.this,"能量球Y不能为空",2000);
                    return;
                }

                if("".equals(nlHourStr)){
                    ToolsUtil.showToast(NLActivity.this,"成熟时间小时不能为空",2000);
                    return;
                }

                if("".equals(nlMinuteStr)){
                    ToolsUtil.showToast(NLActivity.this,"成熟时间分钟不能为空",2000);
                    return;
                }

                if("".equals(nlClickStr)){
                    ToolsUtil.showToast(NLActivity.this,"点击次数不能为空",2000);
                    return;
                }

                //由于能量球位置可能变化，所以列出球可能出现的位置；

                int xLen = map.get("ballX").toString().split(",").length;

                int yLen = map.get("ballY").toString().split(",").length;

                if(xLen == yLen && xLen > 1 && yLen>1){
                    //多位置情况
                    nleArray = new NLEntity[xLen];
                    for(int i=0;i<xLen;i++){
                        nleArray[i] = new NLEntity(Integer.parseInt(map.get("nlX").toString()), Integer.parseInt(map.get("nlY").toString()), Integer.parseInt(map.get("ballX").toString().split(",")[i]), Integer.parseInt(map.get("ballY").toString().split(",")[i]));
                    }

                }else{
                    nleArray = new NLEntity[1];

                    nleArray[0] = new NLEntity(Integer.parseInt(map.get("nlX").toString()), Integer.parseInt(map.get("nlY").toString()), Integer.parseInt(map.get("ballX").toString()), Integer.parseInt(map.get("ballY").toString()));

                }

                Log.i("NLActivity_Log",lightNum+"");

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

                    Log.i("NLActivity_Log",diffTime + "");

                    if(diffTime <= 0){
                        //差值计算错误；
                        ToolsUtil.showToast(NLActivity.this,"时间差值计算错误",3000);
                    }else{
                        //2,开启服务
                        intentService = new Intent(NLActivity.this,NLService.class);
                        Log.i("NLActivity_Log","开启服务");
                        startService(intentService);

                        nlStartBtn.setText("服务启动中，点击停止");
                        nlUpdBtn.setEnabled(false);
                        nlCheck.setEnabled(false);

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
                    nlUpdBtn.setEnabled(true);
                    nlCheck.setEnabled(true);

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

                    nlStartBtn.setEnabled(false);

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

                    nlUpdBtn.setText("修改参数");

                    nlStartBtn.setEnabled(true);

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

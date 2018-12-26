package com.track.mytools.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.track.mytools.activity.NLActivity;
import com.track.mytools.until.ToolsUtil;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class NLService extends Service implements Runnable {

    public static Handler handlerMain = new Handler();

    public static Handler handlerTime = new Handler();

    public static Long lastSecond;

    public static MyThread myThread;

    @Override
    public void onCreate() {
        Log.i("NLService1", "进入服务");

        handlerMain.postDelayed(this, NLActivity.diffTime);//每两秒执行一次runnable.

        lastSecond = NLActivity.diffTime;

        myThread = new MyThread();

        handlerTime.postDelayed(myThread,1000);

        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * 执行shell命令
     *
     * @param cmd
     */
    private void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            //开启定时后等待启动服务
            //1,打开支付宝
            Log.i("NLService2", "打开支付宝");
            String commod[] = {"am start -n com.eg.android.AlipayGphone/com.eg.android.AlipayGphone.AlipayLogin"};
            ToolsUtil.exeCommod(commod);
            Log.i("NLService3", "打开支付宝完成");
            //2,点亮屏幕
            //                        Process process = null;
            //                        DataOutputStream dos = null;
            //                        try {
            //                            process = Runtime.getRuntime().exec("su");
            //                            dos = new DataOutputStream(process.getOutputStream());
            //                            dos.writeBytes("input keyevent 224" + "\n");
            //                            dos.writeBytes("exit\n");
            //                            dos.flush();
            //                        }catch (Exception e){
            //                            Log.e("NLService4",e.getMessage());
            //                        }finally {
            //                            try {
            //                                if (dos != null) {
            //                                    dos.close();
            //                                }
            //                                process.destroy();
            //                            } catch (Exception e) {
            //                            }
            //                        }
            //3，进入蚂蚁森林
            try {
                Thread.sleep(5000);
            } catch (Exception e) {

            }

            Log.i("NLService4", "准备进入蚂蚁森林");
            //                    Instrumentation inst = new Instrumentation();
            //                    inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),
            //                            MotionEvent.ACTION_DOWN, 655, 1050, 0));
            //                    inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),
            //                            MotionEvent.ACTION_UP, 655, 1050, 0));
            execShellCmd("input tap " + NLActivity.nleArray[0].getNlX() + " " + NLActivity.nleArray[0].getNlY());

            Log.i("NLService5", "完成进入蚂蚁森林");

            try {
                Thread.sleep(5000);
            } catch (Exception e) {

            }

            Log.i("NLService6", "开始点击能量球");

            for (int i = 0; i < NLActivity.clickTime; i++) {
                Log.i("NLService6", i + "");
                for(int j= 0 ;j<NLActivity.nleArray.length;j++){
                    execShellCmd("input tap " + NLActivity.nleArray[j].getBallX() + " " + NLActivity.nleArray[j].getBallY());
                    Thread.sleep(500);
                }
                Thread.sleep(500);
            }

            Log.i("NLService7", "结束点击能量球");

            handlerMain.removeCallbacks(this);

            String commod1[] = {"am force-stop com.eg.android.AlipayGphone"}; //关闭支付宝

            ToolsUtil.exeCommod(commod1);

            String commod2[] = {"input keyevent 26"};//关闭屏幕

            ToolsUtil.exeCommod(commod2);

        } catch (Exception e) {
            Log.e("NLActivity", e.getMessage());
        }
    }

    /**
     * 将秒数转换为日时分秒，
     * @param second
     * @return
     */
    public static NLService.NLTime secondToTime(long second){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String hms = formatter.format(second);
        String time[] = hms.split(":");
        return new NLService.NLTime(time[0],time[1],time[2]);
    }

    public static class NLTime{
        private String hour;
        private String minute;
        private String second;

        public NLTime(String hour, String minute, String second) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

        public String getHour() {
            return hour;
        }

        public String getMinute() {
            return minute;
        }

        public String getSecond() {
            return second;
        }
    }

    static class MyThread extends Thread{
        @Override
        public void run() {
            //秒计算位时分秒
            Log.e("NLServiceTime",lastSecond+"");
            Message mes = NLActivity.handler.obtainMessage();
            mes.obj = secondToTime(lastSecond);
            NLActivity.handler.sendMessage(mes);
            lastSecond = lastSecond - 1000;
            if(lastSecond>0){
                handlerTime.postDelayed(this,1000);
            }else{
                handlerTime.removeCallbacks(this);
            }
        }
    }
}

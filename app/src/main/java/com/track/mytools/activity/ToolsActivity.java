package com.track.mytools.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;//注意view的大小写
import android.view.View.OnClickListener;

import com.track.mytools.R;

import java.io.DataOutputStream;

/**
 * 功能主界面
 *
 */


public class ToolsActivity extends Activity implements OnClickListener{

    private Button suffixBtn;
    private Button qrySuBtn;
    private Button psdBtn;
    private Button photoBtn;
    private Button httpBtn;

    private Button payBtn1;
    private Button payBtn2;
    private Button payBtn3;
    private Button payBtn4;
    private Button payBtn5;
    private Button payBtn6;
    private Button payBtn7;
    private Button payBtn8;
    private Button payBtn9;
    private Button payBtn10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        //Activity切换动画
        Transition explode = TransitionInflater.from(this).inflateTransition(R.transition.fade);
        //退出时使用
        getWindow().setExitTransition(explode);
        //第一次进入时使用
        getWindow().setEnterTransition(explode);
        //再次进入时使用
        getWindow().setReenterTransition(explode);
        setContentView(R.layout.activity_tools);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        suffixBtn = (Button)findViewById(R.id.suffixBtn);
        qrySuBtn = (Button)findViewById(R.id.qrySuBtn);
        //psdBtn = (Button)findViewById(R.id.psdBtn);
        //photoBtn = (Button)findViewById(R.id.photoBtn);
        httpBtn = (Button)findViewById(R.id.httpBtn);

        //payBtn1 = (Button)findViewById(R.id.payBtn1);
        //payBtn2 = (Button)findViewById(R.id.payBtn2);
        //payBtn3 = (Button)findViewById(R.id.payBtn3);
        //payBtn4 = (Button)findViewById(R.id.payBtn4);
        //payBtn5 = (Button)findViewById(R.id.payBtn5);
        //payBtn6 = (Button)findViewById(R.id.payBtn6);
        //payBtn7 = (Button)findViewById(R.id.payBtn7);
        //payBtn8 = (Button)findViewById(R.id.payBtn8);
        //payBtn9 = (Button)findViewById(R.id.payBtn9);
        //payBtn10 = (Button)findViewById(R.id.payBtn10);

        suffixBtn.setOnClickListener(this);
        qrySuBtn.setOnClickListener(this);
        //psdBtn.setOnClickListener(this);
        //photoBtn.setOnClickListener(this);
        httpBtn.setOnClickListener(this);

//        payBtn1.setOnClickListener(this);
//        payBtn2.setOnClickListener(this);
//        payBtn3.setOnClickListener(this);
//        payBtn4.setOnClickListener(this);
//        payBtn5.setOnClickListener(this);
//        payBtn6.setOnClickListener(this);
//        payBtn7.setOnClickListener(this);
//        payBtn8.setOnClickListener(this);
//        payBtn9.setOnClickListener(this);
//        payBtn10.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
       // boolean temp = false;
        if(v.getId() == R.id.suffixBtn){
            Log.i("su","跳转到后缀删添");
           // temp = true;
            intent.setClass(ToolsActivity.this, SuffixActivity.class);
            this.startActivity(intent);
           // this.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        }else if(v.getId() == R.id.qrySuBtn){
            Log.i("su","跳转到后缀列表");
           // temp = true;
            intent.setClass(ToolsActivity.this, QrySuffixActivity.class);
            this.startActivity(intent);
           // this.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        }
//        else if(v.getId() == R.id.psdBtn){
//            Log.i("su","跳转到密码本");
//            //temp = true;
//            intent.setClass(ToolsActivity.this, PwdActivity.class);
//            this.startActivity(intent);
//          //  this.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        }else if(v.getId() == R.id.photoBtn){
//            Log.i("su","跳转到图片遮挡");
//           // temp = true;
//            intent.setClass(ToolsActivity.this, PhotoShadeActivity.class);
//            this.startActivity(intent);
//           // this.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        }
        else if(v.getId() == R.id.httpBtn){
            Log.i("su","跳转到http下载");
            // temp = true;
            intent.setClass(ToolsActivity.this, HttpActivity.class);
            this.startActivity(intent);
            // this.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        }
//        else if(v.getId() == R.id.payBtn1){
//            //微信扫一扫
//            String [] commods = {"am start -n com.tencent.mm/com.tencent.mm.plugin.scanner.ui.BaseScanUI"};
//            exeCommod(commods, this);
//        }else if(v.getId() == R.id.payBtn2){
//            //微信付款
//            String [] commods = {"am start -n com.tencent.mm/com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI"};
//            exeCommod(commods, this);
//        }else if(v.getId() == R.id.payBtn3){
//            //支付宝扫一扫
//            String [] commods = {"am start -n com.eg.android.AlipayGphone/com.alipay.mobile.scan.as.main.MainCaptureActivity"};
//            exeCommod(commods, this);
//        }else if(v.getId() == R.id.payBtn4){
//            //支付宝付款
//            String [] commods = {"am start -n com.eg.android.AlipayGphone/com.alipay.mobile.onsitepay9.payer.OspTabHostActivity"};
//           exeCommod(commods, this);
//        }else if(v.getId() == R.id.payBtn5){
//            //QQ扫一扫
//            String [] commods = {"am start -n com.tencent.mobileqq/com.tencent.biz.qrcode.activity.ScannerActivity"};
//            exeCommod(commods, this);
//        }else if(v.getId() == R.id.payBtn6){
//            //QQ付款
//            String [] commods = {"am start -n com.tencent.mobileqq/cooperation.qwallet.plugin.QWalletPluginProxyActivity"};
//            exeCommod(commods, this);
//        }else if(v.getId() == R.id.payBtn7){
//            //百度钱包扫一扫
//            String [] commods = {"am start -n com.baidu.wallet/com.baidu.wallet.qrcodescanner.QRScanCodeActivity"};
//            exeCommod(commods, this);
//        }else if(v.getId() == R.id.payBtn8){
//            //百度钱包付款
//           // Log.i("baidu","百度钱包付款");
//            String [] commods = {"am start -n com.baidu.wallet/com.baidu.wallet.home.MainActivity","am start -n com.baidu.wallet/com.baidu.wallet.scancode.ui.ShowCodeActivity"};
//            exeCommod(commods, this);
//        }else if(v.getId() == R.id.payBtn9){
//            //京东钱包扫一扫
//            String [] commods = {"am start -n com.wangyin.payment/com.zxing.activity.CaptureActivity"};
//            exeCommod(commods, this);
//        }else if(v.getId() == R.id.payBtn10){
//            //京东钱包付款
//            String [] commods = {"am start -n com.wangyin.payment/com.wangyin.payment.paymentcode.ui.PaymentCodeActivity"};
//            exeCommod(commods, this);
//        }

//        if(temp){
//            this.startActivity(intent);
//        }else{
//            ToolsUntil.showToast(this,"制作中....",1000);
//        }
    }

    /**
     * 执行快捷命令
     * @param commod
     * @param toolsActivity
     */
    public void exeCommod(String [] commod,ToolsActivity toolsActivity){
        Process process = null;
        DataOutputStream dos = null;
        for(int i = 0 ;i < commod.length ; i++){
            try {
                process = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(process.getOutputStream());
                dos.writeBytes(commod[i] + "\n");
                dos.writeBytes("exit\n");
                dos.flush();
                process.waitFor();
//                while(true){
//                    ActivityManager activityManager=(ActivityManager) getSystemService(ACTIVITY_SERVICE);
//                    String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
//                    Log.i("nowActivity",runningActivity);
//                }
//              Thread.sleep(3000);
            } catch (Exception e) {
                // return false;
            } finally {
                try {
                    if (dos != null) {
                        dos.close();
                    }
                    process.destroy();
                } catch (Exception e) {
                }
            }
        }

    }

}

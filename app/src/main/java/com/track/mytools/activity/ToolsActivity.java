package com.track.mytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.track.mytools.R;

/**
 * 功能主界面
 *
 */
public class ToolsActivity extends Activity implements OnClickListener{

    private Button suffixBtn;
    private Button qrySuBtn;
    private Button psdBtn;
    private Button httpBtn;
    private Button copyBtn;
    private Button ftpBtn;
    private Button wifiBtn;
    private Button lanBtn;
    private Button nlBtn;

    private Button payBtn1;
    private Button payBtn2;
    private Button payBtn3;
    private Button payBtn4;


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
        psdBtn = (Button)findViewById(R.id.psdBtn);
        httpBtn = (Button)findViewById(R.id.httpBtn);
        copyBtn = (Button)findViewById(R.id.copyBtn);
        ftpBtn = (Button)findViewById(R.id.ftpBtn);
        wifiBtn = (Button)findViewById(R.id.wifiBtn);
        lanBtn = (Button)findViewById(R.id.lanBtn);
        nlBtn = (Button)findViewById(R.id.nlBtn);

        //payBtn1 = (Button)findViewById(R.id.payBtn1);
        //payBtn2 = (Button)findViewById(R.id.payBtn2);
        //payBtn3 = (Button)findViewById(R.id.payBtn3);
        //payBtn4 = (Button)findViewById(R.id.payBtn4);


        suffixBtn.setOnClickListener(this);
        qrySuBtn.setOnClickListener(this);
        psdBtn.setOnClickListener(this);
        httpBtn.setOnClickListener(this);
        copyBtn.setOnClickListener(this);
        ftpBtn.setOnClickListener(this);
        wifiBtn.setOnClickListener(this);
        lanBtn.setOnClickListener(this);
        nlBtn.setOnClickListener(this);

//        payBtn1.setOnClickListener(this);
//        payBtn2.setOnClickListener(this);
//        payBtn3.setOnClickListener(this);
//        payBtn4.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        if(v.getId() == R.id.suffixBtn){
            Log.i("su","跳转到后缀删添");
            intent.setClass(ToolsActivity.this, SuffixActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.qrySuBtn){
            Log.i("su","跳转到后缀列表");
            intent.setClass(ToolsActivity.this, QrySuffixActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.psdBtn){
            Log.i("su","跳转到密码本");
            intent.setClass(ToolsActivity.this, PwdActivity.class);
            this.startActivity(intent);
        }else if(v.getId() == R.id.httpBtn){
            Log.i("su","跳转到http下载");
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
//        }

    }

}

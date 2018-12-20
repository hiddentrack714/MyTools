package com.track.mytools.until;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.util.Log;
import android.widget.TextView;

import com.track.mytools.activity.MainActivity;
import com.track.mytools.activity.ToolsActivity;

/**
 * Created by Track on 2017/1/17.
 */

public class FingerprintUtil extends FingerprintManager.AuthenticationCallback {

    public MainActivity mainActivity;
    public TextView warnTitle;

    public static int fingerStaus = 0; //0:还未识别,1:识别成功

    public FingerprintUtil(MainActivity mainActivity,TextView warnTitle) {
        super();
        this.mainActivity = mainActivity;
        this.warnTitle = warnTitle;
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        Log.i("ff","识别失败1......");
        //ToolsUtil.showToast(mainActivity,"指纹不正确...",1000);
        warnTitle.setText(errString);
        warnTitle.setVisibility(TextView.VISIBLE);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        Log.i("ff","识别失败2......");
        warnTitle.setText(helpString);
        warnTitle.setVisibility(TextView.VISIBLE);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        Log.i("ff","识别成功......");
        fingerStaus = 1;
        Intent intent = new Intent();
        intent.setClass(mainActivity, ToolsActivity.class);
        mainActivity.startActivity(intent);
        mainActivity.finish();
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        Log.i("ff","识别失败3......");
        warnTitle.setText("指纹错误");
        warnTitle.setVisibility(TextView.VISIBLE);
    }
}

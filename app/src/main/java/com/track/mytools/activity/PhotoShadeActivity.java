package com.track.mytools.activity;

import android.app.Activity;
import android.os.Bundle;

import com.track.mytools.R;

import java.io.DataOutputStream;

/**
 * Created by Track on 2017/1/20.
 * 逻辑顺序:
 * 1,进入主界面，展示图片路径
 * 2,输入要遮挡的图片的名称,点击确定
 * 3,在界面展示要遮挡的图片,点击开始遮挡,即可开始遮挡
 */

public class PhotoShadeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photomain);
    }
}

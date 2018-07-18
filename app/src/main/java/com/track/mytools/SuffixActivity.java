package com.track.mytools;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Track on 2017/1/16.
 */

public class SuffixActivity extends Activity {

    private Button addBtn;   //添加后缀按钮
    private Button delBtn;   //删除后缀按钮
    private ToolsUntil tu;
    private ProgressBar proBar;   //进度条
    private Button suffixEditBtn;   //编辑按钮
    private EditText editTextAddPath;
    private EditText editTextDelPath;
    private EditText editTextAddType;
    private EditText editTextDelType;
    private String fianlPath;
    private String fianlType;
    private TextView viewPercent;
    private Activity nowActivity;

    public static Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("su", "进入SuffixActivity。。。。。。。。。。。。。。。。。");
        setContentView(R.layout.activity_suffix);
        addBtn = (Button) findViewById(R.id.addBtn);
        delBtn = (Button) findViewById(R.id.delBtn);
        proBar = (ProgressBar) findViewById(R.id.suffixPro);
        suffixEditBtn = (Button) findViewById(R.id.suffixEditBtn);
        editTextAddPath = (EditText) findViewById(R.id.editTextAddPath);
        editTextDelPath = (EditText) findViewById(R.id.editTextDelPath);
        editTextAddType = (EditText) findViewById(R.id.editTextAddType);
        editTextDelType = (EditText) findViewById(R.id.editTextDelType);
        viewPercent = (TextView) findViewById(R.id.viewPercent);

        editTextAddPath.setText(ToolsEntiy.path);

        editTextDelPath.setText(ToolsEntiy.path);

        editTextAddType.setText(ToolsEntiy.type);

        editTextDelType.setText(ToolsEntiy.type);

        tu = new ToolsUntil(this);

        nowActivity = this;

        //运行在主线程的Handler,它将监听所有的消息（Message）
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                //接受到另一个线程的Message，拿到它的参数，这个参数代表了进度
                //pb.setProgress(arg0.arg1);
                //tvPersent.setText(arg0.arg1 + "%");
                //pb.setSecondaryProgress(arg0.arg1 + 10);
                proBar.setProgress(arg0.arg1);
                viewPercent.setText((String) arg0.obj);
                return false;
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启进度条
                // proBar.setVisibility(View.VISIBLE);

                proBar.setProgress(0);

                fianlPath = editTextAddPath.getText() + "";  //获取最终地址

                fianlType = editTextAddType.getText() + "";  //获取最终后缀

                //开始添加, 1,首先获取数量，2，然后在开始遍历添加

                ToolsUntil.finshFileNum = 1;

                ToolsUntil.dealFileNum = 0;

                ToolsEntiy.errorSuList.clear();

                ToolsUntil.countNum(fianlPath, fianlType, 1);   //获取待处理文件的数量

                viewPercent.setText("0/" + ToolsUntil.dealFileNum);

                Log.i("su", "待处理文件数量:" + ToolsUntil.dealFileNum);

                if (ToolsUntil.dealFileNum == 0) {
                    ToolsUntil.showToast(nowActivity, "暂无可操作文件", 1000);
                    return;
                }

                proBar.setMax(ToolsUntil.dealFileNum);  //设置进度条最大数量

                //handler = new Handler();
                //启动子现成处理删除操作
                // handler.post(add_runable);
                //关闭进度条
                //  proBar.setVisibility(View.GONE);
                Thread t1 = new Thread(add_runable);
                t1.start();

            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // proBar.setVisibility(View.VISIBLE);

                proBar.setProgress(0);

                fianlPath = editTextDelPath.getText() + "";  //获取最终位置

                fianlType = editTextDelType.getText() + "";  //获取最终后缀

                ToolsUntil.finshFileNum = 1;

                ToolsUntil.dealFileNum = 0;

                ToolsEntiy.errorSuList.clear();

                ToolsUntil.countNum(fianlPath, fianlType, 0);

                viewPercent.setText("0/" + ToolsUntil.dealFileNum);

                Log.i("su", "待处理文件数量:" + ToolsUntil.dealFileNum);

                if (ToolsUntil.dealFileNum == 0) {
                    ToolsUntil.showToast(nowActivity, "暂无可操作文件", 1000);
                    return;
                }

                proBar.setMax(ToolsUntil.dealFileNum);  //设置进度条最大数量

                //handler = new Handler();

                //handler.post(del_runable);

                //  proBar.setVisibility(View.GONE);

                Thread t1 = new Thread(del_runable);
                t1.start();


            }
        });

        suffixEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextAddPath.isEnabled() == false && editTextDelPath.isEnabled() == false) {
                    //失效状态 ->编辑状态
                    editTextAddPath.setEnabled(true);
                    editTextDelPath.setEnabled(true);
                    editTextAddType.setEnabled(true);
                    editTextDelType.setEnabled(true);
                    suffixEditBtn.setText("完成");
                } else {
                    //编辑状态
                    editTextAddPath.setEnabled(false);
                    editTextDelPath.setEnabled(false);
                    editTextAddType.setEnabled(false);
                    editTextDelType.setEnabled(false);
                    suffixEditBtn.setText("编辑");
                }

            }
        });
    }

    Runnable del_runable = new Runnable() {
        public void run() {
            tu.delSuffix(fianlPath, fianlType, proBar, viewPercent);

            ToolsUntil.showToast(nowActivity, "后缀删除完成,失败数量:" + ToolsEntiy.errorSuList.size(), 5000);

            if (ToolsEntiy.errorSuList.size() > 0) {
                StringBuffer sb = new StringBuffer();
                sb.append("失败文件目录\n");
                for (String str : ToolsEntiy.errorSuList) {
                    sb.append(str + "\n");
                }

                ToolsUntil.showToast(nowActivity, sb.toString(), 9000);
            }
        }
    };

    Runnable add_runable = new Runnable() {
        public void run() {
            tu.addSuffix(fianlPath, fianlType, proBar, viewPercent);

            ToolsUntil.showToast(nowActivity, "后缀添加完成,失败数量:" + ToolsEntiy.errorSuList.size(), 5000);

            if (ToolsEntiy.errorSuList.size() > 0) {
                StringBuffer sb = new StringBuffer();
                sb.append("失败文件目录\n");
                for (String str : ToolsEntiy.errorSuList) {
                    sb.append(str + "\n");
                }

                ToolsUntil.showToast(nowActivity, sb.toString(), 9000);
            }
        }
    };

}

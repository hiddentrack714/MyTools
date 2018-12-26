package com.track.mytools.activity;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.SuffixEntity;
import com.track.mytools.util.ToolsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Track on 2017/1/16.
 * 后缀删除/添加
 */
public class SuffixActivity extends Activity {

    private Button suffixAddBtn;   //添加后缀按钮
    private Button suffixDelBtn;   //删除后缀按钮
    private Button suffixEditBtn;   //编辑按钮

    private EditText suffixPath;  //后缀删除的目录控件
    private EditText suffixType;  //需要删除的后缀控件
    private EditText suffixFilter;  //过滤的后缀控件

    private String fianlPath; //后缀删除的目录
    private String fianlType; //需要删除的后缀

    private ProgressBar suffixProBar;   //进度条
    private TextView viewPercent; // 百分比显示

    private Activity nowActivity;

    public static Handler handler;

    public static String[] suffixArrayFilter;   //过滤的后缀名数组

    private static boolean isUpd = false; //是否在修改中

    public static int dealFileNum = 0;   //处理文件总数量
    public static int finshFileNum = 0;  //处理完成的数量
    public static List<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
    public static HashMap<String,List<String>> pathMap = new HashMap<String,List<String>>();  //不同对应文件的数量

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_suffix);

        suffixAddBtn = (Button) findViewById(R.id.suffixAddBtn);
        suffixDelBtn = (Button) findViewById(R.id.suffixDelBtn);
        suffixEditBtn = (Button) findViewById(R.id.suffixEditBtn);

        suffixPath = (EditText) findViewById(R.id.suffixPath);
        suffixType = (EditText) findViewById(R.id.suffixType);
        suffixFilter = (EditText) findViewById(R.id.suffixFilter);

        suffixProBar = (ProgressBar) findViewById(R.id.suffixProBar);
        viewPercent = (TextView) findViewById(R.id.viewPercent);

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> map = ToolsDao.qryTable(sdb,SuffixEntity.class).get(0);

        suffixPath.setText(map.get("suffixPath").toString());
        suffixType.setText(map.get("suffixType").toString());
        suffixFilter.setText(map.get("suffixFilter").toString());

        String strFilter = suffixFilter.getText().toString();

        preMethod(strFilter,suffixType.getText().toString());

        nowActivity = this;

        //运行在主线程的Handler,它将监听所有的消息（Message）
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                //接受到另一个线程的Message，拿到它的参数，这个参数代表了进度
                suffixProBar.setProgress(arg0.arg1);
                viewPercent.setText((String) arg0.obj);
                return false;
            }
        });

        //添加后缀按键监听
        suffixAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启进度条
                suffixProBar.setProgress(0);

                fianlPath = suffixPath.getText() + "";  //获取最终地址

                fianlType = suffixType.getText() + "";  //获取最终后缀

                //开始添加, 1,首先获取数量，2，然后在开始遍历添加

                finshFileNum = 1;

                dealFileNum = 0;

                ToolsUtil.countNum(fianlPath, fianlType, 1);   //获取待处理文件的数量

                viewPercent.setText("0/" + dealFileNum);

                Log.i("su", "待处理文件数量:" + dealFileNum);

                if (dealFileNum == 0) {
                    ToolsUtil.showToast(nowActivity, "暂无可操作文件", 1000);
                    return;
                }

                suffixProBar.setMax(dealFileNum);  //设置进度条最大数量

                new Thread(){
                    @Override
                    public void run() {
                        try{
                            ToolsUtil.addSuffix(fianlPath, fianlType, suffixProBar, viewPercent);

                            ToolsUtil.showToast(nowActivity, "后缀添加完成!", 5000);
                        }catch(Exception e){

                        }
                    }
                }.start();
            }
        });

        //删除后缀按键监听
        suffixDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                suffixProBar.setProgress(0);

                fianlPath = suffixPath.getText() + "";  //获取最终位置

                fianlType = suffixType.getText() + "";  //获取最终后缀

                finshFileNum = 1;

                dealFileNum = 0;

                ToolsUtil.countNum(fianlPath, fianlType, 0);

                viewPercent.setText("0/" + dealFileNum);

                Log.i("su", "待处理文件数量:" + dealFileNum);

                if (dealFileNum == 0) {
                    ToolsUtil.showToast(nowActivity, "暂无可操作文件", 1000);
                    return;
                }

                suffixProBar.setMax(dealFileNum);  //设置进度条最大数量

                new Thread(){
                    @Override
                    public void run() {
                        try {
                            ToolsUtil.delSuffix(fianlPath, fianlType, suffixProBar, viewPercent);

                            ToolsUtil.showToast(nowActivity, "后缀删除完成!", 5000);
                        }catch(Exception e){

                        }
                    }
                }.start();
            }
        });

        //修改按钮监听
        suffixEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpd == false) {
                    //失效状态
                    suffixPath.setEnabled(true);
                    suffixType.setEnabled(true);
                    suffixFilter.setEnabled(true);
                    isUpd = true;
                    suffixEditBtn.setText("完成");
                } else {
                    //编辑状态
                    suffixPath.setEnabled(false);
                    suffixType.setEnabled(false);
                    suffixFilter.setEnabled(false);
                    isUpd = false;
                    SQLiteDatabase sdb = ToolsDao.getDatabase();

                    HashMap<String,Object> dataMap = new HashMap<String,Object>();
                    dataMap.put("suffixPath",suffixPath.getText().toString());
                    dataMap.put("suffixType",suffixType.getText().toString());
                    dataMap.put("suffixFilter",suffixFilter.getText().toString());
                    dataMap.put("id",map.get("id"));

                    ToolsDao.saveOrUpdIgnoreExsit(sdb,dataMap,SuffixEntity.class);
                    suffixEditBtn.setText("修改");
                }
            }
        });
    }

    /**
     * 前置方法
     * @param suffixType
     * @param strFilter
     */
    public static void preMethod(String strFilter,String suffixType){
        suffixArrayFilter = new String[strFilter.split(",").length];
        for (int i = 0; i < strFilter.split(",").length; i++) {
            suffixArrayFilter[i] = strFilter.split(",")[i];
            List<String> list = new ArrayList<String>();
            pathMap.put(suffixArrayFilter[i],list);//占位
        }
        List<String> list = new ArrayList<String>();
        pathMap.put(suffixType,list);
        List<String> list1 = new ArrayList<String>();
        pathMap.put("未知",list1);
    }
}

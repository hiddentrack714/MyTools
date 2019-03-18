package com.track.mytools.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.QrySuffixEntity;
import com.track.mytools.service.QrySuffixService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

/**
 * Created by Track on 2017/1/18.
 * 文件查询列表
 */

public class QrySuffixActivity extends ListActivity {
    String[] from={"typeName","typeNum"};              //这里是ListView显示内容每一列的列名
    int[] to={R.id.typeName,R.id.typeNum};   //这里是ListView显示每一列对应的list_item中控件的id

    String[] typeName; //这里第一列所要显示的人名
    String[] typeNum;  //这里是人名对应的ID

    public static ArrayList<HashMap<String,String>> list=null;
    HashMap<String,String> map=null;

    public static SimpleAdapter adapter;

    public static QrySuffixActivity qrySuffixActivity;

    @BindView(R.id.suffixMainLayout)
    LinearLayout suffixMainLayout;

    @BindView(R.id.qrySuffixPath)
    EditText qrySuffixPath;

    @BindView(R.id.qrySuffixStr)
    EditText qrySuffixStr;

    @BindView(R.id.qrySuffixBtn)
    Button qrySuffixBtn;

    @BindView(R.id.qrySuffixUpd)
    Button qrySuffixUpd;

    public static Handler qrySuffixActivityHandler;

    private static boolean isUpd = false;

    private final int EX_FILE_PICKER_RESULT = 0xfa01;
    private String startDirectory = null;// 记忆上一次访问的文件目录路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suffixmain);

        ButterKnife.bind(this);

        qrySuffixActivity = this;

        qrySuffixActivityHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Log.i("QrySuffixActivity_Log","后缀计算完成，开始展示");

                suffixMainLayout.setVisibility(View.GONE);

                list = QrySuffixService.list;

                //创建一个SimpleAdapter对象
                adapter=new SimpleAdapter(QrySuffixActivity.qrySuffixActivity,QrySuffixService.list,R.layout.activity_suffixlist,from,to);
                //调用ListActivity的setListAdapter方法，为ListView设置适配器
                setListAdapter(adapter);
            }
        };

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> dataMap = ToolsDao.qryTable(sdb, QrySuffixEntity.class, QrySuffixActivity.qrySuffixActivity).get(0);

        qrySuffixPath.setText((String)dataMap.get("qrySuffixPath"));
        qrySuffixStr.setText((String)dataMap.get("qrySuffixStr"));

        //监听查找按钮
        qrySuffixBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                suffixMainLayout.setVisibility(View.VISIBLE);

                new Thread(){
                    @Override
                    public void run() {
                        Intent intentService = new Intent(QrySuffixActivity.this, QrySuffixService.class);

                        startService(intentService);
                    }
                }.start();

            }
        });

        qrySuffixUpd.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(isUpd){
                    qrySuffixPath.setEnabled(false);
                    qrySuffixStr.setEnabled(false);

                    isUpd = false;

                    qrySuffixUpd.setText("修改参数");

                    SQLiteDatabase sqd = ToolsDao.getDatabase();
                    HashMap<String,Object> map = new HashMap<String,Object>();
                    map.put("id",dataMap.get("id"));
                    map.put("qrySuffixPath",qrySuffixPath.getText().toString());
                    map.put("qrySuffixStr",qrySuffixStr.getText().toString());

                    ToolsDao.saveOrUpdIgnoreExsit(sqd,map,QrySuffixEntity.class);
                }else{
                    qrySuffixPath.setEnabled(true);
                    qrySuffixStr.setEnabled(true);

                    isUpd = true;

                    qrySuffixUpd.setText("完成");
                }

            }
        });

        qrySuffixPath.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    ExFilePicker exFilePicker = new ExFilePicker();
                    exFilePicker.setCanChooseOnlyOneItem(true);// 单选
                    exFilePicker.setQuitButtonEnabled(true);
                    exFilePicker.setChoiceType(ExFilePicker.ChoiceType.DIRECTORIES);

                    if (TextUtils.isEmpty(startDirectory)) {
                        exFilePicker.setStartDirectory(Environment.getExternalStorageDirectory().getPath());
                    } else {
                        exFilePicker.setStartDirectory(startDirectory);
                    }

                    exFilePicker.start(QrySuffixActivity.this, EX_FILE_PICKER_RESULT);
                }
                return false;
            }
        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (position > 0 && position < list.size() - 1){
            Log.i("QrySuffixActivity_Log","--->" + SuffixActivity.pathMap.get(list.get(position).get("typeName")).size());
            Intent intent = new Intent();
            intent.putExtra("key",list.get(position).get("typeName"));
            intent.setClass(this, QrySuffixDetailActivity.class);
            this.startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EX_FILE_PICKER_RESULT) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                String path = result.getPath();

                List<String> names = result.getNames();
                for (int i = 0; i < names.size(); i++) {
                    File f = new File(path, names.get(i));
                    try {
                        Uri uri = Uri.fromFile(f); //这里获取了真实可用的文件资源

                        qrySuffixPath.setText(uri.getPath());
                        startDirectory = path;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}

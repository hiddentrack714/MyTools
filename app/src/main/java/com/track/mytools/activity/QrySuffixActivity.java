package com.track.mytools.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.SuffixEntity;
import com.track.mytools.service.QrySuffixService;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    public static Handler qrySuffixActivityHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suffixmain);

        ButterKnife.bind(this);

        qrySuffixActivity = this;

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> dataMap = ToolsDao.qryTable(sdb, SuffixEntity.class, QrySuffixActivity.qrySuffixActivity).get(0);

        list=new ArrayList<HashMap<String,String>>();

        map=new HashMap<String,String>();       //为避免产生空指针异常，有几列就创建几个map对象
        map.put("typeName", "当前目录:");
        map.put("typeNum", dataMap.get("suffixPath").toString());
        list.add(map);

        //创建一个SimpleAdapter对象
        adapter=new SimpleAdapter(QrySuffixActivity.qrySuffixActivity,list,R.layout.activity_suffixlist,from,to);
        //调用ListActivity的setListAdapter方法，为ListView设置适配器
        setListAdapter(adapter);

        //休眠1秒钟，防止无法进入，卡上一界面activity，
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    Intent intentService = new Intent(QrySuffixActivity.this, QrySuffixService.class);

                    startService(intentService);
                }catch(Exception e){

                }
            }
        }.start();

        qrySuffixActivityHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Log.i("QrySuffixActivity_Log","后缀计算完成，开始展示");

                suffixMainLayout.setVisibility(View.GONE);

                list.clear();

                list.addAll(QrySuffixService.list);

                adapter.notifyDataSetChanged();
            }
        };

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (position > 0 && position < list.size() - 1){
            Log.i("susee","--->" + SuffixActivity.pathMap.get(list.get(position).get("typeName")).size());
            Intent intent = new Intent();
            intent.putExtra("key",list.get(position).get("typeName"));
            intent.setClass(this, QrySuffixDetailActivity.class);
            this.startActivity(intent);
        }
    }

}

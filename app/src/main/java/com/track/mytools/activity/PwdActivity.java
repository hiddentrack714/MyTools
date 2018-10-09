package com.track.mytools.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.track.mytools.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Track on 2017/2/9.
 * 密码记录本
 */

public class PwdActivity extends ListActivity {
    String[] from={"pwdName","pwdAccount","pwdPsd"};              //这里是ListView显示内容每一列的列名
    int[] to={R.id.pwdName,R.id.pwdAccount,R.id.pwdPsd};   //这里是ListView显示每一列对应的list_item中控件的id

    private Button pwdAddBtn;  //添加
   // private Button pwdChangeBtn;//保存
   // private Button pwdSaveBtn;//修改
    private ListView listView;

    private EditText pwdName;
    private EditText pwdAccount;
    private EditText pwdPsd;

    public static SimpleAdapter adapter;

    private PwdActivity pwdActivity;

    public static ArrayList<HashMap<String,String>> list=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwdmain);

        pwdAddBtn = (Button)findViewById(R.id.pwdAddBtn);
       //pwdChangeBtn = (Button)findViewById(R.id.pwdChangeBtn);
        //pwdSaveBtn = (Button)findViewById(R.id.pwdSaveBtn);

        pwdName = (EditText)findViewById(R.id.pwdName);
        pwdAccount = (EditText)findViewById(R.id.pwdAccount);
        pwdPsd = (EditText)findViewById(R.id.pwdPsd);

        pwdActivity = this;

        list = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("pwdName","民生银行");
        map.put("pwdAccount","6226221202493281");
        map.put("pwdPsd","186714");
        list.add(map);

       // List list = new ArrayList();
        adapter = new SimpleAdapter(this,list,R.layout.activity_pwddetail,from,to);
        //调用ListActivity的setListAdapter方法，为ListView设置适配器
        setListAdapter(adapter);

        //点击添加密码区域
        pwdAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("pwd","pwdAddBtn");
                HashMap<String,String> map = new HashMap<String,String>();
                map.put("pwdName","          ");
                map.put("pwdAccount","                ");
                map.put("pwdPsd","                ");
               // list.add(map);

                ArrayList tempL = (ArrayList)list.clone();
                list.clear();
                tempL.add(map);
                list.addAll(tempL);
                adapter.notifyDataSetChanged();
            }
        });

//        //点击修改密码
//        pwdChangeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("pwd","pwdChangeBtn");
//            }
//        });
//
//        //点击保存修改的密码
//        pwdSaveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("pwd","pwdSaveBtn");
//            }
//        });

//        pwdName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    Log.i("pwd","1");
//            }
//        });
//
//        pwdAccount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("pwd","2");
//            }
//        });
//
//        pwdPsd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("pwd","3");
//            }
//        });



    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.i("check",position + "");
    }



}

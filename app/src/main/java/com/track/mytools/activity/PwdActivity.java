package com.track.mytools.activity;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.track.mytools.R;
import com.track.mytools.adapter.PwdMainAdapter;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.PwdEntity;
import com.track.mytools.util.DesUtil;
import com.track.mytools.util.ToolsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Track on 2017/2/9.
 * 密码记录本
 */

public class PwdActivity extends Activity {

    @BindView(R.id.pwdAddBtn)
    Button pwdAddBtn;  //添加

    @BindView(R.id.pwdSaveBtn)
    Button pwdSaveBtn;//保存

    @BindView(R.id.pwdList)
    ListView pwdList;

    @BindView(R.id.pwdSearch)
    EditText pwdSearch;

    public static PwdActivity pwdActivity;

    public static List<HashMap<String,Object>> qryList;

    public static List<HashMap<String,Object>> tempQryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwdmain);
        ButterKnife.bind(this);

        pwdActivity = this;

        pwdAddBtn = (Button)findViewById(R.id.pwdAddBtn);
        pwdSaveBtn = (Button)findViewById(R.id.pwdSaveBtn);

        pwdList = (ListView)findViewById(R.id.pwdList);

        //检测是否是非法页面跳转
        if(!ToolsUtil.isLegal()){
            ToolsUtil.showToast(PwdActivity.this,"非法页面跳转",5000);
            finish();
        }

        SQLiteDatabase sdb = ToolsDao.getDatabase();

        qryList = ToolsDao.qryTable(sdb,PwdEntity.class,PwdActivity.this);

        //解密
        for(HashMap<String,Object> map:qryList){
            map.put("pwdPsd",DesUtil.desDecrypt(map.get("pwdPsd").toString()));
        }

        //深度复制
        tempQryList = ToolsUtil.deepCopy((ArrayList) qryList);

        Log.i("PwdActivity_Log","存储密码数量:" + qryList.size());

        PwdMainAdapter pwdMainAdapter = new PwdMainAdapter(PwdActivity.this,tempQryList);

        pwdList.setAdapter(pwdMainAdapter);

        //点击添加密码区域
        pwdAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,Object> map = new HashMap<String,Object>();
                map.put("pwdName","");
                map.put("pwdAccount","");
                map.put("pwdPsd","");
                tempQryList.add(map);
                qryList.add(map);
                pwdMainAdapter.notifyDataSetChanged();
            }
        });


        //点击保存修改的密码
        pwdSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(PwdMainAdapter.updMap.size()!=0){

                    ToolsUtil.showToast(PwdActivity.this,"当前还有未保存的修改，无法保存!",3000);

                }else{

                    List<HashMap<String,Object>> daoList = new ArrayList<HashMap<String,Object>>();

                    daoList = ToolsUtil.deepCopy((ArrayList) qryList);

                    for(int i=0 ;i<daoList.size();i++){
                        SQLiteDatabase sdb = ToolsDao.getDatabase();
                        //加密
                        daoList.get(i).put("pwdPsd",DesUtil.desEncrypt(daoList.get(i).get("pwdPsd").toString()));
                        ToolsDao.saveOrUpdIgnoreExsit(sdb,daoList.get(i),PwdEntity.class);
                    }

                    ToolsUtil.showToast(PwdActivity.this,"保存完成!",3000);
                }
            }
        });

        //监听搜索名称
        pwdSearch.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();

                List<HashMap<String,Object>> tempList = new ArrayList<HashMap<String,Object>>();
                for(HashMap<String,Object> map :qryList){
                    String pwdNameStr = map.get("pwdName").toString();

                    if(pwdNameStr.indexOf(str) >-1){
                        tempList.add(map);
                    }
                }

                tempQryList.clear();

                tempQryList.addAll(tempList);

                pwdMainAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

}

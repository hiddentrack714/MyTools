package com.track.mytools.activity;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.track.mytools.R;
import com.track.mytools.adapter.PwdMainAdapter;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.PwdEntity;
import com.track.mytools.util.DesUtil;
import com.track.mytools.util.ToolsUtil;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Track on 2017/2/9.
 * 密码记录本
 */

public class PwdActivity extends Activity {

    private Button pwdAddBtn;  //添加
    private Button pwdSaveBtn;//保存
    private ListView pwdList;

    public static PwdActivity pwdActivity;

    public static List<HashMap<String,Object>> qryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwdmain);

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

        List<HashMap<String,Object>> qryList = ToolsDao.qryTable(sdb,PwdEntity.class,PwdActivity.this);

        //解密
        for(HashMap<String,Object> map:qryList){
            map.put("pwdPsd",DesUtil.desDecrypt(map.get("pwdPsd").toString()));
        }

        Log.i("PwdActivity_Log","存储密码数量:" + qryList.size());

        PwdMainAdapter pma = new PwdMainAdapter(PwdActivity.this,qryList);

        pwdList.setAdapter(pma);

        //点击添加密码区域
        pwdAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("pwd","pwdAddBtn");
                HashMap<String,Object> map = new HashMap<String,Object>();
                map.put("pwdName","");
                map.put("pwdAccount","");
                map.put("pwdPsd","");
                qryList.add(map);
                pma.notifyDataSetChanged();
            }
        });


        //点击保存修改的密码
        pwdSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("pwd","pwdSaveBtn");

                if(PwdMainAdapter.updMap.size()!=0){

                    ToolsUtil.showToast(PwdActivity.this,"当前还有未保存的修改，无法保存!",3000);

                }else{

                    for(int i=0 ;i<qryList.size();i++){
                        SQLiteDatabase sdb = ToolsDao.getDatabase();
                        qryList.get(i).put("pwdPsd",DesUtil.desEncrypt(qryList.get(i).get("pwdPsd").toString()));
                        ToolsDao.saveOrUpdIgnoreExsit(sdb,qryList.get(i),PwdEntity.class);
                        Log.i("PwdActivity",qryList.get(i).toString());
                    }

                    ToolsUtil.showToast(PwdActivity.this,"保存完成!",3000);
                }
            }
        });
    }

}

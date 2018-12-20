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
import com.track.mytools.until.ToolsUntil;

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

        pwdAddBtn = (Button)findViewById(R.id.pwdAddBtn);
        pwdSaveBtn = (Button)findViewById(R.id.pwdSaveBtn);

        pwdList = (ListView)findViewById(R.id.pwdList);

        pwdActivity = this;

        SQLiteDatabase sdb = ToolsDao.getDatabase();

        List<HashMap<String,Object>> qryList = ToolsDao.qryTable(sdb,PwdEntity.class);

        PwdMainAdapter.viewList.clear();

        PwdMainAdapter pma = new PwdMainAdapter(pwdActivity,qryList);

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
                SQLiteDatabase sdb = ToolsDao.getDatabase();

                for(int i=0 ;i<qryList.size();i++){
                    if((boolean)PwdMainAdapter.viewList.get(i).get("changeIng") == true){
                        ToolsUntil.showToast(pwdActivity,"还有未保存的修改!",3000);
                        break;
                    }
                    ToolsDao.saveOrUpdIgnoreExsit(sdb,qryList.get(i),PwdEntity.class);
                    Log.i("PwdActivity",qryList.get(i).toString());
                }

                ToolsUntil.showToast(pwdActivity,"保存完成!",3000);
            }
        });
    }

}

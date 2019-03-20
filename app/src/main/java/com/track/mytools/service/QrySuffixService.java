package com.track.mytools.service;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.track.mytools.R;
import com.track.mytools.activity.QrySuffixActivity;
import com.track.mytools.activity.SuffixActivity;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.QrySuffixEntity;
import com.track.mytools.util.ToolsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QrySuffixService extends Service {

    String[] from={"typeName","typeNum"};              //这里是ListView显示内容每一列的列名
    int[] to={R.id.typeName,R.id.typeNum};   //这里是ListView显示每一列对应的list_item中控件的id

    String[] typeName; //这里第一列所要显示的人名
    String[] typeNum;  //这里是人名对应的ID

    HashMap<String,String> map=null;

    public static ArrayList<HashMap<String,String>> list=null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("QrySuffixService_Log","开始计算后缀");

        SuffixActivity.list.clear();  //初始化

        Set<Map.Entry<String,List<String>>> tempSet = SuffixActivity.pathMap.entrySet();

        for (Map.Entry<String,List<String>> entry:tempSet) {
            entry.getValue().clear();
        }

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> dataMap = ToolsDao.qryTable(sdb, QrySuffixEntity.class, QrySuffixActivity.qrySuffixActivity).get(0);

        SuffixActivity.preMethod(dataMap.get("qrySuffixStr").toString(),null);

        QrySuffixActivity.qrySuffixActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<HashMap<String,String>> listType = ToolsUtil.qrySuffixNum(dataMap.get("qrySuffixPath").toString(),dataMap.get("qrySuffixStr").toString());

                Log.i("QrySuffixService_Log","后缀类型数量:" + listType.size());
                typeName = new String[listType.size()];
                typeNum = new String[listType.size()];

                for (int i = 0; i<listType.size(); i++){
                    typeName[i] = listType.get(i).get("TYPE");
                    typeNum[i] = listType.get(i).get("NUM");
                }

                list=new ArrayList<HashMap<String,String>>();

                int count = 0;

                map=new HashMap<String,String>();       //为避免产生空指针异常，有几列就创建几个map对象
                map.put("typeName", "名称");
                map.put("typeNum", "数量");
                list.add(map);

                for(int j=0; j<listType.size(); j++){
                    map=new HashMap<String,String>();       //为避免产生空指针异常，有几列就创建几个map对象
                    map.put("typeName", typeName[j]);
                    map.put("typeNum", typeNum[j]);
                    count = count + Integer.parseInt(typeNum[j]);
                    list.add(map);
                }

                map=new HashMap<String,String>();       //为避免产生空指针异常，有几列就创建几个map对象
                map.put("typeName", "合计");
                map.put("typeNum", count+"");
                list.add(map);

                Message msg = QrySuffixActivity.qrySuffixActivityHandler.obtainMessage();
                QrySuffixActivity.qrySuffixActivityHandler.sendMessage(msg);
                stopSelf();
            }
        });
    }
}

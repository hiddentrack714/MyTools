package com.track.mytools.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.SuffixEntity;
import com.track.mytools.util.ToolsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suffixmain);

        //创建ArrayList对象；
        list=new ArrayList<HashMap<String,String>>();
        //将数据存放进ArrayList对象中，数据安排的结构是，ListView的一行数据对应一个HashMap对象，
        //HashMap对象，以列名作为键，以该列的值作为Value，将各列信息添加进map中，然后再把每一列对应
        //的map对象添加到ArrayList中

        SuffixActivity.list.clear();  //初始化

        Set<Map.Entry<String,List<String>>> tempSet = SuffixActivity.pathMap.entrySet();

        for (Map.Entry<String,List<String>> entry:tempSet) {
            entry.getValue().clear();
        }

        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> dataMap = ToolsDao.qryTable(sdb,SuffixEntity.class).get(0);

        SuffixActivity.preMethod(dataMap.get("suffixFilter").toString(),dataMap.get("suffixType").toString());

        List<HashMap<String,String>> listType = ToolsUtil.qrySuffixNum(dataMap.get("suffixPath").toString(),dataMap.get("suffixType").toString());

        Log.i("ch","后缀类型数量:" + listType.size());
        typeName = new String[listType.size()];
        typeNum = new String[listType.size()];

        for (int i = 0; i<listType.size(); i++){
            typeName[i] = listType.get(i).get("TYPE");
            typeNum[i] = listType.get(i).get("NUM");
        }

        int count = 0;

        map=new HashMap<String,String>();       //为避免产生空指针异常，有几列就创建几个map对象
        map.put("typeName", "当前目录:");
        map.put("typeNum", dataMap.get("suffixPath").toString());
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

        //创建一个SimpleAdapter对象
        adapter=new SimpleAdapter(this,list,R.layout.activity_suffixlist,from,to);
        //调用ListActivity的setListAdapter方法，为ListView设置适配器
        setListAdapter(adapter);
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

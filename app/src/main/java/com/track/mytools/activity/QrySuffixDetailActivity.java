package com.track.mytools.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.track.mytools.R;
import com.track.mytools.adapter.SuffixMainAdapter;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Track on 2017/1/29.
 */

public class QrySuffixDetailActivity extends Activity {

    @BindView(R.id.smdBtn)
    Button smdBtn;    //删除

    @BindView(R.id.allSelBtn)
    Button allSelBtn; //全选

    @BindView(R.id.unSelBtn)
    Button unSelBtn;  //反选

    @BindView(R.id.canelBtn)
    Button canelBtn;  //取消

    @BindView(R.id.list)
    ListView list;

    HashMap<String, String> map = null;

    private QrySuffixDetailActivity qsda;
    private int pos;
    private String useKey;
    private ArrayList<HashMap<String, String>> tempList;
    private SimpleAdapter adapter;
    private SuffixMainAdapter mainAdapter;
    private String key;

    public static int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suffixmaindetail);
        ButterKnife.bind(this);

        //删除
        smdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("QrySuffixDetailActivity_Log","smdBtn");

                //先检查是否有选中的cb
                if(!ToolsUtil.checkCB(SuffixMainAdapter.isSelected)){
                    ToolsUtil.showToast(qsda,"还没有选中的文件",2000);
                    return;
                }

                pos = position;
                //点击后弹出提示框，是否需要删除当前
                AlertDialog.Builder ad = new AlertDialog.Builder(qsda);
                ad.setTitle("确认");
                ad.setMessage("确定删除选择的多项吗?");
                ad.setPositiveButton("是",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                Set<HashMap.Entry<Integer, Boolean>> entry = SuffixMainAdapter.isSelected.entrySet();

                                int tempMapSize = SuffixMainAdapter.isSelected.size();

                                int tempDelSize = 0;

                                ArrayList tempL = (ArrayList) tempList.clone();//克隆一个新的集合

                                for(HashMap.Entry<Integer, Boolean> allMap : entry){
                                   int allKey =  allMap.getKey();
                                    boolean  value = allMap.getValue();

                                    //先删除的文件，然后一次删除，避免，删除第一个后，刷新list导致的角标改变

                                    if(value){
                                        Log.i("QrySuffixDetailActivity_Log", "Name:-" + allKey + "-" + SuffixActivity.pathMap.get(useKey).get(allKey));
                                        File file = new File(SuffixActivity.pathMap.get(useKey).get(allKey));
                                        file.delete();
                                        if (!file.exists()) {
                                            Log.i("QrySuffixDetailActivity_Log", "DEL_SUCCESS");
                                            //fix 删除当前的子集合刷新后，还要刷新母集合显示的数量
                                            ArrayList<HashMap<String, String>> mainTempL = (ArrayList<HashMap<String, String>>) QrySuffixActivity.list.clone();//克隆一个新的集合
                                            for (HashMap<String, String> map : mainTempL) {
                                                String tempKey = map.get("typeName");
                                                if (key.equalsIgnoreCase(tempKey) || "合计".equalsIgnoreCase(tempKey)) {
                                                    //找到当前后缀的数量集合
                                                    int tempI = Integer.parseInt(map.get("typeNum"));
                                                    Log.i("QrySuffixDetailActivity_Log", key + ":" + tempI);
                                                    map.put("typeNum", (tempI - 1) + "");
                                                    // break;
                                                }
                                            }
                                            QrySuffixActivity.list.clear();
                                            QrySuffixActivity.list.addAll(mainTempL);
                                            QrySuffixActivity.adapter.notifyDataSetChanged();

                                            tempDelSize++;
                                        } else {
                                            Log.i("QrySuffixDetailActivity_Log", "DEL_FAIL");
                                            ToolsUtil.showToast(qsda, "删除失败:" + SuffixActivity.pathMap.get(useKey).get(pos), 2000);
                                        }
                                    }
                                }

                                //倒叙删除,避免删除list的时候混乱删除
                                List<Integer> daoList = new ArrayList<Integer>();
                                for(HashMap.Entry<Integer, Boolean> allMap : entry){
                                    int allKey =  allMap.getKey();
                                    boolean  value = allMap.getValue();

                                    if(value){
                                        //tempL.remove(allKey);
                                        daoList.add(allKey);
                                        //ToolsUtil.pathMap.get(useKey).remove(allKey);
                                    }
                                }

                                Collections.sort(daoList);//大小排序

                                Collections.reverse(daoList);//倒叙排序

                                Log.i("QrySuffixDetailActivity_Log","-->" + tempL.size());

                                for (int i:daoList) {
                                    Log.i("QrySuffixDetailActivity_Log","-->"+i);
                                    tempL.remove(i);
                                    SuffixActivity.pathMap.get(useKey).remove(i);
                                    //删除选择的checkbox
                                    View view = list.getChildAt(i);
                                    if(view != null){
                                        SuffixMainAdapter.ViewHolder holder = (SuffixMainAdapter.ViewHolder) view.getTag();
                                        holder.cb.toggle();
                                        SuffixMainAdapter.isSelected.put(i,false);
                                    }else{
                                        Log.e("QrySuffixDetailActivity_Log","xxxxxxxxxxx");
                                    }

                                }

                                //因为删除了文件，导致下角标顺序改变，所以必须清空checkbox选择信息的Map，重新复制
                                SuffixMainAdapter.isSelected.clear();

                               for (int i = 0;i < tempMapSize - tempDelSize ; i++){

                                   SuffixMainAdapter.isSelected.put(i,false);

                               }

                                Log.i("QrySuffixDetailActivity_Log","-->" + SuffixMainAdapter.isSelected.size());

                                tempList.clear();
                                tempList.addAll(tempL);
                                mainAdapter.notifyDataSetChanged();
                            }
                        });
                ad.setNegativeButton("否", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.i("QrySuffixDetailActivity_Log", "n" + SuffixActivity.pathMap.get(useKey).get(pos));
                    }
                });
                ad.show();
            }
        });

        //全选
        allSelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("QrySuffixDetailActivity_Log","allSelBtn");

                Log.i("QrySuffixDetailActivity_Log","-->" + SuffixMainAdapter.isSelected.size());

                    for(int i = 0 ; i < SuffixMainAdapter.isSelected.size();i++){

                        if(true == SuffixMainAdapter.isSelected.get(i)){

                            continue;
                        }

                        mainAdapter.notifyDataSetChanged();

                        SuffixMainAdapter.isSelected.put(i,true);
                    }
            }
        });

        //取消
        canelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("QrySuffixDetailActivity_Log","canelBtn");

                    for(int i = 0 ; i < SuffixMainAdapter.isSelected.size();i++){

                        if(false == SuffixMainAdapter.isSelected.get(i)){
                            continue;
                        }

                        mainAdapter.notifyDataSetChanged();

                        SuffixMainAdapter.isSelected.put(i,false);
                    }

            }
        });

        //反选
        unSelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("QrySuffixDetailActivity_Log","unSelBtn");
                for(int i = 0 ; i < SuffixMainAdapter.isSelected.size();i++){

                    if(SuffixMainAdapter.isSelected.get(i)==true){
                        SuffixMainAdapter.isSelected.put(i,false);
                    }else{
                        SuffixMainAdapter.isSelected.put(i,true);
                    }

                    mainAdapter.notifyDataSetChanged();
                }
            }
        });

        //点击listview
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
                //点击后弹出提示框，是否需要删除当前
                AlertDialog.Builder ad = new AlertDialog.Builder(qsda);
                ad.setTitle("确认");
                ad.setMessage("确定删除[" + SuffixActivity.pathMap.get(useKey).get(pos) + "]吗?");
                ad.setPositiveButton("是",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Log.i("QrySuffixDetailActivity_Log", "y" + SuffixActivity.pathMap.get(useKey).get(pos));
                                File file = new File(SuffixActivity.pathMap.get(useKey).get(pos));
                                file.delete();
                                if (!file.exists()) {
                                    Log.i("QrySuffixDetailActivity_Log", "DEL_SUCCESS");
                                    ToolsUtil.showToast(qsda, "删除成功:" + SuffixActivity.pathMap.get(useKey).get(pos), 2000);
                                    //删除完成后，刷新当前listview
                                    //ToolsUtil.pathMap.get(useKey).remove(pos);
                                    ArrayList tempL = (ArrayList) tempList.clone();//克隆一个新的集合
                                    tempL.remove(pos);
                                    tempList.clear();
                                    tempList.addAll(tempL);
                                    mainAdapter.notifyDataSetChanged();

                                    //fix 删除当前的listview的集合后，还要删除之前全集合
                                    SuffixActivity.pathMap.get(useKey).remove(pos);

                                    //fix 删除当前的子集合刷新后，还要刷新母集合显示的数量
                                    ArrayList<HashMap<String, String>> mainTempL = (ArrayList<HashMap<String, String>>) QrySuffixActivity.list.clone();//克隆一个新的集合
                                    for (HashMap<String, String> map : mainTempL) {
                                        String tempKey = map.get("typeName");
                                        if (key.equalsIgnoreCase(tempKey) || "合计".equalsIgnoreCase(tempKey)) {
                                            //找到当前后缀的数量集合
                                            int tempI = Integer.parseInt(map.get("typeNum"));
                                            Log.i("QrySuffixDetailActivity_Log", key + ":" + tempI);
                                            map.put("typeNum", (tempI - 1) + "");
                                            // break;
                                        }
                                    }
                                    QrySuffixActivity.list.clear();
                                    QrySuffixActivity.list.addAll(mainTempL);
                                    QrySuffixActivity.adapter.notifyDataSetChanged();
                                } else {
                                    Log.i("QrySuffixDetailActivity_Log", "DEL_FAIL");
                                    ToolsUtil.showToast(qsda, "删除失败:" + SuffixActivity.pathMap.get(useKey).get(pos), 2000);
                                }
                            }
                        });
                ad.setNegativeButton("否", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.i("QrySuffixDetailActivity_Log", "n" + SuffixActivity.pathMap.get(useKey).get(pos));
                    }
                });
                ad.show();
            }
        });

        qsda = this;

        //创建一个SimpleAdapter对象
        Intent intent = getIntent();

        key = intent.getStringExtra("key");
        useKey = key;
        tempList = new ArrayList<HashMap<String, String>>();
        for (String str : SuffixActivity.pathMap.get(key)
                ) {
            map = new HashMap<String, String>();       //为避免产生空指针异常，有几列就创建几个map对象
            map.put("typePath", str);
            tempList.add(map);
        }
        String[] formDeatil = {"typePath"};
        int[] toDeatil = {R.id.typePath};

        mainAdapter = new SuffixMainAdapter(this, tempList, R.layout.activity_suffixdetail, formDeatil, toDeatil);
        //调用ListActivity的setListAdapter方法，为ListView设置适配器

        list.setAdapter(mainAdapter);
    }

}

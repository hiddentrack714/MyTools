package com.track.mytools.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.track.mytools.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Track on 2017/2/21.
 */

public class SuffixMainAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<HashMap<String, String>> list;
    private int layoutID;
    private String flag[];
    private int ItemIDs[];
    public static HashMap<Integer, Boolean> isSelected;
    private String itemString = null; // 记录每个item中textview的值
    private String keyString[] = null;
    private int idValue[] = null;// id值
    //private static int position;
    private ViewHolder holder = null;

    private int initNum = 0;

    /**
     * SuffixMainAdapter构造方法
     * @param context 上文Activity对象
     * @param list  展示的数据集合
     * @param layoutID  展示的layoutId
     * @param flag
     * @param ItemIDs
     */
    public SuffixMainAdapter(Context context, ArrayList<HashMap<String, String>> list,
                             int layoutID, String flag[], int ItemIDs[]) {
        //Log.i("TAG", "构造方法");
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
        this.layoutID = layoutID;
        this.flag = flag;
        this.ItemIDs = ItemIDs;
        keyString = new String[flag.length];
        idValue = new int[ItemIDs.length];
        System.arraycopy(flag, 0, keyString, 0, flag.length);
        System.arraycopy(ItemIDs, 0, idValue, 0, ItemIDs.length);
        init();
    }

    // 初始化 设置所有checkbox都为未选择
    public void init() {
        isSelected = new HashMap<Integer, Boolean>();
        for (int i = 0; i < list.size(); i++) {
            isSelected.put(i, false);
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            Log.i("check", "holder == null--" + position);
            //this.position = position;
            holder = new ViewHolder();
            view = mInflater.inflate(layoutID, null);
            holder.tv = (TextView) view.findViewById(R.id.typePath);
            holder.cb = (CheckBox) view.findViewById(R.id.typeCB);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            Log.i("check", "holder != null--" + position);
        }

        HashMap<String, String> map = list.get(position);

        if (map != null) {
            itemString = (String) map.get(keyString[0]);
            holder.tv.setText(itemString);
        }

        holder = (SuffixMainAdapter.ViewHolder)view.getTag();

        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    Log.i("ck","ck-sel->" + position);

                    isSelected.put(position,true);

                }else{

                   Log.i("ck","ck-unsel->" + position);

                    isSelected.put(position,false);

                }
            }
        });

        holder.cb.setChecked(isSelected.get(position));

        return view;
    }


    public class ViewHolder {
        public TextView tv;
        public CheckBox cb;
    }
}


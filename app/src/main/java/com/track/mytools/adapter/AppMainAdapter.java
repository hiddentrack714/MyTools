package com.track.mytools.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.track.mytools.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppMainAdapter extends BaseAdapter {

    private AppMainAdapter.ViewHolder holder = null;

    private List<HashMap<String,Object>> listData;

    private Context context;

    public static List<View> viewList = new ArrayList<View>();                    //View对象集合

    public static HashMap<Integer, Boolean> isSelected;

    public AppMainAdapter(Context context,List<HashMap<String,Object>> listData){
        this.context = context;
        this.listData= listData;
        init();
    }

    // 初始化 设置所有checkbox都为未选择
    public void init() {
        isSelected = new HashMap<Integer, Boolean>();
        for (int i = 0; i < listData.size(); i++) {
            isSelected.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){

            holder = new AppMainAdapter.ViewHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_appdetail, null);

            holder.appName = (TextView) convertView.findViewById(R.id.appName);
            holder.appPackageName = (TextView) convertView.findViewById(R.id.appPackageName);
            holder.appVersionName = (TextView) convertView.findViewById(R.id.appVersionName);
            holder.appVersionCode = (TextView) convertView.findViewById(R.id.appVersionCode);
            holder.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
            holder.appSize = (TextView) convertView.findViewById(R.id.appSize);
            holder.appCB = (CheckBox) convertView.findViewById(R.id.appCB);

            convertView.setTag(holder);

        }else{

            holder = (AppMainAdapter.ViewHolder) convertView.getTag();

        }

        HashMap<String,Object> listMap= listData.get(position);

        if(listMap.size() == 0){
            return convertView;
        }

        //初始化参数
        holder.appName.setText(listMap.get("appName").toString());
        holder.appPackageName.setText(listMap.get("appPackageName").toString());
        holder.appVersionName.setText(listMap.get("appVersionName").toString());
        holder.appIcon.setImageDrawable((Drawable)listMap.get("appIcon"));
        holder.appVersionCode.setText("("+listMap.get("appVersionCode").toString()+")");
        holder.appSize.setText(listMap.get("appSize").toString());

        holder.appCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    listData.get(position).put("isCheck",true);
                    isSelected.put(position,true);
                }else{
                    listData.get(position).put("isCheck",false);
                    isSelected.put(position,false);
                }
            }
        });

        //关键性语句，防止滑动到下一页的时候，同一位置同样被选择，导致错乱
        holder.appCB.setChecked(isSelected.get(position));

        return convertView;
    }

    public class ViewHolder {
        public TextView appName;        //应用名称
        public TextView appPackageName; //应用包名
        public TextView appVersionName; //应用版本
        public TextView appVersionCode; //应用小版本
        public ImageView appIcon;        //应用图标
        public TextView appSize;        //应用大小
        public CheckBox appCB;        //应用大小
    }
}

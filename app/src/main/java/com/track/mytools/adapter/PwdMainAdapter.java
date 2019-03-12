package com.track.mytools.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.track.mytools.R;
import com.track.mytools.activity.PwdActivity;
import com.track.mytools.activity.PwdEditActivity;

import java.util.HashMap;
import java.util.List;

public class PwdMainAdapter extends BaseAdapter {

    private Context context;

    private List<HashMap<String,Object>> listData;

    private PwdMainAdapter.ViewHolder holder = null;

    public static HashMap<Integer, Boolean> isSelected;

    public PwdMainAdapter(Context context, List<HashMap<String,Object>> listData){
        this.context = context;
        this.listData = listData;
        init();
    }

    public void init() {
        isSelected = new HashMap<Integer, Boolean>();
        for (int i = 0; i < listData.size(); i++) {
            isSelected.put(i, true);
        }
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            Log.i("PwdMainAdapter_Log", "holder == null--" + position);
            holder = new PwdMainAdapter.ViewHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_pwddetail, null);

            holder.pwdName = (TextView) convertView.findViewById(R.id.pwdName);
            holder.pwdAccount = (TextView) convertView.findViewById(R.id.pwdAccount);
            holder.pwdIcon = (ImageView) convertView.findViewById(R.id.pwdIcon);

            convertView.setTag(holder);

        } else {
            Log.i("PwdMainAdapter_Log", "holder != null--" + position);
            holder = (PwdMainAdapter.ViewHolder) convertView.getTag();
        }

        HashMap<String, Object> map = listData.get(position);

        if (map != null) {

            String pwdNameStr = (String)map.get("pwdName");
            String pwdAccountStr = (String)map.get("pwdAccount");
            String pwdIconStr = (String)map.get("pwdIcon");

            //使用修改完成的值
           holder.pwdName.setText(pwdNameStr);
           holder.pwdAccount.setText(pwdAccountStr);

           if(pwdIconStr != null && !"".equals(pwdIconStr)){
               holder.pwdIcon.setImageDrawable(PwdActivity.pwdActivity.getResources().getDrawable(getIcon(pwdIconStr)));
           }else {
               holder.pwdIcon.setImageDrawable(PwdActivity.pwdActivity.getResources().getDrawable(R.drawable.qt));
           }
        }

        holder.pwdName.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                HashMap<String,Object> item = listData.get(position);
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("pwdName",(String)item.get("pwdName"));
                bundle.putString("pwdAccount",(String)item.get("pwdAccount"));
                bundle.putString("pwdPsd",(String)item.get("pwdPsd"));
                bundle.putString("pwdId",(String)item.get("id"));
                bundle.putString("pwdIcon",(String)item.get("pwdIcon"));
                intent.putExtras(bundle);
                intent.setClass(PwdActivity.pwdActivity, PwdEditActivity.class);
                PwdActivity.pwdActivity.startActivity(intent);
                PwdActivity.locationIndex = position;
            }
        });

        return convertView;
    }

    public class ViewHolder {
        public TextView pwdName;   //名称
        public TextView pwdAccount;     //账号
        public ImageView pwdIcon;//图标
    }

    private int getIcon(String indexStr){
        int i = R.drawable.qt; // 默认为其他
        if("cx".equalsIgnoreCase(indexStr)){
            i = R.drawable.cx;
        }else if("gw".equalsIgnoreCase(indexStr)){
            i = R.drawable.gw;
        }else if("jr".equalsIgnoreCase(indexStr)){
            i = R.drawable.jr;
        }else if("yx".equalsIgnoreCase(indexStr)){
            i = R.drawable.yx;
        }else if("sh".equalsIgnoreCase(indexStr)){
            i = R.drawable.sh;
        }else if("sj".equalsIgnoreCase(indexStr)){
            i = R.drawable.sj;
        }else if("ys".equalsIgnoreCase(indexStr)){
            i = R.drawable.ys;
        }else{
            i = R.drawable.qt;
        }
        return i;
    }
}

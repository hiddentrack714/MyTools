package com.track.mytools.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.track.mytools.R;

import java.util.HashMap;
import java.util.List;

public class WifiMainAdapter extends BaseAdapter {

    private WifiMainAdapter.ViewHolder holder = null;

    private List<HashMap<String,String>> listData;

    private Context context;

    public WifiMainAdapter(Context context, List<HashMap<String,String>> listData){
        this.context = context;
        this.listData= listData;
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

            holder = new WifiMainAdapter.ViewHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_wifidetail, null);

            holder.vhSSid = (TextView) convertView.findViewById(R.id.wifiSSid);
            holder.vhPassword = (TextView) convertView.findViewById(R.id.wifiPassword);

            convertView.setTag(holder);

        }else{

            holder = (WifiMainAdapter.ViewHolder) convertView.getTag();

        }

        HashMap<String,String> listMap= listData.get(position);

        if(listMap.size() == 0){
            return convertView;
        }

        //初始化参数
        holder.vhSSid.setText(listMap.get("ssid"));
        holder.vhPassword.setText(listMap.get("passwrd"));

        /* 标识View对象 */
        //将list_view的ID作为Tag的Key值
        //convertView.setTag(R.id.httpList, position);//此处将位置信息作为标识传递
        //viewList.add(convertView);

        return convertView;
    }

    public class ViewHolder {
        public TextView vhSSid; // ssid
        public TextView vhPassword; //密码
    }
}

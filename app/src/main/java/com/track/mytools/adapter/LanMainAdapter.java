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

public class LanMainAdapter extends BaseAdapter {

    private LanMainAdapter.ViewHolder holder = null;

    private Context context;

    private List<HashMap<String,String>> listData;

    public LanMainAdapter(Context context, List<HashMap<String,String>> listData){
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

            holder = new LanMainAdapter.ViewHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_landetail, null);

            holder.tvName = (TextView) convertView.findViewById(R.id.lanName);
            holder.tvIP = (TextView) convertView.findViewById(R.id.lanIP);
            holder.tvMac = (TextView) convertView.findViewById(R.id.lanMac);

            convertView.setTag(holder);

        }else{

            holder = (LanMainAdapter.ViewHolder) convertView.getTag();

        }

        HashMap<String,String> listMap= listData.get(position);

        if(listMap.size() == 0){
            return convertView;
        }

        //初始化参数
        holder.tvName.setText(listMap.get("name").toString());
        holder.tvIP.setText(listMap.get("ip").toString());
        holder.tvMac.setText(listMap.get("mac").toString());

        /* 标识View对象 */
        //将list_view的ID作为Tag的Key值
        //convertView.setTag(R.id.httpList, position);//此处将位置信息作为标识传递
        //viewList.add(convertView);

        return convertView;
    }

    public class ViewHolder {
        public TextView tvName;   //主机名称
        public TextView tvIP; //主机IP
        public TextView tvMac; //主机Mac
    }
}

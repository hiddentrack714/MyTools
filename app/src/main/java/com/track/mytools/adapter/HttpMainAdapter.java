package com.track.mytools.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.track.mytools.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HttpMainAdapter extends BaseAdapter {


    private HttpMainAdapter.ViewHolder holder = null;

    private List<ConcurrentHashMap<String,Object>> listData;

    private Context context;

    public static List<View> viewList = new ArrayList<View>();                    //View对象集合


    public HttpMainAdapter(Context context,List<ConcurrentHashMap<String,Object>> listData){
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null){

            holder = new ViewHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_httpdetail, null);

            holder.tvNo = (TextView) convertView.findViewById(R.id.httpDownNo);
            holder.pb = (ProgressBar) convertView.findViewById(R.id.httpDownPro);
            holder.tvSize = (TextView) convertView.findViewById(R.id.httpDownSize);
            holder.tvName = (TextView) convertView.findViewById(R.id.httpDownName);

            convertView.setTag(holder);

        }else{

            holder = (ViewHolder) convertView.getTag();

        }

        ConcurrentHashMap<String,Object> listMap= listData.get(position);

        //初始化参数
        holder.tvNo.setText(listMap.get("httpDownNo").toString());
        holder.pb.setProgress(Integer.parseInt(listMap.get("httpDownPro").toString()));
        holder.tvSize.setText(listMap.get("httpDownSize").toString());
        holder.tvName.setText(listMap.get("httpDownName").toString());

        /* 标识View对象 */
        //将list_view的ID作为Tag的Key值
        //convertView.setTag(R.id.httpList, position);//此处将位置信息作为标识传递
        viewList.add(convertView);

        return convertView;
    }

    public class ViewHolder {
        public TextView tvNo; // 线程编号
        public ProgressBar pb;  //下载进度
        public TextView tvSize; //文件大小
        public TextView tvName; //文件名称
    }
}

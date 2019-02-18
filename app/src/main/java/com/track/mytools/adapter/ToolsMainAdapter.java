package com.track.mytools.adapter;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.track.mytools.R;
import com.track.mytools.activity.AppExtractActivity;
import com.track.mytools.activity.CopyActivity;
import com.track.mytools.activity.FTPActivity;
import com.track.mytools.activity.HttpActivity;
import com.track.mytools.activity.IPActivity;
import com.track.mytools.activity.LanActivity;
import com.track.mytools.activity.NLActivity;
import com.track.mytools.activity.PwdActivity;
import com.track.mytools.activity.QrySuffixActivity;
import com.track.mytools.activity.SuffixActivity;
import com.track.mytools.activity.ToolsActivity;
import com.track.mytools.activity.WifiActivity;
import com.track.mytools.activity.YCActivity;
import com.track.mytools.util.ToolsUtil;

import java.util.HashMap;
import java.util.List;

public class ToolsMainAdapter extends BaseAdapter {

    private ToolsMainAdapter.ViewHolder holder = null;

    private List<HashMap<String,Object>> listData;

    private Context context;

    public static HashMap<Integer, Boolean> isUse;

    private static String isRoot = null;

    public ToolsMainAdapter(Context context, List<HashMap<String,Object>> listData){
        this.context = context;
        this.listData= listData;
        init();
    }

    // 初始化 设置所有checkbox都为未选择
    public void init() {
        isUse = new HashMap<Integer, Boolean>();
        for (int i = 0; i < listData.size(); i++) {
            isUse.put(i, true);
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

            holder = new ToolsMainAdapter.ViewHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_toolsdetail, null);

            holder.toolsBtn = (Button) convertView.findViewById(R.id.toolsBtn);

            convertView.setTag(holder);

        }else{

            holder = (ToolsMainAdapter.ViewHolder) convertView.getTag();

        }

        HashMap<String,Object> listMap= listData.get(position);

        if(listMap.size() == 0){
            return convertView;
        }

        String btnName = listMap.get("btnName").toString();

        int btnValue = Integer.parseInt(listMap.get("btnValue").toString());

        int btnId = Integer.parseInt(listMap.get("btnId").toString());

        String btnUse = listMap.get("btnUse").toString();

        //初始化参数
        holder.toolsBtn.setText(btnValue);
        holder.toolsBtn.setId(btnId);
        holder.toolsBtn.setEnabled(isUse.get(position));

        if(isRoot == null){
            isRoot = (ToolsUtil.hasRoot() == true ? "y" : "n");
        }

        if("n".equals(isRoot) && "y".equalsIgnoreCase(listMap.get("needRoot").toString())){
                holder.toolsBtn.setEnabled(false);
                holder.toolsBtn.setText(holder.toolsBtn.getText()+"-未获取Root，无法使用");
        }

        if(!ToolsUtil.hasYC()  && "y".equalsIgnoreCase(listMap.get("needYC").toString())){
                holder.toolsBtn.setEnabled(false);
                holder.toolsBtn.setText(holder.toolsBtn.getText() + "-未刷入YC调度，无法使用");
        }

        holder.toolsBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Message msg = ToolsActivity.toolsActivityHandler.obtainMessage();
                if(v.getId() == R.id.suffixBtn){
                    Log.i("ToolsActivity_Log","后缀删添");
                    msg.obj = SuffixActivity.class;
                }else if(v.getId() == R.id.qrySuBtn){
                    Log.i("ToolsActivity_Log","后缀列表");
                    msg.obj = QrySuffixActivity.class;
                }else if(v.getId() == R.id.httpBtn){
                    Log.i("ToolsActivity_Log","http下载");
                    msg.obj = HttpActivity.class;
                }else if(v.getId() == R.id.copyBtn){
                    Log.i("ToolsActivity_Log","快捷复制");
                    msg.obj = CopyActivity.class;
                }else if(v.getId() == R.id.ftpBtn){
                    Log.i("ToolsActivity_Log","FTP下载");
                    msg.obj = FTPActivity.class;
                }else if(v.getId() == R.id.wifiBtn){
                    Log.i("ToolsActivity_Log","Wifi密码");
                    msg.obj = WifiActivity.class;
                }else if(v.getId() == R.id.lanBtn){
                    Log.i("ToolsActivity_Log","局域网设备");
                    msg.obj = LanActivity.class;
                }else if(v.getId() == R.id.nlBtn){
                    Log.i("ToolsActivity_Log","支付宝获取能量");
                    msg.obj = NLActivity.class;
                }else if(v.getId() == R.id.ycBtn){
                    Log.i("ToolsActivity_Log","yc调度模式切换");
                    msg.obj = YCActivity.class;
                }else if(v.getId() == R.id.ipBtn){
                    Log.i("ToolsActivity_Log","WifiIP静态/DHCP切换");
                    msg.obj = IPActivity.class;
                }else if(v.getId() == R.id.appExtractBtn){
                    Log.i("ToolsActivity_Log","App提取");
                    msg.obj = AppExtractActivity.class;
                }else if(v.getId() == R.id.pwdBtn){
                    Log.i("ToolsActivity_Log","密码本");
                    msg.obj = PwdActivity.class;
                }
                ToolsActivity.toolsActivityHandler.sendMessage(msg);
            }
        });

        return convertView;
    }

    public class ViewHolder {
        public Button toolsBtn;
    }

}

package com.track.mytools.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.PwdEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PwdMainAdapter extends BaseAdapter {

    private Context context;

    private List<HashMap<String,Object>> listData;

    private PwdMainAdapter.ViewHolder holder = null;

    public static List<HashMap<String,Object>> viewList = new ArrayList<>();

    public PwdMainAdapter(Context context,List<HashMap<String,Object>> listData){
        this.context = context;
        this.listData = listData;
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

        if (convertView == null) {
            holder = new PwdMainAdapter.ViewHolder();
            convertView = LayoutInflater.from(this.context).inflate(R.layout.activity_pwddetail, null);

            holder.pwdName = (EditText) convertView.findViewById(R.id.pwdName);
            holder.pwdAccount = (EditText) convertView.findViewById(R.id.pwdAccount);
            holder.pwdPsd = (EditText) convertView.findViewById(R.id.pwdPsd);

            holder.pwdChangeBtn = (Button) convertView.findViewById(R.id.pwdChangeBtn);
            holder.pwdDelBtn = (Button) convertView.findViewById(R.id.pwdDelBtn);
            convertView.setTag(holder);

            HashMap<String,Object> map = new  HashMap<String,Object>();
            map.put("pwdName",holder.pwdName);
            map.put("pwdAccount",holder.pwdAccount);
            map.put("pwdPsd",holder.pwdPsd);
            map.put("pwdChangeBtn",holder.pwdChangeBtn);
            map.put("pwdDelBtn",holder.pwdDelBtn);

            map.put("changeIng",false);

            viewList.add(map);
        } else {
            holder = (PwdMainAdapter.ViewHolder) convertView.getTag();
            Log.i("check", "holder != null--" + position);
        }

        HashMap<String, Object> map = listData.get(position);

        if (map != null) {
            holder.pwdName.setText((String)listData.get(position).get("pwdName"));
            holder.pwdAccount.setText((String)listData.get(position).get("pwdAccount"));
            holder.pwdPsd.setText((String)listData.get(position).get("pwdPsd"));
        }

        holder.pwdChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean changeIng = (Boolean)viewList.get(position).get("changeIng");
                Log.i("upd","位置:" + position);

                if(changeIng == false){
                    //准备修改
                    ((EditText)viewList.get(position).get("pwdName")).setEnabled(true);
                    ((EditText)viewList.get(position).get("pwdAccount")).setEnabled(true);
                    ((EditText)viewList.get(position).get("pwdPsd")).setEnabled(true);

                    viewList.get(position).put("changeIng",true);
                    ((Button)viewList.get(position).get("pwdChangeBtn")).setText("完成");
                }else{
                    //完成修改
                    ((EditText)viewList.get(position).get("pwdName")).setEnabled(false);
                    ((EditText)viewList.get(position).get("pwdAccount")).setEnabled(false);
                    ((EditText)viewList.get(position).get("pwdPsd")).setEnabled(false);

                    viewList.get(position).put("changeIng",false);
                    ((Button)viewList.get(position).get("pwdChangeBtn")).setText("修改");

                    //从控件获取值
                    String pwdName = ((EditText)viewList.get(position).get("pwdName")).getText().toString();
                    String pwdAccount = ((EditText)viewList.get(position).get("pwdAccount")).getText().toString();
                    String pwdPsd = ((EditText)viewList.get(position).get("pwdPsd")).getText().toString();

                    listData.get(position).put("pwdName",pwdName);
                    listData.get(position).put("pwdAccount",pwdAccount);
                    listData.get(position).put("pwdPsd",pwdPsd);
                }
            }
        });

        holder.pwdDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("del","位置:" + position);

                //判断要删除的数据是新增的，还是数据库的
                if(null != listData.get(position).get("id")){
                    SQLiteDatabase sdb = ToolsDao.getDatabase();
                    ToolsDao.delTable(sdb,listData.get(position),PwdEntity.class);
                    sdb.close();
                }
                listData.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public class ViewHolder {
        public EditText pwdName;   //名称
        public EditText pwdAccount;     //账号
        public EditText pwdPsd; //密码
        public Button pwdChangeBtn;   //修改按钮
        public Button pwdDelBtn;   //删除按钮
    }
}

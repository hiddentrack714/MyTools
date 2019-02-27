package com.track.mytools.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.track.mytools.R;
import com.track.mytools.activity.PwdActivity;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.PwdEntity;
import com.track.mytools.util.ToolsUtil;

import java.util.HashMap;
import java.util.List;

public class PwdMainAdapter extends BaseAdapter {

    private Context context;

    private List<HashMap<String,Object>> listData;

    private PwdMainAdapter.ViewHolder holder = null;

    public static HashMap<Integer,HashMap<String,Object>> viewMap = new HashMap<Integer,HashMap<String,Object>>();

    public static HashMap<Integer, Boolean> pwdMap;

    public static HashMap<Integer,String> updMap= new HashMap<Integer,String>();
    public static HashMap<String,EditText> updWidMap= new HashMap<String,EditText>();
    public static int updWidInt;

    public static HashMap<Integer,HashMap<String,String>> edMap= new HashMap<Integer,HashMap<String,String>>();//实时保存修改的内容

    public PwdMainAdapter(Context context, List<HashMap<String,Object>> listData){
        this.context = context;
        this.listData = listData;
        viewMap.clear();
        updMap.clear();
        edMap.clear();
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

            holder.pwdName = (EditText) convertView.findViewById(R.id.pwdName);
            holder.pwdAccount = (EditText) convertView.findViewById(R.id.pwdAccount);
            holder.pwdPsd = (EditText) convertView.findViewById(R.id.pwdPsd);

            holder.pwdChangeBtn = (Button) convertView.findViewById(R.id.pwdChangeBtn);
            holder.pwdDelBtn = (Button) convertView.findViewById(R.id.pwdDelBtn);
            convertView.setTag(holder);

        } else {
            holder = (PwdMainAdapter.ViewHolder) convertView.getTag();
           // Log.i("PwdMainAdapter_Log", "holder != null--" + position);
        }

        HashMap<String, Object> map = listData.get(position);

        if (map != null) {
            holder.pwdName.setText((String)listData.get(position).get("pwdName"));
            holder.pwdAccount.setText((String)listData.get(position).get("pwdAccount"));
            holder.pwdPsd.setText((String)listData.get(position).get("pwdPsd"));
        }

        if(viewMap.get(new Integer(position)) == null){
            HashMap<String, Object> conMap = new HashMap<String, Object>();
            conMap.put("pwdName", holder.pwdName);
            conMap.put("pwdAccount", holder.pwdAccount);
            conMap.put("pwdPsd", holder.pwdPsd);
            conMap.put("pwdChangeBtn", holder.pwdChangeBtn);
            conMap.put("pwdDelBtn", holder.pwdDelBtn);

            viewMap.put(new Integer(position), conMap);

            holder.pwdChangeBtn.setOnClickListener(new ChangeBtnListener(position,holder.pwdName,holder.pwdAccount,holder.pwdPsd,holder.pwdChangeBtn));

            holder.pwdDelBtn.setOnClickListener(new DelBtnListener(position,holder.pwdDelBtn,holder.pwdName));

            holder.pwdName.addTextChangedListener(new EditTextListener("pwdName",position));

            holder.pwdAccount.addTextChangedListener(new EditTextListener("pwdAccount",position));

            holder.pwdPsd.addTextChangedListener(new EditTextListener("pwdPsd",position));
        }

        return convertView;
    }

    public class ViewHolder {
        public EditText pwdName;   //名称
        public EditText pwdAccount;     //账号
        public EditText pwdPsd; //密码
        public Button pwdChangeBtn;   //修改按钮
        public Button pwdDelBtn;   //删除按钮
    }

    /**
     * 监听修改按钮
     */
    class ChangeBtnListener implements View.OnClickListener
    {
        private int position;
        private EditText pwdName;
        private EditText pwdAccount;
        private EditText pwdPsd;
        private Button changeBtn;


        public ChangeBtnListener(int position, EditText pwdName, EditText pwdAccount, EditText pwdPsd,Button changeBtn)
        {
            this.position = position;
            this.pwdName = pwdName;
            this.pwdAccount = pwdAccount;
            this.pwdPsd = pwdPsd;
            this.changeBtn = changeBtn;
        }

        @Override
        public void onClick(View v)
        {
            Log.i("PwdMainAdapter_Log","修改:" + position);
            if(updMap.size() == 0 || updMap.containsKey(new Integer(position))) {

                if (updMap.size() == 0) {
                    //准备修改
                    pwdName.setEnabled(true);
                    pwdAccount.setEnabled(true);
                    pwdPsd.setEnabled(true);

                    //默认名称获取焦点
                    pwdName.setFocusable(true);
                    pwdName.setFocusableInTouchMode(true);
                    pwdName.requestFocus();

                    //弹出键盘
                    InputMethodManager imm = (InputMethodManager) PwdActivity.pwdActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(pwdName, 0);

                    changeBtn.setText("完成");

                    updMap.put(new Integer(position),"y");
                    updWidMap.put("pwdName",pwdName);
                    updWidMap.put("pwdAccount",pwdAccount);
                    updWidMap.put("pwdPsd",pwdPsd);

                    PwdMainAdapter.updWidInt = position;

                } else {
                    //完成修改
                    pwdName.setEnabled(false);
                    pwdAccount.setEnabled(false);
                    pwdPsd.setEnabled(false);

                    changeBtn.setText("修改");

                    //从控件获取值
                    String pwdNameStr = pwdName.getText().toString();
                    String pwdAccountStr = pwdAccount.getText().toString();
                    String pwdPsdStr = pwdPsd.getText().toString();

                    listData.get(position).put("pwdName", pwdNameStr);
                    listData.get(position).put("pwdAccount", pwdAccountStr);
                    listData.get(position).put("pwdPsd", pwdPsdStr);

                    PwdActivity.qryList.get(position).put("pwdName", pwdNameStr);
                    PwdActivity.qryList.get(position).put("pwdAccount", pwdAccountStr);
                    PwdActivity.qryList.get(position).put("pwdPsd", pwdPsdStr);

                    updMap.clear();
                    updWidMap.clear();
                }
            } else{
                ToolsUtil.showToast(PwdActivity.pwdActivity,"请先保存上一条修改，再开启新的编辑",2000);
            }
        }

    }

    /**
     * 监听删除按钮
     */
    class DelBtnListener implements View.OnClickListener
    {
        private int position;
        private Button Btn;
        private EditText pwdName;


        public DelBtnListener(int position, Button currentBtn,EditText pwdName)
        {
            this.position = position;
            this.Btn = currentBtn;
            this.pwdName = pwdName;
        }


        @Override
        public void onClick(View v)
        {
            Log.i("PwdMainAdapter_Log","删除:" + position);
            final AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(PwdActivity.pwdActivity);
            normalDialog.setTitle("提示");
            normalDialog.setMessage("确认删除[" + pwdName.getText() + "]的账号及其密码?");
            normalDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(null != listData.get(position).get("id")){
                                SQLiteDatabase sdb = ToolsDao.getDatabase();
                                ToolsDao.delTable(sdb,listData.get(position),PwdEntity.class);
                                sdb.close();
                            }

//                            listData.remove(position);
//                            PwdActivity.qryList.remove(position);
//                            viewMap.remove(new Integer(position));
//                            notifyDataSetChanged();
                            Message msg = PwdActivity.pwdActivityHandler.obtainMessage();
                            msg.arg1=1;
                            PwdActivity.pwdActivityHandler.sendMessage(msg);
                        }
                    });
            normalDialog.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                        }
                    });
            // 显示
            normalDialog.show();
        }

    }

    class EditTextListener implements TextWatcher {

        private String etName;
        private int position;

        public EditTextListener(String etName,int position){
            this.etName = etName;
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            String str = s.toString();
            HashMap<String,String> map = edMap.get(new Integer(position));

            if(map==null){
                map = new HashMap<String,String>();
            }

            if("pwdName".equals(etName)){
                map.put("pwdName",str);
            }

            if("pwdAccount".equals(etName)){
                map.put("pwdAccount",str);
            }

            if("pwdPsd".equals(etName)){
                map.put("pwdPsd",str);
            }

            edMap.put(new Integer(position),map);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}

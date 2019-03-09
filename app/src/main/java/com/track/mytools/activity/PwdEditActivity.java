package com.track.mytools.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.PwdEntity;
import com.track.mytools.util.DesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PwdEditActivity extends Activity {

    @BindView(R.id.pwdEditIcon)
    Spinner pwdEditIcon;  //名称

    @BindView(R.id.pwdEditName)
    EditText pwdEditName;  //名称

    @BindView(R.id.pwdEditAccount)
    EditText pwdEditAccount;  //账号

    @BindView(R.id.pwdEditPsd)
    EditText pwdEditPsd;  //密码

    @BindView(R.id.pwdEditBtn)
    Button pwdEditBtn;  //确定

    @BindView(R.id.pwdEditSee)
    ImageView pwdEditSee;  //显示/隐藏密码

    private boolean isShowPwd = false;

    private String id = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwdedit);
        ButterKnife.bind(this);

        SimpleAdapter simpleAdapter =new SimpleAdapter(this, getListData(), R.layout.activity_pwdicon,
                new String[]{"npic","namepic"}, new int[]{R.id.pwdIconImage,R.id.pwdIconText});

        pwdEditIcon.setAdapter(simpleAdapter);

        Bundle bundle = getIntent().getExtras();

        //参数传递
        if(bundle != null){
            String pwdIdStr = bundle.getString("pwdId");
            id = pwdIdStr;

            String pwdNameStr = bundle.getString("pwdName");
            String pwdAccountStr = bundle.getString("pwdAccount");
            String pwdPsdStr = bundle.getString("pwdPsd");
            String pwdIconStr = bundle.getString("pwdIcon");

            pwdEditName.setText(pwdNameStr);
            pwdEditAccount.setText(pwdAccountStr);
            pwdEditPsd.setText(DesUtil.desDecrypt(PwdEditActivity.this,pwdPsdStr));
            pwdEditIcon.setSelection(getIconIndex(pwdIconStr));
        }

        //监听保存按钮
        pwdEditBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //String pwdIconStr = pwdEditName.getText().toString();
                String pwdNameStr = pwdEditName.getText().toString();
                String pwdAccountStr = pwdEditAccount.getText().toString();
                String pwdPsdStr = pwdEditPsd.getText().toString();

                Log.i("PwdEditActivity_Log",pwdNameStr+":"+pwdAccountStr+":"+pwdPsdStr);

                SQLiteDatabase sqd = ToolsDao.getDatabase();
                HashMap<String,Object> map = new HashMap<String,Object>();
                map.put("pwdName",pwdNameStr);
                map.put("pwdAccount",pwdAccountStr);
                map.put("pwdPsd",DesUtil.desEncrypt(PwdEditActivity.this,pwdPsdStr));
                map.put("id",id);
                map.put("pwdIcon",getIconName(pwdEditIcon.getSelectedItemPosition()));
                ToolsDao.saveOrUpdIgnoreExsit(sqd,map, PwdEntity.class);

                Intent intent = new Intent();
                intent.setClass(PwdEditActivity.this, PwdActivity.class);
                startActivity(intent);
                finish();
                PwdActivity.pwdActivity.finish();
            }
        });

        //监听隐藏/显示图片
        pwdEditSee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isShowPwd){
                    pwdEditSee.setImageDrawable(PwdActivity.pwdActivity.getResources().getDrawable(R.drawable.hp));
                    pwdEditPsd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isShowPwd = false;
                }else{
                    pwdEditSee.setImageDrawable(PwdActivity.pwdActivity.getResources().getDrawable(R.drawable.sp));
                    pwdEditPsd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isShowPwd = true;
                }

            }
        });

    }

    /**
     * 获取图片对应的说明集合
     * @return
     */
    public List<Map<String, Object>> getListData() {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        //每个Map结构为一条数据，key与Adapter中定义的String数组中定义的一一对应。

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("npic", R.drawable.cx);
        map.put("namepic", "出行");
        list.add(map);

        HashMap<String, Object> map2 = new HashMap<String, Object>();
        map2.put("npic", R.drawable.gw);
        map2.put("namepic", "购物");
        list.add(map2);

        HashMap<String, Object> map3 = new HashMap<String, Object>();
        map3.put("npic", R.drawable.jr);
        map3.put("namepic", "金融");
        list.add(map3);

        HashMap<String, Object> map4 = new HashMap<String, Object>();
        map4.put("npic", R.drawable.yx);
        map4.put("namepic", "游戏");
        list.add(map4);

        HashMap<String, Object> map5 = new HashMap<String, Object>();
        map5.put("npic", R.drawable.sh);
        map5.put("namepic", "生活");
        list.add(map5);

        HashMap<String, Object> map6 = new HashMap<String, Object>();
        map6.put("npic", R.drawable.sj);
        map6.put("namepic", "社交");
        list.add(map6);

        HashMap<String, Object> map7 = new HashMap<String, Object>();
        map7.put("npic", R.drawable.ys);
        map7.put("namepic", "影视");
        list.add(map7);

        HashMap<String, Object> map8 = new HashMap<String, Object>();
        map8.put("npic", R.drawable.qt);
        map8.put("namepic", "其他");
        list.add(map8);

        return list;
    }

    /**
     * 根据名称获取图标下角标
     * @param indexStr
     * @return
     */
    private int getIconIndex(String indexStr){
        int i = 7; // 默认为其他
        if("cx".equalsIgnoreCase(indexStr)){
            i = 0;
        }else if("gw".equalsIgnoreCase(indexStr)){
            i = 1;
        }else if("jr".equalsIgnoreCase(indexStr)){
            i = 2;
        }else if("yx".equalsIgnoreCase(indexStr)){
            i = 3;
        }else if("sh".equalsIgnoreCase(indexStr)){
            i = 4;
        }else if("sj".equalsIgnoreCase(indexStr)){
            i = 5;
        }else if("ys".equalsIgnoreCase(indexStr)){
            i = 6;
        }else{
            i = 7;
        }
        return i;
    }

    /**
     * 获取图标下角标
     * @param indexStr
     * @return
     */
    private String getIconName(int indexStr){
        String x = "qt";  // 默认为其他
        if(0 ==indexStr){
            x = "cx";
        }else if(1 == indexStr){
            x = "gw";
        }else if(2 == indexStr){
            x = "jr";
        }else if(3 == indexStr){
            x = "yx";
        }else if(4 == indexStr){
            x = "sh";
        }else if(5 == indexStr){
            x = "sj";
        }else if(6 == indexStr){
            x = "ys";
        }else{
            x = "qt";
        }
        return x;
    }

}

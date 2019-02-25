package com.track.mytools.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.track.mytools.R;
import com.track.mytools.adapter.PwdMainAdapter;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.PwdEntity;
import com.track.mytools.util.DesUtil;
import com.track.mytools.util.ExcelUtil;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

/**
 * Created by Track on 2017/2/9.
 * 密码记录本
 * Listview中的edittext 在键盘消失的时候，会导致消失，需要实时保存
 */

public class PwdActivity extends BaseKeyboardActivity {

    @BindView(R.id.pwdAddBtn)
    Button pwdAddBtn;  //添加

    @BindView(R.id.pwdSaveBtn)
    Button pwdSaveBtn;//保存

    @BindView(R.id.pwdLeadBtn)
    Button pwdLeadBtn;//导入(txt/excel)

    @BindView(R.id.pwdExpBtn)
    Button pwdExpBtn;//导出(txt/excel)

    @BindView(R.id.pwdList)
    ListView pwdList;

    @BindView(R.id.pwdSearch)
    EditText pwdSearch;

    public static PwdActivity pwdActivity;

    public static List<HashMap<String,Object>> qryList;

    public static List<HashMap<String,Object>> tempQryList;

    private final int EX_FILE_PICKER_RESULT = 0xfa01;
    private String startDirectory = null;// 记忆上一次访问的文件目录路径
    private String chooseFlag = "";

    public static boolean isKeybordShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwdmain);
        ButterKnife.bind(this);

        attachKeyboardListeners();

        pwdActivity = this;

        pwdAddBtn = (Button)findViewById(R.id.pwdAddBtn);
        pwdSaveBtn = (Button)findViewById(R.id.pwdSaveBtn);

        pwdList = (ListView)findViewById(R.id.pwdList);

        //检测是否是非法页面跳转
        if(!ToolsUtil.isLegal()){
            ToolsUtil.showToast(PwdActivity.this,"非法页面跳转",5000);
            finish();
        }

        SQLiteDatabase sdb = ToolsDao.getDatabase();

        qryList = ToolsDao.qryTable(sdb,PwdEntity.class,PwdActivity.this);

        //解密
        for(HashMap<String,Object> map:qryList){
            map.put("pwdPsd",DesUtil.desDecrypt(map.get("pwdPsd").toString()));
        }

        //深度复制
        tempQryList = ToolsUtil.deepCopy((ArrayList) qryList);

        Log.i("PwdActivity_Log","存储密码数量:" + qryList.size());

        PwdMainAdapter pwdMainAdapter = new PwdMainAdapter(PwdActivity.this,tempQryList);

        pwdList.setAdapter(pwdMainAdapter);

        //点击添加密码区域
        pwdAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,Object> map = new HashMap<String,Object>();
                map.put("pwdName","");
                map.put("pwdAccount","");
                map.put("pwdPsd","");
                tempQryList.add(map);
                qryList.add(map);
                pwdMainAdapter.notifyDataSetChanged();
            }
        });


        //点击保存修改的密码
        pwdSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(PwdMainAdapter.updMap.size()!=0){

                    ToolsUtil.showToast(PwdActivity.this,"当前还有未保存的修改，无法保存!",3000);

                }else{

                    List<HashMap<String,Object>> daoList = new ArrayList<HashMap<String,Object>>();

                    daoList = ToolsUtil.deepCopy((ArrayList) qryList);

                    for(int i=0 ;i<daoList.size();i++){
                        SQLiteDatabase sdb = ToolsDao.getDatabase();
                        //加密
                        daoList.get(i).put("pwdPsd",DesUtil.desEncrypt(daoList.get(i).get("pwdPsd").toString()));
                        ToolsDao.saveOrUpdIgnoreExsit(sdb,daoList.get(i),PwdEntity.class);
                    }

                    ToolsUtil.showToast(PwdActivity.this,"保存完成!",3000);
                }
            }
        });

        //监听搜索名称
        pwdSearch.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();

                List<HashMap<String,Object>> tempList = new ArrayList<HashMap<String,Object>>();
                for(HashMap<String,Object> map :qryList){
                    String pwdNameStr = map.get("pwdName").toString();

                    if(pwdNameStr.indexOf(str) >-1){
                        tempList.add(map);
                    }
                }

                tempQryList.clear();

                tempQryList.addAll(tempList);

                pwdMainAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //监听导入按钮
        pwdLeadBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(PwdActivity.this);
                normalDialog.setTitle("选项");
                normalDialog.setMessage("是否需要先下载导入的模板?");
                normalDialog.setPositiveButton("是,并且选择保存位置",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                chooseFlag = "leadDir";
                                choose(ExFilePicker.ChoiceType.DIRECTORIES);
                            }
                        });
                normalDialog.setNegativeButton("否",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                chooseFlag = "leadFile";
                                choose(ExFilePicker.ChoiceType.FILES);
                            }
                        });
                // 显示
                normalDialog.show();
            }
        });

        //监听导出按钮
        pwdExpBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                choose(ExFilePicker.ChoiceType.DIRECTORIES);
            }
        });

    }

    /**
     * 选择文件或者目录
     * @param choiceType
     */
    private void choose(ExFilePicker.ChoiceType choiceType){
        ExFilePicker exFilePicker = new ExFilePicker();
        exFilePicker.setCanChooseOnlyOneItem(true);// 单选
        exFilePicker.setQuitButtonEnabled(true);
        exFilePicker.setChoiceType(choiceType);

        if (TextUtils.isEmpty(startDirectory)) {
            exFilePicker.setStartDirectory(Environment.getExternalStorageDirectory().getPath());
        } else {
            exFilePicker.setStartDirectory(startDirectory);
        }

        exFilePicker.start(PwdActivity.this, EX_FILE_PICKER_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EX_FILE_PICKER_RESULT) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                String path = result.getPath();

                List<String> names = result.getNames();
                FileOutputStream out = null;
                InputStream in = null;
                for (int i = 0; i < names.size(); i++) {
                    File f = new File(path, names.get(i));
                    try {
                        Uri uri = Uri.fromFile(f); //这里获取了真实可用的文件资源
                        if("leadDir".equals(chooseFlag)){
                            //移动模板到选择的文件夹
                            out = new FileOutputStream(uri.getPath()+"/密码本.xls");
                            in = PwdActivity.this.getAssets().open("密码本.xls");
                            byte[] buffer = new byte[512];
                            int readBytes = 0;
                            while ((readBytes = in.read(buffer)) != -1) {
                                out.write(buffer, 0, readBytes);
                            }
                            out.flush();
                            ToolsUtil.showToast(PwdActivity.this,"模板成功保存到:"+uri.getPath()+"/密码本.xls",2000);
                        }else if("leadFile".equals(chooseFlag)){
                            //获取编辑好的密码本
                            List<HashMap<String,Object>> list = null;
                            try{
                                list = ExcelUtil.readExcel(uri.getPath());

                                //解析成功后开始加密，然后向数据库添加
                                for(HashMap<String,Object> map:list){
                                    SQLiteDatabase sqd = ToolsDao.getDatabase();
                                    map.put("pwdPsd",DesUtil.desEncrypt(map.get("pwdPsd").toString()));
                                    ToolsDao.saveOrUpdIgnoreExsit(sqd,map,PwdEntity.class);
                                }

                                ToolsUtil.showToast(PwdActivity.this,"密码导入成功",2000);
                                finish();
                                Intent intent = new Intent();
                                intent.setClass(PwdActivity.this,PwdActivity.class);
                                startActivity(intent);

                            }catch(Exception e){
                                ToolsUtil.showToast(PwdActivity.this,"xls解析失败，请检查格式或是重新下载模板填写",2000);
                            }

                        }else{

                            SQLiteDatabase sqd = ToolsDao.getDatabase();
                            List<HashMap<String,Object>> list = ToolsDao.qryTable(sqd,PwdEntity.class,PwdActivity.this);

                            for(HashMap<String,Object> map:list){
                                map.put("pwdPsd",DesUtil.desDecrypt(map.get("pwdPsd").toString()));
                            }

                            if(ExcelUtil.saveExcel(list,uri.getPath()+"/密码本.xls")){
                                ToolsUtil.showToast(PwdActivity.this,"密码成功导出到:"+uri.getPath()+"/密码本.xls",2000);
                            }else{
                                ToolsUtil.showToast(PwdActivity.this,"密码导出失败",2000);
                            }

                        }
                       uri.getPath();
                        startDirectory = path;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if(in!=null){
                            try{
                                out.close();
                            }catch (Exception e){

                            }
                        }

                        if(in!=null){
                            try{
                                in.close();
                            }catch (Exception e){

                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onShowKeyboard(int keyboardHeight) {
        // do things when keyboard is shown
        //bottomContainer.setVisibility(View.GONE);
        Log.i("PwdActivity_Log","显示键盘");
        isKeybordShow = true;
    }

    @Override
    protected void onHideKeyboard() {
        // do things when keyboard is hidden
        //bottomContainer.setVisibility(View.VISIBLE);
        Log.i("PwdActivity_Log","隐藏键盘");
        isKeybordShow = false;
    }
}

package com.track.mytools.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.track.mytools.R;
import com.track.mytools.adapter.PwdMainAdapter;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.PwdEntity;
import com.track.mytools.util.AesUtil;
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

    @BindView(R.id.pwdLeadBtn)
    Button pwdLeadBtn;//导入(excel)

    @BindView(R.id.pwdExpBtn)
    Button pwdExpBtn;//导出(excel)


    @BindView(R.id.pwdDelBtn)
    Button pwdDelBtn;//全删

    @BindView(R.id.pwdList)
    SwipeMenuListView pwdList;

    @BindView(R.id.pwdSearch)
    EditText pwdSearch;

    public static PwdActivity pwdActivity;

    public static List<HashMap<String,Object>> qryList;

    private final int EX_FILE_PICKER_RESULT = 0xfa01;
    private String startDirectory = null;// 记忆上一次访问的文件目录路径
    private String chooseFlag = "";

    public final static int REQUEST_READ_PHONE_STATE = 0xfa02;

    private PwdMainAdapter pwdMainAdapter;

    public static int locationIndex = 0;

    private ArrayList<HashMap<String,Object>> tempList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwdmain);
        ButterKnife.bind(this);

        pwdActivity = this;

        //检测是否是非法页面跳转
        if(!ToolsUtil.isLegal()){
            ToolsUtil.showToast(PwdActivity.this,"非法页面跳转",5000);
            finish();
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_PHONE_NUMBERS}, REQUEST_READ_PHONE_STATE);
        }else{
            pwdActivityMission();
        }
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
                                    map.put("pwdPsd", AesUtil.aesEncrypt((String)map.get("pwdPsd")));
                                    ToolsDao.saveOrUpdIgnoreExsit(sqd,map,PwdEntity.class);
                                }

                                ToolsUtil.setProperties("isUseFinIdMou","y");
                                ToolsActivity.useFP = true;
                                //默认为识别成功，防止未退出应用再次进入该界面时报错
                                ToolsActivity.passFP = true;

                                ToolsUtil.showToast(PwdActivity.this,"密码导入成功",2000);
                                finish();
                                Intent intent = new Intent();
                                intent.setClass(PwdActivity.this,PwdActivity.class);
                                startActivity(intent);

                            }catch(Exception e){
                                e.printStackTrace();
                                Log.i("PwdActivity_Log",e.getMessage());
                                ToolsUtil.showToast(PwdActivity.this,"xls解析失败，请检查格式或是重新下载模板填写",2000);
                            }

                        }else{

                            SQLiteDatabase sqd = ToolsDao.getDatabase();
                            List<HashMap<String,Object>> list = ToolsDao.qryTable(sqd,PwdEntity.class,PwdActivity.this);

                            for(HashMap<String,Object> map:list){
                                if(null != map.get("pwdPsd")){
                                    map.put("pwdPsd",AesUtil.aesDecrypt((String)map.get("pwdPsd")));
                                }else{
                                    map.put("pwdPsd","");
                                }

                                if(null == map.get("pwdAccount")){
                                    map.put("pwdAccount","");
                                }

                                if(null == map.get("pwdName")){
                                    map.put("pwdName","");
                                }

                                if(null == map.get("pwdIcon")){
                                    map.put("pwdIcon","qt");
                                }
                            }

                            if(ExcelUtil.saveExcel(list,uri.getPath()+"/密码本.xls")){
                                if("expDir".equals(chooseFlag)){
                                    SQLiteDatabase sqd1 = ToolsDao.getDatabase();
                                    ToolsDao.delTable(sqd1,PwdEntity.class);
                                }

                                finish();
                                Intent intent = new Intent();
                                intent.setClass(PwdActivity.this,PwdActivity.class);
                                startActivity(intent);

                                ToolsUtil.showToast(PwdActivity.this,"密码成功导出到:"+uri.getPath()+"/密码本.xls",2000);
                            }else{
                                ToolsUtil.showToast(PwdActivity.this,"密码导出失败",2000);
                            }

                        }
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            pwdActivityMission();
        }else{
            ToolsUtil.showToast(PwdActivity.this,"需要获取手机号码参与加密运算，请授予权限",2000);
            finish();
        }
    }

    /**
     * 主线任务
     *
     */
    public void pwdActivityMission(){

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.GREEN));
                // set item width
                openItem.setWidth(150);
                // set item title
                //openItem.setTitle("编辑");
                // set item title fontsize
                //openItem.setTitleSize(18);
                // set item title font color
                //openItem.setTitleColor(Color.WHITE);
                // set a icon
                openItem.setIcon(R.drawable.bj);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.RED));
                // set item width
                deleteItem.setWidth(150);
                // set item title
                //deleteItem.setTitle("删除");
                // set a icon
                deleteItem.setIcon(R.drawable.sc);
                // set item title font color
                //deleteItem.setTitleColor(Color.WHITE);
                // set item title fontsize
                //deleteItem.setTitleSize(18);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        pwdList.setMenuCreator(creator);

        pwdList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                HashMap<String,Object> item = qryList.get(position);
                switch (index) {
                    case 0:
                        // 编辑
                        // open(item);
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("pwdName",(String)item.get("pwdName"));
                        bundle.putString("pwdAccount",(String)item.get("pwdAccount"));
                        bundle.putString("pwdPsd",(String)item.get("pwdPsd"));
                        bundle.putString("pwdId",(String)item.get("id"));
                        bundle.putString("pwdIcon",(String)item.get("pwdIcon"));
                        intent.putExtras(bundle);
                        intent.setClass(PwdActivity.this, PwdEditActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        // 删除
                        SQLiteDatabase sqd = ToolsDao.getDatabase();
                        ToolsDao.delTable(sqd,item,PwdEntity.class);
                        qryList.remove(position);
                        pwdMainAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        pwdList.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // set MenuStateChangeListener
        pwdList.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });

        // other setting
        // listView.setCloseInterpolator(new BounceInterpolator());

        // test item long click
        pwdList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                HashMap<String,Object> item = qryList.get(position);
                String pwdPsd = (String)item.get("pwdPsd");
                if(pwdPsd!=null){
                    pwdPsd = AesUtil.aesDecrypt(pwdPsd);
                }else{
                    pwdPsd = "";
                }
                ToolsUtil.showToast(PwdActivity.this,"密码为:["+pwdPsd+"]",3000);
                return false;
            }
        });

        // Right
        //pwdList.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);

        // Left
        pwdList.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        SQLiteDatabase sdb = ToolsDao.getDatabase();

        qryList = ToolsDao.qryTable(sdb,PwdEntity.class,PwdActivity.this);

        tempList = ToolsUtil.deepCopy((ArrayList)qryList);

        if(qryList.size() > 0 && !ToolsActivity.useFP){
            ToolsUtil.showToast(PwdActivity.this,"密码本存有密码，但是未启用指纹识别",2000);
        }

        Log.i("PwdActivity_Log","存储密码数量:" + qryList.size());

        pwdMainAdapter = new PwdMainAdapter(PwdActivity.this,qryList);

        pwdList.setAdapter(pwdMainAdapter);

        //修改或添加后，滑动到指定位置
        if(locationIndex != 0 ){
            Log.i("PwdActivity_Log","滑动位置:" + locationIndex);
            if(locationIndex == -1){
                pwdList.setSelection(qryList.size() - 1);
            }else {
                pwdList.setSelection(locationIndex);
            }
            locationIndex = 0;
        }

        //点击添加密码区域
        pwdAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PwdActivity.this, PwdEditActivity.class);
                startActivity(intent);
                locationIndex = -1;
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

                                final AlertDialog.Builder normalDialog =
                                        new AlertDialog.Builder(PwdActivity.this);
                                normalDialog.setTitle("选项");
                                normalDialog.setMessage("导入前,是否需要删除原始密码本数据?");
                                normalDialog.setPositiveButton("是",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                SQLiteDatabase sqd = ToolsDao.getDatabase();
                                                ToolsDao.delTable(sqd,PwdEntity.class);

                                                chooseFlag = "leadFile";
                                                choose(ExFilePicker.ChoiceType.FILES);
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
                // 显示
                normalDialog.show();
            }
        });

        //监听导出按钮
        pwdExpBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(PwdActivity.this);
                normalDialog.setTitle("选项");
                normalDialog.setMessage("导出后,是否需要删除原始密码本数据?");
                normalDialog.setPositiveButton("是",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                chooseFlag = "expDir";
                                choose(ExFilePicker.ChoiceType.DIRECTORIES);
                            }
                        });
                normalDialog.setNegativeButton("否",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                choose(ExFilePicker.ChoiceType.DIRECTORIES);
                            }
                        });
                // 显示
                normalDialog.show();
            }
        });

        //监听搜索内容
        pwdSearch.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pwdNameStr = s.toString();

                if(!"".equals(pwdNameStr)){
                    List<HashMap<String,Object>> temp = new ArrayList<HashMap<String,Object>>();
                    for(HashMap<String,Object> map:tempList){
                        if(map.get("pwdName").toString().toUpperCase().indexOf(s.toString().toUpperCase())>-1){
                            temp.add(map);
                        }
                    }
                    qryList.clear();
                    qryList.addAll(temp);
                }else{
                    qryList.clear();
                    qryList.addAll(tempList);
                }

                pwdMainAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //监听全部删除
        pwdDelBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {


                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(PwdActivity.this);
                normalDialog.setTitle("选项");
                normalDialog.setMessage("是否确认全部删除?");
                normalDialog.setPositiveButton("是",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                SQLiteDatabase sqd = ToolsDao.getDatabase();
                                ToolsDao.delTable(sqd,PwdEntity.class);
                                qryList.clear();
                                pwdMainAdapter.notifyDataSetChanged();
                            }
                        });
                normalDialog.setNegativeButton("否",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                // 显示
                normalDialog.show();
            }
        });

    }
}

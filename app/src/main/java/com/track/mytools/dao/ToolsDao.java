package com.track.mytools.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.track.mytools.activity.MainActivity;
import com.track.mytools.annotation.TableNameAnnotation;
import com.track.mytools.enums.AssetsEnum;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolsDao {

    private static SQLiteDatabase sdb;

    /**
     * 获取当前数据库
     * @return
     */
    public static SQLiteDatabase getDatabase(){

        if(sdb == null || !sdb.isOpen()){
            synchronized (ToolsDao.class){
                if(sdb == null || !sdb.isOpen()){
                    try{
                        sdb = SQLiteDatabase.openOrCreateDatabase(String.valueOf(AssetsEnum.ASSETS_DB_PATH),null);
                    }catch(Exception e){
                        Log.e("ToolsDao_Log",e.getMessage());
                    }
                }
            }
        }

        return sdb;
    }


    /**
     * @param db 数据库连接
     * @param map 数据map
     * @param cl 实体类
     * 删除的sql
     *
     */
    public static void delTable(SQLiteDatabase db,HashMap<String,Object> map,Class cl){
        TableNameAnnotation tn =  (TableNameAnnotation)cl.getAnnotation(TableNameAnnotation.class);

        String tableName = tn.value();
        //删除条件
        String whereClause = "id=?";
        //删除条件参数
        String[] whereArgs={map.get("id").toString()};
        //执行删除
        db.delete(tableName,whereClause,whereArgs);

        db.close();
    }


    /**
     * @param db 数据库连接
     * @param cl 实体类
     * 查询的sql
     *
     */
    public static List<HashMap<String,Object>> qryTable(SQLiteDatabase db, Class cl, Context context){
        TableNameAnnotation tn = (TableNameAnnotation)cl.getAnnotation(TableNameAnnotation.class);
        //获取实体上注解对应的表名，
        String tableName = tn.value();

        List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();

        Cursor cursor = null;

        try {

            cursor = db.query(tableName, null, null, null, null, null, null);

        }catch(android.database.sqlite.SQLiteException e){
            //数据库查询失败，采取重新复制db策略
            //应用更新的时候，防止db有更新
            Log.e("ToolsDao_Log","db查询异常:" + e.getMessage());
            boolean isSuccess = MainActivity.initCopyFile(String.valueOf(AssetsEnum.ASSETS_DB_PATH),"mytools.db",context);

            if(isSuccess){
                Log.e("ToolsDao_Log","db初始化完成,请重新进入该界面");

                cursor = db.query(tableName, null, null, null, null, null, null);
            }
        }catch(Exception e){

            Log.e("ToolsDao_Log",e.getMessage());
        }

        Field[] declaredFields = cl.getDeclaredFields();
        Field[] fields = cl.getFields();

        Field[] allFields = new Field[declaredFields.length + fields.length];

        System.arraycopy(declaredFields,0,allFields,0,declaredFields.length);

        System.arraycopy(fields,0,allFields,declaredFields.length,fields.length);

        Log.i("ToolsDao_Log",cl.getName()+"属性数量:" +allFields.length);

        //判断游标是否为空
        if(cursor.moveToFirst()){
            Log.i("ToolsDao_Log","游标长度:" + cursor.getCount()+"");
            //遍历游标
            for(int i = 0 ;i < cursor.getCount() ;i++){
                cursor.moveToPosition(i);
                //获得用户名
                HashMap<String,Object> map = new HashMap<String,Object>();
                for(Field f : allFields){
                    int index = cursor.getColumnIndex(f.getName());
                    Log.i("ToolsDao_Log",f.getName()+":"+index);
                    String value = cursor.getString(index);
                    map.put(f.getName(),value);
                }
                list.add(map);
            }
        }

        cursor.close();

        db.close();

        return list;
    }

    /**
     *保存数据无论表是否存在
     * @param db 数据库连接
     * @param map 数据对象
     * @param cl 实体类
     */
    public static void saveOrUpdIgnoreExsit(SQLiteDatabase db, HashMap<String,Object> map,Class cl){
        //1,根据实体类，获取表名
        TableNameAnnotation tn =  (TableNameAnnotation)cl.getAnnotation(TableNameAnnotation.class);

        String tableName = tn.value();
        Log.i("ToolsDao_Log",tableName);
        //2,判断表是否存在
        boolean isExist = tabbleIsExist(db,tableName);
        Log.i("ToolsDao_Log",tableName+":"+isExist);
        if(!isExist){
            //3,创建表
            String table="create table "+ tableName +"(id integer primary key autoincrement";

            for(Map.Entry<String,Object> entry: map.entrySet()){
                table = table +", "+ entry.getKey()+" text";
            }

            table = table + ")";

            Log.i("ToolsDao_Log",table);

            db.execSQL(table);
        }

        //4,判断数据是修改，还是新增
        ContentValues cValue = new ContentValues();
        for(Map.Entry<String,Object> entry: map.entrySet()){
            cValue.put(entry.getKey(),(String)entry.getValue());
        }

        //5,根据是否存在id判断是插入还是更新
        if(null == map.get("id")){
            cValue.remove("id");
            db.insert(tableName,null,cValue);
        }else{
            String whereClause = "id=?";
            String[] whereArgs={map.get("id").toString()};
            db.update(tableName,cValue,whereClause,whereArgs);
        }

        db.close();
    }


    /**
     * 判断某张表是否存在
     * @param db 数据连接
     * @param tableName 表名
     * @return
     */
    public static boolean tabbleIsExist(SQLiteDatabase db ,String tableName){
        boolean result = false;
        if(tableName == null){
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"+tableName.trim()+"' ";
            Log.i("ToolsDao_Log",sql);
            cursor = db.rawQuery(sql, null);
            cursor.moveToPosition(0);
            int count = cursor.getInt(0);
            if(count>0){
                result = true;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }

}

package com.track.mytools.dao;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ToolsDao {

    /**
     * 获取当前数据库
     * @return
     */
    public static SQLiteDatabase getDataase(){
        try{
             return SQLiteDatabase.openOrCreateDatabase("/sdcard/UCdownloads/mytools.db",null);
        }catch(Exception e){
            Log.e("ToolsDao",e.getMessage());
        }
        return null;
    }

    /**
     * 创建表
     * @return
     */
    private void createTable(SQLiteDatabase db){
        //创建表SQL语句
        String stu_table="create table usertable(_id integer primary key autoincrement,sname text,snumber text)";
        //执行SQL语句
        db.execSQL(stu_table);
    }

}

package com.track.mytools.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Track on 2017/1/16.
 */

public class ToolsEntiy {
    public static String path ;   //目录
    public static String type;  //文件类型
    public static String[] suFilter;   //过滤的后缀名
    public static List<String> errorSuList = new ArrayList<String>();  //后缀删除/添加失败的目录合集

}

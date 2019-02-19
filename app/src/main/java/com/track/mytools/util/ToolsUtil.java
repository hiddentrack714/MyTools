package com.track.mytools.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.track.mytools.activity.SuffixActivity;
import com.track.mytools.activity.ToolsActivity;
import com.track.mytools.entity.HttpThreadEntity;
import com.track.mytools.enums.AssetsEnum;
import com.track.mytools.exception.HttpException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Track on 2017/1/14.
 */

public class ToolsUtil {

    /**
     * 删除后缀
     * @param filePath 位置
     * @param suType 后缀类型 jpg,mp4,avi
     * @param proBar 进度条
     * @param viewPercent 进度条数字
     * @return
     */
    public static void delSuffix(String filePath,String suType, ProgressBar proBar, TextView viewPercent){
        File rootFile = new File(filePath);

        if(rootFile.isDirectory()){
            File [] file =  rootFile.listFiles();
            for (File chFile:file) {
                if(chFile.isDirectory()){
                    delSuffix(chFile.getAbsolutePath(),suType,proBar,viewPercent);
                }else{
                    int pointNum = chFile.getName().split("\\.").length;
                    if (suType.equalsIgnoreCase(chFile.getName().split("\\.")[pointNum - 1])){
                        String newPath = chFile.getAbsolutePath().substring(0,chFile.getAbsolutePath().lastIndexOf("."));

                        if(chFile.renameTo(new File(newPath))){
                            File finishFile = new File(newPath);
                            //删除成功
                            Log.i("su","当前进度:" + SuffixActivity.finshFileNum);

                            Message msg = SuffixActivity.suffixActivityHandler.obtainMessage();
                             //将进度值作为消息的参数包装进去
                            msg.arg1 = SuffixActivity.finshFileNum ;
                             //将消息发送给主线程的Handler
                            msg.obj = SuffixActivity.finshFileNum + "/" + SuffixActivity.dealFileNum;

                            SuffixActivity.suffixActivityHandler.sendMessage(msg);

                            SuffixActivity.finshFileNum ++;
                        }else{
                            continue;
                        }
                    }
                }
            }
        }else{
            Log.i("su","当前路径不是目录");
        }
    }

    /**
     * 添加后缀
     * @param filePath 位置
     * @param suType 后缀类型  jpg,mp4,avi
     * @param proBar 进度条
     * @param viewPercent 进度条数字
     * @return
     */
    public static void addSuffix(String filePath, String suType, ProgressBar proBar, TextView viewPercent){
        File rootFile = new File(filePath);
        if(rootFile.isDirectory()){
            File [] file =  rootFile.listFiles();
            for (File chFile:file) {
                if(chFile.isDirectory()){
                    addSuffix(chFile.getAbsolutePath(),suType, proBar, viewPercent);
                }else{
                    if(chFile.getName().indexOf(".") == -1){
                        //不包含.直接添加后缀
                        File newFile = new File(chFile.getAbsolutePath() + "." + suType);
                        if(chFile.renameTo(newFile)){
                            //修改进度条

                            Log.i("su","当前进度:" + SuffixActivity.finshFileNum);

                            Message msg = SuffixActivity.suffixActivityHandler.obtainMessage();
                            //将进度值作为消息的参数包装进去
                            msg.arg1 = SuffixActivity.finshFileNum ;
                            //将消息发送给主线程的Handler
                            msg.obj = SuffixActivity.finshFileNum + "/" + SuffixActivity.dealFileNum;

                            SuffixActivity.suffixActivityHandler.sendMessage(msg);

                            SuffixActivity.finshFileNum ++;
                        }else{
                            continue;
                        }
                    }else{
                        //包含.
                        String suNow = chFile.getName().substring(chFile.getName().lastIndexOf(".") + 1);   //找到目前的后缀
                        boolean tempBoolean = checkFilter(suNow); ///检查是否在后缀集合中
                        //未找到相同的过滤后缀，可以修改
                        if(tempBoolean && !suType.equalsIgnoreCase(suNow)){
                            if(chFile.renameTo(new File(chFile.getAbsolutePath() + "." + suType))){
                                //修改进度条
                                File finshFile = new File(chFile.getAbsolutePath() + "." + suType);

                                Log.i("name",finshFile.getAbsolutePath());
                                Log.i("su","当前进度:" + SuffixActivity.finshFileNum);

                                Message msg = SuffixActivity.suffixActivityHandler.obtainMessage();
                                //将进度值作为消息的参数包装进去
                                msg.arg1 = SuffixActivity.finshFileNum ;
                                //将消息发送给主线程的Handler
                                msg.obj = SuffixActivity.finshFileNum + "/" + SuffixActivity.dealFileNum;

                                SuffixActivity.suffixActivityHandler.sendMessage(msg);

                                SuffixActivity.finshFileNum ++;

                            }else{
                                continue;
                            }
                        }
                    }

                }
            }
        }else{
            Log.i("su","当前路径不是目录");
        }
    }

    /**
     * 自定义后缀显示时间
     * @param activity
     * @param word
     * @param time
     */
    public static void showToast(final Activity activity, final String word, final long time){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(activity, word, Toast.LENGTH_LONG);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        toast.cancel();
                    }
                }, time);
            }
        });
    }

    /**
     * 计算所有待处理文件数量
     * @param filePath
     * @param suType
     * @param aod  确定是带后缀，还是不带后缀  0:del;1:add
     * @return
     */
    public static int countNum(String filePath, String suType, int aod){
        File rootFile = new File(filePath);
        if(rootFile.isDirectory()){
           // Log.i("su1","当前文件是目录");
            File [] file =  rootFile.listFiles();
            for (File chFile:file) {
                if(chFile.isDirectory()){
                    countNum(chFile.getAbsolutePath(),suType, aod);
                }else{
                  //  Log.i("su","文件名:" + chFile.getName());
                    if(0 == aod){
                        String suNow = chFile.getName().substring(chFile.getName().lastIndexOf(".") + 1);
                        if(suType.equalsIgnoreCase(suNow)){
                           // Log.i("su1","数量+1:" + dealFileNum);
                            SuffixActivity.dealFileNum ++;
                        }
                    }else{
                        //fix 20170118 增加对于有“.”，且后缀名不为过滤文件的检测
                        String suNow = chFile.getName().substring(chFile.getName().lastIndexOf(".") + 1);
                       // Log.i("check",suNow);
                        if((!suType.equalsIgnoreCase(suNow) && checkFilter(suNow)) || chFile.getName().lastIndexOf(".") == -1){
                           // Log.i("su1","数量+1:" + dealFileNum);
                            SuffixActivity.dealFileNum ++;
                        }
                    }
                }
            }
        }
        return SuffixActivity.dealFileNum;
    }

    /**
     * 检测后缀是否在过滤选项中
     * @param suNow 当前后缀
     * @return
     */
    public static boolean checkFilter(String suNow){
        Boolean tempBoolean = true;
        for (String suStr: SuffixActivity.suffixArrayFilter) {
            if(suStr.equalsIgnoreCase(suNow)){
                //找到相同后缀，直接跳出检测
                Log.i("su2","找到过滤后缀:" + suStr);
                tempBoolean = false;
                break;
            }
        }
        Log.i("su4",tempBoolean + "");
        return tempBoolean;
    }

    /**
     * 查询后缀集合
     * @param filePath
     * @return
     */
    public static List<HashMap<String,String>> qrySuffixNum(String filePath,String suffixType){

        File rootFile = new File(filePath);

        if(rootFile.isDirectory()){

            File [] file =  rootFile.listFiles();

            for (File chFile:file) {
                if(chFile.isDirectory()){
                    //如果是文件夹，则继续该方法递归迭代
                    qrySuffixNum(chFile.getAbsolutePath(),suffixType);
                }else{
                    //获取当前文件的后缀名
                    String fileName = chFile.getName();
                    if(fileName.indexOf(".") != -1){
                        //带后缀
                        //bug,对于非常用后缀，显示异常
                        String suType = fileName.substring(fileName.lastIndexOf(".") + 1,fileName.length());
                        //fix 检测当前文件的后缀是否在常用后缀中，如果不在，则为未知文件
                        boolean temp = false;

                        List<String> templist = new ArrayList<String>();

                        for (String str : SuffixActivity.suffixArrayFilter) {
                            templist.add(str);
                        }

                        templist.add(suffixType);

                        for (String str:templist){
                            if (str.equalsIgnoreCase(suType)){
                                //常用文件
                                //判断当前后缀是否在集合中
                                temp = true;
                                break;
                            }
                        }

                        if(temp){
                            HashMap<String,Object> map = checkTypeIn(SuffixActivity.list,suType);

                            Log.i("xxxxxxxx",fileName +"---"+map.get("FLAG"));

                            if((Boolean) map.get("FLAG")){
                                //已存在，直接拿出来+1,在塞回去
                                Log.i("ch","更新:" + suType);
                                int listNo = Integer.parseInt(map.get("NO")+"");
                                int nowNum = Integer.parseInt(SuffixActivity.list.get(listNo).get("NUM")) + 1;
                                Log.i("ch",suType+"更新后:" + nowNum);
                                SuffixActivity.list.get(listNo).put("NUM",nowNum+"");
                            }else{
                                //还不存在,新建MAP
                                Log.i("ch","新建:" + suType.toLowerCase());
                                HashMap<String,String> newMap = new  HashMap<String,String>();
                                newMap.put("TYPE",suType.toLowerCase());
                                newMap.put("NUM",1+"");
                                SuffixActivity.list.add(newMap);
                            }
                            List<String> val =  SuffixActivity.pathMap.get(suType.toLowerCase());
                            val.add(chFile.getAbsolutePath());
                        }else{
                            //未知文件
                            HashMap<String,Object> map = checkTypeIn(SuffixActivity.list,"未知");
                            if((boolean) map.get("FLAG")){
                                //存在
                                int listNo = Integer.parseInt(map.get("NO")+"");
                                int nowNum = Integer.parseInt(SuffixActivity.list.get(listNo).get("NUM")) + 1;
                                Log.i("ch","未知.更新后:" + nowNum);
                                SuffixActivity.list.get(listNo).put("NUM",nowNum+"");
                            }else{
                                //不存在
                                HashMap<String,String> newMap = new  HashMap<String,String>();
                                newMap.put("TYPE","未知");
                                newMap.put("NUM",1+"");
                                SuffixActivity.list.add(newMap);
                            }
                            List<String> val =  SuffixActivity.pathMap.get("未知");
                            val.add(chFile.getAbsolutePath());
                        }

                    }else{
                      //不带后缀
                        HashMap<String,Object> map = checkTypeIn(SuffixActivity.list,"未知");
                        if((boolean) map.get("FLAG")){
                            //存在
                            int listNo = Integer.parseInt(map.get("NO")+"");
                            int nowNum = Integer.parseInt(SuffixActivity.list.get(listNo).get("NUM")) + 1;
                            Log.i("ch","更新后:" + nowNum);
                            SuffixActivity.list.get(listNo).put("NUM",nowNum+"");
                        }else{
                            //不存在
                            HashMap<String,String> newMap = new  HashMap<String,String>();
                            newMap.put("TYPE","未知");
                            newMap.put("NUM",1+"");
                            SuffixActivity.list.add(newMap);
                        }
                        List<String> val =  SuffixActivity.pathMap.get("未知");
                        val.add(chFile.getAbsolutePath());
                    }
                }
            }
        }else{
            Log.i("su1","当前路径不是目录");
        }

        return SuffixActivity.list;
    }

    /**
     * 检测当前类型是否已经在集合中
     * @param list
     * @param type
     * @return
     */
    public static HashMap<String,Object> checkTypeIn(List<HashMap<String,String>> list, String type){
        HashMap<String,Object> mapp = new  HashMap<String,Object>();
        mapp.put("FLAG",false);  //默认为没有

        //1,先判断集合大小
        int listNum = list.size();
        if(listNum > 0){
            for (int i = 0; i < list.size(); i++) {
                if(list.get(i).get("TYPE").equalsIgnoreCase(type)){
                   // Log.i("xxxxxxxx",type +"已存在");
                    mapp.put("FLAG",true);
                    mapp.put("NO",i);
                }
            }
        }
        return mapp;
    }

    /**
     * 检查多选删除是否有选中的文件
     * @param map
     * @return
     */
    public static boolean checkCB(HashMap<Integer,Boolean> map){

        Set<HashMap.Entry<Integer,Boolean>> set  = map.entrySet();

        for (HashMap.Entry<Integer,Boolean> retry: set
             ) {
            if (retry.getValue() == true){
                return true;
            }
        }

        return false;
    }

    /**
     * 检查http下载链接，获取文件大小
     * @param str
     * @throws IOException
     */
    public static HashMap<String,Object> down(String str) throws IOException {
        URL url = new URL(str);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为5秒
        conn.setConnectTimeout(5*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        HashMap<String,Object> map= new HashMap<String,Object>();

        if(404 == conn.getResponseCode()) {
            conn.disconnect();
            //404，下载的文件不存在，所以文件大小为0
            map.put("fileSize",0);
            return map;
        }

        //得到输入流
        InputStream inputStream = conn.getInputStream();

        //文件大小
        int fileSize = conn.getContentLength();
        map.put("fileSize",fileSize);
        map.put("fileStream",inputStream);

        return map;
    }

    /**
     * 保存文件到本地，并且自我更新下载视图
     * @param inputStream
     * @param dir
     * @param str
     * @param pb
     * @return
     * @throws IOException
     */
    public static boolean saveFile(InputStream inputStream, String dir, String str, ProgressBar pb, HttpThreadEntity httpThreadEntity) throws IOException,HttpException {
        byte[] buffer = new byte[512];

        int len = 0;

        int proLen = 0;

        //文件保存位置
        String savePath = dir;

        File saveDir = new File(savePath);
        //判断文件夹是否存在
        if(!saveDir.exists()){
            saveDir.mkdir();
        }

        File file = new File(saveDir + File.separator + str.substring(str.lastIndexOf("/") + 1));

        FileOutputStream fos = new FileOutputStream(file);

        while((len = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
            proLen = proLen + len;
            pb.setProgress(proLen);
        }

        fos.flush();

        if(fos!=null){
            fos.close();
        }

        if(inputStream!=null){
            inputStream.close();
        }

        return true;
    }

    /**
     * 取两位小数点
     * @param str
     * @return
     */
    public static String takePointTwo(String str){

            return str.substring(0,str.lastIndexOf(".") + 3);
    }

    /**
     * 执行快捷命令
     * @param commod
     */
    public static void exeCommod(String [] commod){
        Process process = null;
        DataOutputStream dos = null;
        for(int i = 0 ;i < commod.length ; i++){
            try {
                process = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(process.getOutputStream());
                dos.writeBytes(commod[i] + "\n");
                dos.writeBytes("exit\n");
                dos.flush();
                process.waitFor();
//                while(true){
//                    ActivityManager activityManager=(ActivityManager) getSystemService(ACTIVITY_SERVICE);
//                    String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
//                    Log.i("nowActivity",runningActivity);
//                }
//              Thread.sleep(3000);
            } catch (Exception e) {
                // return false;
            } finally {
                try {
                    if (dos != null) {
                        dos.close();
                    }
                    process.destroy();
                } catch (Exception e) {
                }
            }
        }

    }

    /**
     * 判断当前手机是否有ROOT权限
     * @return
     */
    public static boolean hasRoot()
    {
        Process process = null;
        DataOutputStream os = null;
        try
        {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0)
            {
                return true;
            } else
            {
                return false;
            }
        } catch (Exception e)
        {
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "
                    + e.getMessage());
            return false;
        } finally
        {
            try
            {
                if (os != null)
                {
                    os.close();
                }
                if(process != null)
                {
                    process.destroy();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断是否刷入yc调度
     * @return
     */
    public static boolean hasYC(){
        File file1 = new File("/data/wipe_mode");
        File file2 = new File("/data/powercfg");

        if(file1.exists() || file2.exists()){
            return true;
        }else{
            return false;
        }

    }

    /**
     * 获取当前activity
     * @return
     */
    public static Activity getCurrentActivity () {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
                    null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断跳转是合法，防止跳过指纹检测
     * @return
     */
    public static boolean isLegal(){
        File file = new File(String.valueOf(AssetsEnum.ASSETS_PROPERTIES_PATH));
        Properties properties = new Properties();

        try {
            Reader s = new FileReader(file);
            properties.load(s);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if("y".equals(properties.get("isUseFinIdMou"))){
            if(!ToolsActivity.passFP){
                //非法界面跳转
                return false;
            }
        }
        return true;
    }

    /**
     * 深复制
     * @param list yuan源集合
     * @return
     */
    public static ArrayList<HashMap<String,Object>> deepCopy(ArrayList<HashMap<String,Object>> list){
        ArrayList<HashMap<String,Object>> deepList = new ArrayList<HashMap<String,Object>>(list.size());
        for(HashMap<String,Object> map : list){
            HashMap<String,Object> temp = new HashMap<String,Object>();
            for(Map.Entry<String,Object> entry:map.entrySet()){
                temp.put(entry.getKey(),entry.getValue());
            }
            deepList.add(temp);
        }
        return deepList;
    }

}

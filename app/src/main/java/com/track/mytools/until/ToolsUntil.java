package com.track.mytools.until;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.track.mytools.activity.SuffixActivity;
import com.track.mytools.entity.HttpThreadEntity;
import com.track.mytools.entity.ToolsEntiy;
import com.track.mytools.exception.HttpException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Track on 2017/1/14.
 */

public class ToolsUntil {
    public SuffixActivity suffixActivity;
    public static int dealFileNum = 0;   //处理文件总数量
    public static int finshFileNum = 0;  //处理完成的数量
    public static int errorFileNum = 0;  //处理失败的数量
    public static List<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
    public static HashMap<String,List<String>> pathMap = new HashMap<String,List<String>>();

    static {
        List<String> templist = new ArrayList<String>();
        for (String str : ToolsEntiy.suFilter
                ) {
            templist.add(str);
        }

        templist.add(ToolsEntiy.type);
        for (String str:templist
             ) {
            List<String> list = new ArrayList<String>();
            pathMap.put(str,list);//占位
        }
        List<String> list = new ArrayList<String>();
        pathMap.put("未知",list);
    }

    public ToolsUntil(SuffixActivity suffixActivity){
        this.suffixActivity = suffixActivity;
    }

    /**
     * 删除后缀
     * @param filePath 位置
     * @param suType 后缀类型 jpg,mp4,avi
     * @param proBar 进度条
     * @param viewPercent 进度条数字
     * @return
     */
    public boolean delSuffix(String filePath,String suType, ProgressBar proBar, TextView viewPercent){
        File rootFile = new File(filePath);
        // Toast.makeText(mainActivity, "路径:" +  filePath + ",删除后缀类型:" + suType, Toast.LENGTH_SHORT).show();
        //Log.i("su","删除后缀");
        //Log.i("su","开始检查路径:" + filePath);
      //  int startNum = 1;
        if(rootFile.isDirectory()){
           // Log.i("su","当前文件是目录");
            File [] file =  rootFile.listFiles();
            //Log.i("su","开始迭代根目录");
           // Log.i("su","文件数量:" + file.length);
            for (File chFile:file) {
                if(chFile.isDirectory()){
                    // delSuffix(String filePath,String suType);
                   // Log.i("su","文件夹名:" + chFile.getName());
                    delSuffix(chFile.getAbsolutePath(),suType,proBar,viewPercent);
                }else{
                    //Log.i("su","文件名:" + chFile.getName());
                    int pointNum = chFile.getName().split("\\.").length;
                    if (suType.equalsIgnoreCase(chFile.getName().split("\\.")[pointNum - 1])){
                        //类型相同，开始修改后缀
                        //fix 20170116 增加对多“.”名称的支持
                       // Log.i("su","--->" + chFile.getAbsolutePath());
                        String newPath = chFile.getAbsolutePath().substring(0,chFile.getAbsolutePath().lastIndexOf("."));
                       // Log.i("su","新文件名:" + newPath);
                        if(chFile.renameTo(new File(newPath))){
                            //break;
                            File finishFile = new File(newPath);
                            if(finishFile.exists()){
                                //删除成功
                                Log.i("su","当前进度:" + finshFileNum);
                                //proBar.setProgress(finshFileNum);
                                //viewPercent.setText(finshFileNum + "/" + dealFileNum);

                                Message msg = SuffixActivity.handler.obtainMessage();
                                 //将进度值作为消息的参数包装进去
                                msg.arg1 = finshFileNum ;
                                 //将消息发送给主线程的Handler
                                msg.obj = finshFileNum + "/" + dealFileNum;

                                SuffixActivity.handler.sendMessage(msg);

                                finshFileNum ++;
                            }else{
                                //删除失败
                                errorFileNum++;
                                Log.e("error","删除失败:" + newPath);
                                ToolsEntiy.errorSuList.add(newPath);
                            }
                        }else{
                            ToolsEntiy.errorSuList.add(newPath);
                            errorFileNum++;
                            continue;
                        }
                    }
                }
            }
        }else{
            //Toast.makeText(mainActivity, "路径有误", Toast.LENGTH_SHORT).show();
            Log.i("su","当前路径不是目录");
        }
        //proBar.setVisibility(View.GONE);
        //ToolsUntil.showToast(suffixActivity, "后缀删除完成...", 1000);
        return true;
    }

    /**
     * 添加后缀
     * @param filePath 位置
     * @param suType 后缀类型  jpg,mp4,avi
     * @param proBar 进度条
     * @param viewPercent 进度条数字
     * @return
     */
    public boolean addSuffix(String filePath, String suType, ProgressBar proBar, TextView viewPercent){
        File rootFile = new File(filePath);
        // Toast.makeText(mainActivity, "路径:" +  filePath + ",删除后缀类型:" + suType, Toast.LENGTH_SHORT).show();
        //int startNum = 1;
       // Log.i("suu","初始进度:" + proBar.getProgress());
        //Log.i("su","添加后缀");
        //Log.i("su","开始检查路径:" + filePath);
        if(rootFile.isDirectory()){
           // Log.i("su","当前文件是目录");
            File [] file =  rootFile.listFiles();
           // Log.i("su","开始迭代根目录");
           // Log.i("su","文件数量:" + file.length);
            for (File chFile:file) {
                if(chFile.isDirectory()){
                    // delSuffix(String filePath,String suType);
                   // Log.i("su","文件夹名:" + chFile.getName());
                    addSuffix(chFile.getAbsolutePath(),suType, proBar, viewPercent);
                }else{
                  //  Log.i("su","文件名:" + chFile.getName());
                    //fix 20170116 增加对多“.”名称的支持,目录下文件全部添加后缀
                    //fix 20170117 对于已经添加后缀名的文文件，不再添加
                    //fix 20170118 增加后缀过滤，对于已经有后缀的非选定的后缀，不再增加

                    //检查当前文件时是否含有"."
                    //1,带"."----检测后缀是否是过滤选定的后缀，不是的话添加，是的话过滤
                    //2,不带.----直接添加后缀

                    if(chFile.getName().indexOf(".") == -1){
                        //不包含.直接添加后缀
                        File newFile = new File(chFile.getAbsolutePath() + "." + suType);
                        if(chFile.renameTo(newFile)){
                            //修改进度条
                            //File finshFile = new File(chFile.getAbsolutePath() + "." + suType);
                            if(newFile.exists()){
                                Log.i("su","当前进度:" + finshFileNum);
                                //proBar.setProgress(finshFileNum);
                                //viewPercent.setText(finshFileNum + "/" + dealFileNum);

                                Message msg = SuffixActivity.handler.obtainMessage();
                                //将进度值作为消息的参数包装进去
                                msg.arg1 = finshFileNum ;
                                //将消息发送给主线程的Handler
                                msg.obj = finshFileNum + "/" + dealFileNum;

                                SuffixActivity.handler.sendMessage(msg);


                                finshFileNum ++;
                              //  Log.i("newFile","--->" + newFile.getAbsolutePath());
                            }else{
                                errorFileNum++;
                                Log.e("error","添加失败:" + newFile.getAbsolutePath());
                                ToolsEntiy.errorSuList.add(newFile.getAbsolutePath());
                            }
                        }else{
                            Log.e("fail","失败:" + newFile.getAbsolutePath());
                            ToolsEntiy.errorSuList.add(newFile.getAbsolutePath());
                            errorFileNum++;
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

                                if(finshFile.exists()){
                                    Log.i("name",finshFile.getAbsolutePath());
                                    Log.i("su","当前进度:" + finshFileNum);
                                    //proBar.setProgress(finshFileNum);
                                    //viewPercent.setText(finshFileNum + "/" + dealFileNum);

                                    Message msg = SuffixActivity.handler.obtainMessage();
                                    //将进度值作为消息的参数包装进去
                                    msg.arg1 = finshFileNum ;
                                    //将消息发送给主线程的Handler
                                    msg.obj = finshFileNum + "/" + dealFileNum;

                                    SuffixActivity.handler.sendMessage(msg);

                                    finshFileNum ++;
                                }else{
                                    Log.e("error","添加失败:" + chFile.getAbsolutePath() + "." + suType);
                                    ToolsEntiy.errorSuList.add(chFile.getAbsolutePath() + "." + suType);
                                    errorFileNum++;
                                }
                            }else{
                                errorFileNum++;
                                ToolsEntiy.errorSuList.add(chFile.getAbsolutePath() + "." + suType);
                                continue;
                            }
                        }
                    }


//                    String suNow = chFile.getName().substring(chFile.getName().lastIndexOf(".") + 1);   //找到目前的后缀
//                    if(!suType.equalsIgnoreCase(suNow)){
//                        //已经有后缀的文件名，不改变
//                       // Log.i("su","新文件名:" + chFile.getAbsolutePath() + "." + suType);
//                        if(chFile.renameTo(new File(chFile.getAbsolutePath() + "." + suType))){
//                            //修改进度条
//                            Log.i("su","当前进度:" + finshFileNum);
//                            proBar.setProgress(finshFileNum);
//                            viewPercent.setText(finshFileNum + "/" + dealFileNum);
//                            finshFileNum ++;
//                        }else{
//                            continue;
//                        }
//                    }
                }
            }
        }else{
            //Toast.makeText(mainActivity, "路径有误", Toast.LENGTH_SHORT).show();
            Log.i("su","当前路径不是目录");
        }
        //Log.i("suu","终始进度:" + proBar.getProgress());
        //proBar.setVisibility(View.GONE);
        //ToolsUntil.showToast(suffixActivity, "后缀添加完成...", 1000);
        return true;
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
       // Log.i("su","开始检查路径:" + filePath);
        //int fileNum = 0;
        if(rootFile.isDirectory()){
           // Log.i("su1","当前文件是目录");
            File [] file =  rootFile.listFiles();
           // Log.i("su1","开始迭代根目录");
            //Log.i("su1","文件数量:" + file.length);
            for (File chFile:file) {
                if(chFile.isDirectory()){
                    countNum(chFile.getAbsolutePath(),suType, aod);
                }else{
                  //  Log.i("su","文件名:" + chFile.getName());
                    if(0 == aod){
                        String suNow = chFile.getName().substring(chFile.getName().lastIndexOf(".") + 1);
                        if(suType.equalsIgnoreCase(suNow)){
                           // Log.i("su1","数量+1:" + dealFileNum);
                            dealFileNum ++;
                        }
                    }else{
                        //fix 20170118 增加对于有“.”，且后缀名不为过滤文件的检测
                        String suNow = chFile.getName().substring(chFile.getName().lastIndexOf(".") + 1);
                       // Log.i("check",suNow);
                        if((!suType.equalsIgnoreCase(suNow) && checkFilter(suNow)) || chFile.getName().lastIndexOf(".") == -1){
                           // Log.i("su1","数量+1:" + dealFileNum);
                            dealFileNum ++;
                        }
                    }
                }
            }
        }else{
          //  Log.i("su1","当前路径不是目录");
        }
        return dealFileNum;
    }

    /**
     * 检测后缀是否在过滤选项中
     * @param suNow 当前后缀
     * @return
     */
    public static boolean checkFilter(String suNow){
        Boolean tempBoolean = true;
        for (String suStr: ToolsEntiy.suFilter) {
            if(suStr.equalsIgnoreCase(suNow)){
                //tempNum++;
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
    public static List<HashMap<String,String>> qrySuffixNum(String filePath){

        File rootFile = new File(filePath);
        if(rootFile.isDirectory()){
            Log.i("su1","当前文件是目录");
            File [] file =  rootFile.listFiles();
            Log.i("su1","开始迭代根目录");
            for (File chFile:file) {
                if(chFile.isDirectory()){
                    qrySuffixNum(chFile.getAbsolutePath());
                }else{
                    //获取当前文件的后缀名
                    String fileName = chFile.getName();
                    if(fileName.indexOf(".") != -1){
                        //带后缀
                        //bug,对于非常用后缀，显示异常
                        String suType = fileName.substring(fileName.lastIndexOf(".") + 1,fileName.length());
                        //fix 检测当前文件的后缀是否在常用后缀中，如果不在，则为位置文件
                        boolean temp = false;

                        //String [] cloneArray = ToolsEntiy.suFilter.clone();

                        List<String> templist = new ArrayList<String>();

                        for (String str : ToolsEntiy.suFilter
                             ) {
                            templist.add(str);
                        }

                        templist.add(ToolsEntiy.type);

//                        String [] cloneArray = (String[])templist.toArray();
                       // cloneArray[cloneArray.length - 1] = ToolsEntiy.type;
                       // Log.i("su6",cloneArray.length + ":" + ToolsEntiy.suFilter.length);
                        //cloneArray[cloneArray.length] = ToolsEntiy.type;

                        for (String str:templist){
                            if (str.equalsIgnoreCase(suType)){
                                //常用文件
                                //判断当前后缀是否在集合中
                                temp = true;
                                break;
                            }
                        }
                        if(temp){
                            HashMap<String,Object> map = checkTypeIn(list,suType);

                            Log.i("xxxxxxxx",fileName +"---"+map.get("FLAG"));

                            if((Boolean) map.get("FLAG")){
                                //已存在，直接拿出来+1,在塞回去
                                Log.i("ch","更新:" + suType);
                                int listNo = Integer.parseInt(map.get("NO")+"");
                                int nowNum = Integer.parseInt(list.get(listNo).get("NUM")) + 1;
                                Log.i("ch",suType+"更新后:" + nowNum);
                                list.get(listNo).put("NUM",nowNum+"");
                            }else{
                                //还不存在,新建MAP
                                Log.i("ch","新建:" + suType.toLowerCase());
                                HashMap<String,String> newMap = new  HashMap<String,String>();
                                newMap.put("TYPE",suType.toLowerCase());
                                newMap.put("NUM",1+"");
                                list.add(newMap);
                            }
                            List<String> val =  pathMap.get(suType.toLowerCase());
                            val.add(chFile.getAbsolutePath());
                        }else{
                            //未知文件
                            HashMap<String,Object> map = checkTypeIn(list,"未知");
                            if((boolean) map.get("FLAG")){
                                //存在
                                int listNo = Integer.parseInt(map.get("NO")+"");
                                int nowNum = Integer.parseInt(list.get(listNo).get("NUM")) + 1;
                                Log.i("ch","未知.更新后:" + nowNum);
                                list.get(listNo).put("NUM",nowNum+"");
                            }else{
                                //不存在
                                HashMap<String,String> newMap = new  HashMap<String,String>();
                                newMap.put("TYPE","未知");
                                newMap.put("NUM",1+"");
                                list.add(newMap);
                            }
                            List<String> val =  pathMap.get("未知");
                            val.add(chFile.getAbsolutePath());
                        }

                    }else{
                      //不带后缀
                        HashMap<String,Object> map = checkTypeIn(list,"未知");
                        if((boolean) map.get("FLAG")){
                            //存在
                            int listNo = Integer.parseInt(map.get("NO")+"");
                            int nowNum = Integer.parseInt(list.get(listNo).get("NUM")) + 1;
                            Log.i("ch","更新后:" + nowNum);
                            list.get(listNo).put("NUM",nowNum+"");
                        }else{
                            //不存在
                            HashMap<String,String> newMap = new  HashMap<String,String>();
                            newMap.put("TYPE","未知");
                            newMap.put("NUM",1+"");
                            list.add(newMap);
                        }
                        List<String> val =  pathMap.get("未知");
                        val.add(chFile.getAbsolutePath());
                    }
                }
            }
        }else{
            Log.i("su1","当前路径不是目录");
        }



        return list;
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

            if(!httpThreadEntity.isFileBoolean()){
                //文件下载检测超时，跳出下载方法
                throw new HttpException("文件下载超时");
            }
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

    public static void main(String args[]){
        System.out.print("11111111111");
    }
}

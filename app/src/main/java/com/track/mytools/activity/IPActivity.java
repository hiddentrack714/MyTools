package com.track.mytools.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.track.mytools.R;
import com.track.mytools.dao.ToolsDao;
import com.track.mytools.entity.StaticIPEntity;
import com.track.mytools.util.ToolsUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IPActivity extends Activity {

    @BindView(R.id.staticWifiList)
    Spinner staticWifiList;//WifiList

    @BindView(R.id.staticPassword)
    EditText staticPassword;//密码

    @BindView(R.id.staticIp)
    EditText staticIp;//静态IP

    @BindView(R.id.staticGateWay)
    EditText staticGateWay;//网关

    @BindView(R.id.staticSuffix)
    EditText staticSuffix;//网络前置长度

    @BindView(R.id.staticDNS1)
    Spinner staticDNS1;//DNS1

    @BindView(R.id.iplayout)
    LinearLayout iplayout;//静态Ip设置属性布局框

    @BindView(R.id.wifilayout)
    LinearLayout wifilayout;//WiFiID和password属性布局框

    @BindView(R.id.ipRG)
    RadioGroup ipRG;

    @BindView(R.id.ipSetBtn)
    Button ipSetBtn;//设置按钮

    @BindView(R.id.ipForgetBtn)
    Button ipForgetBtn;//忘记按钮

    @BindView(R.id.ipUpdBtn)
    Button ipUpdBtn;//修改按钮

    @BindView(R.id.staticWifiStatus)
    Switch staticWifiStatus; //wifi开关

    @BindView(R.id.linearLayoutWifiSafe)
    LinearLayout linearLayoutWifiSafe; //安全性

    @BindView(R.id.linearLayoutWifiId)
    LinearLayout linearLayoutWifiId; //ssid

    @BindView(R.id.staticwifiSafeList)
    Spinner staticwifiSafeList;//安全性

    @BindView(R.id.staticWifiId)
    EditText staticWifiId;//wifiid

    private WifiManager mwifiManager;
    private String wifiId;

    private static boolean isUpd = false; // 是否正在修改

    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;
    //两个危险权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean mHasPermission;

    private static List<ScanResult> wifiList;

    public static Handler ipActivityHandler;

    private HashMap<String,Integer> wifiIdMap = new HashMap<String,Integer>();//wifi:id键值对

    private int spinnerTime = 0; // 判断spinner初始化状态

    private List<String> tempList;

    public static Handler ipActivityStateHandler;

    public static IPActivity ipActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);
        ButterKnife.bind(this);

        ipActivity = this;

        spinnerTime++;

        mwifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        //查询静态IP设置的参数
        SQLiteDatabase sdb = ToolsDao.getDatabase();
        HashMap<String,Object> map = ToolsDao.qryTable(sdb, StaticIPEntity.class,IPActivity.this).get(0);
        staticIp.setText(map.get("staticIp").toString());
        staticGateWay.setText(map.get("staticGateWay").toString());
        staticSuffix.setText(map.get("staticSuffix").toString());

        //判断是否开启Wifi模块
        if(mwifiManager.isWifiEnabled()){
            staticWifiStatus.setChecked(true);
        }else{
            staticWifiStatus.setChecked(false);
            iplayout.setVisibility(View.GONE);
            wifilayout.setVisibility(View.GONE);
        }

        //获取定位服务权限
        mHasPermission = checkPermission();
        if (!mHasPermission) {
            Log.i("IPActivity_Log","没有权限");
            requestLocationPermission();
        }else{
            Log.i("IPActivity_Log","拥有权限");
            wifiList = getWifiList();
        }

        //获取Wifi列表，并且赋值到下拉列表
        tempList = new ArrayList<String>();
        int wifiIndex = 0;
        for(ScanResult sr : wifiList){
            //Log.i("IPActivity_Log",sr.SSID + "-" + sr.capabilities);
            String wifiId = "";

            if (sr.capabilities.contains("WPA") || sr.capabilities.contains("wpa")) {
                wifiId = sr.SSID + " - WPA/WPA2 PSK";
            } else if (sr.capabilities.contains("WEP") || sr.capabilities.contains("wep")) {
                wifiId = sr.SSID + " - WEP";
            } else {
                wifiId = sr.SSID + " - 无";
            }

            tempList.add(wifiId);
            wifiIdMap.put(sr.SSID,wifiIndex);
            wifiIndex++;
        }

        tempList.add("添加网络");

        ArrayAdapter adpter = new ArrayAdapter(this,R.layout.activity_wifilist,R.id.wifilistText,tempList);
        staticWifiList.setAdapter(adpter);

        //检测当前Wifi是DHCP还是静态连接
        String wifiStr = getWifiSetting(this);
        boolean isWifi = isWifi(this);

        //判断是否在Wifi连接下
        if(isWifi){
            //Log.i("IPActivity_Log","当前Wifi状态:" + wifiStr);
            List<HashMap<String,String>> list = WifiActivity.getWifigroup();//手机已经记住名字的WiFi集合

            //针对已有WiFi列表做键值对
            wifiId = getWifiId();
            for(HashMap wifiMap : list){
                String ssid = wifiMap.get("ssid").toString();
                if(wifiId.indexOf(ssid) > 0){
                    Log.i("IPActivity_Log","WIFI:" + wifiIdMap);
                    Log.i("IPActivity_Log","ssid:" + ssid);
                    int index = wifiIdMap.get(ssid);
                    staticWifiList.setSelection(index);
                    staticPassword.setText(wifiMap.get("passwrd").toString());
                }
            }

            if("DHCP".equals(wifiStr)){
                //隐藏IP设置属性布局
                iplayout.setVisibility(View.GONE);
                ipRG.check(R.id.wifiMode1);
            }else{
                ipRG.check(R.id.wifiMode2);
            }
        }else{
            Log.i("IPActivity_Log","当前Wifi状态:未连接");
            ipRG.check(R.id.wifiMode3);
            iplayout.setVisibility(View.GONE);
        }

        //运行在主线程的Handler,它将监听所有的消息（Message）
        ipActivityHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                //接受到另一个线程的Message，拿到它的参数，这个参数代表了进度
                ipRG.check(arg0.arg1);
                if(arg0.arg1 == R.id.wifiMode1){
                    iplayout.setVisibility(View.GONE);
                }

                if(arg0.arg1 == R.id.wifiMode1 || arg0.arg1 == R.id.wifiMode2){
                    List<HashMap<String,String>> list = WifiActivity.getWifigroup();
                    wifiId = getWifiId();
                    for(HashMap wifiMap:list){
                        String ssid = wifiMap.get("ssid").toString();
                        if(wifiId.indexOf(ssid) > 0){
                            staticWifiList.setSelection(wifiIdMap.get(ssid));
                            staticPassword.setText(wifiMap.get("passwrd").toString());
                        }
                    }
                }
                return false;
            }
        });

        ipActivityStateHandler = new Handler(new Handler.Callback(){

            @Override
            public boolean handleMessage(Message msg) {
                int id =  ipRG.getCheckedRadioButtonId();
                if(id == R.id.wifiMode3){
                    String wifiStr = getWifiSetting(IPActivity.this);
                    if("DHCP".equals(wifiStr)){
                        ipRG.check(R.id.wifiMode1);
                    }else{
                        ipRG.check(R.id.wifiMode2);
                    }
                }
                return false;
            }
        });

        //监听选择按钮
        ipRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.wifiMode1){
                    iplayout.setVisibility(View.GONE);
                }else if(checkedId == R.id.wifiMode2){
                    iplayout.setVisibility(View.VISIBLE);
                }else{
                    iplayout.setVisibility(View.GONE);
                }
            }
        });

        //监听连接键
        ipSetBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                int id =  ipRG.getCheckedRadioButtonId();
                WifiConfiguration mWifiConfiguration;

                String staticWifiIdStr = staticWifiList.getSelectedItem().toString(); // ssid
                String staticPasswordStr = staticPassword.getText().toString();       //password
                Log.i("IPActivity_Log","WIFILIST:" + staticWifiIdStr);
                if("".equals(staticPasswordStr)){
                    ToolsUtil.showToast(IPActivity.this,"请输入密码",2000);
                    return;
                }

                String wifiSafe = staticwifiSafeList.getSelectedItem().toString();
                if("添加网络".equals(staticWifiIdStr)){
                    //手动添加
                    wifiSafe = staticwifiSafeList.getSelectedItem().toString();
                    if("".equals(staticWifiId.getText().toString())){
                        ToolsUtil.showToast(IPActivity.this,"请输入SSID",2000);
                        return;
                    }
                    staticWifiIdStr = staticWifiId.getText().toString();
                    Log.i("IPActivity_Log","手动添加模式");
                }else{
                    //自动
                    wifiSafe = staticWifiIdStr.split(" - ")[1];
                    staticWifiIdStr = staticWifiIdStr.split(" - ")[0];

                    Log.i("IPActivity_Log","选择模式");
                }

                int wifiSafeInt = 0;

                if("无".equals(wifiSafe)){
                    wifiSafeInt = 1;
                }else if("WEP".equals(wifiSafe)){
                    wifiSafeInt = 2;
                }else{
                    wifiSafeInt = 3;
                }

                Log.i("IPActivity_Log","SSID:" + staticWifiIdStr);
                Log.i("IPActivity_Log","PASSWORD:" + staticPasswordStr);
                Log.i("IPActivity_Log","SAFE:" + wifiSafeInt);


                if(id == R.id.wifiMode1){
                    //DHCP
                    mWifiConfiguration = CreateWifiInfo(staticWifiIdStr, staticPasswordStr, wifiSafeInt);
                    int wcgID = mwifiManager.addNetwork(mWifiConfiguration);
                    boolean bbb = mwifiManager.enableNetwork(wcgID, true);
                    Log.i("IPActivity_Log",bbb+"");
                    if(!bbb){
                        ToolsUtil.showToast(IPActivity.this,"连接异常，请重试!",2000);
                    }
                }else if(id == R.id.wifiMode2){
                    //静态
                    String staticIpStr = staticIp.getText().toString();
                    String staticGateWayStr = staticGateWay.getText().toString();
                    int staticSuffixInt = Integer.parseInt(staticSuffix.getText().toString());
                    String staticDNS1Str = staticDNS1.getSelectedItem().toString().split(":")[1];

                    mWifiConfiguration = CreateWifiInfo(staticWifiIdStr, staticPasswordStr, wifiSafeInt);

                    int wcgID = mwifiManager.addNetwork(mWifiConfiguration);
                    boolean bbb = mwifiManager.enableNetwork(wcgID, true);
                    if(!bbb){
                        ToolsUtil.showToast(IPActivity.this,"连接异常，请重试!",2000);
                    }else{
                        try {
                            setStaticIpConfiguration(mwifiManager, mWifiConfiguration,
                                    InetAddress.getByName(staticIpStr), staticSuffixInt,
                                    InetAddress.getByName(staticGateWayStr),
                                    InetAddress.getAllByName(staticDNS1Str));
                        }catch(Exception e){
                            Log.e("IPActivity_Log",e.getMessage());
                        }
                    }
                }else{
                    //未连接
                    if(mwifiManager.isWifiEnabled()){
                        //是否是连接状态
                        if(!isWifi(IPActivity.this)){
                            mWifiConfiguration = CreateWifiInfo(staticWifiIdStr, staticPasswordStr, wifiSafeInt);
                            int wcgID = mwifiManager.addNetwork(mWifiConfiguration);
                            boolean bbb = mwifiManager.enableNetwork(wcgID, true);
                            Log.i("IPActivity_Log",bbb+"");
                            if(!bbb){
                                ToolsUtil.showToast(IPActivity.this,"连接异常，请重试!",2000);
                            }
                        }else{
                            ToolsUtil.showToast(IPActivity.this,"操作无效!",2000);
                        }
                    }else{
                        ToolsUtil.showToast(IPActivity.this,"还未开启Wifi，无法连接!",2000);
                    }
                }
            }
        });

        //监听忘记按钮
        ipForgetBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                wifiId = getWifiId();
                if(mwifiManager.isWifiEnabled()){
                    if(!forgetWifi()){
                        ToolsUtil.showToast(IPActivity.this,"请手动忘记Wifi密码!",3000);
                    }
                }else{
                    ToolsUtil.showToast(IPActivity.this,"当前未连接Wifi，不能忘记密码!",3000);
                }
            }
        });

        //监听修改按钮
        ipUpdBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(isUpd){
                    //完成修改
                    isUpd = false;
                    staticIp.setEnabled(false);//静态IP
                    staticGateWay.setEnabled(false);//网关
                    staticSuffix.setEnabled(false);//网络前置长度
                    ipUpdBtn.setText("修改");

                    HashMap<String,Object> updMap = new  HashMap<String,Object>();
                    updMap.put("staticIp",staticIp.getText().toString());
                    updMap.put("staticGateWay",staticGateWay.getText().toString());
                    updMap.put("staticSuffix",staticSuffix.getText().toString());
                    updMap.put("id",map.get("id"));

                    SQLiteDatabase sqd = ToolsDao.getDatabase();
                    ToolsDao.saveOrUpdIgnoreExsit(sqd,updMap,StaticIPEntity.class);

                }else{
                    //修改中
                    isUpd = true;
                    staticIp.setEnabled(true);//静态IP
                    staticGateWay.setEnabled(true);//网关
                    staticSuffix.setEnabled(true);//网络前置长度
                    ipUpdBtn.setText("完成");
                }
            }
        });

        //监听WiFi开启
        staticWifiStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mwifiManager.setWifiEnabled(true);
                    wifilayout.setVisibility(View.VISIBLE);

                    //开启一个线程，判断打开WiFi开关后，是否有连接上WiFi，并且判断连接模式
                    ListenThread lt = new ListenThread(IPActivity.this);
                    lt.start();
                }else{
                    mwifiManager.setWifiEnabled(false);
                    wifilayout.setVisibility(View.GONE);
                    iplayout.setVisibility(View.GONE);
                    ipRG.check(R.id.wifiMode3);
                    staticPassword.setText("");
                }
            }
        });

        //wifi列表点击监听
        staticWifiList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("IPActivity_Log","index:" + position);
                if(spinnerTime > 0){
                    String ssidStr = tempList.get(position);
                    if("添加网络".equals(ssidStr)){
                        //展示手动输入ssid，密码类型和密码
                        linearLayoutWifiSafe.setVisibility(View.VISIBLE);
                        linearLayoutWifiId.setVisibility(View.VISIBLE);
                    }else{
                        linearLayoutWifiSafe.setVisibility(View.GONE);
                        linearLayoutWifiId.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 检查是否已经授予权限
     * @return
     */
    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 忘记当前wifi或指定密码
     */
    private boolean forgetWifi(){
        List<WifiConfiguration> wifiConfigs = mwifiManager.getConfiguredNetworks();
        boolean isSuccess = false;
        for (WifiConfiguration wifiConfig : wifiConfigs) {
            String ssid = wifiConfig.SSID;
            if (ssid.equals(wifiId)) {
                if(mwifiManager.removeNetwork(wifiConfig.networkId)){
                    if(mwifiManager.saveConfiguration()){
                        isSuccess =  true;
                    }
                }
                break;
            }
        }

        return isSuccess;
    }

    /**
     * 获取WifiID名称
     * @return
     */
    private String getWifiId(){
        WifiInfo info = mwifiManager.getConnectionInfo();
        wifiId = info != null ? info.getSSID() : null;
        return wifiId;
    }

    /**
     * 获取当前Wifi信息
     * @param context
     * @return
     */
    public String getWifiSetting(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        DhcpInfo dhcpInfo=wifiManager.getDhcpInfo();

        if(dhcpInfo.leaseDuration==0){
            return "StaticIP";
        }else{
            return "DHCP";
        }
    }

    @SuppressWarnings("unchecked")
    public static void setStaticIpConfiguration(WifiManager manager,
                                                WifiConfiguration config, InetAddress ipAddress, int prefixLength,
                                                InetAddress gateway, InetAddress[] dns)
            throws ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException, InstantiationException {
        // First set up IpAssignment to STATIC.
        Object ipAssignment = getEnumValue(
                "android.net.IpConfiguration$IpAssignment", "STATIC");
        callMethod(config, "setIpAssignment",
                new String[] { "android.net.IpConfiguration$IpAssignment" },
                new Object[] { ipAssignment });

        // Then set properties in StaticIpConfiguration.
        Object staticIpConfig = newInstance("android.net.StaticIpConfiguration");

        Object linkAddress = newInstance("android.net.LinkAddress",
                new Class[] { InetAddress.class, int.class }, new Object[] {
                        ipAddress, prefixLength });
        setField(staticIpConfig, "ipAddress", linkAddress);
        setField(staticIpConfig, "gateway", gateway);
        ArrayList<Object> aa = (ArrayList<Object>) getField(staticIpConfig,
                "dnsServers");
        aa.clear();
        for (int i = 0; i < dns.length; i++)
            aa.add(dns[i]);
        callMethod(config, "setStaticIpConfiguration",
                new String[] { "android.net.StaticIpConfiguration" },
                new Object[] { staticIpConfig });
        System.out.println("conconconm" + config);
        int updateNetwork = manager.updateNetwork(config);
        boolean saveConfiguration = manager.saveConfiguration();

        int netId = manager.addNetwork(config);
        manager.disableNetwork(netId);
        boolean  flag  = manager.enableNetwork(netId, true);
        Log.e("IPActivity_Log",netId+"");
        Log.e("IPActivity_Log",flag+"");

    }

    private static Object newInstance(String className)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, NoSuchMethodException,
            IllegalArgumentException, InvocationTargetException {
        return newInstance(className, new Class[0], new Object[0]);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object newInstance(String className,
                                      Class[] parameterClasses, Object[] parameterValues)
            throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ClassNotFoundException {
        Class clz = Class.forName(className);
        Constructor constructor = clz.getConstructor(parameterClasses);
        return constructor.newInstance(parameterValues);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getEnumValue(String enumClassName, String enumValue)
            throws ClassNotFoundException {
        Class enumClz = (Class) Class.forName(enumClassName);
        return Enum.valueOf(enumClz, enumValue);
    }

    private static void setField(Object object, String fieldName, Object value)
            throws IllegalAccessException, IllegalArgumentException,
            NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.set(object, value);
    }

    @SuppressWarnings("rawtypes")
    private static void callMethod(Object object, String methodName,
                                   String[] parameterTypes, Object[] parameterValues)
            throws ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException {
        Class[] parameterClasses = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
            parameterClasses[i] = Class.forName(parameterTypes[i]);

        Method method = object.getClass().getDeclaredMethod(methodName,
                parameterClasses);
        method.invoke(object, parameterValues);
    }



    /**
     * 直接使用set方法调用 可能遇到需要地址转换方法如下：
     *
     */
    public static String int2ip(int ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
    }

    /**
     * 创建WifiConfiguration
     * @param SSID
     * @param Password
     * @param Type
     * @return
     */
    public WifiConfiguration CreateWifiInfo(String SSID, String Password,
                                            int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID+"\"";

        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {

            Boolean c=    mwifiManager.removeNetwork(tempConfig.networkId);
            Log.e("创建新的",""+c);
        }

        if (Type == 1) // WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) // WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 断曾经连接过得WiFi中是否存在指定SSID的WifiConfiguration
     *
     */
    public WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mwifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            System.out.println("existingConfig" + existingConfig.SSID);
            if (existingConfig.SSID.equals("\"" + SSID+"\"")) {
                return existingConfig;
            }
        }
        return null;
    }


    private static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }


    /**
     * 判断是否连接着wifi
     * @param mContext
     * @return
     */
    private static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info= connectivityManager.getActiveNetworkInfo();
        if (info!= null
                && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 请求定位权限
     *
     */
    public void requestLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(IPActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(IPActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(IPActivity.this, "自Android 6.0开始需要打开位置权限才可以搜索到WIFI设备", Toast.LENGTH_SHORT);

                }
                //请求权限
                ActivityCompat.requestPermissions(IPActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       Log.i("IPActivity_Log","定位权限回调");
        wifiList = getWifiList();
    }

    /**
     * 获取WIFI列表
     * @return
     */
    public List<ScanResult> getWifiList() {
        List<ScanResult> scanWifiList = mwifiManager.getScanResults();
        List<ScanResult> wifiList = new ArrayList<>();
        if (scanWifiList != null && scanWifiList.size() > 0) {
            HashMap<String, Integer> signalStrength = new HashMap<String, Integer>();
            for (int i = 0; i < scanWifiList.size(); i++) {
                ScanResult scanResult = scanWifiList.get(i);
                if (!scanResult.SSID.isEmpty()) {
                    String key = scanResult.SSID + " " + scanResult.capabilities;
                    if (!signalStrength.containsKey(key)) {
                        signalStrength.put(key, i);
                        wifiList.add(scanResult);
                    }
                }
            }
        }
        return wifiList;
    }

    //打开Wifi开关，wifi连接状态检测
    class ListenThread extends Thread{
        private Context mContext;
        private final int time = 15;
        public ListenThread(Context mContext){
            this.mContext = mContext;
        }
        @Override
        public void run() {
            boolean isCon = true;
            try{
                int i = 0;
                while(isCon) {
                    Thread.sleep(2000);
                    Log.i("IPActivity_Log","wifi连接监听");
                    if(isWifi(mContext)){
                        isCon = false;
                        Log.i("IPActivity_Log","wifi连接成功");

                        Message msg = IPActivity.ipActivityHandler.obtainMessage();
                        String wifiStr = getWifiSetting(mContext);
                        if("DHCP".equals(wifiStr)){
                            msg.arg1 = R.id.wifiMode1;
                        }else{
                            msg.arg1 = R.id.wifiMode2;
                        }
                        IPActivity.ipActivityHandler.sendMessage(msg);
                    }else{
                        Log.i("IPActivity_Log","wifi连接失败，继续监听");
                    }
                    i++;
                    if(i == time){
                        isCon = false;
                        Log.i("IPActivity_Log","wifi连接监听停止，划拨连接状态为【未连接】");
                        Message msg = IPActivity.ipActivityHandler.obtainMessage();
                        msg.arg1 = R.id.wifiMode3;
                        IPActivity.ipActivityHandler.sendMessage(msg);
                    }
                }
            }catch (Exception e){

            }
        }
    }
}

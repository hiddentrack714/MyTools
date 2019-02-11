package com.track.mytools.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import com.track.mytools.activity.IPActivity;
import com.track.mytools.util.ToolsUtil;

/**
 * 检测WiFi是否成功连接
 *
 */
public class IPReceiver extends BroadcastReceiver {
    public static final int STATE1 = 1;//密码错误
    public static final int STATE2 = 2;//连接成功
    public static final int STATE3 = 3;//连接失败
    public static final int STATE4 = 4;//正在获取ip地址
    public static final int STATE5 = 5;//正在连接

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            //密码错误广播,是不是正在获得IP地址
            int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                //密码错误
                Log.i("IPReceiver","密码错误");
                ToolsUtil.showToast(IPActivity.ipActivity,"密码错误",2000);
            }
            SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(supplicantState);
            if (state == NetworkInfo.DetailedState.CONNECTING) {
                //正在连接
                Log.i("IPReceiver","正在连接");
            } else if (state == NetworkInfo.DetailedState.FAILED
                    || state == NetworkInfo.DetailedState.DISCONNECTING) {
                //连接失败
                Log.i("IPReceiver","连接失败");
            } else if (state == NetworkInfo.DetailedState.CONNECTED) {
                //连接成功
                Log.i("IPReceiver","连接成功");
            } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                //正在获取ip地址
                Log.i("IPReceiver","正在获取ip地址");
                String activity = ToolsUtil.getCurrentActivity().toString();
                if(activity.indexOf("IPActivity")>-1){
                    Log.i("IPReceiver","当前Activity:IPActivity");
                    Message msg = IPActivity.ipActivityStateHandler.obtainMessage();
                    IPActivity.ipActivityStateHandler.sendMessage(msg);
                    //ToolsUtil.showToast(IPActivity.ipActivity,"密码正确",2000);
                }
            } else if (state == NetworkInfo.DetailedState.IDLE) {
                //闲置的
                Log.i("IPReceiver","闲置的");
            }
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            // 监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLING://正在停止0
                    break;
                case WifiManager.WIFI_STATE_DISABLED://已停止1
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN://未知4
                    break;
                case WifiManager.WIFI_STATE_ENABLING://正在打开2
                    break;
                case WifiManager.WIFI_STATE_ENABLED://已开启3
                    break;
                default:
                    break;
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            // 监听wifi的连接状态即是否连上了一个有效无线路由
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                // 获取联网状态的NetWorkInfo对象
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                //获取的State对象则代表着连接成功与否等状态
                NetworkInfo.State state = networkInfo.getState();
                //判断网络是否已经连接
                boolean isConnected = state == NetworkInfo.State.CONNECTED;

                if (isConnected) {

                }
            }
        }
    }
}

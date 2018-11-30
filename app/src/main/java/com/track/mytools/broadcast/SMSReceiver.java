package com.track.mytools.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //接收Intent对象当中的数据
        Bundle bundle=intent.getExtras();
        //在Bundle对象中有一个属性为pdus，该属性为Object数组
        Object[] myOBJpdus=(Object[])bundle.get("pdus");
        //创建一个SmsMessage类型的数组
        SmsMessage[] messages=new SmsMessage[myOBJpdus.length];
        System.out.println(messages.length);
        for (int i = 0; i < myOBJpdus.length; i++) {
            //使用Object数组中的对象创建SmsMessage对象
            messages[i]=SmsMessage.createFromPdu((byte[])myOBJpdus[i]);
            //获取到信息对象的内容
            System.out.println(messages[i].getDisplayOriginatingAddress());
            System.out.println(messages[i].getDisplayMessageBody());
            Log.i("SMSRECEIVER1",messages[i].getDisplayOriginatingAddress());
            Log.i("SMSRECEIVER2",messages[i].getDisplayMessageBody());
        }
    }
}

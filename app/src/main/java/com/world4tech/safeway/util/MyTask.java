package com.world4tech.safeway.util;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

public class MyTask {


    public static void run(Context ctx, String tel_no, String text){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(tel_no, null, text, null, null);

    }

}

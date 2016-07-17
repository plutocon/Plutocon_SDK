package com.kongtech.plutocon.sdk.util;

import android.util.Log;

public class Plog {
    private static final String TAG = "PlutoconSdk";
    private static boolean enable = false;

    public static void enableLogging(boolean enable){
        Plog.enable = enable;
    }

    public static void i(String msg){
        if(enable) Log.i(TAG, msg);
    }

    public static void d(String msg){
        if(enable) Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if(enable) Log.e(TAG, msg);
    }
}

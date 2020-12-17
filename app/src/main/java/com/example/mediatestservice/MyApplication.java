package com.example.mediatestservice;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApplication extends Application {
    private static Context sContext;
    private static final String TAG = "MediaTest";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MyApplication");
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}

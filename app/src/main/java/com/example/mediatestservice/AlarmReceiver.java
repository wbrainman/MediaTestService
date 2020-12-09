package com.example.mediatestservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.example.mediatestservice.common.Common;

import androidx.annotation.RequiresApi;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaTest";
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AlarmReceiver onReceive: ");
//        mAlarmManager.setExactAndAllowWhileIdle(
//                AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() + Common.TIME_1MIN,
//                mPendingIntent
//                );
    }
}

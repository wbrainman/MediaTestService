package com.example.mediatestservice;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.security.AlgorithmConstraints;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.mediatestservice.common.Common;
import com.example.mediatestservice.util.MyLog;

import static com.example.mediatestservice.common.Common.ALARM_ACTION;
import static com.example.mediatestservice.common.Common.TIME_10MIN;
import static com.example.mediatestservice.common.Common.TIME_10S;

public class MediaTestService extends Service {

    private static final String TAG = "MediaTest";
    MediaRecorder mMediaRecorder;
    boolean isRecording = false;

    private String[] permissions = new String[]
            { Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO };
    private List<String> mPermissionList = new ArrayList<>();

    private File recordPath;
    private File recordFile;

    private HandlerThread mHandlerThread = new HandlerThread("MediaTest");
    private Handler mHandler;

    private MyReceiver myReceiver;

    private final IMediaTestService.Stub stub = new IMediaTestService.Stub() {
        @Override
        public void startRecord() throws RemoteException {
            MyLog.d(TAG, "startRecord: ");
            try {
                recordPath = getExternalFilesDir(null);
                File path = new File(recordPath.getPath() + File.separator + "audioRecords");
                recordPath = path;
                if (!path.exists()) {
                    if (!path.mkdirs()) {
                        MyLog.d(TAG, "startRecord: fail to create dir: " + path.getPath());
                        return;
                    }
                    MyLog.d(TAG, "startRecord: create dir: " + path.getPath());
                } else {
                    MyLog.d(TAG, "startRecord: dir already exist: " + path.getPath());
                }

                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date();
                    MyLog.d(TAG, "startRecord: time = " + simpleDateFormat.format(date));
                    recordFile = new File(recordPath, simpleDateFormat.format(date) + ".amr");

                    MyLog.d(TAG, "startRecord: " + recordFile.getAbsolutePath());
                } catch (Exception e) {
                    MyLog.d(TAG, "startRecord: fail to create file");
                }

                mMediaRecorder = new MediaRecorder();
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mMediaRecorder.setOutputFile(recordFile.getAbsolutePath());
                mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                    @Override
                    public void onError(MediaRecorder mr, int what, int extra) {
                        mMediaRecorder.stop();
                        mMediaRecorder.release();
                        mMediaRecorder = null;
                        isRecording = false;
                        MyLog.d(TAG, "startRecord: error what = " + what + " extra = " + extra);
                    }
                });

                mMediaRecorder.prepare();
                mMediaRecorder.start();
                isRecording = true;
                MyLog.d(TAG, "startRecord: end");

            } catch (Exception e) {
                e.printStackTrace();
            }

//            mHandler.sendEmptyMessageDelayed(Common.MSG_STOP_RECORD, Common.TIME_10S);
            mHandler.sendEmptyMessageDelayed(Common.MSG_STOP_RECORD, Common.TIME_3H);
        }

        @Override
        public void stopRecord() throws RemoteException {
            if (!isRecording) {
                return;
            }
            MyLog.d(TAG, "stopRecord: ");
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaRecorder = null;
            isRecording = false;
            MyLog.d(TAG, "stopRecord: end");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.d(TAG, "service onCreate: ");
        MyLog.d(TAG, "service onCreate: ");

        MyLog.setPath(getExternalFilesDir(null));

        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case Common.MSG_START_RECORD:
                        try {
//                            stub.startRecord();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case Common.MSG_STOP_RECORD:
                        try {
                            stub.stopRecord();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        //alarm
        //Intent intent = new Intent(this, AlarmReceiver.class);
//        intent.setAction(ALARM_ACTION);
        Intent intent = new Intent(ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }

//        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pi);
//        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TIME_10MIN, pi);

        //my receiver
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ALARM_ACTION);
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(myReceiver, intentFilter);


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLog.d(TAG, "onBind: ");
        return stub;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.d(TAG, "service onDestroy: ");
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyLog.d(TAG, "onReceive: my " + intent.getAction());
            if (ALARM_ACTION == intent.getAction()) {

                PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
                AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 20);
                calendar.set(Calendar.MINUTE, 30);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                } else {
                    am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
                }

                try {
                    stub.startRecord();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

}

package com.example.mediatestservice;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
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
import com.example.mediatestservice.util.FileUtil;
import com.example.mediatestservice.util.MyLog;

import static com.example.mediatestservice.common.Common.ALARM_ACTION;
import static com.example.mediatestservice.common.Common.TIME_10MIN;
import static com.example.mediatestservice.common.Common.TIME_10S;

public class MediaTestService extends Service implements SurfaceHolder.Callback{

    private static final String TAG = "MediaTest";

    private HandlerThread mHandlerThread = new HandlerThread("MediaTest");
    private Handler mHandler;

    private MyReceiver myReceiver;

    private static final String CHANNEL_ID = "com.example.mediatestservice.channel";
    private static final int NOTIFICATION_ID = 123;

    private final IMediaTestService.Stub stub = new IMediaTestService.Stub() {
        @Override
        public void startRecord() throws RemoteException {

        }

        @Override
        public void stopRecord() throws RemoteException {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.d(TAG, "service onCreate: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        MyLog.setPath(getExternalFilesDir(null));
        initHandlerThread();
        initAm(this);
        initReceiver();
        SurfaceMng.getInstance().init(this);

        MyLog.d(TAG, "service onCreate: end");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initHandlerThread() {
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case Common.MSG_START_RECORD:
                        Log.d(TAG, "handleMessage: receive start cmd");
                        start();
                        break;
                    case Common.MSG_STOP_RECORD:
                        Log.d(TAG, "handleMessage: receive stop cmd");
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initAm(Context context) {
        //alarm
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
    }

    private void initReceiver() {
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
        MediaMng.getInstance().stop();
        SurfaceMng.getInstance().onDestroy(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: width = " + width + ", height = " + height);
        mHandler.sendEmptyMessage(Common.MSG_START_RECORD);

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: ");
    }

    private void start() {
        MyLog.d(TAG, "startRecord: ");
        // prepare file & path
        File recordFile;
        recordFile = FileUtil.createMediaStoreFile(MyApplication.getContext());
        if (null == recordFile) {
            Log.e(TAG, "recordFile is null");
            return;
        }
        mute();
        MediaMng.getInstance().start(recordFile);
//            mHandler.sendEmptyMessageDelayed(Common.MSG_STOP_RECORD, Common.TIME_3H);
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyLog.d(TAG, "onReceive: my " + intent.getAction());
            if (ALARM_ACTION == intent.getAction()) {

//                PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
//                AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
//
//                Calendar calendar = Calendar.getInstance();
//                calendar.set(Calendar.HOUR_OF_DAY, 20);
//                calendar.set(Calendar.MINUTE, 30);
//                calendar.set(Calendar.SECOND, 0);
//                calendar.set(Calendar.MILLISECOND, 0);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
//                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
//                } else {
//                    am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
//                }

//                try {
//                    stub.startRecord();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

            }
        }
    }

    private void mute() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
//        audioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
//        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
//        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
//        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
    }

   @RequiresApi(api = Build.VERSION_CODES.O)
   private void createNotificationChannel() {
       Log.d(TAG, "createNotificationChannel: ");
        String channelName = "MediaTest";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription("描述");
       Log.d(TAG, "createNotificationChannel: 111");

       NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
//       builder.setSmallIcon(R.drawable.ic_launcher_background) //设置通知图标
       builder.setContentTitle("标题")
                .setContentText("内容")
                .setAutoCancel(true) //用户触摸时自动关闭
                .setOngoing(true); //设置处于运行状态

       Log.d(TAG, "createNotificationChannel: 222");
       NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
       notificationManager.createNotificationChannel(channel);
       Log.d(TAG, "createNotificationChannel: 333");

       try {
           startForeground(NOTIFICATION_ID, builder.build());
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
}

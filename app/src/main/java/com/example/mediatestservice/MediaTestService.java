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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.mediatestservice.common.Common;

import static com.example.mediatestservice.common.Common.ALARM_ACTION;
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

    private final IMediaTestService.Stub stub = new IMediaTestService.Stub() {
        @Override
        public void startRecord() throws RemoteException {
            Log.d(TAG, "startRecord: ");
            try {
                recordPath = getExternalFilesDir(null);
                File path = new File(recordPath.getPath() + File.separator + "audioRecords");
                recordPath = path;
                if (!path.exists()) {
                    if (!path.mkdirs()) {
                        Log.d(TAG, "startRecord: fail to create dir: " + path.getPath());
                        return;
                    }
                    Log.d(TAG, "startRecord: create dir: " + path.getPath());
                } else {
                    Log.d(TAG, "startRecord: dir already exist: " + path.getPath());
                }

                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date();
                    Log.d(TAG, "startRecord: time = " + simpleDateFormat.format(date));
                    recordFile = new File(recordPath, simpleDateFormat.format(date) + ".amr");

                    Log.d(TAG, "startRecord: " + recordFile.getAbsolutePath());
                } catch (Exception e) {
                    Log.d(TAG, "startRecord: fail to create file");
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
                        Log.d(TAG, "startRecord: error what = " + what + " extra = " + extra);
                    }
                });

                mMediaRecorder.prepare();
                mMediaRecorder.start();
                isRecording = true;
                Log.d(TAG, "startRecord: end");

            } catch (Exception e) {
                e.printStackTrace();
            }

            mHandler.sendEmptyMessageDelayed(Common.MSG_STOP_RECORD, Common.TIME_10S);
        }

        @Override
        public void stopRecord() throws RemoteException {
            if (!isRecording) {
                return;
            }
            Log.d(TAG, "stopRecord: ");
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaRecorder = null;
            isRecording = false;
            Log.d(TAG, "stopRecord: end");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service onCreate: ");

        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case Common.MSG_START_RECORD:
                        try {
                            stub.startRecord();
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
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 27);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return stub;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "service onDestroy: ");
    }

}

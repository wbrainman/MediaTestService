package com.example.mediatestservice;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        Log.d(TAG, "onCreate: ");
        // TODO: permissions?
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return stub;
    }

//    private void checkPermission() {
//        mPermissionList.clear();
//        for(String permission : permissions) {
//            Log.d(TAG, "initPermission: checking permission : " + permission);
//            if(ContextCompat.checkSelfPermission(
//                    MediaTestService.this, permission) != PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "initPermission: permission : " + permission + "added");
//                mPermissionList.add(permission);
//            }
//        }
//        if(mPermissionList.size() > 0) {
//            ActivityCompat.requestPermissions(MediaTestService.this, permissions, 1);
//        }
//        else {
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//
//        boolean denied = false;
//        switch (requestCode) {
//            case 1:
//                for(int i = 0; i < grantResults.length; i ++) {
//                    if(grantResults[i] == -1) {
//                        denied = true;
//                    }
//                }
//                if(denied) {
//                    // TODO
//                }
//                else {
//                }
//                break;
//            default:
//                break;
//        }
//    }

}

package com.example.mediatestservice;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.mediatestservice.util.MyLog;

import java.io.File;

public class MediaMng {
    private static final String TAG = "MediaTest";
    private static volatile MediaMng instance;
    private MediaRecorder mMediaRecorder;
    boolean isRecording = false;

    private MediaMng() {

    }

    public static MediaMng getInstance() {
        if (null == instance) {
            synchronized (MediaMng.class) {
                if (null == instance) {
                    instance = new MediaMng();
                }
            }
        }
        return instance;
    }

    public void  start(File recordFile) {
        mMediaRecorder = new MediaRecorder();

        MyLog.d(TAG, "startRecord: 000");
        Camera camera = CameraMng.getInstance().initFrontCamera();
        mMediaRecorder.setCamera(camera);

        MyLog.d(TAG, "startRecord: 111");

        try {
            // set video & audio resource
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // set output format
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // set audio encoder
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // set video encoder
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setOrientationHint(270);
            // set video frameRate
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoEncodingBitRate(3*1024*1014);
//            mMediaRecorder.setMaxDuration(30 * 1000);
            // set video size
            mMediaRecorder.setVideoSize(1280, 720);
            // mMediaRecorder.setVideoSize(640, 480);
            // set preview
            mMediaRecorder.setPreviewDisplay(SurfaceMng.getInstance().getSurfaceView().getHolder().getSurface());
            // set output file
            mMediaRecorder.setOutputFile(recordFile.getAbsolutePath());

            mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                    isRecording = false;
                    MyLog.d(TAG, "error what = " + what + " extra = " + extra);
                }
            });
            MyLog.d(TAG, "startRecord: 222");

            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isRecording = true;
            MyLog.d(TAG, "startRecord: end");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        Log.d(TAG, "stop:");
        if (!isRecording) {
            return;
        }
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        CameraMng.getInstance().stop();
        mMediaRecorder = null;
        isRecording = false;
        MyLog.d(TAG, "stopRecord: end");
    }

}

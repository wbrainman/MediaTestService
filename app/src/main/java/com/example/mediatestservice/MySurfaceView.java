package com.example.mediatestservice;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.mediatestservice.util.MyLog;

/**
 * not used, maybe use later.
 */
public class MySurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "MediaTest";
    private  SurfaceHolder mSurfaceHolder;

    public MySurfaceView(Context context) {
        MyLog.d(TAG, "MySurfaceView");
    }

    public void init() {
//        mSurfaceHolder = mSurfaceView.getHolder();
//        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        MyLog.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        MyLog.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        MyLog.d(TAG, "surfaceDestroyed");
    }
}

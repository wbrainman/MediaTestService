package com.example.mediatestservice;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.example.mediatestservice.util.MyLog;

import static android.content.Context.WINDOW_SERVICE;

public class SurfaceMng {

    private static final String TAG = "MediaTest";
    private static volatile SurfaceMng instance;

    private WindowManager mWindowManager;
    private View mRecordView;
    private SurfaceView mSurfaceView;

    private SurfaceMng() {

    }

    public static SurfaceMng getInstance() {
        if (null == instance) {
            synchronized (SurfaceMng.class) {
                if (null == instance) {
                    instance = new SurfaceMng();
                }
            }
        }
       return instance;
    }

    public void init(Context context) {
        MyLog.d(TAG, "initWM");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        }

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        //形成的窗口层级关系，Android 8.0 前后存在区别
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //实现在其他应用和窗口上方显示提醒窗口
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            //表示提供用户交互操作的非应用窗口
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        // what this ?
        layoutParams.format = PixelFormat.RGBX_8888;
        // 显示位置
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        // 是否可触摸，聚焦
        //layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 窗口宽高
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

//        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
//        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = 640;
        layoutParams.height = 480;
//        layoutParams.width = screenWidth;
//        layoutParams.height = screenHeight;
        MyLog.d(TAG, "initWM w = " + layoutParams.width + ", h = " + layoutParams.height);

        MyLog.d(TAG, "initWM screenWidth = " + screenWidth + ", screenHeight = " + screenHeight);

        mRecordView = LayoutInflater.from(context).inflate(R.layout.float_window, null);
        mSurfaceView = (SurfaceView) mRecordView.findViewById(R.id.surface);
        mSurfaceView.getHolder().addCallback((SurfaceHolder.Callback) context);

        mWindowManager.addView(mRecordView, layoutParams);
        MyLog.d(TAG, "initWM end");
    }

    public void onDestroy(Context context) {
        if (mWindowManager != null) {
            mWindowManager.removeView(mRecordView);
        }
    }

    public SurfaceView getSurfaceView() {
       return mSurfaceView;
    }
}

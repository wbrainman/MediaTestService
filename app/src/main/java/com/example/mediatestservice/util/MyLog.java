package com.example.mediatestservice.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.mediatestservice.MyApplication;

import java.io.File;

import static android.content.Context.*;

public class MyLog {
    private static final Boolean MYLOG_SWITCH = true;
    private static final Boolean MYLOG_WRITE_TO_FILE = true;
    private static File mLogPath;
    private static final String TAG = "MediaTest";

    public static void w(String tag, Object msg) {
        log(tag, msg.toString(), 'w');
    }

    public static void e(String tag, Object msg) { // 错误信息
        log(tag, msg.toString(), 'e');
    }

    public static void d(String tag, Object msg) {// 调试信息
        log(tag, msg.toString(), 'd');
    }

    /**
     * 根据tag, msg和等级，输出日志
     * @param tag
     * @param msg
     * @param level
     */
    private static void log(String tag, String msg, char level) {
        if (MYLOG_SWITCH) {//日志文件总开关
            if ('e' == level) {
                Log.e(tag, msg);
            } else if ('w' == level) {
                Log.w(tag, msg);
            } else if ('d' == level) {
                Log.d(tag, msg);
            } else if ('i' == level) {
                Log.i(tag, msg);
            } else {
                Log.v(tag, msg);
            }
            if (MYLOG_WRITE_TO_FILE)//日志写入文件开关
                writeLogtoFile(String.valueOf(level), tag, msg);
        }
    }

    private static void writeLogtoFile(String logType, String tag, String text) {

//        mLogPath = MyApplication.getExternalFilesDir(null);

    }

}

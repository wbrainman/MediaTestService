package com.example.mediatestservice.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.mediatestservice.MyApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.*;

public class MyLog {
    private static final String TAG = "MediaTest";
    private static final Boolean MYLOG_SWITCH = true;
    private static final Boolean MYLOG_WRITE_TO_FILE = true;
    private static File mLogPath = null;
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 日志的输出格式
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式

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
            if (MYLOG_WRITE_TO_FILE) {
                writeLogtoFile(String.valueOf(level), tag, msg);
            }
        }
    }

    private static void writeLogtoFile(String logType, String tag, String text) {
        if (mLogPath == null) {
            Log.e(TAG, "log path is null");
            return;
        }

        Date date = new Date();
        String writeLog = myLogSdf.format(date) + "   " + logType + "   " + tag + "   " + text;

        if (!mLogPath.exists()) {
            if (!mLogPath.mkdirs()) {
                Log.e(TAG, "writeLogtoFile: fail to create dir: " + mLogPath.getPath());
                return;
            }
//            Log.d(TAG, "writeLogtoFile: create dir: " + mLogPath.getPath());
        } else {
//            Log.d(TAG, "writeLogtoFile: dir already exist: " + mLogPath.getPath());
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Log.d(TAG, "writeLogtoFile: time = " + simpleDateFormat.format(date));
        File file = new File(mLogPath, simpleDateFormat.format(date) + ".log");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

       try {
           FileWriter fileWriter = new FileWriter(file, true);
           BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
           bufferedWriter.write(writeLog);
           bufferedWriter.newLine();
           bufferedWriter.close();
           fileWriter.close();
       } catch (Exception e) {
            e.printStackTrace();
       }

    }

    public static void setPath(File file) {
        mLogPath = file;
        File path = new File(mLogPath.getPath() + File.separator + "log");
        mLogPath = path;
        MyLog.d(TAG, "setPath: " + file);
    }

}


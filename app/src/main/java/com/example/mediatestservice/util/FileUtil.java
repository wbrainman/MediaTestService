package com.example.mediatestservice.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
    private static final String TAG = "MediaTest";
    private static File mRecordPath;

    public static File createMediaStoreFile(Context context) {
        File recordPath = context.getExternalFilesDir(null);
        File path = new File(recordPath.getPath() + File.separator + "records");
        recordPath = path;

        if (!recordPath.exists()) {
            if (!recordPath.mkdirs()) {
                Log.d(TAG, "createMediaStoreFile:fail to create dir: " + recordPath.getPath());
                return null;
            }
            Log.d(TAG, "createMediaStoreFile: dir: " + recordPath.getPath());
        } else {
            MyLog.d(TAG, "createMediaStoreFile: dir already exist: " + recordPath.getPath());
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        MyLog.d(TAG, "createMediaStoreFile: time = " + simpleDateFormat.format(date));
        File recordFile = new File(recordPath, simpleDateFormat.format(date) + ".mp4");
        MyLog.d(TAG, "createMediaStoreFile: " + recordFile.getAbsolutePath());

        return recordFile;
    }

}

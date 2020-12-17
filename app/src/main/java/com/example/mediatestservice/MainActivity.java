package com.example.mediatestservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.mediatestservice.util.MyLog;

import java.util.ArrayList;
import java.util.List;

//public class MainActivity extends AppCompatActivity {
public class MainActivity extends Activity {


private static final String TAG = "MediaTest";

    private String[] permissions = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECORD_AUDIO
    };
    private List<String> mPermissionList = new ArrayList<>();

    private IMediaTestService iMediaTestService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLog.d(TAG, "onServiceConnected: ");
            iMediaTestService = IMediaTestService.Stub.asInterface(service);

            try {
//                iMediaTestService.startRecord();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MyLog.d(TAG, "onServiceDisconnected: ");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyLog.setPath(getExternalFilesDir(null));

        checkPermission();
        Intent intent = new Intent(this, MediaTestService.class);
//        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
//        finish();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void checkPermission() {
        MyLog.d(TAG, "checkPermission: ");
        mPermissionList.clear();
        for(String permission : permissions) {
            MyLog.d(TAG, "initPermission: checking permission : " + permission);
            if(ContextCompat.checkSelfPermission(
                    MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                MyLog.d(TAG, "initPermission: permission : " + permission + "added");
                mPermissionList.add(permission);
            }
        }
        if(mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
        else {
        }
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        MyLog.d(TAG, "onRequestPermissionsResult: ");

        boolean denied = false;
        switch (requestCode) {
            case 1:
                for(int i = 0; i < grantResults.length; i ++) {
                    if(grantResults[i] == -1) {
                        denied = true;
                    }
                }
                if(denied) {
                    Toast.makeText(
                            MainActivity.this,
                            "permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                }
                break;
            default:
                break;
        }
    }
}
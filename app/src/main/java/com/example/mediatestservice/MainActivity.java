package com.example.mediatestservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MediaTest";

    private String[] permissions = new String[]
            { Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO };
    private List<String> mPermissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        Intent intent = new Intent(this, MediaTestService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    private void checkPermission() {
        Log.d(TAG, "checkPermission: ");
        mPermissionList.clear();
        for(String permission : permissions) {
            Log.d(TAG, "initPermission: checking permission : " + permission);
            if(ContextCompat.checkSelfPermission(
                    MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "initPermission: permission : " + permission + "added");
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

        Log.d(TAG, "onRequestPermissionsResult: ");

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
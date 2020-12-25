package com.example.mediatestservice;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

public class CameraMng {

    private static final String TAG = "MediaTest";
    private static volatile CameraMng instance;
    private Camera mCamera;

    private CameraMng() {

    }

    public static CameraMng getInstance() {
        if (null == instance) {
            synchronized (CameraMng.class) {
                if (null == instance) {
                    instance = new CameraMng();
                }
            }
        }
        return instance;
    }

    public Camera initFrontCamera() {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);
            getSupportedVideoSizes(mCamera);
            mCamera.unlock();
        }
        return mCamera;
    }

    public void stop() {
        if (mCamera != null) {
            mCamera.lock();
        }
    }

    private void getSupportedVideoSizes(Camera camera) {
        final List<Camera.Size> supportedVideoSizes;

        if (camera.getParameters().getSupportedVideoSizes() != null) {
            supportedVideoSizes = camera.getParameters().getSupportedVideoSizes();
        } else {
            // Video sizes may be null, which indicates that all the supported
            // preview sizes are supported for video recordingr
            supportedVideoSizes = camera.getParameters().getSupportedPreviewSizes();
        }
        for (Camera.Size str : supportedVideoSizes) {
            Log.d(TAG, "supported video sizes "+str.width + ":" + str.height + " ... " + ((float) str.width / str.height));
        }
    }
}

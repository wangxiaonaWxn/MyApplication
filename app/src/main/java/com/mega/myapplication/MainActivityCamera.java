package com.mega.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivityCamera extends AppCompatActivity implements Camera.PreviewCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Camera mCamera;
    private int mOpenCameraId;
    private View mRootView;
    private SurfaceHolder holder;
    private ImageView mImage;
    private byte[] yuvBuffer ;

    String permisson = Manifest.permission.CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_teste);
        mRootView = findViewById(R.id.container);
        SurfaceView surfaceView = findViewById(R.id.camera_temp_surface);
        holder = surfaceView.getHolder();
        mOpenCameraId = 0;
        mImage = findViewById(R.id.image);
        yuvBuffer = new byte[1280 * 960 * 3 / 2];
        surfaceView.post(new Runnable() {
            @Override
            public void run() {
                if (hasPermisson()) {
                    open();
                } else {
                    ActivityCompat.requestPermissions(MainActivityCamera.this, new String[]{permisson}, 1);
                }
            }
        });
    }


    private boolean hasPermisson() {
        return ActivityCompat.checkSelfPermission(getApplicationContext(), permisson) == PackageManager.PERMISSION_GRANTED;
    }
    public boolean open() {
        try {
            Log.d(TAG, "open camera: " + mOpenCameraId);
            mCamera = Camera.open(mOpenCameraId);
            if (mCamera == null) {
                Log.d(TAG, "open camera: failed");
                return false;
            }
        } catch (RuntimeException e) {
            mCamera = null;
            Log.d(TAG, "open camera exception :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        if (mCamera != null) {
            Camera.Size size = getCameraPictureSize(mCamera);
            Log.d(TAG, "take picture width==" + size.width);
            Log.d(TAG, "take picture height==" + size.height);
            Camera.Parameters params = mCamera.getParameters();
            params.setPictureSize(size.width, size.height);
            mCamera.setParameters(params);
            mCamera.setErrorCallback(new Camera.ErrorCallback() {
                @Override
                public void onError(int error, Camera camera) {
                    Log.e(TAG, "onError: error " + error);
                }
            });
            Log.d(TAG, "open camera ok");
            try {
                mCamera.setPreviewCallbackWithBuffer(this);
                mCamera.addCallbackBuffer(yuvBuffer);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                Log.d(TAG, "set holder for camera");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return false;
        }
        return true;
    }
    private Camera.Size getCameraPictureSize(Camera camera) {

        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();

        int width = mRootView.getMeasuredWidth();
        int height = mRootView.getMeasuredHeight();
        Log.d(TAG, "root view width==" + width);
        Log.d(TAG, "root view height==" + height);
        for (Camera.Size size : supportedSizes) {
            if (size.width >= width || size.height >= height) {
                return size;
            }
        }

        return supportedSizes.get(0);
    }

    private Bitmap bitmap;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mCamera.addCallbackBuffer(yuvBuffer);
        Camera.Parameters parameters = camera.getParameters();
        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(),
                1280, 960, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, 1280, 960), 100, out);
        byte[] bytes = out.toByteArray();
        Log.d("wxn", "image");
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        mImage.setImageBitmap(bitmap);
    }
}
package com.mega.myapplication.camera;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.util.Size;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Camera1Manager extends CameraImpl {
    protected static final String TAG = Camera1Manager.class.getSimpleName();
    /**
     * 相机实体
     */
    private Camera mCamera;
    /**
     * 预览的尺寸
     */
    private Camera.Size mCameraPreSize;
    /**
     * 实际的尺寸
     */
    private Camera.Size mCameraPicSize;
    /**
     * 实际的帧率
     */
    private int mFPS;

    public Camera1Manager(CameraStateCallback cameraCallback, TakePhotoCallback photoCallback) {
        super(cameraCallback, photoCallback);
        /**初始化一个默认的格式大小*/
        initCameraConfig();
    }

    public void initCameraConfig() {
        mCameraConfig = new CameraConfig();
        mCameraConfig.minPreviewWidth = 1920;
        mCameraConfig.minPictureWidth = 1920;
        mCameraConfig.rate = 1.778f;
    }

    public boolean isPreview() {
        return mIsPreview;
    }

    public boolean open(int cameraId, Handler handler, int h, int w) {
        releaseCamera();
        try {
            Log.d(TAG, "open camera: " + cameraId);
            mCamera = Camera.open(cameraId);
            if (mCamera == null) {
                Log.d(TAG, "open camera: failed");
                return false;
            }
        } catch (RuntimeException e) {
            mIsCameraOpened = false;
            mCamera = null;
            Log.d(TAG, "open camera exception :" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        if (mCamera != null) {
            cameraCallback.onCameraOpened();
            mCamera.setErrorCallback(new Camera.ErrorCallback() {
                @Override
                public void onError(int error, Camera camera) {
                    Log.e(TAG, "onError: error " + error);
                    cameraCallback.onCameraOpenFailed(error);
                }
            });
            mIsCameraOpened = true;
            /**选择当前设备允许的预览尺寸*/
            Camera.Parameters param = mCamera.getParameters();
            mCameraPreSize = getPropPreviewSize(param.getSupportedPreviewSizes(),
                    mCameraConfig.rate, mCameraConfig.minPreviewWidth);
            mCameraPicSize = getPropPictureSize(param.getSupportedPictureSizes(),
                    mCameraConfig.rate, mCameraConfig.minPictureWidth);
            param.setPictureSize(mCameraPicSize.width, mCameraPicSize.height);
            param.setPreviewSize(mCameraPreSize.width, mCameraPreSize.height);
            mCamera.setParameters(param);
            mFPS = param.getPreviewFrameRate();
            Log.d(TAG, "open camera: fps is " + mFPS);
        } else {
            mIsCameraOpened = false;
            return false;
        }
        return true;
    }

    @Override
    public Size getPictureSize() {
        return new Size(mCameraPicSize.width, mCameraPicSize.height);
    }

    public void setPreviewTexture(@Nullable SurfaceTexture texture) {
        if (mCamera != null) {
            try {
                Log.e(TAG, "----setPreviewTexture");
                mCamera.setPreviewTexture(texture);
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
                Log.e(TAG, "CameraController setPreviewTexture RuntimeException");
            }
        }
    }

    public void startPreview() {
        if (mCamera != null) {
            try {
                if (mIsPreview == false) {
                    mCamera.startPreview();
                    mIsPreview = true;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                Log.e(TAG, "CameraController startPreview RuntimeException");
            }
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            try {
                if (mIsPreview == true) {
                    mCamera.stopPreview();
                    mIsPreview = false;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                Log.e(TAG, "CameraController stopPreview RuntimeException");
            }
        }
    }

    @Nullable
    public Camera getCamera() {
        return mCamera;
    }

    private Camera.Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, mSizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width >= minWidth) && equalRate(s, th)) {
                return s;
            }
            i++;
        }
        if (i >= list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    private Camera.Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, mSizeComparator);

        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width >= minWidth) && equalRate(s, th)) {
                return s;
            }
            i++;
        }
        if (i >= list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    private boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }

    private Comparator<Camera.Size> mSizeComparator = new Comparator<Camera.Size>() {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.height == rhs.height) {
                return 0;
            } else if (lhs.height > rhs.height) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    public void releaseCamera() {
        Log.d(TAG, "releaseCamera: enter");
        if (mCamera != null) {
            Log.d(TAG, "close() start");
            stopPreview();
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
            mIsPreview = false;
            Log.d(TAG, "close() success");
        }
    }

    @Override
    public void takePhoto() {
        Log.d(TAG, "take photo");
        if (mCamera != null) {
            mCamera.setPreviewCallback(mPreviewCallback);
        }
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera1Manager.this.previewFrame(data, camera);
        }
    };

    protected void previewFrame(@Nullable byte[] data, @Nullable Camera camera) {
        Log.d(TAG, "take photo callback");
        if (data != null) {
            mCamera.setPreviewCallback(null);
        }
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(),
                width, height, null);
        Log.d(TAG, " preview format:" + parameters.getPreviewFormat());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        byte[] bytes = out.toByteArray();
        if (takePhotoCallback != null) {
            takePhotoCallback.takePhotoComplete(bytes);
        }
    }
}

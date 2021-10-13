package com.mega.myapplication.camera;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.Size;

public abstract class CameraImpl {
  public CameraStateCallback cameraCallback;
  public TakePhotoCallback takePhotoCallback;
  protected boolean mIsPreview = false;
  protected boolean mIsCameraOpened = false;
    /**
     * 相机的宽高及比例配置
     */
  protected CameraConfig mCameraConfig;

  protected CameraImpl(CameraStateCallback callback, TakePhotoCallback photoCallback) {
      this.cameraCallback = callback;
      this.takePhotoCallback = photoCallback;
  }

  public abstract boolean open(int cameraId, Handler handler, int height, int width);

  public abstract void startPreview();

  public abstract void stopPreview();

  public abstract void releaseCamera();

  public abstract void setPreviewTexture(SurfaceTexture texture);

  public abstract void takePhoto();

  public abstract boolean isPreview();

  public abstract Size getPictureSize();

  public void setCameraConfig(CameraConfig config) {
      this.mCameraConfig = config;
  }

  public interface CameraStateCallback {
      void onCameraOpened();

      void onCameraClosed();

      void onCameraOpenFailed(int error);
  }

  public interface TakePhotoCallback {
      void takePhotoComplete(byte[] photoData);
  }

  public boolean isCameraOpened() {
      return mIsCameraOpened;
  }
}

package com.example.android.camera2video.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Camera2ManagerBase extends CameraImpl {
    private static final String TAG = Camera2ManagerBase.class.getSimpleName();
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private ImageReader mImageReader;
    private SurfaceTexture mSurfaceTexture;
    private Surface mPreviewSurface;
    private Size mPreviewSize;
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "camera opened");
            mCameraDevice = camera;
            cameraCallback.onCameraOpened();
            mIsCameraOpened = true;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "camera disconnected");
            cameraCallback.onCameraClosed();
            mIsCameraOpened = false;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "camera openError");
            cameraCallback.onCameraOpenFailed(error);
            mIsCameraOpened = false;
        }
    };

    public Camera2ManagerBase(CameraStateCallback callback, Context context,
                              TakePhotoCallback photoCall) {
        super(callback, photoCall);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean open(int cameraId, Handler handler, int height, int width) {
        if (mCameraDevice != null) {
            releaseCamera();
        }
        try {
            Log.d(TAG, "height==" + height);
            Log.d(TAG, "width==" + width);
            CameraCharacteristics characteristics = null;
            try {
                characteristics = mCameraManager
                        .getCameraCharacteristics(String.valueOf(cameraId));
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            Size[] sizeArr = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);

            mPreviewSize = getOptimalSize(sizeArr, width, height);
            mCameraManager.openCamera(String.valueOf(cameraId), mStateCallback, handler);
            return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 选择sizeMap中大于并且最接近width和height的size
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth()
                            * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    private final CameraCaptureSession.StateCallback mSessionCallback
            = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (mCameraDevice == null) {
                return;
            }
            mCaptureSession = session;
            try {
                startRequest(null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            repeatPreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Failed to configure capture session.");
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            if (mCaptureSession != null && mCaptureSession.equals(session)) {
                Log.e(TAG, "camera closed , set session as null");
                mCaptureSession = null;
            }
        }

    };

    @Override
    public void startPreview() {
        Log.d(TAG, "start preview");
        if (mCameraDevice == null || mSurfaceTexture == null) {
            Log.d(TAG, "mCameraDevice=" + mCameraDevice);
            Log.d(TAG, "mSurfaceTexture=" + mSurfaceTexture);
            return;
        }
        if (mIsPreview) {
            Log.d(TAG, "start preview is preview now");
            return;
        }
        Log.d(TAG, "start preview");
        mIsPreview = true;
        mImageReader = ImageReader.newInstance(
                mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.i(TAG, "Image Available!==" + Thread.currentThread().getName());
                Image image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                if (takePhotoCallback != null) {
                    takePhotoCallback.takePhotoComplete(data);
                }
                image.close();
            }
        }, null);
        Log.d(TAG, "mPreviewSize==" + mPreviewSize.getWidth());
        Log.d(TAG, " mPreviewSize.getHeight()==" + mPreviewSize.getHeight());
        List<Surface> targets = new ArrayList<>();
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mPreviewSurface = new Surface(mSurfaceTexture);

        targets.add(mPreviewSurface);
        targets.add(mImageReader.getSurface());
        try {
            mCameraDevice.createCaptureSession(targets, mSessionCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopPreview() {
        Log.d(TAG, "stop preview");
        if (!mIsPreview || mCaptureSession == null) {
            Log.e(TAG, "preview has already stopped");
            return;
        }
        mIsPreview = false;
        try {
            mCaptureSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void releaseCamera() {
        Log.d(TAG, "releaseCamera");
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    @Override
    public void setPreviewTexture(SurfaceTexture texture) {
        Log.d(TAG, "setPreviewTexture");
        mSurfaceTexture = texture;
    }

    @Override
    public boolean isPreview() {
        return mIsPreview;
    }

    @Override
    public void takePhoto() {
        Log.d(TAG, "take photo");
        if (mCameraDevice == null) {
            return;
        }
        try {
            //首先我们创建请求拍照的CaptureRequest
            final CaptureRequest.Builder mCaptureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureBuilder.addTarget(mPreviewSurface);
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            //停止预览
            mCaptureSession.stopRepeating();
            //开始拍照，然后回调上面的接口重启预览，因为mCaptureBuilder设置ImageReader作为target，
            // 所以会自动回调ImageReader的onImageAvailable()方法保存图片
            CameraCaptureSession.CaptureCallback captureCallback
                    = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    repeatPreview();
                }
            };
            mCaptureSession.capture(mCaptureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startRequest(Handler handler) throws CameraAccessException {
        mPreviewRequestBuilder =
                mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewRequestBuilder.addTarget(mPreviewSurface);

        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
    }

    private void repeatPreview() {
        //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(),
                    null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}

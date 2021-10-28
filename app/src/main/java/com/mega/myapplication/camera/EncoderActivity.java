package com.mega.myapplication.camera;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.mega.myapplication.R;
import com.mega.myapplication.encoder.FormatVideoEncoder;
import com.mega.myapplication.encoder.Frame;
import com.mega.myapplication.encoder.GetVideoData;
import com.mega.myapplication.encoder.VideoEncoder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EncoderActivity extends AppCompatActivity implements GetVideoData, Camera.PreviewCallback {

    private SurfaceView previewView;
    private VideoEncoder mVideoEncoder;
    private Camera mCamera;
    private View mRootView;
    private int mOpenCameraId = 0;
    private File folderTest = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/test/c1");
    private static final String TAG = EncoderActivity.class.getSimpleName();
    private SurfaceHolder holder;
    private ImageView mImage;
    private byte[] yuvBuffer ;
    private MediaMuxer mediaMuxer;
    private int videoTrack = -1;
    private boolean recording = false;
    private boolean canRecord = false;
    private boolean onPreview = false;
    private MediaFormat videoFormat;
    private Button mStart, mStop;
    private String path = "/storage/1704-1E71";
    private String cameraPath = "/camera1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        yuvBuffer = new byte[1280 * 960 * 3 / 2];
        setContentView(R.layout.encoder_activity);
        mVideoEncoder = new VideoEncoder(this);
        mRootView = findViewById(R.id.root_view);
        previewView = findViewById(R.id.camera_one);
        holder = previewView.getHolder();
        mStart = findViewById(R.id.start);
        mStop = findViewById(R.id.stop);
        previewView.post(new Runnable() {
            @Override
            public void run() {
                open();
            }
        });
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String  currentDateAndTime = sdf.format(new Date());
                startRecord(
                        folder.getAbsolutePath() + "/" + currentDateAndTime +".mp4");
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording = false;
                if (mediaMuxer != null) {
                    if (canRecord) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        canRecord = false;
                    }
                    mediaMuxer = null;
                }
                videoTrack = -1;
            }
        });
        FormatVideoEncoder formatVideoEncoder = FormatVideoEncoder.YUV420Dynamical;
         mVideoEncoder.prepareVideoEncoder(1280, 960, 25, 2560000,
                 90, false, 1, formatVideoEncoder);
////        mVideoEncoder.start();
////        Log.d("wxn", "=" + folder.getPath());
////        String filePath = path + cameraPath;
////        Log.d("wxn", "=" + filePath);
////        File file = new File(filePath);
//        String path = folder.getPath();
//        String fileNa = "/storage/1704-1E71" + "/test.txt";
//        Log.d("wxn", "file name=" + fileNa);
//        File file = new File(fileNa);
//        Log.d("wxn", "create file");
//        writeFileByBytes(fileNa, fileNa.getBytes(), false);
//        if (file.exists()) {
//            Log.d("wxn", "file create success");
//        } else {
//            Log.d("wxn", "file create fail");
//        }
//        tryGetUsbPermission();
//        Log.d("wxn", "fo==" + folderTest.getPath());

    }

    UsbManager mUsbManager;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private void tryGetUsbPermission(){
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION), 0);

        Log.d("wxn", "tryGetUsbPermission");
        //here do emulation to ask all connected usb device for permission
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            //add some conditional check if necessary
            //if(isWeCaredUsbDevice(usbDevice)){
            if(mUsbManager.hasPermission(usbDevice)){
                Log.d("wxn", "has get usb permission");
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                //and also choose option: not ask again
            }else{
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                Log.d("wxn", "do not has permission and get it");
            }
            //}
        }
    }


//    private boolean redUDiskDevsList() {
//        //设备管理器
//        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        //获取U盘存储设备
//        UsbMassStorageDevice storageDevices = UsbMassStorageDevice.getMassStorageDevices(this);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
//        //一般手机只有1个OTG插口
//        for (UsbMassStorageDevice device : storageDevices) {
//            //读取设备是否有权限
//            if (usbManager.hasPermission(device.getUsbDevice())) {
//                readDevice(device);
//                mU_disk_ok = true;
//                Log.d(TAG, "获取到权限: "+mU_disk_ok);
//            } else {
//                //没有权限，进行申请
//                usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
//            }
//        }
//        if (storageDevices.length == 0) {
//            u_disk_message.setText("请插入可用的U盘");
//            mU_disk_ok = false;
//        }else {
//            u_disk_message.setText("");
//        }
//        return mU_disk_ok;
//    }

    /*
     * 获取所有hub存储的路径
     * ****************/
    public static List<String> getHubStoragePaths(Context context) {
        String[] paths = null;
        List<String> data = new ArrayList();    // include sd and usb devices
        StorageManager storageManager = (StorageManager) context.getSystemService(STORAGE_SERVICE);
        try {
            paths = (String[]) StorageManager.class.getMethod("getVolumePaths",
                    new Class[0]).invoke(storageManager, new Object[]{});
            Log.d(TAG, "paths contains " + paths.length);
            for (String path : paths) {
                Log.d(TAG, "path=" + path);
                String state = (String) StorageManager.class
                        .getMethod("getVolumeState", String.class).invoke(storageManager, path);
                Log.d(TAG, "state=" + state);
                if (state.equals(Environment.MEDIA_MOUNTED) && !path.contains("emulated")) {
                    data.add(path);
                    Log.d(TAG, "storage path: " + path);
                    // if mounted a new sd , then to clear database & filemanager
                }
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            Log.d(TAG, "getHubStoragePaths error");
        }
        return data;
    }

    private File folder = new File("/storage/1704-1E71/camera1");
    public void startRecord(final String path)  {
        try {
            mediaMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        recording = true;
    }

    @Override
    public void onSpsPps(ByteBuffer sps, ByteBuffer pps) {

    }

    @Override
    public void onSpsPpsVps(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {

    }

    @Override
    public void getVideoData(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        Log.d(TAG, "getVideoData=");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && recording) {
            if (info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME
                    && !canRecord
                    && videoFormat != null) {
                videoTrack = mediaMuxer.addTrack(videoFormat);
                mediaMuxer.start();
                canRecord = true;
            }
            if (canRecord) mediaMuxer.writeSampleData(videoTrack, h264Buffer, info);
        }
    }

    @Override
    public void onVideoFormat(MediaFormat mediaFormat) {
        videoFormat = mediaFormat;
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

    private int imageFormat = ImageFormat.NV21;
    private Bitmap bitmap;
    private int count = 0;
    String fileName;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        Frame frame = new Frame(data, 180, true, imageFormat);
//         fileName = folderTest.getAbsolutePath() + "/frame" + count++;
//         writeFileByBytes(fileName, data, false);
//         writeDataToFile(frame, fileName);
     //   mVideoEncoder.inputYUVData(frame);
        mCamera.addCallbackBuffer(yuvBuffer);
//        Log.d(TAG, "onPreviewFrame");
//        Camera.Parameters parameters = camera.getParameters();
//        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(),
//                1280, 960, null);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        yuv.compressToJpeg(new Rect(0, 0, 1280, 960), 100, out);
//        byte[] bytes = out.toByteArray();
//        Log.d("wxn", "image");
//        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        mImage.setImageBitmap(bitmap);
    }

    /**
     * 向文件写入byte[]
     *
     * @param fileName 文件名
     * @param bytes    字节内容
     * @param append   是否追加
     * @throws IOException
     */
    public  void writeFileByBytes(String fileName, byte[] bytes, boolean append)  {
        Log.d("wxn", "write file name=" + fileName);
        try(OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName, append))){
            try {
                out.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDataToFile(Frame frame, String fileName) {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {

            fout = new FileOutputStream(fileName);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(frame);

            System.out.println("Done");

        } catch (Exception ex) {

            ex.printStackTrace();

        } finally {

            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
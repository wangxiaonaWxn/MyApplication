package com.mega.myapplication.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.mega.myapplication.R;
import com.mega.myapplication.encoder.FormatVideoEncoder;
import com.mega.myapplication.encoder.Frame;
import com.mega.myapplication.encoder.GetVideoData;
import com.mega.myapplication.encoder.VideoEncoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PreviewActivity extends AppCompatActivity implements GetVideoData {

    private ImageView previewView, imageView2, imageView3, imageView4;
    private VideoEncoder mVideoEncoder, mVideoEncoder2, mVideoEncoder3, mVideoEncoder4;
    private Camera mCamera;
    private View mRootView;
    private int mOpenCameraId = 0;
    private static final String TAG = PreviewActivity.class.getSimpleName();
    private SurfaceHolder holder;
    private ImageView mImage;
    private byte[] yuvBuffer;
    private MediaMuxer mediaMuxer, mediaMuxer2, mediaMuxer3, mediaMuxer4;
    private int videoTrack = -1, videoTrack2 = -1, videoTrack3 = -1,videoTrack4 = -1;
    private boolean recording = false, recording2 = false, recording3 = false, recording4 = false;
    private boolean canRecord = false, canRecord2 = false, canRecord3 = false, canRecord4 = false;
    private boolean onPreview = false;
    private MediaFormat videoFormat, videoFormat2,videoFormat3,videoFormat4;
    private Button mStart, mStop;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         test();
        yuvBuffer = new byte[1280 * 960 * 3 / 2];
        folder = new File(getApplicationContext().getFilesDir().getPath());
        folder2 =  new File(getApplicationContext().getFilesDir().getPath());
        folder3 =  new File(getApplicationContext().getFilesDir().getPath());
        folder4 =  new File(getApplicationContext().getFilesDir().getPath());
        videoFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/videoFolder");
        Log.d("wxn", "path==" + folder.getPath());
        Log.d("wxn", "path==" + getApplicationContext().getFilesDir().getPath() + "/videoFolder");
//        Log.d("wxn", "=" + folder.getPath());
//        fileTest = "test.xml";
//        File file = new File(fileTest);
//        Log.d("wxn", "=" + fileTest);
//        if (file.exists()) {
//            Log.d("wxn", "create file ");
//        } else {
//            Log.d("wxn", "create file fail");
//        }
        setContentView(R.layout.preview_activity);
        mVideoEncoder = new VideoEncoder(this);
        mRootView = findViewById(R.id.root_view);
        previewView = findViewById(R.id.camera_one);
        imageView2 = findViewById(R.id.camera_two);
        imageView3 = findViewById(R.id.camera_three);
        imageView4 = findViewById(R.id.camera_four);
        mStart = findViewById(R.id.start);
        mStop = findViewById(R.id.stop);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentDateAndTime = sdf.format(new Date());
                String fileName = videoFolder.getAbsolutePath() + "/" + currentDateAndTime + "camera1.mp4";
                Log.d("video", "videopath=" + fileName);
                startRecord(fileName);
//                startRecord2(videoFolder.getAbsolutePath() + "/" + currentDateAndTime + "camera2.mp4");
//                startRecord3(videoFolder.getAbsolutePath() + "/" + currentDateAndTime + "camera3.mp4");
//                startRecord4(videoFolder.getAbsolutePath() + "/" + currentDateAndTime + "camera4.mp4");
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

//                recording2 = false;
//                if (mediaMuxer2 != null) {
//                    if (canRecord2) {
//                        mediaMuxer2.stop();
//                        mediaMuxer2.release();
//                        canRecord2 = false;
//                    }
//                    mediaMuxer2 = null;
//                }
//                videoTrack2 = -1;
//
//
//                recording3 = false;
//                if (mediaMuxer3 != null) {
//                    if (canRecord3) {
//                        mediaMuxer3.stop();
//                        mediaMuxer3.release();
//                        canRecord3 = false;
//                    }
//                    mediaMuxer3 = null;
//                }
//                videoTrack3 = -1;
//
//                recording4 = false;
//                if (mediaMuxer4 != null) {
//                    if (canRecord4) {
//                        mediaMuxer4.stop();
//                        mediaMuxer4.release();
//                        canRecord4 = false;
//                    }
//                    mediaMuxer4 = null;
//                }
//                videoTrack4 = -1;
            }
        });
        FormatVideoEncoder formatVideoEncoder = FormatVideoEncoder.YUV420Dynamical;
        mVideoEncoder.prepareVideoEncoder(1280, 960, 25, 2560000,
                180, false, 1, formatVideoEncoder);
        mVideoEncoder.start();

        mVideoEncoder2 = new VideoEncoder(new GetVideoData() {
            @Override
            public void onSpsPps(ByteBuffer sps, ByteBuffer pps) {

            }

            @Override
            public void onSpsPpsVps(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {

            }

            @Override
            public void getVideoData(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
                Log.d(TAG, "getVideoData=");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && recording2) {
                    if (info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME
                            && !canRecord2
                            && videoFormat2 != null) {
                        videoTrack2 = mediaMuxer2.addTrack(videoFormat2);
                        mediaMuxer2.start();
                        canRecord2 = true;
                    }
                    if (canRecord2) mediaMuxer2.writeSampleData(videoTrack2, h264Buffer, info);
                }
            }

            @Override
            public void onVideoFormat(MediaFormat mediaFormat) {
                 videoFormat2 = mediaFormat;
            }
        });
        mVideoEncoder3 = new VideoEncoder(new GetVideoData() {
            @Override
            public void onSpsPps(ByteBuffer sps, ByteBuffer pps) {

            }

            @Override
            public void onSpsPpsVps(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {

            }

            @Override
            public void getVideoData(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
                Log.d(TAG, "getVideoData=");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && recording3) {
                    if (info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME
                            && !canRecord3
                            && videoFormat3!= null) {
                        videoTrack3= mediaMuxer3.addTrack(videoFormat3);
                        mediaMuxer3.start();
                        canRecord3 = true;
                    }
                    if (canRecord3) mediaMuxer3.writeSampleData(videoTrack3, h264Buffer, info);
                }
            }

            @Override
            public void onVideoFormat(MediaFormat mediaFormat) {
                videoFormat3 = mediaFormat;
            }
        });

        mVideoEncoder4 = new VideoEncoder(new GetVideoData() {
            @Override
            public void onSpsPps(ByteBuffer sps, ByteBuffer pps) {

            }

            @Override
            public void onSpsPpsVps(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {

            }

            @Override
            public void getVideoData(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
                Log.d(TAG, "getVideoData=");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && recording4) {
                    if (info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME
                            && !canRecord4
                            && videoFormat4 != null) {
                        videoTrack4 = mediaMuxer4.addTrack(videoFormat4);
                        mediaMuxer4.start();
                        canRecord4 = true;
                    }
                    if (canRecord4) mediaMuxer4.writeSampleData(videoTrack4, h264Buffer, info);
                }
            }

            @Override
            public void onVideoFormat(MediaFormat mediaFormat) {
                videoFormat4 = mediaFormat;
            }
        });

        mVideoEncoder2.prepareVideoEncoder(1280, 960, 25, 2560000,
                180, false, 1, formatVideoEncoder);
        mVideoEncoder2.start();

        mVideoEncoder3.prepareVideoEncoder(1280, 960, 25, 2560000,
                180, false, 1, formatVideoEncoder);
        mVideoEncoder3.start();

        mVideoEncoder4.prepareVideoEncoder(1280, 960, 25, 2560000,
                180, false, 1, formatVideoEncoder);
        mVideoEncoder4.start();
        new FileReadThread().start();
//        new FileReadThreadTwo().start();
//        new FileReadThreadThree().start();
//        new FileReadThreadFour().start();
    }

//    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//            + "/test/c1");
//    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//            + "/test.xml");
//    private File folder2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//            + "/test/c2");
//    private File folder3 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//            + "/test/c3");
//    private File folder4 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//            + "/test/c4");
//    private File videoFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//            + "/video");

    private File folder ;
    private File folder2;
    private File folder3;
    private File folder4;
    private File videoFolder ;
    private File folderTest = new File("/data/user/10/com.mega.myapplication/files/test.txt");




    public void startRecord(final String path) {
        try {
            mediaMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            Log.d("start", "create success");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("start", "create fail");
        }
        recording = true;
    }

    public void startRecord2(final String path) {
        try {
            mediaMuxer2 = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        recording2 = true;
    }

    public void startRecord3(final String path) {
        try {
            mediaMuxer3 = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        recording3 = true;
    }

    public void startRecord4(final String path) {
        try {
            mediaMuxer4 = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        recording4 = true;
    }

    @Override
    public void onSpsPps(ByteBuffer sps, ByteBuffer pps) {

    }

    @Override
    public void onSpsPpsVps(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {

    }

    @Override
    public void getVideoData(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        Log.d(TAG, "getVideoData=video 1==" + info.flags);
        if (info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
            Log.d(TAG, "getVideoData=video 1======" + info.flags);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && recording) {
            if (info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME
                    && !canRecord
                    && videoFormat != null) {
                videoTrack = mediaMuxer.addTrack(videoFormat);
                mediaMuxer.start();
                canRecord = true;
            }
            if (canRecord) {
                Log.d(TAG, "getVideoData write read");
                mediaMuxer.writeSampleData(videoTrack, h264Buffer, info);
            } else {
                Log.d(TAG, "getVideoData not write");
            };
        }
    }

    @Override
    public void onVideoFormat(MediaFormat mediaFormat) {
        videoFormat = mediaFormat;
    }


    public Object readObjectFromFile(String fileName) {
        Object temp = null;
        File file = new File(fileName);
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            ObjectInputStream objIn = new ObjectInputStream(in);
            temp = objIn.readObject();
            objIn.close();
            System.out.println("read object success!");
        } catch (IOException e) {
            System.out.println("read object failed");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return temp;
    }

    Bitmap bitmap;

    public class FileReadThread extends Thread {
        @Override
        public void run() {
            Log.d("wxn", "=FileReadThread start=");
            File[] files = folder.listFiles();
            int len = files.length;
            Log.d("wxn", "=FileReadThread start=");
            if (len == 0) {
                return;
            }
            String fileName;
            Log.d("wxn", "=FileReadThread");
            while (len > 0) {
                len--;
                fileName = files[len].getPath();
                if(fileName.contains("test")
                || fileName.contains("video")){
                    continue;
                }
                Log.d("wxn", "file name=" + fileName);
//                Frame frame = (Frame) readObjectFromFile(fileName);
                byte[] bytesFrame = readFileByBytes(fileName);
                mVideoEncoder.inputYUVData(new Frame(bytesFrame, 180, true, ImageFormat.NV21));
                YuvImage yuv = new YuvImage(bytesFrame, ImageFormat.NV21,
                        1280, 960, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, 1280, 960), 100, out);
                byte[] bytes = out.toByteArray();
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                previewView.post(new Runnable() {
                    @Override
                    public void run() {
                        previewView.setImageBitmap(bitmap);
                    }
                });
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public byte[] readFileByBytes(String fileName)  {
        try {
            try (InputStream in = new BufferedInputStream(new FileInputStream(fileName));
                 ByteArrayOutputStream out = new ByteArrayOutputStream();) {
                byte[] tempbytes = new byte[in.available()];
                for (int i = 0; (i = in.read(tempbytes)) != -1;) {
                    out.write(tempbytes, 0, i);
                }
                return out.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    Bitmap bitmap2;

    public class FileReadThreadTwo extends Thread {
        @Override
        public void run() {
            File[] files = folder2.listFiles();
            int len = files.length;
            if (len == 0) {
                return;
            }
            String fileName;
            while (len > 0) {
                len--;
                fileName = files[len].getPath();
                Log.d("wxn", "file name=" + fileName);
//                Frame frame = (Frame) readObjectFromFile(fileName);
                byte[] bytesFrame = readFileByBytes(fileName);
                mVideoEncoder2.inputYUVData(new Frame(bytesFrame, 180, true, ImageFormat.NV21));
                YuvImage yuv = new YuvImage(bytesFrame, ImageFormat.NV21,
                        1280, 960, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, 1280, 960), 100, out);
                byte[] bytes = out.toByteArray();
                bitmap2 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView2.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView2.setImageBitmap(bitmap2);
                    }
                });
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    Bitmap bitmap3;
    public class FileReadThreadThree extends Thread {
        @Override
        public void run() {
            File[] files = folder3.listFiles();
            int len = files.length;
            if (len == 0) {
                return;
            }
            String fileName;
            while (len > 0) {
                len--;
                fileName = files[len].getPath();
                Log.d("wxn", "file name=" + fileName);
//                Frame frame = (Frame) readObjectFromFile(fileName);
                byte[] bytesFrame = readFileByBytes(fileName);
                mVideoEncoder3.inputYUVData(new Frame(bytesFrame, 180, true, ImageFormat.NV21));
                YuvImage yuv = new YuvImage(bytesFrame, ImageFormat.NV21,
                        1280, 960, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, 1280, 960), 100, out);
                byte[] bytes = out.toByteArray();
                bitmap3 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView3.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView3.setImageBitmap(bitmap3);
                    }
                });
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Bitmap bitmap4;
    public class FileReadThreadFour extends Thread {
        @Override
        public void run() {
            File[] files = folder4.listFiles();
            int len = files.length;
            if (len == 0) {
                return;
            }
            String fileName;
            while (len > 0) {
                len--;
                fileName = files[len].getPath();
                Log.d("wxn", "file name=" + fileName);
//                Frame frame = (Frame) readObjectFromFile(fileName);
                byte[] bytesFrame = readFileByBytes(fileName);
                mVideoEncoder4.inputYUVData(new Frame(bytesFrame, 180, true, ImageFormat.NV21));
                YuvImage yuv = new YuvImage(bytesFrame, ImageFormat.NV21,
                        1280, 960, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, 1280, 960), 100, out);
                byte[] bytes = out.toByteArray();
                bitmap4 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView4.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView4.setImageBitmap(bitmap4);
                    }
                });
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void test() {
        Log.d("wxn", "test");
//      File file[] =  ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
//      for(File files: file) {
//          Log.d("wxn", "file=" + files.getPath());
//      }
//
//      File fit = ContextCompat.getCodeCacheDir(getApplicationContext());
//        Log.d("wxn", "file=" + fit.getPath());
//        File fileDir = getApplicationContext().getFilesDir();
//        String fileName = fileDir.getPath() + "/test.text";
//        File file1 = new File(fileName);
//        try {
//            file1.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.d("wxn", "fileDir=" + fileDir.getPath());
//        writeFileByBytes( fileDir.getPath() + "/test.text", "iiiiiii".getBytes(), false);
//        readFileData( fileDir.getPath() + "/test.text");
    }

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


    //文件写入
    public void writeFileData(String filename, String content){
        try {
            FileOutputStream fos = this.openFileOutput(filename, MODE_PRIVATE);//获得FileOutputStream
            //将要写入的字符串转换为byte数组
            byte[]  bytes = content.getBytes();
            fos.write(bytes);//将byte数组写入文件
            fos.close();//关闭文件输出流

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //文件读取
    public String readFileData(String fileName){
        String result="";
        try{
//            FileInputStream fis = openFileInput(fileName);
//            //获取文件长度
//            int lenght = fis.available();
//            byte[] buffer = new byte[lenght];
//            fis.read(buffer);
            //将byte数组转换成指定格式的字符串
            result = new String(readFileByBytes(fileName), "UTF-8");
            Log.d("wxn", "result=" + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }
}


package com.mega.myapplication.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;

import com.mega.myapplication.R;

public class OpenFourCameraActivity extends AppCompatActivity {

    private TextureView camera0,camera1,camera2,camera3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.four_camera_activity);
        camera0 = findViewById(R.id.camera_one);
        camera1 = findViewById(R.id.camera_two);
        camera2 = findViewById(R.id.camera_three);
        camera3 = findViewById(R.id.camera_four);
        final Camera2Manager manager0 = new Camera2Manager(new CameraImpl.CameraStateCallback() {
            @Override
            public void onCameraOpened() {

            }

            @Override
            public void onCameraClosed() {

            }

            @Override
            public void onCameraOpenFailed(int error) {

            }
        }, getApplicationContext(), new CameraImpl.TakePhotoCallback() {
            @Override
            public void takePhotoComplete(byte[] photoData) {

            }
        });

        final Camera2Manager manager1 = new Camera2Manager(new CameraImpl.CameraStateCallback() {
            @Override
            public void onCameraOpened() {

            }

            @Override
            public void onCameraClosed() {

            }

            @Override
            public void onCameraOpenFailed(int error) {

            }
        }, getApplicationContext(), new CameraImpl.TakePhotoCallback() {
            @Override
            public void takePhotoComplete(byte[] photoData) {

            }
        });

       final Camera2Manager manager2 = new Camera2Manager(new CameraImpl.CameraStateCallback() {
            @Override
            public void onCameraOpened() {

            }

            @Override
            public void onCameraClosed() {

            }

            @Override
            public void onCameraOpenFailed(int error) {

            }
        }, getApplicationContext(), new CameraImpl.TakePhotoCallback() {
            @Override
            public void takePhotoComplete(byte[] photoData) {

            }
        });

        final Camera2Manager manager3 = new Camera2Manager(new CameraImpl.CameraStateCallback() {
            @Override
            public void onCameraOpened() {

            }

            @Override
            public void onCameraClosed() {

            }

            @Override
            public void onCameraOpenFailed(int error) {

            }
        }, getApplicationContext(), new CameraImpl.TakePhotoCallback() {
            @Override
            public void takePhotoComplete(byte[] photoData) {

            }
        });


        camera0.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
               manager0.setPreviewTexture(surface);
               manager0.open(0, null, 960, 1280);
               manager0.startPreview();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
        camera1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                manager1.setPreviewTexture(surface);
                manager1.open(1, null, 960, 1280);
                manager1.startPreview();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
        camera2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                manager2.setPreviewTexture(surface);
                manager2.open(2, null, 960, 1280);
                manager2.startPreview();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
        camera3.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                manager3.setPreviewTexture(surface);
                manager3.open(3, null, 960, 1280);
                manager3.startPreview();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
    }
}
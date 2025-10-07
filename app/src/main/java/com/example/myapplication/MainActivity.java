package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("myapplication");
    }

    private static final int REQ_CAMERA = 1001;

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private String cameraId;
    private android.media.ImageReader imageReader;
    private int previewWidth = 1280;
    private int previewHeight = 720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasCameraPermission()) {
            if (textureView.isAvailable()) {
                openCamera();
            } else {
                textureView.setSurfaceTextureListener(surfaceTextureListener);
            }
        } else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        super.onPause();
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (textureView.isAvailable()) openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    if (hasCameraPermission()) openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
            };

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            if (cameraId == null) {
                for (String id : manager.getCameraIdList()) {
                    CameraCharacteristics chars = manager.getCameraCharacteristics(id);
                    Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraId = id;
                        break;
                    }
                }
                if (cameraId == null && manager.getCameraIdList().length > 0) {
                    cameraId = manager.getCameraIdList()[0];
                }
            }
            if (!hasCameraPermission()) return;
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Camera access error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
            Toast.makeText(MainActivity.this, "Camera error: " + error, Toast.LENGTH_SHORT).show();
        }
    };

    private void startPreview() {
        if (cameraDevice == null || !textureView.isAvailable()) return;

        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        if (surfaceTexture == null) return;

        surfaceTexture.setDefaultBufferSize(previewWidth, previewHeight);
        Surface previewSurface = new Surface(surfaceTexture);

        if (imageReader == null) {
            imageReader = android.media.ImageReader.newInstance(
                    previewWidth,
                    previewHeight,
                    android.graphics.ImageFormat.YUV_420_888,
                    2
            );
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
        }

        try {
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(previewSurface);
            previewRequestBuilder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(
                    Arrays.asList(previewSurface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;
                            captureSession = session;
                            try {
                                previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                                captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                Toast.makeText(MainActivity.this, "Preview failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(MainActivity.this, "Preview config failed", Toast.LENGTH_SHORT).show();
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Start preview error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    public native String stringFromJNI();
    public native void processFrame(byte[] nv21, int width, int height);

    private final android.media.ImageReader.OnImageAvailableListener onImageAvailableListener =
            reader -> {
                android.media.Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    if (image == null) return;
                    if (image.getFormat() != android.graphics.ImageFormat.YUV_420_888) return;
                    byte[] nv21 = yuv420ToNv21(image);
                    if (nv21 != null) {
                        processFrame(nv21, image.getWidth(), image.getHeight());
                    }
                } catch (Throwable ignored) {
                } finally {
                    if (image != null) image.close();
                }
            };

    private static byte[] yuv420ToNv21(android.media.Image image) {
        android.media.Image.Plane[] planes = image.getPlanes();
        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width * height;
        int uvSize = width * height / 2;
        byte[] out = new byte[ySize + uvSize];

        // Y
        java.nio.ByteBuffer yBuf = planes[0].getBuffer();
        int yRowStride = planes[0].getRowStride();
        int pos = 0;
        for (int row = 0; row < height; row++) {
            int len = Math.min(width, yBuf.remaining());
            yBuf.get(out, pos, len);
            pos += len;
            if (row < height - 1) {
                yBuf.position(yBuf.position() + (yRowStride - width));
            }
        }

        // VU (NV21)
        java.nio.ByteBuffer uBuf = planes[1].getBuffer();
        java.nio.ByteBuffer vBuf = planes[2].getBuffer();
        int uRowStride = planes[1].getRowStride();
        int vRowStride = planes[2].getRowStride();
        int uPixelStride = planes[1].getPixelStride();
        int vPixelStride = planes[2].getPixelStride();

        // Interleave V and U bytes
        int uvHeight = height / 2;
        int uvWidth = width / 2;
        for (int row = 0; row < uvHeight; row++) {
            for (int col = 0; col < uvWidth; col++) {
                int vuIndex = pos;
                int vIndex = row * vRowStride + col * vPixelStride;
                int uIndex = row * uRowStride + col * uPixelStride;
                if (vIndex < vBuf.limit() && uIndex < uBuf.limit()) {
                    out[vuIndex] = vBuf.get(vIndex);
                    out[vuIndex + 1] = uBuf.get(uIndex);
                    pos += 2;
                }
            }
        }
        return out;
    }
}
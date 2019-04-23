package com.serenegiant.usbcameratest7.motiondetection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.serenegiant.widget.CameraViewInterface;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MotionDetector {
    class MotionDetectorThread extends Thread {
        private AtomicBoolean isRunning = new AtomicBoolean(true);

        public void stopDetection() {
            isRunning.set(false);
        }

        @Override
        public void run() {
            while (isRunning.get()) {
                long now = System.currentTimeMillis();
                if (now-lastCheck > checkInterval) {
                    lastCheck = now;

                    if (nextData.get() != null) {
                        int[] img = ImageProcessing.decodeYUV420SPtoLuma(nextData.get(), nextWidth.get(), nextHeight.get());

                        // check if it is too dark
                        int lumaSum = 0;
                        for (int i : img) {
                            lumaSum += i;
                        }
                        if (lumaSum < minLuma) {
                            if (motionDetectorCallback != null) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        motionDetectorCallback.onTooDark();
                                    }
                                });
                            }
                        } else if (detector.detect(img, nextWidth.get(), nextHeight.get())) {
                            // check
                            if (motionDetectorCallback != null) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        motionDetectorCallback.onMotionDetected();
                                    }
                                });
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final AggregateLumaMotionDetection detector;
    private long checkInterval = 500;
    private long lastCheck = 0;
    private MotionDetectorCallback motionDetectorCallback;
    private Handler mHandler = new Handler();

    private AtomicReference<byte[]> nextData = new AtomicReference<>();
    private AtomicInteger nextWidth = new AtomicInteger();
    private AtomicInteger nextHeight = new AtomicInteger();
    private int minLuma = 1000;
    private MotionDetectorThread worker;


    private boolean inPreview;
//    private SurfaceHolder previewHolder;
    private Context mContext;
    private CameraViewInterface mSurface;

    public MotionDetector(Context context, CameraViewInterface previewSurface) {
        detector = new AggregateLumaMotionDetection();
        mContext = context;
        mSurface = previewSurface;

        onResume();
    }

    public void setMotionDetectorCallback(MotionDetectorCallback motionDetectorCallback) {
        this.motionDetectorCallback = motionDetectorCallback;
    }

    public void consume(byte[] data, int width, int height) {
        nextData.set(data);
        nextWidth.set(width);
        nextHeight.set(height);
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public void setMinLuma(int minLuma) {
        this.minLuma = minLuma;
    }

    public void setLeniency(int l) {
        detector.setLeniency(l);
    }

    public void onResume() {
        if (checkCameraHardware()) {

            worker = new MotionDetectorThread();
            worker.start();

        }

        // configure preview
        mSurface.setCallback(new CameraViewInterface.Callback() {
            @Override
            public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
                Log.d("saeetg", "awfsfvvgsdge");
            }

            @Override
            public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

                Log.d("saeetg", "awf");



            }

            @Override
            public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
                Log.d("saeetg", "awfsdeg");

            }
        });


    }

    public boolean checkCameraHardware() {
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

//    private Camera getCameraInstance(){
//        Camera c = null;
//
//        try {
//            if (Camera.getNumberOfCameras() >= 2) {
//                //if you want to open front facing camera use this line
//                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//            } else {
//                c = Camera.open();
//            }
//        }
//        catch (Exception e){
//            // Camera is not available (in use or does not exist)
//            //txtStatus.setText("Kamera nicht zur Benutzung freigegeben");
//        }
//        return c; // returns null if camera is unavailable
//    }
//
//    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
//
//        /**
//         * {@inheritDoc}
//         */
//        @Override
//        public void onPreviewFrame(byte[] data, Camera cam) {
//            if (data == null) return;
//            Camera.Size size = cam.getParameters().getPreviewSize();
//            if (size == null) return;
//
//            consume(data, size.width, size.height);
//        }
//    };



    private static Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) result = size;
                }
            }
        }

        return result;
    }

}

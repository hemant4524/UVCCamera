/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.usbcameratest7;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usbcameratest7.constants.FileConstant;
import com.serenegiant.usbcameratest7.motiondetection.MotionDetector;
import com.serenegiant.usbcameratest7.motiondetection.MotionDetectorCallback;
import com.serenegiant.usbcameratest7.server.RestClient;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Show side by side view from two camera.
 * You cane record video images from both camera, but secondarily started recording can not record
 * audio because of limitation of Android AudioRecord(only one instance of AudioRecord is available
 * on the device) now.
 */
public final class MultiCameraActivity extends BaseActivity implements CameraDialog.CameraDialogParent {
    private static final boolean DEBUG = true;    // FIXME set false when production
    private static final String TAG = "MultiCameraActivity";

    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f};

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;

    private UVCCameraHandler mHandlerUL;
    private CameraViewInterface mUVCCameraViewUL;
    private ImageButton mCaptureButtonUL;
    private Surface mUpperLeftPreviewSurface;

    private UVCCameraHandler mHandlerUR;
    private CameraViewInterface mUVCCameraViewUR;
    private ImageButton mCaptureButtonUR;
    private Surface mUpperRightPreviewSurface;




    private UVCCameraHandler mHandlerLL;
    private CameraViewInterface mUVCCameraViewLL;
    private ImageButton mCaptureButtonLL;
    private Surface mLowerLeftPreviewSurface;



    private UVCCameraHandler mHandlerLR;
    private CameraViewInterface mUVCCameraViewLR;
    private ImageButton mCaptureButtonLR;
    private Surface mLowerRightPreviewSurface;


    private UsbDevice deviceUL;
    private UsbDevice deviceUR;
    private UsbDevice deviceLL;
    private UsbDevice deviceLR;
    private MotionDetector motionDetector;
    private ImageButton mSendPhoto;
    private static final boolean USE_SURFACE_ENCODER = false;


    public static final int FRAME_FORMAT_YUYV = 0;
    public static final int FRAME_FORMAT_MJPEG = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSendPhoto = (ImageButton) findViewById(R.id.sendPhoto);
        mSendPhoto.setOnClickListener(mOnClickListener);

//
//        findViewById(R.id.RelativeLayout1).setOnClickListener(mOnClickListener);
//  //      mUVCCameraViewUL = (CameraViewInterface) findViewById(R.id.camera_view_UL);
//        final View view = findViewById(R.id.camera_view_UL);
//        mUVCCameraViewUL = (CameraViewInterface)view;
//		mUVCCameraViewUL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float)UVCCamera.DEFAULT_PREVIEW_HEIGHT);
//        ((UVCCameraTextureView) mUVCCameraViewUL).setOnClickListener(mOnClickListener);
//        mCaptureButtonUL = (ImageButton) findViewById(R.id.capture_button_UL);
//        mCaptureButtonUL.setOnClickListener(mOnClickListener);
//        mCaptureButtonUL.setVisibility(View.INVISIBLE);
//
//        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
//        mHandlerUL = UVCCameraHandler.createHandler(this, mUVCCameraViewUL,
//                USE_SURFACE_ENCODER ? 0 : 1, 640, 480, 1);
        //mHandlerUL = UVCCameraHandler.createHandler(this, mUVCCameraViewUL, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[0]);



        // Camera 1
        findViewById(R.id.RelativeLayout1).setOnClickListener(mOnClickListener);
        mUVCCameraViewUL = (CameraViewInterface) findViewById(R.id.camera_view_UL);
//		mUVCCameraViewUL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float)UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewUL).setOnClickListener(mOnClickListener);
        mCaptureButtonUL = (ImageButton) findViewById(R.id.capture_button_UL);
        mCaptureButtonUL.setOnClickListener(mOnClickListener);
        mCaptureButtonUL.setVisibility(View.INVISIBLE);
        mHandlerUL = UVCCameraHandler.createHandler(this, mUVCCameraViewUL,
                USE_SURFACE_ENCODER ? 0 : 1, 640, 480, FRAME_FORMAT_MJPEG);
        //mHandlerUL = UVCCameraHandler.createHandler(this, mUVCCameraViewUL, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[0]);

        // Camera 2
        mUVCCameraViewUR = (CameraViewInterface) findViewById(R.id.camera_view_UR);
//		mUVCCameraViewUR.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float)UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewUR).setOnClickListener(mOnClickListener);
        mCaptureButtonUR = (ImageButton) findViewById(R.id.capture_button_UR);
        mCaptureButtonUR.setOnClickListener(mOnClickListener);
        mCaptureButtonUR.setVisibility(View.INVISIBLE);
       // mHandlerUR = UVCCameraHandler.createHandler(this, mUVCCameraViewUR, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1]);
        mHandlerUR = UVCCameraHandler.createHandler(this, mUVCCameraViewUR,
                USE_SURFACE_ENCODER ? 0 : 1, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, FRAME_FORMAT_MJPEG);


        // Camera 3

        mUVCCameraViewLL = (CameraViewInterface) findViewById(R.id.camera_view_LL);
//		mUVCCameraViewUL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float)UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewLL).setOnClickListener(mOnClickListener);
        mCaptureButtonLL = (ImageButton) findViewById(R.id.capture_button_LL);
        mCaptureButtonLL.setOnClickListener(mOnClickListener);
        mCaptureButtonLL.setVisibility(View.INVISIBLE);
        mHandlerLL = UVCCameraHandler.createHandler(this, mUVCCameraViewLL, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[0]);
//        mHandlerLL = UVCCameraHandler.createHandler(this, mUVCCameraViewLL,
//                USE_SURFACE_ENCODER ? 0 : 1, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, FRAME_FORMAT_MJPEG);


        // Camera 4

        mUVCCameraViewLR = (CameraViewInterface) findViewById(R.id.camera_view_LR);
//		mUVCCameraViewUL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float)UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewLR).setOnClickListener(mOnClickListener);
        mCaptureButtonLR = (ImageButton) findViewById(R.id.capture_button_LR);
        mCaptureButtonLR.setOnClickListener(mOnClickListener);
        mCaptureButtonLR.setVisibility(View.INVISIBLE);
        mHandlerLR = UVCCameraHandler.createHandler(this, mUVCCameraViewLR, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[0]);

//        mHandlerLR = UVCCameraHandler.createHandler(this, mUVCCameraViewLR,
//                USE_SURFACE_ENCODER ? 0 : 1, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, FRAME_FORMAT_MJPEG);




        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
//
//        mUSBMonitor = getUSBMonitor();
//
//        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, com.serenegiant.uvccamera.R.xml.device_filter);
//        List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(filter.get(0));
//        mUSBMonitor.requestPermission(deviceList.get(0));


//        motionDetector = new MotionDetector(this,  mUVCCameraViewUL);
//        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
//            @Override
//            public void onMotionDetected() {
//              Log.d("Motion","Motion detected");
//            }
//
//            @Override
//            public void onTooDark() {
//                Log.d("Motion","Too dark here");
//            }
//        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        if (mUVCCameraViewUR != null)
            mUVCCameraViewUR.onResume();
        if (mUVCCameraViewUL != null)
            mUVCCameraViewUL.onResume();
    }

    @Override
    protected void onStop() {
        mHandlerUR.close();
        if (mUVCCameraViewUR != null)
            mUVCCameraViewUR.onPause();
        mHandlerUL.close();
        mCaptureButtonUR.setVisibility(View.INVISIBLE);
        if (mUVCCameraViewUL != null)
            mUVCCameraViewUL.onPause();
        mCaptureButtonUL.setVisibility(View.INVISIBLE);
        mUSBMonitor.unregister();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mHandlerUR != null) {
            mHandlerUR = null;
        }
        if (mHandlerUL != null) {
            mHandlerUL = null;
        }


        if (mHandlerLR != null) {
            mHandlerLR = null;
        }
        if (mHandlerLL != null) {
            mHandlerLL = null;
        }

        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraViewUR = null;
        mCaptureButtonUR = null;
        mUVCCameraViewUL = null;
        mCaptureButtonUL = null;

        mUVCCameraViewLR = null;
        mCaptureButtonLR = null;
        mUVCCameraViewLL = null;
        mCaptureButtonLL = null;
        super.onDestroy();
    }

    private boolean clickLeft = false;

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.camera_view_UL:
//                    clickLeft = true;
                    if (mHandlerUL != null) {
                        if (!mHandlerUL.isOpened()) {
                            CameraDialog.showDialog(MultiCameraActivity.this);
                        } else {
                            mHandlerUL.close();
                            setCameraButton();
                        }
                    }


                    break;
                case R.id.capture_button_UL:



//                    if (mHandlerUL != null) {
//                        if (mHandlerUL.isOpened()) {
//                            if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
//                                if (!mHandlerUL.isRecording()) {
//                                    mCaptureButtonUL.setColorFilter(0xffff0000);    // turn red
//
//                                    savePhoto(mHandlerUL,"1");
//
//                                    mHandlerUL.startRecording();
//                                } else {
//                                    mCaptureButtonUL.setColorFilter(0);    // return to default color
//                                    mHandlerUL.stopRecording();
//                                }
//                            }
//                        }
//                    }
                    break;
                case R.id.camera_view_UR:
                    clickLeft = false;
                    if (mHandlerUR != null) {
                        if (!mHandlerUR.isOpened()) {
                            CameraDialog.showDialog(MultiCameraActivity.this);
                        } else {
                            mHandlerUR.close();
                            setCameraButton();
                        }
                    }
                    break;
                case R.id.capture_button_UR:
                    if (mHandlerUR != null) {
                        if (mHandlerUR.isOpened()) {
                            if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                                if (!mHandlerUR.isRecording()) {
                                    mCaptureButtonUR.setColorFilter(0xffff0000);    // turn red
                                    mHandlerUR.startRecording();
                                } else {
                                    mCaptureButtonUR.setColorFilter(0);    // return to default color
                                    mHandlerUR.stopRecording();
                                }
                            }
                        }
                    }
                    break;

                //  Lower camera


                //  Lower left

                case R.id.camera_view_LL:
                    clickLeft = false;
                    if (mHandlerLL != null) {
                        if (!mHandlerLL.isOpened()) {
                            CameraDialog.showDialog(MultiCameraActivity.this);
                        } else {
                            mHandlerLL.close();
                            setCameraButton();
                        }
                    }
                    break;
                case R.id.capture_button_LL:
                    if (mHandlerLL != null) {
                        if (mHandlerLL.isOpened()) {
                            if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                                if (!mHandlerLL.isRecording()) {
                                    mCaptureButtonLL.setColorFilter(0xffff0000);    // turn red
                                    mHandlerLL.startRecording();
                                } else {
                                    mCaptureButtonLL.setColorFilter(0);    // return to default color
                                    mHandlerLL.stopRecording();
                                }
                            }
                        }
                    }
                    break;

                //  Lower right
                case R.id.camera_view_LR:
                    clickLeft = false;
                    if (mHandlerLR != null) {
                        if (!mHandlerLR.isOpened()) {
                            CameraDialog.showDialog(MultiCameraActivity.this);
                        } else {
                            mHandlerLR.close();
                            setCameraButton();
                        }
                    }
                    break;
                case R.id.capture_button_LR:
                    if (mHandlerLR != null) {
                        if (mHandlerLR.isOpened()) {
                            if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                                if (!mHandlerLR.isRecording()) {
                                    mCaptureButtonLR.setColorFilter(0xffff0000);    // turn red
                                    mHandlerLR.startRecording();
                                } else {
                                    mCaptureButtonLR.setColorFilter(0);    // return to default color
                                    mHandlerLR.stopRecording();
                                }
                            }

                            savePhoto(mHandlerLR, "4");
                        }
                    }
                    break;


                case R.id.sendPhoto:


                    if (checkPermissionWriteExternalStorage()) {
//                        // Camera 1 - Upper left
                        if(mHandlerUL.isOpened()){
                            savePhoto(mHandlerUL, "1");
                        }

                        // Camera 2 Upper right
                        if(mHandlerUR.isOpened()){
                            savePhoto(mHandlerUR,"2");
                        }

                        // Camera 3 - Down left
                        if(mHandlerLL.isOpened()){
                            savePhoto(mHandlerLL, "3");
                        }

                        // Camera 4 Down right
                        if(mHandlerLR.isOpened()){
                            savePhoto(mHandlerLR,"4");
                        }

                        Toast.makeText(MultiCameraActivity.this, "Captures photos.", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                sendPhotos();
                            }
                        },5000);
                    }



                    break;
            }


        }


    };


    private void savePhoto(UVCCameraHandler cameraHandler, String cameraId) {

        // Delete all files

        File root = Environment.getExternalStorageDirectory();

        String dirPath = root.getPath() + File.separator + FileConstant.APP_ROOT_FOLDER_NAME;

        File folder = new File(dirPath);




        if (!folder.exists()) {
            folder.mkdirs();
        }

        String picPath = dirPath + File.separator + cameraId
                + FileConstant.SUFFIX_JPEG;

        cameraHandler.captureStill(picPath);

    }

    /**
     * Send photo to server
     *
     */
    private void sendPhotos(){


        File root = Environment.getExternalStorageDirectory();

        String dirPath = root.getPath() + File.separator + FileConstant.APP_ROOT_FOLDER_NAME;

        File folder = new File(dirPath);

        // Send file to server
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        MultipartBody.Part[] parts = new MultipartBody.Part[folder.listFiles().length];

        int i=0;

        if (folder.exists()) {

            for (File file : folder.listFiles()) {
                Log.d("File", "Upload file: "+file.getAbsolutePath());
                RequestBody surveyBody = RequestBody.create(MediaType.parse("image/*"), file);
                parts[i] = MultipartBody.Part.createFormData("files[]", file.getName(), surveyBody);
                i++;
            }
        }



        Call<JsonObject> call = RestClient.getInstance().getApiService().uploadMultiFile1(parts);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("Success response:", ": "+response);

                Toast.makeText(MultiCameraActivity.this, "Image upload successfully! ", Toast.LENGTH_LONG).show();


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                Log.d("Error in  webservice:", " " + t.getMessage());
                Toast.makeText(MultiCameraActivity.this, "Error in  webservice:"+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "UsbDevice onAttach:" + device);
            //Toast.makeText(MultiCameraActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "UsbDevice onConnect:" + device);


            if (!mHandlerUL.isOpened()) {
                mHandlerUL.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewUL.getSurfaceTexture();
                mHandlerUL.startPreview(new Surface(st));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // mCaptureButtonUL.setVisibility(View.VISIBLE);
                    }
                });



            } else if (!mHandlerUR.isOpened()) {
                mHandlerUR.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewUR.getSurfaceTexture();
                mHandlerUR.startPreview(new Surface(st));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      //  mCaptureButtonUR.setVisibility(View.VISIBLE);
                    }
                });


                motionDetector = new MotionDetector(MultiCameraActivity.this,  mUVCCameraViewUR);
                motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
                    @Override
                    public void onMotionDetected() {
                        Log.d("Motion","Motion detected");
                    }

                    @Override
                    public void onTooDark() {
                        Log.d("Motion","Too dark here");
                    }
                });
            } else if (!mHandlerLL.isOpened()) {
                mHandlerLL.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewLL.getSurfaceTexture();
                mHandlerLL.startPreview(new Surface(st));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      //  mCaptureButtonLL.setVisibility(View.VISIBLE);
                    }
                });

                motionDetector = new MotionDetector(MultiCameraActivity.this,  mUVCCameraViewLL);
                motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
                    @Override
                    public void onMotionDetected() {
                        Log.d("Motion","Motion detected");
                    }

                    @Override
                    public void onTooDark() {
                        Log.d("Motion","Too dark here");
                    }
                });

            } else if (!mHandlerLR.isOpened()) {
                mHandlerLR.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewLR.getSurfaceTexture();
                mHandlerLR.startPreview(new Surface(st));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // mCaptureButtonLR.setVisibility(View.VISIBLE);
                    }
                });

                motionDetector = new MotionDetector(MultiCameraActivity.this,  mUVCCameraViewLR);
                motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
                    @Override
                    public void onMotionDetected() {
                        Log.d("Motion","Motion detected");
                    }

                    @Override
                    public void onTooDark() {
                        Log.d("Motion","Too dark here");
                    }
                });
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:" + device);

            if (deviceUL != null && device.getDeviceId() == deviceUL.getDeviceId()) {
                if (mHandlerUL != null) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mHandlerUL.close();
                            if (mUpperLeftPreviewSurface != null) {
                                mUpperLeftPreviewSurface.release();
                                mUpperLeftPreviewSurface = null;
                                deviceUL = null;
                            }
                            setCameraButton();
                        }
                    }, 0);
                }
            } else if (deviceUR != null && device.getDeviceId() == deviceUR.getDeviceId()) {
                if (mHandlerUR != null) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mHandlerUR.close();
                            if (mUpperRightPreviewSurface != null) {
                                mUpperRightPreviewSurface.release();
                                mUpperRightPreviewSurface = null;
                                deviceUR = null;
                            }
                            setCameraButton();
                        }
                    }, 0);
                }
            } else if (deviceLL != null && device.getDeviceId() == deviceLL.getDeviceId()) {
                if (mHandlerLL != null) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mHandlerLL.close();
                            if (mLowerRightPreviewSurface != null) {
                                mLowerLeftPreviewSurface.release();
                                mLowerLeftPreviewSurface = null;
                                deviceLL = null;
                            }
                            setCameraButton();
                        }
                    }, 0);
                }
            } else if (deviceLR != null && device.getDeviceId() == deviceLR.getDeviceId()) {
                if (mHandlerLR != null) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mHandlerUR.close();
                            if (mLowerRightPreviewSurface != null) {
                                mLowerRightPreviewSurface.release();
                                mLowerRightPreviewSurface = null;
                                deviceLR = null;
                            }
                            setCameraButton();
                        }
                    }, 0);
                }
            }


//			if ((mHandlerUL != null) && !mHandlerUL.isEqual(device)) {
//				queueEvent(new Runnable() {
//					@Override
//					public void run() {
//						mHandlerUL.close();
//						if (mUpperLeftPreviewSurface != null) {
//							mUpperLeftPreviewSurface.release();
//							mUpperLeftPreviewSurface = null;
//						}
//						setCameraButton();
//					}
//				}, 0);
//			} else if ((mHandlerR != null) && !mHandlerR.isEqual(device)) {
//				queueEvent(new Runnable() {
//					@Override
//					public void run() {
//						mHandlerR.close();
//						if (mUpperRightPreviewSurface != null) {
//							mUpperRightPreviewSurface.release();
//							mUpperRightPreviewSurface = null;
//						}
//						setCameraButton();
//					}
//				}, 0);
//			}
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:" + device);
            Toast.makeText(MultiCameraActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onCancel:");
        }
    };

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCameraButton();
                }
            }, 0);
        }
    }

    private void setCameraButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((mHandlerUL != null) && !mHandlerUL.isOpened() && (mCaptureButtonUL != null)) {
                    mCaptureButtonUL.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerUR != null) && !mHandlerUR.isOpened() && (mCaptureButtonUR != null)) {
                    mCaptureButtonUR.setVisibility(View.INVISIBLE);
                }

                if ((mHandlerLL != null) && !mHandlerLL.isOpened() && (mCaptureButtonLL != null)) {
                    mCaptureButtonLL.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerLR != null) && !mHandlerLR.isOpened() && (mCaptureButtonLR != null)) {
                    mCaptureButtonLR.setVisibility(View.INVISIBLE);
                }

            }
        }, 0);
    }
}

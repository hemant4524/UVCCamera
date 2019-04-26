package com.serenegiant.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.serenegiant.constants.AppConstant;
import com.serenegiant.constants.Constants;
import com.serenegiant.constants.FileConstant;
import com.serenegiant.receiver.NotificationReceiver;
import com.serenegiant.server.RestClient;
import com.serenegiant.serviceclient.CameraClient;
import com.serenegiant.serviceclient.ICameraClientCallback;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usbcameratest4.MainActivity;
import com.serenegiant.usbcameratest4.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyService extends Service {
    private static final boolean DEBUG = true;
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;

    private USBMonitor mUSBMonitor;
    private List<UsbDevice> list;
    private String TAG = MyService.class.getSimpleName();
    private static final int NOTIFICATION = 101;

    private List<CameraClient> cameraClientList;
    private CameraClient cameraClient;
    private Handler handler;

    int size;
    int i = 0;
    private static final String DIR_NAME = "UNOAiCamera";
    private NotificationManager mNotificationManager;
    private ArrayList<Integer> cameraIds;


    public MyService() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        cameraClientList = new ArrayList<>();

        final File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), DIR_NAME);
        try {
            String path = dir.getAbsolutePath();
            File folder = new File(path);
            if (folder != null && folder.exists() && folder.listFiles() != null) {

                for (File file : folder.listFiles()) {
                    Log.d("File", "Delete file: " + file.getAbsolutePath());
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification(getString(R.string.app_name));


        startHandler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Start Foreground Intent ");
            // your start service code
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
            //your end servce code
            stopForeground(true);
            stopSelf();

            final Intent stopUVCService = new Intent(getApplicationContext(), UVCService.class);
            stopUVCService.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            getApplication().startService(stopUVCService);

        }
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startHandler() {

        if (mUSBMonitor == null) {
            mUSBMonitor = new USBMonitor(getApplicationContext(), mOnDeviceConnectListener);
            final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
            mUSBMonitor.setDeviceFilter(filters);
            list = mUSBMonitor.getDeviceList();

            if (DEBUG) Log.d(TAG, "startHandler: " + list.size());


            cameraIds = new ArrayList<>();
            for (int i = 0; i < mUSBMonitor.getDeviceList().size(); i++) {

                String name = mUSBMonitor.getDeviceList().get(i).getConfiguration(0).getInterface(0).getName();
                if (name == null || !name.equalsIgnoreCase("bluetooth radio")) {
                    cameraIds.add(i);
                }
            }


            size = list.size();
            handler = new Handler(getMainLooper());
//            handler.post(runnable);

            Thread thread = new Thread(new CameraThread());
            thread.start();

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    cameraClient = new CameraClient(getApplicationContext(), mCameraListener);
//                    cameraClient.select(list.get(cameraIds.get(0)));
//                    cameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//                    cameraClient.connect();
//
//                }
//            }, 1000);
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    cameraClient.disconnect();
//                    cameraClient.release();
//                    final Intent stopIntent = new Intent(getApplicationContext(), UVCService.class);
//                    stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
//                    getApplicationContext().startService(stopIntent);
//                }
//            }, 10000);
//
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    cameraClient = new CameraClient(getApplicationContext(), mCameraListener);
//                    cameraClient.select(list.get(cameraIds.get(1)));
//                    cameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//                    cameraClient.connect();
//
//                }
//            }, 11000);
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    cameraClient.disconnect();
//                    cameraClient.release();
//                    final Intent stopIntent = new Intent(getApplicationContext(), UVCService.class);
//                    stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
//                    getApplicationContext().startService(stopIntent);
//                    stopSelf();
//                }
//            }, 21000);


//            if (cameraIds.size() > 1) {
//
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        cameraClient.disconnect();
//                        cameraClient.release();
//
//                        cameraClient = new CameraClient(getApplicationContext(), mCameraListener);
//                        cameraClient.select(list.get(cameraIds.get(1)));
//                        cameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//                        cameraClient.connect();
//
//                    }
//                }, 30000);
//
//            }


//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    File root = Environment.getExternalStorageDirectory();
//
//                    final File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), DIR_NAME);
//
////                    String dirPath = root.getPath() + File.separator + FileConstant.APP_ROOT_FOLDER_NAME;
//                    String dirPath = dir.getAbsolutePath();
//
//                    File folder = new File(dirPath);
//
//                    // Send file to server
//                    MultipartBody.Builder builder = new MultipartBody.Builder();
//                    builder.setType(MultipartBody.FORM);
//
//                    MultipartBody.Part[] parts = new MultipartBody.Part[folder.listFiles().length];
//
//                    int i = 0;
//
//                    if (folder.exists()) {
//
//                        for (File file : folder.listFiles()) {
//                            Log.d("File", "Upload file: " + file.getAbsolutePath());
//                            RequestBody surveyBody = RequestBody.create(MediaType.parse("image/*"), file);
//                            parts[i] = MultipartBody.Part.createFormData("files[]", file.getName(), surveyBody);
//                            i++;
//                        }
//                    }
//
//
//                    Call<JsonObject> call = RestClient.getInstance().getApiService().uploadMultiFile1(parts);
//                    call.enqueue(new Callback<JsonObject>() {
//                        @Override
//                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//
//                            Log.d("Success response:", ": " + response);
//
//                            Toast.makeText(getApplicationContext(), "Image upload successfully! ", Toast.LENGTH_LONG).show();
//
//
//                        }
//
//                        @Override
//                        public void onFailure(Call<JsonObject> call, Throwable t) {
//
//                            Log.d("Error in  webservice:", " " + t.getMessage());
//                            Toast.makeText(getApplicationContext(), "Error in  webservice:" + t.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                }
//            }, 25000);


//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if(cameraClient !=null)
//                        cameraClient.disconnect();
//
//                    cameraClient = new CameraClient(getApplicationContext(), mCameraListener);
//                    cameraClient.select(list.get(2));
//                    cameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//                    cameraClient.connect();
//                }
//            },15000);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        if (cameraClient != null) {
//            cameraClient.release();
//            cameraClient.disconnect();
//
//        }
        if (DEBUG) Log.v(TAG, "onDestroy:");

        final Intent stopIntent = new Intent(getApplicationContext(), UVCService.class);
        stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        getApplicationContext().startService(stopIntent);
    }

    private final ICameraClientCallback mCameraListener = new ICameraClientCallback() {
        @Override
        public void onConnect() {
            if (DEBUG) Log.v(TAG, "onConnect:");

            if (!isMyServiceRunning(UVCService.class)) {

                // start UVCService
                final Intent intent = new Intent(getApplicationContext(), UVCService.class);
                intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                getApplication().startService(intent);

            } else {

                final Intent intent = new Intent(getApplicationContext(), UVCService.class);
                getApplication().stopService(intent);

            }

        }

        @Override
        public void onDisconnect() {
            if (DEBUG) Log.v(TAG, "onDisconnect:");
        }

    };

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {

        @Override
        public void onAttach(UsbDevice device) {

        }

        @Override
        public void onDettach(UsbDevice device) {

        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {

        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {

        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };


    private class CameraThread  implements Runnable {

        private boolean IS_RUNNING = true;

        public CameraThread() {

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            // Insert custom code here

            while (IS_RUNNING){
                if (i < cameraIds.size()) {

                            if (cameraClient != null) {
                                cameraClient.disconnect();
                                cameraClient.release();
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            cameraClient = new CameraClient(getApplicationContext(), mCameraListener);
                            cameraClient.select(list.get(cameraIds.get(i)));
                            cameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
                            cameraClient.connect();



                    if (DEBUG)
                        Log.d(TAG, "run->>> :" + i + " size:" + list.get(cameraIds.get(i)).getDeviceName());

                    // Repeat every 2 seconds
                    //  handler.postDelayed(runnable, 8000);

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    i = i + 1;

                } else {
                    if (DEBUG)
                        try {
                            Thread.sleep(5000);
                            if (cameraClient != null) {
                                cameraClient.disconnect();
                                cameraClient.release();
                                Log.d(TAG,"stop camera thread");
                                IS_RUNNING = false;

                              //  sendPhotoServer();


                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                }
            }




        }
    }


    private void sendPhotoServer(){


                    File root = Environment.getExternalStorageDirectory();

                    final File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), DIR_NAME);

//                    String dirPath = root.getPath() + File.separator + FileConstant.APP_ROOT_FOLDER_NAME;
                    String dirPath = dir.getAbsolutePath();

                    File folder = new File(dirPath);

                    // Send file to server
                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    builder.setType(MultipartBody.FORM);

                    MultipartBody.Part[] parts = new MultipartBody.Part[folder.listFiles().length];

                    int i = 0;

                    if (folder.exists()) {

                        for (File file : folder.listFiles()) {
                            Log.d("File", "Upload file: " + file.getAbsolutePath());
                            RequestBody surveyBody = RequestBody.create(MediaType.parse("image/*"), file);
                            parts[i] = MultipartBody.Part.createFormData("files[]", file.getName(), surveyBody);
                            i++;
                        }
                    }


                    Call<JsonObject> call = RestClient.getInstance().getApiService().uploadMultiFile1(parts);
                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                            Log.d("Success response:", ": " + response);
                            stopSelf();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Image upload successfully! ", Toast.LENGTH_LONG).show();
                                }
                            });



                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {

                            Log.d("Error in  webservice:", " " + t.getMessage());
                            stopSelf();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Error in  webservice:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * helper method to show/change message on notification area
     * and set this service as foreground service to keep alive as possible as this can.
     *
     * @param text
     */
    private void showNotification(final CharSequence text) {

        Intent intentConfirm = new Intent(this, NotificationReceiver.class);
        intentConfirm.setAction(AppConstant.START_ACTION);
        intentConfirm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntentConfirm = PendingIntent.getBroadcast(this, 0, intentConfirm, PendingIntent.FLAG_CANCEL_CURRENT);

        if (DEBUG) Log.v(TAG, "showNotification:" + text);


        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }


        // Set the info for the views that show in the notification panel.
        final Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, channel)
                    .setSmallIcon(R.drawable.ic_launcher)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(getText(R.string.camera_manager))  // the label of the entry
                    //	.addAction(android.R.drawable.ic_menu_camera,"Camera",pendingIntentConfirm)
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))  // The intent to send when the entry is clicked
                    .build();
        } else {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(getText(R.string.camera_manager))  // the label of the entry
                    //	.addAction(android.R.drawable.ic_menu_camera,"Camera",pendingIntentConfirm)
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))  // The intent to send when the entry is clicked
                    .build();
        }

        startForeground(NOTIFICATION, notification);
        // Send the notification.
        mNotificationManager.notify(NOTIFICATION, notification);
    }

    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "camera  service 1 ";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel("snap map channel", name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return "snap map channel";
    }

}

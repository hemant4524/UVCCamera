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

package com.serenegiant.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.widget.FrameLayout;

import com.serenegiant.common.BaseService;
import com.serenegiant.constants.AppConstant;
import com.serenegiant.constants.Constants;
import com.serenegiant.encoder.MediaMuxerWrapper;
import com.serenegiant.receiver.NotificationReceiver;
import com.serenegiant.serviceclient.CameraClient;
import com.serenegiant.serviceclient.ICameraClientCallback;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usbcameratest4.MainActivity;
import com.serenegiant.usbcameratest4.R;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UVCService extends BaseService {
	private static final boolean DEBUG = true;
	private static final String TAG = "UVCService";

	private static final int NOTIFICATION = R.string.app_name;

	private USBMonitor mUSBMonitor;
	private NotificationManager mNotificationManager;
	private List<UsbDevice> list;
	private CameraClient mCameraClient;

	public UVCService() {
		if (DEBUG) Log.d(TAG, "Constructor:");
	}

	private static boolean flip = true;

	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUG) Log.d(TAG, "onCreate:");
		if (mUSBMonitor == null) {
			mUSBMonitor = new USBMonitor(getApplicationContext(), mOnDeviceConnectListener);
			final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
			 mUSBMonitor.setDeviceFilter(filters);
			 list = mUSBMonitor.getDeviceList();
//



//			 mCameraClient = new CameraClient(this, new ICameraClientCallback() {
//				@Override
//				public void onConnect() {
//
//				}
//
//				@Override
//				public void onDisconnect() {
//
//				}
//			});

//				mCameraClient.select(list.get(flip?0:1));
//				mCameraClient.resize(640, 480);
//				mCameraClient.connect();

			    mUSBMonitor.register();

//			    UVCCameraTextureView uvcCameraTextureView = new UVCCameraTextureView(this);
//			    uvcCameraTextureView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
//			    CameraViewInterface cameraViewInterface = uvcCameraTextureView;
//			    mCameraClient.addSurface(cameraViewInterface.getSurface(),false);
//			    mCameraClient.captureStill(MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, "Hariomom.jpg").toString());

		}
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification(getString(R.string.app_name));

	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Start Foreground Intent ");
            // your start service code
        }
        else if (intent.getAction().equals( Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
			if (mUSBMonitor != null) {
				mUSBMonitor.unregister();
				mUSBMonitor = null;
			}
            //your end servce code
            stopForeground(true);
            stopSelf();

        }
        return START_STICKY;
    }

	@Override
	public void onDestroy() {
		if (DEBUG) Log.d(TAG, "onDestroy:");
		if (mUSBMonitor != null) {
			mUSBMonitor.unregister();
			mUSBMonitor = null;
		}
		stopForeground(true/*removeNotification*/);
		if (mNotificationManager != null) {
			mNotificationManager.cancel(NOTIFICATION);
			mNotificationManager = null;
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent intent) {

		if (DEBUG) Log.d(TAG, "onBind:" + intent);
		final String action = intent != null ? intent.getAction() : null;
		if (IUVCService.class.getName().equals(action)) {
			Log.i(TAG, "return mBasicBinder");
			return mBasicBinder;
		}
		if (IUVCSlaveService.class.getName().equals(action)) {
			Log.i(TAG, "return mSlaveBinder");
			return mSlaveBinder;
		}
		return null;
	}

	@Override
	public void onRebind(final Intent intent) {
		if (DEBUG) Log.d(TAG, "onRebind:" + intent);
	}

	@Override
	public boolean onUnbind(final Intent intent) {
		if (DEBUG) Log.d(TAG, "onUnbind:" + intent);
		if (checkReleaseService()) {
			stopSelf();
		}
		if (DEBUG) Log.d(TAG, "onUnbind:finished");
		return true;
	}

//********************************************************************************
	/**
	 * helper method to show/change message on notification area
	 * and set this service as foreground service to keep alive as possible as this can.
	 * @param text
	 */
	private void showNotification(final CharSequence text) {


		String channel;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			channel = createChannel();
		else {
			channel = "";
		}


		Intent intentConfirm = new Intent(this, NotificationReceiver.class);
		intentConfirm.setAction(AppConstant.START_ACTION);
		intentConfirm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntentConfirm = PendingIntent.getBroadcast(this, 0, intentConfirm, PendingIntent.FLAG_CANCEL_CURRENT);

		if (DEBUG) Log.v(TAG, "showNotification:" + text);
        // Set the info for the views that show in the notification panel.
		final Notification notification;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			notification = new Notification.Builder(this, channel)
				.setSmallIcon(R.drawable.ic_launcher)  // the status icon
				.setTicker(text)  // the status text
				.setWhen(System.currentTimeMillis())  // the time stamp
				.setContentTitle("UnoCameraService")  // the label of the entry
			//	.addAction(android.R.drawable.ic_menu_camera,"Camera",pendingIntentConfirm)
				.setContentText(text)  // the contents of the entry
				.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))  // The intent to send when the entry is clicked
				.build();
		}else{
			notification = new Notification.Builder(this)
					.setSmallIcon(R.drawable.ic_launcher)  // the status icon
					.setTicker(text)  // the status text
					.setWhen(System.currentTimeMillis())  // the time stamp
					.setContentTitle("UnoCameraService")  // the label of the entry
					//	.addAction(android.R.drawable.ic_menu_camera,"Camera",pendingIntentConfirm)
					.setContentText(text)  // the contents of the entry
					.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))  // The intent to send when the entry is clicked
					.build();
		}

		startForeground(NOTIFICATION, notification);
        // Send the notification.
		mNotificationManager.notify(NOTIFICATION, notification);
    }

	private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
		@Override
		public void onAttach(final UsbDevice device) {
			if (DEBUG) Log.d(TAG, "OnDeviceConnectListener#onAttach:");
		}

		@Override
		public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
			if (DEBUG) Log.d(TAG, "OnDeviceConnectListener#onConnect:");
			queueEvent(new Runnable() {
				@Override
				public void run() {
					final int key = device.hashCode();
					CameraServer service;
					synchronized (sServiceSync) {
						service = sCameraServers.get(key);
						if (service == null) {

							service = CameraServer.createServer(UVCService.this, ctrlBlock, device.getVendorId(), device.getProductId());
							sCameraServers.append(key, service);

							CameraServer finalService = service;
							new Timer().schedule(new TimerTask() {
								@Override
								public void run() {
									//Do something after 10000ms
									finalService.captureStill(MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, ".jpg").toString());
								}
							}, 10000);
						} else {
							Log.w(TAG, "service already exist before connection");
						}
						sServiceSync.notifyAll();
					}
				}
			}, 0);
		}

		@Override
		public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
			if (DEBUG) Log.d(TAG, "OnDeviceConnectListener#onDisconnect:");
			queueEvent(new Runnable() {
				@Override
				public void run() {
					removeService(device);
				}
			}, 0);
		}

		@Override
		public void onDettach(final UsbDevice device) {
			if (DEBUG) Log.d(TAG, "OnDeviceConnectListener#onDettach:");
		}

		@Override
		public void onCancel(final UsbDevice device) {
			if (DEBUG) Log.d(TAG, "OnDeviceConnectListener#onCancel:");
			synchronized (sServiceSync) {
				sServiceSync.notifyAll();
			}
		}
	};

	private void removeService(final UsbDevice device) {
		final int key = device.hashCode();
		synchronized (sServiceSync) {
			final CameraServer service = sCameraServers.get(key);
			if (service != null)
				service.release();
			sCameraServers.remove(key);
			sServiceSync.notifyAll();
		}
		if (checkReleaseService()) {
			stopSelf();
		}
	}
//********************************************************************************
	private static final Object sServiceSync = new Object();
	private static final SparseArray<CameraServer> sCameraServers = new SparseArray<CameraServer>();

	/**
	 * get CameraService that has specific ID<br>
	 * if zero is provided as ID, just return top of CameraServer instance(non-blocking method) if exists or null.<br>
	 * if non-zero ID is provided, return specific CameraService if exist. block if not exists.<br>
	 * return null if not exist matched specific ID<br>
	 * @param serviceId
	 * @return
	 */
	private static CameraServer getCameraServer(final int serviceId) {
		synchronized (sServiceSync) {
			CameraServer server = null;
			if ((serviceId == 0) && (sCameraServers.size() > 0)) {
				server = sCameraServers.valueAt(0);
			} else {
				server = sCameraServers.get(serviceId);
				if (server == null)
					try {
						Log.i(TAG, "waiting for service is ready");
						sServiceSync.wait();
					} catch (final InterruptedException e) {
					}
					server = sCameraServers.get(serviceId);
			}
			return server;
		}
	}

	/**
	 * @return true if there are no camera connection
	 */
	private static boolean checkReleaseService() {
		CameraServer server = null;
		synchronized (sServiceSync) {
			final int n = sCameraServers.size();
			if (DEBUG) Log.d(TAG, "checkReleaseService:number of service=" + n);
			for (int i = 0; i < n; i++) {
				server = sCameraServers.valueAt(i);
				Log.i(TAG, "checkReleaseService:server=" + server + ",isConnected=" + (server != null && server.isConnected()));
				if (server != null && !server.isConnected()) {
					sCameraServers.removeAt(i);
					server.release();
				}
			}
			return sCameraServers.size() == 0;
		}
	}

//********************************************************************************
	private final IUVCService.Stub mBasicBinder = new IUVCService.Stub() {
		private IUVCServiceCallback mCallback;

		@Override
		public int select(final UsbDevice device, final IUVCServiceCallback callback) throws RemoteException {
			flip = !flip;
			if (DEBUG) Log.d(TAG, "mBasicBinder#select:device=" + (device !=null ? device.getDeviceName() : null));
			mCallback = callback;
			final int serviceId = device.hashCode();
			CameraServer server = null;
			synchronized (sServiceSync) {
				server = sCameraServers.get(serviceId);
				if (server == null) {


					Log.i(TAG, "request permission** "+flip);
					//mUSBMonitor.requestPermission(flip?list.get(2):list.get(0));

					mUSBMonitor.requestPermission(device);
					Log.i(TAG, "wait for getting permission");
					try {
						sServiceSync.wait();
					} catch (final Exception e) {
						Log.e(TAG, "connect:", e);
					}
					Log.i(TAG, "check service again");
					server = sCameraServers.get(serviceId);
					if (server == null) {
						throw new RuntimeException("failed to open USB device(has no permission)");
					}
				}
			}
			if (server != null) {
				Log.i(TAG, "success to get service:serviceId=" + serviceId);
				server.registerCallback(callback);
			}
			return serviceId;
		}

		@Override
		public void release(final int serviceId) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mBasicBinder#release:");
			synchronized (sServiceSync) {
				final CameraServer server = sCameraServers.get(serviceId);
				if (server != null) {
					if (server.unregisterCallback(mCallback)) {
						if (!server.isConnected()) {
							sCameraServers.remove(serviceId);
							if (server != null) {
								server.release();
							}
							final CameraServer srv = sCameraServers.get(serviceId);
							Log.w(TAG, "srv=" + srv);
						}
					}
				}
			}
			mCallback = null;
		}

		@Override
		public boolean isSelected(final int serviceId) throws RemoteException {
			return getCameraServer(serviceId) != null;
		}

		@Override
		public void releaseAll() throws RemoteException {
			if (DEBUG) Log.d(TAG, "mBasicBinder#releaseAll:");
			CameraServer server;
			synchronized (sServiceSync) {
				final int n = sCameraServers.size();
				for (int i = 0; i < n; i++) {
					server = sCameraServers.valueAt(i);
					sCameraServers.removeAt(i);
					if (server != null) {
						server.release();
					}
				}
			}
		}

		@Override
		public void resize(final int serviceId, final int width, final int height) {
			if (DEBUG) Log.d(TAG, "mBasicBinder#resize:");
			final CameraServer server = getCameraServer(serviceId);
			if (server == null) {
				throw new IllegalArgumentException("invalid serviceId");
			}
			server.resize(width, height);
		}

		@Override
		public void connect(final int serviceId) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mBasicBinder#connect:");
			final CameraServer server = getCameraServer(serviceId);
			if (server == null) {
				throw new IllegalArgumentException("invalid serviceId");
			}
			server.connect();
		}

		@Override
		public void disconnect(final int serviceId) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mBasicBinder#disconnect:");
			final CameraServer server = getCameraServer(serviceId);
			if (server == null) {
				throw new IllegalArgumentException("invalid serviceId");
			}
			server.disconnect();
		}

		@Override
		public boolean isConnected(final int serviceId) throws RemoteException {
			final CameraServer server = getCameraServer(serviceId);
			return (server != null) && server.isConnected();
		}

		@Override
		public void addSurface(final int serviceId, final int id_surface, final Surface surface, final boolean isRecordable) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mBasicBinder#addSurface:id=" + id_surface + ",surface=" + surface);
			final CameraServer server = getCameraServer(serviceId);
			if (server != null)
				server.addSurface(id_surface, surface, isRecordable, null);
		}

		@Override
		public void removeSurface(final int serviceId, final int id_surface) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mBasicBinder#removeSurface:id=" + id_surface);
			final CameraServer server = getCameraServer(serviceId);
			if (server != null)
				server.removeSurface(id_surface);
		}

		@Override
		public boolean isRecording(final int serviceId) throws RemoteException {
			final CameraServer server = getCameraServer(serviceId);
			return server != null && server.isRecording();
		}

		@Override
		public void startRecording(final int serviceId) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mBasicBinder#startRecording:");
			final CameraServer server = getCameraServer(serviceId);
			if ((server != null) && !server.isRecording()) {
				server.startRecording();
			}
		}

		@Override
		public void stopRecording(final int serviceId) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mBasicBinder#stopRecording:");
			final CameraServer server = getCameraServer(serviceId);
			if ((server != null) && server.isRecording()) {
				server.stopRecording();
			}
		}

		@Override
		public void captureStillImage(final int serviceId, final String path) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mBasicBinder#captureStillImage:" + path);
			final CameraServer server = getCameraServer(serviceId);
			if (server != null) {
				server.captureStill(path);
			}
		}

    };

//********************************************************************************
	private final IUVCSlaveService.Stub mSlaveBinder = new IUVCSlaveService.Stub() {
		@Override
		public boolean isSelected(final int serviceID) throws RemoteException {
			return getCameraServer(serviceID) != null;
		}

		@Override
		public boolean isConnected(final int serviceID) throws RemoteException {
			final CameraServer server = getCameraServer(serviceID);
			return server != null && server.isConnected();
		}

		@Override
		public void addSurface(final int serviceID, final int id_surface, final Surface surface, final boolean isRecordable, final IUVCServiceOnFrameAvailable callback) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mSlaveBinder#addSurface:id=" + id_surface + ",surface=" + surface);
			final CameraServer server = getCameraServer(serviceID);
			if (server != null) {
				server.addSurface(id_surface, surface, isRecordable, callback);
			} else {
				Log.e(TAG, "failed to get CameraServer:serviceID=" + serviceID);
			}
		}

		@Override
		public void removeSurface(final int serviceID, final int id_surface) throws RemoteException {
			if (DEBUG) Log.d(TAG, "mSlaveBinder#removeSurface:id=" + id_surface);
			final CameraServer server = getCameraServer(serviceID);
			if (server != null) {
				server.removeSurface(id_surface);
			} else {
				Log.e(TAG, "failed to get CameraServer:serviceID=" + serviceID);
			}
		}
	};


	@NonNull
	@TargetApi(26)
	private synchronized String createChannel() {
		NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		String name = "snap map fake location ";
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

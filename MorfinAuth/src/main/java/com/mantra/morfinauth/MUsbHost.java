package com.mantra.morfinauth;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import com.mantra.morfinauth.enums.DeviceModel;

import java.util.HashMap;

public class MUsbHost {

	private Context context;
	private UsbHostCallback callback;

	private static final String ACTION_USB_PERMISSION = "com.mantra.MUsbHost.USB_PERMISSION";
	private UsbManager manager;

	private static int fd = 0;
	private static final int MANTRA_VENDOR_ID = 0x2C0F;
	private static final int MFS500_PRODUCT_ID = 0x1100;
	private static final int MELO31_PRODUCT_ID = 0x120B;
	private static final int MARC10_PRODUCT_ID = 0x120D;
	private static final int MARC30_PRODUCT_ID = 0x1208;
	private static final int MFS200_PRODUCT_ID = 0x1105;
	private static final int MELO30_PRODUCT_ID = 0x1218;
	private static final int MELO20_PRODUCT_ID = 0x1101;
	private static String PRODUCT_NAME = "";

	public MUsbHost(Context context, UsbHostCallback callback) {
		this.context = context;
		this.callback = callback;
		manager = (UsbManager) this.context.getSystemService(Context.USB_SERVICE);
		PRODUCT_NAME = "";
	}

	private boolean IsMantraDevice(int vid, int pid) {
		if (vid == MANTRA_VENDOR_ID &&  pid == MARC30_PRODUCT_ID || pid == MFS500_PRODUCT_ID
				|| pid == MELO31_PRODUCT_ID || pid == MARC10_PRODUCT_ID || pid == MELO20_PRODUCT_ID
				|| pid == MFS200_PRODUCT_ID || pid == MELO30_PRODUCT_ID) {
			switch (pid) {
				case MFS500_PRODUCT_ID:
					PRODUCT_NAME = "MFS500";
					break;
				case MELO31_PRODUCT_ID:
					PRODUCT_NAME = "MELO31";
					break;
				case MARC10_PRODUCT_ID:
					PRODUCT_NAME = "MARC10";
					break;
				case MARC30_PRODUCT_ID:
					PRODUCT_NAME = "MARC30";
					break;
				case MFS200_PRODUCT_ID:
					PRODUCT_NAME = "MFS200";
					break;
				case MELO30_PRODUCT_ID:
					PRODUCT_NAME = "MELO30";
					break;
				case MELO20_PRODUCT_ID:
					PRODUCT_NAME = "MELO20";
					break;
			}
			return true;
		}
		PRODUCT_NAME = "";
		return false;
	}

	public String GetProductName() {
		if (fd == 0) {
			PRODUCT_NAME = "";
		}
		return PRODUCT_NAME;
	}

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void RegisteredUsbHost() {
		try {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_USB_PERMISSION);
			filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
			filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				this.context.registerReceiver(mUsbReceiver, filter, Context.RECEIVER_EXPORTED);
			}else{
				this.context.registerReceiver(mUsbReceiver, filter);
			}
			FindDeviceAndRequestPermission();
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}

	public void UnRegisteredUsbHost() {
		try {
			if (mUsbReceiver != null) {
				this.context.unregisterReceiver(mUsbReceiver);
			}
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}

	public int getFd() {
		return fd;
	}

	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			assert action != null;
			switch (action) {
				case UsbManager.ACTION_USB_DEVICE_ATTACHED:
					FindDeviceAndRequestPermission();
					break;
				case UsbManager.ACTION_USB_DEVICE_DETACHED:
					UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					int vid = device.getVendorId();
					int pid = device.getProductId();
					if (IsMantraDevice(vid, pid)) {
						if (callback != null) {
							callback.OnUsbDeviceDisconnected(DeviceModel.valueFor(PRODUCT_NAME));
						}
						fd = 0;
						PRODUCT_NAME = "";
					}
					break;
				case ACTION_USB_PERMISSION:
					synchronized (this) {
						try {
							UsbDevice device2 = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
							vid = device2.getVendorId();
							pid = device2.getProductId();

							if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
								if (IsMantraDevice(vid, pid)) {
									UsbDeviceConnection connection = manager.openDevice(device2);
									try {
										fd = connection.getFileDescriptor();
									} catch (Exception ignore) {
										fd = 0;
										ignore.printStackTrace();
									}
									if (callback != null) {
										if (fd != 0) {
											callback.OnUsbDeviceConnected(true, fd,DeviceModel.valueFor(PRODUCT_NAME));
										} else {
											callback.OnUsbDeviceConnected(false, fd,DeviceModel.valueFor(PRODUCT_NAME));
										}
									}
								}
							} else {
								if (IsMantraDevice(vid, pid)) {
									if (callback != null) {
										fd = 0;
										callback.OnUsbDeviceConnected(false, fd,DeviceModel.valueFor(PRODUCT_NAME));
									}
								}
							}
						} catch (Exception ignore) {
							ignore.printStackTrace();
						}
					}
					break;
			}
		}
	};

	@SuppressLint("MutableImplicitPendingIntent")
	protected void FindDeviceAndRequestPermission() {
		try {
			UsbDevice device = null;
			HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
			for (UsbDevice usbDevice : deviceList.values()) {
				int pid = usbDevice.getProductId();
				int vid = usbDevice.getVendorId();
				if (IsMantraDevice(vid, pid)) {
					device = usbDevice;
					break;
				}
			}
			if (device == null) {
				return;
			}
			if (PRODUCT_NAME != null && !PRODUCT_NAME.equalsIgnoreCase("")) {
				callback.OnUsbDevices(PRODUCT_NAME);
			}
			Intent intent = new Intent(ACTION_USB_PERMISSION);
			PendingIntent mPermissionIntent;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				mPermissionIntent = PendingIntent.getBroadcast(this.context, 0, intent,
						PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT | PendingIntent.FLAG_MUTABLE);
			}else{
				mPermissionIntent = PendingIntent.getBroadcast(this.context, 0, intent,
						PendingIntent.FLAG_MUTABLE);
			}
			manager.requestPermission(device, mPermissionIntent);
		} catch (Exception e) {
			Log.e("", "FindDeviceAndRequestPermission.Exception", e);
		}
	}

	public interface UsbHostCallback {
		void OnUsbDevices(String name);
		void OnUsbDeviceConnected(boolean hasPermission, int fd, DeviceModel model);
		void OnUsbDeviceDisconnected(DeviceModel model);
	}
}

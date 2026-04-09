package com.mantra.morfinauth;

import android.content.Context;
import android.util.Log;

import com.mantra.morfinauth.enums.DeviceDetection;
import com.mantra.morfinauth.enums.DeviceModel;
import com.mantra.morfinauth.enums.ImageFormat;
import com.mantra.morfinauth.enums.LogLevel;
import com.mantra.morfinauth.enums.TemplateFormat;

import java.util.ArrayList;
import java.util.List;

public class MorfinAuth implements MUsbHost.UsbHostCallback, MorfinAuthNative.Callback {

    private MUsbHost mUsbHost;
    private static MorfinAuth_Callback morfin_auth_callback;

    private int fd = 0;
    private DeviceModel deviceModel = null;
    private static DeviceInfo info = null;

    private boolean isDeviceInit = false;
    private static boolean isCaptureRunning = false;
    private static boolean isStopCaptureRunning = false;
    private List<DeviceList> connectedDeviceList = null;
    private Thread initThread;
    private Thread isConnectThread;

    /**
     * Morfin_auth Constructor for registered callback and usb devices.
     * Registered usb broadcast receiver
     *
     * @param context                 application context
     * @param morfin_auth_callback callback
     */
    public MorfinAuth(Context context, MorfinAuth_Callback morfin_auth_callback) {
        MorfinAuth.morfin_auth_callback = morfin_auth_callback;
        MorfinAuthNative.RegisterCallback(this);
        mUsbHost = new MUsbHost(context, this);
        mUsbHost.RegisteredUsbHost();
    }

    public int GetConnectedDevices(List<String> lists) {
        if (connectedDeviceList == null) {
            return MorfinAuthNative.IMG_PROCESS_E_NO_DEVICE;
        }
        for (DeviceList list : connectedDeviceList) {
            lists.add(list.Model);
        }
        return MorfinAuthNative.MORFIN_AUTH_SUCCESS;
    }

    public int GetSupportedDevices(List<String> lists) {
        try {
            int[] siz = new int[1];
            int ret = MorfinAuthNative.GetSupportedDeviceList(null, siz);
            if (ret != 0) {
                return ret;
            } else {
                if (siz[0] == 0) {
                    return ret;
                }
                DeviceList[] deviceLists = new DeviceList[siz[0]];
                ret = MorfinAuthNative.GetSupportedDeviceList(deviceLists, siz);
                if (ret == 0) {
                    for (DeviceList list : deviceLists) {
                        if(!list.Model.equals("MARC30")){
                            lists.add(list.Model);
                        }
                    }
                }
                return ret;
            }
        } catch (Exception e) {
            return MorfinAuthNative.MORFIN_AUTH_UNHANDLED_EXCEPTION;
        }
    }

    public synchronized int Init(DeviceModel name, String clientKey , DeviceInfo deviceInfo) {
        try {
            if (name == null) {
                return MorfinAuthNative.IMG_PROCESS_E_NO_DEVICE;
            }
            if (isDeviceInit && info != null && deviceModel == name) {
//                deviceInfo = info;
                if (deviceInfo != null) {
                    deviceInfo.Make = info.Make;
                    deviceInfo.Model = info.Model;
                    deviceInfo.SerialNo = info.SerialNo;
                    deviceInfo.Width = info.Width;
                    deviceInfo.Height = info.Height;
                    deviceInfo.DPI = info.DPI;
                }
                return 0;
            }
            if (deviceModel == null || deviceModel != name) {
                Uninit();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mUsbHost.FindDeviceAndRequestPermission();
                    }
                }).start();
                //wait for permission
                try {
                    initThread = Thread.currentThread();
//                    wait();
                    synchronized (initThread) {
                        initThread.wait(10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    initThread = null;
                }
                if (deviceModel == null) {
                    return MorfinAuthNative.DEVICE_NOT_INITIALIZED;
                }
            }
            byte[] key = null;
            if (clientKey != null && clientKey.length() > 0) {
                key = clientKey.getBytes();
            }
            info = new DeviceInfo();
            int ret = MorfinAuthNative.Init(fd, deviceModel.getDeviceName(),key, info);
            if (ret == 0) {
                if (deviceInfo != null) {
                    deviceInfo.Make = info.Make;
                    deviceInfo.Model = info.Model;
                    deviceInfo.SerialNo = info.SerialNo;
                    deviceInfo.Width = info.Width;
                    deviceInfo.Height = info.Height;
                    deviceInfo.DPI = info.DPI;
                }
                isDeviceInit = true;
            } else {
                deviceModel = DeviceModel.valueFor(deviceModel.getDeviceName());
                isDeviceInit = false;
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return MorfinAuthNative.MORFIN_AUTH_UNHANDLED_EXCEPTION;
        }
    }


    /**
     * @param clientKey
     * @param signeKey
     * @return
     *//*
    public int GetClientKey(String clientKey, String[] signeKey) {
        byte[] signedKey = new byte[2048];
        int[] keyLen = new int[1];
        int ret = MorfinAuthNative.GenerateSignedKey(clientKey.getBytes(), signedKey, keyLen);
        if (ret == 0) {
            byte[] finalKey = new byte[keyLen[0]];
            System.arraycopy(signedKey, 0, finalKey, 0, keyLen[0]);
            // Set the first element of the array to the resulting string
            signeKey[0] = new String(finalKey);
            Log.i("TAG", "Key : " + signeKey[0]);
        }
        return ret;
    }*/

    /**
     * Initialized MFS500, MARK 10 & MELO 31 devices
     *
     * @param name device model name for initialize
     * @return device init success on return 0 else return -error code
     */
    public synchronized int Init(DeviceModel name, DeviceInfo deviceInfo) {
        return Init(name, null, deviceInfo);
    }

    /**
     * Uninitialized MFS500, MARK 10 & MELO 31 devices
     *
     * @return device uninit success on return 0 else return -error code
     */
    public int Uninit() {
        try {
            isDeviceInit = false;
            int ret = 0;
            if (isCaptureRunning) {
                ret = StopCapture();
            }
            ret = MorfinAuthNative.Uninit();
            info = null;
            isCaptureRunning = false;
            isStopCaptureRunning=false;
            return ret;
        } catch (Exception e) {
            return MorfinAuthNative.MORFIN_AUTH_UNHANDLED_EXCEPTION;
        }
    }

    /**
     * Get running sdk version
     *
     * @return sdk version
     */
    public String GetSDKVersion() {
        return MorfinAuthNative.GetVersion();
    }

    /**
     * Check device is connected or not
     *
     * @param name device model name
     * @return device connected on return true else return false
     */
    public boolean IsDeviceConnected(final DeviceModel name) {
        int ret = 0;
        if (name == null) {
            return false;
        }
        if (deviceModel == name) {
            ret = MorfinAuthNative.IsDeviceConnected(fd, name.getDeviceName());
        } else {
            if (deviceModel == null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mUsbHost.FindDeviceAndRequestPermission();
                    }
                }).start();
                try {
                    isConnectThread = Thread.currentThread();
                    synchronized (isConnectThread) {
                        isConnectThread.wait(10000);
                    }
                } catch (InterruptedException e) {

                } finally {
                    isConnectThread = null;
                }
                if (deviceModel == null) {
                    return false;
                }
            }
            ret = MorfinAuthNative.IsDeviceConnected(fd, name.getDeviceName());
        }
        return ret == 0;
    }

    /**
     * Device initialize success after start capture working. This is asynchronous capture.
     * Capture success after OnComplete event called
     *
     * @param minQuality minimum finger quality (Value: 0 to 100)
     * @param timeOut    wait for capture finger. timeout set in milliseconds. 0 for infinite timeout.
     * @return capture started on return 0 else -error code return
     */
    public int StartCapture(int minQuality, int timeOut) {
        if (isCaptureRunning) {
            return MorfinAuthNative.CAPTURE_ALREADY_STARTED;
        }
        if (isStopCaptureRunning) {
            return MorfinAuthNative.MORFIN_AUTH_STOP_CAPTURE_RUNNING;
        }
        isCaptureRunning = true;
        if (deviceModel == null) {
            isCaptureRunning = false;
            return MorfinAuthNative.IMG_PROCESS_E_NO_DEVICE;
        }
        if (!isDeviceInit || fd == 0) {
            isCaptureRunning = false;
            return MorfinAuthNative.DEVICE_NOT_INITIALIZED;
        }
        if (timeOut < 1000 && timeOut > 0) {
            timeOut = 1000;
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        int ret = MorfinAuthNative.StartCapture(minQuality, timeOut);
        if (ret != 0) {
            isCaptureRunning = false;
        }
        return ret;
    }

    /**
     * Device initialize success after start capture working. This is synchronous capture.
     *
     * @param minQuality minimum finger quality (Value: 0 to 100)
     * @param timeOut    wait for capture finger. timeout set in milliseconds. 0 for infinite timeout.
     * @return capture success on return 0 else -error code return
     */
    public int AutoCapture(int minQuality, int timeOut, int[] quality, int[] nfiq) {
        if (isCaptureRunning) {
            return MorfinAuthNative.CAPTURE_ALREADY_STARTED;
        }
        if (isStopCaptureRunning) {
            return MorfinAuthNative.MORFIN_AUTH_STOP_CAPTURE_RUNNING;
        }
        isCaptureRunning = true;
        if (deviceModel == null) {
            isCaptureRunning = false;
            return MorfinAuthNative.IMG_PROCESS_E_NO_DEVICE;
        }
        if (!isDeviceInit || fd == 0) {
            isCaptureRunning = false;
            return MorfinAuthNative.DEVICE_NOT_INITIALIZED;
        }
        if (quality == null || nfiq == null) {
            return MorfinAuthNative.OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }
        /*try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }*/
        int ret = MorfinAuthNative.AutoCapture(minQuality, timeOut, quality, nfiq);
        isCaptureRunning = false;
        return ret;
    }

    /**
     * Capture stop
     *
     * @return capture stop on return 0 else -error code
     */
    public int StopCapture() {
        if (deviceModel == null) {
            return MorfinAuthNative.IMG_PROCESS_E_NO_DEVICE;
        }
        if (!isDeviceInit || fd == 0) {
            return MorfinAuthNative.DEVICE_NOT_INITIALIZED;
        }
        if (!isCaptureRunning) {
            return MorfinAuthNative.MORFIN_AUTH_CAPTURE_NOT_RUNNING;
        }
        if (isStopCaptureRunning) {
            return MorfinAuthNative.MORFIN_AUTH_STOP_CAPTURE_RUNNING;
        }
        isStopCaptureRunning = true;
        int ret = MorfinAuthNative.StopCapture();
        isStopCaptureRunning = false;
        isCaptureRunning = false;
        return ret;
    }

    /**
     * Get last finger capture image
     *
     * @param image            passed image reference
     * @param imageLen         image reference length
     * @param compressionRatio WSQ and JPEG2000 for parameter work
     * @param format           image format
     * @return get image success on return 0 else -error code
     */
    public int GetImage(byte[] image, int[] imageLen, int compressionRatio, ImageFormat format) {
        if (deviceModel == null) {
            return MorfinAuthNative.IMG_PROCESS_E_NO_DEVICE;
        }
        if (!isDeviceInit || fd == 0) {
            return MorfinAuthNative.DEVICE_NOT_INITIALIZED;
        }
        if (image == null || imageLen == null) {
            return MorfinAuthNative.OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }
        int[] dataLen = new int[]{(info.Width * info.Height + 1100)};
        byte[] data = new byte[dataLen[0]];
        int ret = MorfinAuthNative.GetImage(data, dataLen, format.getValue(), compressionRatio);
        if (ret == 0) {
            imageLen[0] = dataLen[0];
            System.arraycopy(data, 0, image, 0, imageLen[0]);
        }
        return ret;
    }

    /**
     * Get last finger capture template
     *
     * @param template    passed template reference
     * @param templateLen template reference length
     * @param format      image format
     * @return get template success on return 0 else -error code
     */
    public int GetTemplate(byte[] template, int[] templateLen, TemplateFormat format) {
        if (deviceModel == null) {
            return MorfinAuthNative.IMG_PROCESS_E_NO_DEVICE;
        }
        if (!isDeviceInit || fd == 0) {
            return MorfinAuthNative.DEVICE_NOT_INITIALIZED;
        }

        if (template == null || templateLen == null) {
            return MorfinAuthNative.OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }
        int[] dataLen = new int[]{(info.Width * info.Height)};
        byte[] data = new byte[dataLen[0]];
        int ret = MorfinAuthNative.GetTemplate(data, dataLen, format.getValue());
        if (ret == 0) {
            templateLen[0] = dataLen[0];
            System.arraycopy(data, 0, template, 0, templateLen[0]);
        }
        return ret;
    }

    /**
     * Check capture running status
     *
     * @return capture running on return true else return false
     */
    public boolean IsCaptureRunning() {
        return isCaptureRunning;
    }

    /**
     * Compare two ANSI or ISO(FMR) template and return matching score.
     * Matching score range 0 to 1000
     *
     * @param probeTemplate   probe template ANSI or ISO(FMR)
     * @param galleryTemplate gallery template ANSI or ISO(FMR)
     * @param format          template format (ANSI or ISO)
     * @return return >=0 on matching score else -error code
     */
    public int MatchTemplate(byte[] probeTemplate, byte[] galleryTemplate, int[] matchScore, TemplateFormat format) {
        if (deviceModel == null) {
            return MorfinAuthNative.IMG_PROCESS_E_NO_DEVICE;
        }
        if (!isDeviceInit) {
            return MorfinAuthNative.DEVICE_NOT_INITIALIZED;
        }

        if (probeTemplate == null || galleryTemplate == null || matchScore == null) {
            return MorfinAuthNative.OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }

        int[] matchScore1 = new int[1];
        int ret = MorfinAuthNative.MatchTemplate(probeTemplate, galleryTemplate, matchScore1, format.getValue());
        if (ret == 0) {
            matchScore[0] = matchScore1[0];
        }
        return ret;
    }

    /**
     * Error description
     *
     * @param errorCode error code
     * @return error description
     */
    public String GetErrorMessage(int errorCode) {
        switch (errorCode) {
            case MorfinAuthNative.MORFIN_AUTH_CAPTURE_NOT_RUNNING:
                return "Capture not running";
            case MorfinAuthNative.MORFIN_AUTH_STOP_CAPTURE_RUNNING:
                return "Stop Capture already running";
            case MorfinAuthNative.MORFIN_AUTH_UNHANDLED_EXCEPTION:
                return "Unhandled exceptions";
            case MorfinAuthNative.unsupported_licence:
            case MorfinAuthNative.IMG_PROCESS_E_BAD_LICENSE:
                return "Unsupported Licence";
            default:
                return MorfinAuthNative.GetErrorMessage(errorCode);
        }

    }

    /**
     * SDK level log write in file for more debug
     *
     * @param file  file name with full path
     * @param level write level log. Default OFF
     */
    public void SetLogProperties(String file, LogLevel level) {
        if (file == null || file.length() == 0) {
            return;
        }
        MorfinAuthNative.EnableLogs(level.getValue(), file);
    }

    /**
     * Release all memory, data and unregistered broadcast receiver
     */
    public void Dispose() {
        fd = 0;
        deviceModel = null;
        info = null;
        isDeviceInit = false;
        isCaptureRunning = false;
        isStopCaptureRunning=false;
        if (mUsbHost != null) {
            mUsbHost.UnRegisteredUsbHost();
        }
    }

    /**
     * Start capture or auto capture call on preview callback called from Native .SO
     *
     * @param errorCode    [OUT] 0 if no error else return -error code
     * @param Quality      [OUT] finger print quality
     * @param previewImage [OUT] finger image for display
     */
    public void PreviewCallback(int errorCode, int Quality, byte[] previewImage) {
        if (morfin_auth_callback != null) {
            morfin_auth_callback.OnPreview(errorCode, Quality, previewImage);
        }
    }

    /**
     * Start capture call after capture success/timeout/other error on complete callback called from Native .SO
     * AutoCapture on this callback not called.
     *
     * @param errorCode [OUT] 0 if no error else return -error code
     * @param Quality   [OUT] finger print quality
     * @param NFIQ      [OUT] finger print NFIQ
     */
    public void CompleteCallback(int errorCode, int Quality, int NFIQ) {
        isCaptureRunning = false;
        if (morfin_auth_callback != null) {
            morfin_auth_callback.OnComplete(errorCode, Quality, NFIQ);
        }
    }

    @Override
    public void FingerPositionCallback(int errorCode, int position) {
        if (morfin_auth_callback != null) {
            morfin_auth_callback.OnFingerPosition(errorCode, position);
        }
    }

    @Override
    public void OnUsbDevices(String name) {
        connectedDeviceList = new ArrayList<>();
        DeviceList list = new DeviceList();
        list.Model = name;
        connectedDeviceList.add(list);
        if (morfin_auth_callback != null) {
            morfin_auth_callback.OnDeviceDetection(list.Model, DeviceDetection.CONNECTED);
        }
    }

    @Override
    public void OnUsbDeviceConnected(boolean hasPermission, int fd,DeviceModel model) {
        if (hasPermission) {
            this.fd = fd;
            this.deviceModel = model;
        }
        try {
            if (initThread != null && initThread.isAlive())
                synchronized (initThread) {
                    initThread.interrupt();
                }

            if (isConnectThread != null && isConnectThread.isAlive())
                synchronized (isConnectThread) {
                    isConnectThread.interrupt();
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnUsbDeviceDisconnected(DeviceModel model) {
        this.fd = 0;
        deviceModel = null;
        DeviceList list = new DeviceList();
        list.Model = model.getDeviceName();
        Uninit();
        if (connectedDeviceList != null) {
            for (int i = 0; i < connectedDeviceList.size(); i++) {
                DeviceList list1 = connectedDeviceList.get(i);
                if (list1.Model.equals(list.Model)) {
                    connectedDeviceList.remove(i);
                    break;
                }
            }
            if (connectedDeviceList.size() == 0) {
                connectedDeviceList = null;
            }
        }
        while (isCaptureRunning) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (morfin_auth_callback != null) {
            morfin_auth_callback.OnDeviceDetection(model.getDeviceName(), DeviceDetection.DISCONNECTED);
        }
    }
}
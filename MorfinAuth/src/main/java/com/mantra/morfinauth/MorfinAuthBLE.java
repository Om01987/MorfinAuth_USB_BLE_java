package com.mantra.morfinauth;

import android.annotation.SuppressLint;
import android.content.Context;

import com.mantra.morfinauth.ble.MorfinAuthBLE_Callback;
import com.mantra.morfinauth.ble.enums.CaptureFormat;
import com.mantra.morfinauth.ble.model.BatteryInformation;
import com.mantra.morfinauth.ble.model.MorfinBleDevice;
import com.mantra.morfinauth.ble.model.TimerValues;
import com.mantra.morfinauth.enums.ImageFormat;
import com.mantra.morfinauth.enums.TemplateFormat;

import java.util.List;

public class MorfinAuthBLE {

    private MorfinAuthBLENative morfinAuthBLENative;

    /**
     * @param context application context
     * @param morfinAuthBLECallBacks MorfinAuthBLE_Callback interface class
     * @throws RuntimeException
     */
    public MorfinAuthBLE(Context context, MorfinAuthBLE_Callback morfinAuthBLECallBacks) throws RuntimeException {
        morfinAuthBLENative = new MorfinAuthBLENative(context);
        morfinAuthBLENative.registerCallBacks(morfinAuthBLECallBacks);
    }


    /**
     * @return start discover nearby devices
     */
    @SuppressLint("NewApi")
    public int DiscoverDevices() {
        return morfinAuthBLENative.discoverDevices();
    }

    /**
     * @return stops discovering nearby devices
     */
    public int StopDiscover() {
        return morfinAuthBLENative.stopDiscover();
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    public int ConnectDevice(MorfinBleDevice morfinBleDevice) {
        return morfinAuthBLENative.connectDevice(morfinBleDevice);
    }

    @SuppressLint("MissingPermission")
    public int Disconnect() {
        return morfinAuthBLENative.DisConnect();
    }

    /**
     * @return string of SDK version e.g 1.0
     */
    public String GetSDKVersion() {
        return morfinAuthBLENative.GetSDKVersion();
    }

    /**
     * @param batteryCondition (contains , charger connected or not, battery percentage,
     *                        battery health,temperature)
     * @return 0 on success else negative error code
     */
    public int GetBatteryInformation(BatteryInformation batteryCondition) {
        return morfinAuthBLENative.getBatteryInformation(batteryCondition);
    }

    /**
     * @param timerValues (contains timer value of advertisement, sleep timer, deep sleep timer(off) )
     * @return 0 on success with all timer values else negative error code
     */
    public int GetDeviceTimerValues(TimerValues timerValues) {
        return morfinAuthBLENative.getDeviceTimerValues(timerValues);
    }

    /**
     * @param timerValues (pass timer value of advertisement, sleep timer, deep sleep timer(off) )
     * @return 0 on success else negative error code
     */
    public int SetDeviceTimerValues(TimerValues timerValues) {
        return morfinAuthBLENative.setDeviceTimerValues(timerValues);
    }


    /**
     * @param lists (list of supported devices)
     * @return 0 on success with list of supported devices else negative error code
     */
    public int GetSupportedDeviceList(List<String> lists) {
        try {
            int[] siz = new int[1];
            int ret = morfinAuthBLENative.getSupportedDeviceList(null, siz);
            if (ret != 0) {
                return ret;
            } else {
                if (siz[0] == 0) {
                    return ret;
                }
                DeviceList[] deviceLists = new DeviceList[siz[0]];
                ret = morfinAuthBLENative.getSupportedDeviceList(deviceLists, siz);
                if (ret == 0) {
                    for (DeviceList list : deviceLists) {
                        lists.add(list.Model);
                    }
                }
                return ret;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MorfinAuthNative.MORFIN_AUTH_UNHANDLED_EXCEPTION;
        }
    }

    /**
     * @param clientKey pass client key if your device support clientkey feature
     * @param deviceInfo (contains srNo, make ,model ,height, width )
     * @return 0 on success with fill deviceInfo else negative error code
     */
    public int InitDevice(String clientKey, DeviceInfo deviceInfo) {
        byte[] key = null;
        if (clientKey != null && clientKey.length() > 0) {
            key = clientKey.getBytes();
        }
        return morfinAuthBLENative.InitDevice(key, deviceInfo);
    }
    public int InitDevice( DeviceInfo deviceInfo) {
        return morfinAuthBLENative.InitDevice(null, deviceInfo);
    }


    /**
     * @return
     */
    public int UnInitDevice(){
        return morfinAuthBLENative.unInitDevice();
    }

    /**
     * @param captureFormat
     * @param min_Quality
     * @param timeout
     * @param quality
     * @param nfiq
     * @return
     */
    public int StartCapture(CaptureFormat captureFormat, int min_Quality, int timeout, int[] quality, int[] nfiq) {
        return morfinAuthBLENative.startCapture(captureFormat, min_Quality, timeout, quality, nfiq);
    }

    /**
     * @return
     */
    public int StopCapture(){
        return morfinAuthBLENative.stopCapture();
    }

    /**
     * @param galleryTemplate
     * @param reqQuality
     * @param timeout
     * @param format
     * @param matchScore
     * @return
     */
    public int MatchTemplate(byte[] galleryTemplate, int reqQuality, int timeout, TemplateFormat format, int[] matchScore){
        return morfinAuthBLENative.matchTemplate(galleryTemplate,reqQuality,timeout,format,matchScore);
    }

    /**
     * @param format
     * @param template
     * @param templateLen
     * @return
     */
    public int GetTemplate(TemplateFormat format, byte[] template, int[] templateLen) {
        return morfinAuthBLENative.GetTemplate(format, template, templateLen);
    }

    /**
     * @param format
     * @param image
     * @param imageLen
     * @return
     */
    public int GetImage(ImageFormat format, byte[] image, int[] imageLen) {
        return morfinAuthBLENative.GetImage(format, image, imageLen);
    }

    /**
     * @param bluetoothDevice
     * @return
     */
    public boolean IsDeviceConnected(MorfinBleDevice bluetoothDevice){
        return morfinAuthBLENative.isDeviceConnected(bluetoothDevice);
    }

    /**
     * @param errorCode
     * @return
     */
    public String GetErrorDescription(int errorCode){
        return morfinAuthBLENative.getErrorDescription(errorCode);
    }

    public static final int MORFIN_AUTH_SUCCESS = 0;
    /**
     * Invalid Param
     */
    public static final int IMG_PROCESS_E_INVALIDPARAM = -1605;

    /**
     * failed to Init Device
     */
    public static final int FAILED_TO_INIT_DEVICE = -2005;
    /**
     * failed to stop capture
     */
    public static final int FAILED_TO_STOP_CAPTURE = -2012;
    /**
     * failed to Uninit Device
     */
    public static final int FAILED_TO_UINIT_DEVICE = -2013;
    /**
     * Invalid license key
     */
    public static final int INVLD_LIC_KEY = -2018;
    /**
     * Capture Timeout occurs
     */
    public static final int MORFIN_AUTH_CAPTURE_TIMEOUT = -2019;
    /**
     * failed to Memory allocation failed
     */
    public static final int FAILED_TO_ALLOC_MEM = -2020;

    /**
     * Device already Initialized
     */
    public static final int DEVICE_ALREADY_INITIALIZED = -2024;

    /**
     * Device Not Initialized
     */
    public static final int DEVICE_NOT_INITIALIZED = -2025;
    /**
     * object can not be null or empty
     */
    public static final int OBJECT_CANNOT_BE_NULL_OR_EMPTY = -2026;
    /**
     * failed to get template
     */
    public static final int FAILED_TO_GET_TEMPLATE = -2037;
    /**
     * Unsupported Image Format
     */
    public static final int UNSUPPORTED_IMAGE_FORMAT = -2040;
    /**
     * Unsupported template formate
     */
    public static final int UNSUPPORTED_TEMPLATE_FORMAT = -2041;
    /**
     * Invalid Template version
     */
    public static final int INVLD_TEMPLATE_VERSION = -2043;
    /**
     * NULL parameter provided
     */
    public static final int MORFIN_AUTH_E_NULL_PARAM = -2045;
    /**
     * Invalid Quality value pass
     */
    public static final int MORFIN_AUTH_E_QTY_OUT_OF_RANGE = -2047;
    /**
     * capture stop
     */
    public static final int CAPTURE_STOP = -2054;
    /**
     * Bad image captured
     */
    public static final int BAD_CAPTURE_IMAGE = -2055;
    /**
     * Invalid timeout
     */
    public static final int TIMEOUT_OUT_OF_RANGE = -2056;
    /**
     * capture already started
     */
    public static final int CAPTURE_ALREADY_STARTED = -2023;

    /**
     * Invalid Client key
     */
    public static final int INVALID_CLIENT_KEY = -3001;
    /**
     * Fail to generate client key
     */
    public static final int FAILED_TO_GENERATE_CLIENT_KEY = -3002;

    /**
     * Bluetooth permission not allowed
     */
    public static final int BLUETOOTH_PERMISSION_NOT_ALLOW = -4000;
    /**
     * Location permission not allowed
     */
    public static final int LOCATION_PERMISSION_NOT_ALLOW = -4001;
    /**
     * BLE not supported
     */
    public static final int BLE_NOT_SUPPORTED = -4002;
    /**
     * Bluetooth is disable
     */
    public static final int BLUETOOTH_DISABLE = -4003;
    /**
     * Location is disable
     */
    public static final int LOCATION_DISABLE = -4004;
    /**
     * Failed to connect
     */
    public static final int FAILED_TO_CONNECT = -4005;
    /**
     * Device not connected
     */
    public static final int DEVICE_NOT_CONNECTED = -4006;
    /**
     * Request timeout
     */
    public static final int REQUEST_TIMEOUT = -4007;
    /**
     * Invalid data received
     */
    public static final int INVALID_DATA_RECEIVED = -4008;
    /**
     * Failed to get battery condition
     */
    public static final int FAILED_TO_GET_BATTERY_CONDITION = -4009;
    /**
     * Invalid value pass
     */
    public static final int INVALID_VALUE_PASS = -4010;
    /**
     * Failed to set timer value
     */
    public static final int FAILED_TO_SET_TIMER_VALUE = -4011;
    /**
     * Failed to get timer value
     */
    public static final int FAILED_TO_GET_TIMER_VALUE = -4012;
    /**
     * Capture failed
     */
    public static final int CAPTURE_FAILED = -4013;
    /**
     * Failed to get image
     */
    public static final int FAILED_TO_GET_IMAGE = -4014;
    /**
     * Matching template fail
     */
    public static final int MATCH_TEMPLATE_FAIL = -4015;
    /**
     * Previous process is running
     */
    public static final int PREVIOUS_PROCESS_IS_RUNNING = -4097;
    /**
     * Internal error
     */
    public static final int INTERNAL_ERROR = -4098;
    /**
     * Unknown error
     */
    public static final int UNKNOWN_ERROR = -4099;

    /**
     * TEMPLATE_MATCHING_IN_PROGRESS
     */
    public static final int TEMPLATE_MATCHING_IN_PROGRESS = -4049;

}

package com.mantra.morfinauth;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.PHY_OPTION_NO_PREFERRED;
import static android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHORIZATION;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.content.Context.BLUETOOTH_SERVICE;

import static com.mantra.morfinauth.MorfinAuthBLE.BAD_CAPTURE_IMAGE;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_GENERATE_CLIENT_KEY;
import static com.mantra.morfinauth.MorfinAuthBLE.INVALID_CLIENT_KEY;
import static com.mantra.morfinauth.MorfinAuthBLE.INVLD_TEMPLATE_VERSION;
import static com.mantra.morfinauth.MorfinAuthBLE.MORFIN_AUTH_E_NULL_PARAM;
import static com.mantra.morfinauth.MorfinAuthBLE.TEMPLATE_MATCHING_IN_PROGRESS;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_GET_BATTERY_INFO;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_GET_BLE_MFG_DATA;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_GET_CHARGER_INFO;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_GET_FIRMWARE_MARC;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_GET_FIRMWARE_MELO;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_GET_TIMER_VALUE;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_MARC_DEVICE_INIT;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_MARC_MATCH_TEMPLATE;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_MARC_START_CAPTURE;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_MARC_STOP_CAPTURE;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_MELO_DEVICE_INIT;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_MELO_MATCH_TEMPLATE;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_MELO_START_CAPTURE;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_MELO_STOP_CAPTURE;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_READ_PACKET;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_SEND_CHALLENGE_KEY;
import static com.mantra.morfinauth.MorfinAuthBLENative.CommandType.COMMAND_SET_TIMER_VALUE;
import static com.mantra.morfinauth.ble.enums.CaptureFormat.ANSI_378;
import static com.mantra.morfinauth.ble.enums.CaptureFormat.FIR_2005;
import static com.mantra.morfinauth.ble.enums.CaptureFormat.FIR_2011;
import static com.mantra.morfinauth.ble.enums.CaptureFormat.FMR_2005;
import static com.mantra.morfinauth.ble.enums.CaptureFormat.FMR_2011;

import static com.mantra.morfinauth.MorfinAuthBLE.BLE_NOT_SUPPORTED;
import static com.mantra.morfinauth.MorfinAuthBLE.BLUETOOTH_DISABLE;
import static com.mantra.morfinauth.MorfinAuthBLE.BLUETOOTH_PERMISSION_NOT_ALLOW;
import static com.mantra.morfinauth.MorfinAuthBLE.CAPTURE_ALREADY_STARTED;
import static com.mantra.morfinauth.MorfinAuthBLE.CAPTURE_FAILED;
import static com.mantra.morfinauth.MorfinAuthBLE.CAPTURE_STOP;
import static com.mantra.morfinauth.MorfinAuthBLE.DEVICE_ALREADY_INITIALIZED;
import static com.mantra.morfinauth.MorfinAuthBLE.DEVICE_NOT_CONNECTED;
import static com.mantra.morfinauth.MorfinAuthBLE.DEVICE_NOT_INITIALIZED;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_ALLOC_MEM;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_CONNECT;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_GET_BATTERY_CONDITION;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_GET_IMAGE;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_GET_TEMPLATE;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_GET_TIMER_VALUE;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_INIT_DEVICE;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_SET_TIMER_VALUE;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_STOP_CAPTURE;
import static com.mantra.morfinauth.MorfinAuthBLE.FAILED_TO_UINIT_DEVICE;
import static com.mantra.morfinauth.MorfinAuthBLE.IMG_PROCESS_E_INVALIDPARAM;
import static com.mantra.morfinauth.MorfinAuthBLE.INTERNAL_ERROR;
import static com.mantra.morfinauth.MorfinAuthBLE.INVALID_DATA_RECEIVED;
import static com.mantra.morfinauth.MorfinAuthBLE.INVALID_VALUE_PASS;
import static com.mantra.morfinauth.MorfinAuthBLE.INVLD_LIC_KEY;
import static com.mantra.morfinauth.MorfinAuthBLE.LOCATION_DISABLE;
import static com.mantra.morfinauth.MorfinAuthBLE.LOCATION_PERMISSION_NOT_ALLOW;
import static com.mantra.morfinauth.MorfinAuthBLE.MATCH_TEMPLATE_FAIL;
import static com.mantra.morfinauth.MorfinAuthBLE.MORFIN_AUTH_CAPTURE_TIMEOUT;
import static com.mantra.morfinauth.MorfinAuthBLE.MORFIN_AUTH_E_QTY_OUT_OF_RANGE;
import static com.mantra.morfinauth.MorfinAuthBLE.MORFIN_AUTH_SUCCESS;
import static com.mantra.morfinauth.MorfinAuthBLE.OBJECT_CANNOT_BE_NULL_OR_EMPTY;
import static com.mantra.morfinauth.MorfinAuthBLE.PREVIOUS_PROCESS_IS_RUNNING;
import static com.mantra.morfinauth.MorfinAuthBLE.REQUEST_TIMEOUT;
import static com.mantra.morfinauth.MorfinAuthBLE.TIMEOUT_OUT_OF_RANGE;
import static com.mantra.morfinauth.MorfinAuthBLE.UNKNOWN_ERROR;
import static com.mantra.morfinauth.MorfinAuthBLE.UNSUPPORTED_IMAGE_FORMAT;
import static com.mantra.morfinauth.MorfinAuthBLE.UNSUPPORTED_TEMPLATE_FORMAT;



import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelUuid;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.mantra.morfinauth.ble.MorfinAuthBLE_Callback;
import com.mantra.morfinauth.ble.enums.CaptureFormat;

import com.mantra.morfinauth.ble.enums.MorfinBleState;
import com.mantra.morfinauth.ble.enums.MorfinNotifications;
import com.mantra.morfinauth.ble.model.BatteryInformation;
import com.mantra.morfinauth.ble.model.MFGData;
import com.mantra.morfinauth.ble.model.MorfinBleDevice;
import com.mantra.morfinauth.ble.model.TimerValues;
import com.mantra.morfinauth.enums.ImageFormat;
import com.mantra.morfinauth.enums.TemplateFormat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MorfinAuthBLENative {

    static {
        System.loadLibrary("MorfinAuth_BLE");
    }

    private String SDKVersion = "1.0";

    private Context context;
    private final BluetoothManager bleManager;
    private final BluetoothAdapter bleAdapter;
    private BluetoothLeScanner bleScanner;
    private MorfinAuthBLE_Callback morfinAuthCallback;
    private BluetoothGatt mBluetoothGatt;
    private CountDownLatch connectionLatch = null;
    private CountDownLatch lostPacketLatch = null;
    private CountDownLatch abortProcessLatch = null;
    private CountDownLatch mLatch = null;
    private CountDownLatch mBioLatch = null;
    private boolean isDeviceConnected = false;
    private MorfinBleDevice morfinBleDevice;
    private BluetoothGattService mBluetoothGattService;
    private boolean disconnectCall = false;
    private int ErrorCode;
    private int bioErrorCode;
    private List<String> notificationCharList;
    private byte[] receivedData;
    private byte[] receivedBioData;
    private int totalPacket;
    private int currentPacket;
    private int lastPacketNo;
    private ArrayList<Integer> packetLoss;
    private CommandType runningCommand = CommandType.COMMAND_NONE;
    private CommandType runningBioCommand = CommandType.COMMAND_NONE;
    private byte[][] raw;
    private byte[] receivedCheckSum;
    private MFGData mfgData;
    private byte[] captureTemplate;
    private byte[] captureImage;
    private CaptureFormat currentCaptureFormat;
    private String vID;
    private String pID;
    private String uniqueID;
    private DeviceInfo deviceInfo;
    private Handler waitTimer;
    private int TIME_TO_WAIT = 2000;
    private boolean deviceDisconnecting = false;

    private final String SERVICE_UUID = "90830001-f6ae-c2a8-2176-8b2831eadeee";
    private final String NOTIFICATION_UUID = "90830003-f6ae-c2a8-2176-8b2831eadeee";
    private final String WRITE_UUID = "90830002-f6ae-c2a8-2176-8b2831eadeee";
    private final String FINGERPRINT_MODULE_WRITE_UUID = "90830004-f6ae-c2a8-2176-8b2831eadeee";
    private final String FINGERPRINT_MODULE_NOTIFICATION_UUID = "90830005-f6ae-c2a8-2176-8b2831eadeee";
    private final byte CMD_PAY_LOAD_ZERO = (byte) 0x00;
    private int bufferCRC = 0;
    private ArrayList<MorfinBLDevice> discoverDevice = new ArrayList<>();
    private MorfinBLDevice connectedDevice;
    private final AtomicBoolean challengeKeyVerificationStarted = new AtomicBoolean(false);
    private class MorfinBLDevice {
        String Name;
        String MacAddress;
    }


    //======================Finger Print Module =========================
    private int FINGERPRINT_DEVICE_TIMEOUT = 5;

    protected MorfinAuthBLENative(Context context) {
        this.context = context;
        this.bleManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        this.bleAdapter = bleManager.getAdapter();
        this.bleScanner = bleAdapter.getBluetoothLeScanner();

    }


    protected void registerCallBacks(MorfinAuthBLE_Callback morfinAuthCallback) {
        this.morfinAuthCallback = morfinAuthCallback;
    }

    private boolean isDeviceSupportBLE() {
        if (this.context == null) {
            return false;
        }
        return this.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private int isBLEDeviceReady() {
        if (this.context == null) {
            return DEVICE_NOT_CONNECTED;
        }
        //Check location permission
        if (ContextCompat.checkSelfPermission(
                this.context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return LOCATION_PERMISSION_NOT_ALLOW;
        }

        //Check Bluetooth permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if ((ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) ||
                    (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)) {
                return BLUETOOTH_PERMISSION_NOT_ALLOW;
            }
        }

        //Check Location status
        LocationManager lm = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!lm.isLocationEnabled()) {
                return LOCATION_DISABLE;
            }
        } else {
            int mode = Settings.Secure.getInt(this.context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            if (mode == Settings.Secure.LOCATION_MODE_OFF) {
                return LOCATION_DISABLE;
            }

        }

        //Check Bluetooth is status
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            return BLUETOOTH_DISABLE;
        }

        return MORFIN_AUTH_SUCCESS;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected int discoverDevices() {
        if (this.context == null) {
            return -1;
        }

        if (!isDeviceSupportBLE()) {
            return BLE_NOT_SUPPORTED;
        }
        int bleReady = isBLEDeviceReady();

        if (bleReady != MORFIN_AUTH_SUCCESS) {
            return bleReady;
        }
        discoverDevice.clear();
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setServiceUuid(ParcelUuid.fromString(SERVICE_UUID));

        scanFilters.add(builder.build());

        ScanSettings.Builder settingBuilder = new ScanSettings.Builder();
        settingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setLegacy(false)
                .setReportDelay(0);

        if (bleAdapter != null && scanCallback != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                    return BLUETOOTH_PERMISSION_NOT_ALLOW;
                }
            }
            if (bleScanner == null) {
                bleScanner = bleAdapter.getBluetoothLeScanner();
            }
            bleScanner.startScan(scanFilters, settingBuilder.build(), scanCallback);
        }

        return MORFIN_AUTH_SUCCESS;
    }

    protected int stopDiscover() {
        if (this.context == null) {
            return -1;
        }
        if (bleAdapter != null && scanCallback != null && bleScanner != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                    return BLUETOOTH_PERMISSION_NOT_ALLOW;
                }
            }
            bleScanner.stopScan(scanCallback);
        }
        return MORFIN_AUTH_SUCCESS;

    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (!result.getDevice().getName().contains("MARC10") && !result.getDevice().getName().contains("MELO20")) {
                return;
            }

            for (MorfinBLDevice device: discoverDevice) {
                if (device.Name.equals(result.getDevice().getName())) {
                    return;
                }
            }

            MorfinBLDevice device = new MorfinBLDevice();
            device.Name = result.getDevice().getName();
            device.MacAddress = result.getDevice().getAddress();

            discoverDevice.add(device);

            MorfinBleDevice detectedDevice = new MorfinBleDevice();
            detectedDevice.name =  device.Name;
            detectedDevice.macAddress = device.MacAddress;


//            detectedDevice.rssi = result.getRssi();

            morfinAuthCallback.OnDeviceDiscovered(detectedDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    protected String GetSDKVersion() {
        return SDKVersion;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    protected int connectDevice(MorfinBleDevice morfinBleDevice) {
        if (bleAdapter == null) {
            return FAILED_TO_CONNECT;
        }
        if (connectionLatch != null) {
            while (connectionLatch.getCount() > 0) {
                connectionLatch.countDown();
            }
        }
        try {
            MorfinBLDevice connectDevice = null;
            this.deviceInfo = null;
            this.connectedDevice = null;
            this.morfinBleDevice = null;
            for (MorfinBLDevice device : discoverDevice) {
                if (device.MacAddress.equals(morfinBleDevice.macAddress)) {
                    connectDevice = device;
                    break;
                }
            }

            if(connectDevice == null){
                return FAILED_TO_CONNECT;
            }

            BluetoothDevice bluetoothDevice = bleAdapter.getRemoteDevice(connectDevice.MacAddress);

            if (bluetoothDevice == null) {
                return FAILED_TO_CONNECT;
            }
            int bleReady = isBLEDeviceReady();
            if (bleReady != MORFIN_AUTH_SUCCESS) {
                return bleReady;
            }

            if (this.context == null) {
                return UNKNOWN_ERROR;
            }
            connectionLatch = new CountDownLatch(1);
            mBluetoothGatt = bluetoothDevice.connectGatt(this.context, true, mGattCallBacks, 2);

            if (connectionLatch.await(20, TimeUnit.SECONDS)) {
                if (!isDeviceConnected) {
                    connectionLatch = null;
                    return FAILED_TO_CONNECT;
                } else {
                    return MORFIN_AUTH_SUCCESS;
                }
            } else {
                ErrorCode = REQUEST_TIMEOUT;
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                connectionLatch = null; // Reset the latch to allow future connections
                return FAILED_TO_CONNECT;
            }

        } catch (Exception ignore) {
            return FAILED_TO_CONNECT;
        }

    }

    @SuppressLint("MissingPermission")
    protected int DisConnect() {

        int bleReady = isBLEDeviceReady();
        if (bleReady != MORFIN_AUTH_SUCCESS) {
            return bleReady;
        }
        disconnectCall = true;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        morfinBleDevice = null;
        connectedDevice = null;
        deviceInfo = null;
        mfgData = null;
        discoverDevice.clear();
        // Release any thread that might be waiting in connectDevice()
        if (connectionLatch != null) {
            connectionLatch.countDown();
        }
        // Set the latch to null to allow a new connection attempt later
        connectionLatch = null;
        return MORFIN_AUTH_SUCCESS;
    }

    private final BluetoothGattCallback mGattCallBacks = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            if (status == GATT_SUCCESS) {
                if (challengeKeyVerificationStarted.compareAndSet(false, true)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            verifyChallengeKey();
                        }
                    }).start();
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @SuppressLint("MissingPermission")
        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            if (status == GATT_SUCCESS) {
                if (txPhy == BluetoothDevice.PHY_LE_1M && rxPhy == BluetoothDevice.PHY_LE_1M) {
                    gatt.setPreferredPhy(BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_LE_2M, PHY_OPTION_NO_PREFERRED);
                }else {
                    if (challengeKeyVerificationStarted.compareAndSet(false, true)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                verifyChallengeKey();
                            }
                        }).start();
                    }
                }
            } else {
                if (challengeKeyVerificationStarted.compareAndSet(false, true)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            verifyChallengeKey();
                        }
                    }).start();
                }

            }

        }

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
//             Log.v("BLE_AAR", "Device : " + status + " address ::" + gatt.getDevice().getAddress() + " newState :: " + newState);
            if (status == GATT_SUCCESS) {
                connectedDevice = new MorfinBLDevice();
                connectedDevice.Name = gatt.getDevice().getName();
                connectedDevice.MacAddress = gatt.getDevice().getAddress();

                morfinBleDevice = new MorfinBleDevice();
                morfinBleDevice.name = connectedDevice.Name ;
                morfinBleDevice.macAddress = connectedDevice.MacAddress ;

                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:


                        int bondstate = gatt.getDevice().getBondState();
                        if (bondstate == BOND_NONE || bondstate == BOND_BONDED) {
                            boolean ans = gatt.discoverServices();
                        }
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        if (morfinAuthCallback != null) {
                            morfinAuthCallback.OnDeviceConnectionStatus(morfinBleDevice, MorfinBleState.CONNECTING);
                        }
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        if (morfinAuthCallback != null) {
                            morfinAuthCallback.OnDeviceConnectionStatus(morfinBleDevice, MorfinBleState.DISCONNECTING);
                        }
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        challengeKeyVerificationStarted.set(false);
                        if (morfinAuthCallback != null) {
                            if (disconnectCall) {
                                isDeviceConnected = false;
                                gatt.close();
                                morfinAuthCallback.OnDeviceConnectionStatus(morfinBleDevice, MorfinBleState.DISCONNECTED);
                                disconnectCall = false;
                            }
                        }
                        break;
                }
            } else if (status == 19) {
                switch (newState) {
                    case BluetoothProfile.STATE_DISCONNECTED:
//                        Log.v("Jay", "device disconnecting :" + deviceDisconnecting);
                        challengeKeyVerificationStarted.set(false);
                        if (deviceDisconnecting) {
                            isDeviceConnected = false;
                            gatt.close();
                            morfinBleDevice = null;
                            connectedDevice = null;
                            deviceInfo = null;
                            deviceDisconnecting = false;
                            if (morfinAuthCallback != null) {
                                morfinAuthCallback.OnDeviceConnectionStatus(morfinBleDevice, MorfinBleState.DISCONNECTED);
                            }
                        }
                        break;
                }
            } else if (status == GATT_INSUFFICIENT_AUTHORIZATION) {
                switch (newState) {
                    case BluetoothProfile.STATE_DISCONNECTED:
                        challengeKeyVerificationStarted.set(false);
                        if (morfinAuthCallback != null) {
                            morfinAuthCallback.OnDeviceConnectionStatus(morfinBleDevice, MorfinBleState.OUT_OF_RANGE);

                        }
                        break;
                }
            }
        }

        @SuppressLint("MissingPermission")
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            switch (status) {
                case GATT_SUCCESS:
                    if (context == null) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    mBluetoothGatt = gatt;
                    try {
                        mBluetoothGattService = gatt.getService(ParcelUuid.fromString(SERVICE_UUID).getUuid());
                        notificationCharList = new ArrayList<>();
                        notificationCharList.add(NOTIFICATION_UUID);
                        notificationCharList.add(FINGERPRINT_MODULE_NOTIFICATION_UUID);
                        subscribeToCharacteristics(mBluetoothGatt);

                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                    try {
                        Thread.sleep(500);
                        gatt.requestMtu(512);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(500);
                        gatt.readPhy();
                        gatt.setPreferredPhy(BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_LE_2M, PHY_OPTION_NO_PREFERRED);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (characteristic.getUuid().toString().equals(NOTIFICATION_UUID)) {
                    handleResponse(characteristic.getValue());
                } else if (characteristic.getUuid().toString().equals(FINGERPRINT_MODULE_NOTIFICATION_UUID)) {
                    handleFingerDataResponse(characteristic.getValue());
                }
            }
        }

        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            super.onCharacteristicChanged(gatt, characteristic, value);
            if (characteristic.getUuid().toString().equals(NOTIFICATION_UUID)) {
                handleResponse(value);
            } else if (characteristic.getUuid().toString().equals(FINGERPRINT_MODULE_NOTIFICATION_UUID)) {
                handleFingerDataResponse(value);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
            super.onDescriptorRead(gatt, descriptor, status, value);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (!notificationCharList.isEmpty()) {
                notificationCharList.remove(0);
            }
            subscribeToCharacteristics(gatt);

        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
            super.onServiceChanged(gatt);
        }
    };

    @SuppressLint("MissingPermission")
    private void subscribeToCharacteristics(BluetoothGatt gatt) {
        if (notificationCharList.isEmpty()) {
            return;
        }
        try {


            BluetoothGattCharacteristic notificationChar = mBluetoothGattService.getCharacteristic(ParcelUuid.fromString(notificationCharList.get(0)).getUuid());


            if (notificationChar != null) {


                for (BluetoothGattDescriptor descriptor : notificationChar.getDescriptors()) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                }
                gatt.setCharacteristicNotification(notificationChar, true);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void verifyChallengeKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        try {
            Thread.sleep(1000);

            int ret = runCommand(CommandType.COMMAND_GET_CHALLENGE_KEY, null, 10);
            byte[] encryptKey = null;
            if (receivedData != null && ErrorCode != INVALID_DATA_RECEIVED) {


                if (verifyBLEResponse(CommandType.COMMAND_GET_CHALLENGE_KEY.getValue(), receivedData, null, false) == 0 && receivedData[2] == CMD_PAY_LOAD_ZERO) {
                    encryptKey = new byte[6];
                    byte[] recChecksum = new byte[1];
                    recChecksum[0] = receivedData[13];
                    System.arraycopy(receivedData, 7, encryptKey, 0, encryptKey.length);
                    if (verifyBLEResponse(CommandType.COMMAND_GET_CHALLENGE_KEY.getValue(), encryptKey, recChecksum, true) != 0) {
                        ret = FAILED_TO_CONNECT;
                    }

                } else {
                    ret = FAILED_TO_CONNECT;
                }
            } else {
                ret = FAILED_TO_CONNECT;
            }

            if (ret == MORFIN_AUTH_SUCCESS && encryptKey != null) {
                int[] nativeKey = new int[6];
                ret = getConnectionKey(encryptKey, connectedDevice.MacAddress, nativeKey);
                if (ret != MORFIN_AUTH_SUCCESS) {
                    return;
                }
                byte[] payload = new byte[6];
                for (int i = 0; i < 6; i++) {
                    byte b = (byte) nativeKey[i];
                    payload[i] = b;
                }


                ret = runCommand(COMMAND_SEND_CHALLENGE_KEY, payload, 10);

                if (receivedData != null && ErrorCode != INVALID_DATA_RECEIVED) {
                    if (verifyBLEResponse(COMMAND_SEND_CHALLENGE_KEY.getValue(), receivedData, null, false) == 0 && receivedData[2] == CMD_PAY_LOAD_ZERO) {
                        mfgData = new MFGData();

                        ret = getMfgData(mfgData);
                        if (ret != MORFIN_AUTH_SUCCESS) {

                            isDeviceConnected = false;
                            mBluetoothGatt.disconnect();
                            mBluetoothGatt.close();
                            if (connectionLatch != null) {
                                connectionLatch.countDown();
                            }
                        }else{
                            int model = -1;
                            if(connectedDevice.Name.contains("MARC10")){
                                model = 0;
                            }else if(connectedDevice.Name.contains("MELO20")){
                                model = 1;
                            }
                            int verifyFW = verifyFW(mfgData.firmwareVer,model,false);
                            if(verifyFW != MORFIN_AUTH_SUCCESS){
                                isDeviceConnected = false;
                                mBluetoothGatt.disconnect();
                                mBluetoothGatt.close();
                                if (connectionLatch != null) {
                                    connectionLatch.countDown();
                                }
                                ret =  FAILED_TO_CONNECT;
                            }

                            if(deviceInfo!= null){
                                this.deviceInfo = null;
                                DeviceInfo dI = new DeviceInfo();
                                InitDevice(null, dI);
                            }
                        }
                    } else {
                        ret = FAILED_TO_CONNECT;
                    }
                } else {
                    ret = FAILED_TO_CONNECT;
                }
            } else {
                isDeviceConnected = false;
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                if (connectionLatch != null) {
                    connectionLatch.countDown();
                }
            }
            if (ret == MORFIN_AUTH_SUCCESS) {
                if (morfinAuthCallback != null) {
                    isDeviceConnected = true;
                    if (connectionLatch != null) {
                        connectionLatch.countDown();
                    }
                    morfinAuthCallback.OnDeviceConnectionStatus(morfinBleDevice, MorfinBleState.CONNECTED);
                }
            } else {
                isDeviceConnected = false;
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                if (connectionLatch != null) {
                    connectionLatch.countDown();
                }
            }
        } catch (Exception ignore) {

        }
    }


    private void handleResponse(byte[] value) {
        if (value == null) {
            ErrorCode = INVALID_DATA_RECEIVED;
            mLatch.countDown();
            return;
        }
        /*StringBuilder hex = new StringBuilder();
        for (byte i : value) {
            hex.append(String.format("%02X ", i));
        }
        Log.v("Jay", "Final Response :" + hex);*/
        if (verifyBLEResponse(CommandType.COMMAND_DISCONNECT_NOTIFICATION.getValue(), value, null, false) == 0) {
            switch (value[7]) {
                case 0x01:
//                    Log.v("Jay", "device disconnecting :" + deviceDisconnecting);
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.DeviceDisconnecting);
                    deviceDisconnecting = true;
                    return;
                case 0x02:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.DeviceUninitialized);
                    unInitDevice();
                    return;
            }

        }

        if (verifyBLEResponse(CommandType.COMMAND_FAULT_NOTIFICATION.getValue(), value, null, false) == 0 && value.length == 10) {
            switch (Utils.getIntFrombyte(value[7], value[8], false)) {
                case 0:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.NoFaultFound);
                    break;
                case 1:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.BatteryDisconnected);
                    break;
                case 2:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.BatteryTemperatureAbnormal);
                    break;
                case 3:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.ChargingInputFault);
                    break;
                case 4:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.OTGFault);
                    break;
                case 5:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.ChargingTimerExpiration);
                    break;
                case 6:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.BatteryOverPower);
                    break;
                case 7:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.Charging);
                    break;
                case 8:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.Discharging);
                    break;
                case 9:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.ChargingRequiredWarning);
                    break;
                case 16:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.BatteryPowerIsCriticalLow);
                    break;
                case 17:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.ChargerPlugged);
                    break;
                case 18:
                    morfinAuthCallback.MorfinDeviceStatusNotification(MorfinNotifications.ChargerUnplugged);
                    break;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendFaultNotificationACK();
                }
            }).start();

            return;
        }


        if (verifyBLEResponse(runningCommand.getValue(), value, null, false) == 0) {
            ErrorCode = MORFIN_AUTH_SUCCESS;
            receivedData = new byte[value.length];
            System.arraycopy(value, 0, receivedData, 0, value.length);
            mLatch.countDown();
        }

    }

    private void handleFingerDataResponse(byte[] data) {
        if (data == null) {
            bioErrorCode = INVALID_DATA_RECEIVED;
            if (runningBioCommand == COMMAND_READ_PACKET) {
                lostPacketLatch.countDown();
            } else if (runningBioCommand == COMMAND_MELO_STOP_CAPTURE || runningBioCommand == COMMAND_MARC_STOP_CAPTURE) {
                abortProcessLatch.countDown();
            } else {
                mBioLatch.countDown();
            }
        }


        /*StringBuilder hex = new StringBuilder();
        for (byte i : data) {
            hex.append(String.format("%02X ", i));
        }
        Log.v("Jay", "Final Response :" + hex);*/

        if (runningBioCommand == COMMAND_MELO_DEVICE_INIT || runningBioCommand == COMMAND_MELO_START_CAPTURE
                || runningBioCommand == COMMAND_READ_PACKET || runningBioCommand == COMMAND_MARC_DEVICE_INIT
                || runningBioCommand == COMMAND_MARC_START_CAPTURE || runningBioCommand == COMMAND_MARC_STOP_CAPTURE
                || runningBioCommand == COMMAND_MELO_STOP_CAPTURE || runningBioCommand == COMMAND_MELO_MATCH_TEMPLATE
                || runningBioCommand == COMMAND_MARC_MATCH_TEMPLATE || runningBioCommand == COMMAND_GET_FIRMWARE_MELO
                || runningBioCommand == COMMAND_GET_FIRMWARE_MARC) {


            try {

                totalPacket = Utils.getIntFrombyte(data[0], data[1], false);
                currentPacket = Utils.getIntFrombyte(data[2], data[3], false);

                if (runningBioCommand == COMMAND_MELO_STOP_CAPTURE || runningBioCommand == COMMAND_MARC_STOP_CAPTURE) {
                    raw = null;
                    packetLoss = null;
                    receivedBioData = null;
                    if (verifyResponse(COMMAND_MARC_STOP_CAPTURE.getValue(), data, null, false) != 0 &&
                            verifyResponse(COMMAND_MELO_STOP_CAPTURE.getValue(), data, null, false) != 0) {
                        return;
                    }

                }

                if (totalPacket > 1) {//Capture response with FIR response

                    if (runningBioCommand != COMMAND_MELO_START_CAPTURE && runningBioCommand != COMMAND_MARC_START_CAPTURE) {
                        return;
                    }


                    if (currentPacket > totalPacket) {
                        bioErrorCode = INVALID_DATA_RECEIVED;
                        mBioLatch.countDown();
                        return;
                    }

                    //packet wait logic
                    HandlerThread handlerThread = new HandlerThread("PacketWaitThread");
                    handlerThread.start();
                    if (currentPacket == 1) {
                        waitTimer = new Handler(handlerThread.getLooper());
                        startWaitTimer();
                    } else if (currentPacket == totalPacket) {
                        stopWaitTimer();
                    } else {
                        restartWaitTimer();
                    }


                    if (lastPacketNo != (currentPacket - 1)) { //Calculate Packet loss
                        while (lastPacketNo != (currentPacket - 1)) {
                            packetLoss.add(lastPacketNo + 1);
                            lastPacketNo++;
                        }
                    }

                    if (currentPacket == 1) {
                        if (verifyResponse(runningBioCommand.getValue(), data, null, false) == 0) { //First packet add same as it is
                            try {

                                raw = new byte[totalPacket][];
                                bioErrorCode = MORFIN_AUTH_SUCCESS;
                                receivedBioData = new byte[data.length];
                                System.arraycopy(data, 0, receivedBioData, 0, data.length);
                                raw[currentPacket - 1] = new byte[data.length - 11];
                                System.arraycopy(data, 11, raw[currentPacket - 1], 0, data.length - 11);


                            } catch (Exception e) {
                                bioErrorCode = UNKNOWN_ERROR;
                                if (mBioLatch != null) {
                                    mBioLatch.countDown();
                                }
                                e.printStackTrace();
                            }

                        } else {
                            bioErrorCode = INVALID_DATA_RECEIVED;
                            mBioLatch.countDown();
                            return;
                        }

                    } else if (currentPacket == totalPacket) { //Last packet logic
                        if (receivedBioData == null) {
                            bioErrorCode = INVALID_DATA_RECEIVED;
                            mBioLatch.countDown();
                            return;
                        }
                        try {

                            if ((data.length - 8) > 0) {

                                raw[currentPacket - 1] = new byte[data.length - 8];
                                System.arraycopy(data, 4, raw[currentPacket - 1], 0, data.length - 8);

                                //Copy checksum
                                receivedCheckSum = new byte[4];
                                System.arraycopy(data, data.length - 4, receivedCheckSum, 0, receivedCheckSum.length);
                            } else if (data.length == 8) {
                                //Copy checksum
                                receivedCheckSum = new byte[4];
                                System.arraycopy(data, data.length - 4, receivedCheckSum, 0, receivedCheckSum.length);
                            } else {

                                bufferCRC = 8 - data.length;


                                byte[] temp = new byte[raw[currentPacket - 2].length];
                                System.arraycopy(raw[currentPacket - 2], 0, temp, 0, raw[currentPacket - 2].length);


                                receivedCheckSum = new byte[4];
                                //copy buffer CRC to received Checksum
                                System.arraycopy(temp, temp.length - bufferCRC, receivedCheckSum, 0, bufferCRC);
                                System.arraycopy(data, data.length - 4, receivedCheckSum, bufferCRC, 4 - bufferCRC);


                                //remove bufferCRC bytes from raw data
                                raw[currentPacket - 2] = new byte[temp.length - bufferCRC];
                                System.arraycopy(temp, 0, raw[currentPacket - 2], 0, temp.length - bufferCRC);


                            }

                            runningBioCommand = CommandType.COMMAND_NONE;


                        } catch (Exception e) {
                            bioErrorCode = UNKNOWN_ERROR;
                            runningBioCommand = CommandType.COMMAND_NONE;
                            if (mBioLatch != null) {
                                mBioLatch.countDown();
                            }
                            e.printStackTrace();
                        }


                    } else { //Other packets rather than last or first
                        if (receivedBioData == null) {
                            bioErrorCode = INVALID_DATA_RECEIVED;
                            mBioLatch.countDown();
                            return;
                        }

                        try {

                            raw[currentPacket - 1] = new byte[data.length - 4];
                            System.arraycopy(data, 4, raw[currentPacket - 1], 0, data.length - 4);

                        } catch (Exception e) {
                            bioErrorCode = UNKNOWN_ERROR;
                            if (mBioLatch != null) {
                                mBioLatch.countDown();
                            }
                            e.printStackTrace();
                        }

                    }
                    lastPacketNo++;
                } else { //Init response || FMR response || Read packet data

                    if (runningBioCommand == COMMAND_READ_PACKET) {
                        try {
                            bioErrorCode = MORFIN_AUTH_SUCCESS;
                            int receivedPacket = Utils.getIntFrombyte(data[2], data[3], false);
//                            StringBuilder hex1 = new StringBuilder();
//                            for (byte i : data) {
//                                hex1.append(String.format("%02X ", i));
//                            }
                            if (receivedPacket == lastPacketNo) {
                                if ((data.length - 8) > 0) {
                                    raw[receivedPacket - 1] = new byte[data.length - 8];
                                    System.arraycopy(data, 4, raw[receivedPacket - 1], 0, data.length - 8);
                                    //Copy checksum
                                    receivedCheckSum = new byte[4];
                                    System.arraycopy(data, (data.length - 4), receivedCheckSum, 0, receivedCheckSum.length);
                                } else if (data.length == 8) {
                                    //Copy checksum
                                    receivedCheckSum = new byte[4];
                                    System.arraycopy(data, data.length - 4, receivedCheckSum, 0, receivedCheckSum.length);
                                } else {

                                    bufferCRC = 8 - data.length;


                                    byte[] temp = new byte[raw[currentPacket - 2].length];
                                    System.arraycopy(raw[currentPacket - 2], 0, temp, 0, raw[currentPacket - 2].length);


                                    receivedCheckSum = new byte[4];
                                    //copy buffer CRC to received Checksum
                                    System.arraycopy(temp, temp.length - bufferCRC, receivedCheckSum, 0, bufferCRC);
                                    System.arraycopy(data, data.length - 4, receivedCheckSum, bufferCRC, 4 - bufferCRC);


                                    //remove bufferCRC bytes from raw data
                                    raw[currentPacket - 2] = new byte[temp.length - bufferCRC];
                                    System.arraycopy(temp, 0, raw[currentPacket - 2], 0, temp.length - bufferCRC);

                                }

                            } else {
                                raw[receivedPacket - 1] = new byte[data.length - 4];
                                System.arraycopy(data, 4, raw[receivedPacket - 1], 0, data.length - 4);
                            }
                            lostPacketLatch.countDown();
                        } catch (Exception e) {
                            bioErrorCode = UNKNOWN_ERROR;
                            if (lostPacketLatch != null) {
                                lostPacketLatch.countDown();
                            }
                            e.printStackTrace();
                        }

                    } else if (runningBioCommand == COMMAND_MARC_STOP_CAPTURE || runningBioCommand == COMMAND_MELO_STOP_CAPTURE) {
                        bioErrorCode = MORFIN_AUTH_SUCCESS;
                        abortProcessLatch.countDown();
                    } else if (verifyResponse(runningBioCommand.getValue(), data, null, false) == 0) {
                        bioErrorCode = MORFIN_AUTH_SUCCESS;
                        receivedBioData = new byte[data.length];
                        System.arraycopy(data, 0, receivedBioData, 0, data.length);
                    }

                }
                if (totalPacket == currentPacket && mBioLatch != null) {
                    mBioLatch.countDown();
                }
            } catch (Exception e) {

                bioErrorCode = UNKNOWN_ERROR;
                if (mBioLatch != null) {
                    mBioLatch.countDown();
                }
                e.printStackTrace();
            }
        }/*else{
            SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH);
            Calendar calendar = Calendar.getInstance();
            String dateTime = df.format(calendar.getTime());
//            Utils.DataLog(context,Arrays.toString(data),"GarbageData"+dateTime,".txt");
        }*/
    }

    @SuppressLint("MissingPermission")
    private void sendFaultNotificationACK() {
        if (!isDeviceConnected) {
            return;
        }


        BluetoothGattCharacteristic mWriteCharacteristic = mBluetoothGattService.getCharacteristic(ParcelUuid.fromString(WRITE_UUID).getUuid());

        byte[] payloadData = new byte[1];
        payloadData[0] = 0x01;

        byte[] Command = new byte[504];
        int[] size = new int[1];
        byte[] finalCommand = null;

        int nat = generateBLECommand(CommandType.COMMAND_FAULT_NOTIFICATION.getValue(), payloadData, Command, size);


        if (nat == 0) {
            finalCommand = new byte[size[0]];

            System.arraycopy(Command, 0, finalCommand, 0, size[0]);

        } else {
            return;
        }


        mWriteCharacteristic.setValue(finalCommand);
        mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        if (mWriteCharacteristic != null && finalCommand != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                int result = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic, finalCommand, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            } else {
                boolean result = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private int runCommand(CommandType command, byte[] payload, int timeout) {


        int bleReady = isBLEDeviceReady();
        if (bleReady != MORFIN_AUTH_SUCCESS) {
            return bleReady;
        }
        if (mLatch != null) {
            while (mLatch.getCount() > 0) {
                mLatch.countDown();
            }
        }

        if (mBluetoothGattService == null && mBluetoothGatt == null) {
            return DEVICE_NOT_CONNECTED;
//            mBluetoothGattService = mBluetoothGatt.getService(ParcelUuid.fromString(SERVICE_UUID).getUuid());

        }
        try {

            BluetoothGattCharacteristic mWriteCharacteristic = null;
            mWriteCharacteristic = mBluetoothGattService.getCharacteristic(ParcelUuid.fromString(WRITE_UUID).getUuid());
            ByteBuffer buildCommand = null;

            mLatch = new CountDownLatch(1);


            switch (command) {
                case COMMAND_GET_CHALLENGE_KEY:
                    runningCommand = CommandType.COMMAND_GET_CHALLENGE_KEY;
                    break;
                case COMMAND_SEND_CHALLENGE_KEY:
                    runningCommand = COMMAND_SEND_CHALLENGE_KEY;
                    break;
                case COMMAND_GET_BATTERY_INFO:
                    runningCommand = COMMAND_GET_BATTERY_INFO;
                    break;
                case COMMAND_GET_CHARGER_INFO:
                    runningCommand = COMMAND_GET_CHARGER_INFO;
                    break;
                case COMMAND_SET_TIMER_VALUE:
                    runningCommand = COMMAND_SET_TIMER_VALUE;
//                    output.write(SET_TIMER_VALUES);
                    break;
                case COMMAND_GET_TIMER_VALUE:
                    runningCommand = COMMAND_GET_TIMER_VALUE;
                    break;
                case COMMAND_GET_BLE_MFG_DATA:
                    runningCommand = COMMAND_GET_BLE_MFG_DATA;
                    break;
                default:
                    return INVALID_VALUE_PASS;
            }

            byte[] Command = new byte[504];
            int[] size = new int[1];
            byte[] finalCommand = null;

            int nat = generateBLECommand(command.getValue(), payload, Command, size);

            if (nat == 0) {
                finalCommand = new byte[size[0]];

                System.arraycopy(Command, 0, finalCommand, 0, size[0]);
                //Todo: Comment on relese
                /*StringBuilder hex = new StringBuilder();
                for (byte i : finalCommand) {
                    hex.append(String.format("%02X ", i));
                }
                Log.v("Jay", "final Command :" + hex);*/
            } else {
                return INTERNAL_ERROR;
            }


            if (mWriteCharacteristic != null && finalCommand != null) {

                mWriteCharacteristic.setValue(finalCommand);
                mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    int result = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic, finalCommand, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                } else {
                    boolean result = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Return
        try {
            if (mLatch.await(timeout, TimeUnit.SECONDS)) {
                return ErrorCode;
            } else {
                ErrorCode = REQUEST_TIMEOUT;
                return ErrorCode;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    //=============================FAPDevice================================

    @SuppressLint("MissingPermission")
    private int runBiometricDeviceCommand(CommandType command, byte[] payload, int totalPacketsNo, int currentPacketNo,int captureTimeout) {

        int bleReady = isBLEDeviceReady();

        if (!isDeviceConnected) {
            return DEVICE_NOT_CONNECTED;
        }

        if (bleReady != MORFIN_AUTH_SUCCESS) {
            return bleReady;
        }
        if (command == COMMAND_READ_PACKET) {
            if (runningBioCommand == COMMAND_MELO_STOP_CAPTURE || runningBioCommand == COMMAND_MARC_STOP_CAPTURE) {
                bioErrorCode = CAPTURE_STOP;
                if (lostPacketLatch != null) {
                    lostPacketLatch.countDown();
                }
                return bioErrorCode;
            }
            if (lostPacketLatch != null) {
                while (lostPacketLatch.getCount() > 0) {
                    lostPacketLatch.countDown();
                }
            }

        } else if (command == COMMAND_MARC_STOP_CAPTURE || command == COMMAND_MELO_STOP_CAPTURE) {

            if (runningBioCommand == COMMAND_MARC_START_CAPTURE || runningBioCommand == COMMAND_MELO_START_CAPTURE
                    || runningBioCommand == COMMAND_MARC_MATCH_TEMPLATE || runningBioCommand == COMMAND_MELO_MATCH_TEMPLATE) {


                bioErrorCode = CAPTURE_STOP;
                if (mBioLatch != null) {
                    mBioLatch.countDown();
                }
            }

            if (abortProcessLatch != null) {
                while (abortProcessLatch.getCount() > 0) {
                    abortProcessLatch.countDown();
                }
            }
        } else {
            if (mBioLatch != null) {
                while (mBioLatch.getCount() > 0) {

                    if (mBioLatch != null) {
                        while (mBioLatch.getCount() > 0) {
                            if (command == COMMAND_MARC_START_CAPTURE || command == COMMAND_MELO_START_CAPTURE) {
                                if (runningBioCommand == COMMAND_MARC_START_CAPTURE || runningBioCommand == COMMAND_MELO_START_CAPTURE
                                        || runningBioCommand == COMMAND_MARC_MATCH_TEMPLATE || runningBioCommand == COMMAND_MELO_MATCH_TEMPLATE) {
                                    bioErrorCode = CAPTURE_ALREADY_STARTED;
                                    return bioErrorCode;
                                } else {
                                    bioErrorCode = PREVIOUS_PROCESS_IS_RUNNING;
                                    return bioErrorCode;
                                }/*else{
                            mLatch.countDown();
                        }*/

                            } else {
                                bioErrorCode = PREVIOUS_PROCESS_IS_RUNNING;
                                return bioErrorCode;
                            }
                        }
                    }
                }
            }
        }

        if (mBluetoothGattService == null && mBluetoothGatt == null) {
            return -1;
//            mBluetoothGattService = mBluetoothGatt.getService(ParcelUuid.fromString(Const.SERVICE_UUID).getUuid());

        }
        try {


            BluetoothGattCharacteristic mWriteCharacteristic = null;
            mWriteCharacteristic = Objects.requireNonNull(mBluetoothGattService).getCharacteristic(ParcelUuid.fromString(FINGERPRINT_MODULE_WRITE_UUID).getUuid());

            if (command == COMMAND_READ_PACKET) {
                lostPacketLatch = new CountDownLatch(1);
            } else if (command == COMMAND_MARC_STOP_CAPTURE || command == COMMAND_MELO_STOP_CAPTURE) {
                abortProcessLatch = new CountDownLatch(1);
            } else {
                mBioLatch = new CountDownLatch(1);
            }


            /*byte[] total = Utils.getByteArrayFromInt(totalPacketsNo);
            byte[] current = Utils.getByteArrayFromInt(currentPacketNo);*/

            /*output.write(total, 0, total.length);//Total number of package
            output.write(current, 0, current.length);//Current Package*/


            switch (command) {
                case COMMAND_MELO_DEVICE_INIT:
                    runningBioCommand = COMMAND_MELO_DEVICE_INIT;
                    FINGERPRINT_DEVICE_TIMEOUT = 5 + captureTimeout;
                    break;
                case COMMAND_MARC_DEVICE_INIT:
                    runningBioCommand = COMMAND_MARC_DEVICE_INIT;
                    FINGERPRINT_DEVICE_TIMEOUT = 5 + captureTimeout;
                    break;
                case COMMAND_MELO_START_CAPTURE:
                    runningBioCommand = COMMAND_MELO_START_CAPTURE;
                    FINGERPRINT_DEVICE_TIMEOUT = 25 + captureTimeout;
                    break;
                case COMMAND_MARC_START_CAPTURE:
                    runningBioCommand = COMMAND_MARC_START_CAPTURE;
                    FINGERPRINT_DEVICE_TIMEOUT = 25 + captureTimeout;
                    break;
                case COMMAND_MELO_MATCH_TEMPLATE:
                    runningBioCommand = COMMAND_MELO_MATCH_TEMPLATE;
                    FINGERPRINT_DEVICE_TIMEOUT = 10 + captureTimeout;
                    break;
                case COMMAND_READ_PACKET:
                    runningBioCommand = COMMAND_READ_PACKET;
                    FINGERPRINT_DEVICE_TIMEOUT = 5 + captureTimeout;
                    break;
                case COMMAND_MELO_STOP_CAPTURE:
                    runningBioCommand = COMMAND_MELO_STOP_CAPTURE;
                    FINGERPRINT_DEVICE_TIMEOUT = 5 + captureTimeout;
                    break;
                case COMMAND_MARC_STOP_CAPTURE:
                    runningBioCommand = COMMAND_MARC_STOP_CAPTURE;
                    FINGERPRINT_DEVICE_TIMEOUT = 5 + captureTimeout;
                    break;
                case COMMAND_MARC_MATCH_TEMPLATE:
                    runningBioCommand = COMMAND_MARC_MATCH_TEMPLATE;
                    FINGERPRINT_DEVICE_TIMEOUT = 10 + captureTimeout;
                    break;
                case COMMAND_GET_FIRMWARE_MELO:
                    runningBioCommand = COMMAND_GET_FIRMWARE_MELO;
                    FINGERPRINT_DEVICE_TIMEOUT = 5 + captureTimeout;
                    break;
                case COMMAND_GET_FIRMWARE_MARC:
                    runningBioCommand = COMMAND_GET_FIRMWARE_MARC;
                    FINGERPRINT_DEVICE_TIMEOUT = 5 + captureTimeout;
                    break;


            }



            byte[] Command = new byte[504];
            int[] size = new int[1];
            byte[] finalCommand = null;

            int nat = generateCommand(command.getValue(), payload, Command, size);

            if (nat == 0) {
                finalCommand = new byte[size[0]];

                System.arraycopy(Command, 0, finalCommand, 0, size[0]);
                // Todo : Commit befer relese
                /*StringBuilder hex = new StringBuilder();
                for (byte i : finalCommand) {
                    hex.append(String.format("%02X ", i));
                }
                Log.v("Jay", "final Command :" + hex);*/
            } else {
                return INTERNAL_ERROR;
            }


            mWriteCharacteristic.setValue(finalCommand);
            mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);


            if (mWriteCharacteristic != null && finalCommand != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    int result = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic, finalCommand, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                } else {
                    boolean result = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Return
        try {
            if (command == COMMAND_READ_PACKET) {
                if (lostPacketLatch.await(FINGERPRINT_DEVICE_TIMEOUT, TimeUnit.SECONDS)) {
                    return bioErrorCode;
                } else {
                    bioErrorCode = REQUEST_TIMEOUT;
                    runningBioCommand = CommandType.COMMAND_NONE;
                    return bioErrorCode;
                }
            } else if (command == COMMAND_MARC_STOP_CAPTURE || command == COMMAND_MELO_STOP_CAPTURE) {
                if (abortProcessLatch.await(FINGERPRINT_DEVICE_TIMEOUT, TimeUnit.SECONDS)) {
                    return bioErrorCode;
                } else {
                    bioErrorCode = MORFIN_AUTH_SUCCESS;
                    return bioErrorCode;
                }
            } else {
                if (mBioLatch.await(FINGERPRINT_DEVICE_TIMEOUT, TimeUnit.SECONDS)) {
                    return bioErrorCode;
                } else {
                    if (lastPacketNo != totalPacket) {
                        while (lastPacketNo != totalPacket) {
                            if (packetLoss != null) {
                                packetLoss.add(lastPacketNo + 1);
                            }
                            lastPacketNo++;
                        }

                        bioErrorCode = MORFIN_AUTH_SUCCESS;
                        return bioErrorCode;
                    }
                    bioErrorCode = REQUEST_TIMEOUT;
                    runningBioCommand = CommandType.COMMAND_NONE;
                    if (mBioLatch != null) {
                        mBioLatch = null;
                    }
                    return bioErrorCode;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    //===========================BLE Commands==============================


    protected int getBatteryInformation(BatteryInformation batteryInformation) {
        if (batteryInformation == null) {
            return INVALID_VALUE_PASS;
        }
        int ret = runCommand(COMMAND_GET_BATTERY_INFO, null, 2);

        if (receivedData != null && ErrorCode != INVALID_DATA_RECEIVED && ret == 0) {
            if (verifyBLEResponse(COMMAND_GET_BATTERY_INFO.getValue(), receivedData, null, false) == 0
                    && receivedData[2] == CMD_PAY_LOAD_ZERO) {
                batteryInformation.batteryTemperature = (Utils.getIntFrombyte(receivedData[7], receivedData[8], true) / 100);
                batteryInformation.batteryChargePercentage = Utils.getIntFrombyte(receivedData[15], receivedData[16], false);
                batteryInformation.batteryHealthPercentage = Utils.getIntFrombyte(receivedData[17], receivedData[18], false);

                ret = runCommand(CommandType.COMMAND_GET_CHARGER_INFO, null, 2);

                if(receivedData != null && ErrorCode != INVALID_DATA_RECEIVED && ret == 0){
                    if (verifyBLEResponse(CommandType.COMMAND_GET_CHARGER_INFO.getValue(), receivedData, null, false) == 0 && receivedData[2] == CMD_PAY_LOAD_ZERO) {
                        if (receivedData[9] == 0x00) {
                            batteryInformation.chargerConnected = 0;
                        } else {
                            batteryInformation.chargerConnected = 1;
                        }
                    }else {
                        ret = FAILED_TO_GET_BATTERY_CONDITION;
                    }
                }else{
                    ret = FAILED_TO_GET_BATTERY_CONDITION;
                }


            } else {
                ret = FAILED_TO_GET_BATTERY_CONDITION;
            }
        } else {
            ret = FAILED_TO_GET_BATTERY_CONDITION;
        }
        return ret;
    }

    protected int setDeviceTimerValues(TimerValues timerValues) {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        if (timerValues != null) {
            if (timerValues.sleepMode < 10 || timerValues.sleepMode > 240) {
                return INVALID_VALUE_PASS;
            }
            payload.write(timerValues.sleepMode);

            if (timerValues.offMode < 10 || timerValues.offMode > 240) {
                return INVALID_VALUE_PASS;
            }
            payload.write(timerValues.offMode);

            if (timerValues.advertisement < 10 || timerValues.advertisement > 240) {
                return INVALID_VALUE_PASS;
            }
            payload.write(timerValues.advertisement);


        }
        ByteBuffer payloadArray = ByteBuffer.allocate(payload.size());
        payloadArray.put(payload.toByteArray());

        int ret = runCommand(COMMAND_SET_TIMER_VALUE, payloadArray.array(), 2);
        if (receivedData != null && ErrorCode != INVALID_DATA_RECEIVED) {
            if (verifyBLEResponse(COMMAND_SET_TIMER_VALUE.getValue(), receivedData, null, false) == 0 && receivedData[2] == CMD_PAY_LOAD_ZERO) {
                return ret;
            } else {
                ret = FAILED_TO_SET_TIMER_VALUE;
            }
        } else {
            ret = FAILED_TO_SET_TIMER_VALUE;
        }
        return ret;
    }

    protected int getDeviceTimerValues(TimerValues timerValues) {
        if (timerValues == null) {
            return INVALID_VALUE_PASS;
        }

        int ret = runCommand(COMMAND_GET_TIMER_VALUE, null, 2);
        if (receivedData != null && ErrorCode != INVALID_DATA_RECEIVED) {
            if (verifyBLEResponse(COMMAND_GET_TIMER_VALUE.getValue(), receivedData, null, false) == 0 && receivedData[2] == CMD_PAY_LOAD_ZERO) {

                timerValues.sleepMode = (int) receivedData[7];
                timerValues.offMode = (int) receivedData[8];
                timerValues.advertisement = (int) receivedData[9];
                return ret;
            } else {
                ret = FAILED_TO_GET_TIMER_VALUE;
            }
        } else {
            ret = FAILED_TO_GET_TIMER_VALUE;
        }
        return ret;
    }

    protected int getSupportedDeviceList(DeviceList[] deviceLists, int[] deviceListSize) {

        try {
            if (deviceListSize == null) {
                return INVALID_VALUE_PASS;
            }

            deviceListSize[0] = 2;

            if (deviceLists != null && deviceLists.length == deviceListSize[0]) {
                for (int i = 0; i < deviceLists.length; i++) {
                    if (i == 0) {
                        deviceLists[i] = new DeviceList();
                        deviceLists[i].Model = "MARC10BL";
                    } else if (i == 1) {
                        deviceLists[i] = new DeviceList();
                        deviceLists[i].Model = "MELO20BL";
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return MORFIN_AUTH_SUCCESS;
    }

    @SuppressLint("MissingPermission")
    protected boolean isDeviceConnected(MorfinBleDevice bluetoothDevice) {

        int bleReady = isBLEDeviceReady();
        if (bleReady != MORFIN_AUTH_SUCCESS) {
            return false;
        }

        if (bleManager == null) return false;

        List<BluetoothDevice> connectedDevices =
                bleManager.getConnectedDevices(BluetoothProfile.GATT);

        for (BluetoothDevice device : connectedDevices) {
            if (device.getAddress().equals(bluetoothDevice.macAddress)) {
                return true; // Device is connected
            }
        }

        return false; // Device not connected
    }

    private int getMfgData(MFGData mfgData) {
        try {
            int ret = runCommand(COMMAND_GET_BLE_MFG_DATA, null, 10);
            if (receivedData != null && ErrorCode != INVALID_DATA_RECEIVED) {
                if (verifyBLEResponse(COMMAND_GET_BLE_MFG_DATA.getValue(), receivedData, null, false) == 0 && receivedData[2] == CMD_PAY_LOAD_ZERO) {

                    byte[] receivedMFG = new byte[128];
                    System.arraycopy(receivedData, 7, receivedMFG, 0, receivedMFG.length);

                    byte[] receivedCRC = new byte[4];
                    System.arraycopy(receivedMFG, 12, receivedCRC, 0, receivedCRC.length);
                    byte[] dataForCRC = new byte[110]; //change 110 after fapFW
                    System.arraycopy(receivedMFG, 18, dataForCRC, 0, dataForCRC.length);

                    if (verifyResponse(CommandType.COMMAND_NONE.getValue(), dataForCRC, receivedCRC, true) != 0) {

                        return INTERNAL_ERROR;
                    }

                    byte[] vendorID = new byte[4];
                    System.arraycopy(receivedMFG, 18, vendorID, 0, vendorID.length);
                    mfgData.vendorID = Utils.convertASCII(vendorID).trim();

                    byte[] productID = new byte[4];
                    System.arraycopy(receivedMFG, 22, productID, 0, productID.length);
                    mfgData.productID = Utils.convertASCII(productID).trim();

                    byte[] serialNo = new byte[10];
                    System.arraycopy(receivedMFG, 26, serialNo, 0, serialNo.length);
                    mfgData.serialNo = Utils.convertASCII(serialNo).trim();

                    byte[] firmwareVersion = new byte[12];
                    System.arraycopy(receivedMFG, 36, firmwareVersion, 0, firmwareVersion.length);
                    mfgData.firmwareVer = Utils.convertASCII(firmwareVersion).trim();

                    byte[] distributor = new byte[10];
                    System.arraycopy(receivedMFG, 48, distributor, 0, distributor.length);
                    mfgData.distributor = Utils.convertASCII(distributor).trim();

                    byte[] reseller = new byte[10];
                    System.arraycopy(receivedMFG, 58, reseller, 0, reseller.length);
                    mfgData.reseller = Utils.convertASCII(reseller).trim();

                    byte[] lockingKey = new byte[10];
                    System.arraycopy(receivedMFG, 68, lockingKey, 0, lockingKey.length);
                    mfgData.endUserLockingKey = Utils.convertASCII(lockingKey).trim();

                    byte[] make = new byte[6];
                    System.arraycopy(receivedMFG, 78, make, 0, make.length);
                    mfgData.make = Utils.convertASCII(make).trim();

                    byte[] model = new byte[10];
                    System.arraycopy(receivedMFG, 84, model, 0, model.length);
                    mfgData.model = Utils.convertASCII(model).trim();

                    byte[] manufactureDate = new byte[6];
                    System.arraycopy(receivedMFG, 94, manufactureDate, 0, manufactureDate.length);
                    mfgData.dateOfManufacture = Utils.convertASCII(manufactureDate).trim();

                    byte[] macID = new byte[6];
                    System.arraycopy(receivedMFG, 100, macID, 0, macID.length);
                    mfgData.macID = Utils.byteArrayToString(macID).trim();

                    byte[] countryCode = new byte[6];
                    System.arraycopy(receivedMFG, 106, countryCode, 0, countryCode.length);
                    mfgData.countryZoneCode = Utils.convertASCII(countryCode).trim();

                    byte[] fapFWVer = new byte[12];
                    System.arraycopy(receivedMFG, 112, fapFWVer, 0, fapFWVer.length);
                    mfgData.fapFWVer = Utils.convertASCII(fapFWVer).trim();


                } else {
                    ret = INTERNAL_ERROR;
                }
            } else {
                ret = INTERNAL_ERROR;
            }
            return ret;
        } catch (Exception e) {
            return UNKNOWN_ERROR;

        }

    }

    //==============================FAP API======================================

    protected synchronized int InitDevice(byte[] clientKey, DeviceInfo deviceInfo) {
        totalPacket = 0;
        currentPacket = 0;
        lastPacketNo = 0;
        CommandType command = CommandType.COMMAND_NONE;

        if (deviceInfo == null) {
            return OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }

        if(!isDeviceConnected(morfinBleDevice)){
            return DEVICE_NOT_CONNECTED;
        }

        if (isBLEDeviceReady() != MORFIN_AUTH_SUCCESS) {
            return DEVICE_NOT_CONNECTED;
        }

        if (this.deviceInfo != null) {
            return DEVICE_ALREADY_INITIALIZED;
        }

        if (connectedDevice != null) {
            if (connectedDevice.Name.contains("MARC")) {
                command = COMMAND_MARC_DEVICE_INIT;
            } else if (connectedDevice.Name.contains("MELO")) {
                command = COMMAND_MELO_DEVICE_INIT;
            }
        } else {
            return DEVICE_NOT_CONNECTED;
        }
        byte[] endUser = mfgData.endUserLockingKey.getBytes();
        int ret = ValidateClientKeyBL(clientKey,endUser);

        if(ret != MORFIN_AUTH_SUCCESS){
            return INVALID_CLIENT_KEY;
        }

        ret = runBiometricDeviceCommand(command, null, 1, 1,0);

        if (receivedBioData != null && bioErrorCode == MORFIN_AUTH_SUCCESS && ret == 0) {
            if (verifyResponse(command.getValue(), receivedBioData, null, false) == 0 && receivedBioData[6] == CMD_PAY_LOAD_ZERO) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                for (int i = 11; i < (receivedBioData.length - 4); i++) {
                    output.write(receivedBioData[i]);
                }

                byte[] CRC = new byte[4];
                System.arraycopy(receivedBioData, (receivedBioData.length - 4), CRC, 0, 4);

                if (verifyResponse(command.getValue(), output.toByteArray(), CRC, true) != 0) {
                    return INVALID_DATA_RECEIVED;
                }

                try {
                    String strDeviceInfo = new String(output.toByteArray(), StandardCharsets.UTF_8);
                    ArrayList<String> info = new ArrayList<>(Arrays.asList(strDeviceInfo.split(",")));
                    ArrayList<String> srNo = new ArrayList<>(Arrays.asList(connectedDevice.Name.split("_")));
                    if (info != null && info.size() == 8 && srNo != null && srNo.size() == 2) {
                        deviceInfo.Make = info.get(0);
                        deviceInfo.Model = info.get(1) + "BL";
                        deviceInfo.SerialNo = srNo.get(1);
                        vID = info.get(3);
                        pID = info.get(4);
                        uniqueID = info.get(5);
                        deviceInfo.Width = Integer.parseInt(info.get(6));
                        deviceInfo.Height = Integer.parseInt(info.get(7));

                        this.deviceInfo = deviceInfo;
                    } else {
                        ret = FAILED_TO_INIT_DEVICE;
                        return ret;
                    }
                } catch (Exception ignore) {
                    ret = FAILED_TO_INIT_DEVICE;
                    return ret;
                }
            } else {
                ret = FAILED_TO_INIT_DEVICE;
                return ret;
            }
            totalPacket = 0;
            currentPacket = 0;
            lastPacketNo = 0;
            command = CommandType.COMMAND_NONE;
            if (connectedDevice != null) {
                if (connectedDevice.Name.contains("MARC")) {
                    command = COMMAND_GET_FIRMWARE_MARC;
                } else if (connectedDevice.Name.contains("MELO")) {
                    command = COMMAND_GET_FIRMWARE_MELO;
                }
            } else {
                return DEVICE_NOT_CONNECTED;
            }

            ret = runBiometricDeviceCommand(command, null, 1, 1,0);
            if (receivedBioData != null && bioErrorCode == MORFIN_AUTH_SUCCESS && ret == 0) {
                if (verifyResponse(command.getValue(), receivedBioData, null, false) == 0 && receivedBioData[6] == CMD_PAY_LOAD_ZERO) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();

                    for (int i = 11; i <= 21; i++) {
                        output.write(receivedBioData[i]);
                    }


                    String strFWVersion = new String(output.toByteArray(), StandardCharsets.UTF_8);
                    int model = -1;
                    if(connectedDevice.Name.contains("MARC10")){
                        model = 0;
                    }else if(connectedDevice.Name.contains("MELO20")){
                        model = 1;
                    }
                    int verifyFW = verifyFW(strFWVersion,model,true);


                    if(verifyFW != MORFIN_AUTH_SUCCESS){
                        this.deviceInfo = null;
                        deviceInfo = new DeviceInfo();
                        return FAILED_TO_INIT_DEVICE;
                    }

                }else{
                    this.deviceInfo = null;
                    deviceInfo = new DeviceInfo();
                    ret = FAILED_TO_INIT_DEVICE;
                }
            }else{
                this.deviceInfo = null;
                deviceInfo = new DeviceInfo();
                ret = FAILED_TO_INIT_DEVICE;
            }

        } else {
            ret = bioErrorCode;
        }
        if (bioErrorCode != PREVIOUS_PROCESS_IS_RUNNING) {
            runningBioCommand = CommandType.COMMAND_NONE;
        }
        return ret;
    }

    protected int unInitDevice() {

        if (deviceInfo == null) {
            return DEVICE_NOT_INITIALIZED;
        }

        try {

            if (runningBioCommand == COMMAND_MARC_START_CAPTURE || runningBioCommand == COMMAND_MELO_START_CAPTURE
                    || runningBioCommand == COMMAND_MARC_MATCH_TEMPLATE || runningBioCommand == COMMAND_MELO_MATCH_TEMPLATE) {
                stopCapture();
                bioErrorCode = CAPTURE_FAILED;
                if (mBioLatch != null) {
                    while (mBioLatch.getCount() > 0) {
                        mBioLatch.countDown();
                    }
                }
                if (lostPacketLatch != null) {
                    while (lostPacketLatch.getCount() > 0) {
                        lostPacketLatch.countDown();
                    }
                }
            }

            totalPacket = 0;
            currentPacket = 0;
            lastPacketNo = 0;
            captureImage = null;
            captureTemplate = null;
            deviceInfo = null;

            raw = null;
            receivedData = null;
            receivedBioData = null;
            packetLoss = null;
            mBioLatch = null;
            mLatch = null;
            lostPacketLatch = null;
            abortProcessLatch = null;
            vID = "";
            pID = "";
            uniqueID = "";

            return MORFIN_AUTH_SUCCESS;
        } catch (Exception e) {
            return FAILED_TO_UINIT_DEVICE;
        }
    }

    protected int startCapture(CaptureFormat captureFormat, int min_quality, int timeout,
                               int[] quality, int[] nfiq) {
        totalPacket = 0;
        currentPacket = 0;
        lastPacketNo = 0;
        captureImage = null;
        captureTemplate = null;
        bufferCRC = 0;
        currentCaptureFormat = captureFormat;

        if (quality == null || nfiq == null || captureFormat == null) {
            return OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }
        if(!isDeviceConnected(morfinBleDevice)){
            return DEVICE_NOT_CONNECTED;
        }
        if (deviceInfo == null || deviceInfo.Model.isEmpty()) {
            return DEVICE_NOT_INITIALIZED;
        }
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        CommandType command = CommandType.COMMAND_NONE;
        if (deviceInfo.Model.contains("MARC")) {
            payload.write(0x01);//Number of Finger

            switch (captureFormat) {
                case FMR_2005:
                case FIR_2005:
                    payload.write(0x00);
                    break;
                case FMR_2011:
                case FIR_2011:
                    payload.write(0x01);
                    break;
                case ANSI_378:
                    payload.write(0x02);
                    break;
                default:
                    payload.write(0x00);
                    break;
            }

            payload.write(0x00);//No of IRIS record to be capture
            payload.write(0x00);//Iris Format
            payload.write(0x00);//No of face photo record to be capture
            payload.write(0x00);//Face Format: 0 for FID

            if (timeout > 30 || timeout < 1) {
                return TIMEOUT_OUT_OF_RANGE;
            }

            payload.write((byte) timeout);

            if (min_quality < 1 || min_quality > 100) {
                return MORFIN_AUTH_E_QTY_OUT_OF_RANGE;
            }
            payload.write((byte) min_quality); //Upper Quality

            //Lower Quality
            if (min_quality > 30) {
                payload.write(30);
            } else if (min_quality == 1) {
                payload.write(1);
            } else {
                payload.write((byte) (min_quality - 1));
            }

            switch (captureFormat) {

                case FMR_2005:
                case FMR_2011:
                case ANSI_378:
                    payload.write(0x02);
                    break;
                case FIR_2005:
                case FIR_2011:
                    payload.write(0x04);
                    break;
//                    case RAW:
//                    case WSQ:
//                        payload.write(0x05);
//                        break;
                default:
                    payload.write(0x02);
                    break;

            }
            command = COMMAND_MARC_START_CAPTURE;

        }else if(deviceInfo.Model.contains("MELO")){
                payload.write(0x01);//Number of Finger

                switch (captureFormat) {
                    case FMR_2005:
                        payload.write(0x00);
                        break;
                    case FMR_2011:
                        payload.write(0x01);
                        break;
                    case ANSI_378:
                        payload.write(0x02);
                        break;
                    default:
                        payload.write(0x00);
                        break;
                }

                payload.write(0x00); //only wsq support

//                switch (captureRequest.imageDataAlgorithm){
//                    case WSQ_V3_0:
//                        payload.write(0x00);
//                        break;
//                    case JPEG_2K:
//                        payload.write(0x01);
//                        break;
//                    default:
//                        return MorfinErrorCode.INVALID_PAYLOAD_DATA;
//                }


                switch (captureFormat) {
                    case FIR_2005:
                        payload.write(0x00);
                        break;
                    case FIR_2011:
                        payload.write(0x01);
                        break;
                    default:
                        payload.write(0x00);
                        break;
                }

                if (timeout > 30 || timeout < 1) {
                    return TIMEOUT_OUT_OF_RANGE;
                }

                payload.write((byte) timeout);

                if (min_quality < 1 || min_quality > 100) {
                    return MORFIN_AUTH_E_QTY_OUT_OF_RANGE;
                }
                payload.write((byte) min_quality);

                switch (captureFormat) {
                    case FMR_2005:
                    case FMR_2011:
                    case ANSI_378:
                        payload.write(0x02);
                        break;
                    case FIR_2005:
                    case FIR_2011:
                        payload.write(0x03);
                        break;
                    default:
                        payload.write(0x02);
                        break;

                }
                command = COMMAND_MELO_START_CAPTURE;
        }

        ByteBuffer payloadArray = ByteBuffer.allocate(payload.size());
        payloadArray.put(payload.toByteArray());
        packetLoss = new ArrayList<>();
        raw = null;


      /*  if(morfinBleDevice!=null &&morfinBleDevice.name.contains("MARC10")){
        }else {
            command = Const.START_CAPTURE_MELO20;
        }*/
        int ret = runBiometricDeviceCommand(command, payloadArray.array(), 1, 1,timeout);
        if (receivedBioData != null && bioErrorCode == MORFIN_AUTH_SUCCESS) {
            if (verifyResponse(command.getValue(), receivedBioData, null, false) == 0) {

                if (receivedBioData[5] == CMD_PAY_LOAD_ZERO &&
                        (receivedBioData[6] == CMD_PAY_LOAD_ZERO ||
                                receivedBioData[6] == 0x01 || receivedBioData[6] == 0x02
                                || receivedBioData[6] == 0x03 || receivedBioData[6] == 0x04
                                || receivedBioData[6] == 0x05 || receivedBioData[6] == 0x06
                                || receivedBioData[6] == 0x07 || receivedBioData[6] == 0x08)) {

                    if (!packetLoss.isEmpty()) {

                        for (int i = 0; i < packetLoss.size(); i++) {

                            ByteArrayOutputStream lostPacket = new ByteArrayOutputStream();
                            lostPacket.write(Utils.getByteArrayFromInt(packetLoss.get(i)), 0, 2);
                            ByteBuffer lostPacketArray = ByteBuffer.allocate(lostPacket.size());
                            lostPacketArray.put(lostPacket.toByteArray());
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            if (bioErrorCode == CAPTURE_STOP || ret == CAPTURE_STOP) {
                                return CAPTURE_STOP;
                            }
                            int req = runBiometricDeviceCommand(COMMAND_READ_PACKET, lostPacketArray.array(), 1, 1,0);

                            if (receivedBioData != null && bioErrorCode == MORFIN_AUTH_SUCCESS) {

                            } else {
                                ret = FAILED_TO_GET_IMAGE;
                                runningBioCommand = CommandType.COMMAND_NONE;
                                return ret;
                            }

                        }

                    }


                    byte[] combinedArray = null;

                    try {
                        if (captureFormat == FIR_2005 || captureFormat == FIR_2011 ||
                                captureFormat == FMR_2005 || captureFormat == FMR_2011 || captureFormat == ANSI_378) {
                            if (raw != null) {
                                for (int i = 0; i < raw.length; i++) {
                                    if (raw[i] != null) {
                                        if (combinedArray != null) {
                                            byte[] temp = new byte[combinedArray.length + raw[i].length];
                                            System.arraycopy(combinedArray, 0, temp, 0, combinedArray.length);
                                            System.arraycopy(raw[i], 0, temp, combinedArray.length, raw[i].length);

                                            combinedArray = new byte[temp.length];
                                            System.arraycopy(temp, 0, combinedArray, 0, temp.length);

                                        } else {
                                            combinedArray = new byte[raw[i].length];
                                            System.arraycopy(raw[i], 0, combinedArray, 0, raw[i].length);
                                        }
                                    }


                                }
                            }
                        }
                    } catch (Exception e) {
                        bioErrorCode = UNKNOWN_ERROR;
                        if (mBioLatch != null) {
                            mBioLatch.countDown();
                        }
                        e.printStackTrace();
                    }

                    switch (captureFormat) {

                        case FMR_2005:
                        case FMR_2011:
                        case ANSI_378:

                            if (receivedBioData != null ) {
                                if (bioErrorCode == MORFIN_AUTH_SUCCESS) {
                                    if (verifyResponse(command.getValue(), receivedBioData, null, false) == 0) {
                                        byte[] rawImage = null;
                                        if (combinedArray != null) {
                                            try {
                                                if (receivedCheckSum != null) {
                                                    int verifyData = verifyResponse(command.getValue(), combinedArray, receivedCheckSum, true);


                                                    if (verifyData != 0) {
                                                        ret = CAPTURE_FAILED;
                                                        runningBioCommand = CommandType.COMMAND_NONE;
                                                        return ret;
                                                    }
                                                    switch (deviceInfo.Model) {
                                                        case "MARC10BL":
                                                            quality[0] = combinedArray[0];
                                                            nfiq[0] = combinedArray[2];
                                                            break;
                                                        case "MELO20BL":
                                                            quality[0] = combinedArray[0];
                                                            nfiq[0] = combinedArray[1];
                                                            break;
                                                    }

//                                                    quality[0] = combinedArray[0];
//                                                    captureResponse.NMPoint = combinedArray[1];
//                                                    nfiq[0] = combinedArray[2];

                                                    rawImage = new byte[combinedArray.length - 3];
                                                    System.arraycopy(combinedArray, 3, rawImage, 0, rawImage.length);

                                                } else {
                                                    return INVALID_DATA_RECEIVED;
                                                }

                                            } catch (Exception e) {
                                                return CAPTURE_FAILED;
                                            }
                                        } else {

                                            switch (deviceInfo.Model) {
                                                case "MARC10BL":
                                                    quality[0] =  receivedBioData[11];
                                                    nfiq[0]     = receivedBioData[13];
                                                    break;
                                                case "MELO20BL":
                                                    quality[0] =  receivedBioData[11];
                                                    nfiq[0]    =  receivedBioData[12];
                                                    break;
                                            }

                                            rawImage = new byte[receivedBioData.length - 18];

                                            System.arraycopy(receivedBioData, 14, rawImage, 0, rawImage.length);
                                            receivedCheckSum = new byte[4];
                                            System.arraycopy(receivedBioData, receivedBioData.length - 4, receivedCheckSum, 0, receivedCheckSum.length);

                                            if (receivedCheckSum != null) {
                                                byte[] qty = new byte[3];

                                                qty[0] = receivedBioData[11];
                                                qty[1] = receivedBioData[12];
                                                qty[2] = receivedBioData[13];

                                                byte[] totalPayload = new byte[rawImage.length + qty.length];
                                                System.arraycopy(qty, 0, totalPayload, 0, qty.length);
                                                System.arraycopy(rawImage, 0, totalPayload, qty.length, rawImage.length);


                                                int verifyData = verifyResponse(command.getValue(), totalPayload, receivedCheckSum, true);

                                                if (verifyData != 0) {
                                                    ret = CAPTURE_FAILED;
                                                    runningBioCommand = CommandType.COMMAND_NONE;
                                                    return ret;
                                                }
                                            }
                                        }
                                        captureTemplate = new byte[rawImage.length];
                                        System.arraycopy(rawImage, 0, captureTemplate, 0, rawImage.length);
                                    } else {
                                        ret = CAPTURE_FAILED;
                                    }
                                } else {
                                    ret = bioErrorCode;
                                }
                            }
                            break;
                        case FIR_2005:
                        case FIR_2011:
                            if (receivedCheckSum != null) {
                                int verifyData = -1;
                                if (combinedArray != null) {
                                    verifyData = verifyResponse(command.getValue(), combinedArray, receivedCheckSum, true);
                                }

                                if (verifyData != 0) {
                                    ret = CAPTURE_FAILED;
                                    runningBioCommand = CommandType.COMMAND_NONE;
                                    return ret;
                                }
                            }

                            try {

                                if (combinedArray != null) {
                                    switch (deviceInfo.Model) {
                                        case "MARC10BL":
                                            quality[0] = combinedArray[0];
                                            nfiq[0] = combinedArray[2];
                                            break;
                                        case "MELO20BL":
                                            quality[0] = combinedArray[0];
                                            nfiq[0] = combinedArray[1];
                                            break;
                                    }
                                    byte[] firData = new byte[combinedArray.length - 3];
                                    System.arraycopy(combinedArray, 3, firData, 0, firData.length);
                                    captureImage = new byte[firData.length];
                                    System.arraycopy(firData, 0, captureImage, 0, firData.length);
                                }
                            } catch (Exception e) {
                                return INTERNAL_ERROR;
                            }
                            break;
                    }
                    runningBioCommand = CommandType.COMMAND_NONE;
                    return ret;
                } else if (receivedBioData[5] == 0x01) {
                    switch (receivedBioData[6]) {
                        case 0x01:
                            ret = INTERNAL_ERROR;
                            break;
                        case 0x03:
                        case 0x21:
                            ret = IMG_PROCESS_E_INVALIDPARAM;
                            break;
                        case 0x04:
                            ret = FAILED_TO_ALLOC_MEM;
                            break;
                        case 0x51:
                            ret = DEVICE_NOT_INITIALIZED;
                            break;
                        case 0x10:
                            ret = MORFIN_AUTH_CAPTURE_TIMEOUT;
                            break;
                        default:
                            ret = CAPTURE_FAILED;
                            break;
                    }
                    runningBioCommand = CommandType.COMMAND_NONE;
                    return ret;
                } else if (receivedBioData[5] == 0x02) {
                    if (connectedDevice.Name.contains("MARC")) {
                        switch (receivedBioData[6]) {
                            case 0x01:
                            case 0x02:
                                ret = CAPTURE_ALREADY_STARTED;
                                break;
                            default:
                                ret = CAPTURE_FAILED;
                                break;
                        }
                    } else if (connectedDevice.Name.contains("MELO")) {
                        switch (receivedBioData[6]) {

                            case 0x01:
                                ret = CAPTURE_ALREADY_STARTED;
                                break;
                            case 0x04:
                                ret = TEMPLATE_MATCHING_IN_PROGRESS;
                                break;
                            default:
                                ret = CAPTURE_FAILED;
                                break;
                        }

                    }
                    runningBioCommand = CommandType.COMMAND_NONE;
                    return ret;
                }
            } else {
                ret = CAPTURE_FAILED;
            }
        } else {
            ret = bioErrorCode;
        }
        if (runningBioCommand != COMMAND_MARC_STOP_CAPTURE && runningBioCommand != COMMAND_MELO_STOP_CAPTURE) {
            if (bioErrorCode != PREVIOUS_PROCESS_IS_RUNNING && bioErrorCode != CAPTURE_ALREADY_STARTED) {
                runningBioCommand = CommandType.COMMAND_NONE;
            }
        }
        return ret;
    }

    protected synchronized int stopCapture() {
        totalPacket = 0;
        currentPacket = 0;
        lastPacketNo = 0;

        stopWaitTimer();
        CommandType command = CommandType.COMMAND_NONE;
        if (isBLEDeviceReady() != MORFIN_AUTH_SUCCESS) {
            return DEVICE_NOT_CONNECTED;
        }
        if(!isDeviceConnected(morfinBleDevice)){
            return DEVICE_NOT_CONNECTED;
        }

        if (deviceInfo == null) {
            return DEVICE_NOT_INITIALIZED;
        }

        if (connectedDevice != null) {
            if (connectedDevice.Name.contains("MARC")) {
                command = COMMAND_MARC_STOP_CAPTURE;
            } else if (connectedDevice.Name.contains("MELO")) {
                command = COMMAND_MELO_STOP_CAPTURE;
            }
        } else {
            return DEVICE_NOT_CONNECTED;
        }
        int ret = runBiometricDeviceCommand(command, null, 1, 1,0);
        if (receivedBioData != null && bioErrorCode == MORFIN_AUTH_SUCCESS) {
            if (verifyResponse(command.getValue(), receivedBioData, null, false) == 0) {
                if (receivedBioData[6] == CMD_PAY_LOAD_ZERO) {
                    return ret;
                } else {
                    return MORFIN_AUTH_SUCCESS;
                }
            } else {
                ret = MORFIN_AUTH_SUCCESS;
            }

        } else {
            ret = MORFIN_AUTH_SUCCESS;
        }
        if (bioErrorCode != PREVIOUS_PROCESS_IS_RUNNING) {
            runningBioCommand = CommandType.COMMAND_NONE;
        }
        return ret;
    }

    protected int matchTemplate(byte[] galleryTemplate, int requestQuality, int timeout, TemplateFormat format, int[] matchScore) {
        totalPacket = 0;
        currentPacket = 0;
        lastPacketNo = 0;

        if (isBLEDeviceReady() != MORFIN_AUTH_SUCCESS) {
            return DEVICE_NOT_CONNECTED;
        }

        if (deviceInfo == null) {
            return DEVICE_NOT_INITIALIZED;
        }
        if (matchScore == null) {
            return OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        CommandType command = CommandType.COMMAND_NONE;

        if(deviceInfo.Model.contains("MARC")) {
            command = COMMAND_MARC_MATCH_TEMPLATE;
        }else if(deviceInfo.Model.contains("MELO")){
            command = COMMAND_MELO_MATCH_TEMPLATE;
        }

        if (format == null) {
            return OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }

        if (galleryTemplate == null || galleryTemplate.length == 0) {
            return OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }
        switch (format) {
            case FMR_V2005:
                payload.write((int) 0);
                break;
            case FMR_V2011:
                payload.write((int) 1);
                break;
            case ANSI_V378:
                payload.write((int) 2);
                break;
        }
        if (timeout > 255 || timeout < 0) {
            return TIMEOUT_OUT_OF_RANGE;
        }

        payload.write(timeout);
        if (requestQuality < 1 || requestQuality > 100) {
            return MORFIN_AUTH_E_QTY_OUT_OF_RANGE;
        }
        payload.write(requestQuality);
        payload.write((int) 1); //No of Template to match
        payload.write((int) 1); //Template count
        payload.write(Utils.getByteArrayFromInt(galleryTemplate.length), 0, 2); //Template len
        payload.write(galleryTemplate, 0, galleryTemplate.length);

        ByteBuffer payloadArray = ByteBuffer.allocate(payload.size());
        payloadArray.put(payload.toByteArray());

        int ret = runBiometricDeviceCommand(command, payloadArray.array(), 1, 1,timeout);
        if (receivedBioData != null && bioErrorCode == MORFIN_AUTH_SUCCESS) {
            if (verifyResponse(command.getValue(), receivedBioData, null, false) == 0 && receivedBioData[6] == CMD_PAY_LOAD_ZERO) {
                matchScore[0] = Utils.getIntFrombyte(receivedBioData[12], receivedBioData[13], false);
            } else {
                ret = MATCH_TEMPLATE_FAIL;
            }
        } else {
            ret = MATCH_TEMPLATE_FAIL;
        }
        runningBioCommand = CommandType.COMMAND_NONE;
        return ret;
    }

    protected int GetTemplate(TemplateFormat format, byte[] template, int[] templateLen) {
        if (template == null || templateLen == null || format == null) {
            return OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }
        switch (format) {
            case FMR_V2005:
                if (currentCaptureFormat == FMR_2011 || currentCaptureFormat == ANSI_378) {
                    return INVLD_TEMPLATE_VERSION;
                }
                break;
            case FMR_V2011:
                if (currentCaptureFormat == FMR_2005 || currentCaptureFormat == ANSI_378) {
                    return INVLD_TEMPLATE_VERSION;
                }
                break;
            case ANSI_V378:
                if (currentCaptureFormat == FMR_2005 || currentCaptureFormat == FMR_2011) {
                    return INVLD_TEMPLATE_VERSION;
                }
                break;
            default:
                return UNSUPPORTED_TEMPLATE_FORMAT;
        }
        if (captureTemplate == null) {
            return FAILED_TO_GET_TEMPLATE;
        }
        try {
            templateLen[0] = captureTemplate.length;
            System.arraycopy(captureTemplate, 0, template, 0, captureTemplate.length);
            return MORFIN_AUTH_SUCCESS;
        } catch (Exception e) {
            return INTERNAL_ERROR;
        }
    }

    protected int GetImage(ImageFormat image_format, byte[] image, int[] image_len) {
        if (image == null || image_len == null || image_format == null) {
            return OBJECT_CANNOT_BE_NULL_OR_EMPTY;
        }
        if (captureImage == null) {
            return FAILED_TO_GET_IMAGE;
        }
        byte[] imgData = null;
        switch (image_format) {
            case FIR_V2005:
                try {
                    if (currentCaptureFormat == FIR_2011) {
                        return FAILED_TO_GET_IMAGE;
                    }
                    image_len[0] = captureImage.length;
                    System.arraycopy(captureImage, 0, image, 0, captureImage.length);
                    return MORFIN_AUTH_SUCCESS;
                } catch (Exception e) {
                    return INTERNAL_ERROR;
                }

            case FIR_V2011:
                try {
                    if (currentCaptureFormat == FIR_2005) {
                        return FAILED_TO_GET_IMAGE;
                    }
                    image_len[0] = captureImage.length;
                    System.arraycopy(captureImage, 0, image, 0, captureImage.length);
                    return MORFIN_AUTH_SUCCESS;
                } catch (Exception e) {
                    return INTERNAL_ERROR;
                }
            case RAW:
                try {
                    imgData = null;
                    if (currentCaptureFormat == FIR_2005) {
                        imgData = new byte[captureImage.length - 46];
                        System.arraycopy(captureImage, 46, imgData, 0, imgData.length);
                    } else if (currentCaptureFormat == FIR_2011) {
                        imgData = new byte[captureImage.length - 57];
                        System.arraycopy(captureImage, 57, imgData, 0, imgData.length);
                    }
                    if (imgData != null) {
                        byte[] rawImage = new byte[(deviceInfo.Width * deviceInfo.Height) + 2];
                        int[] rawImageLen = new int[1];
                        int[] width = new int[1];
                        int[] height = new int[1];
                        int[] depth = new int[1];
                        int[] ppi = new int[1];
                        int ret = DecodeWSQImage(imgData, rawImage, rawImageLen, width, height, depth, ppi);

                        if (ret != 0) {
                            return FAILED_TO_GET_IMAGE;
                        }
//                        byte[] wsqToRaw = decodeToRaw();
                        image_len[0] = rawImageLen[0];
                        System.arraycopy(rawImage, 0, image, 0, rawImage.length);

                    } else {
                        return FAILED_TO_GET_IMAGE;
                    }
                    return MORFIN_AUTH_SUCCESS;
                } catch (Exception e) {
                    return INTERNAL_ERROR;
                }
            case WSQ:
                try {
                    imgData = null;

                    if (currentCaptureFormat == FIR_2005) {
                        imgData = new byte[captureImage.length - 46];
                        System.arraycopy(captureImage, 46, imgData, 0, imgData.length);
                    } else if (currentCaptureFormat == FIR_2011) {
                        imgData = new byte[captureImage.length - 57];
                        System.arraycopy(captureImage, 57, imgData, 0, imgData.length);
                    }
                    if (imgData != null) {
                        image_len[0] = imgData.length;
                        System.arraycopy(imgData, 0, image, 0, imgData.length);

                    } else {
                        return FAILED_TO_GET_IMAGE;
                    }
                    return MORFIN_AUTH_SUCCESS;
                } catch (Exception e) {
                    return INTERNAL_ERROR;
                }
            case BMP:
                try {
                    imgData = null;
                    if (currentCaptureFormat == FIR_2005) {
                        imgData = new byte[captureImage.length - 46];
                        System.arraycopy(captureImage, 46, imgData, 0, imgData.length);
                    } else if (currentCaptureFormat == FIR_2011) {
                        imgData = new byte[captureImage.length - 57];
                        System.arraycopy(captureImage, 57, imgData, 0, imgData.length);
                    }
                    if (imgData != null) {
                        byte[] rawImage = new byte[(deviceInfo.Width * deviceInfo.Height) + 2];
                        int[] rawImageLen = new int[1];
                        int[] width = new int[1];
                        int[] height = new int[1];
                        int[] depth = new int[1];
                        int[] ppi = new int[1];
                        int ret = DecodeWSQImage(imgData, rawImage, rawImageLen, width, height, depth, ppi);


                        if (ret != 0) {
                            return FAILED_TO_GET_IMAGE;
                        }

                        byte[] bmp;
                        bmp = Utils.rawToBitmapBytes(rawImage, deviceInfo.Width, deviceInfo.Height);
                        if (bmp != null) {
                            image_len[0] = bmp.length;
                            System.arraycopy(bmp, 0, image, 0, bmp.length);
                        } else {
                            return FAILED_TO_GET_IMAGE;
                        }

                    } else {
                        return FAILED_TO_GET_IMAGE;
                    }
                    return MORFIN_AUTH_SUCCESS;
                } catch (Exception e) {
                    return INTERNAL_ERROR;
                }
            default:
                return UNSUPPORTED_IMAGE_FORMAT;

        }

    }

    protected String getErrorDescription(int errorCode) {

        switch (errorCode) {
            case REQUEST_TIMEOUT:
                return "Request timeout";
            case BLE_NOT_SUPPORTED:
                return "BLE not supported on this device";
            case BLUETOOTH_DISABLE:
                return "Bluetooth is disabled";
            case LOCATION_DISABLE:
                return "Location is disabled";
            case BLUETOOTH_PERMISSION_NOT_ALLOW:
                return "Bluetooth/Location permission is not allowed";
            case FAILED_TO_CONNECT:
                return "Failed to connect";
            case INVALID_DATA_RECEIVED:
                return "Invalid data received";
            case INVALID_VALUE_PASS:
                return "Invalid param pass";
            case DEVICE_NOT_CONNECTED:
                return "Device not connected";
            case MORFIN_AUTH_CAPTURE_TIMEOUT:
                return "Capture time out";
            case DEVICE_NOT_INITIALIZED:
                return "Device not initialized";
            case FAILED_TO_ALLOC_MEM:
                return "Memory allocation failed";
            case MORFIN_AUTH_SUCCESS:
                return "Success";
            case IMG_PROCESS_E_INVALIDPARAM:
                return "Image process param invalid";
            case CAPTURE_STOP:
                return "Capture stop";
            case FAILED_TO_STOP_CAPTURE:
                return "Failed to stop capture";
            case FAILED_TO_UINIT_DEVICE:
                return "Device uninitialized failed";
            case INVLD_LIC_KEY:
                return "Invalid Licence key";
            case DEVICE_ALREADY_INITIALIZED:
                return "Device already initialized";
            case OBJECT_CANNOT_BE_NULL_OR_EMPTY:
                return "object can not be null or empty";
            case FAILED_TO_GET_TEMPLATE:
                return "Failed to get template";
            case UNSUPPORTED_IMAGE_FORMAT:
                return "Unsupported Image Format";
            case UNSUPPORTED_TEMPLATE_FORMAT:
                return "Unsupported Template Format";
            case MORFIN_AUTH_E_QTY_OUT_OF_RANGE:
                return "Invalid value passed in quality";
            case TIMEOUT_OUT_OF_RANGE:
                return "Invalid value passed in timeout";
            case CAPTURE_ALREADY_STARTED:
                return "Capture already started";
            case FAILED_TO_GET_BATTERY_CONDITION:
                return "Failed to get battery condition";
            case FAILED_TO_SET_TIMER_VALUE:
                return "Failed to set timer values";
            case FAILED_TO_GET_TIMER_VALUE:
                return "Failed to get timer values";
            case CAPTURE_FAILED:
                return "Capture failed";
            case FAILED_TO_GET_IMAGE:
                return "Failed to get image";
            case MATCH_TEMPLATE_FAIL:
                return "Matching template fail";
            case PREVIOUS_PROCESS_IS_RUNNING:
                return "Previous process is running";
            case INTERNAL_ERROR:
                return "Internal error";
            case FAILED_TO_INIT_DEVICE:
                return "Failed to init Device";
            case INVLD_TEMPLATE_VERSION:
                return "Invalid template version";
            case MORFIN_AUTH_E_NULL_PARAM:
                return "Auth parameter is null";
            case BAD_CAPTURE_IMAGE:
                return "Bad capture image";
            case TEMPLATE_MATCHING_IN_PROGRESS:
                return "Template matching in progress";
            case INVALID_CLIENT_KEY:
                return "Invalid client key";
            case FAILED_TO_GENERATE_CLIENT_KEY:
                return "Fail to generate client key";
            default:
                return "UNKNOWN ERROR";

        }

    }

    protected int getClientKeyBL(String clientKey, String[] signeKey) {
        byte[] signedKey = new byte[1024];
        int[] keyLen = new int[1];
        int ret = GenerateSignedKeyBL(clientKey.getBytes(), signedKey, keyLen);
        if (ret == 0) {
            byte[] finalKey = new byte[keyLen[0]];
            System.arraycopy(signedKey, 0, finalKey, 0, keyLen[0]);
            // Set the first element of the array to the resulting string
            signeKey[0] = new String(finalKey);
            ret = FAILED_TO_GENERATE_CLIENT_KEY;
        }
        return ret;
    }

    private void startWaitTimer() {
        if (waitTimer != null) {
            waitTimer.postDelayed(myRunnable, TIME_TO_WAIT);
        }
    }

    private void stopWaitTimer() {
        if (waitTimer != null) {
            waitTimer.removeCallbacks(myRunnable);
        }
    }

    private void restartWaitTimer() {
        if (waitTimer != null) {
            waitTimer.removeCallbacks(myRunnable);
            waitTimer.postDelayed(myRunnable, TIME_TO_WAIT);
        }
    }

    Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBioLatch != null) {
                if (lastPacketNo != totalPacket) {
                    while (lastPacketNo != totalPacket) {
                        if (packetLoss != null) {
                            packetLoss.add(lastPacketNo + 1);
                        }
                        lastPacketNo++;
                    }
                }

                bioErrorCode = MORFIN_AUTH_SUCCESS;
                mBioLatch.countDown();
            }
        }
    };

    enum CommandType {
        COMMAND_NONE                    (-1),
        COMMAND_MARC_DEVICE_INIT        (0),
        COMMAND_MARC_START_CAPTURE      (1),
        COMMAND_MARC_STOP_CAPTURE       (2),
        COMMAND_MARC_MATCH_TEMPLATE     (3),
        COMMAND_MELO_DEVICE_INIT        (4),
        COMMAND_MELO_START_CAPTURE      (5),
        COMMAND_MELO_STOP_CAPTURE       (6),
        COMMAND_MELO_MATCH_TEMPLATE     (7),
        COMMAND_READ_PACKET             (8),
        COMMAND_GET_CHALLENGE_KEY       (9),
        COMMAND_SEND_CHALLENGE_KEY      (10),
        COMMAND_GET_BATTERY_INFO        (11),
        COMMAND_SET_TIMER_VALUE         (12),
        COMMAND_GET_TIMER_VALUE         (13),
        COMMAND_GET_BLE_MFG_DATA        (14),
        COMMAND_DISCONNECT_NOTIFICATION (15),
        COMMAND_FAULT_NOTIFICATION      (16),
        COMMAND_GET_FIRMWARE_MARC       (17),
        COMMAND_GET_FIRMWARE_MELO       (18),
        COMMAND_GET_CHARGER_INFO        (19);
        private final int value;

        private CommandType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }



    private static native int generateCommand(int commandType, byte[] payload, byte[] outComm, int[] commLen);
    private static native int generateBLECommand(int commandType, byte[] payload, byte[] outComm, int[] commLen);
    private static native int verifyResponse(int commandType, byte[] resData, byte[] resCRC, boolean verify);
    private static native int verifyBLEResponse(int commandType, byte[] resData, byte[] resCRC, boolean verify);
    private static native int getConnectionKey(byte[] inData, String address, int[] out);
    private static native int DecodeWSQImage(byte[] WSQImage, byte[] RawImage, int[] RawImageLen,
                                            int[] width, int[] height, int[] depth, int[] ppi);
    private static native int verifyFW(String fwVer,int model,boolean isFapFw);
    private static native int GenerateSignedKeyBL(byte[] clientKey, byte[] signedKey, int[] keyLen);
    private static native int ValidateClientKeyBL(byte[] clientKey,byte[] endUser);
}
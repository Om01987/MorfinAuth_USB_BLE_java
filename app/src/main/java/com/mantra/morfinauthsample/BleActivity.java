package com.mantra.morfinauthsample;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.mantra.morfinauth.DeviceInfo;
import com.mantra.morfinauth.MorfinAuthBLE;

import com.mantra.morfinauth.MorfinAuthNative;
import com.mantra.morfinauth.ble.MorfinAuthBLE_Callback;
import com.mantra.morfinauth.ble.enums.CaptureFormat;
import com.mantra.morfinauth.ble.model.BatteryInformation;
import com.mantra.morfinauth.ble.model.TimerValues;
import com.mantra.morfinauth.enums.ImageFormat;
import com.mantra.morfinauth.ble.enums.MorfinBleState;
import com.mantra.morfinauth.ble.enums.MorfinNotifications;
import com.mantra.morfinauth.enums.TemplateFormat;
import com.mantra.morfinauth.ble.model.MorfinBleDevice;
import com.mantra.morfinauthsample.adapter.ActionAdapter;
import com.mantra.morfinauthsample.adapter.MenuAdapter;
import com.mantra.morfinauthsample.adapter.NavigationMenuItem;
import com.mantra.morfinauthsample.adapter.RecyclerViewAdapter;
import com.mantra.morfinauthsample.databinding.ActivityBleBinding;
import com.mantra.morfinauthsample.dialog.GenerateKeyDialog;
import com.mantra.morfinauthsample.dialog.SelectQualityDialog;
import com.mantra.morfinauthsample.dialog.SelectTemplateFormatDialog;
import com.mantra.morfinauthsample.dialog.SelectTimeoutDialog;
import com.mantra.morfinauthsample.dialog.SetKeyDialog;
import com.mantra.morfinauthsample.dialog.ViewKeyDialog;
import com.mantra.morfinauthsample.dialog.ble.SelectCaptureFormatDialogble;
import com.mantra.morfinauthsample.dialog.ble.SelectImageFormatDialogble;
import com.mantra.morfinauthsample.dialog.ble.SelectTimerValueDialog;
import com.mantra.morfinauthsample.dialog.ble.ShowTimerDialog;
import com.mantra.morfinauthsample.dialog.ble.ShowbattryDialog;
import com.mantra.morfinauthsample.ui.InputFilterMinMax;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class BleActivity extends AppCompatActivity implements
        MenuAdapter.ItemClickListener
        , ActionAdapter.ActionClickListener
        , BleActionAdapter.BleActionClickListener
        , NavigationView.OnNavigationItemSelectedListener
        , MorfinAuthBLE_Callback {

    private MenuAdapter menuAdapter;
    private ActionAdapter actionAdapter;
    private BleActionAdapter bleActionAdapter;
    ArrayList<NavigationMenuItem> navigationMenuItemArrayList,
            navigationMenuActionArrayList, navigationMenuBleActionArrayList;
    private MorfinAuthBLE morfinAuthBLE;
    private static final String strSelect = "No Device";
    private byte[] lastCapFingerDataImage;
    private byte[] lastCapFingerDataTemplate;
    private DeviceInfo lastDeviceInfo;
    ImageFormat captureImageFormatenum;

    TemplateFormat captureTemplateFormatenum;
    CaptureFormat capCaptureFormatenum;
    private boolean isStartCaptureRunning;
    private boolean isStopCaptureRunning;


    private enum ScannerAction {
        Capture
    }

    private ScannerAction scannerAction = ScannerAction.Capture;
    private SelectImageFormatDialogble selectImageFormatDialogble;

    private SelectTemplateFormatDialog selectTemplateFormatDialog;
    private SelectQualityDialog selectQualityDialog;
    private SelectTimeoutDialog selectTimeoutDialog;
    private ShowbattryDialog showbattryDialog;
    private SelectTimerValueDialog selectTimerValueDialog;
    private ShowTimerDialog showTimerDialog;
    private SelectCaptureFormatDialogble selectCaptureFormatDialogble;
    private String clientKey = "";
    public static long lastClickTime = 0;
    public static int ClickThreshold = 1000;
    int minQuality = 60;
    int timeOut = 10;

    public int sleepModeTimerValue = 0;
    public int offModeTimerValue = 0;
    public int advertisementTimerValue = 0;

    public boolean InitCalled = false;

    ActivityBleBinding binding;

    private List<MorfinBleDevice> scannedDevices = new ArrayList<>();
    private RecyclerViewAdapter deviceadapter;
    private MorfinBleDevice morfinBleDevice;
    private SetKeyDialog selectKeyDialog;
    private GenerateKeyDialog selectGenerateKeyDialog;
    private ViewKeyDialog viewKeyDialog;
    private String SignKey = "";



    @Override
    /**
     * TODO: Android on create method run on first when app launch
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBleBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.containerBle.contentMain.txtApp.setText(getString(R.string.app_name) + " (" + BuildConfig.VERSION_NAME + ")");

        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(BleActivity.this);
        binding.menuitemList.setLayoutManager(mLayoutManager1);
        addMenuItem();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(BleActivity.this);
        binding.menuActionList.setLayoutManager(mLayoutManager);
        addActionItem();


        RecyclerView.LayoutManager mBleLayoutManager = new LinearLayoutManager(BleActivity.this);
        binding.menuBleactionList.setLayoutManager(mBleLayoutManager);
        addBleActionItem();

        binding.navView.setCheckedItem(R.id.nav_home);
        binding.navView.setNavigationItemSelectedListener(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        morfinAuthBLE = new MorfinAuthBLE(this, this);
        String file = getExternalFilesDir(null).toString();
        captureImageFormatenum = (ImageFormat.BMP);
        captureTemplateFormatenum = (TemplateFormat.FMR_V2005);
        capCaptureFormatenum = (CaptureFormat.FIR_2005);
        binding.containerBle.contentMain.txtconnect.setText(getString(R.string.select_ble_devices));
        binding.containerBle.contentMain.txtconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * TODO: select ble text method click
             */
            public void onClick(View v) {
                clearText();
                if(morfinBleDevice == null ){
                    showBleDialog();
                }else if(!morfinAuthBLE.IsDeviceConnected(morfinBleDevice)){
                    showBleDialog();
                }
            }
        });
        clearText();
        setonClick();
    }

    /**
     * TODO: common click method
     */
    private void setonClick() {
        binding.containerBle.contentMain.ivFpLogo.setOnClickListener(view -> onLogoClicked());
        binding.containerBle.contentMain.rlSideMenu.setOnClickListener(view -> onViewClicked());
        binding.ivSettingMenu.setOnClickListener(view -> onCloseMenuClicked());
    }

    /**
     * TODO: Drawer logo click on home screen
     */
    public void onLogoClicked() {
        binding.drawerLayout.openDrawer(Gravity.LEFT);
    }

    /**
     * TODO: open Drawer.
     */
    public void onViewClicked() {
        binding.drawerLayout.openDrawer(Gravity.LEFT);
    }

    /**
     * TODO: close Drawer.
     */
    public void onCloseMenuClicked() {
        binding.drawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    /**
     * TODO: menu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    /**
     * TODO: Describe method.
     */
    public boolean onSupportNavigateUp() {
        return false;
    }

    @Override
    /**
     * TODO: stop method.
     */
    protected void onStop() {
        super.onStop();
        if (isStartCaptureRunning) {
            StopCapture();
        }
        isStartCaptureRunning = false;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    /**
     * TODO: back press method.
     */
    public void onBackPressed() {
        super.onBackPressed();
        if (morfinAuthBLE != null) {
            morfinAuthBLE.Disconnect();
        }
    }

    @Override
    /**
     * TODO:  Pause method.
     */
    protected void onPause() {
        super.onPause();
    }

    @Override
    /**
     * TODO: onDestroy method.
     */
    protected void onDestroy() {
        super.onDestroy();
       /* try {
            isStartCaptureRunning = false;
            isStopCaptureRunning = false;
            morfinAuthBLE.unInitDevice();
            morfinAuthBLE.disConnectDevice();
            morfinAuthBLE.stopDiscover();
            if (scannedDevices != null && !scannedDevices.isEmpty()) {
                scannedDevices.clear();
            }
            setClearDeviceInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * TODO: add Menu method.
     */
    public void addMenuItem() {
        try {
            navigationMenuItemArrayList = new ArrayList<>();
            NavigationMenuItem navigationMenuItem;
            try {
                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.menu_get_sdk_version);
                navigationMenuItemArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.menu_supported_device_list);
                navigationMenuItemArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.menu_select_image_format);
                navigationMenuItemArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.menu_select_template_format);
                navigationMenuItemArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.menu_set_quality);
                navigationMenuItemArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.menu_set_timeout);
                navigationMenuItemArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.menu_gen_client_key);
                navigationMenuItemArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.menu_gen_view_key);
                navigationMenuItemArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.menu_set_key);
                navigationMenuItemArrayList.add(navigationMenuItem);

                menuAdapter = new MenuAdapter(BleActivity.this, navigationMenuItemArrayList, this);
                binding.menuitemList.setAdapter(menuAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * TODO: add ble method add.
     */
    public void addBleActionItem() {
        try {
            navigationMenuBleActionArrayList = new ArrayList<>();
            NavigationMenuItem navigationMenuItem;
            try {

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.select_ble_devices);
                navigationMenuBleActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.disconnect);
                navigationMenuBleActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.batteryinfo);
                navigationMenuBleActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.gettimer);
                navigationMenuBleActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.settimer);
                navigationMenuBleActionArrayList.add(navigationMenuItem);

                bleActionAdapter = new BleActionAdapter(BleActivity.this,
                        navigationMenuBleActionArrayList, this);
                binding.menuBleactionList.setAdapter(bleActionAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    /**
     * TODO: ble menu click method.
     */
    public void onBleActionClick(int position, String item_name) {
        binding.drawerLayout.closeDrawer(Gravity.LEFT);
        clearText();
        binding.containerBle.contentMain.imgFinger.post(new Runnable() {
            @Override
            public void run() {
                binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
            }
        });
        switch (position) {
            case 0://select ble Devices
                if(morfinBleDevice == null ){
                    showBleDialog();
                }else if( !morfinAuthBLE.IsDeviceConnected(morfinBleDevice)){
                    showBleDialog();
                } else {
                    setLogs("Already " + morfinBleDevice.name + " Device Connected", false);
                }
                break;
            case 1://Disconnect Devices
                morfinAuthBLE.StopDiscover();
                morfinAuthBLE.UnInitDevice();
                morfinAuthBLE.Disconnect();
                ClearBleview();
                setClearDeviceInfo();
                break;
            case 2://batteryinfo
                if (morfinBleDevice != null) {
                    BatteryInformation batteryInformation = new BatteryInformation();
                    int ret = morfinAuthBLE.GetBatteryInformation(batteryInformation);
                    if (ret == 0) {
                        showBattryInfoDialog(batteryInformation);
                    }
                } else {
                    setLogs("Ble Device Not Connected", true);
                }
                break;
            case 3://get timer value
                if (morfinBleDevice != null) {
                    TimerValues timerValues = new TimerValues();
                    int ret1 = morfinAuthBLE.GetDeviceTimerValues(timerValues);
                    if (ret1 == 0) {
                        showGetTimerValueDialog(timerValues);
                    } else {
                        setLogs("Get Timer Values Error " + ret1, true);
                    }
                } else {
                    setLogs("Ble Device Not Connected", true);
                }
                break;
            case 4://select timer value
                if (morfinBleDevice != null) {
                    TimerValues timerValues = new TimerValues();
                    int ret1 = morfinAuthBLE.GetDeviceTimerValues(timerValues);
                    if (ret1 == 0) {
                        showTimerValueDialog(timerValues);
                    } else {
                        setLogs("Get Timer Values Error " + ret1, true);
                    }
                } else {
                    setLogs("Ble Device Not Connected", true);
                }
        }
    }

    /**
     * TODO: clear ble view.
     */
    public void ClearBleview() {
        if (scannedDevices != null && !scannedDevices.isEmpty()) {
            scannedDevices.clear();
        }
        binding.containerBle.contentMain.txtconnect.post(new Runnable() {

            @Override
            public void run() {
                binding.containerBle.contentMain.txtconnect.setText(getString(R.string.select_ble_devices));
                binding.containerBle.contentMain.txtconnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(morfinBleDevice == null ){
                            showBleDialog();
                        }else if( !morfinAuthBLE.IsDeviceConnected(morfinBleDevice)){
                            showBleDialog();
                        }
                    }
                });
            }
        });

        binding.containerBle.contentMain.ivStatusFp.post(new Runnable() {
            @Override
            public void run() {
                binding.containerBle.contentMain.ivStatusFp.setImageResource(R.drawable.finger_red);
            }
        });

    }

    /**
     * TODO: show Get Timer value Dialog.
     */
    private void showGetTimerValueDialog(TimerValues timerValues) {
        showTimerDialog = new ShowTimerDialog(this);
        showTimerDialog.show();
        showTimerDialog.holder.txtslmode.setText(": " + timerValues.sleepMode + " Min");
        showTimerDialog.holder.txtoffmode.setText(": " + timerValues.offMode + " Min");
        showTimerDialog.holder.txtadvtime.setText(": " + timerValues.advertisement + " Min");
        showTimerDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                showTimerDialog.dismiss();
            }
        });
    }


    /**
     * TODO: show set Timer value Dialog.
     */
    private void showTimerValueDialog(TimerValues timerValues) {
        selectTimerValueDialog = new SelectTimerValueDialog(this);
        selectTimerValueDialog.show();
        selectTimerValueDialog.holder.edtSleepMode.setText("" + timerValues.sleepMode);
        selectTimerValueDialog.holder.edtSleepMode.setHint("Enter value (10–240 min)");
        selectTimerValueDialog.holder.edtOffMode.setText("" + timerValues.offMode);
        selectTimerValueDialog.holder.edtOffMode.setHint("Enter value (10–240 min)");
        selectTimerValueDialog.holder.edtAdver.setText("" + timerValues.advertisement);
        selectTimerValueDialog.holder.edtAdver.setHint("Enter value (10–240 min)");
        selectTimerValueDialog.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectTimerValueDialog.dismiss();
            }
        });

        selectTimerValueDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                try {
                    sleepModeTimerValue = Integer.parseInt(selectTimerValueDialog.holder.edtSleepMode.getText().toString());
                    offModeTimerValue = Integer.parseInt(selectTimerValueDialog.holder.edtOffMode.getText().toString());
                    advertisementTimerValue = Integer.parseInt(selectTimerValueDialog.holder.edtAdver.getText().toString());
                    TimerValues timerValues = new TimerValues();
                    timerValues.sleepMode = sleepModeTimerValue;
                    timerValues.offMode = offModeTimerValue;
                    timerValues.advertisement = advertisementTimerValue;
                    int ret = morfinAuthBLE.SetDeviceTimerValues(timerValues);
                    if (ret == 0) {
                        setLogs("Set Timer Success", false);
                    } else {
                        setLogs("Set Timer Error: " + ret + " (" + morfinAuthBLE.GetErrorDescription(ret) + ")", true);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                selectTimerValueDialog.dismiss();
            }
        });
    }


    /**
     * TODO: menu added method.
     */
    public void addActionItem() {
        try {
            navigationMenuActionArrayList = new ArrayList<>();
            NavigationMenuItem navigationMenuItem;
            try {
                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.action_check_device);
                navigationMenuActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.init);
                navigationMenuActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.SyncCapture);
                navigationMenuActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.StopSyncCapture);
                navigationMenuActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.saveImageble);
                navigationMenuActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.MatchTemplate);
                navigationMenuActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.uninit);
                navigationMenuActionArrayList.add(navigationMenuItem);

                actionAdapter = new ActionAdapter(BleActivity.this, navigationMenuActionArrayList, this);
                binding.menuActionList.setAdapter(actionAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    /**
     * TODO:menu item click.
     */
    public void onItemClick(int position, String item_name) {
        binding.drawerLayout.closeDrawer(Gravity.LEFT);
        clearText();
        switch (position) {
            case 0://sdk version
                String sdkVersion = morfinAuthBLE.GetSDKVersion();
                setLogs("SDK Version : " + sdkVersion, false);
                break;
            case 1://supported device List
                List<String> supportedList = new ArrayList<>();
                int ret = morfinAuthBLE.GetSupportedDeviceList(supportedList);
                if (ret == 0) {
                    StringBuilder str = new StringBuilder();
                    for (String list : supportedList) {
                        if (str.length() != 0) {
                            str.append(", ");
                        }
                        str.append(list);
                    }
                    setLogs("Supported Devices: " + str.toString(), false);
                } else {
                    setLogs("Supported Devices Error: " + ret + " (" + morfinAuthBLE.GetErrorDescription(ret) + ")", true);
                }
                break;
            case 2://select Image Format
                showImageFormatDialog();
                break;
            case 3://select Template Format
                showTemplateFormatDialog();
                break;
            case 4://Set Quality
                showSetQualityDialog();
                break;
            case 5://set Timeout
                showSetTimeoutDialog();
                break;
            case 6://generate sign key
                showGenerateKeyDialog();
                break;
            case 7://View sign key
                showViewKeyDialog();
                break;
            case 8://set key
                showSetKeyDialog();
                break;
        }
    }

    @Override
    /**
     * TODO: menu item click.
     */
    public void onActionClick(int position, String item_name) {
        binding.drawerLayout.closeDrawer(Gravity.LEFT);
        clearText();
        binding.containerBle.contentMain.imgFinger.post(new Runnable() {
            @Override
            public void run() {
                binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
            }
        });
        switch (position) {
            case 0://check device,is device connected
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (morfinBleDevice != null) {
                                boolean ret = morfinAuthBLE.IsDeviceConnected(morfinBleDevice);
                                if (ret) {
                                    binding.containerBle.contentMain.ivStatusFp.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.containerBle.contentMain.ivStatusFp.setImageResource(R.drawable.finger_green);
                                        }
                                    });
                                    setLogs(morfinBleDevice.name + " Device Connected", false);
                                } else {
                                    binding.containerBle.contentMain.ivStatusFp.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.containerBle.contentMain.ivStatusFp.setImageResource(R.drawable.finger_red);
                                        }
                                    });
                                    setLogs("Ble Device Not Connected", true);
                                }
                            } else {
                                setLogs("Ble Device Not Connected", true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            setLogs("Device not found", true);
                        }
                    }
                }).start();
                break;
            case 1://Init
                if (!InitCalled) {
                    // Show progress dialog
                    ProgressDialog progressDialog = new ProgressDialog(BleActivity.this);
                    progressDialog.setMessage("Initializing device...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    InitCalled = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DeviceInfo info = new DeviceInfo();
                                int ret = morfinAuthBLE.InitDevice((clientKey.isEmpty()) ? null : clientKey, info);
                                // Post the result handling to the UI thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ret != 0) {
                                            progressDialog.dismiss(); // Dismiss dialog in all cases
                                            InitCalled = false;
                                            setLogs("Init: " + ret + " (" + morfinAuthBLE.GetErrorDescription(ret) + ")", true);
                                        } else {
                                            progressDialog.dismiss(); // Dismiss dialog in all cases
                                            InitCalled = false;
                                            lastDeviceInfo = info;
                                            setLogs("Init Success", false);
                                            setDeviceInfo(info);
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        InitCalled = false;
                                        setLogs("Device not found", false);
                                    }
                                });
                            }
                        }
                    }).start();
                }
                break;
            case 2://start Capture
                showCaptureFormatDialog(true);
                break;
            case 3://stop Capture
                StopCapture();
                break;
            case 4://Save image
                if (lastDeviceInfo == null) {
                    setLogs("Please run device init first", true);
                    return;
                }
                saveData();
                break;
            case 5://Match Finger
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        matchData();
                    }
                }).start();
                break;
            case 6://UnInit
                UninitDevice();
                break;
        }
    }

    public void UninitDevice(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = morfinAuthBLE.UnInitDevice();
                    isStartCaptureRunning = false;
                    isStopCaptureRunning = false;
                    if (ret == 0) {
                        setLogs("UnInit Success", false);
                    } else {
                        setLogs("UnInit: " + ret + " (" + morfinAuthBLE.GetErrorDescription(ret) + ")", true);
                    }
                    lastDeviceInfo = null;
                    lastCapFingerDataImage = null;
                    setClearDeviceInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    captureThread = null;
                }
            }
        }).start();
    }

    /**
     * TODO: Metch templete method.
     */
    public void matchData() {
        try {
            if (lastCapFingerDataTemplate == null) {
                setLogs("Error : Capture template empty : ", true);
                return;
            }
            if (capCaptureFormatenum == CaptureFormat.FMR_2005 ||
                    capCaptureFormatenum == CaptureFormat.FMR_2011 ||
                    capCaptureFormatenum == CaptureFormat.ANSI_378) {
                int[] matchScore = new int[1];
                setLogs("Capture started put finger on scanner.", false);
                int ret = morfinAuthBLE.MatchTemplate(lastCapFingerDataTemplate, minQuality, timeOut,
                        captureTemplateFormatenum, matchScore);
                if (ret < 0) {
                    setLogs("Error: " + ret + "(" + morfinAuthBLE.GetErrorDescription(ret) + ")", true);
                } else {
                    BleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.finger_blue);
                        }
                    });
                    if (matchScore[0] >= 96) {
                        setLogs("Template matched with score: " + matchScore[0], false);
                    } else {
                        setLogs("Template not matched, score: " + matchScore[0], false);
                    }
                }
            } else {
                setLogs("Capture template empty : ", true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: show Generate Key Dialog.
     */
    private void showGenerateKeyDialog() {
        selectGenerateKeyDialog = new GenerateKeyDialog(this);
        selectGenerateKeyDialog.show();
        selectGenerateKeyDialog.holder.edtKey.setText("");
        selectGenerateKeyDialog.holder.edtKey.setSelection(selectGenerateKeyDialog.holder.edtKey.getText().length());
        selectGenerateKeyDialog.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectGenerateKeyDialog.dismiss();
            }
        });

        selectGenerateKeyDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                try {
                    if (selectGenerateKeyDialog.holder.edtKey.getText().toString().trim().isEmpty()) {
                        Toast.makeText(BleActivity.this, "Key Not Empty", Toast.LENGTH_LONG).show();
                    } else {
                        SignKey = "";
                        SignKey = genClientKey(selectGenerateKeyDialog.holder.edtKey.getText().toString().trim());
                        selectGenerateKeyDialog.dismiss();
                        if (!SignKey.isEmpty()) {
                            Toast.makeText(BleActivity.this, "Key Generated", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (NumberFormatException e) {
                    selectGenerateKeyDialog.dismiss();
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * TODO: Create clint key method.
     */
    private String genClientKey(String clientkey) {
        String ret = "";
        String pubKey = readRawResource(BleActivity.this);
        // Remove header/footer and decode Base64
        try {
            String cleanPem = pubKey
                    .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.decode(cleanPem, Base64.NO_PADDING);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            PublicKey publicKey = kf.generatePublic(spec);


            // Encrypt using RSA OAEP with SHA-256
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT
            );
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);

            byte[] encryptedBytes = null;
            long epochSeconds = System.currentTimeMillis() / 1000;

            String finalKey = epochSeconds + clientkey;
            encryptedBytes = cipher.doFinal(finalKey.getBytes());

            ret = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);

        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException |
                 NoSuchAlgorithmException
                 | InvalidKeySpecException | InvalidAlgorithmParameterException |
                 InvalidKeyException e) {
            e.printStackTrace();
            Toast.makeText(BleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return ret.replaceAll("\\r?\\n", "");
    }

    public String readRawResource(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.public_key);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            return bos.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * TODO: show View Key Dialog.
     */
    private void showViewKeyDialog() {
        viewKeyDialog = new ViewKeyDialog(this);
        viewKeyDialog.show();
        if (SignKey == null) {
            viewKeyDialog.holder.txtKey.setText("Key Not Generated.");
        } else {
            if (SignKey.isEmpty()) {
                viewKeyDialog.holder.txtKey.setText("Key Not Generated.");
            } else {
                viewKeyDialog.holder.txtKey.setText("" + SignKey);
            }
        }
        viewKeyDialog.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                viewKeyDialog.dismiss();
            }
        });

        viewKeyDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("SignKey", SignKey);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(BleActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    /**
     * TODO: show Set Key Dialog method.
     */
    private void showSetKeyDialog() {
        selectKeyDialog = new SetKeyDialog(this);
        selectKeyDialog.show();
        selectKeyDialog.holder.edtKey.setText("" + SignKey);
        selectKeyDialog.holder.edtKey.setSelection(selectKeyDialog.holder.edtKey.getText().length());
        selectKeyDialog.holder.txtClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectKeyDialog.holder.edtKey.setText("");
                clientKey = "";
                SignKey = "";
            }
        });
        selectKeyDialog.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectKeyDialog.dismiss();
            }
        });

        selectKeyDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                if (selectKeyDialog.holder.edtKey.getText().toString().trim().isEmpty()) {
                    Toast.makeText(BleActivity.this, "Key Not Empty", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        clientKey = (selectKeyDialog.holder.edtKey.getText().toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    selectKeyDialog.dismiss();
                }

            }
        });
    }

    /**
     * TODO: Set Device info method.
     */
    private void setDeviceInfo(DeviceInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            /**
             * TODO: Describe method.
             */
            public void run() {
                try {
                    if (info == null)
                        return;
                    binding.containerBle.contentMain.txtMake.setText(getString(R.string.make) + " " + info.Make);
                    binding.containerBle.contentMain.txtModel.setText(getString(R.string.model) + " " + info.Model);
                    binding.containerBle.contentMain.txtSerialNo.setText(getString(R.string.serial_no) + " " + info.SerialNo);
                    binding.containerBle.contentMain.txtWH.setText(getString(R.string.w_h) + " " + info.Width + " / " + info.Height);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * TODO: Clear Device Info.
     */
    private void setClearDeviceInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    binding.containerBle.contentMain.txtMake.setText(getString(R.string.make));
                    binding.containerBle.contentMain.txtModel.setText(getString(R.string.model));
                    binding.containerBle.contentMain.txtSerialNo.setText(getString(R.string.serial_no));
                    binding.containerBle.contentMain.txtWH.setText(getString(R.string.w_h));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * TODO: Start Capture method.
     */
    private void StartCapture() {
        if (lastDeviceInfo == null) {
            setLogs("Please run device init first", true);
            return;
        }
        scannerAction = ScannerAction.Capture;
        StartSyncCapture();
    }

    private Thread captureThread = null;

    /**
     * TODO: Start Sync Capture method.
     */
    private void StartSyncCapture() {
        if (isStartCaptureRunning || (captureThread != null && captureThread.isAlive())) {
            setLogs("Start sync Capture Ret: " + MorfinAuthNative.CAPTURE_ALREADY_STARTED
                    + " (" + morfinAuthBLE.GetErrorDescription(MorfinAuthNative.CAPTURE_ALREADY_STARTED) + ")", true);
            return;
        }
        if (isStopCaptureRunning) {
            return;
        }
        if (lastDeviceInfo == null) {
            setLogs("Please run device init first", true);
            return;
        }

        // Create and show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Capturing fingerprint...");
        progressDialog.setCancelable(false);
        //progressDialog.show();

        isStartCaptureRunning = true;
        captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    binding.containerBle.contentMain.imgFinger.post(new Runnable() {
                        @Override
                        public void run() {
                            binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
                        }
                    });
                    int qty[] = new int[1];
                    int nfiq[] = new int[1];
                    setLogs("Capture started put finger on scanner", false);
                    int ret = morfinAuthBLE.StartCapture(capCaptureFormatenum, minQuality, timeOut, qty, nfiq);
                    if (ret != 0) {
                        binding.containerBle.contentMain.imgFinger.post(new Runnable() {
                            @Override
                            public void run() {
                                binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
                            }
                        });
                        if (ret == -2057) {
                            setLogs("Device Not Connected", true);
                        } else {
                            setLogs("Start Sync Capture Ret: " + ret + " (" + morfinAuthBLE.GetErrorDescription(ret) + ")", true);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    } else {
                        String log = "Capture Success";
                        String message;
                        if (nfiq[0] > 0) {
                            message = "Quality: " + qty[0] + " NFIQ: " + nfiq[0];
                        } else {
                            message = "Quality: " + qty[0];
                        }
                        setLogs(log, false);
                        setTxtStatusMessage(message);
                        if (scannerAction == ScannerAction.Capture) {
                            if (capCaptureFormatenum == CaptureFormat.FMR_2005 ||
                                    capCaptureFormatenum == CaptureFormat.FMR_2011 ||
                                    capCaptureFormatenum == CaptureFormat.ANSI_378) {
                                BleActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.finger_blue);
                                    }
                                });
                                gettemplate(capCaptureFormatenum);
                            } else {
                                int Size = lastDeviceInfo.Width * lastDeviceInfo.Height + 1111;
                                int[] iSize = new int[1];
                                byte[] bImage = new byte[Size];
                                ret = morfinAuthBLE.GetImage(captureImageFormatenum, bImage, iSize);
                                if (ret == 0) {
                                    lastCapFingerDataImage = new byte[Size];
                                    System.arraycopy(bImage, 0, lastCapFingerDataImage, 0,
                                            bImage.length);
                                    if (lastCapFingerDataImage != null) {
                                        Bitmap previewBitmap = BitmapFactory.decodeByteArray(lastCapFingerDataImage, 0,
                                                lastCapFingerDataImage.length);
                                        if (previewBitmap != null) {
                                            BleActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    binding.containerBle.contentMain.imgFinger.setImageBitmap(previewBitmap);
                                                }
                                            });
                                        } else {
                                            BleActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.finger_blue);
                                                }
                                            });
                                        }
                                    } else {
                                        BleActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.finger_blue);
                                            }
                                        });
                                    }
                                } else {
                                    BleActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.finger_blue);
                                        }
                                    });
                                }
                            }
                        }
                    }
                    isStartCaptureRunning = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                } finally {
                    captureThread = null;
                }
            }
        });
        captureThread.start();
    }

    /**
     * TODO: Stop Capture method.
     */
    private void StopCapture() {
        try {
            isStopCaptureRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                        int ret = morfinAuthBLE.StopCapture();
                        isStopCaptureRunning = false;
                        isStartCaptureRunning = false;
                        setLogs("StopCapture: " + ret + " (" + morfinAuthBLE.GetErrorDescription(ret) + ")", false);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            setLogs("Error", true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    /**
     * TODO: Describe method.
     */
    private void clearText() {
        runOnUiThread(new Runnable() {
            public void run() {
                binding.containerBle.contentMain.txtCaptureStatus.setText("");
                binding.containerBle.contentMain.txtStatusMessage.setText("");
            }
        });
    }

    /**
     * TODO: set message on text method.
     */
    private void setLogs(final String logs, boolean isError) {
        binding.containerBle.contentMain.txtCaptureStatus.post(new Runnable() {
            @Override
            public void run() {
                if (isError) {
                    binding.containerBle.contentMain.txtCaptureStatus.setTextColor(Color.parseColor("#FA5757"));
                } else {
                    binding.containerBle.contentMain.txtCaptureStatus.setTextColor(Color.WHITE);
                }
                binding.containerBle.contentMain.txtCaptureStatus.setText(logs);
            }
        });
    }

    /**
     * TODO: set message on text method.
     */
    private void setTxtStatusMessage(final String logs) {
        binding.containerBle.contentMain.txtStatusMessage.post(new Runnable() {
            @Override
            public void run() {
                binding.containerBle.contentMain.txtStatusMessage.setText(logs);
            }
        });
    }

    /**
     * TODO: Save template and Image.
     */
    public void saveData() {
        try {
            int Size = lastDeviceInfo.Width * lastDeviceInfo.Height + 1111;
            int[] iSize = new int[Size];
            byte[] bImage1 = new byte[Size];
            if (capCaptureFormatenum == CaptureFormat.FMR_2005 ||
                    capCaptureFormatenum == CaptureFormat.FMR_2011 ||
                    capCaptureFormatenum == CaptureFormat.ANSI_378) {
                try {
                    byte[] bTemplate = new byte[Size];
                    int ret = morfinAuthBLE.GetTemplate(captureTemplateFormatenum, bTemplate, iSize);
                    byte[] finaldata = new byte[iSize[0]];
                    System.arraycopy(bTemplate, 0, finaldata, 0, iSize[0]);
                    if (ret == 0) {
                        switch (captureTemplateFormatenum) {
                            case FMR_V2005:
                                WriteTemplateFile("ISOTemplate_2005.iso", finaldata);
                                break;
                            case FMR_V2011:
                                WriteTemplateFile("ISOTemplate_2011.iso", finaldata);
                                break;
                            case ANSI_V378:
                                WriteTemplateFile("ANSITemplate_378.iso", finaldata);
                                break;
                        }
                    } else {
                        setLogs("Save Template Ret: " + ret
                                + " (" + morfinAuthBLE.GetErrorDescription(ret) + ")", true);
                    }
                } catch (Exception e) {
                    setLogs("Error Saving Template.", true);
                    e.printStackTrace();
                }
            } else {
                int ret = morfinAuthBLE.GetImage(captureImageFormatenum, bImage1, iSize);
                byte[] bImage = new byte[iSize[0]];
                System.arraycopy(bImage1, 0, bImage, 0, iSize[0]);
                if (ret == 0) {
                    switch (captureImageFormatenum) {
                        case RAW:
                            WriteImageFile("Raw.raw", bImage);
                            break;
                        case BMP:
                            WriteImageFile("Bitmap.bmp", bImage);
                            break;
                        case WSQ:
                            WriteImageFile("WSQ.wsq", bImage);
                            break;
                        case FIR_V2005:
                            WriteImageFile("FIR_2005.iso", bImage);
                            break;
                        case FIR_V2011:
                            WriteImageFile("FIR_2011.iso", bImage);
                            break;
                    }
                } else {
                    setLogs("Save Template Ret: " + ret
                            + " (" + morfinAuthBLE.GetErrorDescription(ret) + ")", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setLogs("Error Saving Image.", true);
        }
    }

    /**
     * TODO: get template method.
     */
    public void gettemplate(CaptureFormat format) {
        try {
            int Size = lastDeviceInfo.Width * lastDeviceInfo.Height + 1111;
            int[] iSize = new int[1];
            try {
                byte[] bTemplate = new byte[Size];
                int ret;
                if (format == CaptureFormat.FMR_2005) {
                    ret = morfinAuthBLE.GetTemplate(TemplateFormat.FMR_V2005, bTemplate, iSize);
                } else if (format == CaptureFormat.FMR_2011) {
                    ret = morfinAuthBLE.GetTemplate(TemplateFormat.FMR_V2011, bTemplate, iSize);
                } else {
                    ret = morfinAuthBLE.GetTemplate(TemplateFormat.ANSI_V378, bTemplate, iSize);
                }
                if (ret == 0) {
                    //setLogs("Error", true);
                    lastCapFingerDataTemplate = new byte[iSize[0]];
                    System.arraycopy(bTemplate, 0, lastCapFingerDataTemplate, 0, iSize[0]);
                } else {
                    setLogs("Get template: " + ret + " (" + morfinAuthBLE.GetErrorDescription(ret) + ")", true);
                }
            } catch (Exception e) {
                setLogs("Error Saving Template.", true);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            setLogs("Error Saving Image.", true);
        }
    }

    /**
     * TODO: Write Image File method.
     */
    private void WriteImageFile(String filename, byte[] bytes) {
        try {
            String path = null; //context.getExternalFilesDir(null) + "//MantraCert//";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                path = getExternalMediaDirs()[0].toString() + "//FingerData//Image";
            } else {
                path = getExternalFilesDir(null) + "//FingerData//Image";
            }
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = path + "//" + filename;
            file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(bytes);
            stream.close();
            setLogs("Image Saved", false);
        } catch (Exception e1) {
            e1.printStackTrace();
            setLogs("Error Saving Image", false);
        }
    }

    /**
     * TODO: Write Template File method.
     */
    private void WriteTemplateFile(String filename, byte[] bytes) {
        try {
            String path = null; //context.getExternalFilesDir(null) + "//MantraCert//";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                path = getExternalMediaDirs()[0].toString() + "//FingerData//Template";
            } else {
                path = getExternalFilesDir(null) + "//FingerData//Template";
            }
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = path + "//" + filename;
            file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(bytes);
            stream.close();
            setLogs("Template Saved", false);
        } catch (Exception e1) {
            e1.printStackTrace();
            setLogs("Error Saving Template", false);
        }
    }

    /**
     * TODO: show Image Format Dialog method.
     */
    private void showImageFormatDialog() {
        selectImageFormatDialogble = new SelectImageFormatDialogble(this);
        selectImageFormatDialogble.show();
        try {
            if (captureImageFormatenum != null) {
                switch (captureImageFormatenum) {
                    case BMP:
                        selectImageFormatDialogble.holder.cbBmp.setChecked(true);
                        break;
                    case RAW:
                        selectImageFormatDialogble.holder.cbRaw.setChecked(true);
                        break;
                    case WSQ:
                        selectImageFormatDialogble.holder.cbWsq.setChecked(true);
                        break;
                    case FIR_V2005:
                        selectImageFormatDialogble.holder.cbFIRV2005.setChecked(true);
                        break;
                    case FIR_V2011:
                        selectImageFormatDialogble.holder.cbFIRV2011.setChecked(true);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectImageFormatDialogble.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectImageFormatDialogble.dismiss();
            }
        });

        selectImageFormatDialogble.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                captureImageFormatenum = ImageFormat.BMP;
                if (selectImageFormatDialogble.holder.cbBmp.isChecked()) {
                    captureImageFormatenum = (ImageFormat.BMP);
                } else if (selectImageFormatDialogble.holder.cbWsq.isChecked()) {
                    captureImageFormatenum = (ImageFormat.WSQ);
                } else if (selectImageFormatDialogble.holder.cbRaw.isChecked()) {
                    captureImageFormatenum = (ImageFormat.RAW);
                } else if (selectImageFormatDialogble.holder.cbFIRV2005.isChecked()) {
                    captureImageFormatenum = (ImageFormat.FIR_V2005);
                } else if (selectImageFormatDialogble.holder.cbFIRV2011.isChecked()) {
                    captureImageFormatenum = (ImageFormat.FIR_V2011);
                }
                selectImageFormatDialogble.dismiss();
            }
        });
    }


    /**
     * TODO: show Capture Format Dialog method.
     */
    private void showCaptureFormatDialog(boolean Show) {
        selectCaptureFormatDialogble = new SelectCaptureFormatDialogble(this);
        selectCaptureFormatDialogble.show();
        if (Show) {
            selectCaptureFormatDialogble.holder.txtSave.setText("Start");
        } else {
            selectCaptureFormatDialogble.holder.txtSave.setText("Save");
        }
        try {
            if (capCaptureFormatenum != null) {
                switch (capCaptureFormatenum) {
                    case FMR_2005:
                        selectCaptureFormatDialogble.holder.cbFmr2005.setChecked(true);
                        break;
                    case FMR_2011:
                        selectCaptureFormatDialogble.holder.cbFmr2011.setChecked(true);
                        break;
                    case ANSI_378:
                        selectCaptureFormatDialogble.holder.cbAnsi378.setChecked(true);
                        break;
                    case FIR_2005:
                        selectCaptureFormatDialogble.holder.cbFIRV2005.setChecked(true);
                        break;
                    case FIR_2011:
                        selectCaptureFormatDialogble.holder.cbFIRV2011.setChecked(true);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectCaptureFormatDialogble.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectCaptureFormatDialogble.dismiss();
            }
        });
        selectCaptureFormatDialogble.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                if (selectCaptureFormatDialogble.holder.cbFmr2005.isChecked()) {
                    capCaptureFormatenum = (CaptureFormat.FMR_2005);
                } else if (selectCaptureFormatDialogble.holder.cbFmr2011.isChecked()) {
                    capCaptureFormatenum = (CaptureFormat.FMR_2011);
                } else if (selectCaptureFormatDialogble.holder.cbAnsi378.isChecked()) {
                    capCaptureFormatenum = (CaptureFormat.ANSI_378);
                } else if (selectCaptureFormatDialogble.holder.cbFIRV2005.isChecked()) {
                    capCaptureFormatenum = (CaptureFormat.FIR_2005);
                } else if (selectCaptureFormatDialogble.holder.cbFIRV2011.isChecked()) {
                    capCaptureFormatenum = (CaptureFormat.FIR_2011);
                }
                if (Show) {
                    StartCapture();
                }
                selectCaptureFormatDialogble.dismiss();
            }
        });
    }


    /**
     * TODO: show Template Format Dialog method.
     */
    private void showTemplateFormatDialog() {
        selectTemplateFormatDialog = new SelectTemplateFormatDialog(this);
        selectTemplateFormatDialog.show();
        try {
            if (captureTemplateFormatenum != null) {
                switch (captureTemplateFormatenum) {
                    case FMR_V2005:
                        selectTemplateFormatDialog.holder.cbFMRV2005.setChecked(true);
                        break;
                    case FMR_V2011:
                        selectTemplateFormatDialog.holder.cbFMRV2011.setChecked(true);
                        break;
                    case ANSI_V378:
                        selectTemplateFormatDialog.holder.cbANSIV378.setChecked(true);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectTemplateFormatDialog.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectTemplateFormatDialog.dismiss();
            }
        });

        selectTemplateFormatDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                if (selectTemplateFormatDialog.holder.cbFMRV2005.isChecked()) {
                    captureTemplateFormatenum = (TemplateFormat.FMR_V2005);
                } else if (selectTemplateFormatDialog.holder.cbFMRV2011.isChecked()) {
                    captureTemplateFormatenum = (TemplateFormat.FMR_V2011);
                } else if (selectTemplateFormatDialog.holder.cbANSIV378.isChecked()) {
                    captureTemplateFormatenum = (TemplateFormat.ANSI_V378);
                }
                selectTemplateFormatDialog.dismiss();
            }
        });
    }

    /**
     * TODO: show Battry Info Dialog.
     */
    private void showBattryInfoDialog(BatteryInformation batteryInformation) {
        showbattryDialog = new ShowbattryDialog(this);
        showbattryDialog.show();
        showbattryDialog.holder.txtchconnected.setText(
                batteryInformation.chargerConnected == 1 ? "Connected" : "Disconnected"
        );
        showbattryDialog.holder.txtchPercentage.setText("" + batteryInformation.batteryChargePercentage + " %");
        showbattryDialog.holder.txtHePercentage.setText("" + batteryInformation.batteryHealthPercentage + " %");
        showbattryDialog.holder.txtTemperature.setText("" + batteryInformation.batteryTemperature + " " + getString(R.string.temperature_celsius));
        showbattryDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                showbattryDialog.dismiss();
            }
        });
    }

    InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            try {
                String input = dest.toString().substring(0, dstart)
                        + source.toString().substring(start, end)
                        + dest.toString().substring(dend);
                if (input.isEmpty()) return null;

                int value = Integer.parseInt(input);
                if (value >= 1 && value <= 100) {
                    return null;
                }
            } catch (NumberFormatException e) {
                // Ignore
                e.printStackTrace();
            }
            return "";
        }
    };

    /**
     * TODO: show SetQuality Dialog method.
     */
    private void showSetQualityDialog() {
        selectQualityDialog = new SelectQualityDialog(this);
        selectQualityDialog.show();
        selectQualityDialog.holder.edtQuality.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(3)});
        selectQualityDialog.holder.edtQuality.setText("" + minQuality);
        selectQualityDialog.holder.edtQuality.setSelection(selectQualityDialog.holder.edtQuality.getText().length());
        selectQualityDialog.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectQualityDialog.dismiss();
            }
        });

        selectQualityDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                try {
                    minQuality = Integer.parseInt(selectQualityDialog.holder.edtQuality.getText().toString());
                    if (minQuality > 100 || (minQuality < 1)) {
                        selectQualityDialog.holder.edtQuality.setError(getString(R.string.quality_should_be_between_1_100));
                        return;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                selectQualityDialog.dismiss();
            }
        });
    }

    /**
     * TODO: show Set Timeout Dialog method.
     */
    private void showSetTimeoutDialog() {
        selectTimeoutDialog = new SelectTimeoutDialog(this);
        selectTimeoutDialog.show();
        selectTimeoutDialog.holder.txttimeout.setText(R.string.timeout_sec + " ");
        selectTimeoutDialog.holder.edtTimeout.setHint(R.string.enter_timeout_1_30_sec);
        selectTimeoutDialog.holder.edtTimeout.setInputType(InputType.TYPE_CLASS_NUMBER);
        selectTimeoutDialog.holder.edtTimeout.setFilters(new InputFilter[]{
                new InputFilterMinMax(1, 30)
        });
        selectTimeoutDialog.holder.edtTimeout.setText("" + timeOut);
        selectTimeoutDialog.holder.edtTimeout.setSelection(selectTimeoutDialog.holder.edtTimeout.getText().length());
        selectTimeoutDialog.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectTimeoutDialog.dismiss();
            }
        });

        selectTimeoutDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                try {
                    timeOut = Integer.parseInt(selectTimeoutDialog.holder.edtTimeout.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                selectTimeoutDialog.dismiss();
            }
        });
    }

    @Override
    /**
     * TODO: On Device Discovered and put on list using bluetooth.
     */
    public void OnDeviceDiscovered(MorfinBleDevice morfinBleDevice) {
        runOnUiThread(() -> addDevice(morfinBleDevice));
    }


    @Override
    /**
     * TODO: On Device Connection Status.
     */
    public void OnDeviceConnectionStatus(MorfinBleDevice morfinBleDevice, MorfinBleState morfinDeviceState) {
        clearText();
        if (morfinDeviceState == MorfinBleState.CONNECTED) {
            this.morfinBleDevice = morfinBleDevice;
            BleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUiForConnectedDevice(morfinBleDevice);
                }
            });
        } else if (morfinDeviceState == MorfinBleState.CONNECTING) {
            // todo
        } else if (morfinDeviceState == MorfinBleState.DISCONNECTED) {
            this.morfinBleDevice = null;
            ClearBleview();
        } else if (morfinDeviceState == MorfinBleState.DISCONNECTING) {
            // todo
        } else if (morfinDeviceState == MorfinBleState.OUT_OF_RANGE) {
            // todo
            setLogs("Ble Device Out Of Range", true);
            ClearBleview();
        }
    }

    @Override
    /**
     * TODO: send all notification dialog.
     */
    public void MorfinDeviceStatusNotification(MorfinNotifications morfinNotifications) {
        String message;
        if (morfinNotifications == MorfinNotifications.BatteryDisconnected) {
            message = "Battery Disconnected";
        } else if (morfinNotifications == MorfinNotifications.BatteryTemperatureAbnormal) {
            message = "Battery Temperature Abnormal";
        } else if (morfinNotifications == MorfinNotifications.ChargingInputFault) {
            message = "Charging Input Fault";
        } else if (morfinNotifications == MorfinNotifications.OTGFault) {
            message = "OTG Fault";
        } else if (morfinNotifications == MorfinNotifications.ChargingTimerExpiration) {
            message = "Charging Timer Expiration";
        } else if (morfinNotifications == MorfinNotifications.BatteryOverPower) {
            message = "Battery Over Power";
        } else if (morfinNotifications == MorfinNotifications.Charging) {
            message = "Battery Charging";
        } else if (morfinNotifications == MorfinNotifications.Discharging) {
            message = "Battery Discharging";
        } else if (morfinNotifications == MorfinNotifications.ChargingRequiredWarning) {
            message = "Charger Required Warning";
        } else if (morfinNotifications == MorfinNotifications.BatteryPowerIsCriticalLow) {
            message = "Battery Power Is Critical Low";
        } else if (morfinNotifications == MorfinNotifications.DeviceDisconnecting) {
            message = "Device Disconnecting";
        } else if (morfinNotifications == MorfinNotifications.DeviceUninitialized) {
            message = "Device Uninitialized";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isStartCaptureRunning = false;
                    isStopCaptureRunning = false;
                    lastDeviceInfo = null;
                    lastCapFingerDataImage = null;
                    setClearDeviceInfo();
                }
            });
        } else if (morfinNotifications == MorfinNotifications.ChargerPlugged) {
            message = "Charger Plugged";
        } else if (morfinNotifications == MorfinNotifications.ChargerUnplugged) {
            message = "Charger Unplugged";
        } else {
            message = null;
        }
        Log.e("notification","message :" + message);
        if (message != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
                }
            });
            if (morfinNotifications == MorfinNotifications.DeviceUninitialized) {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       UninitDevice();
                       clearText();
                       binding.containerBle.contentMain.imgFinger.post(new Runnable() {
                           @Override
                           public void run() {
                               binding.containerBle.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
                           }
                       });
                   }
               });
            }
          /*  // Show the message immediately
            setLogs(message, true);
            // Schedule clearing or updating after 5 seconds
            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                setLogs("", true); // Clear or set to something else
            }, 10000);*/
        }
    }

    private void addDevice(MorfinBleDevice device) {
        // Avoid duplicates by checking address (or other unique ID)
        scannedDevices.add(device);
        deviceadapter.notifyItemInserted(scannedDevices.size() - 1);
    }

    /**
     * TODO: show nearby ble device in this dialog.
     */
    private void showBleDialog() {


        // Create custom dialog
        final Dialog bleDialog = new Dialog(this);
        bleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bleDialog.setContentView(R.layout.custom_ble_dialog);
        bleDialog.setCancelable(true);

        // Set custom window attributes
        Window window = bleDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }

        // Initialize views
        TextView title = bleDialog.findViewById(R.id.dialog_title);
        RecyclerView recyclerView = bleDialog.findViewById(R.id.recyclerView);
        ProgressBar progressBar = bleDialog.findViewById(R.id.scan_progress);
        TextView emptyView = bleDialog.findViewById(R.id.empty_view);
        ImageButton closeBtn = bleDialog.findViewById(R.id.close_btn);

        title.setText("Select BLE Device");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add divider between items
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Initialize adapter
        deviceadapter = new RecyclerViewAdapter(scannedDevices, device -> {
            // Dismiss the selection dialog
            bleDialog.dismiss();

            // Show connecting progress
            ProgressDialog connectingDialog = new ProgressDialog(this);
            connectingDialog.setMessage("Connecting to " + device.name);
            connectingDialog.setCancelable(false);
            connectingDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int ret1 = morfinAuthBLE.StopDiscover();
                }
            }).start();

            // Stop discovery and attempt connection
            new Thread(() -> {
                //long startT = System.currentTimeMillis();
                if(morfinBleDevice != null){
                    morfinBleDevice = null;
                }
                int ret = morfinAuthBLE.ConnectDevice(device);
                //Log.d("BleActivity", "Connection result: " + ret + "Time :" +(System.currentTimeMillis()-startT));
                runOnUiThread(() -> {
                    connectingDialog.dismiss();
                    if (ret == 0) {
                       /* // Connection successful
                        updateUiForConnectedDevice(device);
                        Toast.makeText(this, "Connected to " + device.name, Toast.LENGTH_SHORT).show();*/
                    } else {
                        // Connection failed
                        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
                        //showBleDialog(); // Reopen dialog if failed
                        // Clear scanned devices
                        if (scannedDevices != null) {
                            scannedDevices.clear();
                        }
                    }
                });
            }).start();
        });

        recyclerView.setAdapter(deviceadapter);

        // Handle empty state
        deviceadapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                emptyView.setVisibility(deviceadapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });

        // Close button click
        closeBtn.setOnClickListener(v -> {
            if (scannedDevices != null) {
                scannedDevices.clear();
            }
            bleDialog.dismiss();
        });

        // Start scanning when dialog shows
        bleDialog.setOnShowListener(dialog -> {
            progressBar.setVisibility(View.VISIBLE);
            emptyView.setText(R.string.scanning_for_devices);

            new Handler().postDelayed(() -> {
                int ret = morfinAuthBLE.DiscoverDevices();
                // Stop progress after 10 seconds
                new Handler().postDelayed(() -> {
                    progressBar.setVisibility(View.GONE);
                    emptyView.setText(R.string.no_devices_found);
                }, 10000);
            }, 300);
        });

        bleDialog.show();
    }

    /**
     * TODO: update a device connected name.
     */
    private void updateUiForConnectedDevice(MorfinBleDevice device) {
        if (device.name != null) {
            binding.containerBle.contentMain.ivStatusFp.setImageResource(R.drawable.finger_green);
            binding.containerBle.contentMain.txtconnect.setText(device.name);
        }
        // Clear scanned devices
        if (scannedDevices != null) {
            scannedDevices.clear();
        }
        // Disable connect button
        //binding.containerBle.contentMain.txtconnect.setOnClickListener(null);
    }
}
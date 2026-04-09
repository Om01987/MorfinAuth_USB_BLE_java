package com.mantra.morfinauthsample;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.BuildConfig;
import com.mantra.morfinauth.DeviceInfo;
import com.mantra.morfinauth.MorfinAuth;
import com.mantra.morfinauth.MorfinAuthNative;
import com.mantra.morfinauth.MorfinAuth_Callback;
import com.mantra.morfinauth.enums.DeviceDetection;
import com.mantra.morfinauth.enums.DeviceModel;
import com.mantra.morfinauth.enums.FingerPosition;
import com.mantra.morfinauth.enums.ImageFormat;
import com.mantra.morfinauth.enums.LogLevel;
import com.mantra.morfinauth.enums.TemplateFormat;
import com.mantra.morfinauthsample.adapter.ActionAdapter;
import com.mantra.morfinauthsample.adapter.MenuAdapter;
import com.mantra.morfinauthsample.adapter.NavigationMenuItem;
import com.mantra.morfinauthsample.adapter.SelectorAdapter;
import com.mantra.morfinauthsample.databinding.ActivityMainBinding;
import com.mantra.morfinauthsample.dialog.GenerateKeyDialog;
import com.mantra.morfinauthsample.dialog.SelectImageFormatDialog;
import com.mantra.morfinauthsample.dialog.SelectQualityDialog;
import com.mantra.morfinauthsample.dialog.SelectTemplateFormatDialog;
import com.mantra.morfinauthsample.dialog.SelectTimeoutDialog;
import com.mantra.morfinauthsample.dialog.SetKeyDialog;
import com.mantra.morfinauthsample.dialog.ViewKeyDialog;

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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class MainActivity extends AppCompatActivity
        implements MenuAdapter.ItemClickListener
        , ActionAdapter.ActionClickListener
        , NavigationView.OnNavigationItemSelectedListener
        , MorfinAuth_Callback {

    public ActivityMainBinding binding;

    private MenuAdapter menuAdapter;
    private ActionAdapter actionAdapter;
    ArrayList<NavigationMenuItem> navigationMenuItemArrayList, navigationMenuActionArrayList;
    private MorfinAuth morfinAuth;
    ArrayList<String> modelName;
    SelectorAdapter adapter;
    private static final String strSelect = "No Device";
    private byte[] lastCapFingerDataTemplate;
    private DeviceInfo lastDeviceInfo;
    ImageFormat captureImageFormatenum;
    TemplateFormat captureTemplateFormatenum;
    private boolean isStartCaptureRunning;
    private boolean isStopCaptureRunning;

    private enum ScannerAction {
        Capture, MatchISO, MatchAnsi
    }

    private ScannerAction scannerAction = ScannerAction.Capture;
    private SelectImageFormatDialog selectImageFormatDialog;
    private SelectTemplateFormatDialog selectTemplateFormatDialog;
    private SelectQualityDialog selectQualityDialog;
    private SetKeyDialog selectKeyDialog;
    private GenerateKeyDialog selectGenerateKeyDialog;
    private ViewKeyDialog viewKeyDialog;
    private SelectTimeoutDialog selectTimeoutDialog;
    private String clientKey = "";
    private String SignKey = "";
    public static long lastClickTime = 0;
    public static int ClickThreshold = 1000;
    int minQuality = 60;
    int timeOut = 10000;

    public boolean InitCalled = false;

    @Override
    /**
     * TODO: On create Method for run on app launch.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.container.contentMain.txtApp.setText(getString(R.string.app_name) + " (" + BuildConfig.VERSION_NAME + ")");

        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(MainActivity.this);
        binding.menuitemList.setLayoutManager(mLayoutManager1);
        addMenuItem();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
        binding.menuActionList.setLayoutManager(mLayoutManager);
        addActionItem();

        binding.navView.setCheckedItem(R.id.nav_home);
        binding.navView.setNavigationItemSelectedListener(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        modelName = new ArrayList<>();
        modelName.add(strSelect);
        adapter = new SelectorAdapter(this, modelName);
        binding.container.contentMain.spDeviceName.setAdapter(adapter);

        morfinAuth = new MorfinAuth(this, this);
        String file = getExternalFilesDir(null).toString();
        morfinAuth.SetLogProperties(file, LogLevel.OFF);
        captureImageFormatenum = (ImageFormat.BMP);
        captureTemplateFormatenum = (TemplateFormat.FMR_V2005);
        clearText();
        setonClick();
    }


    /**
     * TODO: onclick listener for navigation.
     */
    private void setonClick() {
        binding.container.contentMain.ivFpLogo.setOnClickListener(view -> onLogoClicked());
        binding.container.contentMain.rlSideMenu.setOnClickListener(view -> onViewClicked());
        binding.ivSettingMenu.setOnClickListener(view -> onCloseMenuClicked());
    }

    public void onLogoClicked() {
        binding.drawerLayout.openDrawer(Gravity.LEFT);
    }

    public void onViewClicked() {
        binding.drawerLayout.openDrawer(Gravity.LEFT);
    }

    public void onCloseMenuClicked() {
        binding.drawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }

    @Override
    protected void onStop() {
        if (isStartCaptureRunning) {
            StopCapture();
        }
        isStartCaptureRunning = false;
        super.onStop();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (isStartCaptureRunning) {
            StopCapture();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isStartCaptureRunning) {
            StopCapture();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            isStartCaptureRunning = false;
            isStopCaptureRunning = false;
            morfinAuth.Uninit();
            morfinAuth.Dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * TODO: add Menu on navigation.
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

                menuAdapter = new MenuAdapter(MainActivity.this, navigationMenuItemArrayList, this);
                binding.menuitemList.setAdapter(menuAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * TODO: add Menu on navigation.
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
                navigationMenuItem.menu_name = getString(R.string.AutoCapture);
                navigationMenuActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.saveImage);
                navigationMenuActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.MatchFinger);
                navigationMenuActionArrayList.add(navigationMenuItem);

                navigationMenuItem = new NavigationMenuItem();
                navigationMenuItem.menu_name = getString(R.string.uninit);
                navigationMenuActionArrayList.add(navigationMenuItem);

                actionAdapter = new ActionAdapter(MainActivity.this, navigationMenuActionArrayList, this);
                binding.menuActionList.setAdapter(actionAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * TODO: navigation menu Click.
     */
    @Override
    public void onItemClick(int position, String item_name) {
        binding.drawerLayout.closeDrawer(Gravity.LEFT);
        clearText();
        switch (position) {
            case 0://sdk version
                String sdkVersion = morfinAuth.GetSDKVersion();
                setLogs("SDK Version : " + sdkVersion, false);
                break;
            case 1://supported device List
                List<String> supportedList = new ArrayList<>();
                int ret = morfinAuth.GetSupportedDevices(supportedList);
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
                    setLogs("Supported Devices Error: " + ret + " (" + morfinAuth.GetErrorMessage(ret) + ")", true);
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

    /**
     * TODO: show Generate key Dialog.
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
                        Toast.makeText(MainActivity.this, "Key Not Empty", Toast.LENGTH_LONG).show();
                    } else {
                        SignKey = "";
                        SignKey = genClientKey(selectGenerateKeyDialog.holder.edtKey.getText().toString().trim());
                        selectGenerateKeyDialog.dismiss();
                        if(!SignKey.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Key Generated", Toast.LENGTH_LONG).show();
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
     * TODO: show View key Dialog.
     */
    private void showViewKeyDialog() {
        viewKeyDialog = new ViewKeyDialog(this);
        viewKeyDialog.show();
        if(SignKey == null){
            viewKeyDialog.holder.txtKey.setText("Key Not Generated.");
        }else{
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
                Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * TODO: navigation Menu Click.
     */
    @Override
    public void onActionClick(int position, String item_name) {
        binding.drawerLayout.closeDrawer(Gravity.LEFT);
        clearText();
        binding.container.contentMain.imgFinger.post(new Runnable() {
            @Override
            public void run() {
                binding.container.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
            }
        });
        switch (position) {
            case 0://check device,is device connected
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String device = binding.container.contentMain.spDeviceName.getSelectedItem().toString();
                            boolean ret = morfinAuth.IsDeviceConnected(DeviceModel.valueFor(device));
                            if (ret) {
                                binding.container.contentMain.ivStatusFp.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.container.contentMain.ivStatusFp.setImageResource(R.drawable.finger_green);
                                    }
                                });
                                setLogs("Device Connected", false);
                            } else {
                                binding.container.contentMain.ivStatusFp.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.container.contentMain.ivStatusFp.setImageResource(R.drawable.finger_red);
                                    }
                                });
                                setLogs("Device Not Connected", true);
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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            InitCalled = true;
                            try {
                                String device = binding.container.contentMain.spDeviceName.getSelectedItem().toString();
                                DeviceInfo info = new DeviceInfo();
                                int ret = morfinAuth.Init(DeviceModel.valueFor(device), (clientKey.isEmpty()) ? null : clientKey, info);
                                if (ret != 0) {
                                    InitCalled = false;
                                    setLogs("Init: " + ret + " (" + morfinAuth.GetErrorMessage(ret) + ")", true);
                                } else {
                                    InitCalled = false;
                                    lastDeviceInfo = info;
                                    setLogs("Init Success", false);
                                    setDeviceInfo(info);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                InitCalled = false;
                                setLogs("Device not found", false);
                            }
                        }
                    }).start();
                }
                break;
            case 2://start Capture
                if (lastDeviceInfo == null) {
                    setLogs("Please run device init first", true);
                    return;
                }
                if (!morfinAuth.IsCaptureRunning()) {
                    scannerAction = ScannerAction.Capture;
                    StartCapture();
                } else {
                    setLogs("Capture Ret: " + MorfinAuthNative.CAPTURE_ALREADY_STARTED
                            + " (" + morfinAuth.GetErrorMessage(MorfinAuthNative.CAPTURE_ALREADY_STARTED) + ")", true);
                }
                break;
            case 3://stop Capture
                StopCapture();
                break;
            case 4://Auto Capture
                if (lastDeviceInfo == null) {
                    setLogs("Please run device init first", true);
                    return;
                }
                if (!morfinAuth.IsCaptureRunning()) {
                    scannerAction = ScannerAction.Capture;
                    StartSyncCapture();
                } else {
                    setLogs("Capture Ret: " + MorfinAuthNative.CAPTURE_ALREADY_STARTED
                            + " (" + morfinAuth.GetErrorMessage(MorfinAuthNative.CAPTURE_ALREADY_STARTED) + ")", true);
                }
                break;
            case 5://Save image
                if (lastDeviceInfo == null) {
                    setLogs("Please run device init first", true);
                    return;
                }
                saveData();
                break;
            case 6://Match Finger
                if (lastDeviceInfo == null) {
                    setLogs("Please run device init first", true);
                    return;
                }
                if (lastCapFingerDataTemplate == null) {
                    setLogs("Please capture finger for matching", true);
                    return;
                }
                scannerAction = ScannerAction.MatchISO;
                StartSyncCapture();
                break;
            case 7://UnInit
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int ret = morfinAuth.Uninit();
                            isStartCaptureRunning = false;
                            isStopCaptureRunning = false;
                            if (ret == 0) {
                                setLogs("UnInit Success", false);
                            } else {
                                setLogs("UnInit: " + ret + " (" + morfinAuth.GetErrorMessage(ret) + ")", true);
                            }
                            lastDeviceInfo = null;
                            lastCapFingerDataTemplate = null;
                            setClearDeviceInfo();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            captureThread = null;
                        }
                    }
                }).start();
                break;
        }
    }

    /**
     * TODO: Set Device info Click.
     */
    private void setDeviceInfo(DeviceInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (info == null)
                        return;
                    binding.container.contentMain.txtMake.setText(getString(R.string.make) + " " + info.Make);
                    binding.container.contentMain.txtModel.setText(getString(R.string.model) + " " + info.Model);
                    binding.container.contentMain.txtSerialNo.setText(getString(R.string.serial_no) + " " + info.SerialNo);
                    binding.container.contentMain.txtWH.setText(getString(R.string.w_h) + " " + info.Width + " / " + info.Height);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * TODO:  Device info clear.
     */
    private void setClearDeviceInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    binding.container.contentMain.txtMake.setText(getString(R.string.make));
                    binding.container.contentMain.txtModel.setText(getString(R.string.model));
                    binding.container.contentMain.txtSerialNo.setText(getString(R.string.serial_no));
                    binding.container.contentMain.txtWH.setText(getString(R.string.w_h));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * TODO:  Start Capture method.
     */
    private void StartCapture() {
        if (isStartCaptureRunning) {
            setLogs("StartCapture Ret: " + MorfinAuthNative.CAPTURE_ALREADY_STARTED
                    + " (" + morfinAuth.GetErrorMessage(MorfinAuthNative.CAPTURE_ALREADY_STARTED) + ")", true);
            return;
        }
        if (isStopCaptureRunning) {
            return;
        }
        if (lastDeviceInfo == null) {
            setLogs("Please run device init first", true);
            return;
        }
        isStartCaptureRunning = true;
        try {
            binding.container.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
            int ret = morfinAuth.StartCapture(minQuality, timeOut);
            if (ret != 0) {
                isStartCaptureRunning = false;
            }
            setLogs("StartCapture Ret: " + ret + " (" + morfinAuth.GetErrorMessage(ret) + ")", ret == 0 ? false : true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Thread captureThread = null;

    /**
     * TODO:  Auto Capture method.
     */
    private void StartSyncCapture() {
        if (isStartCaptureRunning || (captureThread != null && captureThread.isAlive())) {
            setLogs("Start sync Capture Ret: " + MorfinAuthNative.CAPTURE_ALREADY_STARTED
                    + " (" + morfinAuth.GetErrorMessage(MorfinAuthNative.CAPTURE_ALREADY_STARTED) + ")", true);
            return;
        }
        if (isStopCaptureRunning) {
            return;
        }
        if (lastDeviceInfo == null) {
            setLogs("Please run device init first", true);
            return;
        }
        isStartCaptureRunning = true;
        captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    binding.container.contentMain.imgFinger.post(new Runnable() {
                        @Override
                        public void run() {
                            binding.container.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
                        }
                    });
                    int qty[] = new int[1];
                    int nfiq[] = new int[1];
                    setLogs("Auto Capture Started", false);
                    int ret = morfinAuth.AutoCapture(minQuality, timeOut, qty, nfiq);
                    if (ret != 0) {
                        binding.container.contentMain.imgFinger.post(new Runnable() {
                            @Override
                            public void run() {
                                binding.container.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
                            }
                        });
                        if (ret == -2057) {
                            setLogs("Device Not Connected", true);
                        } else {
                            setLogs("Start Sync Capture Ret: " + ret + " (" + morfinAuth.GetErrorMessage(ret) + ")", true);
                        }
                    } else {
                        String log = "Capture Success";
                        String message = "Quality: " + qty[0] + " NFIQ: " + nfiq[0];
                        setLogs(log, false);
                        setTxtStatusMessage(message);
                        ShowFinalFinger();
                        if (scannerAction == ScannerAction.Capture) {
                            int Size = lastDeviceInfo.Width * lastDeviceInfo.Height + 1111;
                            byte[] bTemplateData = new byte[Size];
                            int[] tSize = new int[Size];
                            ret = morfinAuth.GetTemplate(bTemplateData, tSize, captureTemplateFormatenum);
                            if (ret == 0) {
                                lastCapFingerDataTemplate = new byte[Size];
                                System.arraycopy(bTemplateData, 0, lastCapFingerDataTemplate, 0,
                                        bTemplateData.length);
                            } else {
                                setLogs(morfinAuth.GetErrorMessage(ret), true);
                            }
                        }
                        if (scannerAction.equals(ScannerAction.MatchISO) || scannerAction.equals(ScannerAction.MatchAnsi)) {
                            matchData();
                        }
                    }
                    isStartCaptureRunning = false;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    captureThread = null;
                }
            }
        });
        captureThread.start();
    }

    /**
     * TODO:  stop Capture method.
     */
    private void StopCapture() {
        try {
            isStopCaptureRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int ret = morfinAuth.StopCapture();
                    isStopCaptureRunning = false;
                    isStartCaptureRunning = false;
                    setLogs("StopCapture: " + ret + " (" + morfinAuth.GetErrorMessage(ret) + ")", false);
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

    private void clearText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.container.contentMain.txtCaptureStatus.setText("");
                binding.container.contentMain.txtStatusMessage.setText("");
            }
        });
    }

    private void setLogs(final String logs, boolean isError) {
        binding.container.contentMain.txtCaptureStatus.post(new Runnable() {
            @Override
            public void run() {
                if (isError) {
                    binding.container.contentMain.txtCaptureStatus.setTextColor(Color.RED);
                } else {
                    binding.container.contentMain.txtCaptureStatus.setTextColor(Color.WHITE);
                }
                binding.container.contentMain.txtCaptureStatus.setText(logs);
            }
        });
    }

    private void setTxtStatusMessage(final String logs) {
        binding.container.contentMain.txtStatusMessage.post(new Runnable() {
            @Override
            public void run() {
                binding.container.contentMain.txtStatusMessage.setText(logs);
            }
        });
    }

    /**
     * TODO:  usb Device connect and disconnect method.
     */
    @Override
    public void OnDeviceDetection(String DeviceName, DeviceDetection detection) {
        isStartCaptureRunning = false;
        isStopCaptureRunning = false;
        if (detection == DeviceDetection.CONNECTED) {
            if (DeviceName != null) {
                binding.container.contentMain.ivStatusFp.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.container.contentMain.ivStatusFp.setImageResource(R.drawable.finger_green);
                    }
                });
                boolean exist = false;
                for (String string : modelName) {
                    if (string.equals(DeviceName)) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    modelName.add(DeviceName);
                    modelName.remove(strSelect);
                }
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setLogs("Device connected", false);
        } else if (detection == DeviceDetection.DISCONNECTED) {
            try {
                lastCapFingerDataTemplate = null;
                lastDeviceInfo = null;
                setLogs("Device Not Connected", true);
                setTxtStatusMessage("");
                binding.container.contentMain.ivStatusFp.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.container.contentMain.ivStatusFp.setImageResource(R.drawable.finger_red);
                    }
                });
                try {
                    morfinAuth.Uninit();
                    for (String temp : modelName) {
                        if (temp.equals(DeviceName)) {
                            modelName.remove(temp);
                            if (modelName.size() == 0) {
                                modelName.add(strSelect);
                            }
                            break;
                        }
                    }
                    try {
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setClearDeviceInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                binding.container.contentMain.imgFinger.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.container.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
                    }
                });
            }
        }
    }

    /**
     * TODO:  preview method.
     */
    @Override
    public void OnPreview(int errorCode, int quality, byte[] image) {
        try {
            if (errorCode == 0 && image != null) {
                Bitmap previewBitmap = BitmapFactory.decodeByteArray(image, 0,
                        image.length);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.container.contentMain.imgFinger.setImageBitmap(previewBitmap);
                    }
                });
                setLogs("Preview Quality: " + quality, false);
            } else {
                if (errorCode == -2057) {
                    setLogs("Device Not Connected", true);
                } else {
                    setLogs("Preview Error Code: " + errorCode + " (" + morfinAuth.GetErrorMessage(errorCode) + ")", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO:  final Complete method.
     */
    @Override
    public void OnComplete(int errorCode, int Quality, int NFIQ) {
        try {
            isStartCaptureRunning = false;
            if (errorCode == 0) {
                String log = "Capture Success";
                String quality = "Quality: " + Quality + " NFIQ: " + NFIQ;
                setLogs(log, false);
                setTxtStatusMessage(quality);
                ShowFinalFinger();
                if (scannerAction == ScannerAction.Capture) {
                    int Size = lastDeviceInfo.Width * lastDeviceInfo.Height + 1111;
                    byte[] bTemplateData = new byte[Size];
                    int[] tSize = new int[Size];
                    int ret = morfinAuth.GetTemplate(bTemplateData, tSize, captureTemplateFormatenum);
                    if (ret == 0) {
                        lastCapFingerDataTemplate = new byte[Size];
                        System.arraycopy(bTemplateData, 0, lastCapFingerDataTemplate, 0,
                                bTemplateData.length);
                    } else {
                        setLogs(morfinAuth.GetErrorMessage(ret), true);
                    }
                }
            } else {
                setTxtStatusMessage("");
                binding.container.contentMain.imgFinger.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.container.contentMain.imgFinger.setImageResource(R.drawable.bg_clor_white);
                    }
                });
                if (errorCode == -2057) {
                    setLogs("Device Not Connected", true);
                } else {
                    setLogs("CaptureComplete: " + errorCode + " (" + morfinAuth.GetErrorMessage(errorCode) + ")", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO:  show a final finger in image.
     */
    private void ShowFinalFinger() {
        int Size = lastDeviceInfo.Width * lastDeviceInfo.Height + 1111;
        int[] iSize = new int[1];
        byte[] bImage1 = new byte[Size];
        int ret = morfinAuth.GetImage(bImage1, iSize, 1, ImageFormat.BMP);
        byte[] bImage = new byte[iSize[0]];
        System.arraycopy(bImage1, 0, bImage, 0, iSize[0]);
        Bitmap previewBitmap = BitmapFactory.decodeByteArray(bImage, 0,
                bImage.length);
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.container.contentMain.imgFinger.setImageBitmap(previewBitmap);
            }
        });
    }


    /**
     * TODO:  finger position show in message .
     */
    @Override
    public void OnFingerPosition(int errorCode, int position) {
        if (position == FingerPosition.POSTION_OK.getValue()) {
            setTxtStatusMessage("POSITION OK");
        } else if (position == FingerPosition.POSTION_LEFT.getValue()) {
            setTxtStatusMessage("POSITION LEFT");
        } else if (position == FingerPosition.POSTION_RIGHT.getValue()) {
            setTxtStatusMessage("POSITION RIGHT");
        } else if (position == FingerPosition.POSTION_TOP.getValue()) {
            setTxtStatusMessage("POSITION TOP");
        } else if (position == FingerPosition.POSTION_NOT_IN_BOTTOM.getValue()) {
            setTxtStatusMessage("POSITION NOT IN BOTTOM");
        } else if (position == FingerPosition.POSTION_NOT_OK.getValue()) {
            setTxtStatusMessage("POSITION NOT OK");
        } else if (position == FingerPosition.POSTION_PLACE_FINGER.getValue()) {
            setTxtStatusMessage("PLACE FINGER ON THE SENSOR");
        }
    }

    /**
     * TODO:  save data like image and template .
     */
    public void saveData() {
        try {
            int Size = lastDeviceInfo.Width * lastDeviceInfo.Height + 1111;
            int[] iSize = new int[1];
            byte[] bImage1 = new byte[Size];
            int ret = morfinAuth.GetImage(bImage1, iSize, 1, captureImageFormatenum);
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
                    case JPEG2000:
                        WriteImageFile("JPEG2000.jp2", bImage);
                        break;
                    case WSQ:
                        WriteImageFile("WSQ.wsq", bImage);
                        break;
                    case FIR_V2005:
                        WriteImageFile("FIR_2005.iso", bImage);
                        break;
                    case FIR_WSQ_V2005:
                        WriteImageFile("FIR_WSQ_2005.iso", bImage);
                        break;
                    case FIR_V2011:
                        WriteImageFile("FIR_2011.iso", bImage);
                        break;
                    case FIR_WSQ_V2011:
                        WriteImageFile("FIR_WSQ_2011.iso", bImage);
                        break;
                    case FIR_JPEG2000_V2005:
                        WriteImageFile("FIR_JPEG2000_2005.iso", bImage);
                        break;
                    case FIR_JPEG2000_V2011:
                        WriteImageFile("FIR_JPEG2000_2011.iso", bImage);
                        break;
                }
                try {
                    byte[] bTemplate = new byte[Size];
                    ret = morfinAuth.GetTemplate(bTemplate, iSize, captureTemplateFormatenum);
                    byte[] finaldata = new byte[iSize[0]];
                    System.arraycopy(bTemplate, 0, finaldata, 0, iSize[0]);
                    if (ret == 0) {
                        switch (captureTemplateFormatenum) {
                            case FMR_V2005:
                                WriteTemplateFile("ISOTemplate_2005.iso", finaldata);
                                break;
                            case FMR_V2011:
                                WriteTemplateFile("ISOTemplate_20011.iso", finaldata);
                                break;
                            case ANSI_V378:
                                WriteTemplateFile("ANSITemplate_378.iso", finaldata);
                                break;
                        }
                    } else {
                        setLogs("Save Template Ret: " + ret
                                + " (" + morfinAuth.GetErrorMessage(ret) + ")", true);
                    }
                } catch (Exception e) {
                    setLogs("Error Saving Template.", true);
                    e.printStackTrace();
                }
            } else {
                setLogs("Save Template Ret: " + ret
                        + " (" + morfinAuth.GetErrorMessage(ret) + ")", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setLogs("Error Saving Image.", true);
        }
    }

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

    private void showImageFormatDialog() {
        selectImageFormatDialog = new SelectImageFormatDialog(this);
        selectImageFormatDialog.show();
        try {
            if (captureImageFormatenum != null) {
                switch (captureImageFormatenum) {
                    case BMP:
                        selectImageFormatDialog.holder.cbBmp.setChecked(true);
                        break;
                    case RAW:
                        selectImageFormatDialog.holder.cbRaw.setChecked(true);
                        break;
                    case WSQ:
                        selectImageFormatDialog.holder.cbWsq.setChecked(true);
                        break;
                    case JPEG2000:
                        selectImageFormatDialog.holder.cbJpg2000.setChecked(true);
                        break;
                    case FIR_V2005:
                        selectImageFormatDialog.holder.cbFIRV2005.setChecked(true);
                        break;
                    case FIR_V2011:
                        selectImageFormatDialog.holder.cbFIRV2011.setChecked(true);
                        break;
                    case FIR_WSQ_V2005:
                        selectImageFormatDialog.holder.cbFIRWSQV2005.setChecked(true);
                        break;
                    case FIR_WSQ_V2011:
                        selectImageFormatDialog.holder.cbFIRWSQV2011.setChecked(true);
                        break;
                    case FIR_JPEG2000_V2005:
                        selectImageFormatDialog.holder.cbFIRJPEGV2005.setChecked(true);
                        break;
                    case FIR_JPEG2000_V2011:
                        selectImageFormatDialog.holder.cbFIRJPEGV2011.setChecked(true);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectImageFormatDialog.holder.txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                selectImageFormatDialog.dismiss();
            }
        });

        selectImageFormatDialog.holder.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < ClickThreshold) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                captureImageFormatenum = ImageFormat.BMP;
                if (selectImageFormatDialog.holder.cbBmp.isChecked()) {
                    captureImageFormatenum = (ImageFormat.BMP);
                } else if (selectImageFormatDialog.holder.cbJpg2000.isChecked()) {
                    captureImageFormatenum = (ImageFormat.JPEG2000);
                } else if (selectImageFormatDialog.holder.cbWsq.isChecked()) {
                    captureImageFormatenum = (ImageFormat.WSQ);
                } else if (selectImageFormatDialog.holder.cbRaw.isChecked()) {
                    captureImageFormatenum = (ImageFormat.RAW);
                } else if (selectImageFormatDialog.holder.cbFIRV2005.isChecked()) {
                    captureImageFormatenum = (ImageFormat.FIR_V2005);
                } else if (selectImageFormatDialog.holder.cbFIRV2011.isChecked()) {
                    captureImageFormatenum = (ImageFormat.FIR_V2011);
                } else if (selectImageFormatDialog.holder.cbFIRWSQV2005.isChecked()) {
                    captureImageFormatenum = (ImageFormat.FIR_WSQ_V2005);
                } else if (selectImageFormatDialog.holder.cbFIRWSQV2011.isChecked()) {
                    captureImageFormatenum = (ImageFormat.FIR_WSQ_V2011);
                } else if (selectImageFormatDialog.holder.cbFIRJPEGV2005.isChecked()) {
                    captureImageFormatenum = (ImageFormat.FIR_JPEG2000_V2005);
                } else if (selectImageFormatDialog.holder.cbFIRJPEGV2011.isChecked()) {
                    captureImageFormatenum = (ImageFormat.FIR_JPEG2000_V2011);
                }
                selectImageFormatDialog.dismiss();
            }
        });
    }

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
     * TODO:  set key Dialog.
     */
    @SuppressLint("ClickableViewAccessibility")
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
                    Toast.makeText(MainActivity.this, "Key Not Empty", Toast.LENGTH_LONG).show();
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

    private void showSetQualityDialog() {
        selectQualityDialog = new SelectQualityDialog(this);
        selectQualityDialog.show();
        selectQualityDialog.holder.edtQuality.setText("" + minQuality);
        selectQualityDialog.holder.edtQuality.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(3)});
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
                if(selectQualityDialog.holder.edtQuality.getText().toString().isEmpty()){
                    selectQualityDialog.holder.edtQuality.setError("Quality cannot be empty");
                }else{
                    try {
                        minQuality = Integer.parseInt(selectQualityDialog.holder.edtQuality.getText().toString());
                        if (minQuality > 100 || (minQuality < 1)) {
                            selectQualityDialog.holder.edtQuality.setError("Quality should be between 1-100");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    selectQualityDialog.dismiss();
                }

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
            }
            return "";
        }
    };

    private void showSetTimeoutDialog() {
        selectTimeoutDialog = new SelectTimeoutDialog(this);
        selectTimeoutDialog.show();
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
                if(selectTimeoutDialog.holder.edtTimeout.getText().toString().isEmpty()){
                    selectTimeoutDialog.holder.edtTimeout.setError("Timeout cannot be empty");
                }else{
                    try {
                        timeOut = Integer.parseInt(selectTimeoutDialog.holder.edtTimeout.getText().toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    selectTimeoutDialog.dismiss();
                }
            }
        });
    }

    /**
     * TODO: finger matching template method.
     */
    public void matchData() {
        try {
            if (scannerAction.equals(ScannerAction.MatchISO) || scannerAction.equals(ScannerAction.MatchAnsi)) {
                if (lastCapFingerDataTemplate == null) {
                    return;
                }
                int Size = lastDeviceInfo.Width * lastDeviceInfo.Height + 1111;
                byte[] bTemplateData = new byte[Size];
                int[] tSize = new int[Size];
                int ret = morfinAuth.GetTemplate(bTemplateData, tSize, captureTemplateFormatenum);
                if (ret == 0) {
                    byte[] Verify_Template = new byte[Size];
                    System.arraycopy(bTemplateData, 0, Verify_Template, 0,
                            bTemplateData.length);
                    int[] matchScore = new int[1];
                    ret = morfinAuth.MatchTemplate(lastCapFingerDataTemplate, Verify_Template, matchScore, captureTemplateFormatenum);
                    if (ret < 0) {
                        setLogs("Error: " + ret + "(" + morfinAuth.GetErrorMessage(ret) + ")", true);
                    } else {
                        if (matchScore[0] >= 96) {
                            setLogs("Finger matched with score: " + matchScore[0], false);
                        } else {
                            setLogs("Finger not matched, score: " + matchScore[0], false);
                        }
                    }
                } else {
                    setLogs("Error: " + ret + "(" + morfinAuth.GetErrorMessage(ret) + ")", true);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO:Generate Encrypted key Method.
     */
    private String genClientKey(String clientkey){
        String ret = "";
        String pubKey = readRawResource(MainActivity.this);
        // Remove header/footer and decode Base64
        try {
            String cleanPem = pubKey
                    .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.decode(cleanPem,Base64.NO_PADDING);

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
            long epochSeconds  = System.currentTimeMillis()/1000;

            String finalKey = epochSeconds+ clientkey;
            encryptedBytes = cipher.doFinal(finalKey.getBytes());

            ret = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);

        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException |
                 NoSuchAlgorithmException
                 | InvalidKeySpecException | InvalidAlgorithmParameterException |
                 InvalidKeyException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return ret.replaceAll("\\r?\\n", "");
    }

    public String readRawResource(Context context){
        try {
            InputStream inputStream = context.getResources().
                    openRawResource(R.raw.public_key);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            return bos.toString("UTF-8");
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
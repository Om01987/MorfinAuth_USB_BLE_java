package com.mantra.morfinauthsample;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mantra.morfinauthsample.databinding.ActivityConnectionModeBinding;

import java.util.ArrayList;
import java.util.List;

public class ConnectionModeActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1001;
    ActivityConnectionModeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectionModeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setonClick();
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();

        // Always required for Bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add("android.permission.BLUETOOTH_ADMIN");
            permissions.add("android.permission.BLUETOOTH");
            permissions.add("android.permission.BLUETOOTH_SCAN");
            permissions.add("android.permission.BLUETOOTH_CONNECT");
            permissions.add("android.permission.ACCESS_FINE_LOCATION");
            permissions.add("android.permission.ACCESS_COARSE_LOCATION");
        } else {
            // For older versions, we need location permissions
            permissions.add("android.permission.ACCESS_FINE_LOCATION");
            permissions.add("android.permission.ACCESS_COARSE_LOCATION");
        }

        Dexter.withContext(this)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            checkBluetoothAndProceed();
                        } else {
                            cheackLocationonoroff();
                            /*Toast.makeText(ConnectionModeActivity.this,
                                    "Permissions are required to use Bluetooth features",
                                    Toast.LENGTH_SHORT).show();*/
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(
                            List<PermissionRequest> permissions, PermissionToken token) {
                        // Show rationale if needed
                        new AlertDialog.Builder(ConnectionModeActivity.this)
                                .setTitle("Permissions Needed")
                                .setMessage("This app needs Bluetooth permissions to work properly")
                                .setPositiveButton("OK", (dialog, which) -> token.continuePermissionRequest())
                                .setNegativeButton("Cancel", (dialog, which) -> token.cancelPermissionRequest())
                                .show();
                    }
                }).check();
    }

    private void checkBluetoothAndProceed() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            cheackLocationonoroff();
        }
    }

    public void cheackLocationonoroff(){
        boolean isLocationEnabled;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            isLocationEnabled = lm.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
            );
            isLocationEnabled = (mode != Settings.Secure.LOCATION_MODE_OFF);
        }

        if (isLocationEnabled) {
            // Location button ON
            Intent fingerIntent = new Intent(ConnectionModeActivity.this, BleActivity.class);
            startActivity(fingerIntent);
        } else {
            // Location button OFF
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    private void setonClick() {
        binding.btnBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 27){
                    checkPermissions();
                }else{
                    Toast.makeText(ConnectionModeActivity.this,"This feature is not supported on your Android version.",Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.btnUsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fingerIntent = new Intent(ConnectionModeActivity.this,
                        MainActivity.class);
                startActivity(fingerIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth was enabled
              cheackLocationonoroff();
            } else {
                // User declined to enable Bluetooth
                Toast.makeText(this, "Bluetooth is required for this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
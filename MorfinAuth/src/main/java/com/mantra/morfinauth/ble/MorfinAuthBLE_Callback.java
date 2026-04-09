package com.mantra.morfinauth.ble;

import com.mantra.morfinauth.ble.enums.MorfinBleState;
import com.mantra.morfinauth.ble.enums.MorfinNotifications;
import com.mantra.morfinauth.ble.model.MorfinBleDevice;

public interface MorfinAuthBLE_Callback {

    /**
     * Start discover devices , on successfully start discover nearby mantra BLE fingerprint sensor, on result
     * discovered device find on this callback
     * @param morfinBleDevice [OUT] available Mantra BLE fingerprint scanner
     */
    public void OnDeviceDiscovered(MorfinBleDevice morfinBleDevice);
    public void OnDeviceConnectionStatus(MorfinBleDevice morfinBleDevice, MorfinBleState morfinDeviceState);
    public void MorfinDeviceStatusNotification(MorfinNotifications morfinNotifications);
}


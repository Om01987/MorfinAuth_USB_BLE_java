package com.mantra.morfinauth.ble.enums;

public enum MorfinNotifications {
    NoFaultFound(0),
    BatteryDisconnected(1),
    BatteryTemperatureAbnormal(2),
    ChargingInputFault(3),
    OTGFault(4),
    ChargingTimerExpiration(5),
    BatteryOverPower(6),
    Charging(7),
    Discharging(8),
    ChargingRequiredWarning(9),
    BatteryPowerIsCriticalLow(10),
    DeviceDisconnecting(11),
    DeviceUninitialized(12),
    ChargerPlugged(13),
    ChargerUnplugged(14);
    private final int value;

    private MorfinNotifications(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

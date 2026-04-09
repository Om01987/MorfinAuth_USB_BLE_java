package com.mantra.morfinauth.enums;

public enum DeviceDetection {
    DISCONNECTED(0),
    CONNECTED(1);

    int detection = 0;

    DeviceDetection(int detection) {
        this.detection = detection;
    }
}
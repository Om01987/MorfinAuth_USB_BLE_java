package com.mantra.morfinauth.ble.enums;

public enum MorfinBleState {

    CONNECTED(0),
    CONNECTING(1),
    DISCONNECTED(2),
    DISCONNECTING(3),
    OUT_OF_RANGE(4);

    private final int value;

    private MorfinBleState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}

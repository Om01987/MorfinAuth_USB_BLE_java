package com.mantra.morfinauth.ble.enums;

public enum CaptureFormat {
    FMR_2005(0),
    FMR_2011(1),
    ANSI_378(2),
    FIR_2005(3),
    FIR_2011(4);
   /* WSQ(4),
    RAW(5);*/
    private final int value;
    private CaptureFormat(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

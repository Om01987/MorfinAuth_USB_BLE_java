package com.mantra.morfinauth.enums;

public enum LogLevel {
    OFF(0),
    ERROR(1),
    INFO(2),
    DEBUG(3);

    private final int value;

    private LogLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
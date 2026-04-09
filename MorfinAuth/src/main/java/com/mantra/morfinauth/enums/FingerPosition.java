package com.mantra.morfinauth.enums;

public enum FingerPosition {

    POSTION_OK(0),
    POSTION_LEFT(1),
    POSTION_RIGHT(2),
    POSTION_TOP(3),
    POSTION_NOT_IN_BOTTOM(4),
    POSTION_NOT_OK(5),
    POSTION_PLACE_FINGER(6);

    int value = 0;

    FingerPosition(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

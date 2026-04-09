package com.mantra.morfinauth.enums;

public enum TemplateFormat {
    /**
     * ISO/IES 19794-2 :2005
     */
    FMR_V2005(0),
    /**
     * ISO/IES 19794-2 :2011
     */
    FMR_V2011(1),
    /**
     * ANSI/INCITS 378 :2004
     */
    ANSI_V378(2);

    private final int value;

    private TemplateFormat(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
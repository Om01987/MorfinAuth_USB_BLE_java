package com.mantra.morfinauth.enums;

public enum ImageFormat {
    /**
     * To get bmp image data
     */
    BMP(0),
    /**
     * To get jpeg2000 image data
     */
    JPEG2000(1),
    /**
     * To get wsq image data
     */
    WSQ(2),
    /**
     * To get raw image data
     */
    RAW(3),
    /**
     * ISO/IES 19794-4 :2005
     */
    FIR_V2005(4),
    /**
     * ISO/IES 19794-4 :2011
     */
    FIR_V2011(5),
    /**
     * ISO/IES 19794-4 :2005 template with WSQ compression
     */
    FIR_WSQ_V2005(6),
    /**
     * ISO/IES 19794-4 :2011 template with WSQ compression
     */
    FIR_WSQ_V2011(7),
    /**
     * ISO/IES 19794-4 :2005 template with JPEG2000 compression
     */
    FIR_JPEG2000_V2005(8),
    /**
     * ISO/IES 19794-4 :2011 template with JPEG2000 compression
     */
    FIR_JPEG2000_V2011(9);

    private final int value;

    private ImageFormat(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
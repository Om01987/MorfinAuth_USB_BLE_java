package com.mantra.morfinauth.enums;

import java.util.HashMap;
import java.util.Map;

public enum DeviceModel {

    MFS500 ("MFS500"),
    MELO31 ("MELO31"),
    MARC10 ("MARC10"),
    MFS200("MFS200"),
    MELO20("MELO20"),
    MELO30("MELO30"),
    MARC30("MARC30");

    private String deviceModel;

    DeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceName() {
        return deviceModel;
    }

    /**
     * Enum value of not alow white space match sp value for require
     */
    private static final Map<String, DeviceModel> map = new HashMap<>();
    static {
        for (DeviceModel en : values()) {
            map.put(en.deviceModel, en);
        }
    }

    public static DeviceModel valueFor(String name) {
        return map.get(name);
    }
}

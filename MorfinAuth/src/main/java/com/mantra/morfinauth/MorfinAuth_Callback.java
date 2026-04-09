package com.mantra.morfinauth;

import com.mantra.morfinauth.enums.DeviceDetection;

public interface MorfinAuth_Callback {

    /**
     * Device attached / detached event called
     *
     * @param DeviceName connect / disconnect device name
     * @param detection attached / detached 1/0
     */
//    public void OnDeviceDetection(String DeviceName, String EventName);
    public void OnDeviceDetection(String DeviceName, DeviceDetection detection);
    /**
     * Start capture or auto capture call on preview callback called from Native .SO
     *
     * @param ErrorCode [OUT] 0 if no error else return -error code
     * @param Quality   [OUT] finger print quality
     * @param Image     [OUT] finger image for display
     */
    public void OnPreview(int ErrorCode, int Quality, byte[] Image);

    /**
     * Start capture call after capture success/timeout/other error on complete callback called from Native .SO
     * AutoCapture on this callback not called.
     *
     * @param ErrorCode  [OUT] 0 if no error else return -error code
     * @param Quality   [OUT] finger print quality
     * @param NFIQ      [OUT] finger print NFIQ
     */
    public void OnComplete(int ErrorCode, int Quality, int NFIQ);


    public void OnFingerPosition(int errorCode, int position);
}

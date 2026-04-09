package com.mantra.morfinauth;

public class MorfinAuthNative {

	private static Callback callback;

	static {
		System.loadLibrary("iengine_ansi_iso");
		System.loadLibrary("c++_shared");
		System.loadLibrary("Morfin_Auth_Core");
	}

	/**
	 * Registered preview and capture complete callback
	 *
	 * @param callback registered preview and capture complete callback
	 */
	protected static void RegisterCallback(Callback callback) {
		MorfinAuthNative.callback = callback;
	}

	/**
	 * MFS500, MFS100V2, Mapro CX, Mapro OX, MARC30 devices connection status
	 *
	 * @param fd           [IN] file descriptor of open device.
	 * @param product_name [IN] set product name (MFS500, MFS100V2, Mapro CX, Mapro OX, MARC30)
	 * @return device connected on return 0 otherwise return error code
	 */
	protected static native int IsDeviceConnected(int fd, String product_name);

	/**
	 * Generates timed client key from client key
	 *
	 * @param clientKey  [IN] client key for specific endUser.
	 * @param signedKey  [OUT] generated timed client key.
	 * @param keyLen     [OUT] signedKey length.
	 * @return key generated if return 0 otherwise return -error code
	 */
	protected static native int GenerateSignedKey(byte[] clientKey, byte[] signedKey, int[] keyLen);

	/**
	 * MFS500, MFS100V2, Mapro CX, Mapro OX, MARC30 device initialized
	 *
	 * @param fd           [IN] file descriptor of open device.
	 * @param product_name [IN] set product name (MFS500, MFS100V2, Mapro CX, Mapro OX, MARC30)
	 * @param device_info  [OUT] passed reference of device info.
	 * @return device initialized success on return 0 and return device information otherwise return -error code
	 */
	protected static native int Init(int fd, String product_name, byte[] clientKey, DeviceInfo device_info);

	/**
	 * Get SDK version
	 *
	 * @return sdk version
	 */
	protected static native String GetVersion();

//    /**
//     * Get PCB Number
//     *
//     * @return pcb number
//     */
//    protected static native String GetPCBNO();

	/**
	 * Start asynchronous capture. This function work after device initialized successfully.
	 *
	 * @param min_quality [IN] min quality for capture stop. Quality range 0 - 100. Default value set 60
	 * @param timeout     [IN] timeout set in milliseconds. 0 set on infinite time.
	 * @return capture start success on return 0 otherwise return -error code
	 */
	protected static native int StartCapture(int min_quality, int timeout);

	/**
	 * Start Synchronise capture. This function work after device initialized successfully.
	 *
	 * @param min_quality [IN] min quality for capture stop. Quality range 0 - 100. Default value set 60
	 * @param timeout     [IN] timeout set in milliseconds. 0 set on infinite time.
	 * @param quality     [OUT] capture success on return finger quality
	 * @param nfiq        [OUT] capture success on return finger NFIQ
	 * @return capture success on return 0 otherwise return -error code
	 */
	protected static native int AutoCapture(int min_quality, int timeout, int[] quality, int[] nfiq);

	/**
	 * Stop capture if capture running
	 *
	 * @return capture stop success on return 0 otherwise return -error code
	 */
	protected static native int StopCapture();

	/**
	 * After last capture success get fingerprint image in different format like BMP, JPEG2000, WSQ, PNG, RAW
	 *
	 * @param image             [OUT] return finger image data
	 * @param image_len         [IN/OUT] passed image allocated length. Image generate on return actual length
	 * @param image_format      [IN] BMP(0), PNG(1), JPEG2000(2), WSQ(3), RAW(4) passed
	 * @param compression_ratio [IN] Valid for WSQ(Ratio 1-10) and JPEG200(Ratio 1-20)
	 * @return image generate success on return 0 otherwise return -error code
	 */
	protected static native int GetImage(byte[] image, int[] image_len, int image_format, int compression_ratio);

	/**
	 * After last capture success get fingerprint template in different format like FMR, FIR, FIR_WSQ, FIR_JPEG2000, ANSI
	 *
	 * @param template        [OUT] return finger template data
	 * @param template_len    [IN/OUT] passed template allocated length. template generate on return actual length
	 * @param template_format [IN] FMR(0), FIR(1), FIR_WSQ(2), FIR_JPEG2000(3), ANSI(4) passed
	 * @return template generate success on return 0 otherwise return -error code
	 */
	protected static native int GetTemplate(byte[] template, int[] template_len, int template_format);

	/**
	 * Match FMR and ANSI template
	 *
	 * @param prob_template    [IN] probe template
	 * @param gallery_template [IN] gallery template
	 * @param match_score      [OUT] return matching score (Range 0 - 1000)
	 * @param template_format  [IN] FMR(0), ANSI(4)
	 * @return match template success on return 0 otherwise -error code
	 */
	protected static native int MatchTemplate(byte[] prob_template, byte[] gallery_template, int[] match_score, int template_format);

	/**
	 * Write libs log in file. Required WRITE_EXTERNAL_STORAGE permission.
	 *
	 * @param log_level [IN] set log level NATIVE_LVL_LOG_OFF(0), NATIVE_LVL_LOG_ERROR(1), NATIVE_LVL_LOG_INFO(2), NATIVE_LVL_LOG_DEBUG(3).
	 *                  Default NATIVE_LVL_LOG_OFF(0)
	 * @param file_path [IN] full sd card path with file name
	 */
	protected static native void EnableLogs(int log_level, String file_path);

	/**
	 * Get error message description
	 *
	 * @param error_code [IN] passed error code
	 * @return error message
	 */
	protected static native String GetErrorMessage(int error_code);

	/**
	 * uninitialized device
	 *
	 * @return device uninitialized success on return 0 else return -error code
	 */
	protected static native int Uninit();

	protected static native int GetDeviceList(DeviceList[] deviceLists, int[] deviceListSize);

	protected static native int GetSupportedDeviceList(DeviceList[] deviceLists, int[] deviceListSize);

	protected static native int RegisterDetectionCallback();

	public static void DeviceCallback(int event, String deviceName) {
		//TODO NOT APPLICABLE IN ANDROID
	}

	/**
	 * Start capture or auto capture call on preview callback called from Native .SO
	 *
	 * @param errorCode    [OUT] 0 if no error else return -error code
	 * @param Quality      [OUT] finger print quality
	 * @param previewImage [OUT] finger image for display
	 */
	public static void PreviewCallback(int errorCode, int Quality, byte[] previewImage) {
		if (MorfinAuthNative.callback != null) {
			MorfinAuthNative.callback.PreviewCallback(errorCode, Quality, previewImage);
		}
	}

	/**
	 * Start capture call after capture success/timeout/other error on complete callback called from Native .SO
	 * AutoCapture on this callback not called.
	 *
	 * @param errorCode [OUT] 0 if no error else return -error code
	 * @param Quality   [OUT] finger print quality
	 * @param NFIQ      [OUT] finger print NFIQ
	 */
	public static void CompleteCallback(int errorCode, int Quality, int NFIQ) {
		if (MorfinAuthNative.callback != null) {
			MorfinAuthNative.callback.CompleteCallback(errorCode, Quality, NFIQ);
		}
	}

	public static void PositionCallback(int errorCode, int fingerPosition) {
		if (MorfinAuthNative.callback != null) {
			MorfinAuthNative.callback.FingerPositionCallback(errorCode, fingerPosition);
		}
	}

	public interface Callback {
		void PreviewCallback(int errorCode, int quality, byte[] image);

		void CompleteCallback(int errorCode, int quality, int nfiq);

		void FingerPositionCallback(int errorCode, int position);
	}

	/**
	 * Succes
	 */
	public static final int MORFIN_AUTH_SUCCESS = 0;
	/**
	 * No Device found in Image Process
	 */
	public static final int IMG_PROCESS_E_NO_DEVICE = -1601;
	/**
	 * memory error found in Image Process
	 */
	public static final int IMG_PROCESS_E_MEMORY = -1602;
	/**
	 * Invalid license found in Image Process
	 */
	public static final int IMG_PROCESS_E_BAD_LICENSE = -1603;
	/**
	 * Unkown error in Image process
	 */
	public static final int IMG_PROCESS_E_OTHER = -1604;
	/**
	 * Invalid Paramer
	 */
	public static final int IMG_PROCESS_E_INVALIDPARAM = -1605;
	/**
	 * VID Not Match
	 */
	public static final int IMG_PROCESS_VID_NOTMATCH = -1606;
	/**
	 * PID Not Match
	 */
	public static final int IMG_PROCESS_PID_NOTMATCH = -1607;
	/**
	 * CID Not Match
	 */
	public static final int IMG_PROCESS_CID_NOTMATCH = -1608;
	/**
	 * No Seial number found
	 */
	public static final int IMG_PROCESS_E_NOSERIAL = -1609;
	/**
	 * Image Process Not Initlialzed
	 */
	public static final int IMG_PROCESS_E_NOTINITIALIZED = -1610;
	/**
	 * Image Process No file found
	 */
	public static final int IMG_PROCESS_E_NO_FILE = -1611;
	/**
	 * Image Process No Licence file found
	 */
	public static final int unsupported_licence = -1612;
	/**
	 * Invalid name length in Image Process
	 */
	public static final int INVLD_PRODUCT_NAME_LEN = -2001;
	/**
	 * Invalid name in Image Process
	 */
	public static final int INVLD_PRODUCT_NAME = -2002;
	/**
	 * failed to get Product Id
	 */
	public static final int FAILED_TO_GET_PRODUCT_ID = -2003;
	/**
	 * failed Init Communication
	 */
	public static final int FAILED_TO_INIT_COMM = -2004;
	/**
	 * failed to Init Device
	 */
	public static final int FAILED_TO_INIT_DEVICE = -2005;
	/**
	 * failed to get Hardware Id
	 */
	public static final int FAILED_TO_GET_HWID = -2006;
	/**
	 * failed to Register Callback
	 */
	public static final int FAILED_TO_REGISTER_CALLBACK = -2007;
	/**
	 * failed to Create Thread
	 */
	public static final int FAILED_TO_CREATE_THREAD = -2008;
	/**
	 * failed to create timeout thread
	 */
	public static final int FAILED_TO_CREATE_TIMEOUT_THREAD = -2009;
	/**
	 * failed to create Callback thread
	 */
	public static final int FAILED_TO_CREATE_CLBK_THREAD = -2010;
	/**
	 * failed to Start Capture
	 */
	public static final int FAILED_TO_START_CAPTURE = -2011;
	/**
	 * failed to Stop Capture
	 */
	public static final int FAILED_TO_STOP_CAPTURE = -2012;
	/**
	 * failed to Uninit Device
	 */
	public static final int FAILED_TO_UINIT_DEVICE = -2013;
	/**
	 * failed to Uninit LIBS
	 */
	public static final int FAILED_TO_UINIT_LIBS = -2014;
	/**
	 * failed to restore MFG data
	 */
	public static final int FAILED_TO_RESTORE_MFG_DATA = -2015;
	/**
	 * Failed to get MFG data
	 */
	public static final int FAILED_TO_GET_MFG_DATA = -2016;
	/**
	 * failed to get FPS Data
	 */
	public static final int FAILED_TO_FPS_GET_DATA = -2017;
	/**
	 * Invalid license key
	 */
	public static final int INVLD_LIC_KEY = -2018;
	/**
	 * Capture Timeout occurs
	 */
	public static final int MORFIN_AUTH_CAPTURE_TIMEOUT = -2019;
	/**
	 * failed to Memory allocation failed
	 */
	public static final int FAILED_TO_ALLOC_MEM = -2020;
	/**
	 * failed to get serial number
	 */
	public static final int FAILED_TO_GET_SER_NO = 2021;
	/**
	 * Device Info Struct
	 */
	public static final int NULL_DEVICE_INFO_STRUCT = -2022;
	/**
	 * capture already started
	 */
	public static final int CAPTURE_ALREADY_STARTED = -2023;
	/**
	 * Device already Initialized
	 */
	public static final int DEVICE_ALREADY_INITIALIZED = -2024;
	/**
	 * Device Not Initialized
	 */
	public static final int DEVICE_NOT_INITIALIZED = -2025;
	/**
	 * object can not be null or empty
	 */
	public static final int OBJECT_CANNOT_BE_NULL_OR_EMPTY = -2026;
	/**
	 * Device not connected
	 */
	public static final int DEVICE_NOT_CONNECTED = -2027;
	/**
	 * invalid callback function found
	 */
	public static final int NULL_CALLBACK_FUNC_FOUND = -2028;
	/**
	 * failed to create array parsing
	 */
	public static final int FAILED_TO_CREATE_PARSE_ARR = -2029;
	/**
	 * failed to read Device manufacture data
	 */
	public static final int FAILED_TO_READ_DEV_MFG_DATA = -2030;
	/**
	 * failed to Init Image Processing
	 */
	public static final int IPL_DEV_INIT_FAILED = -2031;
	/**
	 * failed to Init for Image Processing
	 */
	public static final int IPL_INIT_FAILED = -2032;
	/**
	 * failed to get Image Qaulity
	 */
	public static final int FAILED_TO_GET_IMG_QLTY = -2033;
	/**
	 * failed to process Raw data
	 */
	public static final int FAILED_TO_PROCESS_RAW_DATA = -2034;
	/**
	 * failed to process MFG data
	 */
	public static final int FAILED_TO_PROCESS_MFG_DATA = -2035;
	/**
	 * failed to Set encryption key
	 */
	public static final int FAILED_TO_SET_ENC_KEY = -2036;
	/**
	 * failed to get template
	 */
	public static final int FAILED_TO_GET_TEMPLATE = -2037;
	/**
	 * Finger not Captured
	 */
	public static final int FINGER_NOT_CAPTURED = -2038;
	/**
	 * Unsupported Image Format
	 */
	public static final int UNSUPPORTED_IMAGE_FORMAT = -2040;
	/**
	 * Unsupported template formate
	 */
	public static final int UNSUPPORTED_TEMPLATE_FORMAT = -2041;
	/**
	 * Invalid Compression ratio
	 */
	public static final int INVLD_COMPRESSION_RATIO = -2042;
	/**
	 * Invalid Template version
	 */
	public static final int INVLD_TEMPLATE_VERSION = -2043;
	/**
	 * Some exception occurred
	 */
	public static final int MORFIN_AUTH_E_EXCEPTION_OCCURRED = -2044;
	/**
	 * NULL parameter provided
	 */
	public static final int MORFIN_AUTH_E_NULL_PARAM = -2045;
	/**
	 * Failed to get version
	 */
	public static final int FAILED_TO_GET_VER = -2046;
	/**
	 * Quality out of range. It should be between 0 to 100
	 */
	public static final int MORFIN_AUTH_E_QTY_OUT_OF_RANGE = -2047;
	/**
	 * Invalid log level.
	 */
	public static final int INVLD_LOG_LEVEL = -2048;
	/**
	 * Invalid firmware version
	 */
	public static final int INVLD_FIRMWARE = -2049;
	/**
	 * Device Not Streaming
	 */
	public static final int DEVICE_NOT_STREAMING = -2050;
	/**
	 * Unsupported feature
	 */
	public static final int MORFIN_AUTH_UNSUPPORTED_FEATURE = -2051;
	/**
	 * Failed to create log level.
	 */
	public static final int FAILED_TO_CREATE_LOG_FILE = -2052;
	/**
	 * Capture stop in progress.
	 */
	public static final int CAPTURE_STOP_IN_PROGRESS = -2053;
	/**
	 * Capture stop.
	 */
	public static final int CAPTURE_STOP = -2054;
	/**
	 * Bad image captured
	 */
	public static final int BAD_CAPTURE_IMAGE = -2055;
	/**
	 * Timeout out of range
	 */
	public static final int TIMEOUT_OUT_OF_RANGE = -2056;
	/**
	 * Hardware Interface Error
	 */
	public static final int HARDWARE_INTERFACE_ERROR = -2057;
	/**
	 * Some mfg library related error happened
	 */
	public static final int MFG_LIB_ERROR = -3000;

    //Capture not running
    public static final int MORFIN_AUTH_CAPTURE_NOT_RUNNING = -2061;

    // Unhandled exceptions
    public static final int MORFIN_AUTH_UNHANDLED_EXCEPTION = -2062;

    //Stop Capture already running
    public static final int MORFIN_AUTH_STOP_CAPTURE_RUNNING = -2063;

}

package com.mantra.morfinauth;

import com.mantra.morfinauth.ble.BitmapConverter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Utils {
    private static int MAC_ADDRESS_LENGTH = 6;
    protected static int getIntFrombyte(byte first, byte second, boolean isSigned) {
        int out;
        if (isSigned) {
            out = ((first) << 8) | (second & 0xFF);
        } else {
            out = ((first & 0xFF) << 8) | (second & 0xFF);
        }
        return out;
    }



    protected static String convertASCII(byte[] data) {
        try {
            StringBuilder sb = new StringBuilder(data.length);
            for (byte datum : data) {
                if (datum < 0) throw new IllegalArgumentException();
                sb.append((char) datum);
            }
            return sb.toString();
        } catch (Exception e) {
            return "Invalid Data";
        }

    }
    protected static byte[] rawToBitmapBytes(byte[] raw, int width, int height) {
        byte[] image = new byte[(width * height) + (height * 3) + 2000];
        int[] imageLen = new int[1];
        int ret = BitmapConverter.createBitmap(raw, width, height, image, imageLen, false);
        if (ret == 0) {
            return Arrays.copyOf(image, imageLen[0]);
        }
        return null;
    }

    protected static byte[] getByteArrayFromInt(int value) {
        byte[] result = new byte[2];
        result[0] = (byte) (value >> 8);
        result[1] = (byte) (value);
//        System.out.println("converted byte :"+ Arrays.toString(result));
        return result;
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);


    // Helper function to convert byte array to a readable string (for debugging)
    protected static String byteArrayToString(byte[] byteArray) {
        StringBuilder builder = new StringBuilder();
        for (byte b : byteArray) {
            builder.append(String.format("%02X", b));
            builder.append(":");
        }
        return builder.toString().substring(0, builder.length() - 1);  // Remove trailing colon
    }

}

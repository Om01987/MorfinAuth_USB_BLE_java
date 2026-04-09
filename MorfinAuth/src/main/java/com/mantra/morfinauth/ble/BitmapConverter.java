package com.mantra.morfinauth.ble;

public class BitmapConverter {
    
    static final int FILE_HEADER = 14;
    static final int IMAGE_HEADER = 40;
    static final int COLOR_PALETTE = 1024;

    private static int createNewRawBuffer(byte[] rawImage, int width, int height, byte[] refNewRawImage, int[] refNewSize, boolean isColorImg) {
        int offset = 1;
        if (isColorImg)
            offset = 3;

        int padding = (4 - ((width * offset) % 4)) % 4;
        int scanlinebytes = width * offset;
        int totalScanlinebytes = scanlinebytes + padding;
        refNewSize[0] = height * totalScanlinebytes;

        if (rawImage != null && refNewRawImage != null) {
            // Fill new array with original buffer, pad remaining with zeros
            for (int a = 0; a < height; a++) {
                System.arraycopy(rawImage, a * width * offset, refNewRawImage, a * (width * offset + padding), width * offset);
                for (int pad = 0; pad < padding; pad++) {
                    refNewRawImage[a * (width * offset + padding) + offset * width + pad] = 0x00;
                }
            }
        }
        return 0; // BMP_E_SUCCESS equivalent
    }

    public static int createBitmap(byte[] rawImage, int width, int height, byte[] refImage, int[] refImageSize, boolean invert) {
        if (rawImage == null) {
            return -1; // BMP_E_NULL equivalent
        }

        // Check Padding
        int[] rawImageSize = new int[1];
        byte[] refNewRawImage = null;
        createNewRawBuffer(rawImage, width, height, refNewRawImage, rawImageSize, false);
        if (refImage == null) {
            refImageSize[0] = FILE_HEADER + IMAGE_HEADER + COLOR_PALETTE + rawImageSize[0];
            return 0; // BMP_E_SUCCESS equivalent
        }

        refNewRawImage = new byte[rawImageSize[0]];
        createNewRawBuffer(rawImage, width, height, refNewRawImage, rawImageSize, false);

        refImageSize[0] = FILE_HEADER + IMAGE_HEADER + COLOR_PALETTE + rawImageSize[0];

        // Create Image
        byte[] colorPalette = new byte[COLOR_PALETTE]; // a palette containing 256 colors
        byte[] bmpFileHeader = new byte[FILE_HEADER];
        byte[] dibHeader = new byte[IMAGE_HEADER];
        
        int fileSize = refImageSize[0];
        int offset = FILE_HEADER + IMAGE_HEADER + COLOR_PALETTE;
        
        // Create Bitmap File Header (populate BMP_File_Header array)
        // Bitmap Type 2 bytes
        bmpFileHeader[0] = 'B';
        bmpFileHeader[1] = 'M';

        // File Size 4 bytes
        bmpFileHeader[2] = (byte) (fileSize & 0x000000FF);
        bmpFileHeader[3] = (byte) ((fileSize & 0x0000FF00) >> 8);
        bmpFileHeader[4] = (byte) ((fileSize & 0x00FF0000) >> 16);
        bmpFileHeader[5] = (byte) ((fileSize & 0xFF000000) >> 24);

        // Reserved Bytes 2 bytes
        bmpFileHeader[6] = 0;
        bmpFileHeader[7] = 0;

        // Reserved Bytes 2 bytes
        bmpFileHeader[8] = 0;
        bmpFileHeader[9] = 0;

        // Offset bits 4 bytes
        bmpFileHeader[10] = (byte) (offset & 0x000000FF);
        bmpFileHeader[11] = (byte) ((offset & 0x0000FF00) >> 8);
        bmpFileHeader[12] = (byte) ((offset & 0x00FF0000) >> 16);
        bmpFileHeader[13] = (byte) ((offset & 0xFF000000) >> 24);

        // Create DIB Header (populate DIB_header array)
        // DIB header length 4 bytes
        dibHeader[0] = (byte) (IMAGE_HEADER & 0x000000FF);
        dibHeader[1] = (byte) ((IMAGE_HEADER & 0x0000FF00) >> 8);
        dibHeader[2] = (byte) ((IMAGE_HEADER & 0x00FF0000) >> 16);
        dibHeader[3] = (byte) ((IMAGE_HEADER & 0xFF000000) >> 24);

        // Image Width 4 bytes
        dibHeader[4] = (byte) (width & 0x000000FF);
        dibHeader[5] = (byte) ((width & 0x0000FF00) >> 8);
        dibHeader[6] = (byte) ((width & 0x00FF0000) >> 16);
        dibHeader[7] = (byte) ((width & 0xFF000000) >> 24);

        // Image Height 4 bytes
        dibHeader[8] = (byte) (-height & 0x000000FF);
        dibHeader[9] = (byte) ((-height & 0x0000FF00) >> 8);
        dibHeader[10] = (byte) ((-height & 0x00FF0000) >> 16);
        dibHeader[11] = (byte) ((-height & 0xFF000000) >> 24);

        // color planes. N.B. Must be set to 1 2 bytes
        dibHeader[12] = 1;
        dibHeader[13] = 0;

        // Bits per pixel 2 bytes
        dibHeader[14] = 8;
        dibHeader[15] = 0;

        // compression method N.B. BI_RGB = 0 4 bytes
        dibHeader[16] = 0;
        dibHeader[17] = 0;
        dibHeader[18] = 0;
        dibHeader[19] = 0;

        // Length of raw bitmap data 4 bytes
        dibHeader[20] = (byte) (rawImageSize[0] & 0x000000FF);
        dibHeader[21] = (byte) ((rawImageSize[0] & 0x0000FF00) >> 8);
        dibHeader[22] = (byte) ((rawImageSize[0] & 0x00FF0000) >> 16);
        dibHeader[23] = (byte) ((rawImageSize[0] & 0xFF000000) >> 24);

        // horizontal resolution N.B. not important 4 bytes
        dibHeader[24] = (byte) (19700 & 0x000000FF);
        dibHeader[25] = (byte) ((19700 & 0x0000FF00) >> 8);
        dibHeader[26] = (byte) ((19700 & 0x00FF0000) >> 16);
        dibHeader[27] = (byte) ((19700 & 0xFF000000) >> 24);

        // Vertical resolution N.B. not important 4 bytes
        dibHeader[28] = (byte) (19700 & 0x000000FF);
        dibHeader[29] = (byte) ((19700 & 0x0000FF00) >> 8);
        dibHeader[30] = (byte) ((19700 & 0x00FF0000) >> 16);
        dibHeader[31] = (byte) ((19700 & 0xFF000000) >> 24);

        // number of colors in the palette 4 bytes
        dibHeader[32] = (byte) (256 & 0x000000FF);
        dibHeader[33] = (byte) ((256 & 0x0000FF00) >> 8);
        dibHeader[34] = (byte) ((256 & 0x00FF0000) >> 16);
        dibHeader[35] = (byte) ((256 & 0xFF000000) >> 24);

        // number of important colors used N.B. 0 4 bytes
        dibHeader[36] = 0;
        dibHeader[37] = 0;
        dibHeader[38] = 0;
        dibHeader[39] = 0;

        // creates byte array of 256 color gray scale palette
        for (int i = 0; i < 256; i++) {
            colorPalette[4 * i] = (byte) i;
            colorPalette[4 * i + 1] = (byte) i;
            colorPalette[4 * i + 2] = (byte) i;
            colorPalette[4 * i + 3] = 0x00;
        }

        // Now add headers and color palette into refImage
        System.arraycopy(bmpFileHeader, 0, refImage, 0, FILE_HEADER);
        System.arraycopy(dibHeader, 0, refImage, FILE_HEADER, IMAGE_HEADER);
        System.arraycopy(colorPalette, 0, refImage, FILE_HEADER + IMAGE_HEADER, COLOR_PALETTE);

        // Now add the raw image to refImage
        System.arraycopy(refNewRawImage, 0, refImage, FILE_HEADER + IMAGE_HEADER + COLOR_PALETTE, rawImageSize[0]);

        return 0; // BMP_E_SUCCESS equivalent
    }
}

package com.lightcrafts.image.libs;

import java.io.FileNotFoundException;

class LCJPEGReaderNative {

    /**
     * The number of colors per pixel. This is set from native code.
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    int m_colorsPerPixel;

    /**
     * The colorspace of the image, one of: <code>CS_GRAYSCALE</code>, <code>CS_RGB</code>, <code>
     * CS_YCbRr</code>, <code>CS_CMYK</code>, <code>CS_YCCK</code>, or <code>CS_UNKNOWN</code>. This
     * is set from native code.
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    int m_colorSpace;

    /**
     * The image height. This is set from native code.
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    int m_height;

    /**
     * This is where the native code stores a pointer to the <code>JPEG</code> native data
     * structure. Do not touch this from Java except to compare it to zero.
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    long m_nativePtr;

    /**
     * The image width. This is set from native code.
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    int m_width;

    /**
     * Dispose of an <code>LCJPEGReader</code>.
     */
    native void dispose() throws LCImageLibException;

    /**
     * Begin using the {@link LCImageDataProvider} to get JPEG image data.
     *
     * @param provider The {@link LCImageDataProvider} to get image data from.
     * @param bufSize The size of the buffer to use.
     * @param maxWidth The maximum width of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     * @param maxHeight The maximum height of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     */
    native void beginRead(
            LCImageDataProvider provider, int bufSize, int maxWidth, int maxHeight)
            throws LCImageLibException;

    /**
     * Reads and decodes and encoded set of scanlines from the JPEG image.
     *
     * @param buf The buffer into which to read the image data.
     * @param offset The offset into the buffer where the image data will begin being placed.
     * @param numLines The number of scanlines to read.
     * @return Returns the number of scanlines read.
     */
    synchronized native int readScanLines(byte[] buf, long offset, int numLines)
            throws LCImageLibException;

    native void openForReading(byte[] fileNameUtf8, int maxWidth, int maxHeight)
            throws FileNotFoundException, LCImageLibException;

    static {
        System.loadLibrary("LCJPEG");
    }
}
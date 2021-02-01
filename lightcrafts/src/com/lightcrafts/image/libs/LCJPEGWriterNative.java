package com.lightcrafts.image.libs;

import java.io.IOException;

class LCJPEGWriterNative {

    /**
     * This is where the native code stores a pointer to the <code>JPEG</code> native data
     * structure. Do not touch this from Java except to compare it to zero.
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    long m_nativePtr;

    /**
     * Dispose of an <code>LCJPEGReader</code>.
     */
    native void dispose() throws LCImageLibException;

    /**
     * Compresses and writes a raw set of scanlines to the JPEG image.
     *
     * @param buf The buffer from which to compress the image data.
     * @param offset The offset into the buffer where the image data will begin being read.
     * @param numLines The number of scanlines to compress.
     * @return Returns the number of scanlines written.
     */
    native synchronized int writeScanLines(byte[] buf, int offset,
                                           int numLines,
                                           int lineStride)
            throws LCImageLibException;

    /**
     * Write an APP segment to the JPEG file.
     *
     * @param marker The APP segment marker.
     * @param buf The buffer comprising the raw binary contents for the segment.
     */
    native void writeSegment(int marker, byte[] buf)
            throws LCImageLibException;

    native void openForWriting(byte[] fileNameUtf8, int width, int height,
                               int colorsPerPixel, int colorSpace,
                               int quality)
            throws IOException, LCImageLibException;

    /**
     * Begin using the {@link LCImageDataProvider} to get JPEG image data.
     *  @param bufSize The size of the buffer (in bytes) to use.
     * @param width The width of the image in pixels.
     * @param height The height of the image in pixels.
     * @param colorsPerPixel The number of color components per pixel.
     * @param colorSpace The colorspace of the input image; must be one of {@link
     * LCJPEGConstants#CS_GRAYSCALE}, {@link LCJPEGConstants#CS_RGB}, {@link
     * LCJPEGConstants#CS_YCbRr}, {@link LCJPEGConstants#CS_CMYK}, or {@link
     * LCJPEGConstants#CS_YCCK}.
     * @param quality Image quality: 0-100.
     */
    native void beginWrite(LCImageDataReceiver receiver, int bufSize,
                           int width, int height, int colorsPerPixel,
                           int colorSpace, int quality)
            throws LCImageLibException;

    static {
        System.loadLibrary("LCJPEG");
    }
}
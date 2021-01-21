package com.lightcrafts.utils;

public class DCRawNative {
    public native static void interpolateRedBlue(
	    short[] jdata, int width, int height, int lineStride,
	    int rOffset, int gOffset, int bOffset,
	    int rx0, int ry0, int bx0, int by0);

    public native static void interpolateGreen(
            short[] srcData, short[] destData, int width, int height,
            int srcLineStride, int destLineStride,
            int srcOffset, int rOffset, int gOffset, int bOffset,
            int gx, int gy, int ry );

    static {
	System.loadLibrary("DCRaw");
    }
}

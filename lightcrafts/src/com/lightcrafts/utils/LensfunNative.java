/* Copyright (C) 2017- Masahiro Kitagawa */

package com.lightcrafts.utils;

import com.lightcrafts.platform.Platform;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Wrapper for Lensfun C++ library.
 */
@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public class LensfunNative {

    static native long init(String path);
    static native void destroy(long handle);

    static native String[] getCameraNames(long lfHandle);
    static native String[] getLensNames(long lfHandle);
    static native String[] getLensNamesForCamera(
            long lfHandle, String cameraMaker, String cameraModel);

    static native void initModifier(long lfHandle, int fullWidth, int fullHeight,
                                     String cameraMaker, String cameraModel,
                                     String lensMaker, String lensModel,
                                     float focal, float aperture);

    static native void initModifierWithPoly5Lens(long lfHandle,
                                                  int fullWidth, int fullHeight,
                                                  float k1, float k2, float kr, float kb,
                                                  float focal, float aperture);

    static native void distortionColor(
            long lfHandle,
            short[] srcData, short[] dstData,
            int srcRectX, int srcRectY, int srcRectWidth, int srcRectHeight,
            int dstRectX, int dstRectY, int dstRectWidth, int dstRectHeight,
            int srcPixelStride, int dstPixelStride,
            int srcROffset, int srcGOffset, int srcBOffset,
            int dstROffset, int dstGOffset, int dstBOffset,
            int srcLineStride, int dstLineStride);

    static native int[] backwardMapRect(
            long lfHandle,
            int dstRectX, int dstRectY, int dstRectWidth, int dstRectHeight);

    static {
        System.loadLibrary("LCLENSFUN");
    }
}

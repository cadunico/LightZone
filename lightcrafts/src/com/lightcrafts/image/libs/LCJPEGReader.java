/* Copyright (C) 2005-2011 Fabio Riccardi */
/* Copyright (C) 2018-     Masahiro Kitagawa */

package com.lightcrafts.image.libs;

import com.lightcrafts.image.types.AdobeEmbedJPEGSegmentFilter;
import com.lightcrafts.image.types.AdobeJPEGSegmentFilter;
import com.lightcrafts.image.types.JPEGImageInfo;
import com.lightcrafts.jai.opimage.CachedImage;
import com.lightcrafts.utils.ProgressIndicator;
import com.lightcrafts.utils.UserCanceledException;
import com.lightcrafts.utils.thread.ProgressThread;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;

import static com.lightcrafts.image.types.JPEGConstants.JPEG_APPC_MARKER;
import static com.lightcrafts.image.types.JPEGConstants.JPEG_APPE_MARKER;
import static com.lightcrafts.jai.JAIContext.CMYKColorSpace;
import static com.lightcrafts.jai.JAIContext.TILE_HEIGHT;
import static com.lightcrafts.jai.JAIContext.TILE_WIDTH;
import static com.lightcrafts.jai.JAIContext.fileCache;
import static com.lightcrafts.jai.JAIContext.gray22ColorSpace;
import static com.lightcrafts.jai.JAIContext.sRGBColorSpace;

/**
 * An <code>LCJPEGReader</code> is a Java wrapper around the LibJPEG library for reading JPEG
 * images.
 *
 * @author Paul J. Lucas [paul@lightcrafts.com]
 * @see <a href="http://www.ijg.org/">LibJPEG</a>
 */
public final class LCJPEGReader implements LCImageReader {

    private final LCJPEGReaderNative jpegReaderNative = new LCJPEGReaderNative();

    /**
     * The default buffer size for use with {@link #LCJPEGReader(LCImageDataProvider).
     */
    private static final int DEFAULT_BUF_SIZE = 32 * 1024;

    /**
     * This is <code>true</code> only if the JPEG file has an Adobe (APPE) segment.
     */
    private boolean m_hasAdobeSegment;

    /**
     * This is <code>true</code> only if the JPEG file has an Adobe Embed (APPC) segment.
     */
    private boolean m_hasAdobeEmbedMarker;

    /**
     * The actual end-result image.
     */
    private PlanarImage m_image;

    /**
     * Construct an <code>LCJPEGReader</code>.
     *
     * @param fileName The name of the JPEG file to read.
     */
    public LCJPEGReader(String fileName)
            throws FileNotFoundException, LCImageLibException, UnsupportedEncodingException {
        this(fileName, 0, 0, null);
    }

    /**
     * Construct an <code>LCJPEGReader</code>.
     *
     * @param fileName The name of the JPEG file to read.
     * @param maxWidth The maximum width of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     * @param maxHeight The maximum height of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     * @param jpegInfo The {@link JPEGImageInfo} of the image, or <code>null</code>.
     */
    public LCJPEGReader(String fileName, int maxWidth, int maxHeight, JPEGImageInfo jpegInfo)
            throws FileNotFoundException, LCImageLibException, UnsupportedEncodingException {
        openForReading(fileName, maxWidth, maxHeight);
        if (jpegInfo != null) {
            if (jpegInfo.getFirstSegmentFor(JPEG_APPE_MARKER, new AdobeJPEGSegmentFilter())
                    != null) {
                m_hasAdobeSegment = true;
            }
            if (jpegInfo.getFirstSegmentFor(JPEG_APPC_MARKER, new AdobeEmbedJPEGSegmentFilter())
                    != null) {
                m_hasAdobeEmbedMarker = true;
            }
        }
    }

    /**
     * Construct an <code>LCJPEGReader</code>.
     *
     * @param provider The {@link LCImageDataProvider} to get image data from.
     */
    public LCJPEGReader(LCImageDataProvider provider) throws LCImageLibException {
        this(provider, 0, 0);
    }

    /**
     * Construct an <code>LCJPEGReader</code>.
     *
     * @param provider The {@link LCImageDataProvider} to get image data from.
     * @param maxWidth The maximum width of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     * @param maxHeight The maximum height of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     */
    public LCJPEGReader(LCImageDataProvider provider, int maxWidth, int maxHeight)
            throws LCImageLibException {
        jpegReaderNative.beginRead(provider, DEFAULT_BUF_SIZE, maxWidth, maxHeight);
    }

    /**
     * Construct an <code>LCJPEGReader</code>.
     *
     * @param provider The {@link LCImageDataProvider} to get image data from.
     * @param bufSize The size of the buffer (in bytes) to use.
     */
    public LCJPEGReader(LCImageDataProvider provider, int bufSize) throws LCImageLibException {
        jpegReaderNative.beginRead(provider, bufSize, 0, 0);
    }

    /**
     * Construct an <code>LCJPEGReader</code>.
     *
     * @param provider The {@link LCImageDataProvider} to get image data from.
     * @param bufSize The size of the buffer to use.
     * @param maxWidth The maximum width of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     * @param maxHeight The maximum height of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     */
    public LCJPEGReader(LCImageDataProvider provider, int bufSize, int maxWidth, int maxHeight)
            throws LCImageLibException {
        jpegReaderNative.beginRead(provider, bufSize, maxWidth, maxHeight);
    }

    /**
     * Gets the number of color components per pixel.
     *
     * @return Returns said number.
     */
    public int getColorsPerPixel() {
        return jpegReaderNative.m_colorsPerPixel;
    }

    /**
     * Gets the colorspace of the image.
     *
     * @return Returns one of: <code>CS_GRAYSCALE</code>, <code>CS_RGB</code>,
     * <code>CS_YCbRr</code>,
     * <code>CS_CMYK</code>, <code>CS_YCCK</code>, or <code>CS_UNKNOWN</code>.
     */
    public int getColorSpace() {
        return jpegReaderNative.m_colorSpace;
    }

    /**
     * Gets the height of the image in pixels.
     *
     * @return Returns said height.
     */
    public int getHeight() {
        return jpegReaderNative.m_height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanarImage getImage() throws LCImageLibException, UserCanceledException {
        return getImage( null, null );
    }

    /**
     * Gets the JPEG image.
     *
     * @param cs The {@link ColorSpace} to use.
     * @return Returns said image.
     */
    public synchronized PlanarImage getImage(ColorSpace cs)
            throws LCImageLibException, UserCanceledException {
        return getImage(null, cs);
    }

    /**
     * Gets the JPEG image.
     *
     * @param thread The thread that will do the getting.
     * @param cs The {@link ColorSpace} to use.
     * @return Returns said image.
     */
    public synchronized PlanarImage getImage(ProgressThread thread, ColorSpace cs)
            throws LCImageLibException, UserCanceledException {
        if (m_image == null) {
            boolean userCanceled = false;
            try {
                readImage(thread, cs);
            } catch (UserCanceledException e) {
                userCanceled = true;
                throw e;
            } finally {
                try {
                    dispose();
                } catch (LCImageLibException e) {
                    //
                    // The JPEG library will complain if dispose() is called
                    // before the entire image has been read ("Application
                    // transferred too few scanlines") because the user clicked
                    // the "Cancel" button.  Therefore, ignore any exception if
                    // this is the case, but rethrow it otherwise.
                    //
                    if (!userCanceled) {
                        throw e;
                    }
                }
            }
        }
        return m_image;
    }

    /**
     * Gets the width of the image in pixels.
     *
     * @return Returns said width.
     */
    public int getWidth() {
        return jpegReaderNative.m_width;
    }

    /**
     * Finalize this class by calling {@link #dispose()}.
     */
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    /**
     * Open a JPEG file for reading.
     *
     * @param fileName The name of the JPEG file to open.
     * @param maxWidth The maximum width of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     * @param maxHeight The maximum height of the image to get, rescaling if necessary. A value of 0
     * means don't scale.
     */
    private void openForReading(String fileName, int maxWidth, int maxHeight)
            throws FileNotFoundException, LCImageLibException {
        byte[] fileNameUtf8 = (fileName + '\000').getBytes(StandardCharsets.UTF_8);
        jpegReaderNative.openForReading(fileNameUtf8, maxWidth, maxHeight);
    }

    /**
     * Reads the JPEG image.
     *
     * @param thread The thread that will do the getting.
     * @param cs The {@link ColorSpace} to use.
     */
    private void readImage(ProgressThread thread, ColorSpace cs)
            throws LCImageLibException, UserCanceledException {
        final ProgressIndicator indicator = ProgressIndicatorFactory.create(thread, jpegReaderNative.m_height);

        // todo: deal with color models other than rgb and grayscale

        if (cs == null) {
            cs =
                    (jpegReaderNative.m_colorsPerPixel == 1
                            ? gray22ColorSpace
                            : jpegReaderNative.m_colorsPerPixel == 3 ? sRGBColorSpace : CMYKColorSpace);
        }

        // Color model for the image (and everything else).
        final ComponentColorModel ccm =
                new ComponentColorModel(cs, false, false, Transparency.OPAQUE,
                        DataBuffer.TYPE_BYTE);

        // Sample model for the readout buffer large enough to hold a tile or a
        // strip of the image.
        final SampleModel jpegTsm = ccm.createCompatibleSampleModel(jpegReaderNative.m_width, TILE_HEIGHT);

        // The readout buffer itself.
        final DataBufferByte db = new DataBufferByte(jpegReaderNative.m_colorsPerPixel * jpegReaderNative.m_width * TILE_HEIGHT);

        // Sample model for the output image.
        final SampleModel tsm = ccm.createCompatibleSampleModel(TILE_WIDTH, TILE_HEIGHT);

        // Layout of the output image.
        final ImageLayout layout =
                new ImageLayout(0, 0, jpegReaderNative.m_width, jpegReaderNative.m_height, 0, 0, TILE_WIDTH, TILE_HEIGHT, tsm, ccm);

        // The output image itself, directly allocated in the file cache.
        final CachedImage image = new CachedImage(layout, fileCache);

        // Load Image Data
        for (int tileY = 0, totalLinesRead = 0; totalLinesRead < jpegReaderNative.m_height; tileY++) {
            if (thread != null && thread.isCanceled()) {
                throw new UserCanceledException();
            }

            final int tileHeight = Math.min(TILE_HEIGHT, jpegReaderNative.m_height - totalLinesRead);

            // Wrap the data buffer with a Raster representing the input data.
            final WritableRaster raster =
                    Raster.createWritableRaster(jpegTsm, db, new Point(0, tileY * TILE_HEIGHT));

            final int linesRead = jpegReaderNative.readScanLines(db.getData(), 0, tileHeight);
            if (linesRead <= 0) {
                System.out.println("Problem with readScanLines, returned: " + linesRead);
                break;
            }

            if (m_hasAdobeSegment && jpegReaderNative.m_colorsPerPixel == 4 && !m_hasAdobeEmbedMarker) {
                //
                // CMYK JPEG images generated by Photoshop are inverted, so we
                // have to invert the data to make it look right.
                //
                LCImageLibUtil.invert(db);
            }

            totalLinesRead += linesRead;

            image.setData(raster);
            indicator.incrementBy(linesRead);
        }
        indicator.setIndeterminate(true);
        m_image = image;
    }

    public void dispose() throws LCImageLibException {
        jpegReaderNative.dispose();
    }
}
/* vim:set et sw=4 ts=4: */

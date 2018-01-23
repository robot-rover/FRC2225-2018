package org.usfirst.frc.team2225.season2018.jetson.label;

import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ContourTracer;
import boofcv.alg.misc.ImageMiscOps;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_I32;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Finds objects in a binary image by tracing their contours.  The output is labeled binary image, set of external
 * and internal contours for each object/blob.  Blobs can be defined using a 4 or 8 connect rule.  The algorithm
 * works by processing the image in a single pass.  When a new object is encountered its contour is traced.  Then
 * the inner pixels are labeled.  If an internal contour is found it will also be traced.  See [1] for all
 * the details.  The original algorithm has been modified to use different connectivity rules.
 * </p>
 *
 * <p>
 * Output: Background pixels (0 in input image) are assigned a value of 0, Each blob is then assigned a unique
 * ID starting from 1 up to the number of blobs.
 * </p>
 *
 * <p>
 * Internally, the input binary image is copied into another image which will have a 1 pixel border of all zeros
 * around it.  This ensures that boundary checks will not need to be done, speeding up the algorithm by about 25%.
 * </p>
 *
 * <p>
 * [1] Fu Chang and Chun-jen Chen and Chi-jen Lu, "A linear-time component-labeling algorithm using contour
 * tracing technique" Computer Vision and Image Understanding, 2004
 * </p>
 *
 * @author Peter Abeles
 */
public class LabelNoContour {

    // traces edge pixels
    private TracerNoContour tracer;

    // binary image with a border of zero.
    private GrayS32 border = new GrayS32(1,1);

    // predeclared/recycled data structures

    // internal book keeping variables
    private int x,y,indexIn,indexOut;

    public int nextLabel;

    /**
     * Configures the algorithm.
     *
     * @param rule Connectivity rule.  4 or 8
     */
    public LabelNoContour( ConnectRule rule ) {
        tracer = new TracerNoContour(rule);
    }

    /**
     * Processes the binary image to find the contour of and label blobs.
     *
     * @param binary Input binary image. Not modified.
     * @param labeled Output. Labeled image.  Modified.
     */
    public int process(GrayS32 binary , GrayS32 labeled ) {
        // initialize data structures

        // ensure that the image border pixels are filled with zero by enlarging the image
        if( border.width != binary.width+2 || border.height != binary.height+2)  {
            border.reshape(binary.width + 2, binary.height + 2);
            ImageMiscOps.fillBorder(border, 0, 1);
        }
        border.subimage(1,1,border.width-1,border.height-1, null).setTo(binary);

        // labeled image must initially be filled with zeros
        ImageMiscOps.fill(labeled,0);

        binary = border;
        tracer.setInputs(binary,labeled);

        // Outside border is all zeros so it can be ignored
        nextLabel = 1;
        for( y = 1; y < binary.height-1; y++ ) {
            indexIn = binary.startIndex + y*binary.stride+1;
            indexOut = labeled.startIndex + (y-1)*labeled.stride;

            for( x = 1; x < binary.width-1; x++ , indexIn++ , indexOut++) {
                int bit = binary.data[indexIn];

                // white pixels are ignored
                if( !(bit == 1) )
                    continue;

                int label = labeled.data[indexOut];

//				long startTraceTime = System.currentTimeMillis();
                boolean handled = false;
                if( label == 0 && binary.data[indexIn - binary.stride ] != 1 ) {
                    handleStep1();
                    handled = true;
                    label = nextLabel;
                    nextLabel++;
                }
                // could be an external and internal contour
                if( binary.data[indexIn + binary.stride ] == 0 ) {
                    handleStep2(labeled, label);
                    handled = true;
                }
                if( !handled ) {
                    handleStep3(labeled);
                }
            }
        }
        return nextLabel - 1;
    }

    /**
     *  Step 1: If the pixel is unlabeled and the pixel above is white, then it
     *          must be an external contour of a newly encountered blob.
     */
    private void handleStep1() {
        tracer.trace(nextLabel,x,y,true);
    }

    /**
     * Step 2: If the pixel below is unmarked and white then it must be an internal contour
     *         Same behavior it the pixel in question has been labeled or not already
     */
    private void handleStep2(GrayS32 labeled, int label) {
        // if the blob is not labeled and in this state it cannot be against the left side of the image
        if( label == 0 )
            label = labeled.data[indexOut-1];
        tracer.trace(label,x,y,false);
    }

    /**
     * Step 3: Must not be part of the contour but an inner pixel and the pixel to the left must be
     *         labeled
     */
    private void handleStep3(GrayS32 labeled) {
        if( labeled.data[indexOut] == 0 )
            labeled.data[indexOut] = labeled.data[indexOut-1];
    }

}

import boofcv.abst.distort.FDistort;
import boofcv.alg.color.ColorHsv;
import boofcv.alg.color.ColorRgb;
import boofcv.alg.color.ColorYuv;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.alg.feature.detect.edge.EdgeSegment;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.alg.interpolate.InterpolationType;
import boofcv.alg.misc.ImageStatistics;
import boofcv.alg.segmentation.ms.ClusterLabeledImage;
import boofcv.alg.shapes.ShapeFittingOps;
import boofcv.core.image.ConvertImage;
import boofcv.core.image.border.BorderType;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.*;
import georegression.struct.point.Point2D_I32;
import javafx.scene.image.PixelFormat;
import org.ddogleg.struct.FastQueue;

import javax.swing.colorchooser.ColorSelectionModel;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static com.sun.javafx.util.Utils.clamp;

/**
 * Demonstration of how to convert a point sequence describing an objects outline/contour into a sequence of line
 * segments.  Useful when analysing shapes such as squares and triangles or when trying to simply the low level
 * pixel output.
 *
 * @author Peter Abeles
 */
public class ExampleFitPolygon {

    private static ListDisplayPanel gui = new ListDisplayPanel();

    public static void testFind(Planar<GrayF32> image, float colorSelect) {
        Planar<GrayF32> work = new Planar<>(GrayF32.class, image.width/8, image.height/8, image.getNumBands());

        InterpolatePixelS<GrayF32> interp = FactoryInterpolation.
                createPixelS(0, 255, InterpolationType.BILINEAR, BorderType.EXTENDED, GrayF32.class);

        // Tell it which image is being interpolated
        interp.setImage(image.bands[0]);
        FDistort scaler = new FDistort();
        scaler.interp(interp);
        for(int i = 0; i < image.getNumBands(); i++){
            scaler.init(image.bands[i], work.bands[i]);
            scaler.scaleExt().apply();
        }
        ColorHsv.rgbToHsv_F32(work, work);
        GrayF32 hue = new GrayF32(work.width, work.height);
        for(int i = 0; i < work.bands[0].data.length; i++) {
            float sqr = Math.max(Math.abs(work.bands[0].data[i] - colorSelect)-0.50f, 0);
            hue.data[i] = clamp(5/((float)Math.sqrt(sqr) - 100 * Math.max(0, 0.5f - work.bands[1].data[i])), 10f, 244f);
        }
        gui.addImage(hue, "PreProcess");
        GrayU8 binary = ThresholdImageOps.threshold(hue, null, 100f, false);
        //BinaryImageOps.removePointNoise(binary, binary);

        binary = BinaryImageOps.dilate4(binary, 2, null);
        binary = BinaryImageOps.erode4(binary, 2, null);
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Pre-Mean");
        BlurImageOps.mean(binary, binary, 12, null);

        GrayS32 labeled = new GrayS32(binary.width, binary.height);
        List<Contour> contours = BinaryImageOps.contour(binary, ConnectRule.FOUR, labeled);
        BlobInfo[] clusters = getBlobInfo(labeled, contours.size());
        System.out.println(Arrays.toString(clusters));
        BufferedImage out = new BufferedImage(binary.width, binary.height, BufferedImage.TYPE_INT_RGB);
        out = VisualizeBinaryData.renderBinary(binary, false, out);
        out = VisualizeBinaryData.render(contours, new Color(0, 200, 0), out);
        Graphics g = out.createGraphics();
        g.setColor(Color.red);
        for(BlobInfo info : clusters) {
            g.drawLine(info.getX(), info.getY() + 20, info.getX(), info.getY() - 20);
            g.drawLine(info.getX() + 20, info.getY(), info.getX() - 20, info.getY());
            g.drawString(String.valueOf(info.size), info.getX() + 2, info.getY() - 2);
        }
        g.dispose();
        gui.addImage(out, "Binary");
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static BlobInfo[] getBlobInfo(GrayS32 labelImage, int numLabels) {
        BlobInfo[] result = new BlobInfo[numLabels];
        for(int i = 0; i < result.length; i++) {
            result[i] = new BlobInfo();
        }

        for( int y = 0; y < labelImage.height; y++ ) {
            int start = labelImage.startIndex + y*labelImage.stride;
            int end = start + labelImage.width;

            for( int index = start; index < end; index++ ) {
                int v = labelImage.data[index];
                if( v > 0 ) {
                    //Minus 1 because index 0 should be layer 1. Layer 0 is background
                    result[v-1].centerX += index - start;
                    result[v-1].centerY += y;
                    result[v-1].size++;
                }
            }
        }
        for(int i = 0; i < result.length; i++) {
            result[i].centerX /= result[i].size;
            result[i].centerY /= result[i].size;
        }
        return result;
    }

    public static class BlobInfo {
        int size = 0;
        double centerX = 0;
        double centerY  = 0;
        public int getX() {
            return (int) Math.round(centerX);
        }

        public int getY() {
            return (int) Math.round(centerY);
        }
        @Override
        public String toString() {
            return "{" + centerX + ", " + centerY + "} : " + size;
        }
    }

    static List<Planar<GrayF32>> images;

    public static void main( String args[] ) {
        // load and convert the image into a usable format
        images = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            images.add(ConvertBufferedImage.convertFromPlanar(
                    UtilImageIO.loadImage("jetson/src/test/resources/testImage-" + i + ".jpg"),
                    null, true, GrayF32.class
            ));
        }

        //fitCannyEdges(input);
        //fitCannyBinary(input);
        //fitBinaryImage(input);
        Scanner kbIn = new Scanner(System.in);
        String input;
        ShowImages.showWindow(gui, "Polygon from Contour", false);
        runGui(color);
        while (kbIn.nextLine().length() == 0){
            //float selection = Float.parseFloat(input);
            gui.reset();
            runGui(color);
            System.out.println("Run");
        }
        System.exit(0);
    }

    final static float color = 1.07f;

    public static void runGui(float colorSelection) {
        for (int i = 0; i < images.size(); i++) {
            gui.addImage(images.get(i), "Stock image #" + i);
            testFind(images.get(i), colorSelection);
        }
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}
import boofcv.alg.color.ColorHsv;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import org.usfirst.frc.team2225.season2018.jetson.BlobInfo;
import org.usfirst.frc.team2225.season2018.jetson.VisionPipeline;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ExampleFitPolygon {

    private ExampleFitPolygon() {}

    private static ListDisplayPanel gui = new ListDisplayPanel();

    public static void testFind(Planar<GrayF32> image) {
        VisionPipeline pipeline = new VisionPipeline(image.width, image.height, 2);
        BlobInfo[] clusters = pipeline.process(image);

        System.out.println(Arrays.toString(clusters));
        BufferedImage out = VisualizeBinaryData.renderLabeledBG(pipeline.getBinaryBuff(1), clusters.length, null);
        Graphics g = out.createGraphics();
        g.setColor(Color.red);
        for(BlobInfo info : clusters) {
            g.drawLine(info.getX(), info.getY() + 20, info.getX(), info.getY() - 20);
            g.drawLine(info.getX() + 20, info.getY(), info.getX() - 20, info.getY());
            g.drawString(String.valueOf(info.size), info.getX() + 2, info.getY() - 2);
        }
        g.dispose();
        /*GrayF32 buffCont = pipeline.getDownBuff(0);
        GrayU8 toBinary = new GrayU8(buffCont.width, buffCont.height);
        for(int i = 0; i < buffCont.data.length; i++)
            toBinary.data[i] = (byte) buffCont.data[i];*/
        gui.addImage(out,"Binary");
    }

    static List<Planar<GrayF32>> images;

    public static void main( String args[] ) {
        loadImages();
        testPerformance();
        testImages();
    }

    public static void testPerformance() {
        VisionPipeline pipeline = new VisionPipeline(images.get(0).width, images.get(0).height, 2);
        System.out.println("Warming JVM");
        for(int i = 0; i < 10; i++) {
            for(Planar<GrayF32> image : images)
                pipeline.process(image);
        }
        System.out.println("Starting Performance Test");
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < 10; i++) {
            for(Planar<GrayF32> image : images)
                pipeline.process(image);
        }
        long endTime = System.currentTimeMillis();
        long total = endTime - startTime;
        double average = total / (double)(10 * images.size());
        double fps = 1/average * 1000;
        System.out.println("Total Execution Time: " + total + "ms, Average Time per Op: " + average + "ms, FPS: " + fps + " fps.");
    }

    public static void loadImages() {
        // load and convert the image into a usable format
        images = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            images.add(ConvertBufferedImage.convertFromPlanar(
                    UtilImageIO.loadImage("jetson/src/test/resources/downscaled/testImage-" + i + ".jpg"),
                    null, true, GrayF32.class
            ));
        }
    }

    public static void testImages() {

        Scanner kbIn = new Scanner(System.in);
        String input;
        ShowImages.showWindow(gui, "Polygon from Contour", true);
        Planar<GrayF32> hsv = images.get(0);
        ColorHsv.rgbToHsv_F32(images.get(0), images.get(0));
        for(int i = 0; i < hsv.bands[0].data.length; i++) {
            float sqr = Math.max(Math.abs(hsv.bands[0].data[i] - 1.07f)-0.50f, 0);
            hsv.bands[0].data[i] = VisionPipeline.clamp(5/((float)Math.sqrt(sqr) - 100 * Math.max(0, 0.5f - hsv.bands[1].data[i])), 10f, 244f);
        }
        gui.addImage(images.get(0), "Real");

        Planar<GrayF32> testImage = images.get(0);
        for(int i = 0; i < 5; i++)
            System.out.println("H: " + testImage.bands[0].data[i] + ", S: " + testImage.bands[1].data[i] + ", V: " + testImage.bands[2].data[i]);


        runGui();
        while (kbIn.nextLine().length() == 0){

            gui.reset();
            runGui();
            System.out.println("Run");
        }
        System.exit(0);
    }

    public static void runGui() {
        for (int i = 0; i < images.size(); i++) {
            gui.addImage(images.get(i), "Stock image #" + i);
            testFind(images.get(i));
        }
    }

}
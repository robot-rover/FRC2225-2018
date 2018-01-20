import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
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
        VisionPipeline pipeline = new VisionPipeline(image.width, image.height, 8);
        BlobInfo[] clusters = pipeline.process(image);

        gui.addImage(pipeline.getHueStep(), "PreProcess");
        System.out.println(Arrays.toString(clusters));
        BufferedImage out = new BufferedImage(pipeline.getBinaryStep().width, pipeline.getBinaryStep().height, BufferedImage.TYPE_INT_RGB);
        out = VisualizeBinaryData.renderBinary(pipeline.getBinaryStep(), false, out);
        out = VisualizeBinaryData.render(pipeline.getContours(), new Color(0, 200, 0), out);
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

    static List<Planar<GrayF32>> images;

    public static void main( String args[] ) {
        loadImages();
        testImages();
    }

    public static void testPerformance() {
        VisionPipeline pipeline = new VisionPipeline(images.get(0).width, images.get(0).height, 8);
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
                    UtilImageIO.loadImage("jetson/src/test/resources/testImage-" + i + ".jpg"),
                    null, true, GrayF32.class
            ));
        }
    }

    public static void testImages() {

        Scanner kbIn = new Scanner(System.in);
        String input;
        ShowImages.showWindow(gui, "Polygon from Contour", false);
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
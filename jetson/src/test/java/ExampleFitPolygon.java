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
import jcuda.Pointer;
import org.bytedeco.javacpp.opencv_videoio;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameRecorder;
import org.usfirst.frc.team2225.season2018.jetson.BlobInfo;
import org.usfirst.frc.team2225.season2018.jetson.VisionPipeline;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_EXPOSURE;

public class ExampleFitPolygon {

    private ExampleFitPolygon() {}

    private static ListDisplayPanel gui = new ListDisplayPanel();

    public static void testFind(Planar<GrayF32> image) {
        /*VisionPipeline pipeline = new VisionPipeline(image.width, image.height, 2);
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
        gui.addImage(out,"Binary");*/
    }

    static List<Planar<GrayF32>> images;

    public static void main( String args[] ) throws IOException {
        /*loadImages();
        testPerformance();*/
        testWebcamPerformance();
        //testImages();
        testWebcam();
        System.out.println("Done");
    }

    @SuppressWarnings("Duplicates")
    public static void testWebcamPerformance() throws IOException {

        final AtomicBoolean readLock = new AtomicBoolean(false);
        final AtomicBoolean processLock = new AtomicBoolean(false);
        VisionPipeline pipeline = new VisionPipeline(640, 480, 2, readLock, processLock);
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.setFrameRate(30);
        grabber.start();

        System.out.println("Framerate: " + grabber.getFrameRate());
        long dur = System.currentTimeMillis();
        for(int i = 0; i < 60; i++) {
            grabber.grab();
        }
        dur = System.currentTimeMillis() - dur;
        System.out.println("Time for 60 frames: " + dur + "ms, Average Time per Op: " + (dur / 60.0) + "ms, FPS: " + (1/(dur / 60.0) * 1000) + " fps.");
        ByteBuffer image = (ByteBuffer) grabber.grab().image[0];
        ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
        System.out.println("Starting Performance Test");
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < 60; i++) {
            synchronized (readLock) {
                while (readLock.get()) {
                    try {
                        readLock.wait(1000);
                    } catch (InterruptedException e) {}
                }
            }
            grabber.grab();
            synchronized (processLock) {
                while (processLock.get()) {
                    try {
                        processLock.wait(1000);
                    } catch (InterruptedException e) {}
                }
            }
            readLock.set(true);
            processLock.set(true);
            pool.execute(() -> {
                long three = System.currentTimeMillis();
                pipeline.process(Pointer.to(image));
                long four = System.currentTimeMillis() - three;
                synchronized (processLock) {
                    processLock.set(false);
                    processLock.notifyAll();
                }
            });
        }
        long endTime = System.currentTimeMillis();
        long total = endTime - startTime;
        double average = total / (double)(60);
        double fps = 1/average * 1000;
        System.out.println("Total Execution Time: " + total + "ms, Average Time per Op: " + average + "ms, FPS: " + fps + " fps.");
        grabber.stop();
    }

    public static void testWebcam() throws IOException {
        ShowImages.showWindow(gui, "Polygon from Contour", true);
        final AtomicBoolean readLock = new AtomicBoolean(false);
        final AtomicBoolean processLock = new AtomicBoolean(false);
        VisionPipeline pipeline = new VisionPipeline(640, 480, 2, readLock, processLock);
        System.out.println("Opening Webcam");
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();
        ByteBuffer image = (ByteBuffer) grabber.grab().image[0];
        ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
        while(true) {
            waitForLock(readLock);
            grabber.grab();
            waitForLock(processLock);
            pool.execute(() -> {
                readLock.get();
                pipeline.process(Pointer.to(image));
                BlobInfo[] clusters = pipeline.getResults();
                Planar<GrayU8> planar = pipeline.getPlanar();
                GrayF32 floatImage = pipeline.getFloatImage();
                GrayS32 in = pipeline.getBinaryBuff(1);
                GrayU8 bin = new GrayU8(in.width, in.height);
                for(int i = 0; i < in.data.length; i++)
                    bin.data[i] = (byte) (in.data[i] == 0 ? 0 : 1);
                BufferedImage out = VisualizeBinaryData.renderBinary(bin, false, null);
                Graphics g = out.createGraphics();
                g.setColor(Color.red);
                for (BlobInfo info : clusters) {
                    g.drawLine(info.getX(), info.getY() + 20, info.getX(), info.getY() - 20);
                    g.drawLine(info.getX() + 20, info.getY(), info.getX() - 20, info.getY());
                    g.drawString(String.valueOf(info.size), info.getX() + 2, info.getY() - 2);
                }
                g.dispose();
                gui.reset();
                /*for(int i = 0; i < 3; i++)
                    gui.addImage(planar.bands[i], "Color band " + i);
                gui.addImage(planar, "Color");
                gui.addImage(floatImage, "floats");*/
                /*for(int i = 0; i < planar.bands[0].data.length; i++) {
                    int x = i % 640;
                    int y = i / 640;
                    planar.bands[0].data[i] = (byte) (bin.data[y/2*320 + x / 2] == 0 ? 0 : 255);
                }*/
                for(int i = 0; i < planar.bands[0].data.length; i++) {
                    planar.bands[0].data[i] = (byte) floatImage.data[i];
                }
                gui.addImage(planar, "Binary");
                synchronized (processLock) {
                    processLock.set(false);
                    processLock.notifyAll();
                }
            });
        }
    }

    private static void waitForLock(AtomicBoolean readLock) {
        synchronized (readLock) {
            while (readLock.get()) {
                try {
                    readLock.wait(1000);
                } catch (InterruptedException e) {}
            }
            readLock.set(true);
        }
    }

    public static void testPerformance() {
        /*VisionPipeline pipeline = new VisionPipeline(images.get(0).width, images.get(0).height, 2);
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
        System.out.println("Total Execution Time: " + total + "ms, Average Time per Op: " + average + "ms, FPS: " + fps + " fps.");*/
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
            hsv.bands[0].data[i] = clamp(5/((float)Math.sqrt(sqr) - 100 * Math.max(0, 0.5f - hsv.bands[1].data[i])), 0f, 255f);
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

    private static float clamp(float val, float max, float min) {
        return Math.max(min, Math.min(max, val));
    }

    public static void runGui() {
        for (int i = 0; i < images.size(); i++) {
            gui.addImage(images.get(i), "Stock image #" + i);
            testFind(images.get(i));
        }
    }

}
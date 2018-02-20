package org.usfirst.frc.team2225.season2018.jetson;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import jcuda.Pointer;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoprocessorExample {
    static GrayU8 image;
    public static void main(String[] args) throws IOException {
        image = new GrayU8(0, 0);
        //image = new Planar<>(GrayU8.class, 0, 0, 1);
        //image = new GrayF32(0,0);
        ImageStream stream = new ImageStream(CoprocessorExample::getImage);
        stream.start();
        final AtomicBoolean readLock = new AtomicBoolean(false);
        final AtomicBoolean processLock = new AtomicBoolean(false);
        VisionPipeline pipeline = new VisionPipeline(640, 480, 2, readLock, processLock);
        System.out.println("Opening Webcam");
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        Process cameraSetup = new ProcessBuilder("v4l2-ctl", "-d", "/dev/video0", "-l", "-c", "brightness=0,contrast=32,saturation=64,hue=0,white_balance_automatic=1,gain_automatic=0,gain=0,sharpness=0").inheritIO().start();
        //Process cameraSetup = new ProcessBuilder("v4l2-ctl",  "-d", "/dev/video0", "-l", "-c", "brightness=133,contrast=5,saturation=83,exposure_auto=1,exposure_absolute=156").inheritIO().start();

        try {
            cameraSetup.waitFor();
        } catch (InterruptedException e) {}
        grabber.start();
        ByteBuffer buff = (ByteBuffer) grabber.grab().image[0];
        ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
        long time = System.currentTimeMillis();
        int iter = 0;
        while(true) {
            waitForLock(readLock);
            grabber.grab();
            waitForLock(processLock);
            pool.execute(() -> {
                readLock.get();
                pipeline.process(Pointer.to(buff));
                GrayS32 in = pipeline.getBinaryBuff(1);
                GrayU8 bin = new GrayU8(in.width, in.height);
                for(int i = 0; i < in.data.length; i++)
                    bin.data[i] = (byte) (in.data[i] == 0 ? 0 : 255);
                synchronized (image) {
                    image = bin;
                }
                synchronized (processLock) {
                    processLock.set(false);
                    processLock.notifyAll();
                }
            });
            if(iter % 10 == 0) {
                System.out.println("Frame Time: " + (System.currentTimeMillis()-time)/10 + "ms");
                time = System.currentTimeMillis();
            }
        }
    }

    public static byte[] getImage() {
        ByteArrayOutputStream imageClone = new ByteArrayOutputStream();
        synchronized (image) {
            try {
                ImageIO.write(ConvertBufferedImage.convertTo(image, null, true), "png", imageClone);
            } catch (IOException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
        return imageClone.toByteArray();
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
}
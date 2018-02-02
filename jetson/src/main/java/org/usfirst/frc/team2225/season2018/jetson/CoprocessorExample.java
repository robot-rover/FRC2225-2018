package org.usfirst.frc.team2225.season2018.jetson;

import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.InterleavedU8;
import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoprocessorExample {
    public static void main(String[] args) {
        AtomicBoolean readLock = new AtomicBoolean(false);
        AtomicBoolean processLock = new AtomicBoolean(false);
        VisionPipeline pipeline = new VisionPipeline(640, 480, 2, readLock, processLock);
        System.out.println("Opening Webcam");
        Webcam cam = UtilWebcamCapture.openDevice("Microsoft® LifeCam HD-3000", 640, 480);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
        //while(true) {
            synchronized (readLock) {
                while (readLock.get()) {
                    System.out.println("read waiting");
                    try {
                        readLock.wait(1000);
                    } catch (InterruptedException e) {}
                }
            }
            readLock.set(true);
            InterleavedU8 interleavedU8 = new InterleavedU8(640, 480, 3);
            ByteBuffer buff = ByteBuffer.wrap(interleavedU8.data);
            cam.getImageBytes(buff);
            synchronized (processLock) {
                while (processLock.get()) {
                    System.out.println("process waiting");
                    try {
                        processLock.wait(1000);
                    } catch (InterruptedException e) {}
                }
            }
            processLock.set(true);
            pool.execute(() -> {
                pipeline.process(interleavedU8);
                BlobInfo[] clusters = pipeline.getResults();
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
                synchronized (processLock) {
                    processLock.set(false);
                    processLock.notifyAll();
                }
                try {
                    ImageIO.write(out, "png", new File("Output.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        //}
    }
}
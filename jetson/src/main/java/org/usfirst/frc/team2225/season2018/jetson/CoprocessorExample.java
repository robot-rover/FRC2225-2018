package org.usfirst.frc.team2225.season2018.jetson;

import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.InterleavedU8;
import jcuda.Pointer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CoprocessorExample {
    public static void main(String[] args) throws IOException {
        VisionPipeline pipeline = new VisionPipeline(640, 480, 2);
        System.out.println("Opening Webcam");
        //Webcam cam = Webcam.getWebcams().get(1).openDevice("Microsoft® LifeCam HD-3000", 640, 480);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));
        //while(true) {
            InterleavedU8 interleavedU8 = new InterleavedU8(640, 480, 3);
            ByteBuffer buff = ByteBuffer.wrap(interleavedU8.data);
            //cam.getImageBytes(buff);
                pipeline.process(Pointer.to(interleavedU8.data));
                BlobInfo[] clusters = pipeline.getResults();
                GrayS32 in = pipeline.getBinaryBuff(1);
                GrayU8 bin = new GrayU8(in.width, in.height);
        //}
    }
}
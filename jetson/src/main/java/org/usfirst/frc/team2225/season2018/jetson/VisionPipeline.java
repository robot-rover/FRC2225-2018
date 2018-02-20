package org.usfirst.frc.team2225.season2018.jetson;

import boofcv.struct.ConnectRule;
import boofcv.struct.image.*;
import georegression.struct.point.Point2D_I32;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdevice;
import jcuda.driver.CUdeviceptr;
import org.usfirst.frc.team2225.season2018.jetson.label.LabelNoContour;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static jcuda.driver.JCudaDriver.*;

public class VisionPipeline {
    final static float color = 1.07f;

    Shader interToPlanar;
    Shader combinedInit;
    Shader downThresh;
    Shader dilate4;
    Shader erode4;
    Shader blurMeanBin;

    CUcontext pctx;
    CUdevice dev;

    CUdeviceptr interleaved;
    CUdeviceptr floatImage;
    CUdeviceptr planarHsvImage[] = new CUdeviceptr[3];
    CUdeviceptr binBuff[] = new CUdeviceptr[2];

    int global_work_size;
    int global_work_size_scaled;
    int local_work_size;

    GrayS32 binary;
    GrayS32 labeled;
    LabelNoContour alg;
    Point2D_I32 inputDimension;
    Point2D_I32 scaledDimension;
    int scaleFactor;

    BlobInfo[] results;

    final boolean async;
    final AtomicBoolean readLock;
    final AtomicBoolean processLock;

    public VisionPipeline(int inputWidth, int inputHeight, int scaleFactor, AtomicBoolean readLock, AtomicBoolean processLock) throws IOException {
        if(readLock == null || processLock == null) {
            async = false;
            this.processLock = null;
            this.readLock = null;
        } else {
            async = true;
            this.readLock = readLock;
            this.processLock = processLock;
        }
        inputDimension = new Point2D_I32(inputWidth, inputHeight);
        if (scaleFactor % 2 != 0)
            throw new RuntimeException("Odd scale factors are not supported");
        this.scaleFactor = scaleFactor;
        scaledDimension = new Point2D_I32(inputWidth / scaleFactor, inputHeight / scaleFactor);

        binary = new GrayS32(scaledDimension.x, scaledDimension.y);
        labeled = new GrayS32(scaledDimension.x, scaledDimension.y);
        alg = new LabelNoContour(ConnectRule.FOUR);

        setExceptionsEnabled(true);

        cuInit(0);
        pctx = new CUcontext();
        dev = new CUdevice();
        cuDeviceGet(dev, 0);
        byte[] nameBytes = new byte[20];
        cuDeviceGetName(nameBytes, 20, dev);
        System.out.println(new String(nameBytes));
        cuCtxCreate(pctx, 0, dev);

        interleaved = new CUdeviceptr();
        cuMemAlloc(interleaved, Sizeof.BYTE * inputDimension.x * inputDimension.y * 3);
        floatImage = new CUdeviceptr();
        cuMemAlloc(floatImage, Sizeof.FLOAT * inputDimension.x * inputDimension.y * 3);
        for(int i = 0; i < 3; i++) {
            planarHsvImage[i] = new CUdeviceptr();
            cuMemAlloc(planarHsvImage[i], Sizeof.FLOAT * inputDimension.x * inputDimension.y);
        }
        for(int i = 0; i < 2; i++) {
            binBuff[i] = new CUdeviceptr();
            cuMemAlloc(binBuff[i], Sizeof.FLOAT * scaledDimension.x * scaledDimension.y);
        }
        final boolean recompile = true;
        interToPlanar = new Shader("interToPlanar", recompile);
        combinedInit = new Shader("combinedInit", recompile);
        downThresh = new Shader("scaleThresh", recompile);
        blurMeanBin = new Shader("blurMeanBin", recompile);

        global_work_size = inputDimension.x * inputDimension.y;
        global_work_size_scaled = scaledDimension.x * scaledDimension.y;
        local_work_size = 64;

        setKernelArgs();
    }

    public VisionPipeline(int inputWidth, int inputHeight, int scaleFactor) throws IOException {
        this(inputWidth, inputHeight, scaleFactor, null, null);
    }

    private void setKernelArgs() {
        // Set the arguments for the rgb2Hsv kernel
        interToPlanar.params = Pointer.to(
                Pointer.to(interleaved),
                Pointer.to(planarHsvImage[0]),
                Pointer.to(planarHsvImage[1]),
                Pointer.to(planarHsvImage[2]),
                Pointer.to(new int[]{inputDimension.x * inputDimension.y})
        );

        combinedInit.params = Pointer.to(
                Pointer.to(planarHsvImage[0]),
                Pointer.to(planarHsvImage[1]),
                Pointer.to(planarHsvImage[2]),
                Pointer.to(floatImage),
                Pointer.to(new int[]{inputDimension.x * inputDimension.y})
        );

        downThresh.params = Pointer.to(
                Pointer.to(floatImage),
                Pointer.to(binBuff[0]),
                Pointer.to(new int[]{scaleFactor}),
                Pointer.to(new int[]{inputDimension.x}),
                Pointer.to(new int[]{inputDimension.y}),
                Pointer.to(new float[]{100f}),
                Pointer.to(new int[]{0})
        );

        blurMeanBin.params = Pointer.to(
                Pointer.to(binBuff[0]),
                Pointer.to(binBuff[1]),
                Pointer.to(new int[]{scaledDimension.x}),
                Pointer.to(new int[]{scaledDimension.y}),
                Pointer.to(new int[]{5})
        );
    }

    /**
     * Set readLock and writeLock to true before calling this method
     */
    public void process(Pointer inputImage) {
        if(async && (!readLock.get() || !processLock.get())) {
            throw new RuntimeException("readLock and processLock should both be true when calling process");
        }

        cuCtxSetCurrent(pctx);
        cuMemcpyHtoD(interleaved, inputImage, Sizeof.BYTE * inputDimension.x * inputDimension.y * 3);
        if (async)
            synchronized (readLock) {
                readLock.set(false);
                readLock.notifyAll();
            }
        cuLaunchKernel(interToPlanar.function,
                (global_work_size + 1023) / 1024, 1, 1,           // Grid dimension
                1024, 1, 1,  // Block dimension
                0, null,           // Shared memory size and stream
                interToPlanar.params, null);
        cuLaunchKernel(combinedInit.function,
                (global_work_size + 1023) / 1024, 1, 1,
                1024, 1, 1,
                0, null,
                combinedInit.params, null);
        cuLaunchKernel(downThresh.function,
                (global_work_size_scaled + 1023) / 1024, 1, 1,
                1024,1, 1,
                0, null,
                downThresh.params, null);
        cuLaunchKernel(blurMeanBin.function,
                (global_work_size_scaled + 1023) / 1024, 1, 1,
                1024, 1, 1,
                0, null,
                blurMeanBin.params, null);
        cuCtxSynchronize();
        cuMemcpyDtoH(Pointer.to(binary.data), binBuff[1], Sizeof.FLOAT * binary.data.length);
        int size = alg.process(binary, labeled);
        results = getBlobInfo(labeled, size);
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

        for (BlobInfo info : result) {
            info.centerX /= info.size;
            info.centerY /= info.size;
        }
        return result;
    }

    public BlobInfo[] getResults() {
        return results;
    }

    public GrayS32 getBinaryBuff(int index) {
        GrayS32 image = new GrayS32(scaledDimension.x, scaledDimension.y);
        cuMemcpyDtoH(Pointer.to(image.data), binBuff[index], Sizeof.INT * scaledDimension.x * scaledDimension.y);
        return image;
    }

    public Planar<GrayU8> getPlanar() {
        Planar<GrayU8> image = new Planar<>(GrayU8.class, inputDimension.x, inputDimension.y, 3);
        for(int i = 0; i < 3; i++)
            cuMemcpyDtoH(Pointer.to(image.bands[i].data), planarHsvImage[i], Sizeof.BYTE * inputDimension.x * inputDimension.y);
        return image;
    }

    public GrayF32 getFloatImage() {
        GrayF32 image = new GrayF32(inputDimension.x, inputDimension.y);
        cuMemcpyDtoH(Pointer.to(image.data), floatImage, Sizeof.FLOAT * inputDimension.x * inputDimension.y);
        return image;
    }

    public GrayS32 getBinary() {
        return binary;
    }
}

package org.usfirst.frc.team2225.season2018.jetson;

import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.InterleavedU8;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_I32;
import org.jocl.*;
import org.usfirst.frc.team2225.season2018.jetson.label.LabelNoContour;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.jocl.CL.*;

public class VisionPipeline {
    final static float color = 1.07f;

    final static int platformIndex = 0;
    final static long deviceType = CL_DEVICE_TYPE_ALL;
    final static int deviceIndex = 0;

    Shader interToPlanar;
    Shader combinedInit;
    Shader downThresh;
    Shader dilate4;
    Shader erode4;
    Shader blurMeanBin;

    cl_context context;
    cl_command_queue commandQueue;

    cl_mem interleaved;
    cl_mem planarHsvImage[] = new cl_mem[3];
    cl_mem binBuff[] = new cl_mem[2];

    long[] global_work_size;
    long[] global_work_size_scaled;
    long[] local_work_size;

    GrayS32 binary;
    GrayS32 labeled;
    LabelNoContour alg;
    Point2D_I32 inputDimension;
    Point2D_I32 scaledDimension;
    int scaleFactor;
    AtomicBoolean readLock;
    AtomicBoolean writeLock;

    BlobInfo[] results;

    public VisionPipeline(int inputWidth, int inputHeight, int scaleFactor, AtomicBoolean readLock, AtomicBoolean writeLock) {
        inputDimension = new Point2D_I32(inputWidth, inputHeight);
        if (scaleFactor % 2 != 0)
            throw new RuntimeException("Odd scale factors are not supported");
        this.scaleFactor = scaleFactor;
        scaledDimension = new Point2D_I32(inputWidth / scaleFactor, inputHeight / scaleFactor);
        this.readLock = readLock;
        this.writeLock = writeLock;

        binary = new GrayS32(scaledDimension.x, scaledDimension.y);
        labeled = new GrayS32(scaledDimension.x, scaledDimension.y);
        alg = new LabelNoContour(ConnectRule.FOUR);

        setExceptionsEnabled(true);

        // Obtain the number of OpenCL platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        cl_queue_properties properties = new cl_queue_properties();

        //properties.addProperty(CL_QUEUE_PROPERTIES, CL_QUEUE_PROFILING_ENABLE);

        // Create a command-queue for the selected device
        commandQueue =
                clCreateCommandQueueWithProperties(context, device, properties, null);

        interleaved = clCreateBuffer(context, CL_MEM_READ_ONLY & CL_MEM_HOST_WRITE_ONLY,
                Sizeof.cl_uchar * inputDimension.x * inputDimension.y * 3, null, null);
        for(int i = 0; i < 3; i++)
            planarHsvImage[i] = clCreateBuffer(context, 0L,
                    Sizeof.cl_float * inputDimension.x * inputDimension.y, null, null);
        for(int i = 0; i < 2; i++)
            binBuff[i] = clCreateBuffer(context, 0L,
                    Sizeof.cl_float * scaledDimension.x * scaledDimension.y, null, null);

        interToPlanar = new Shader("interToPlanar", context);
        combinedInit = new Shader("combinedInit", context);
        downThresh = new Shader("scaleThresh", context);
        blurMeanBin = new Shader("blurMeanBin", context);

        global_work_size = new long[]{inputDimension.x * inputDimension.y};
        global_work_size_scaled = new long[]{scaledDimension.x * scaledDimension.y};
        local_work_size = new long[]{64};

        setKernelArgs();
    }

    private void setKernelArgs() {
        // Set the arguments for the rgb2Hsv kernel
        clSetKernelArg(interToPlanar.kernel, 0,
                Sizeof.cl_mem, Pointer.to(interleaved));
        clSetKernelArg(interToPlanar.kernel, 1,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[0]));
        clSetKernelArg(interToPlanar.kernel, 2,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[1]));
        clSetKernelArg(interToPlanar.kernel, 3,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[2]));
        clSetKernelArg(interToPlanar.kernel, 4,
                Sizeof.cl_int, Pointer.to(new int[]{inputDimension.x * inputDimension.y}));

        // Set the arguments for the rgb2Hsv kernel
        clSetKernelArg(combinedInit.kernel, 0,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[0]));
        clSetKernelArg(combinedInit.kernel, 1,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[1]));
        clSetKernelArg(combinedInit.kernel, 2,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[2]));
        clSetKernelArg(combinedInit.kernel, 3,
                Sizeof.cl_int, Pointer.to(new int[]{inputDimension.x * inputDimension.y}));

        // Set the arguments for the downscale kernel
        clSetKernelArg(downThresh.kernel, 0,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[0]));
        clSetKernelArg(downThresh.kernel, 1,
                Sizeof.cl_mem, Pointer.to(binBuff[0]));
        clSetKernelArg(downThresh.kernel, 2,
                Sizeof.cl_int, Pointer.to(new int[]{scaleFactor}));
        clSetKernelArg(downThresh.kernel, 3,
                Sizeof.cl_int, Pointer.to(new int[]{inputDimension.x}));
        clSetKernelArg(downThresh.kernel, 4,
                Sizeof.cl_int, Pointer.to(new int[]{inputDimension.y}));
        clSetKernelArg(downThresh.kernel, 5,
                Sizeof.cl_float, Pointer.to(new float[]{100f}));
        clSetKernelArg(downThresh.kernel, 6,
                Sizeof.cl_int, Pointer.to(new int[]{0}));

        // Set the arguments for the kernel
        clSetKernelArg(blurMeanBin.kernel, 0,
                Sizeof.cl_mem, Pointer.to(binBuff[0]));
        clSetKernelArg(blurMeanBin.kernel, 1,
                Sizeof.cl_mem, Pointer.to(binBuff[1]));
        clSetKernelArg(blurMeanBin.kernel, 2,
                Sizeof.cl_int, Pointer.to(new int[]{scaledDimension.x}));
        clSetKernelArg(blurMeanBin.kernel, 3,
                Sizeof.cl_int, Pointer.to(new int[]{scaledDimension.y}));
        clSetKernelArg(blurMeanBin.kernel, 4,
                Sizeof.cl_int, Pointer.to(new int[]{10}));
    }

    /**
     * Set readLock and writeLock to true before calling this method
     * @param input
     */
    public void process(InterleavedU8 input) {
        if(!readLock.get() || !writeLock.get())
            throw new IllegalStateException("Must set readLock and writeLock to true before calling VisionPipeline#process()");
        clEnqueueWriteBuffer(commandQueue, interleaved, true, 0, Sizeof.cl_uchar * input.data.length,
                Pointer.to(input.data), 0, null, null);

        synchronized (readLock) {
            readLock.set(false);
            readLock.notifyAll();
        }

        clEnqueueNDRangeKernel(commandQueue, interToPlanar.kernel, 1, null,
                global_work_size, local_work_size, 0, null, null);
        clEnqueueNDRangeKernel(commandQueue, combinedInit.kernel, 1, null,
                global_work_size, local_work_size, 0, null, null);
        clEnqueueNDRangeKernel(commandQueue, downThresh.kernel, 1, null,
                global_work_size_scaled, local_work_size, 0, null, null);
        clEnqueueNDRangeKernel(commandQueue, blurMeanBin.kernel, 1, null,
                global_work_size_scaled, local_work_size, 0, null, null);
        clEnqueueReadBuffer(commandQueue, binBuff[1], true, 0,
                Sizeof.cl_float * binary.data.length, Pointer.to(binary.data), 0, null, null);
        clFinish(commandQueue);
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
        clEnqueueReadBuffer(commandQueue, binBuff[index], true, 0,
                Sizeof.cl_int * scaledDimension.x * scaledDimension.y, Pointer.to(image.data), 0, null, null);
        return image;
    }

    public Planar<GrayF32> getPlanar() {
        Planar<GrayF32> image = new Planar<>(GrayF32.class, inputDimension.x, inputDimension.y, 3);
        for(int i = 0; i < 3; i++)
            clEnqueueReadBuffer(commandQueue, planarHsvImage[i], true, 0,
                    Sizeof.cl_float * inputDimension.x * inputDimension.y, Pointer.to(image.bands[i].data), 0, null, null);
        return image;
    }

    public GrayS32 getBinary() {
        return binary;
    }
}

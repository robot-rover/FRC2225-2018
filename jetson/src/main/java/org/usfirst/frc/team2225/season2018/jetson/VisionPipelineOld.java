package org.usfirst.frc.team2225.season2018.jetson;

import boofcv.abst.distort.FDistort;
import boofcv.alg.color.ColorHsv;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import org.jocl.*;
import org.usfirst.frc.team2225.season2018.jetson.label.LabelNoContour;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jocl.CL.*;
import static org.usfirst.frc.team2225.season2018.jetson.VisionPipeline.getBlobInfo;

public class VisionPipelineOld {
    final static float color = 1.07f;

    final static int platformIndex = 0;
    final static long deviceType = CL_DEVICE_TYPE_ALL;
    final static int deviceIndex = 0;

    Shader combinedInit;
    Shader downThresh;
    Shader dilate4;
    Shader erode4;
    Shader blurMeanBin;

    cl_context context;
    cl_command_queue commandQueue;

    int scaleFactor;
    int width;
    int height;

    cl_mem planarHsvImage[] = new cl_mem[3];
    cl_mem binBuff[] = new cl_mem[2];

    long[] global_work_size;
    long[] global_work_size_scaled;
    long[] local_work_size;

    GrayS32 binary;
    GrayS32 labeled;


    public VisionPipelineOld(int width, int height, int scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.width = width;
        this.height = height;

        binary = new GrayS32(width / scaleFactor, height / scaleFactor);
        labeled = new GrayS32(width / scaleFactor, height / scaleFactor);
        // Switch from Error codes to Exceptions
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

        properties.addProperty(CL_QUEUE_PROPERTIES, CL_QUEUE_PROFILING_ENABLE);

        // Create a command-queue for the selected device
        commandQueue =
                clCreateCommandQueueWithProperties(context, device, properties, null);

        for(int i = 0; i < 3; i++)
            planarHsvImage[i] = clCreateBuffer(context, CL_MEM_USE_HOST_PTR,
                    Sizeof.cl_float * width * height, Pointer.to(new float[width * height]), null);
        for(int i = 0; i < 2; i++)
            binBuff[i] = clCreateBuffer(context, CL_MEM_USE_HOST_PTR,
                    Sizeof.cl_float * width * height / scaleFactor / scaleFactor, Pointer.to(new float[width * height / scaleFactor / scaleFactor]), null);

        combinedInit = new Shader("combinedInit", context);
        downThresh = new Shader("scaleThresh", context);
        blurMeanBin = new Shader("blurMeanBin", context);

        setKernelArgs(width, height);

        global_work_size = new long[]{width * height};
        global_work_size_scaled = new long[]{global_work_size[0] / scaleFactor};
        local_work_size = new long[]{64};
    }

    private void setKernelArgs(int imageWidth, int imageHeight) {
        int imageLength = imageWidth * imageHeight;
        // Set the arguments for the rgb2Hsv kernel
        clSetKernelArg(combinedInit.kernel, 0,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[0]));
        clSetKernelArg(combinedInit.kernel, 1,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[1]));
        clSetKernelArg(combinedInit.kernel, 2,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[2]));
        clSetKernelArg(combinedInit.kernel, 3,
                Sizeof.cl_int, Pointer.to(new int[]{imageLength}));

        // Set the arguments for the downscale kernel
        clSetKernelArg(downThresh.kernel, 0,
                Sizeof.cl_mem, Pointer.to(planarHsvImage[0]));
        clSetKernelArg(downThresh.kernel, 1,
                Sizeof.cl_mem, Pointer.to(binBuff[0]));
        clSetKernelArg(downThresh.kernel, 2,
                Sizeof.cl_int, Pointer.to(new int[]{scaleFactor}));
        clSetKernelArg(downThresh.kernel, 3,
                Sizeof.cl_int, Pointer.to(new int[]{width}));
        clSetKernelArg(downThresh.kernel, 4,
                Sizeof.cl_int, Pointer.to(new int[]{height}));
        clSetKernelArg(downThresh.kernel, 5,
                Sizeof.cl_float, Pointer.to(new float[]{100f}));
        clSetKernelArg(downThresh.kernel, 6,
                Sizeof.cl_int, Pointer.to(new int[]{0}));

        int scaledLength = imageLength / scaleFactor / scaleFactor;

        // Set the arguments for the kernel
        clSetKernelArg(blurMeanBin.kernel, 0,
                Sizeof.cl_mem, Pointer.to(binBuff[0]));
        clSetKernelArg(blurMeanBin.kernel, 1,
                Sizeof.cl_mem, Pointer.to(binBuff[1]));
        clSetKernelArg(blurMeanBin.kernel, 2,
                Sizeof.cl_int, Pointer.to(new int[]{imageWidth / scaleFactor}));
        clSetKernelArg(blurMeanBin.kernel, 3,
                Sizeof.cl_int, Pointer.to(new int[]{imageHeight / scaleFactor}));
        clSetKernelArg(blurMeanBin.kernel, 4,
                Sizeof.cl_int, Pointer.to(new int[]{6}));
    }

    public BlobInfo[] process(Planar<GrayF32> image, AtomicBoolean readLock, AtomicBoolean processLock) {
        cl_event[] events = new cl_event[5];
        readLock.set(true);
        processLock.set(true);
        for(int i = 0; i < events.length; i++)
            events[i] = null;
        //long one = System.currentTimeMillis();
        for(int i = 0; i < 3; i++)
            clEnqueueWriteBuffer(commandQueue, planarHsvImage[i], true, 0, Sizeof.cl_float * image.bands[i].data.length,
                    Pointer.to(image.bands[i].data), 0, null, events[0]);
        synchronized (readLock) {
            readLock.set(false);
            readLock.notifyAll();
        }
        //long two = System.currentTimeMillis();
        clEnqueueNDRangeKernel(commandQueue, combinedInit.kernel, 1, null,
                global_work_size, local_work_size, 0, null, events[1]);
        clEnqueueNDRangeKernel(commandQueue, downThresh.kernel, 1, null,
                global_work_size_scaled, local_work_size, 0, null, events[2]);
        clEnqueueNDRangeKernel(commandQueue, blurMeanBin.kernel, 1, null,
                global_work_size_scaled, local_work_size, 0, null, events[3]);
        clEnqueueReadBuffer(commandQueue, binBuff[1], true, 0,
                Sizeof.cl_float * binary.data.length, Pointer.to(binary.data), 0, null, events[4]);
        clFinish(commandQueue);
        //long three = System.currentTimeMillis();
        LabelNoContour alg = new LabelNoContour(ConnectRule.FOUR);
        int size = alg.process(binary, labeled);
        BlobInfo[] results = getBlobInfo(labeled, size);
        /*long four = System.currentTimeMillis();
        System.out.println("Buffer Write: " + (two - one));
        System.out.println("Process and Read: " + (three - two));
        System.out.println("Component Labelling: " + (four - three));
        for(int i = 0; i < events.length; i++) {
            long[] queued = new long[1];
            clGetEventProfilingInfo(events[i], CL_PROFILING_COMMAND_QUEUED, Sizeof.cl_ulong, Pointer.to(queued), null);
            long[] submit = new long[1];
            clGetEventProfilingInfo(events[i], CL_PROFILING_COMMAND_SUBMIT, Sizeof.cl_ulong, Pointer.to(submit), null);
            long[] start = new long[1];
            clGetEventProfilingInfo(events[i], CL_PROFILING_COMMAND_START, Sizeof.cl_ulong, Pointer.to(start), null);
            long[] end = new long[1];
            clGetEventProfilingInfo(events[i], CL_PROFILING_COMMAND_END, Sizeof.cl_ulong, Pointer.to(end), null);
            System.out.println(getShaderName(i) + ": Queued -- " + String.format("%10d", (submit[0] - queued[0])) + " -> Submit -- " + String.format("%8d", (start[0] - submit[0])) + " -> Start -- " + String.format("%8d", (end[0] - start[0])) + " -> End");
        }
        System.exit(0);*/

        return results;
    }

    private String getShaderName(int index) {
        switch (index) {
            case 0: return "Buffer Write";
            case 1: return "combinedInit";
            case 2: return "scaleThresh ";
            case 3: return "blurMeanBin ";
            case 4: return "Buffer Read ";
            default: return "UNKNOWN    ";
        }
    }

    public GrayS32 getLabeled() {
        return labeled;
    }

    public GrayS32 getBinaryBuff(int index) {
        GrayS32 image = new GrayS32(width / scaleFactor, height / scaleFactor);
            clEnqueueReadBuffer(commandQueue, binBuff[index], true, 0,
                    Sizeof.cl_int * width * height / scaleFactor / scaleFactor, Pointer.to(image.data), 0, null, null);
        return image;
    }

    public GrayF32 getDownBuff(int index) {
        GrayF32 image = new GrayF32(width / scaleFactor, height / scaleFactor);
        clEnqueueReadBuffer(commandQueue, binBuff[index], true, 0,
                Sizeof.cl_float * width * height / scaleFactor / scaleFactor, Pointer.to(image.data), 0, null, null);
        return image;
    }

    public Planar<GrayF32> getPlanar() {
        Planar<GrayF32> image = new Planar<>(GrayF32.class, width, height, 3);
        for(int i = 0; i < 3; i++)
            clEnqueueReadBuffer(commandQueue, planarHsvImage[i], true, 0,
                Sizeof.cl_float * width * height, Pointer.to(image.bands[i].data), 0, null, null);
        return image;
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}

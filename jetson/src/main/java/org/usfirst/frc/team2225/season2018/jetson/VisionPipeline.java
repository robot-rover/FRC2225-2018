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

import java.util.ArrayList;
import java.util.List;

public class VisionPipeline {
    public Planar<GrayF32> getHsvStep() {
        return hsv;
    }

    public GrayF32 getScaledStep() {
        return scaled;
    }

    public GrayF32 getHueStep() {
        return hue;
    }

    public GrayU8 getBinaryStep() {
        return binary;
    }

    public GrayS32 getLabeledStep() {
        return labeled;
    }

    public List<Contour> getContours() {
        return contours;
    }

    final Planar<GrayF32> hsv;
    final FDistort scaler;
    final GrayF32 scaled;
    final GrayF32 hue;
    final GrayU8 binary;
    final GrayU8 binaryLink;
    final GrayS32 labeled;

    List<Contour> contours;

    final static float color = 1.07f;

    public VisionPipeline(int width, int height, int downscale) {
        hsv = new Planar<>(GrayF32.class, width, height, 3);
        hue = new GrayF32(width, height);
        scaler = new FDistort();
        scaled = new GrayF32(width/downscale, height/downscale);
        binary = new GrayU8(width/downscale, height/downscale);
        binaryLink = new GrayU8(width/downscale, height/downscale);
        labeled = new GrayS32(width/downscale, height/downscale);
        contours = new ArrayList<>();
    }

    public BlobInfo[] process(Planar<GrayF32> image) {
        ColorHsv.rgbToHsv_F32(image, hsv);

        for(int i = 0; i < hsv.bands[0].data.length; i++) {
            float sqr = Math.max(Math.abs(hsv.bands[0].data[i] - color)-0.50f, 0);
            hue.data[i] = VisionPipeline.clamp(5/((float)Math.sqrt(sqr) - 100 * Math.max(0, 0.5f - hsv.bands[1].data[i])), 10f, 244f);
        }
        scaler.init(hue, scaled);
        scaler.scaleExt().apply();

        ThresholdImageOps.threshold(scaled, binary, 100f, false);
        BinaryImageOps.dilate4(binary, 2, binaryLink);
        BinaryImageOps.erode4(binaryLink, 2, binary);
        BlurImageOps.mean(binary, binary, 12, binaryLink);

        contours = BinaryImageOps.contour(binary, ConnectRule.FOUR, labeled);
        return VisionPipeline.getBlobInfo(labeled, contours.size());
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

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}

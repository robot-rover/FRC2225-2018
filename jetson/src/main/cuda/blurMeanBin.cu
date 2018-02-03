extern "C"
__device__ int clamp(int x, int a, int b){
  return max(a, min(b, x));
}

extern "C"
__device__ int getPixel(int x, int y, int width, int height) {
    int xBound = clamp(x, 0, width - 1);
    int yBound = clamp(y, 0, height - 1);
    return xBound + yBound * width;
}

extern "C"
__global__ void blurMeanBin(int *src, int *dst, const int width, const int height, const int radius) {
    int size = width * height;
    int gid = blockIdx.x *blockDim.x + threadIdx.x;
    if (gid < size) {
        int x = gid % width;
        int xMx = x + radius;
        int xMn = x - radius;
        int y = gid / width;
        int yMx = y + radius;
        y = y - radius;
        float mean = 0;
        for (; y <= yMx; y++) {
            for(x = xMn; x <= xMx; x++) {
                mean = mean + src[getPixel(x, y, width, height)];
            }
        }
        mean = mean / (2*radius + 1) / (2*radius+1);
        if(mean > 0.81) {
            dst[gid] = 1;
        } else {
            dst[gid] = 0;
        }
    }
}
extern "C"
__global__ void scaleThresh(float *src, int *dst, const int scale, const int width, const int height, const float threshold, const int down) {
    int gid  = blockIdx.x *blockDim.x + threadIdx.x;
    int size = width * height;
    int downSqr = scale * scale;
    if(gid < size / downSqr) {
        int gidPerRow = width / scale;
        int y = gid / gidPerRow;
        y = y * scale;
        int yMax = y + scale;
        int x = gid % gidPerRow;
        x = x * scale;
        int xInit = x;
        int xMax = x + scale;
        float val = 0;
        int addr;
        for(; y < yMax; y++) {
            x = xInit;
            for(; x < xMax; x++) {
                addr = y * width + x;
                val = val + src[addr];
            }
        }

        if( val / downSqr <= threshold) {
            dst[gid] = down;
        } else {
            dst[gid] = !down;
        }
    }
}
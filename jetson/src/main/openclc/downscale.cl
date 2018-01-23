__kernel void downscale(__global float *src, __global float *dst, const int scale, const int width, const int height) {
    int gid  = get_global_id(0);
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
        dst[gid] = val / downSqr;
    }
}
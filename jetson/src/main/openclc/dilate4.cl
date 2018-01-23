int getPixel(int x, int y, int width, int height) {
    int xBound = clamp(x, 0, width - 1);
    int yBound = clamp(y, 0, height - 1);
    return xBound + yBound * width;
}

__kernel void dilate4(__global int *src, __global int *dst, const int width, const int height) {
    int size = width * height;
    int gid = get_global_id(0);
    if (gid < size) {
        int x = gid % width;
        int y = gid / width;
        int result = src[getPixel(x, y, width, height)];
        result = result | src[getPixel(x+1, y, width, height)];
        result = result | src[getPixel(x-1, y, width, height)];
        result = result | src[getPixel(x, y+1, width, height)];
        result = result | src[getPixel(x, y-1, width, height)];
        if(result) {
            dst[gid] = 1;
        } else {
            dst[gid] = 0;
        }
    }
}
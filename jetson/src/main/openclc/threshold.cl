__kernel void threshold(__global float *src, __global int *dst, int const down, float const threshold, int const size) {
    int gid = get_global_id(0);
    if(gid < size) {
        if(src[gid] <= threshold) {
            dst[gid] = down;
        } else {
            dst[gid] = !down;
        }
    }
}
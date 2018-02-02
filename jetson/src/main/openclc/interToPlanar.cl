__kernel void interToPlanar(__global const uchar *interleaved, __global float *r, __global float *g, __global float *b, const int size) {
    int gid = get_global_id(0);
    if(gid < size) {
        int index = gid * 3;
        r[gid] = (float) interleaved[index];
        g[gid] = (float) interleaved[index+1];
        b[gid] = (float) interleaved[index+2];
    }
}
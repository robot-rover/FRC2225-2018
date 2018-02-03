extern "C"
__global__ void interToPlanar(const char *interleaved, char *r, char *g, char *b, const int size) {
    int gid = blockIdx.x *blockDim.x + threadIdx.x;
    if(gid < size) {
        int index = gid * 3;
        r[gid] = interleaved[index+2];
        g[gid] = interleaved[index+1];
        b[gid] = interleaved[index];
    }
}
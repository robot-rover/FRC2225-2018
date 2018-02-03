extern "C"
__device__ int clamp(int x, int a, int b) {
  return max(a, min(b, x));
}

extern "C"
__global__ void combinedInit(unsigned char *ra, unsigned char *ga, unsigned char *ba, float *val, const int size) {
    int gid = blockIdx.x *blockDim.x + threadIdx.x;
    if (gid < size) {
        // Begin rgbToHsv
        unsigned char r = ra[gid];
        unsigned char g = ga[gid];
        unsigned char b = ba[gid];
        float hue;
        float sat;
        unsigned char mx = max(r, g);
        mx = max(mx, b);
        unsigned char mn = (float) min(r, g);
        mn = min(mn, b);

        float delta = (float) (mx - mn);
        //bv[gid] = mx;

        if(mx == 0) {
            val[gid] = 0.0f;
            return;
        } else {
            sat = delta / (float) mx;

            if(r == mx)
                hue = (float)(g - b) / delta;
            else if( g == mx )
                hue = 2 + (float)( b - r ) / delta;
            else
                hue = 4 + (float)( r - g ) / delta;

            hue = hue * 1.0471976f;

            if (hue < 0)
                hue = hue + 6.2831855;
        }

        // Begin hue

        float sqr = hue - 1.07f;
        sqr = fabs(sqr) - 0.3f;
        sqr = max(sqr, 0.0f);
        sqr = sqrt(sqr);
        float sVal = 0.5f - sat;
        mx = max(0.0f, sVal);
        mx = mx * 100.0f;
        sqr = sqr - mx;
        sqr = 5.0f/sqr;
        val[gid] = clamp(sqr, 0.0f, 255.0f);
    }
}
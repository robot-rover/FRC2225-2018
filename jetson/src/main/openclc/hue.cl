__kernel void hue(__global float *hm, __global float *s, __global float *v, int const size) {
    int gid = get_global_id(0);
    if(gid < size) {
        float sqr = hm[gid] - 1.07f;
        sqr = fabs((float)sqr);
        sqr = sqr - 0.5f;
        sqr = max(sqr, 0.0f);
        sqr = sqrt(sqr);
        float sVal = 0.5f - s[gid];
        float mx = max(0.0f, sVal);
        mx = mx * 100.0f;
        sqr = sqr - mx;
        sqr = 5.0f/sqr;
        hm[gid] = clamp(sqr, 10.0f, 244.0f);
    }
}
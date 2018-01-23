__kernel void rgbToHsv(__global float *rh, __global float *gs, __global float *bv, int const size) {
    int gid = get_global_id(0);
    if(gid < size) {
        float r = rh[gid];
        float g = gs[gid];
        float b = bv[gid];
        float mx = max(r, g);
        mx = max(mx, b);
        float mn = min(r, g);
        mn = min(mn, b);

        float delta = mx - mn;
        bv[gid] = mx;

        if(mx != 0) {
            gs[gid] = delta / mx;
        } else {
            rh[gid] = 0;
            gs[gid] = 0;
            return;
        }

        float h;
        if(r == mx)
            h = (g - b) / delta;
        else if( g == mx )
            h = 2 + ( b - r ) / delta;
        else
            h = 4 + ( r - g ) / delta;

        h = h * 1.0471976;

        if (h < 0)
            h = h + 6.2831855;
        rh[gid] = h;
    }
}
__kernel void combinedInit(__global float *rh, __global float *gs, __global float *bv, int const size) {
    int gid = get_global_id(0);
    if (gid < size) {
        // Begin rgbToHsv
        float r = rh[gid];
        float g = gs[gid];
        float b = bv[gid];
        float hue;
        float sat;
        float mx = max(r, g);
        mx = max(mx, b);
        float mn = min(r, g);
        mn = min(mn, b);

        float delta = mx - mn;
        //bv[gid] = mx;

        if(mx == 0) {
            rh[gid] = 0.0f;
            return;
        } else {
            sat = delta / mx;

            if(r == mx)
                hue = (g - b) / delta;
            else if( g == mx )
                hue = 2 + ( b - r ) / delta;
            else
                hue = 4 + ( r - g ) / delta;

            hue = hue * 1.0471976;

            if (hue < 0)
                hue = hue + 6.2831855;
        }

        // Begin hue

        float sqr = hue - 1.07f;
        sqr = fabs((float)sqr) - 0.3f;
        sqr = max(sqr, 0.0f);
        sqr = sqrt(sqr);
        float sVal = 0.5f - sat;
        mx = max(0.0f, sVal);
        mx = mx * 100.0f;
        sqr = sqr - mx;
        sqr = 5.0f/sqr;
        rh[gid] = clamp(sqr, 0.0f, 255.0f);
    }
}
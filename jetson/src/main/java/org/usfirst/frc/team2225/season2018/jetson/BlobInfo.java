package org.usfirst.frc.team2225.season2018.jetson;

public class BlobInfo {
    public int size = 0;
    public double centerX = 0;
    public double centerY  = 0;
    public int getX() {
        return (int) Math.round(centerX);
    }

    public int getY() {
        return (int) Math.round(centerY);
    }

    @Override
    public String toString() {
        return "{" + centerX + ", " + centerY + "} : " + size;
    }
}

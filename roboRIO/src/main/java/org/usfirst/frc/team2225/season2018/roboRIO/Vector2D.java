package org.usfirst.frc.team2225.season2018.roboRIO;

/**
 * A mutable representation of a vector in 2D space with various utility methods for vector math.
 * Note: for performance reasons, most methods change the vector they are called on, they do NOT create a new object.
 */
public class Vector2D {
    public double x;
    public double y;

    /**
     * Creates a Vector2D with 0 magnitude
     */
    public Vector2D() {
        x = 0;
        y = 0;
    }

    /**
     * Creates a Vector2D with the specified cartesian components
     *
     * @param x the magnitude in the x direction
     * @param y the magnitude in the y direction
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    /**
     * Creates a Vector2D with the specified magnitude and direction
     *
     * @param magnitude The length (magnitude) of the vector
     * @param direction The direction of the vector in radians. 0 is straight in the +x direction, increasing counter-clockwise
     * @return The created Vector2D object
     */
    public static Vector2D ofDirection(double magnitude, double direction) {
        Vector2D vector = new Vector2D();
        vector.x = magnitude * Math.cos(direction);
        vector.y = magnitude * Math.sin(direction);
        return vector;
    }

    public static Vector2D up() {
        return new Vector2D(0, 1);
    }

    public static Vector2D down() {
        return new Vector2D(0, -1);
    }

    public static Vector2D left() {
        return new Vector2D(-1, 0);
    }

    public static Vector2D right() {
        return new Vector2D(1, 0);
    }

    /**
     * Rotate a vector in Cartesian space.
     *
     * @param angle angle in radians by which to rotate vector counter-clockwise.
     */
    public void rotate(double angle) {
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        double[] out = new double[2];
        out[0] = x * cosA - y * sinA;
        out[1] = x * sinA + y * cosA;
        x = out[0];
        y = out[1];
    }

    /**
     * Gets the magnitude (length) of the vector
     *
     * @return The magnitude
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Calculates the dot product of two vectors
     *
     * @param vector The vector to dot product with this one
     * @return The calculated dot product
     */
    public double dot(Vector2D vector) {
        return x * vector.x + y * vector.y;
    }

    /**
     * Calculates the direction of this vector in radians
     * 0 is +x direction, with values increasing counter-clockwise
     *
     * @return The direction, in radians, ranging from 0 -> 2*PI
     */
    public double getDirection() {
        double dir = Math.atan2(y, x);
        if (dir < 0)
            dir += 2 * Math.PI;
        return dir;
    }

    /**
     * Adds the components of <code>vector</code> to this Vector2D
     *
     * @param vector The vector to add to this vector
     * @return This vector (for chaining)
     */
    public Vector2D add(Vector2D vector) {
        x += vector.x;
        y += vector.y;
        return this;
    }

    /**
     * Subtracts the components of <code>vector</code> from this Vector2D
     *
     * @param vector The vector to subtract from this vector
     * @return This vector (for chaining)
     */
    public Vector2D subtract(Vector2D vector) {
        x -= vector.x;
        y -= vector.y;
        return this;
    }

    /**
     * Normalizes this vector to a unit vector (length one)
     *
     * @return This vector (for chaining)
     */
    public Vector2D normalize() {
        double magnitude = magnitude();
        x /= magnitude;
        y /= magnitude;
        return this;
    }

    /**
     * Maps this vector from circular space to square space as defined by Simple Stretching Method
     * <a href="https://arxiv.org/pdf/1509.06344.pdf">https://arxiv.org/pdf/1509.06344.pdf</a>
     *
     * @return This vector (for chaining)
     */
    public Vector2D mapCircleToSquare() {
        double fromOrigin = Math.sqrt(x * x + y * y);
        double u = fromOrigin;
        double v = x / y * fromOrigin;
        if (x * x >= y * y) {
            x = Math.signum(x) * u;
            y = Math.signum(x) * v;
        } else {
            x = Math.signum(y) * v;
            y = Math.signum(y) * u;
        }
        return this;
    }

    /**
     * Maps this vector from square space to circular space as defined by Simple Stretching Method
     * <a href="https://arxiv.org/pdf/1509.06344.pdf">https://arxiv.org/pdf/1509.06344.pdf</a>
     *
     * @return This vector (for chaining)
     */
    public Vector2D mapSquareToCircle() {
        double fromOrigin = Math.sqrt(x * x + y * y);
        double u = x * y / fromOrigin;
        if (x * x >= y * y) {
            x = Math.signum(x) * x * x / fromOrigin;
            y = Math.signum(x) * u;
        } else {
            x = Math.signum(y) * u;
            y = Math.signum(y) * y * y / fromOrigin;
        }
        return this;
    }

    /**
     * Maps this vector from square space to space of a square rotated 45 degrees and inscribed inside the original square.
     * The purpose is to ensure for any vector contained within (-1, -1) and (1, 1), its components will add up to at most 1
     *
     * @return This vector (for chaining)
     */
    public Vector2D mapSquareToDiamond() {
        if (y == 0 || x == 0) {
        } else if (y == -1 && x == -1) {
            x = -0.5;
            y = -0.5;
        } else {
            double s = Math.max(Math.abs(x), Math.abs(y));
            x = Math.signum(y) * s / (y / x + Math.signum(y) * Math.signum(x));
            y = (s - Math.abs(x)) * Math.signum(y);
        }
        return this;
    }

    /**
     * Mulitplies this vector by a scalar
     *
     * @param scalar The value of the scalar
     * @return This vector (for chaining)
     */
    public Vector2D multiply(double scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }

    /**
     * Divides this vector by a scalar
     *
     * @param scalar The value of the scalar
     * @return This vector (for chaining)
     */
    public Vector2D divide(double scalar) {
        x /= scalar;
        y /= scalar;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o instanceof Vector2D) {
            Vector2D vec = (Vector2D) o;
            return vec.x == x && vec.y == y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        long xBits = Double.doubleToLongBits(x);
        long yBits = Double.doubleToLongBits(y);
        hash ^= xBits;
        hash ^= xBits >> 32;
        hash ^= yBits;
        hash ^= yBits >> 32;
        return hash;
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + "}";
    }

    public double[] export() {
        return new double[]{x, y};
    }
}

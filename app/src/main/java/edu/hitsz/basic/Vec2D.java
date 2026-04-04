package edu.hitsz.basic;

/**
 * 2D向量类，用于表示游戏中的向量，如位置、速度等
 * @author ChillyHigh
 */
public class Vec2D {
    private double x;
    private double y;

    public Vec2D() {
        this(0, 0);
    }

    public Vec2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vec2D setX(double x) {
        this.x = x;
        return this;
    }

    public Vec2D setY(double y) {
        this.y = y;
        return this;
    }

    public Vec2D set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2D add(Vec2D other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vec2D sub(Vec2D other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public Vec2D normalize() {
        double length = getLength();
        if (length != 0) {
            this.x /= length;
            this.y /= length;
        }
        return this;
    }

    public Vec2D scale(double scale) {
        this.x *= scale;
        this.y *= scale;
        return this;
    }

    public static double distance(Vec2D a, Vec2D b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }
}

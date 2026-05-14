package edu.hitsz.basic;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CapsuleHitBox extends HitBox {
    private double length;
    private double radius;
    private double angleDegrees;

    public CapsuleHitBox(AbstractFlyingObject owner, double offsetX, double offsetY, double length, double radius,
                         double angleDegrees) {
        this(owner, offsetX, offsetY, length, radius, angleDegrees, 1.0);
    }

    public CapsuleHitBox(AbstractFlyingObject owner, double offsetX, double offsetY, double length, double radius,
                         double angleDegrees, double scale) {
        super(owner, offsetX, offsetY, scale);
        this.length = Math.max(0.0, length * scale);
        this.radius = Math.max(0.0, radius * scale);
        this.angleDegrees = angleDegrees;
    }

    public void setLocalOffset(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setLength(double length) {
        this.length = Math.max(0.0, length);
    }

    public void setRadius(double radius) {
        this.radius = Math.max(0.0, radius);
    }

    public void setAngleDegrees(double angleDegrees) {
        this.angleDegrees = angleDegrees;
    }

    private double[][] segmentEndpoints() {
        double rad = Math.toRadians(angleDegrees);
        double cx = ownerX() + offsetX * Math.cos(rad) - offsetY * Math.sin(rad);
        double cy = ownerY() + offsetX * Math.sin(rad) + offsetY * Math.cos(rad);
        double dx = Math.cos(rad) * (length * 0.5);
        double dy = Math.sin(rad) * (length * 0.5);
        return new double[][]{
                {cx - dx, cy - dy},
                {cx + dx, cy + dy}
        };
    }

    @Override
    protected boolean collideWithRect(RectHitBox rect) {
        double[][] endpoints = segmentEndpoints();
        return segmentIntersectsExpandedRect(
                endpoints[0][0], endpoints[0][1],
                endpoints[1][0], endpoints[1][1],
                rect.getLeft() - radius,
                rect.getTop() - radius,
                rect.getRight() + radius,
                rect.getBottom() + radius
        );
    }

    @Override
    protected boolean collideWithCapsule(CapsuleHitBox capsule) {
        double[][] a = segmentEndpoints();
        double[][] b = capsule.segmentEndpoints();
        double threshold = radius + capsule.radius;
        return segmentDistanceSquared(
                a[0][0], a[0][1], a[1][0], a[1][1],
                b[0][0], b[0][1], b[1][0], b[1][1]
        ) <= threshold * threshold;
    }

    @Override
    public void render(GraphicsContext gc) {
        double[][] endpoints = segmentEndpoints();
        gc.save();
        gc.setStroke(Color.ORANGE);
        gc.strokeLine(endpoints[0][0], endpoints[0][1], endpoints[1][0], endpoints[1][1]);
        gc.strokeOval(endpoints[0][0] - radius, endpoints[0][1] - radius, radius * 2.0, radius * 2.0);
        gc.strokeOval(endpoints[1][0] - radius, endpoints[1][1] - radius, radius * 2.0, radius * 2.0);
        gc.restore();
    }

    private static boolean segmentIntersectsExpandedRect(double x1, double y1, double x2, double y2,
                                                         double left, double top, double right, double bottom) {
        if (pointInsideRect(x1, y1, left, top, right, bottom) || pointInsideRect(x2, y2, left, top, right, bottom)) {
            return true;
        }
        return lineIntersectsLine(x1, y1, x2, y2, left, top, right, top)
                || lineIntersectsLine(x1, y1, x2, y2, right, top, right, bottom)
                || lineIntersectsLine(x1, y1, x2, y2, right, bottom, left, bottom)
                || lineIntersectsLine(x1, y1, x2, y2, left, bottom, left, top);
    }

    private static boolean pointInsideRect(double x, double y, double left, double top, double right, double bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    private static boolean lineIntersectsLine(double x1, double y1, double x2, double y2,
                                              double x3, double y3, double x4, double y4) {
        double denominator = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (Math.abs(denominator) < 1e-6) {
            return false;
        }
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denominator;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denominator;
        return ua >= 0.0 && ua <= 1.0 && ub >= 0.0 && ub <= 1.0;
    }

    private static double segmentDistanceSquared(double p1x, double p1y, double q1x, double q1y,
                                                 double p2x, double p2y, double q2x, double q2y) {
        double[] u = {q1x - p1x, q1y - p1y};
        double[] v = {q2x - p2x, q2y - p2y};
        double[] w = {p1x - p2x, p1y - p2y};
        double a = dot(u, u);
        double b = dot(u, v);
        double c = dot(v, v);
        double d = dot(u, w);
        double e = dot(v, w);
        double D = a * c - b * b;
        double sN;
        double sD = D;
        double tN;
        double tD = D;

        if (D < 1e-6) {
            sN = 0.0;
            sD = 1.0;
            tN = e;
            tD = c;
        } else {
            sN = b * e - c * d;
            tN = a * e - b * d;
            if (sN < 0.0) {
                sN = 0.0;
                tN = e;
                tD = c;
            } else if (sN > sD) {
                sN = sD;
                tN = e + b;
                tD = c;
            }
        }

        if (tN < 0.0) {
            tN = 0.0;
            if (-d < 0.0) {
                sN = 0.0;
            } else if (-d > a) {
                sN = sD;
            } else {
                sN = -d;
                sD = a;
            }
        } else if (tN > tD) {
            tN = tD;
            if (-d + b < 0.0) {
                sN = 0.0;
            } else if (-d + b > a) {
                sN = sD;
            } else {
                sN = -d + b;
                sD = a;
            }
        }

        double sc = Math.abs(sN) < 1e-6 ? 0.0 : sN / sD;
        double tc = Math.abs(tN) < 1e-6 ? 0.0 : tN / tD;
        double dx = w[0] + sc * u[0] - tc * v[0];
        double dy = w[1] + sc * u[1] - tc * v[1];
        return dx * dx + dy * dy;
    }

    private static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1];
    }
}

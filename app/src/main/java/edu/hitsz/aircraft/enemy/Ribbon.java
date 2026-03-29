package edu.hitsz.aircraft.enemy;
//TODO: 后续文件移动位置

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Ribbon类，用于给Apollo和Artemis添加尾部效果
 */
public class Ribbon {
    private static final int RIBBON_SEGMENTS = 12; // 尾部最大长度
    private final double[] oldX = new double[RIBBON_SEGMENTS + 2];
    private final double[] oldY = new double[RIBBON_SEGMENTS + 2];
    private final double[] oldRot = new double[RIBBON_SEGMENTS + 2];

    private final int direction; // -1 左丝带, 1 右丝带
    private final double scale;

    public Ribbon(int direction, double scale) {
        this.direction = direction;
        this.scale = scale;
    }

    /**
     * 更新尾部历史记录
     * 
     * @param locationX
     * @param locationY
     * @param rotation
     */
    public void update(double locationX, double locationY, double rotation) {
        for (int i = oldX.length - 1; i > 0; i--) {
            oldX[i] = oldX[i - 1];
            oldY[i] = oldY[i - 1];
            oldRot[i] = oldRot[i - 1];
        }
        oldX[0] = locationX;
        oldY[0] = locationY;
        oldRot[0] = Math.toRadians(rotation);
    }

    public void render(GraphicsContext gc) {
        List<Vec2> points = buildPoints(oldX[0], oldY[0], Math.toDegrees(oldRot[0]));
        gc.setStroke(javafx.scene.paint.Color.WHITE);
        for (int i = 0; i < points.size() - 1; i++) {
            double ratio = i / (double) RIBBON_SEGMENTS;
            Vec2 p1 = points.get(i);
            Vec2 p2 = points.get(i + 1);
            gc.setLineWidth(Math.min(16.0 * (1 - ratio) + 0.5, 4.0));
            gc.setStroke(ribbonColor(ratio));
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }

    }

    private Color ribbonColor(double ratio) {
        Color start = Color.rgb(209, 252, 205, 0.8);
        Color end = Color.rgb(44, 132, 28, 0.1);
        return start.interpolate(end, Math.pow(ratio, 1.5));
    }

    private List<Vec2> buildPoints(
            double locationX,
            double locationY,
            double rotation) {
        List<Vec2> points = new ArrayList<>();
        double rotationRad = Math.toRadians(rotation);
        double timeSeconds = System.nanoTime() / 1_000_000_000.0;

        Vec2 ribbonOffset = new Vec2(0, -14.0 * scale).rotate(rotationRad);
        ribbonOffset = ribbonOffset.add(new Vec2(26.0 * direction, 0).rotate(rotationRad));
        Vec2 root = new Vec2(locationX, locationY).add(ribbonOffset);

        double currentSegmentRotation = rotationRad;

        for (int i = 0; i < RIBBON_SEGMENTS; i++) {
            double ratio = i / (double) RIBBON_SEGMENTS;

            double wrappedAngularOffset = wrapAngle(oldRot[i + 1] - currentSegmentRotation) * 0.6;
            double segmentRotationOffset = Math.clamp(wrappedAngularOffset, -0.24, 0.24);

            double sinusoidalFactor = Math.sin(ratio * 2.22 + timeSeconds * 3.4)
                    * lerpValue(0.0, 0.37, ratio)
                    * 12.0
                    * scale;

            Vec2 stretchDirection = new Vec2(0, 1).rotate(currentSegmentRotation);
            Vec2 normalDirection = new Vec2(direction, 0).rotate(currentSegmentRotation);
            Vec2 sinusoidalOffset = normalDirection.multiply(sinusoidalFactor);
            Vec2 stretch = stretchDirection.multiply(ratio * 540.0 * scale);

            points.add(root.add(stretch).add(sinusoidalOffset));

            currentSegmentRotation += segmentRotationOffset;
        }

        return points;
    }

    private static double wrapAngle(double angle) {
        return angle - 2 * Math.PI * Math.floor((angle + Math.PI) / (2 * Math.PI));
    }

    // 线性插值工具，value在[from, to]范围内时返回0.0-1.0的比例，否则返回0.0或1.0
    private static double lerpValue(double from, double to, double value) {
        if (Math.abs(to - from) < 1e-6) {
            return value >= to ? 1.0 : 0.0;
        }
        return Math.clamp((value - from) / (to - from), 0.0, 1.0);
    }

}

final class Vec2 {
    public final double x;
    public final double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(x + other.x, y + other.y);
    }

    public Vec2 multiply(double k) {
        return new Vec2(x * k, y * k);
    }

    public double length() {
        return Math.hypot(x, y);
    }

    public Vec2 normalize() {
        double len = length();
        if (len < 1e-6)
            return new Vec2(0, 0);
        return new Vec2(x / len, y / len);
    }

    public Vec2 rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec2(
                x * cos - y * sin,
                x * sin + y * cos);
    }
}

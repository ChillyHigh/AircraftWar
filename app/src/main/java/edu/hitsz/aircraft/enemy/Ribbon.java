package edu.hitsz.aircraft.enemy;
//TODO: 后续文件移动位置

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;

/**
 * Ribbon类，用于给Apollo和Artemis添加尾部效果
 */
public class Ribbon {
    private static final int RIBBON_SEGMENTS = 12; // 尾部最大长度

    // 历史记录数组
    private final double[] oldX = new double[RIBBON_SEGMENTS + 2];
    private final double[] oldY = new double[RIBBON_SEGMENTS + 2];
    private final double[] oldRot = new double[RIBBON_SEGMENTS + 2];

    // 预分配的顶点数组，避免每帧 new 列表和对象
    private final double[] pointsX = new double[RIBBON_SEGMENTS];
    private final double[] pointsY = new double[RIBBON_SEGMENTS];
    private final double[] leftX = new double[RIBBON_SEGMENTS];
    private final double[] leftY = new double[RIBBON_SEGMENTS];
    private final double[] rightX = new double[RIBBON_SEGMENTS];
    private final double[] rightY = new double[RIBBON_SEGMENTS];
    private final double[] quadX = new double[4];
    private final double[] quadY = new double[4];

    private final int direction; // -1 左丝带, 1 右丝带
    private final double scale;
    private final Color startColor;
    private final Color endColor;

    public Ribbon(int direction, double scale, Color startColor, Color endColor) {
        this.direction = direction;
        this.scale = scale;
        this.startColor = startColor;
        this.endColor = endColor;
    }

    /**
     * 更新尾部历史记录
     */
    public void update(double locationX, double locationY, double rotationRad) {
        System.arraycopy(oldX, 0, oldX, 1, oldX.length - 1);
        System.arraycopy(oldY, 0, oldY, 1, oldY.length - 1);
        System.arraycopy(oldRot, 0, oldRot, 1, oldRot.length - 1);

        oldX[0] = locationX;
        oldY[0] = locationY;
        oldRot[0] = rotationRad;
    }

    public void render(GraphicsContext gc) {
        buildPointsInPlace(oldX[0], oldY[0], oldRot[0]);

        double baseWidth = 8.0 * scale;

        for (int i = 0; i < RIBBON_SEGMENTS; i++) {
            double rx;
            double ry;

            if (i < RIBBON_SEGMENTS - 1) {
                double dx = pointsX[i + 1] - pointsX[i];
                double dy = pointsY[i + 1] - pointsY[i];
                double len = Math.hypot(dx, dy);
                rx = -dy / (len < 1e-5 ? 1 : len);
                ry = dx / (len < 1e-5 ? 1 : len);
            } else {
                double dx = pointsX[i] - pointsX[i - 1];
                double dy = pointsY[i] - pointsY[i - 1];
                double len = Math.hypot(dx, dy);
                rx = -dy / (len < 1e-5 ? 1 : len);
                ry = dx / (len < 1e-5 ? 1 : len);
            }

            double ratio = i / (double) RIBBON_SEGMENTS;
            double currentHalfWidth = (baseWidth * 0.5) * (1.0 - ratio);

            leftX[i] = pointsX[i] + rx * currentHalfWidth;
            leftY[i] = pointsY[i] + ry * currentHalfWidth;
            rightX[i] = pointsX[i] - rx * currentHalfWidth;
            rightY[i] = pointsY[i] - ry * currentHalfWidth;
        }

        gc.save();

        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.setEffect(new GaussianBlur(2));

        for (int i = 0; i < RIBBON_SEGMENTS - 1; i++) {
            double ratio = i / (double) RIBBON_SEGMENTS;
            setQuad(i);
            gc.setFill(ribbonColor(ratio));
            gc.fillPolygon(quadX, quadY, 4);

            // setInsetQuad(i, 0.42);
            // gc.setFill(coreColor(ratio));
            // gc.fillPolygon(quadX, quadY, 4);
        }

        gc.restore();
    }

    private Color ribbonColor(double ratio) {
        return startColor.interpolate(endColor, Math.pow(ratio, 1.5));
    }

    /**
     * 核心优化：展开向量计算，直接写入成员数组 pointsX 和 pointsY
     */
    private void buildPointsInPlace(double locationX, double locationY, double rotationRad) {
        double timeSeconds = System.nanoTime() / 1_000_000_000.0;

        // 展开 Vec2(0, -14.0 * scale).rotate(rotationRad)
        double cosRot = Math.cos(rotationRad);
        double sinRot = Math.sin(rotationRad);
        double off1X = 14.0 * scale * sinRot;
        double off1Y = -14.0 * scale * cosRot;

        // 展开 Vec2(26.0 * direction, 0).rotate(rotationRad)
        double off2X = 26.0 * scale * direction * cosRot;
        double off2Y = 26.0 * scale * direction * sinRot;

        double rootX = locationX + off1X + off2X;
        double rootY = locationY + off1Y + off2Y;

        double currentSegmentRotation = rotationRad;

        for (int i = 0; i < RIBBON_SEGMENTS; i++) {
            double ratio = i / (double) RIBBON_SEGMENTS;

            double wrappedAngularOffset = wrapAngle(oldRot[i + 1] - currentSegmentRotation) * 0.6;
            double segmentRotationOffset = Math.clamp(wrappedAngularOffset, -0.24, 0.24);

            double sinusoidalFactor = Math.sin(ratio * 2.22 + timeSeconds * 3.4)
                    * lerpValue(0.0, 0.37, ratio)
                    * 12.0
                    * scale;

            // 展开向量旋转和乘法
            double cosCSR = Math.cos(currentSegmentRotation);
            double sinCSR = Math.sin(currentSegmentRotation);

            // Vec2(0, 1).rotate(currentSegmentRotation)
            double stretchDirX = -sinCSR;
            double stretchDirY = cosCSR;

            // Vec2(direction, 0).rotate(currentSegmentRotation)
            double normDirX = direction * cosCSR;
            double normDirY = direction * sinCSR;

            double stretchFactor = ratio * 540.0 * scale;

            // 直接计算最终的 X 和 Y 坐标，存入预分配的数组
            pointsX[i] = rootX + stretchDirX * stretchFactor + normDirX * sinusoidalFactor;
            pointsY[i] = rootY + stretchDirY * stretchFactor + normDirY * sinusoidalFactor;

            currentSegmentRotation += segmentRotationOffset;
        }
    }

    private static double wrapAngle(double angle) {
        return angle - 2 * Math.PI * Math.floor((angle + Math.PI) / (2 * Math.PI));
    }

    private static double lerpValue(double from, double to, double value) {
        if (Math.abs(to - from) < 1e-6) {
            return value >= to ? 1.0 : 0.0;
        }
        return Math.clamp((value - from) / (to - from), 0.0, 1.0);
    }

    private void setQuad(int index) {
        quadX[0] = leftX[index];
        quadY[0] = leftY[index];
        quadX[1] = leftX[index + 1];
        quadY[1] = leftY[index + 1];
        quadX[2] = rightX[index + 1];
        quadY[2] = rightY[index + 1];
        quadX[3] = rightX[index];
        quadY[3] = rightY[index];
    }

}

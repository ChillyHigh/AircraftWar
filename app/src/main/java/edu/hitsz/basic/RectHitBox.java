package edu.hitsz.basic;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RectHitBox extends HitBox {
    private final double halfWidth;
    private final double halfHeight;

    public RectHitBox(AbstractFlyingObject owner, double offsetX, double offsetY, double width, double height) {
        this(owner, offsetX, offsetY, width, height, 1.0);
    }

    public RectHitBox(AbstractFlyingObject owner, double offsetX, double offsetY, double width, double height,
                      double scale) {
        super(owner, offsetX, offsetY, scale);
        this.halfWidth = width * scale / 2.0;
        this.halfHeight = height * scale / 2.0;
    }

    public double getCenterX() {
        return ownerX() + offsetX;
    }

    public double getCenterY() {
        return ownerY() + offsetY;
    }

    public double getLeft() {
        return getCenterX() - halfWidth;
    }

    public double getRight() {
        return getCenterX() + halfWidth;
    }

    public double getTop() {
        return getCenterY() - halfHeight;
    }

    public double getBottom() {
        return getCenterY() + halfHeight;
    }

    @Override
    protected boolean collideWithRect(RectHitBox rect) {
        return getLeft() <= rect.getRight()
                && getRight() >= rect.getLeft()
                && getTop() <= rect.getBottom()
                && getBottom() >= rect.getTop();
    }

    @Override
    protected boolean collideWithCapsule(CapsuleHitBox capsule) {
        return capsule.collideWithRect(this);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.save();
        gc.setStroke(Color.WHITE);
        gc.strokeRect(getLeft(), getTop(), halfWidth * 2.0, halfHeight * 2.0);
        gc.restore();
    }
}

package edu.hitsz.basic;

public class HitBox {
    private final double offsetX;
    private final double offsetY;
    private final double halfWidth;
    private final double halfHeight;
    private final AbstractFlyingObject owner;

    public HitBox(double offsetX, double offsetY, double width, double height, AbstractFlyingObject owner) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.halfWidth = width / 2.0;
        this.halfHeight = height / 2.0;
        this.owner = owner;
    }

    public boolean isCollidingWith(HitBox other) {
        return getLeft(this) <= other.getRight(other)
                && getRight(this) >= other.getLeft(other)
                && getTop(this) <= other.getBottom(other)
                && getBottom(this) >= other.getTop(other);
    }

    private double getLeft(HitBox hitBox) {
        return hitBox.owner.getLocationX() + hitBox.offsetX - hitBox.halfWidth;
    }

    private double getRight(HitBox hitBox) {
        return hitBox.owner.getLocationX() + hitBox.offsetX + hitBox.halfWidth;
    }

    private double getTop(HitBox hitBox) {
        return hitBox.owner.getLocationY() + hitBox.offsetY - hitBox.halfHeight;
    }

    private double getBottom(HitBox hitBox) {
        return hitBox.owner.getLocationY() + hitBox.offsetY + hitBox.halfHeight;
    }
}

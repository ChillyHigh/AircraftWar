package edu.hitsz.basic;

import javafx.scene.canvas.GraphicsContext;

public abstract class HitBox {
    protected final AbstractFlyingObject owner;
    protected double offsetX;
    protected double offsetY;

    protected HitBox(AbstractFlyingObject owner, double offsetX, double offsetY, double scale) {
        this.owner = owner;
        this.offsetX = offsetX * scale;
        this.offsetY = offsetY * scale;
    }

    public final boolean isCollidingWith(HitBox other) {
        if (other instanceof RectHitBox rect) {
            return collideWithRect(rect);
        }
        if (other instanceof CapsuleHitBox capsule) {
            return collideWithCapsule(capsule);
        }
        throw new IllegalStateException("Unsupported hit box type: " + other.getClass().getName());
    }

    protected abstract boolean collideWithRect(RectHitBox rect);

    protected abstract boolean collideWithCapsule(CapsuleHitBox capsule);

    public abstract void render(GraphicsContext gc);

    protected double ownerX() {
        return owner.getLocationX();
    }

    protected double ownerY() {
        return owner.getLocationY();
    }
}

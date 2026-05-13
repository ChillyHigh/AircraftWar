package edu.hitsz.bullet;

import edu.hitsz.basic.CapsuleHitBox;
import edu.hitsz.basic.HitBox;
import edu.hitsz.basic.SpriteAnimator;
import javafx.scene.image.Image;

public class ArtemisLaserBullet extends EnemyBullet {

    private static final int FRAME_WIDTH = 92;
    private static final int FRAME_HEIGHT = 22;
    private static final double HITBOX_LENGTH = 70.0;
    private static final double HITBOX_RADIUS = 8.0;

    private final SpriteAnimator animator;

    public ArtemisLaserBullet(double locationX, double locationY, double speedX, double speedY, int power) {
        super(locationX, locationY, speedX, speedY, power, Math.toDegrees(Math.atan2(speedY, speedX)));
        this.animator = new SpriteAnimator("artemis_laser", FRAME_WIDTH, FRAME_HEIGHT, 1, 4, 3, 0);
        this.rotation = Math.toDegrees(Math.atan2(speedY, speedX));
        this.width = FRAME_WIDTH;
        this.height = FRAME_HEIGHT;
        this.scale = 0.5;
        this.setHitBox(new CapsuleHitBox(this, 0.0, 0.0, HITBOX_LENGTH, HITBOX_RADIUS, rotation, scale));
        this.INIT_ANGLE = 0.0;
    }

    @Override
    public void forward() {
        super.forward();
        animator.update();
        rotation = Math.toDegrees(Math.atan2(speedY, speedX));
    }

    @Override
    protected HitBox createDefaultHitBox() {
        return new CapsuleHitBox(this, 0.0, 0.0, HITBOX_LENGTH, HITBOX_RADIUS, rotation, scale);
    }

    @Override
    public Image getImage() {
        return animator.getCurrentImage();
    }
}

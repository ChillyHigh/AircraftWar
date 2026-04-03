package edu.hitsz.bullet;

import edu.hitsz.basic.SpriteAnimator;
import javafx.scene.image.Image;

public class ArtemisLaserBullet extends EnemyBullet {

    private static final int FRAME_WIDTH = 92;
    private static final int FRAME_HEIGHT = 22;

    private final SpriteAnimator animator;

    public ArtemisLaserBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        this.animator = new SpriteAnimator("artemis_laser", FRAME_WIDTH, FRAME_HEIGHT, 1, 4, 3, 0);
        this.rotation = Math.toDegrees(Math.atan2(speedY, speedX));
        this.width = FRAME_WIDTH;
        this.height = FRAME_HEIGHT;
        this.scale = 0.5;
    }

    @Override
    public void forward() {
        super.forward();
        animator.update();
        rotation = Math.toDegrees(Math.atan2(speedY, speedX));
    }

    @Override
    public Image getImage() {
        return animator.getCurrentImage();
    }
}

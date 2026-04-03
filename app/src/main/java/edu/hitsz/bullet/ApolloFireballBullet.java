package edu.hitsz.bullet;

import edu.hitsz.basic.SpriteAnimator;
import javafx.scene.image.Image;

public class ApolloFireballBullet extends EnemyBullet {

    private static final int FRAME_WIDTH = 48;
    private static final int FRAME_HEIGHT = 96;

    private final SpriteAnimator animator;

    public ApolloFireballBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        this.animator = new SpriteAnimator("apollo_fireball", FRAME_WIDTH, FRAME_HEIGHT, 1, 6, 2, 0);
        this.rotation = Math.toDegrees(Math.atan2(speedY, speedX)) - 90.0;
        this.width = FRAME_WIDTH;
        this.height = FRAME_HEIGHT;
        this.scale = 0.6;
    }

    @Override
    public void forward() {
        super.forward();
        animator.update();
        rotation = Math.toDegrees(Math.atan2(speedY, speedX)) - 90.0;
    }

    @Override
    public Image getImage() {
        return animator.getCurrentImage();
    }
}

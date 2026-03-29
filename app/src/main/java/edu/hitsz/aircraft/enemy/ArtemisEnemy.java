package edu.hitsz.aircraft.enemy;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.basic.SpriteAnimator;
import javafx.scene.image.Image;

public class ArtemisEnemy extends AbstractEnemy {

    private final SpriteAnimator animator;

    public ArtemisEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        super.score = 100;
        super.rotation = 180.0;
        super.scale = 0.5;
        this.animator = new SpriteAnimator("artemis",
                204, 226, 10, 9, 5, 0);
        this.width = animator.getFrameWidth();
        this.height = animator.getFrameHeight();
    }

    @Override
    public void forward() {
        super.forward();
        animator.update();
    }

    @Override
    public Image getImage() {
        return animator.getCurrentImage();
    }
}

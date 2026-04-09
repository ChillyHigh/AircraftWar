package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class MobEnemyFactory implements EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new MobEnemy(
                randomLocationX(ImageManager.MOB_ENEMY_IMAGE.getWidth()),
                randomLocationY(),
                0,
                10,
                30);
    }

    private int randomLocationX(double imageWidth) {
        return (int) (Math.random() * (Main.WINDOW_WIDTH - imageWidth));
    }

    private int randomLocationY() {
        return (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
    }
}

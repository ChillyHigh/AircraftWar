package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.BossEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class BossEnemyFactory implements EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new BossEnemy(
                randomLocationX(ImageManager.BOSS_ENEMY_IMAGE.getWidth()),
                randomLocationY(),
                0,
                2,
                90);
    }

    private int randomLocationX(double imageWidth) {
        return (int) (Math.random() * (Main.WINDOW_WIDTH - imageWidth));
    }

    private int randomLocationY() {
        return (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
    }
}

package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.EliteEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class EliteEnemyFactory implements EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new EliteEnemy(
                randomLocationX(ImageManager.ELITE_ENEMY_IMAGE.getWidth()),
                randomLocationY(),
                0,
                5,
                30);
    }

    private int randomLocationX(double imageWidth) {
        return (int) (Math.random() * (Main.WINDOW_WIDTH - imageWidth));
    }

    private int randomLocationY() {
        return (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
    }
}

package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.EliteProEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class EliteProEnemyFactory implements EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new EliteProEnemy(
                randomLocationX(ImageManager.ELITE_PRO_ENEMY_IMAGE.getWidth()),
                randomLocationY(),
                0,
                5,
                60);
    }

    private int randomLocationX(double imageWidth) {
        return (int) (Math.random() * (Main.WINDOW_WIDTH - imageWidth));
    }

    private int randomLocationY() {
        return (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05);
    }
}

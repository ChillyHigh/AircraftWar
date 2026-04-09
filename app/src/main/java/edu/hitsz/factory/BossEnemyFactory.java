package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.BossEnemy;

public class BossEnemyFactory extends EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new BossEnemy(
                randomLocation(),
                -50,
                0,
                2,
                90);
    }

}

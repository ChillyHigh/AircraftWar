package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.EliteEnemy;

public class EliteEnemyFactory extends EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new EliteEnemy(
                randomLocation(),
                -30,
                0,
                5,
                30);
    }
}

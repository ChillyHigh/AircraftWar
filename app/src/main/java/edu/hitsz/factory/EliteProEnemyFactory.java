package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.EliteProEnemy;

public class EliteProEnemyFactory extends EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new EliteProEnemy(
                randomLocation(),
                -30,
                0,
                5,
                60);
    }
}

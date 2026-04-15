package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.ElitePlusEnemy;

public class ElitePlusEnemyFactory extends EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new ElitePlusEnemy(
                randomLocation(),
                -30,
                (int) Math.round(Math.random() * 3),
                5,
                30);
    }
}

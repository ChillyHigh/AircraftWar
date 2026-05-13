package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.EliteProEnemy;

public class EliteProEnemyFactory extends EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new EliteProEnemy(
                randomLocation(),
                -30,
                scaleSpeed((int) Math.round(Math.random() * 3)),
                scaleSpeed(5),
                scaleHp(60));
    }
}

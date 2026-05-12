package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.enemy.MobEnemy;

public class MobEnemyFactory extends EnemyFactory {
    @Override
    public AbstractEnemy createEnemy() {
        return new MobEnemy(
                randomLocation(),
                -30,
                scaleSpeed(0),
                scaleSpeed(10),
                scaleHp(30));
    }

}

package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.application.Main;

public abstract class EnemyFactory {
    public abstract AbstractEnemy createEnemy();

    protected int randomLocation() {
        return (int) (Math.random() * (Main.WINDOW_WIDTH));
    }

}

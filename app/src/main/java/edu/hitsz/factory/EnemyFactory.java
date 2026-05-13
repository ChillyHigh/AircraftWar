package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.application.Main;

public abstract class EnemyFactory {
    protected double hpRate = 1.0;
    protected double speedRate = 1.0;

    public abstract AbstractEnemy createEnemy();

    public void setRates(double hpRate, double speedRate) {
        this.hpRate = hpRate;
        this.speedRate = speedRate;
    }

    protected int scaleHp(int hp) {
        return (int) Math.round(hp * hpRate);
    }

    protected int scaleSpeed(int speed) {
        return (int) Math.round(speed * speedRate);
    }

    protected int randomLocation() {
        return (int) (Math.random() * (Main.WINDOW_WIDTH));
    }

}

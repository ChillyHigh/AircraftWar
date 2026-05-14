package edu.hitsz.bullet;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.observer.PropObserver;
import edu.hitsz.thread.TimeOutThread;

/**
 * 敌机子弹
 *
 * @Author hitsz
 */
public class EnemyBullet extends BaseBullet implements PropObserver {
    private TimeOutThread freezeThread;
    private boolean frozen = false;
    private double cachedSpeedX;
    private double cachedSpeedY;

    public EnemyBullet(double locationX, double locationY, double speedX, double speedY, int power, double angle) {
        super(locationX, locationY, speedX, speedY, power, angle);
        this.INIT_ANGLE = 180.0;
    }

    @Override
    public void onBomb() {
        vanish();
    }

    @Override
    public void onCollide(AbstractFlyingObject other) {
        if (other instanceof HeroAircraft) {
            vanish();
        }
    }

    @Override
    public void onFreeze() {
        if (!frozen) {
            cachedSpeedX = speedX;
            cachedSpeedY = speedY;
            frozen = true;
        }
        speedX = 0;
        speedY = 0;
        if (freezeThread == null || !freezeThread.isAlive()) {
            freezeThread = new TimeOutThread(5_000L, () -> {
                if (!notValid()) {
                    speedX = cachedSpeedX;
                    speedY = cachedSpeedY;
                }
                frozen = false;
                freezeThread = null;
            });
            freezeThread.start();
        } else {
            freezeThread.extend(5_000L);
        }
    }

}

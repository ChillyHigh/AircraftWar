package edu.hitsz.bullet;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.basic.AbstractFlyingObject;

/**
 * 英雄机子弹
 *
 * @Author hitsz
 */
public class HeroBullet extends BaseBullet {

    public HeroBullet(double locationX, double locationY, double speedX, double speedY, int power, double angle) {
        super(locationX, locationY, speedX, speedY, power, angle);
    }

    @Override
    public void onCollide(AbstractFlyingObject other) {
        if (other instanceof AbstractEnemy) {
            vanish();
        }
    }

}

package edu.hitsz.bullet;

import edu.hitsz.application.Main;
import edu.hitsz.basic.AbstractFlyingObject;

/**
 * 子弹基类
 * @author hitsz
 */
public abstract class BaseBullet extends AbstractFlyingObject {

    private int power = 10;

    public BaseBullet(double locationX, double locationY, double speedX, double speedY, int power) {
        super(locationX, locationY, speedX, speedY);
        this.power = power;
    }

    @Override
    public void forward() {
        super.forward();

        // 判定 x 轴出界
        if (location.getX() <= 0 || location.getX() >= Main.WINDOW_WIDTH) {
            vanish();
        }

        // 判定 y 轴出界
        if (speed.getY() > 0 && location.getY() >= Main.WINDOW_HEIGHT) {
            // 向下飞行出界
            vanish();
        } else if (location.getY() <= 0) {
            // 向上飞行出界
            vanish();
        }
    }

    public int getPower() {
        return power;
    }
}

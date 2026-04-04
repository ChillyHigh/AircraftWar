package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Main;
import edu.hitsz.basic.AbstractFlyingObject;

public abstract class BaseProp extends AbstractFlyingObject {

    public BaseProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void forward() {
        super.forward();

        // 判定 x 轴出界
        if (location.getX() <= 0 || location.getX() >= Main.WINDOW_WIDTH) {
            vanish();
        }

        // 判定 y 轴出界
        if (location.getY() >= Main.WINDOW_HEIGHT || location.getY() <= 0) {
            // 向下飞行出界
            vanish();
        }
    }

    // 道具被英雄机获得后激活效果
    public void activate(HeroAircraft heroAircraft) {
    }

}

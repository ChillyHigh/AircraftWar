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
        if (locationX <= -getWidth() * scale / 2.0 || locationX >= Main.WINDOW_WIDTH + getWidth() * scale / 2.0) {
            vanish();
        }

        // 判定 y 轴出界
        if (locationY >= Main.WINDOW_HEIGHT + getHeight() * scale / 2.0 || locationY <= -getHeight() * scale / 2.0) {
            // 向下飞行出界
            vanish();
        }
    }

    // 道具被英雄机获得后激活效果
    public void activate(HeroAircraft heroAircraft) {
    }

}

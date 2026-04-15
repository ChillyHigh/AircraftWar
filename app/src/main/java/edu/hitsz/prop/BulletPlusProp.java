package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.CircleShootStrategy;

public class BulletPlusProp extends BaseProp {
    public BulletPlusProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() {
        // 英雄机切换为圆圈射击策略
        HeroAircraft.getHeroAircraft().setShootStrategy(new CircleShootStrategy());
        System.out.println("FirePlusSupply active!");
    }

}

package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.FanShootStrategy;

public class BulletProp extends BaseProp {
    public BulletProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() {
        // 英雄机切换为散射子弹
        HeroAircraft.getHeroAircraft().setShootStrategy(new FanShootStrategy());
        System.out.println("FireSupply active!");
    }

}

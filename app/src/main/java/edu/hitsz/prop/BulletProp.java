package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BulletProp extends BaseProp {
    public BulletProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        // 英雄机增加子弹数量
        System.out.println("FireSupply active!");
    }

}

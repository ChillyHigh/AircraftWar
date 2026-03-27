package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BulletPlusProp extends BaseProp {
    public BulletPlusProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        // 英雄机增加子弹数量
        System.out.println("FirePlusSupply active!");
    }

}

package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BulletProp extends BaseProp {
    public BulletProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        // 英雄机增加子弹数量
        System.out.println("FireSupply active!");
    }

}

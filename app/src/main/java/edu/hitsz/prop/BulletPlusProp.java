package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.CircleShootStrategy;

public class BulletPlusProp extends BaseProp {
    public BulletPlusProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() {
        HeroAircraft.getHeroAircraft().setShootStrategy(new CircleShootStrategy(), 10_000L);
        System.out.println("FirePlusSupply active!");
    }

}

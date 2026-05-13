package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.FanShootStrategy;

public class BulletProp extends BaseProp {
    public BulletProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() {
        HeroAircraft.getHeroAircraft().setShootStrategy(new FanShootStrategy(), 10_000L);
        System.out.println("FireSupply active!");
    }

}

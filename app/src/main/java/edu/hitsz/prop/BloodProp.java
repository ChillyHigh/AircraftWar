package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BloodProp extends BaseProp {
    public BloodProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() {
        // 英雄机加血
        HeroAircraft.getHeroAircraft().increaseHp(30);
    }
}

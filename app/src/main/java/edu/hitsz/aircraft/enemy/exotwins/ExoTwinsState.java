package edu.hitsz.aircraft.enemy.exotwins;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.enemy.ExoTwinsManager;

public abstract class ExoTwinsState {
    protected HeroAircraft hero = HeroAircraft.getHeroAircraft();

    protected int stateTimer = 0;

//    public ExoTwinsState() {
//        this.exoTwinsManager = exoTwinsManager;
//        apollo = exoTwinsManager.getApollo();
//        artemis = exoTwinsManager.getArtemis();
//    }

    public abstract void update(ExoTwinsManager exoTwinsManager);
}

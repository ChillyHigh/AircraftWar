package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.BulletFactory;

import java.util.LinkedList;
import java.util.List;

public class FanShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft shooter, String bulletType) {
        List<BaseBullet> res = new LinkedList<>();
        double x = shooter.getLocationX();
        double y = shooter.getLocationY() + shooter.getDirection() * 2;
        double baseSpeedY = shooter.getSpeedY() + shooter.getDirection() * 3;

        res.add(BulletFactory.createBullet("enemy",
                x - 15, y,
                -1, Math.sqrt(baseSpeedY * baseSpeedY - 4),
                shooter.getPower(), 210));
        res.add(BulletFactory.createBullet("enemy",
                x, y,
                0, Math.sqrt(baseSpeedY * baseSpeedY - 4),
                shooter.getPower(), 180));
        res.add(BulletFactory.createBullet("enemy",
                x + 15, y,
                1, Math.sqrt(baseSpeedY * baseSpeedY - 4),
                shooter.getPower(), 150));
        return res;
    }
}

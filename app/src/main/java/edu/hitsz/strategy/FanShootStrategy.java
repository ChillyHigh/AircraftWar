package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.BulletFactory;

import java.util.LinkedList;
import java.util.List;

public class FanShootStrategy implements ShootStrategy {
    private static final double[] OFFSETS_X = {-15, 0, 15};
    private static final double[] SPEEDS_X = {-1, 0, 1};

    @Override
    public List<BaseBullet> shoot(AbstractAircraft shooter, String bulletType) {
        List<BaseBullet> res = new LinkedList<>();
        double x = shooter.getLocationX();
        double y = shooter.getLocationY() + shooter.getDirection() * 2;
        double baseSpeedY = shooter.getSpeedY() + shooter.getDirection() * 3;

        for (int i = 0; i < OFFSETS_X.length; i++) {
            double speedX = SPEEDS_X[i];
            double angle = Math.toDegrees(Math.atan2(baseSpeedY, speedX)) + 90.0;
            res.add(BulletFactory.createBullet(bulletType,
                    x + OFFSETS_X[i], y,
                    speedX, baseSpeedY,
                    shooter.getPower(), angle));
        }
        return res;
    }
}

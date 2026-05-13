package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.BulletFactory;

import java.util.LinkedList;
import java.util.List;

public class CircleShootStrategy implements ShootStrategy {
    private final double BULLET_SPEED = 5.0;

    @Override
    public List<BaseBullet> shoot(AbstractAircraft shooter, String bulletType) {
        List<BaseBullet> res = new LinkedList<>();
        double x = shooter.getLocationX();
        double y = shooter.getLocationY() + shooter.getDirection() * 2;
        double baseVx = shooter.getSpeedX();
        double baseVy = shooter.getSpeedY();
        int MAX_BULLET = 20;
        for (int i = 0; i < MAX_BULLET; i++) {
            res.add(BulletFactory.createBullet(bulletType,
                    x, y,
                    BULLET_SPEED * Math.cos(Math.PI * 2 * i / MAX_BULLET) + baseVx,
                    BULLET_SPEED * Math.sin(Math.PI * 2 * i / MAX_BULLET) + baseVy,
                    shooter.getPower(),
                    (double) (360 * i) / MAX_BULLET + 90
            ));
        }
        return res;
    }
}

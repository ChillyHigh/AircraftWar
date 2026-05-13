package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.BulletFactory;

import java.util.LinkedList;
import java.util.List;

public class StraightShootStrategy implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft shooter, String bulletType) {
        List<BaseBullet> res = new LinkedList<>();
        double x = shooter.getLocationX();
        double y = shooter.getLocationY() + shooter.getDirection() * 2;
        double speedX = 0;
        double speedY = shooter.getSpeedY() + shooter.getDirection() * 5;
        BaseBullet bullet;
        for (int i = 0; i < shooter.getShootNum(); i++) {
            // 子弹发射位置相对飞机位置向前偏移
            // 多个子弹横向分散
            bullet = BulletFactory.createBullet(bulletType,
                    x + (i * 2 - shooter.getShootNum() + 1) * 10,
                    y,
                    speedX,
                    speedY,
                    shooter.getPower());
            res.add(bullet);
        }
        return res;
    }
}

package edu.hitsz.factory;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

public class BulletFactory {
    public static BaseBullet createBullet(String bulletType, double locationX, double locationY, double speedX, double speedY, int power) {
        switch (bulletType) {
            case "hero":
                return new HeroBullet(locationX, locationY, speedX, speedY, power, 0);
            case "enemy":
                return new EnemyBullet(locationX, locationY, speedX, speedY, power, 180);
            default:
                return null;
        }
    }

    public static BaseBullet createBullet(String bulletType, double locationX, double locationY, double speedX, double speedY, int power, double angle) {
        switch (bulletType) {
            case "hero":
                return new HeroBullet(locationX, locationY, speedX, speedY, power, angle);
            case "enemy":
                return new EnemyBullet(locationX, locationY, speedX, speedY, power, angle);
            default:
                return null;
        }
    }
}

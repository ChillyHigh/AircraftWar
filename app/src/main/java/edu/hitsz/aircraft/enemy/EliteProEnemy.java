package edu.hitsz.aircraft.enemy;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.strategy.FanShootStrategy;

/**
 * 精英敌机，可射击和掉落道具
 *
 * @author CH
 */
public class EliteProEnemy extends AbstractEnemy {

    public EliteProEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        // 设置精英敌机分数
        super.score = 30;
        setDropConfig("Blood", "Bullet", "BulletPlus", "Bomb");

        this.shootStrategy = new FanShootStrategy();
    }

//    @Override
//    public List<BaseBullet> shoot() {
//        List<BaseBullet> res = new LinkedList<>();
//        double x = getLocationX();
//        double y = getLocationY() + direction * 2;
//        double baseSpeedY = getSpeedY() + direction * 5;
//
//        res.add(new EnemyBullet(x - 15, y, -2, Math.sqrt(baseSpeedY * baseSpeedY - 4), power));
//        res.add(new EnemyBullet(x, y, 0, baseSpeedY, power));
//        res.add(new EnemyBullet(x + 15, y, 2, Math.sqrt(baseSpeedY * baseSpeedY - 4), power));
//        return res;
//    }
}

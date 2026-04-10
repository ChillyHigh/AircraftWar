package edu.hitsz.aircraft.enemy;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.strategy.StraightShootStrategy;

/**
 * 精英敌机，可射击和掉落道具
 *
 * @author CH
 */
public class ElitePlusEnemy extends AbstractEnemy {
    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        // 设置精英敌机分数
        super.score = 30;
        setDropConfig("Blood", "Bullet", "BulletPlus", "Bomb");
        this.shootNum = 2;
        this.shootStrategy = new StraightShootStrategy();
    }

}

package edu.hitsz.aircraft.enemy;

import edu.hitsz.aircraft.AbstractEnemy;

/**
 * 精英敌机，可射击和掉落道具
 * 
 * @author CH
 */
public class BossEnemy extends AbstractEnemy {
    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        // 设置Boss敌机分数
        super.score = 100;
        setDropConfig("Blood", "Bullet", "BulletPlus", "Bomb", "Freeze");
    }

}

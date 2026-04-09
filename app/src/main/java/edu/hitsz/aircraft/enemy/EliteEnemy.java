package edu.hitsz.aircraft.enemy;

import java.util.LinkedList;
import java.util.List;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

/**
 * 精英敌机，可射击和掉落道具
 * 
 * @author CH
 */
public class EliteEnemy extends AbstractEnemy {
    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        // 设置精英敌机分数
        super.score = 30;
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        double x = this.getLocationX();
        double y = this.getLocationY() + direction * 2;
        double speedX = 0;
        double speedY = this.getSpeedY() + direction * 5;
        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            // 子弹发射位置相对飞机位置向前偏移
            // 多个子弹横向分散
            bullet = new EnemyBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power);
            res.add(bullet);
        }
        System.out.println("EliteEnemy shoot: " + res.size() + " bullets");
        return res;
    }

}

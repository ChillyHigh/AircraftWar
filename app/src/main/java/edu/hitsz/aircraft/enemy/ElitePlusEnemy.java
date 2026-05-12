package edu.hitsz.aircraft.enemy;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.application.Main;
import edu.hitsz.strategy.StraightShootStrategy;

/**
 * 精英敌机，可射击和掉落道具
 *
 * @author CH
 */
public class ElitePlusEnemy extends AbstractEnemy {
    private static final double PROP_DROP_RATE = 0.5;

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        // 设置精英敌机分数
        super.score = 30;
        setDropConfig(PROP_DROP_RATE, "Blood", "Bullet", "BulletPlus", "Bomb");
        this.shootNum = 2;
        this.shootStrategy = new StraightShootStrategy();
    }

    @Override
    public void forward() {
        super.forward();
        if (locationX < 0 || locationX > Main.WINDOW_WIDTH) {
            speedX = -speedX;
        }
    }

    @Override
    protected long getFreezeDuration() {
        return 5_000L;
    }

}

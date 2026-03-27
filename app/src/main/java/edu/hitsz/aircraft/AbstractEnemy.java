package edu.hitsz.aircraft;

import java.util.LinkedList;
import java.util.List;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;

/**
 * 敌机的抽象父类
 *
 * @author CH
 */
public abstract class AbstractEnemy extends AbstractAircraft {
    // 敌机被击落后增加的分数
    protected int score = 10;

    // 子弹射击方向
    protected int direction = 1;

    // 每次射击发射子弹数量
    protected int shootNum = 1;

    // 子弹威力
    protected int power = 30;

    public AbstractEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    /**
     * 通过射击产生子弹，默认实现为不射击，返回空列表
     *
     * @return 射击出的子弹List
     */
    @Override
    public List<BaseBullet> shoot() {
        return new LinkedList<>();
    }

    public int getScore() {
        return score;
    }
}

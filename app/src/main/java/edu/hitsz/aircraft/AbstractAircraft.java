package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ShootStrategy;

import java.util.List;

/**
 * 所有种类飞机的抽象父类：
 *
 * @author hitsz
 */
public abstract class AbstractAircraft extends AbstractFlyingObject {

    //最大生命值
    protected int maxHp;
    protected int hp;

    // 子弹射击方向 (向上发射：-1，向下发射：1)
    protected int direction = 1;

    // 每次射击发射子弹数量
    protected int shootNum = 1;

    // 子弹威力
    protected int power = 10;

    // 射击策略
    protected ShootStrategy shootStrategy;

    /**
     * 构造函数
     *
     * @param locationX 飞机初始位置X坐标
     * @param locationY 飞机初始位置Y坐标
     * @param speedX    飞机初始速度X坐标
     * @param speedY    飞机初始速度Y坐标
     * @param hp        飞机初始生命值
     */
    public AbstractAircraft(double locationX, double locationY, double speedX, double speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    public void decreaseHp(int decrease) {
        hp -= decrease;
        if (hp <= 0) {
            hp = 0;
            vanish();
        }
    }

    public int getHp() {
        return hp;
    }

    public int getDirection() {
        return direction;
    }

    public int getShootNum() {
        return shootNum;
    }

    public int getPower() {
        return power;
    }

    public void setShootStrategy(ShootStrategy strategy) {
        this.shootStrategy = strategy;
    }

    public void setShootStrategy(ShootStrategy strategy, long durationMs) {
        this.shootStrategy = strategy;
    }

    /**
     * 飞机射击方法，可射击对象必须实现
     *
     * @return 可射击对象需实现，返回子弹
     * 非可射击对象空实现，返回null
     */
    public abstract List<BaseBullet> shoot();

}


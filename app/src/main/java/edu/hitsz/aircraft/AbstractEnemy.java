package edu.hitsz.aircraft;

import edu.hitsz.application.AudioManager;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.PropFactory;
import edu.hitsz.observer.PropObserver;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.thread.TimeOutThread;

import java.util.LinkedList;
import java.util.List;

/**
 * 敌机的抽象父类
 *
 * @author CH
 */
public abstract class AbstractEnemy extends AbstractAircraft implements PropObserver {
    private TimeOutThread freezeThread;
    private boolean frozen = false;
    private double cachedSpeedX;
    private double cachedSpeedY;

    // 敌机被击落后增加的分数
    protected int score = 10;

    // 道具掉落概率
    protected double propDropRate = 1.0;

    // 可掉落道具类型
    protected String[] droppablePropTypes = new String[0];

    public AbstractEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT + getHeight() * scale / 2.0) {
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
        if (shootStrategy == null) {
            return new LinkedList<>();
        }
        return shootStrategy.shoot(this, "enemy");
    }

    public List<BaseBullet> getBullets() {
        return new LinkedList<>();
    }

    public int getScore() {
        return score;
    }

    protected void setDropConfig(String... droppablePropTypes) {
        this.droppablePropTypes = droppablePropTypes;
    }

    protected void setDropConfig(double propDropRate, String... droppablePropTypes) {
        this.propDropRate = propDropRate;
        this.droppablePropTypes = droppablePropTypes;
    }

    /**
     * 产生道具的方法
     *
     * @return 道具实例
     */
    public List<BaseProp> dropProp() {
        if (droppablePropTypes.length == 0 || Math.random() > propDropRate) {
            return new LinkedList<>();
        }
        // 随机选择一个道具类型
        String propType = droppablePropTypes[(int) (Math.random() * droppablePropTypes.length)];
        LinkedList<BaseProp> props = new LinkedList<>();
        props.add(PropFactory.creatProp(propType, getLocationX(), getLocationY(), 0, 2));
        return props;
    }

    @Override
    public void onBomb() {
        vanish();
    }

    @Override
    public void onCollide(AbstractFlyingObject other) {
        if (other instanceof BaseBullet bullet) {
            AudioManager.playSound(AudioManager.BULLET_HIT);
            decreaseHp(bullet.getPower());
            return;
        }
        if (other instanceof HeroAircraft) {
            vanish();
        }
    }

    @Override
    public void onFreeze() {
        if (!frozen) {
            cachedSpeedX = speedX;
            cachedSpeedY = speedY;
            frozen = true;
        }
        speedX = 0;
        speedY = 0;
        if (freezeThread == null || !freezeThread.isAlive()) {
            freezeThread = new TimeOutThread(getFreezeDuration(), () -> {
                if (!notValid()) {
                    speedX = cachedSpeedX;
                    speedY = cachedSpeedY;
                }
                frozen = false;
                freezeThread = null;
            });
            freezeThread.start();
        } else if (getFreezeDuration() != Long.MAX_VALUE) {
            freezeThread.extend(getFreezeDuration());
        }
    }

    protected long getFreezeDuration() {
        return Long.MAX_VALUE;
    }
}

package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.PropFactory;
import edu.hitsz.prop.BaseProp;

import java.util.LinkedList;
import java.util.List;

/**
 * 敌机的抽象父类
 *
 * @author CH
 */
public abstract class AbstractEnemy extends AbstractAircraft {
    // 敌机被击落后增加的分数
    protected int score = 10;

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

    public int getScore() {
        return score;
    }

    protected void setDropConfig(String... droppablePropTypes) {
        this.droppablePropTypes = droppablePropTypes;
    }

    /**
     * 产生道具的方法
     *
     * @return 道具实例
     */
    public BaseProp dropProp() {
        if (droppablePropTypes.length == 0) {
            return null;
        }
        // 随机选择一个道具类型
        String propType = droppablePropTypes[(int) (Math.random() * droppablePropTypes.length)];
        return PropFactory.creatProp(propType, getLocationX(), getLocationY(), 0, 2);
    }
}

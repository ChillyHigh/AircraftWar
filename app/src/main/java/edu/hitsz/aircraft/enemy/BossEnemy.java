package edu.hitsz.aircraft.enemy;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.application.Main;
import edu.hitsz.factory.PropFactory;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.strategy.CircleShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 精英敌机，可射击和掉落道具
 *
 * @author CH
 */
public class BossEnemy extends AbstractEnemy {
    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootStrategy = new CircleShootStrategy();
        // 设置Boss敌机分数
        super.score = 100;
        setDropConfig("Blood", "Bullet", "BulletPlus", "Bomb", "Freeze");
    }

    @Override
    public void forward() {
        super.forward();
        if (speedY >= 0.1) {
            speedY *= 0.96;
        } else {
            speedY = 0;
        }
        if (locationX < 0 || locationX > Main.WINDOW_WIDTH) {
            speedX = -speedX;
        }
    }

    @Override
    public List<BaseProp> dropProp() {
        String propType = droppablePropTypes[(int) (Math.random() * droppablePropTypes.length)];
        LinkedList<BaseProp> props = new LinkedList<>();
        props.add(PropFactory.creatProp(propType, getLocationX() - 50, getLocationY(), 0, 2));
        propType = droppablePropTypes[(int) (Math.random() * droppablePropTypes.length)];
        props.add(PropFactory.creatProp(propType, getLocationX(), getLocationY(), 0, 2));
        propType = droppablePropTypes[(int) (Math.random() * droppablePropTypes.length)];
        props.add(PropFactory.creatProp(propType, getLocationX() + 50, getLocationY(), 0, 2));

        return props;

    }

    @Override
    public void onBomb() {
        decreaseHp(200);
    }

    @Override
    public void onFreeze() {
    }

}

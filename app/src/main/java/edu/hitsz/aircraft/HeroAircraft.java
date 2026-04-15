package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.StraightShootStrategy;

import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 *
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    // 单例模式的存储
    private volatile static HeroAircraft hero;

    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        direction = -1;
        shootStrategy = new StraightShootStrategy();
    }

    /**
     * 返回英雄机的单例实例。
     *
     * @return 英雄机对象
     */
    public static HeroAircraft getHeroAircraft() {
        if (hero == null) {
            synchronized (HeroAircraft.class) {
                hero = new HeroAircraft(
                        Main.WINDOW_WIDTH / 2,
                        Main.WINDOW_HEIGHT - (int) ImageManager.HERO_IMAGE.getHeight(),
                        0, 0, 100);
            }
        }
        return hero;
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    /**
     * 通过射击产生子弹
     *
     * @return 射击出的子弹List
     */
    @Override
    public List<BaseBullet> shoot() {
        return shootStrategy.shoot(this, "hero");

    }

    @Override
    public void decreaseHp(int decrease) {

    }

    public void increaseHp(int increase) {
        hp = Math.max(hp + increase, maxHp);
    }

}

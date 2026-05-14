package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.aircraft.enemy.exotwins.ApolloEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ArtemisEnemy;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;
import edu.hitsz.thread.TimeOutThread;

import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 *
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {
    // 单例模式的存储
    private volatile static HeroAircraft hero;
    private TimeOutThread fireStrategyThread;

    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        direction = -1;
        shootStrategy = new StraightShootStrategy();
        this.power = 30;
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
                        0, 0, 10000);
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


    public void increaseHp(int increase) {
        hp = Math.min(hp + increase, maxHp);
    }

    @Override
    public void onCollide(AbstractFlyingObject other) {
        if (other instanceof EnemyBullet enemyBullet) {
            decreaseHp(enemyBullet.getPower());
            return;
        }
        if (other instanceof BaseBullet baseBullet) {
            decreaseHp(baseBullet.getPower());
            return;
        }
        if (other instanceof ApolloEnemy apolloEnemy) {
            if (apolloEnemy.canDealContactDamage()) {
                decreaseHp(apolloEnemy.getDashContactDamage());
            }
            return;
        }
        if (other instanceof ArtemisEnemy artemisEnemy) {
            if (artemisEnemy.canDealContactDamage()) {
                decreaseHp(artemisEnemy.getDashContactDamage());
            }
            return;
        }
        if (other instanceof AbstractEnemy) {
            decreaseHp(Integer.MAX_VALUE);
        }
    }

    @Override
    public void setShootStrategy(ShootStrategy strategy, long durationMs) {
        super.setShootStrategy(strategy);
        if (fireStrategyThread == null || !fireStrategyThread.isAlive()) {
            fireStrategyThread = new TimeOutThread(durationMs, () -> {
                HeroAircraft.super.setShootStrategy(new StraightShootStrategy());
                fireStrategyThread = null;
            });
            fireStrategyThread.start();
        } else {
            fireStrategyThread.extend(durationMs);
        }
    }

}

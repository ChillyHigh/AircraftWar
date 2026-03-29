package edu.hitsz.aircraft.enemy;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.basic.SpriteAnimator;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ApolloEnemy extends AbstractEnemy {

    private final SpriteAnimator animator;

    private enum State {
        INIT,
        NORMAL,
        DAMAGED,
        CRITICAL
    }

    private State state = State.INIT;
    private int counter1 = 0;

    private final Ribbon leftRibbon = new Ribbon(-1, 0.5);
    private final Ribbon rightRibbon = new Ribbon(1, 0.5);

    private final List<BaseBullet> bullets = new ArrayList<>();

    public ApolloEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        // 设置Boss敌机分数
        super.score = 100;
        super.rotation = 180.0;
        super.scale = 0.5;
        this.animator = new SpriteAnimator("apollo",
                204, 226, 10, 9, 5, 3);
        this.width = animator.getFrameWidth();
        this.height = animator.getFrameHeight();

    }

    @Override
    public void forward() {
        if (state == State.INIT) {
            updateInit();
        }
        if (state == State.NORMAL) {
            updateNormal();
        }
        animator.update();
        leftRibbon.update(locationX, locationY, rotation);
        rightRibbon.update(locationX, locationY, rotation);

    }

    /**
     * Boss敌机在创建之后会自动飞行到英雄飞机附近，之后才开始正常飞行和攻击<br>
     * 该方法计算Boss敌机的初始飞行路径
     */
    private void updateInit() {
        HeroAircraft hero = HeroAircraft.getHeroAircraft();

        double targetX = hero.getLocationX() + 120;
        double targetY = hero.getLocationY() - 140;
        double distance = Math.hypot(targetX - locationX, targetY - locationY);
        double speed = 10.0;
        if (distance < 10.0) {
            speed = speed * (distance / 10.0); // 距离越近速度越慢，避免过冲
        }
        if (moveToward(targetX, targetY, speed, 0.9))
            state = State.NORMAL;
        rotateToward(targetX - 120, targetY + 140, 5.0, 0.9);
    }

    private void updateNormal() {
        HeroAircraft hero = HeroAircraft.getHeroAircraft();
        counter1++;
        if (counter1 == 50 || counter1 == 100 || counter1 == 200 || counter1 == 250) {
            // 先清空之前的子弹列表，避免子弹无限增加
            bullets.clear();
            // 添加朝着英雄飞机方向的子弹
            double bulletSpeed = 5.0;
            double dx = hero.getLocationX() - locationX;
            double dy = hero.getLocationY() - locationY;
            double distance = Math.hypot(dx, dy);
            if (distance > 1e-6) {
                double bulletSpeedX = dx / distance * bulletSpeed;
                double bulletSpeedY = dy / distance * bulletSpeed;
                bullets.add(new EnemyBullet(locationX,
                        locationY,
                        (int) Math.round(bulletSpeedX),
                        (int) Math.round(bulletSpeedY),
                        0));
            }
        }
        double targetX = hero.getLocationX() + 120;
        double targetY = hero.getLocationY() - 140;
        if (150 <= counter1 && counter1 <= 300) {
            targetX -= 240;
        }
        if (counter1 > 300) {
            counter1 = 0;
        }
        moveToward(targetX, targetY, 10.0, 0.9);
        rotateToward(hero.getLocationX(), hero.getLocationY(), 10.0, 0.9);
    }

    @Override
    public Image getImage() {
        return animator.getCurrentImage();
    }

    @Override
    public void decreaseHp(int decrease) {
        if (state == State.INIT) {
            return;
        }
        // hp -= decrease;
        // if (hp <= 0) {
        // hp = 0;
        // vanish();
        // }
    }

    @Override
    public List<BaseBullet> shoot() {
        return this.bullets;
    }

    @Override
    public void preRender(GraphicsContext gc) {
        leftRibbon.render(gc);
        rightRibbon.render(gc);
    }
}

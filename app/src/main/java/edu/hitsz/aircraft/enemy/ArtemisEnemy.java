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
import javafx.scene.paint.Color;

public class ArtemisEnemy extends AbstractEnemy {

    private final SpriteAnimator animator;

    private enum State {
        INIT,
        NORMAL,
        TWIN,
        DASH,
    }

    private State state = State.INIT;
    private int counter1 = 0;

    private double desiredX;
    private double desiredY;
    private double lookX;
    private double lookY;
    private double desiredMoveSpeed;
    private double desiredRotateSpeed;
    private double dashVelocityX;
    private double dashVelocityY;
    private double intertia = 0.7;


    private final Ribbon leftRibbon = new Ribbon(-1, 0.5, Color.rgb(255, 126, 70), Color.rgb(0, 115, 255));
    private final Ribbon rightRibbon = new Ribbon(1, 0.5, Color.rgb(255, 126, 70), Color.rgb(0, 115, 255));

    private final List<BaseBullet> bullets = new ArrayList<>();

    public ArtemisEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        super.score = 100;
        super.rotation = 180.0;
        super.scale = 0.5;
        this.animator = new SpriteAnimator("artemis",
                204, 226, 10, 9, 5, 0);
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
        if (state == State.TWIN) {
            moveToward(desiredX, desiredY, desiredMoveSpeed, intertia);
            rotateToward(lookX, lookY, desiredRotateSpeed, intertia);
        }
        if (state == State.DASH) {
            super.actualX += dashVelocityX;
            super.actualY += dashVelocityY;
            this.locationX = (int) Math.round(actualX);
            this.locationY = (int) Math.round(actualY);
            rotation = Math.toDegrees(Math.atan2(dashVelocityY, dashVelocityX)) + 90;
        }
        animator.update();
        leftRibbon.update(locationX, locationY, Math.toRadians(rotation));
        rightRibbon.update(locationX, locationY, Math.toRadians(rotation));

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
        if (distance < 5.0) {
            state = State.NORMAL;
        }
        moveToward(targetX, targetY, speed, 0.9);
        rotateToward(targetX - 120, targetY + 140, 5.0, 0.9);
    }

    private void updateNormal() {
        HeroAircraft hero = HeroAircraft.getHeroAircraft();
        counter1++;
        if (counter1 == 50 || counter1 == 100 || counter1 == 200 || counter1 == 250) {
            // 先清空之前的子弹列表，避免子弹无限增加
            bullets.clear();
            // 添加朝着飞机指向方向的子弹
            double bulletSpeed = 5.0;
            double angleRad = Math.toRadians(rotation - 90);
            double bulletSpeedX = bulletSpeed * Math.cos(angleRad);
            double bulletSpeedY = bulletSpeed * Math.sin(angleRad);
            bullets.add(new EnemyBullet(locationX,
                    locationY,
                    (int) Math.round(bulletSpeedX),
                    (int) Math.round(bulletSpeedY),
                    0));
        }
        double targetX = hero.getLocationX() - 120;
        double targetY = hero.getLocationY() + 140;
        if (150 <= counter1 && counter1 <= 300) {
            targetX += 240;
        }
        if (counter1 > 300) {
            counter1 = 0;
        }
        double distance = Math.hypot(targetX - locationX, targetY - locationY);
        double speed = 10.0;
        if (distance < 10.0) {
            speed = speed * (distance / 10.0); // 距离越近速度越慢，避免过冲
        }
        moveToward(targetX, targetY, speed, 0.95);
        rotateToward(hero.getLocationX(), hero.getLocationY(), 6.0, 0.9);
    }

    /**
     * Manager调用，用来管理Apollo和Artemis的行为切换<br>
     * 
     * @param targetX
     * @param targetY
     * @param lookX
     * @param lookY
     * @param moveSpeed
     * @param rotateSpeed
     * @param animationIndex
     */
    public void setDesiredState(
            double targetX,
            double targetY,
            double lookX,
            double lookY,
            double moveSpeed,
            double rotateSpeed,
            double intertia,
            int animationIndex) {

        this.desiredX = targetX;
        this.desiredY = targetY;
        this.lookX = lookX;
        this.lookY = lookY;
        this.desiredMoveSpeed = moveSpeed;
        this.desiredRotateSpeed = rotateSpeed;
        this.intertia = intertia;
        animator.setIndex(animationIndex);
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

    public void setState(int state) {
        if (state == 0) {
            this.state = State.NORMAL;
            return;
        } else if (state == 1) {
            this.state = State.TWIN;
            return;
        } else if (state == 2) {
            this.state = State.DASH;
            return;
        }
        this.state = State.INIT;
    }

    public void setDashVelocity(double vx, double vy) {
        this.dashVelocityX = vx;
        this.dashVelocityY = vy;
    }
}

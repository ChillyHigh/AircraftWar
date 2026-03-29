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

    private double speedX = 0;
    private double speedY = 0;
    private double omega = 0.0; // 角速度，单位为度/帧

    private double actualX = 0.0;
    private double actualY = 0.0;

    public AbstractEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT + getHeight() / 2) {
            vanish();
        }
    }

    /**
     * 朝着目标点移动，最大速度为maxSpeed
     * 
     * @param targetX
     * @param targetY
     * @param maxSpeed
     * @param inertia  0.0-1.0，速度更新的惯性，0表示瞬间达到目标速度，1表示永远保持当前速度
     * @return 是否已经到达目标点
     */
    protected boolean moveToward(double targetX, double targetY, double maxSpeed, double inertia) {
        double dx = targetX - locationX;
        double dy = targetY - locationY;
        double distance = Math.hypot(dx, dy);

        if (distance < 1e-6) { // 已经到达目标点
            actualX = targetX;
            actualY = targetY;
            locationX = (int) Math.round(targetX);
            locationY = (int) Math.round(targetY);
            speedX = 0;
            speedY = 0;
            return true;
        }

        // 目标速度
        double targetSpeedX = dx / distance * maxSpeed;
        double targetSpeedY = dy / distance * maxSpeed;

        // 当前速度逐渐逼近目标速度
        speedX = speedX * inertia + targetSpeedX * (1.0 - inertia);
        speedY = speedY * inertia + targetSpeedY * (1.0 - inertia);
        // TODO: 等super换double
        actualX += speedX;
        actualY += speedY;
        this.locationX = (int) Math.round(actualX);
        this.locationY = (int) Math.round(actualY);

        return false;
    }

    protected void rotateToward(double targetX, double targetY, double maxOmega, double inertia) {
        double dx = targetX - this.locationX;
        double dy = targetY - this.locationY;

        if (Math.abs(dx) < 1e-6 && Math.abs(dy) < 1e-6) {
            return;
        }

        double targetRotation = Math.toDegrees(Math.atan2(dy, dx)) + 90.0;

        if (maxOmega == Double.POSITIVE_INFINITY) { // 无旋转限制，直接设置角度
            this.rotation = targetRotation;
            return;
        }
        double targetOmega = normalizeAngle(targetRotation - this.rotation) * 0.1; // Proportional control
        if (targetOmega > maxOmega) {
            targetOmega = maxOmega;
        } else if (targetOmega < -maxOmega) {
            targetOmega = -maxOmega;
        }
        omega = omega * inertia + targetOmega * (1.0 - inertia);

        this.rotation += omega;
    }

    private double normalizeAngle(double angle) {
        return angle - 360.0 * Math.floor((angle + 180.0) / 360.0);
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

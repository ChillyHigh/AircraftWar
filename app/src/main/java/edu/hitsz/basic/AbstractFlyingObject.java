package edu.hitsz.basic;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.ImageManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * 可飞行对象的父类
 *
 * @author hitsz
 */
public abstract class AbstractFlyingObject {
    protected double INIT_ANGLE;

    //图片中心位置坐标
    protected double locationX;
    protected double locationY;

    // 移动速度
    protected double speedX;
    protected double speedY;

    // 对象的缩放大小
    protected double scale = 1.0;

    // 对象的旋转角度，单位：角度
    protected double rotation = 0.0;

    // 对象的旋转速度，单位：角度/帧
    protected double omega = 0.0;

    //图片, null 表示未设置
    protected Image image = null;

    //x 轴长度，根据图片尺寸获得, -1 表示未设置
    protected int width = -1;

    //y 轴长度，根据图片尺寸获得, -1 表示未设置
    protected int height = -1;

    //有效（生存）标记，标记为 false的对象会在下次刷新时清除
    protected boolean isValid = true;

    protected HitBox hitBox;

    public AbstractFlyingObject() {
    }

    public AbstractFlyingObject(double locationX, double locationY, double speedX, double speedY) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    /**
     * 可飞行对象根据速度移动
     */
    public void forward() {
        locationX += speedX;
        locationY += speedY;
    }

    public boolean collidesWith(AbstractFlyingObject flyingObject) {
        return !notValid()
                && !flyingObject.notValid()
                && this.getHitBox().isCollidingWith(flyingObject.getHitBox());
    }

    public void onCollide(AbstractFlyingObject other) {
        // 默认无额外碰撞行为
    }

    public HitBox getHitBox() {
        if (hitBox == null) {
            hitBox = createDefaultHitBox();
        }
        return hitBox;
    }

    protected HitBox createDefaultHitBox() {
        double hitBoxWidth = getWidth();
        double hitBoxHeight = getHeight();

        if (this instanceof AbstractAircraft) {
            hitBoxHeight /= 2.0;
        }

        return new RectHitBox(this, 0.0, 0.0, hitBoxWidth, hitBoxHeight, scale);
    }

    protected void setHitBox(HitBox hitBox) {
        this.hitBox = hitBox;
    }

    public double getLocationX() {
        return locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public void setLocation(double locationX, double locationY) {
        this.locationX = locationX;
        this.locationY = locationY;
    }

    public AbstractFlyingObject setSpeed(double speedX, double speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
        return this;
    }

    public double getSpeedX() {
        return speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public Image getImage() {
        if (image == null) {
            image = ImageManager.get(this);
        }
        return image;
    }

    public int getWidth() {
        if (width == -1) {
            // 若未设置，则查询图片宽度并设置
            Image resolved = ImageManager.get(this);
            if (resolved == null) {
                throw new IllegalStateException(this.getClass().getName() + " has no image!");
            }
            width = (int) resolved.getWidth();
        }
        return width;
    }

    public int getHeight() {
        if (height == -1) {
            // 若未设置，则查询图片高度并设置
            Image resolved = ImageManager.get(this);
            if (resolved == null) {
                throw new IllegalStateException(this.getClass().getName() + " has no image!");
            }
            height = (int) resolved.getHeight();
        }
        return height;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public boolean notValid() {
        return !this.isValid;
    }

    /**
     * 标记消失，
     * isValid = false.
     * notValid() => true.
     */
    public void vanish() {
        isValid = false;
    }

    public void render(GraphicsContext gc) {
        if (notValid()) {
            return;
        }
        Image resolved = getImage();
        if (resolved == null) {
            throw new IllegalStateException(this.getClass().getName() + " has no image!");
        }
        drawImage(gc, resolved, locationX, locationY, rotation + INIT_ANGLE, scale);
    }

    /**
     * 绘制贴图，包含旋转和缩放
     *
     * @param gc        图形上下文
     * @param image     图片
     * @param locationX x 坐标
     * @param locationY y 坐标
     * @param angle     旋转角度，单位：角度
     * @param scale     缩放比例
     */
    public static void drawImage(GraphicsContext gc, Image image, double locationX, double locationY,
                                 double angle, double scale) {
        double drawWidth = image.getWidth() * scale;
        double drawHeight = image.getHeight() * scale;

        gc.save();
        gc.translate(locationX, locationY);
        gc.rotate(angle);
        gc.drawImage(image, -drawWidth / 2.0, -drawHeight / 2.0, drawWidth, drawHeight);
        gc.restore();
    }

    //===== 移动和旋转的方法 =====

    /**
     * 朝着目标点移动，最大速度为maxSpeed
     *
     * @param targetX  目标点 x 坐标
     * @param targetY  目标点 y 坐标
     * @param maxSpeed 最大速度，单位：像素/帧
     * @param inertia  0.0-1.0，速度更新的惯性，0表示瞬间达到目标速度，1表示永远保持当前速度
     * @return 是否已经到达目标点
     */
    protected boolean moveToward(double targetX, double targetY, double maxSpeed, double inertia) {
        double dx = targetX - locationX;
        double dy = targetY - locationY;

        double distance = Math.hypot(dx, dy);

        if (distance < 1 || maxSpeed == Double.POSITIVE_INFINITY) {
            locationX = targetX;
            locationY = targetY;
            speedX = 0;
            speedY = 0;
            return true;
        }

        if (distance < maxSpeed * 0.5) {
            maxSpeed = distance * 0.5;
        }

        double targetSpeedX = dx / distance * maxSpeed;
        double targetSpeedY = dy / distance * maxSpeed;

        speedX = speedX * inertia + targetSpeedX * (1.0 - inertia);
        speedY = speedY * inertia + targetSpeedY * (1.0 - inertia);
        this.locationX += speedX;
        this.locationY += speedY;

        return false;
    }

    /**
     * 朝着目标点旋转，最大旋转速度为maxOmega
     *
     * @param targetX  目标点 x 坐标
     * @param targetY  目标点 y 坐标
     * @param maxOmega 最大旋转速度，单位：角度/帧
     * @param inertia  0.0-1.0，旋转速度更新的惯性，0表示瞬间达到目标旋转速度，1表示永远保持当前旋转速度
     */
    protected void rotateToward(double targetX, double targetY, double maxOmega, double inertia) {
        double dx = targetX - this.locationX;
        double dy = targetY - this.locationY;

        if (Math.abs(dx) < 1e-6 && Math.abs(dy) < 1e-6) {
            return;
        }

        double targetRotation = Math.toDegrees(Math.atan2(dy, dx)) + 90.0;

        if (maxOmega == Double.POSITIVE_INFINITY) {
            this.rotation = targetRotation;
            return;
        }
        double targetOmega = normalizeAngle(targetRotation - this.rotation) * 0.1;
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
}

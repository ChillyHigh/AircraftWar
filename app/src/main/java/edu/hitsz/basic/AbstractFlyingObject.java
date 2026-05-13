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

    // 初始的角度，跟图片有关，默认是0度
    protected double initRotation = 0.0;

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
        hitBox = createDefaultHitBox();
    }

    /**
     * 可飞行对象根据速度移动
     */
    public void forward() {
        locationX += speedX;
        locationY += speedY;
//        handleBoundary();
    }

//    /**
//     * 遇到边界的处理方法
//     */
//    protected void handleBoundary() {
//        if (locationX <= 0 || locationX >= Main.WINDOW_WIDTH) {
//            // 横向超出边界后反向
//            speedX = -speedX;
//        }
//    }

    /**
     * 碰撞检测，当对方坐标进入我方范围，判定我方击中<br>
     * 对方与我方覆盖区域有交叉即判定撞击。
     * <br>
     * 非飞机对象区域：
     * 横向，[x - width/2, x + width/2]
     * 纵向，[y - height/2, y + height/2]
     * <br>
     * 飞机对象区域：
     * 横向，[x - width/2, x + width/2]
     * 纵向，[y - height/4, y + height/4]
     *
     * @param flyingObject 撞击对方
     * @return true: 我方被击中; false 我方未被击中
     */
    public boolean crash(AbstractFlyingObject flyingObject) {
        return this.getHitBox().isCollidingWith(flyingObject.getHitBox());
    }

    public HitBox getHitBox() {
        return hitBox;
    }

    protected HitBox createDefaultHitBox() {
        double hitBoxWidth = getWidth() * scale;
        double hitBoxHeight = getHeight() * scale;

        if (this instanceof AbstractAircraft) {
            hitBoxHeight /= 2.0;
        }

        return new HitBox(0, 0, hitBoxWidth, hitBoxHeight, this);
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
        Image image = getImage();
        if (image == null) {
            throw new IllegalStateException(this.getClass().getName() + " has no image!");
        }
        drawImage(gc, image, locationX, locationY, rotation + initRotation, scale);

        // 绘制出碰撞箱边框
//        hitBox.render(gc);
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

        if (distance < 1 || maxSpeed == Double.POSITIVE_INFINITY) { //已到达目标点 或 无速度限制，直接设置位置
            locationX = targetX;
            locationY = targetY;
            speedX = 0;
            speedY = 0;
            return true;
        }

        // 靠近目标点时速度逐渐降低，避免过冲
        if (distance < maxSpeed * 0.5) {
            maxSpeed = distance * 0.5;
        }

        // 目标速度
        double targetSpeedX = dx / distance * maxSpeed;
        double targetSpeedY = dy / distance * maxSpeed;

        // 当前速度逐渐逼近目标速度
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

}

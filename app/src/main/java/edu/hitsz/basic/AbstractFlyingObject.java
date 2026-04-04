package edu.hitsz.basic;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * 可飞行对象的父类
 *
 * @author hitsz
 */
public abstract class AbstractFlyingObject {

    //图片中心位置坐标
    protected Vec2D location;

    // 移动速度
    protected Vec2D speed;

    // 对象的缩放大小
    protected double scale = 1.0;

    // 对象的旋转角度，单位：角度
    protected double rotation = 0.0;

    //图片, null 表示未设置
    protected Image image = null;

    //x 轴长度，根据图片尺寸获得, -1 表示未设置
    protected int width = -1;

    //y 轴长度，根据图片尺寸获得, -1 表示未设置
    protected int height = -1;

    //有效（生存）标记，标记为 false的对象会在下次刷新时清除
    protected boolean isValid = true;

    public AbstractFlyingObject() {
    }

    public AbstractFlyingObject(double locationX, double locationY, double speedX, double speedY) {
        this.location = new Vec2D(locationX, locationY);
        this.speed = new Vec2D(speedX, speedY);
    }

    /**
     * 可飞行对象根据速度移动
     */
    public void forward() {
        location.add(speed);
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
     *  <br>
     * 非飞机对象区域：
     *  横向，[x - width/2, x + width/2]
     *  纵向，[y - height/2, y + height/2]
     *  <br>
     * 飞机对象区域：
     *  横向，[x - width/2, x + width/2]
     *  纵向，[y - height/4, y + height/4]
     *
     * @param flyingObject 撞击对方
     * @return true: 我方被击中; false 我方未被击中
     */
    public boolean crash(AbstractFlyingObject flyingObject) {
        // 缩放因子，用于控制 y轴方向区域范围
        int factor = this instanceof AbstractAircraft ? 2 : 1; //我方
        int fFactor = flyingObject instanceof AbstractAircraft ? 2 : 1;//对方

        //对方坐标、宽度、高度
        double x = flyingObject.getLocationX();
        double y = flyingObject.getLocationY();
        int fWidth = flyingObject.getWidth();
        int fHeight = flyingObject.getHeight();

        return x + (fWidth+this.getWidth())/2 > location.getX()
                && x - (fWidth+this.getWidth())/2 < location.getX()
                && y + ( fHeight/fFactor+this.getHeight()/factor )/2 > location.getY()
                && y - ( fHeight/fFactor+this.getHeight()/factor )/2 < location.getY();
    }

    public double getLocationX() {
        return location.getX();
    }

    public double getLocationY() {
        return location.getY();
    }

    public Vec2D getLocation() {
        return location;
    }

    public void setLocation(double locationX, double locationY){
        this.location.set(locationX, locationY);
    }


    public AbstractFlyingObject setSpeed(Vec2D speed) {
        this.speed = speed;
        return this;
    }

    public AbstractFlyingObject setSpeed(double speedX, double speedY) {
        this.speed.set(speedX, speedY);
        return this;
    }

    public double getSpeedX() {
        return speed.getX();
    }

    public double getSpeedY() {
        return speed.getY();
    }

    public Image getImage() {
        if (image == null){
            image = ImageManager.get(this);
        }
        return image;
    }

    public int getWidth() {
        if (width == -1){
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
        if (height == -1){
            // 若未设置，则查询图片高度并设置
            Image resolved = ImageManager.get(this);
            if (resolved == null) {
                throw new IllegalStateException(this.getClass().getName() + " has no image!");
            }
            height = (int) resolved.getHeight();
        }
        return height;
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
        if (notValid()){
            return;
        }
        Image image = getImage();
        if (image == null) {
            throw new IllegalStateException(this.getClass().getName() + " has no image!");
        }
        drawImage(gc, image, location, rotation, scale);
    }

    /**
     * 绘制贴图，包含旋转和缩放
     * @param gc 图形上下文
     * @param image 图片
     * @param location 位置
     * @param angle 旋转角度，单位：角度
     * @param scale 缩放比例
     */
    public static void drawImage(GraphicsContext gc, Image image, Vec2D location, double angle, double scale) {
        double drawWidth = image.getWidth() * scale;
        double drawHeight = image.getHeight() * scale;

        gc.save();
        gc.translate(location.getX(), location.getY());
        gc.rotate(angle);
        gc.drawImage(image, -drawWidth / 2.0, -drawHeight / 2.0, drawWidth, drawHeight);
        gc.restore();
    }

}


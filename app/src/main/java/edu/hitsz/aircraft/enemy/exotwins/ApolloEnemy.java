package edu.hitsz.aircraft.enemy.exotwins;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.enemy.ExoTwinsManager;
import edu.hitsz.aircraft.enemy.Ribbon;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.basic.SpriteAnimator;
import edu.hitsz.bullet.BaseBullet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class ApolloEnemy extends AbstractEnemy {
    private static final int DASH_CONTACT_DAMAGE = 300;

    private final SpriteAnimator animator;
    private final SpriteAnimator animatorGlow;
    private final Ribbon leftRibbon = new Ribbon(-1, 0.5, Color.rgb(12, 214, 49), Color.rgb(0, 115, 255));
    private final Ribbon rightRibbon = new Ribbon(1, 0.5, Color.rgb(12, 214, 49), Color.rgb(0, 115, 255));
    private final List<BaseBullet> bullets = new ArrayList<>();

    private boolean contactDamageEnabled = false;
    private boolean dashMode = false;
    private int sharedHp;
    private ExoTwinsManager manager;

    private double desiredX;
    private double desiredY;
    private double lookX;
    private double lookY;
    private double desiredMoveSpeed;
    private double desiredRotateSpeed;
    private double dashVelocityX;
    private double dashVelocityY;
    private double inertia = 0.7;

    public ApolloEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        super.score = 1000;
        super.rotation = 180.0;
        super.scale = 0.5;
        this.sharedHp = hp;
        this.animator = new SpriteAnimator("apollo", 204, 226, 10, 9, 5, 8);
        this.animatorGlow = new SpriteAnimator("apollo_glow", 204, 226, 10, 9, 5, 8);
        this.width = animator.getFrameWidth();
        this.height = animator.getFrameHeight();
    }

    @Override
    public void forward() {
        if (dashMode) {
            locationX += dashVelocityX;
            locationY += dashVelocityY;
            rotation = Math.toDegrees(Math.atan2(dashVelocityY, dashVelocityX)) + 90.0;
        } else {
            moveToward(desiredX, desiredY, desiredMoveSpeed, inertia);
            rotateToward(lookX, lookY, desiredRotateSpeed, inertia);
        }

        animator.update();
        animatorGlow.update();
        leftRibbon.update(locationX, locationY, Math.toRadians(rotation));
        rightRibbon.update(locationX, locationY, Math.toRadians(rotation));
    }

    public void setDesiredState(double targetX, double targetY, double lookX, double lookY, double moveSpeed,
                                double rotateSpeed, double inertia, int animationIndex) {
        this.desiredX = targetX;
        this.desiredY = targetY;
        this.lookX = lookX;
        this.lookY = lookY;
        this.desiredMoveSpeed = moveSpeed;
        this.desiredRotateSpeed = rotateSpeed;
        this.inertia = inertia;
        animator.setIndex(animationIndex);
        animatorGlow.setIndex(animationIndex);
    }

    public void setSharedHp(int hp) {
        this.sharedHp = hp;
        this.hp = hp;
        this.maxHp = hp;
    }

    public void setManager(ExoTwinsManager manager) {
        this.manager = manager;
    }

    public void setContactDamageEnabled(boolean contactDamageEnabled) {
        this.contactDamageEnabled = contactDamageEnabled;
    }

    public boolean canDealContactDamage() {
        return contactDamageEnabled;
    }

    public int getDashContactDamage() {
        return DASH_CONTACT_DAMAGE;
    }

    @Override
    public void decreaseHp(int decrease) {
        if (manager != null) {
            manager.applyDamage(decrease);
            return;
        }
        sharedHp -= decrease;
        hp = sharedHp;
        if (sharedHp <= 0) {
            sharedHp = 0;
            hp = 0;
            vanish();
        }
    }

    @Override
    public List<BaseBullet> shoot() {
        return new ArrayList<>();
    }

    @Override
    public List<BaseBullet> getBullets() {
        List<BaseBullet> firedBullets = new ArrayList<>(bullets);
        bullets.clear();
        return firedBullets;
    }

    @Override
    public Image getImage() {
        return animator.getCurrentImage();
    }

    @Override
    public void render(GraphicsContext gc) {
        leftRibbon.render(gc);
        super.render(gc);

        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.translate(locationX, locationY);
        gc.rotate(rotation);
        gc.drawImage(animatorGlow.getCurrentImage(), -width * scale / 2.0, -height * scale / 2.0,
                width * scale, height * scale);
        gc.restore();

        rightRibbon.render(gc);
    }

    @Override
    public void onCollide(AbstractFlyingObject other) {
        if (other instanceof HeroAircraft) {
            return;
        }
        super.onCollide(other);
    }

    public void setState(int state) {
        boolean nextDashMode = state == 2;
        if (dashMode && !nextDashMode) {
            double dashRotationRad = Math.atan2(dashVelocityY, dashVelocityX) + Math.PI / 2.0;
            leftRibbon.resetHistory(locationX, locationY, dashRotationRad);
            rightRibbon.resetHistory(locationX, locationY, dashRotationRad);
        }
        dashMode = nextDashMode;
    }

    public void setDashVelocity(double vx, double vy) {
        this.dashVelocityX = vx;
        this.dashVelocityY = vy;
    }

    public void addBullet(BaseBullet bullet) {
        bullets.add(bullet);
    }

    @Override
    public void onBomb() {
        this.decreaseHp(200);
    }
}

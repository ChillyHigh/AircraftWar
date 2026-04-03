package edu.hitsz.aircraft.enemy.exotwins;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.enemy.Ribbon;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.SpriteAnimator;
import edu.hitsz.bullet.ArtemisLaserBullet;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.ArtemisRayBullet;
import edu.hitsz.shaderCPU.ArtemisRayTelegraphShader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ArtemisEnemy extends AbstractEnemy {
    private static final double EXOTHERMAL_MUZZLE_DISTANCE = 54.0;
    private static final double EXOTHERMAL_TELEGRAPH_LENGTH = 1000.0;
    private static final int EXOTHERMAL_TELEGRAPH_SHADER_WIDTH = 512;
    private static final int EXOTHERMAL_TELEGRAPH_SHADER_HEIGHT = 64;

    private enum PostRenderEffect {
        NONE,
        EXOTHERMAL_CHARGE_UP,
        EXOTHERMAL_GLEAM,
    }

    private final SpriteAnimator animator;
    private final SpriteAnimator animatorGlow;

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
    private final ArtemisRayTelegraphShader exothermalTelegraphShader =
            new ArtemisRayTelegraphShader(EXOTHERMAL_TELEGRAPH_SHADER_WIDTH, EXOTHERMAL_TELEGRAPH_SHADER_HEIGHT);
    private PostRenderEffect postRenderEffect = PostRenderEffect.NONE;
    private double postRenderEffectProgress = 0.0;
    private int postRenderEffectTimer = 0;

    public ArtemisEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);

        super.score = 100;
        super.rotation = 180.0;
        super.scale = 0.5;
        this.animator = new SpriteAnimator("artemis",
                204, 226, 10, 9, 5, 0);
        this.animatorGlow = new SpriteAnimator("artemis_glow",
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
        animatorGlow.update();
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
        animatorGlow.setIndex(animationIndex);
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
        List<BaseBullet> firedBullets = new ArrayList<>(bullets);
        bullets.clear();
        return firedBullets;
    }

    @Override
    public void preRender(GraphicsContext gc) {
        leftRibbon.render(gc);
        rightRibbon.render(gc);
    }

    @Override
    public void postRender(GraphicsContext gc) {
        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.translate(locationX, locationY);
        gc.rotate(rotation);
        gc.drawImage(animatorGlow.getCurrentImage(), -width * scale / 2.0, -height * scale / 2.0,
                width * scale, height * scale);
        gc.restore();

        renderExothermalPostEffect(gc);
    }

    public void setState(int state) {
        State nextState;
        if (state == 0) {
            nextState = State.NORMAL;
        } else if (state == 1) {
            nextState = State.TWIN;
        } else if (state == 2) {
            nextState = State.DASH;
        } else {
            nextState = State.INIT;
        }
        if (this.state != State.DASH && nextState == State.DASH) {
            double dashRotationRad = Math.atan2(dashVelocityY, dashVelocityX) + Math.PI / 2.0;
            leftRibbon.resetHistory(locationX, locationY, dashRotationRad);
            rightRibbon.resetHistory(locationX, locationY, dashRotationRad);
        }
        this.state = nextState;
    }

    public void setDashVelocity(double vx, double vy) {
        this.dashVelocityX = vx;
        this.dashVelocityY = vy;
    }

    public void queueBulletByAngle(double angleRad, double bulletSpeed, int bulletPower) {
        int muzzleDistance = 18; // 子弹从飞机到枪口的距离
        double bulletX = locationX + Math.cos(angleRad) * muzzleDistance;
        double bulletY = locationY + Math.sin(angleRad) * muzzleDistance;
        queueBulletFromPosition(bulletX, bulletY, angleRad, bulletSpeed, bulletPower);
    }

    public void queueBulletFromPosition(double spawnX, double spawnY, double angleRad, double bulletSpeed, int bulletPower) {
        int bulletSpeedX = (int) Math.round(Math.cos(angleRad) * bulletSpeed);
        int bulletSpeedY = (int) Math.round(Math.sin(angleRad) * bulletSpeed);

        bullets.add(new ArtemisLaserBullet((int) Math.round(spawnX), (int) Math.round(spawnY), bulletSpeedX, bulletSpeedY, bulletPower));
    }

    public void queueExothermalDisintegrationRay(int bulletPower) {
        bullets.add(new ArtemisRayBullet(this, bulletPower));
    }

    public void clearExothermalPostRenderEffect() {
        postRenderEffect = PostRenderEffect.NONE;
        postRenderEffectProgress = 0.0;
        postRenderEffectTimer = 0;
    }

    public void setExothermalChargeUpEffect(double progress) {
        updatePostRenderEffect(PostRenderEffect.EXOTHERMAL_CHARGE_UP, progress);
    }

    public void setExothermalGleamEffect(double progress) {
        updatePostRenderEffect(PostRenderEffect.EXOTHERMAL_GLEAM, progress);
    }

    public double getExothermalMuzzleX() {
        return locationX + Math.cos(getForwardAngleRad()) * EXOTHERMAL_MUZZLE_DISTANCE;
    }

    public double getExothermalMuzzleY() {
        return locationY + Math.sin(getForwardAngleRad()) * EXOTHERMAL_MUZZLE_DISTANCE;
    }

    public double getForwardAngleRad() {
        return Math.toRadians(rotation - 90.0);
    }

    private void updatePostRenderEffect(PostRenderEffect effect, double progress) {
        if (effect == PostRenderEffect.NONE) {
            clearExothermalPostRenderEffect();
            return;
        }

        if (postRenderEffect != effect) {
            postRenderEffectTimer = 0;
        } else {
            postRenderEffectTimer++;
        }
        postRenderEffect = effect;
        postRenderEffectProgress = Math.clamp(progress, 0.0, 1.0);
    }

    private void renderExothermalPostEffect(GraphicsContext gc) {
        switch (postRenderEffect) {
            case NONE -> {
                return;
            }
            case EXOTHERMAL_CHARGE_UP -> drawExothermalChargeUp(gc);
            case EXOTHERMAL_GLEAM -> drawExothermalGleam(gc);
        }
    }

    private void drawExothermalChargeUp(GraphicsContext gc) {
        double chargeInterpolant = inverseLerpBump(0.0, 0.24, 0.9, 1.0, postRenderEffectProgress);
        if (chargeInterpolant <= 1e-3) {
            return;
        }

        double startX = getExothermalMuzzleX();
        double startY = getExothermalMuzzleY();
        double angle = getForwardAngleRad();
        double beamWidth = chargeInterpolant * 5.0;
        double beamHeight = beamWidth * 2.0;
        exothermalTelegraphShader.renderFrame(postRenderEffectTimer / 60.0, chargeInterpolant);

        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.translate(startX, startY);
        gc.rotate(Math.toDegrees(angle));
        gc.drawImage(
                exothermalTelegraphShader.getOutputImage(),
                0.0,
                -beamHeight * 0.5,
                EXOTHERMAL_TELEGRAPH_LENGTH,
                beamHeight);
        gc.restore();
    }

    private void drawExothermalGleam(GraphicsContext gc) {
        double gleamInterpolant = inverseLerp(0.0, 0.7, postRenderEffectProgress);
        if (gleamInterpolant <= 1e-3) {
            return;
        }

        double flareOpacity = inverseLerp(1.0, 0.75, gleamInterpolant);
        double flareScale = Math.pow(convert01To010(gleamInterpolant), 1.4) * 1.9 + 0.1;
        flareScale *= 0.4;
        double flareRotation = smoothStep(0.0, Math.PI * 2.0, Math.pow(gleamInterpolant, 0.2)) + Math.PI / 4.0;

        Image bloom = ImageManager.EXO_MUZZLE_BLOOM_IMAGE;
        Image flare = ImageManager.EXO_MUZZLE_FLARE_IMAGE;
        double drawX = getExothermalMuzzleX();
        double drawY = getExothermalMuzzleY();

        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);

        gc.setGlobalAlpha(flareOpacity * 0.30);
        drawCenteredImage(gc, bloom, drawX, drawY, flareScale * 1.9, 0.0);

        gc.setGlobalAlpha(flareOpacity * 0.54);
        drawCenteredImage(gc, bloom, drawX, drawY, flareScale, 0.0);

        gc.setGlobalAlpha(flareOpacity);
        drawCenteredImage(gc, flare, drawX, drawY, flareScale, flareRotation);

        gc.restore();
    }

    private void drawCenteredImage(GraphicsContext gc, Image image, double centerX, double centerY, double scaleFactor, double rotationRad) {
        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(Math.toDegrees(rotationRad));
        gc.drawImage(
                image,
                -image.getWidth() * scaleFactor * 0.5,
                -image.getHeight() * scaleFactor * 0.5,
                image.getWidth() * scaleFactor,
                image.getHeight() * scaleFactor);
        gc.restore();
    }


    private double inverseLerp(double start, double end, double value) {
        if (Math.abs(end - start) < 1e-6) {
            return 0.0;
        }
        return Math.clamp((value - start) / (end - start), 0.0, 1.0);
    }

    private double inverseLerpBump(double startFadeIn, double endFadeIn, double startFadeOut, double endFadeOut, double value) {
        return inverseLerp(startFadeIn, endFadeIn, value) * inverseLerp(endFadeOut, startFadeOut, value);
    }

    private double convert01To010(double value) {
        return 1.0 - Math.abs(Math.clamp(value, 0.0, 1.0) * 2.0 - 1.0);
    }

    private double smoothStep(double start, double end, double value) {
        double t = Math.clamp(value, 0.0, 1.0);
        t = t * t * (3.0 - 2.0 * t);
        return start + (end - start) * t;
    }

}

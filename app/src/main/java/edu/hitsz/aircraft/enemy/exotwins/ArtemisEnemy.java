package edu.hitsz.aircraft.enemy.exotwins;

import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.enemy.ExoTwinsManager;
import edu.hitsz.aircraft.enemy.Ribbon;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.basic.SpriteAnimator;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.shaderCPU.ArtemisRayTelegraphShader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class ArtemisEnemy extends AbstractEnemy {
    private static final int DASH_CONTACT_DAMAGE = 300;
    private static final double EXOTHERMAL_MUZZLE_DISTANCE = 54.0;
    private static final double EXOTHERMAL_TELEGRAPH_LENGTH = 1000.0;
    private static final int EXOTHERMAL_TELEGRAPH_SHADER_WIDTH = 512;
    private static final int EXOTHERMAL_TELEGRAPH_SHADER_HEIGHT = 64;

    private enum PostRenderEffect {
        NONE,
        EXOTHERMAL_CHARGE_UP,
        EXOTHERMAL_GLEAM
    }

    private final SpriteAnimator animator;
    private final SpriteAnimator animatorGlow;
    private final Ribbon leftRibbon = new Ribbon(-1, 0.5, Color.rgb(255, 126, 70), Color.rgb(0, 115, 255));
    private final Ribbon rightRibbon = new Ribbon(1, 0.5, Color.rgb(255, 126, 70), Color.rgb(0, 115, 255));
    private final List<BaseBullet> bullets = new ArrayList<>();
    private final ArtemisRayTelegraphShader exothermalTelegraphShader =
            new ArtemisRayTelegraphShader(EXOTHERMAL_TELEGRAPH_SHADER_WIDTH, EXOTHERMAL_TELEGRAPH_SHADER_HEIGHT);

    private PostRenderEffect postRenderEffect = PostRenderEffect.NONE;
    private boolean contactDamageEnabled = false;
    private boolean dashMode = false;
    private boolean deathAnimationMode = false;
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
    private double postRenderEffectProgress = 0.0;
    private int postRenderEffectTimer = 0;

    public ArtemisEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        super.score = 1000;
        super.rotation = 180.0;
        super.scale = 0.5;
        this.sharedHp = hp;
        this.animator = new SpriteAnimator("artemis", 204, 226, 10, 9, 5, 8);
        this.animatorGlow = new SpriteAnimator("artemis_glow", 204, 226, 10, 9, 5, 8);
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
        if (deathAnimationMode) {
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    @Override
    public List<BaseBullet> getBullets() {
        if (deathAnimationMode) {
            bullets.clear();
            return new ArrayList<>();
        }
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

        renderExothermalPostEffect(gc);
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
        if (!dashMode && nextDashMode) {
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

    public void beginDeathAnimation() {
        deathAnimationMode = true;
        bullets.clear();
        clearExothermalPostRenderEffect();
        contactDamageEnabled = false;
        dashMode = false;
        speedX = 0.0;
        speedY = 0.0;
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
        gc.drawImage(exothermalTelegraphShader.getOutputImage(), 0.0, -beamHeight * 0.5,
                EXOTHERMAL_TELEGRAPH_LENGTH, beamHeight);
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
        gc.drawImage(image, -image.getWidth() * scaleFactor * 0.5, -image.getHeight() * scaleFactor * 0.5,
                image.getWidth() * scaleFactor, image.getHeight() * scaleFactor);
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

    @Override
    public void onBomb() {
        this.decreaseHp(200);
    }
}

package edu.hitsz.bullet;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.enemy.exotwins.ArtemisEnemy;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.basic.CapsuleHitBox;
import edu.hitsz.basic.HitBox;
import edu.hitsz.shaderCPU.ArtemisRayBulletShader;
import edu.hitsz.shaderCPU.PrimitiveBloomShaderCPU;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;

public class ArtemisRayBullet extends BaseBullet {

    public static final int LIFETIME = 132;

    private static final double MAX_LENGTH = 1000.0;
    private static final double LENGTH_GROWTH_PER_FRAME = 58.0;
    private static final double MUZZLE_DISTANCE = 34.0;
    private static final int SHADER_WIDTH = 512;
    private static final int SHADER_HEIGHT = 144;
    private static final double HITBOX_RADIUS = 18.0;

    private final ArtemisEnemy owner;
    private final PrimitiveBloomShaderCPU bloomShader;
    private final ArtemisRayBulletShader beamShader;

    private int timer = 0;
    private double laserLength = 0.0;

    public ArtemisRayBullet(ArtemisEnemy owner, int power) {
        super(owner.getLocationX(), owner.getLocationY(), 0, 0, power, 0.0);
        this.owner = owner;
        this.bloomShader = new PrimitiveBloomShaderCPU(SHADER_WIDTH, SHADER_HEIGHT);
        this.beamShader = new ArtemisRayBulletShader(SHADER_WIDTH, SHADER_HEIGHT);
        this.width = 1;
        this.height = 1;
        this.scale = 0.5;
    }

    @Override
    public void forward() {
        if (owner == null || owner.notValid()) {
            vanish();
            return;
        }

        timer++;
        laserLength = Math.min(MAX_LENGTH, laserLength + LENGTH_GROWTH_PER_FRAME);
        locationX = getBeamStartX();
        locationY = getBeamStartY();
        rotation = Math.toDegrees(getDirectionAngle());

        if (timer >= LIFETIME) {
            vanish();
        }
    }

    @Override
    protected HitBox createDefaultHitBox() {
        return new CapsuleHitBox(this, 0.0, 0.0, 0.0, HITBOX_RADIUS, 0.0);
    }

    @Override
    public HitBox getHitBox() {
        HitBox hitBox = super.getHitBox();
        if (hitBox instanceof CapsuleHitBox capsule) {
            capsule.setLength(laserLength);
            capsule.setRadius(HITBOX_RADIUS);
            capsule.setLocalOffset(laserLength * 0.5, 0.0);
            capsule.setAngleDegrees(rotation);
        }
        return hitBox;
    }

    @Override
    public void onCollide(AbstractFlyingObject other) {
        if (other instanceof HeroAircraft) {
            // 长激光在命中英雄后继续存在
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (notValid() || owner == null || owner.notValid()) {
            return;
        }

        double beamScale = computeBeamScale();
        if (beamScale <= 1e-3) {
            return;
        }

        double startX = getBeamStartX();
        double startY = getBeamStartY();
        double pulse = 0.92 + Math.sin(timer * 0.55) * 0.08;
        double drawHeight = 124.0 * beamScale * pulse;
        bloomShader.renderFrame(0.45, 0.32 * beamScale);
        beamShader.renderFrame(timer, beamScale, pulse);

        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.translate(startX, startY);
        gc.scale(scale, scale);
        gc.rotate(Math.toDegrees(getDirectionAngle()));
        gc.drawImage(bloomShader.getOutputImage(), 0.0, -drawHeight * 0.70, laserLength, drawHeight * 1.40);
        gc.drawImage(beamShader.getOutputImage(), 0.0, -drawHeight * 0.7, laserLength, drawHeight * 1.40);
        double nozzleGlowSize = 56.0 * beamScale * pulse;
        gc.setFill(Color.rgb(255, 132, 38, 0.40 * beamScale));
        gc.fillOval(-nozzleGlowSize * 0.5, -nozzleGlowSize * 0.5, nozzleGlowSize, nozzleGlowSize);
        gc.restore();
    }

    private double computeBeamScale() {
        double appear = Math.min(0.7, timer / 14.0);
        double disappear = Math.clamp((LIFETIME - timer) / 12.0, 0.0, 0.7);
        return Math.sin(appear * Math.PI * 0.5) * disappear;
    }

    private double getBeamStartX() {
        return owner.getLocationX() + Math.cos(getDirectionAngle()) * MUZZLE_DISTANCE;
    }

    private double getBeamStartY() {
        return owner.getLocationY() + Math.sin(getDirectionAngle()) * MUZZLE_DISTANCE;
    }

    private double getDirectionAngle() {
        return Math.toRadians(owner.getRotation() - 90.0);
    }
}

package edu.hitsz.bullet;

import edu.hitsz.aircraft.enemy.exotwins.ArtemisEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.CustomRenderable;
import edu.hitsz.shaderCPU.ArtemisRayBulletShader;
import edu.hitsz.shaderCPU.PrimitiveBloomShaderCPU;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ArtemisRayBullet extends BaseBullet implements CustomRenderable {

    public static final int LIFETIME = 132;

    private static final double MAX_LENGTH = 1000.0;
    private static final double LENGTH_GROWTH_PER_FRAME = 58.0;
    private static final double MUZZLE_DISTANCE = 34.0;
    private static final int SHADER_WIDTH = 512;
    private static final int SHADER_HEIGHT = 144;

    private final ArtemisEnemy owner;
    private final PrimitiveBloomShaderCPU bloomShader;
    private final ArtemisRayBulletShader beamShader;

    private int timer = 0;
    private double laserLength = 0.0;

    public ArtemisRayBullet(ArtemisEnemy owner, int power) {
        super(owner.getLocationX(), owner.getLocationY(), 0, 0, power);
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
        locationX = (int) Math.round(getBeamStartX());
        locationY = (int) Math.round(getBeamStartY());

        if (timer >= LIFETIME) {
            vanish();
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
//        gc.fillOval(laserLength - nozzleGlowSize * 0.5, -nozzleGlowSize * 0.5, nozzleGlowSize, nozzleGlowSize);
        gc.restore();
    }

    @Override
    public void renderOverlay(GraphicsContext gc) {
        if (notValid() || owner == null || owner.notValid()) {
            return;
        }

        double beamScale = computeBeamScale();
        if (beamScale <= 1e-3) {
            return;
        }

        double pulse = 0.92 + Math.sin(timer * 0.55) * 0.08;

//        gc.save();
//        gc.setGlobalBlendMode(BlendMode.ADD);
//        gc.translate(getBeamStartX(), getBeamStartY());
//        gc.rotate(Math.toDegrees(getDirectionAngle()));
//        drawMuzzleBloom(gc, beamScale * 1.3, pulse);
//        drawMuzzleFlare(gc, beamScale * 1.3, pulse);
//        gc.restore();
    }

    private void drawMuzzleBloom(GraphicsContext gc, double beamScale, double pulse) {
        Image bloom = ImageManager.EXO_MUZZLE_BLOOM_IMAGE;

        double outerScale = 1.62 * beamScale * pulse;
        gc.setGlobalAlpha(0.30 * beamScale);
        gc.drawImage(
                bloom,
                -bloom.getWidth() * outerScale * 0.5,
                -bloom.getHeight() * outerScale * 0.5,
                bloom.getWidth() * outerScale,
                bloom.getHeight() * outerScale);

        double innerScale = 0.86 * beamScale * pulse * scale;
        gc.setGlobalAlpha(0.54 * beamScale);
        gc.drawImage(
                bloom,
                -bloom.getWidth() * innerScale * 0.5,
                -bloom.getHeight() * innerScale * 0.5,
                bloom.getWidth() * innerScale,
                bloom.getHeight() * innerScale);

        double coreScale = 0.34 * beamScale * (0.92 + pulse * 0.12);
        gc.setGlobalAlpha(0.96 * beamScale);
        gc.drawImage(
                bloom,
                -bloom.getWidth() * coreScale * 0.5,
                -bloom.getHeight() * coreScale * 0.5,
                bloom.getWidth() * coreScale,
                bloom.getHeight() * coreScale);
        gc.setGlobalAlpha(1.0);
    }

    private void drawMuzzleFlare(GraphicsContext gc, double beamScale, double pulse) {
        Image flare = ImageManager.EXO_MUZZLE_FLARE_IMAGE;
        double flarePulse = 0.86 + Math.sin(timer * 0.32) * 0.14;
        double flareScale = 0.92 * beamScale * pulse * flarePulse;

        gc.save();
        gc.rotate(Math.toDegrees(Math.PI / 4.0 + timer * 0.03));
        gc.setGlobalAlpha(0.88 * beamScale);
        gc.drawImage(
                flare,
                -flare.getWidth() * flareScale * 0.5,
                -flare.getHeight() * flareScale * 0.5,
                flare.getWidth() * flareScale,
                flare.getHeight() * flareScale);
        gc.restore();
        gc.setGlobalAlpha(1.0);
    }

    private double computeOverlayScale() {
        return Math.clamp(currentBeamBaseWidth() / 100.0, 0.0, 1.35);
    }

    private double currentBeamBaseWidth() {
        double appear = inverseLerp(0.0, 60.0, timer);
        double disappear = inverseLerp(0.0, 12.0, LIFETIME - timer);
        return elasticOut(appear) * disappear * 100.0;
    }

    private double laserWidthFunction(double completionRatio) {
        double pulsation = Math.sin(Math.PI * completionRatio * 8.0 - Math.PI * 2.0 * timer / 10.0) * 18.0;
        double baseWidth = pulsation + currentBeamBaseWidth();
        double startInterpolant = inverseLerp(0.02, 0.09, completionRatio);
        double startingScaleFactor = smoothStep(0.0, 1.0, Math.pow(startInterpolant, 0.45));
        return Math.max(0.0, baseWidth * startingScaleFactor);
    }

    private double bloomWidthFunction(double completionRatio) {
        return laserWidthFunction(completionRatio) * 1.8;
    }

    private double bloomOpacityFunction(double completionRatio) {
        return inverseLerp(0.01, 0.065, completionRatio)
                * inverseLerp(0.9, 0.7, completionRatio)
                * 0.32;
    }

    private double inverseLerp(double start, double end, double value) {
        if (Math.abs(end - start) < 1e-6) {
            return 0.0;
        }
        return Math.clamp((value - start) / (end - start), 0.0, 1.0);
    }

    private double smoothStep(double start, double end, double value) {
        double t = inverseLerp(start, end, value);
        return t * t * (3.0 - 2.0 * t);
    }

    private double elasticOut(double value) {
        double x = Math.clamp(value, 0.0, 1.0);
        if (x <= 0.0) {
            return 0.0;
        }
        if (x >= 1.0) {
            return 1.0;
        }
        double c4 = 2.0 * Math.PI / 3.0;
        return Math.pow(2.0, -10.0 * x) * Math.sin((x * 10.0 - 0.75) * c4) + 1.0;
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

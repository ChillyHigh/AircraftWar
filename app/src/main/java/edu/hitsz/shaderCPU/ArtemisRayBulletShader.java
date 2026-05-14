package edu.hitsz.shaderCPU;

import edu.hitsz.application.ImageManager;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;

public class ArtemisRayBulletShader extends ShaderCPU {
    private static final double EDGE_GLOW_INTENSITY = 0.15;
    private static final double EDGE_COLOR_SUB_R = 0.1;
    private static final double EDGE_COLOR_SUB_G = 0.9;
    private static final double EDGE_COLOR_SUB_B = 0.9;

    private final int[] techyNoisePixels;
    private final int techyNoiseWidth;
    private final int techyNoiseHeight;

    private final int[] blotchNoisePixels;
    private final int blotchNoiseWidth;
    private final int blotchNoiseHeight;

    private double time;
    private double beamOpacity;
    private double pulse;

    public ArtemisRayBulletShader(int width, int height) {
        super(width, height);

        Image techyNoise = ImageManager.TECHY_NOISE;
        techyNoiseWidth = (int) techyNoise.getWidth();
        techyNoiseHeight = (int) techyNoise.getHeight();
        techyNoisePixels = new int[techyNoiseWidth * techyNoiseHeight];
        techyNoise.getPixelReader().getPixels(
                0, 0, techyNoiseWidth, techyNoiseHeight,
                PixelFormat.getIntArgbInstance(), techyNoisePixels, 0, techyNoiseWidth);

        Image blotchNoise = ImageManager.WAVY_BLOTCH_NOISE;
        blotchNoiseWidth = (int) blotchNoise.getWidth();
        blotchNoiseHeight = (int) blotchNoise.getHeight();
        blotchNoisePixels = new int[blotchNoiseWidth * blotchNoiseHeight];
        blotchNoise.getPixelReader().getPixels(
                0, 0, blotchNoiseWidth, blotchNoiseHeight,
                PixelFormat.getIntArgbInstance(), blotchNoisePixels, 0, blotchNoiseWidth);
    }

    public void renderFrame(double time, double beamOpacity, double pulse) {
        this.time = time;
        this.beamOpacity = beamOpacity;
        this.pulse = pulse;
        render();
    }

    @Override
    protected int shade(int x, int y, double u, double v) {
        double coordsX = u;
        double coordsY = v;
        double widthScale = nozzleWidthScale(coordsX);
        double localY = (coordsY - 0.5) / Math.max(widthScale, 1e-4) + 0.5;
        if (localY <= 0.0 || localY >= 1.0) {
            return 0;
        }

        double distanceFromCenter = Math.abs(localY - 0.5);

        double baseR = 1.0;
        double baseG = 0.56;
        double baseB = 0.0;
        double baseA = 0.95 * beamOpacity * pulse;

        double edgeGlow = EDGE_GLOW_INTENSITY / Math.pow(Math.max(distanceFromCenter, 0.0015), 0.8);
        double r = saturate(baseR * edgeGlow);
        double g = saturate(baseG * edgeGlow);
        double b = saturate(baseB * edgeGlow);
        double a = saturate(baseA * edgeGlow);

        r -= distanceFromCenter * EDGE_COLOR_SUB_R;
        g -= distanceFromCenter * EDGE_COLOR_SUB_G;
        b -= distanceFromCenter * EDGE_COLOR_SUB_B;

        double lightning = sampleChannel(
                techyNoisePixels, techyNoiseWidth, techyNoiseHeight,
                coordsX * 0.9 + time * -1.9, localY, 0);
        double additive = lightning * a * (g + 0.35);
        r += additive;
        g += additive;
        b += additive;

        double centerGlow = 0.1 / Math.max(distanceFromCenter, 0.0015);
        r += centerGlow;
        g += centerGlow * 0.72;
        b += centerGlow * 0.18;

        double edgeFade = smoothstep(0.5, 0.3, distanceFromCenter);
        r *= edgeFade;
        g *= edgeFade;
        b *= edgeFade;
        a *= edgeFade;

        double noise = sampleChannel(
                blotchNoisePixels, blotchNoiseWidth, blotchNoiseHeight,
                coordsX * 0.8 + time * -3.3, localY * 1.75, 0);
        double endOfLaserFade = smoothstep(0.98, 0.9 + noise * 0.06, coordsX);
        r *= endOfLaserFade;
        g *= endOfLaserFade;
        b *= endOfLaserFade;
        a *= endOfLaserFade;

        double finalNoise = noise + 1.0 + step(0.5, noise + (0.5 - distanceFromCenter));
        r *= finalNoise;
        g *= finalNoise;
        b *= finalNoise;
        a *= finalNoise;

        return toArgb(a, r, g, b);
    }

    private static double nozzleWidthScale(double completionRatio) {
        double startInterpolant = inverseLerp(0.02, 0.09, completionRatio);
        return smoothstep(0.0, 1.0, Math.pow(startInterpolant, 0.45));
    }

    private static double inverseLerp(double start, double end, double value) {
        if (Math.abs(end - start) <= 1e-8) {
            return value >= end ? 1.0 : 0.0;
        }
        return Math.clamp((value - start) / (end - start), 0.0, 1.0);
    }
}

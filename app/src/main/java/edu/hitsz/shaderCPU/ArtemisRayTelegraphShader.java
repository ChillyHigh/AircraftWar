package edu.hitsz.shaderCPU;

import edu.hitsz.application.ImageManager;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;

public class ArtemisRayTelegraphShader extends ShaderCPU {
    private static final double BASE_R = 1.0;
    private static final double BASE_G = 82.0 / 255.0;
    private static final double BASE_B = 10.0 / 255.0;

    private final int[] noisePixels;
    private final int noiseWidth;
    private final int noiseHeight;

    private double globalTime;
    private double opacity;

    public ArtemisRayTelegraphShader(int width, int height) {
        super(width, height);
        Image noise = ImageManager.WAVY_BLOTCH_NOISE;
        noiseWidth = (int) noise.getWidth();
        noiseHeight = (int) noise.getHeight();
        noisePixels = new int[noiseWidth * noiseHeight];
        noise.getPixelReader().getPixels(
                0, 0, noiseWidth, noiseHeight,
                PixelFormat.getIntArgbInstance(), noisePixels, 0, noiseWidth);
    }

    public void renderFrame(double globalTime, double opacity) {
        this.globalTime = globalTime;
        this.opacity = opacity;
        render();
    }

    @Override
    protected int shade(int x, int y, double u, double v) {
        double horizontalBump = quadraticBump(v);
        double innerHorizontalInterpolant = smoothstep(0.89, 1.0, horizontalBump);

        double r = BASE_R * Math.pow(horizontalBump, 2.0);
        double g = BASE_G * Math.pow(horizontalBump, 2.0);
        double b = BASE_B * Math.pow(horizontalBump, 2.0);
        double a = opacity * Math.pow(horizontalBump, 2.0);

        r += innerHorizontalInterpolant * 0.5;
        g += innerHorizontalInterpolant * 0.5;
        b += innerHorizontalInterpolant * 0.5;
        a += innerHorizontalInterpolant * 0.5;

        double firstNoise = sampleChannel(noisePixels, noiseWidth, noiseHeight, u * 20.0, 0.0, 0);
        double secondNoise = sampleChannel(noisePixels, noiseWidth, noiseHeight, u * 6.0 - globalTime * 3.0 - firstNoise * 0.2, v, 0);
        double amplification = 1.0 + smoothstep(0.04, 0.0, secondNoise) * a * Math.pow(innerHorizontalInterpolant, 2.0) * 8.0;

        r *= amplification;
        g *= amplification;
        b *= amplification;
        a *= amplification;

        return toArgb(a, r, g, b);
    }

    private double quadraticBump(double value) {
        return value * (4.0 - value * 4.0);
    }
}

package edu.hitsz.shaderCPU;

import edu.hitsz.application.ImageManager;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;

public class ExoMechCloudShader extends ShaderCPU {
    private static final double TAU = 6.283185307179586;
    private static final double CLOUD_EXPOSURE = 0.7;
    private static final double VERTICAL_SPEED = 0.016;
    private static final double HORIZONTAL_SPEED = -0.00045;
    private static final double SUN_X = 0.62;
    private static final double SUN_Y = -0.34;
    private static final double SUN_Z = 1.25;

    private final int[] cloudPixels;
    private final int cloudWidth;
    private final int cloudHeight;

    private double cloudTime;
    private double horizontalOffset;
    private double verticalOffset;
    private double cloudDensity = 0.8;

    public ExoMechCloudShader(int width, int height) {
        super(width, height);

        Image cloudMap = ImageManager.CLOUD_DENSITY_MAP;
        this.cloudWidth = (int) cloudMap.getWidth();
        this.cloudHeight = (int) cloudMap.getHeight();
        this.cloudPixels = new int[cloudWidth * cloudHeight];
        cloudMap.getPixelReader().getPixels(0, 0, cloudWidth, cloudHeight,
                PixelFormat.getIntArgbInstance(), this.cloudPixels, 0, cloudWidth);
    }

    public void renderFrame() {
        cloudTime += 0.054;
        horizontalOffset += HORIZONTAL_SPEED;
        verticalOffset -= VERTICAL_SPEED;
        render();
    }

    @Override
    protected int shade(int x, int y, double u, double v) {
        double rayOriginX = u;
        double rayOriginY = v;
        double light = calculateScatteredLight(rayOriginX, rayOriginY) * lerp(0.75, 1.0, 1.0 - v);
        // double light = 5;
        light = (1.0 - Math.exp(-light * CLOUD_EXPOSURE)) * lerp(4.0, 1.0, cloudDensity);
        light = Math.clamp(light, 0.0, 1.0);

        double bodyDensity = calculateCloudDensityAtPoint(rayOriginX * 1.2, rayOriginY * 1.2, -0.86);
        double bodyMask = smoothstep(0.10, 0.34, bodyDensity);

        // double highlight = smoothstep(0.08, 0.92, light);
        // double alpha = Math.clamp(highlight * 0.25 + bodyMask * 0.55, 0.0, 0.96);

        // double core = Math.clamp(light * 0.86 + bodyMask * 0.24, 0.0, 1.0);
        // double r = Math.clamp(core * 0.76 + highlight * 0.18, 0.0, 1.0);
        // double g = Math.clamp(core * 0.88 + highlight * 0.14, 0.0, 1.0);
        // double b = Math.clamp(core * 1.06 + highlight * 0.24, 0.0, 1.0);

        // 直接从 CloudDensityMap 取一份“底图”，不要只把它当噪声源。
        double baseU = u * 0.5 + horizontalOffset;
        double baseV = v + verticalOffset * 0.2;
        double baseTex = sampleChannel(cloudPixels, cloudWidth, cloudHeight, baseU, baseV, 0);

        // 云层主体：以底图为主，bodyMask 只做补强。
        double cloudBase = Math.clamp(baseTex * 0.92 + bodyMask * 0.18, 0.0, 1.0);

        // 原版接近的暗蓝灰 tint。
        double tintR = 48.0 / 255.0;
        double tintG = 57.0 / 255.0;
        double tintB = 70.0 / 255.0;

        // 散射只负责轻微提亮，不再主导颜色。
        double brightness = 0.62 + light * 0.28;

        double highlight = smoothstep(0.18, 0.72, light) * bodyMask;

        double baseR = cloudBase * tintR * brightness;
        double baseG = cloudBase * tintG * brightness;
        double baseB = cloudBase * tintB * brightness;

        double r = Math.clamp(baseR + highlight * 0.22, 0.0, 1.0);
        double g = Math.clamp(baseG + highlight * 0.24, 0.0, 1.0);
        double b = Math.clamp(baseB + highlight * 0.28, 0.0, 1.0);

        // alpha 跟云底走，不跟 highlight 走。
        double alpha = Math.clamp(cloudBase * 0.86 + highlight * 0.12, 0.0, 0.95);

        return toArgb(alpha, r, g, b);
    }

    /**
     * 计算散射光，根据云密度和太阳位置计算散射光强度。
     *
     * @param rayOriginX 射线原点 X 坐标
     * @param rayOriginY 射线原点 Y 坐标
     * @return 散射光强度
     */
    private double calculateScatteredLight(double rayOriginX, double rayOriginY) {
        double light = calculateCloudDensityAtPoint(rayOriginX * 2.0, rayOriginY * 2.0, -2.0)
                + calculateCloudDensityAtPoint(rayOriginX * 1.72, rayOriginY * 1.72, -1.72);

        double toSunX = SUN_X - rayOriginX;
        double toSunY = SUN_Y - rayOriginY;
        double toSunZ = SUN_Z + 1.0;
        double toSunLength = Math.sqrt(toSunX * toSunX + toSunY * toSunY + toSunZ * toSunZ);
        double cosTheta = toSunZ / Math.max(toSunLength, 1.0e-6);

        double g = 0.67;
        double gSquared = g * g;
        double cosThetaSquared = cosTheta * cosTheta;
        double denominator = Math.pow(1.0 + gSquared - cosTheta * g * 2.0, 1.5) * (gSquared + 2.0);
        double phaseMie = ((1.0 - gSquared) * (cosThetaSquared + 1.0)) / Math.max(denominator, 1.0e-6) * 0.1193662;
        return light * phaseMie * 560.0;
    }

    private double calculateCloudDensityAtPoint(double u, double v, double z) {
        double scaledU = u * (1.0 - z);
        double scaledV = v * (1.0 - z);

        scaledU = scaledU * 0.5 + horizontalOffset;
        scaledV += verticalOffset;

        double offsetU = scaledU * 0.9 + cloudTime * 0.07;
        double offsetV = scaledV * 0.9 + cloudTime * 0.05;
        double uvOffsetX = sampleChannel(cloudPixels, cloudWidth, cloudHeight, offsetU, offsetV, 0) * 0.025;
        double uvOffsetY = sampleChannel(cloudPixels, cloudWidth, cloudHeight, offsetU, offsetV, 1) * 0.025;

        double densityU = scaledU * 0.2 + uvOffsetX;
        double densityV = scaledV * 0.2 + uvOffsetY;
        double densityA = sampleChannel(cloudPixels, cloudWidth, cloudHeight, densityU, densityV, 0);
        double densityB = sampleChannel(cloudPixels, cloudWidth, cloudHeight, densityU, densityV, 2);
        double density = lerp(densityA, densityB, Math.sin(z * TAU) * 0.5 + 0.5);
        return density * cloudDensity * lerp(0.15, 0.9, cloudDensity);
    }

    private static double sampleChannel(int[] pixels, int w, int h, double u, double v, int channel) {
        int argb = sampleArray(pixels, w, h, u, v);
        return switch (channel) {
            case 0 -> ((argb >> 16) & 0xFF) / 255.0;
            case 1 -> ((argb >> 8) & 0xFF) / 255.0;
            case 2 -> (argb & 0xFF) / 255.0;
            default -> ((argb >> 24) & 0xFF) / 255.0;
        };
    }

    private static int sampleArray(int[] pixels, int w, int h, double u, double v) {
        double wrappedU = u - Math.floor(u);
        double wrappedV = v - Math.floor(v);
        int x = Math.min((int) (wrappedU * w), w - 1);
        int y = Math.min((int) (wrappedV * h), h - 1);
        return pixels[y * w + x];
    }

    private static int toArgb(double a, double r, double g, double b) {
        int alpha = (int) (Math.clamp(a, 0.0, 1.0) * 255.0);
        int red = (int) (Math.clamp(r, 0.0, 1.0) * 255.0);
        int green = (int) (Math.clamp(g, 0.0, 1.0) * 255.0);
        int blue = (int) (Math.clamp(b, 0.0, 1.0) * 255.0);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * Math.clamp(t, 0.0, 1.0);
    }

    /**
     * 平滑过渡函数，用于在两个边缘之间平滑过渡。
     *
     * @param edge0 第一个边缘值
     * @param edge1 第二个边缘值
     * @param x     输入值
     * @return 平滑过渡值
     */
    private static double smoothstep(double edge0, double edge1, double x) {
        double t = Math.clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }
}

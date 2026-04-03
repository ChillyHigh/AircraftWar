package edu.hitsz.shaderCPU;

public class PrimitiveBloomShaderCPU extends ShaderCPU {
    private double innerGlowIntensity;
    private double opacity;

    public PrimitiveBloomShaderCPU(int width, int height) {
        super(width, height);
    }

    public void renderFrame(double innerGlowIntensity, double opacity) {
        this.innerGlowIntensity = innerGlowIntensity;
        this.opacity = opacity;
        render();
    }

    @Override
    protected int shade(int x, int y, double u, double v) {
        double distanceFromCenter = Math.abs(v - 0.5);
        double glow = innerGlowIntensity / Math.max(distanceFromCenter, 0.0008);
        double mask = Math.pow(quadraticBump(v), 1.6);

        double r = saturate(1.0 * glow) * opacity;
        double g = saturate(0.27 * glow) * opacity;
        double b = saturate(0.0 * glow) * opacity;
        double a = saturate(glow) * mask * opacity;
        return toArgb(a, r, g, b);
    }

    private static double quadraticBump(double x) {
        return x * (4.0 - x * 4.0);
    }

}

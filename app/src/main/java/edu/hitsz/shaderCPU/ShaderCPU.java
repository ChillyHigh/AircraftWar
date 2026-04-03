package edu.hitsz.shaderCPU;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.stream.IntStream;

public abstract class ShaderCPU {
    protected final int width;
    protected final int height;
    private final int[] pixelBuffer;
    private final WritableImage outputImage;
    private final PixelWriter pixelWriter;

    public ShaderCPU(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixelBuffer = new int[width * height];
        this.outputImage = new WritableImage(width, height);
        this.pixelWriter = outputImage.getPixelWriter();
    }

    /**
     * 子类必须实现的抽象方法，用于计算单个像素的颜色。
     *
     * @param x 屏幕像素 X 坐标
     * @param y 屏幕像素 Y 坐标
     * @param u 归一化的 U 坐标 (0.0 ~ 1.0)
     * @param v 归一化的 V 坐标 (0.0 ~ 1.0)
     * @return 32位的 ARGB 颜色整数
     */
    protected abstract int shade(int x, int y, double u, double v);

    /**
     * 渲染管线：由父类统一调度多线程
     */
    public final void render() {
        // 使用 Java 并行流，使用 CPU 多核心
        IntStream.range(0, height).parallel().forEach(y -> {
            int rowOffset = y * width;
            double v = (double) y / height;

            for (int x = 0; x < width; x++) {
                double u = (double) x / width;

                // 调用子类具体实现的着色逻辑
                pixelBuffer[rowOffset + x] = shade(x, y, u, v);
            }
        });

        // 一次性批量写入 JavaFX 图像，避免零散调用 API
        pixelWriter.setPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(),
                pixelBuffer, 0, width);
    }

    /**
     * 获取渲染完成后的图像，供 GraphicsContext.drawImage 使用
     */
    public final WritableImage getOutputImage() {
        return outputImage;
    }

    protected static double sampleChannel(int[] pixels, int w, int h, double u, double v, int channel) {
        int argb = sampleArray(pixels, w, h, u, v);
        return switch (channel) {
            case 0 -> ((argb >> 16) & 0xFF) / 255.0;
            case 1 -> ((argb >> 8) & 0xFF) / 255.0;
            case 2 -> (argb & 0xFF) / 255.0;
            default -> ((argb >> 24) & 0xFF) / 255.0;
        };
    }

    protected static int sampleArray(int[] pixels, int w, int h, double u, double v) {
        double wrappedU = u - Math.floor(u);
        double wrappedV = v - Math.floor(v);
        int x = Math.min((int) (wrappedU * w), w - 1);
        int y = Math.min((int) (wrappedV * h), h - 1);
        return pixels[y * w + x];
    }

    protected static int toArgb(double a, double r, double g, double b) {
        int alpha = (int) (saturate(a) * 255.0);
        int red = (int) (saturate(r) * 255.0);
        int green = (int) (saturate(g) * 255.0);
        int blue = (int) (saturate(b) * 255.0);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    protected static double step(double edge, double x) {
        return x >= edge ? 1.0 : 0.0;
    }

    protected static double saturate(double value) {
        return Math.clamp(value, 0.0, 1.0);
    }

    /**
     * 平滑过渡函数，用于在两个边缘之间平滑过渡。
     *
     * @param edge0 第一个边缘值
     * @param edge1 第二个边缘值
     * @param x     输入值
     * @return 平滑过渡值
     */
    protected static double smoothstep(double edge0, double edge1, double x) {
        double t = Math.clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }
}

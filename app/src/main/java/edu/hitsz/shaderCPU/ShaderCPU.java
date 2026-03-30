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
}

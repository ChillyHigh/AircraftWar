package edu.hitsz.application;

import edu.hitsz.shaderCPU.ExoMechCloudShader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;

public final class RenderBackground {
    private static final int RENDER_SCALE = 1;
    private static final int CACHE_WIDTH = Main.WINDOW_WIDTH / RENDER_SCALE;
    private static final int CACHE_HEIGHT = Main.WINDOW_HEIGHT / RENDER_SCALE;
    private static final ExoMechCloudShader CLOUD_SHADER = new ExoMechCloudShader(CACHE_WIDTH, CACHE_HEIGHT);

    private static boolean initialized = false;
    private static int backGroundTop = 0;
    private static int frameCounter = 0;
    private static double opacity = 1.0;

    public static void renderBackground(GraphicsContext gc) {
        initializeIfNeeded();

        backGroundTop = (backGroundTop + 1) % Main.WINDOW_HEIGHT;
        opacity = Math.min(1.0, opacity + 0.0035);

        frameCounter++;
        if ((frameCounter & 1) == 0) {
            CLOUD_SHADER.renderFrame();
        }
//        gc.setFill(Color.rgb(48, 57, 70, 0.3));
//        gc.fillRect(0, 0, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);

        gc.save();
        gc.setGlobalAlpha(opacity);
        gc.setEffect(new GaussianBlur(8));
        gc.drawImage(CLOUD_SHADER.getOutputImage(), 0, 0, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        gc.restore();

        gc.save();
        gc.setFill(Color.rgb(24, 30, 44, 0.24 * opacity));
        gc.fillRect(0, 0, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        gc.restore();
    }

    public static void resetFadeIn() {
        initializeIfNeeded();
        opacity = 0.0;
        frameCounter = 0;
    }

    public static boolean isFullyOpaque() {
        return opacity >= 0.999;
    }

    private static void initializeIfNeeded() {
        if (initialized) {
            return;
        }
        CLOUD_SHADER.renderFrame();
        initialized = true;
    }
}

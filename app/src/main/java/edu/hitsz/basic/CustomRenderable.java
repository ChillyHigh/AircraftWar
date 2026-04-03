package edu.hitsz.basic;

import javafx.scene.canvas.GraphicsContext;

//TODO: 整合到AbstractFlyingObject
public interface CustomRenderable {
    void render(GraphicsContext gc);

    default void renderOverlay(GraphicsContext gc) {
        // 默认没有额外覆盖层
    }
}

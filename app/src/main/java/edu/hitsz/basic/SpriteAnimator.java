package edu.hitsz.basic;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.application.ImageManager;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public final class SpriteAnimator {
    private final List<Image> frames;
    private final int frameWidth;
    private final int frameHeight;
    private int currentFrame;
    private int frameCounter;

    private final int framesX;
    private final int framesY;

    // 每隔多少次 update 切换到下一帧
    private final int frameInterval;

    // index 表示当前正在播放的动画组
    private int index;

    public SpriteAnimator(
            String name,
            int frameWidth,
            int frameHeight,
            int framesX,
            int framesY,
            int frameInterval,
            int index) {
        Image spriteSheet = ImageManager.getSpriteSheet(name);
        if (spriteSheet == null) {
            throw new IllegalArgumentException("未知精灵表: " + name);
        }

        // 精灵表是一个包含 framesX * framesY 个帧的网格，每个帧的尺寸为 frameWidth * frameHeight
        PixelReader reader = spriteSheet.getPixelReader();
        if (reader == null) {
            throw new IllegalStateException("Cannot read pixels from sprite sheet: " + name);
        }
        // 实际的图片尺寸
        int actualWidth = (int) Math.round(spriteSheet.getWidth());
        int actualHeight = (int) Math.round(spriteSheet.getHeight());
        List<Image> frames = new ArrayList<>(framesX * framesY);
        for (int x = 0; x < framesX; x++) {
            for (int y = 0; y < framesY; y++) {
                frames.add(extractFrame(
                        reader,
                        x * frameWidth,
                        y * frameHeight,
                        frameWidth,
                        frameHeight,
                        actualWidth,
                        actualHeight));
            }
        }
        this.frames = frames;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.framesX = framesX;
        this.framesY = framesY;
        this.frameInterval = frameInterval;
        setIndex(index);
    }

    public void update() {
        frameCounter++;
        if (frameCounter >= frameInterval) {
            frameCounter = 0;
            currentFrame = (currentFrame + 1) % framesY + index * framesY;
        }
    }

    public Image getCurrentImage() {
        return frames.get(currentFrame);
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setIndex(int index) {
        if (index < 0 || index >= framesX) {
            throw new IllegalArgumentException("动画索引无效: " + index);
        }
        if (this.index != index) {
            this.currentFrame = index * framesY;
            this.frameCounter = 0;
        }
        this.index = index;
    }

    /**
     * 从精灵表中提取单帧图像
     *
     * @param reader       精灵表的像素读取器
     * @param sourceX      帧在精灵表中的起始 x 坐标
     * @param sourceY      帧在精灵表中的起始 y 坐标
     * @param frameWidth   帧的宽度
     * @param frameHeight  帧的高度
     * @param actualWidth  精灵表的实际宽度
     * @param actualHeight 精灵表的实际高度
     * @return Image 从精灵表中提取的帧图像
     */
    private static Image extractFrame(
            PixelReader reader,
            int sourceX,
            int sourceY,
            int frameWidth,
            int frameHeight,
            int actualWidth,
            int actualHeight) {
        if (sourceX + frameWidth <= actualWidth && sourceY + frameHeight <= actualHeight) {
            return new WritableImage(reader, sourceX, sourceY, frameWidth, frameHeight);
        }

        WritableImage frame = new WritableImage(frameWidth, frameHeight);
        int copyWidth = Math.clamp(actualWidth - sourceX, 0, frameWidth);
        int copyHeight = Math.clamp(actualHeight - sourceY, 0, frameHeight);
        if (copyWidth > 0 && copyHeight > 0) {
            frame.getPixelWriter().setPixels(0, 0, copyWidth, copyHeight, reader, sourceX, sourceY);
        }
        return frame;
    }
}

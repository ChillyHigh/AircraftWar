package edu.hitsz.application;

import java.util.Random;

public class WindowShaker {

    private static final Random SHAKE_RANDOM = new Random();
    private static double shakeStrength = 0.0;
    private static double shakeDecay = 0.88;
    private static int shakeFramesLeft = 0;
    private static double shakeOffsetX = 0.0;
    private static double shakeOffsetY = 0.0;

    public static void startScreenShake(double strength, double decay, int frames) {
        shakeStrength = Math.max(shakeStrength, strength);
        shakeDecay = decay;
        shakeFramesLeft = Math.max(shakeFramesLeft, frames);
    }

    public static void updateScreenShake() {
        if (shakeFramesLeft <= 0 || shakeStrength < 0.05) {
            shakeOffsetX = 0.0;
            shakeOffsetY = 0.0;
            return;
        }
        shakeOffsetX = (SHAKE_RANDOM.nextDouble() * 2.0 - 1.0) * shakeStrength;
        shakeOffsetY = (SHAKE_RANDOM.nextDouble() * 2.0 - 1.0) * shakeStrength;
        shakeStrength *= shakeDecay;
        shakeFramesLeft--;
    }

    public static double getShakeOffsetX() {
        return shakeOffsetX;
    }

    public static double getShakeOffsetY() {
        return shakeOffsetY;
    }
}

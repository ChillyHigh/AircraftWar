package edu.hitsz.application;

import edu.hitsz.thread.MusicThread;

/**
 * 音频管理器，负责游戏中所有音效和背景音乐的播放
 */
public class AudioManager {
    private static final Object BGM_LOCK = new Object();
    private static MusicThread bgmThread;
    private static String currentBgmPath;

    public static final String BGM = "/videos/bgm.wav";
    public static final String BGM_BOSS = "/videos/bgm_boss.wav";
    public static final String BULLET_HIT = "/videos/bullet_hit.wav";
    public static final String BOMB_EXPLOSION = "/videos/bomb_explosion.wav";
    public static final String GET_SUPPLY = "/videos/get_supply.wav";
    public static final String GAME_OVER = "/videos/game_over.wav";
    public static final String EXO_TWINS_DASH_TELEGRAPH = "/extraSounds/ArtemisApolloDashTelegraph.wav";
    public static final String EXO_TWINS_DASH = "/extraSounds/ArtemisApolloDash.wav";
    public static final String EXO_LASER_SHOOT = "/extraSounds/ExoLaserShoot.wav";
    public static final String ARTEMIS_SPIN_LASERBEAM = "/extraSounds/ArtemisSpinLaserbeam.wav";
    public static final String EXO_TWINS_BGM = "/extraSounds/ExoTwinsBoss.wav";
    public static final String EXO_TWINS_WARNING_BEEP_1 = "/extraSounds/WarningBeep1.wav";
    public static final String EXO_TWINS_WARNING_BEEP_2 = "/extraSounds/WarningBeep2.wav";
    public static final String EXO_TWINS_DEATH_COLLISION = "/extraSounds/DeathAnimationCollision.wav";

    /**
     * 播放背景音乐，循环播放<br>
     * 如果已经有正在播放的，淡入淡出切换到新的背景音乐
     *
     * @param bgmPath 背景音乐文件路径
     */
    public static void playBGM(String bgmPath) {
        synchronized (BGM_LOCK) {
            if (bgmPath != null && bgmPath.equals(currentBgmPath) && bgmThread != null && bgmThread.isAlive()) {
                return;
            }

            stopCurrentBgmLocked();
            MusicThread nextThread = new MusicThread(bgmPath, true);
            bgmThread = nextThread;
            currentBgmPath = bgmPath;
            nextThread.start();
        }
    }

    /**
     * 停止背景音乐
     */
    public static void stopBGM() {
        synchronized (BGM_LOCK) {
            stopCurrentBgmLocked();
            bgmThread = null;
            currentBgmPath = null;
        }
    }

    public static void playSound(String sfxPath) {
        MusicThread sfxThread = new MusicThread(sfxPath);
        sfxThread.start();
    }

    private static void stopCurrentBgmLocked() {
        if (bgmThread == null) {
            return;
        }
        bgmThread.stopMusic();
        try {
            bgmThread.join(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

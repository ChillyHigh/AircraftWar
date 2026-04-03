package edu.hitsz.application;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.net.URL;

/**
 * 音频管理器，负责游戏中所有音效和背景音乐的播放
 */
public class AudioManager {
    /**
     * 音效缓存，存储已加载的音效，避免重复加载同一音效文件
     */
    private static Map<String, AudioClip> sfxCache = new ConcurrentHashMap<>();

    /**
     * 媒体播放器，播放长的背景音乐
     */
    private static MediaPlayer bgmPlayer;

    public static final String BGM = "/videos/bgm.wav";
    public static final String BGM_BOSS = "/videos/bgm_boss.wav";
    public static final String BULLET_HIT = "/videos/bullet_hit.wav";
    public static final String BOMB_EXPLOSION = "/videos/bomb_explosion.wav";
    public static final String GET_SUPPLY = "/videos/get_supply.wav";
    public static final String GAME_OVER = "/videos/game_over.wav";
    public static final String LASER_CANNON = "/extraSounds/Splatshot.wav";
    public static final String EXO_TWINS_DASH_TELEGRAPH = "/extraSounds/ArtemisApolloDashTelegraph.wav";
    public static final String EXO_TWINS_DASH = "/extraSounds/ArtemisApolloDash.wav";
    public static final String EXO_LASER_SHOOT = "/extraSounds/ExoLaserShoot.wav";
    public static final String EXO_PLASMA_SHOOT = "/extraSounds/ExoPlasmaShoot.wav";
    public static final String ARTEMIS_SPIN_LASERBEAM = "/extraSounds/ArtemisSpinLaserbeam.wav";
    public static final String GATLING_LASER_FIRE_START = "/extraSounds/GatlingLaserFireStart.wav";
    public static final String GATLING_LASER_FIRE_LOOP = "/extraSounds/GatlingLaserFireLoop.wav";

    /**
     * 播放背景音乐，循环播放<br>
     * 如果已经有正在播放的，淡入淡出切换到新的背景音乐
     *
     * @param bgmPath 背景音乐文件路径
     */
    public static void playBGM(String bgmPath) {
        // 停止当前背景音乐
        stopBGM();

        // 加载新的背景音乐
        Media media = new Media(resolveResourceUrl(bgmPath));
        bgmPlayer = new MediaPlayer(media);
        bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE); // 循环播放
        bgmPlayer.play();
    }

    /**
     * 停止背景音乐
     */
    public static void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
    }

    public static void playSound(String sfxPath) {
        AudioClip clip = sfxCache.computeIfAbsent(sfxPath, path -> new AudioClip(resolveResourceUrl(path)));
        if (clip != null) {
            clip.play();
        }
    }

    /**
     * 解析url路径，返回JavaFX可用的资源路径
     */
    private static String resolveResourceUrl(String path) {
        URL url = AudioManager.class.getResource(path);
        if (url == null) {
            throw new IllegalArgumentException("未找到音频资源: " + path);
        }
        return url.toExternalForm();
    }
}

package edu.hitsz.application;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.enemy.*;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.prop.*;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 综合管理图片的加载，访问
 * 提供图片的静态访问方法
 *
 * @author hitsz
 */
public class ImageManager {

    /**
     * 类名-图片 映射，存储各基类的图片
     * 可使用 CLASSNAME_IMAGE_MAP.get( obj.getClass().getName() ) 获得 obj 所属基类对应的图片
     */
    private static final Map<String, Image> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static Image BACKGROUND_IMAGE;
    public static Image BACKGROUND_IMAGE_2;
    public static Image BACKGROUND_IMAGE_3;
    public static Image BACKGROUND_IMAGE_4;
    public static Image BACKGROUND_IMAGE_5;


    public static Image HERO_IMAGE;
    public static Image HERO_BULLET_IMAGE;
    public static Image ENEMY_BULLET_IMAGE;
    public static Image MOB_ENEMY_IMAGE;
    public static Image ELITE_ENEMY_IMAGE;
    public static Image ELITE_PLUS_ENEMY_IMAGE;
    public static Image ELITE_PRO_ENEMY_IMAGE;
    public static Image BOSS_ENEMY_IMAGE;

    public static Image APOLLO_IMAGE;
    public static Image ARTEMIS_IMAGE;
    public static Image APOLLP_GLOW_IMAGE;
    public static Image ARTEMIS_GLOW_IMAGE;
    public static Image ARTEMIS_LASER_IMAGE;
    public static Image APOLLO_FIREBALL_IMAGE;
    public static Image EXO_MUZZLE_BLOOM_IMAGE;
    public static Image EXO_MUZZLE_FLARE_IMAGE;

    public static Image PROP_BLOOD_IMAGE;
    public static Image PROP_BOMB_IMAGE;
    public static Image PROP_BULLET_IMAGE;
    public static Image PROP_BULLET_PLUS_IMAGE;
    public static Image PROP_FREEZE_IMAGE;

    public static Image CLOUD_DENSITY_MAP;
    public static Image TECHY_NOISE;
    public static Image WAVY_BLOTCH_NOISE;

    static {
        try {

            BACKGROUND_IMAGE = loadFromResources("/images/bg.jpg");
            BACKGROUND_IMAGE_2 = loadFromResources("/images/bg2.jpg");
            BACKGROUND_IMAGE_3 = loadFromResources("/images/bg3.jpg");
            BACKGROUND_IMAGE_4 = loadFromResources("/images/bg4.jpg");
            BACKGROUND_IMAGE_5 = loadFromResources("/images/bg5.jpg");

            HERO_IMAGE = loadFromResources("/images/hero.png");
            MOB_ENEMY_IMAGE = loadFromResources("/images/mob.png");
            HERO_BULLET_IMAGE = loadFromResources("/images/bullet_hero.png");
            ENEMY_BULLET_IMAGE = loadFromResources("/images/bullet_enemy.png");
            ELITE_ENEMY_IMAGE = loadFromResources("/images/elite.png");
            ELITE_PLUS_ENEMY_IMAGE = loadFromResources("/images/elitePlus.png");
            ELITE_PRO_ENEMY_IMAGE = loadFromResources("/images/elitePro.png");
            BOSS_ENEMY_IMAGE = loadFromResources("/images/boss.png");

            PROP_BLOOD_IMAGE = loadFromResources("/images/prop_blood.png");
            PROP_BOMB_IMAGE = loadFromResources("/images/prop_bomb.png");
            PROP_BULLET_IMAGE = loadFromResources("/images/prop_bullet.png");
            PROP_BULLET_PLUS_IMAGE = loadFromResources("/images/prop_bulletPlus.png");
            PROP_FREEZE_IMAGE = loadFromResources("/images/prop_freeze.png");

            APOLLO_IMAGE = loadFromResources("/images/Apollo.png");
            APOLLP_GLOW_IMAGE = loadFromResources("/images/ApolloGlow.png");
            ARTEMIS_IMAGE = loadFromResources("/images/Artemis.png");
            ARTEMIS_GLOW_IMAGE = loadFromResources("/images/ArtemisGlow.png");
            ARTEMIS_LASER_IMAGE = loadFromResources("/images/ArtemisLaser.png");
            APOLLO_FIREBALL_IMAGE = loadFromResources("/images/ApolloFireball.png");
            EXO_MUZZLE_BLOOM_IMAGE = loadFromResources("/images/ExoMuzzleBloom.png");
            EXO_MUZZLE_FLARE_IMAGE = loadFromResources("/images/ExoMuzzleFlare.png");

            CLOUD_DENSITY_MAP = loadFromResources("/textures/CloudDensityMap.png");
            TECHY_NOISE = loadFromResources("/textures/TechyNoise.png");
            WAVY_BLOTCH_NOISE = loadFromResources("/textures/WavyBlotchNoise.png");

            CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
            CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
            CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
            CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(ElitePlusEnemy.class.getName(), ELITE_PLUS_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(EliteProEnemy.class.getName(), ELITE_PRO_ENEMY_IMAGE);
            CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);

            CLASSNAME_IMAGE_MAP.put(BloodProp.class.getName(), PROP_BLOOD_IMAGE);
            CLASSNAME_IMAGE_MAP.put(BombProp.class.getName(), PROP_BOMB_IMAGE);
            CLASSNAME_IMAGE_MAP.put(BulletProp.class.getName(), PROP_BULLET_IMAGE);
            CLASSNAME_IMAGE_MAP.put(BulletPlusProp.class.getName(), PROP_BULLET_PLUS_IMAGE);
            CLASSNAME_IMAGE_MAP.put(FreezeProp.class.getName(), PROP_FREEZE_IMAGE);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static Image get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Image get(Object obj) {
        if (obj == null) {
            return null;
        }
        return get(obj.getClass().getName());
    }

    public static Image getSpriteSheet(String name) {
        if (name == null) {
            return null;
        }
        return switch (name.toLowerCase()) {
            case "apollo" -> APOLLO_IMAGE;
            case "artemis" -> ARTEMIS_IMAGE;
            case "apollo_glow" -> APOLLP_GLOW_IMAGE;
            case "artemis_glow" -> ARTEMIS_GLOW_IMAGE;
            case "artemis_laser" -> ARTEMIS_LASER_IMAGE;
            case "apollo_fireball" -> APOLLO_FIREBALL_IMAGE;
            case "exo_muzzle_bloom" -> EXO_MUZZLE_BLOOM_IMAGE;
            case "exo_muzzle_flare" -> EXO_MUZZLE_FLARE_IMAGE;
            default -> null;
        };
    }

    private static Image loadFromResources(String resourcePath) throws IOException {
        try (InputStream input = ImageManager.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new Image(input);
        }
    }
}

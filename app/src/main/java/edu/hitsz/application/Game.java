package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.aircraft.enemy.EliteEnemy;
import edu.hitsz.aircraft.enemy.ElitePlusEnemy;
import edu.hitsz.aircraft.enemy.EliteProEnemy;
import edu.hitsz.aircraft.enemy.BossEnemy;

import edu.hitsz.aircraft.enemy.MobEnemy;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.PropFactory;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.basic.AbstractFlyingObject;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;

import java.util.LinkedList;
import java.util.List;

/**
 * 游戏主面板，游戏启动
 * 
 * @author hitsz
 */
public class Game extends Pane {

    private int backGroundTop = 0;

    // 游戏逻辑更新间隔
    private int timeInterval = 40;

    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<BaseProp> props;

    // 屏幕中出现的敌机最大数量
    private final int enemyMaxNumber = 5;

    // 敌机生成周期
    protected double enemySpawnCycle = 20;
    private int enemySpawnCounter = 0;

    // 英雄机和敌机射击周期
    protected double shootCycle = 20;
    private int shootCounter = 0;

    // 当前玩家分数
    private int score = 0;

    // 游戏结束标志
    private boolean gameOverFlag = false;

    // 画布和画笔
    private final Canvas canvas;
    private final GraphicsContext gc;

    // JavaFX 提供的动画定时器
    private AnimationTimer timer;
    // 上一帧时刻(纳秒)
    private long lastNanoTime = 0L;
    // 累加器，记录两帧之间经过的实际时间
    private long nanoAccumulator = 0L;

    public Game() {
        setPrefSize(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        setPickOnBounds(true);

        canvas = new Canvas(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        heroAircraft = HeroAircraft.getHeroAircraft();

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        // 启动英雄机鼠标监听（移动/拖拽即跟随）
        new HeroController(this, heroAircraft);

    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {

        if (timer != null) {
            return;
        }

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameOverFlag) {
                    stop();
                    return;
                }
                if (lastNanoTime == 0L) {
                    lastNanoTime = now;
                    return;
                }

                // 固定时间步长
                long delta = now - lastNanoTime;
                lastNanoTime = now;
                nanoAccumulator += delta;

                long stepNanos = timeInterval * 1_000_000L;
                while (nanoAccumulator >= stepNanos && !gameOverFlag) {
                    nanoAccumulator -= stepNanos;
                    tick();
                }
                // 重绘界面
                render();
            }
        };
        timer.start();

    }

    private void tick() {
        enemySpawnCounter++;
        if (enemySpawnCounter >= enemySpawnCycle) {
            enemySpawnCounter = 0;
            // 随机产生敌机
            if (enemyAircrafts.size() < enemyMaxNumber) {
                // 生成0-4之间的随机数
                int index = (int) (Math.random() * 5);
                AbstractAircraft enemy = null;
                // TODO: 之后切换成工厂模式
                switch (index) {
                    case 0:
                        enemy = new MobEnemy(
                                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                0,
                                10,
                                30);
                        break;
                    case 1:
                        enemy = new EliteEnemy(
                                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                0,
                                5,
                                30);
                        break;
                    case 2:
                        enemy = new ElitePlusEnemy(
                                (int) (Math.random()
                                        * (Main.WINDOW_WIDTH - ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                0,
                                5,
                                30);
                        break;
                    case 3:
                        enemy = new EliteProEnemy(
                                (int) (Math.random()
                                        * (Main.WINDOW_WIDTH - ImageManager.ELITE_PRO_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                0,
                                5,
                                60);
                        break;
                    case 4:
                        enemy = new BossEnemy(
                                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.BOSS_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                0,
                                2,
                                90);
                        break;
                }
                enemyAircrafts.add(enemy);
            }
        }
        // 飞机射出子弹
        shootAction();
        // 子弹移动
        bulletsMoveAction();
        // 飞机移动
        aircraftMoveAction();
        // 撞击检测
        crashCheckAction();
        // 后处理
        postProcessAction();
        // 游戏结束检查
        checkResultAction();
    }

    // ***********************
    // Action 各部分
    // ***********************
    private void shootAction() {
        shootCounter++;
        if (shootCounter >= shootCycle) {
            shootCounter = 0;
            // 英雄机射击
            AudioManager.playSound(AudioManager.LASER_CANNON);
            heroBullets.addAll(heroAircraft.shoot());
            // 敌机射击
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        // 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                // 英雄机撞击到敌机子弹
                // 英雄机损失一定生命值
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已被其他子弹击毁的敌机，不再检测
                    // 避免多个子弹重复击毁同一敌机的判定
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    // 敌机撞击到英雄机子弹
                    // 敌机损失一定生命值
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        // TODO 获得分数，产生道具补给
                        BaseProp prop = PropFactory.creatProp("Blood",
                                enemyAircraft.getLocationX(),
                                enemyAircraft.getLocationY(), 0, 2);
                        score += 10;
                        props.add(prop);

                    }
                }
                // 英雄机 与 敌机 相撞，均损毁
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // TODO: 我方获得道具，道具生效
        for (BaseProp prop : props) {
            if (prop.notValid()) {
                continue;
            }
            if (heroAircraft.crash(prop)) {
                // 英雄机撞击到道具
                // 道具生效
                AudioManager.playSound(AudioManager.GET_SUPPLY);
                prop.activate(heroAircraft);
                prop.vanish();
            }
        }

    }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
     * 3. 删除无效的道具
     */
    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        // TODO: 删除无效道具
        props.removeIf(AbstractFlyingObject::notValid);
    }

    /**
     * 检查游戏是否结束，若结束：关闭线程池
     */
    private void checkResultAction() {
        // 游戏结束检查英雄机是否存活
        if (heroAircraft.getHp() <= 0) {
            gameOverFlag = true;
            System.out.println("Game Over!");
        }
    };

    // ***********************
    // Render 各部分
    // ***********************

    private void render() {
        // 背景滚动
        gc.clearRect(0, 0, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);

        gc.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT);
        gc.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        paintImageWithPositionRevised(enemyBullets);
        paintImageWithPositionRevised(heroBullets);
        paintImageWithPositionRevised(enemyAircrafts);

        // TODO: 绘制道具
        paintImageWithPositionRevised(props);
        // 绘制英雄机
        gc.drawImage(
                ImageManager.HERO_IMAGE,
                heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2.0,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2.0);

        paintScoreAndLife();
    }

    private void paintImageWithPositionRevised(List<? extends AbstractFlyingObject> objects) {
        if (objects.isEmpty()) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            Image image = object.getImage();
            if (image == null) {
                throw new IllegalStateException(
                        objects.getClass().getName() + " has object without image: " + object.getClass().getName());
            }
            gc.drawImage(
                    image,
                    object.getLocationX() - image.getWidth() / 2.0,
                    object.getLocationY() - image.getHeight() / 2.0);
        }
    }

    private void paintScoreAndLife() {
        int x = 10;
        int y = 25;
        gc.setFill(Color.rgb(255, 0, 0));
        gc.setFont(Font.font("SansSerif", FontWeight.BOLD, 22));
        gc.fillText("SCORE:" + this.score, x, y);
        y = y + 20;
        gc.fillText("LIFE:" + this.heroAircraft.getHp(), x, y);
    }

}

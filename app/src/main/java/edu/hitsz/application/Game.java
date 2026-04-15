package edu.hitsz.application;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.*;
import edu.hitsz.prop.BaseProp;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
    private int timeInterval = 20;

    private final HeroAircraft heroAircraft;
    private final List<AbstractEnemy> enemyAircrafts;
    private final List<EnemyFactory> enemyFactories;
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

    // 召唤BOSS的分数间隔
    private final int BOSS_SCORE_INTERVAL = 1000;

    // 上次召唤BOSS时的分数
    private int lastBossScore = 0;

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
        enemyFactories = List.of(
                new MobEnemyFactory(),
                new EliteEnemyFactory(),
                new ElitePlusEnemyFactory(),
                new EliteProEnemyFactory()/*,
                new BossEnemyFactory()*/);
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
                EnemyFactory enemyFactory = enemyFactories.get((int) (Math.random() * enemyFactories.size()));
                enemyAircrafts.add(enemyFactory.createEnemy());
            }
        }
        // 召唤BOSS敌机
        if (score - lastBossScore >= BOSS_SCORE_INTERVAL) {
            enemyAircrafts.add(new BossEnemyFactory().createEnemy());
            lastBossScore = score;
        }
        // 飞机射出子弹
        shootAction();
        // 子弹移动
        bulletsMoveAction();
        // 飞机移动
        aircraftMoveAction();
        // 道具移动
        propsMoveAction();
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
//            AudioManager.playSound(AudioManager.LASER_CANNON);
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

    private void propsMoveAction() {
        for (BaseProp prop : props) {
            prop.forward();
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
            for (AbstractEnemy enemyAircraft : enemyAircrafts) {
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
                        score += enemyAircraft.getScore();
                        props.addAll(enemyAircraft.dropProp());
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
                prop.activate();
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
    }

    ;

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
        renderObjects(enemyBullets);
        renderObjects(heroBullets);
        renderObjects(enemyAircrafts);

        renderObjects(props);
        // 绘制英雄机
        heroAircraft.render(gc);

        paintScoreAndLife();
    }

    private void renderObjects(List<? extends AbstractFlyingObject> objects) {
        if (objects.isEmpty()) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            object.render(gc);
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

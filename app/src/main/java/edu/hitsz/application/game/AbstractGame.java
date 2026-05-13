package edu.hitsz.application.game;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.AbstractEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.AudioManager;
import edu.hitsz.application.HeroController;
import edu.hitsz.application.Main;
import edu.hitsz.application.WindowShaker;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.factory.BossEnemyFactory;
import edu.hitsz.factory.EliteEnemyFactory;
import edu.hitsz.factory.ElitePlusEnemyFactory;
import edu.hitsz.factory.EliteProEnemyFactory;
import edu.hitsz.factory.EnemyFactory;
import edu.hitsz.factory.MobEnemyFactory;
import edu.hitsz.fxml.rankPageCtrl;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FreezeProp;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public abstract class AbstractGame extends Pane {

    private int backGroundTop = 0;

    // 游戏逻辑更新间隔
    private final int timeInterval = 20;

    private final HeroAircraft heroAircraft;
    protected final List<AbstractEnemy> enemyAircrafts;
    protected final List<EnemyFactory> enemyFactories;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<BaseProp> props;

    private final Image backgroundImage;

    // 屏幕中出现的敌机最大数量
    protected int enemyMaxNumber = 5;

    // 敌机生成周期
    protected double enemySpawnCycle = 20;

    // 英雄机和敌机射击周期
    protected double enemyShootCycle = 20;
    private int enemyShootCounter = 0;

    protected double heroShootCycle = 20;
    private int heroShootCounter = 0;
    protected double enemyHpRate = 1.0;
    protected double enemySpeedRate = 1.0;
    protected double bossHpRate = 1.0;
    protected double eliteEnemyRate = 0.25;

    // 当前玩家分数
    protected int score = 0;

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

    public AbstractGame(Image backgroundImage) {
        setPrefSize(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        setPickOnBounds(true);

        this.backgroundImage = backgroundImage;
        canvas = new Canvas(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        heroAircraft = HeroAircraft.getHeroAircraft();

        enemyAircrafts = new LinkedList<>();
        enemyFactories = List.of(
                new MobEnemyFactory(),
                new EliteEnemyFactory(),
                new ElitePlusEnemyFactory(),
                new EliteProEnemyFactory()
        );
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        // 启动英雄机鼠标监听（移动/拖拽即跟随）
        new HeroController(this, heroAircraft);
    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public final void action() {
        if (timer != null) {
            return;
        }
        initializeDifficulty();

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
        updateDifficulty();
        spawnEnemy();
        spawnBoss();

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
    // 钩子方法
    // ***********************
    protected abstract void initializeDifficulty();

    protected abstract void updateDifficulty();

    protected abstract void spawnEnemy();

    protected abstract void spawnBoss();

    protected abstract String getDifficultyName();

    /**
     * 应用敌机工厂的倍率设置
     */
    protected void applyEnemyFactoryRates() {
        for (EnemyFactory enemyFactory : enemyFactories) {
            enemyFactory.setRates(enemyHpRate, enemySpeedRate);
        }
    }

    protected EnemyFactory chooseEnemyFactory() {
        if (Math.random() < 1 - eliteEnemyRate) {
            return enemyFactories.get(0);
        }
        return enemyFactories.get(1 + (int) (Math.random() * (enemyFactories.size() - 1)));
    }

    protected BossEnemyFactory createBossEnemyFactory() {
        BossEnemyFactory bossEnemyFactory = new BossEnemyFactory();
        bossEnemyFactory.setRates(bossHpRate, enemySpeedRate);
        return bossEnemyFactory;
    }

    protected void upgradeEnemyDifficulty(double eliteRateStep, double spawnCycleStep, double hpRateStep,
                                          double speedRateStep) {
        eliteEnemyRate = Math.min(0.95, eliteEnemyRate + eliteRateStep);
        enemySpawnCycle = Math.max(6.0, enemySpawnCycle - spawnCycleStep);
        enemyHpRate += hpRateStep;
        enemySpeedRate += speedRateStep;
        applyEnemyFactoryRates();
    }

    protected List<AbstractEnemy> getEnemyAircrafts() {
        return enemyAircrafts;
    }

    /**
     * 用于绘制背景层，子类可覆写以接入动态云层等效果
     */
    protected void renderBackgroundLayer(GraphicsContext gc) {
        gc.drawImage(backgroundImage, 0, this.backGroundTop - Main.WINDOW_HEIGHT);
        gc.drawImage(backgroundImage, 0, this.backGroundTop);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }
    }

    protected void renderForegroundLayer(GraphicsContext gc) {
    }

    // ***********************
    // Action 各部分
    // ***********************
    private void shootAction() {
        for (AbstractEnemy enemyAircraft : enemyAircrafts) {
            enemyBullets.addAll(enemyAircraft.getBullets());
        }

        enemyShootCounter++;
        if (enemyShootCounter >= enemyShootCycle) {
            enemyShootCounter = 0;
            // 敌机射击
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }
        heroShootCounter++;
        if (heroShootCounter >= heroShootCycle) {
            heroShootCounter = 0;
            // 英雄机射击
            heroBullets.addAll(heroAircraft.shoot());
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
            if (bullet.collidesWith(heroAircraft)) {
                bullet.onCollide(heroAircraft);
                heroAircraft.onCollide(bullet);
            }
        }

        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractEnemy enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    continue;
                }
                if (bullet.collidesWith(enemyAircraft)) {
                    bullet.onCollide(enemyAircraft);
                    enemyAircraft.onCollide(bullet);
                    if (enemyAircraft.notValid()) {
                        score += enemyAircraft.getScore();
                        props.addAll(enemyAircraft.dropProp());
                    }
                    if (bullet.notValid()) {
                        break;
                    }
                }
            }
        }

        // 英雄机 与 敌机 相撞
        for (AbstractEnemy enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            if (enemyAircraft.collidesWith(heroAircraft)) {
                enemyAircraft.onCollide(heroAircraft);
                heroAircraft.onCollide(enemyAircraft);
            }
        }

        // 英雄获得补给
        for (BaseProp prop : props) {
            if (prop.notValid()) {
                continue;
            }
            if (heroAircraft.collidesWith(prop)) {
                if (prop instanceof BombProp bombProp) {
                    for (AbstractEnemy enemyAircraft : enemyAircrafts) {
                        if (!enemyAircraft.notValid()) {
                            bombProp.addObserver(enemyAircraft);
                        }
                    }
                    for (BaseBullet bullet : enemyBullets) {
                        if (!bullet.notValid() && bullet instanceof EnemyBullet enemyBullet) {
                            bombProp.addObserver(enemyBullet);
                        }
                    }
                } else if (prop instanceof FreezeProp freezeProp) {
                    for (AbstractEnemy enemyAircraft : enemyAircrafts) {
                        if (!enemyAircraft.notValid()) {
                            freezeProp.addObserver(enemyAircraft);
                        }
                    }
                    for (BaseBullet bullet : enemyBullets) {
                        if (!bullet.notValid() && bullet instanceof EnemyBullet enemyBullet) {
                            freezeProp.addObserver(enemyBullet);
                        }
                    }
                }
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
        props.removeIf(AbstractFlyingObject::notValid);
    }

    /**
     * 检查游戏是否结束，若结束：关闭线程池
     */
    private void checkResultAction() {
        // 游戏结束检查英雄机是否存活
        if (heroAircraft.getHp() <= 0) {
            finishGame(true);
        }
    }

    protected void finishGame(boolean playGameOverSound) {
        if (gameOverFlag) {
            return;
        }
        gameOverFlag = true;
        System.out.println("Game Over!");
        AudioManager.stopBGM();
        if (playGameOverSound) {
            AudioManager.playSound(AudioManager.GAME_OVER);
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/rankPage.fxml"));
            Parent root = loader.load();

            rankPageCtrl controller = loader.getController();
            controller.setCurrentScore(score, getDifficultyName());

            Stage stage = (Stage) getScene().getWindow();
            stage.setScene(new Scene(root, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ***********************
    // Render 各部分
    // ***********************
    private void render() {
        gc.clearRect(0, 0, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        WindowShaker.updateScreenShake();
        gc.save();
        gc.translate(WindowShaker.getShakeOffsetX(), WindowShaker.getShakeOffsetY());

        renderBackgroundLayer(gc);
        renderObjects(enemyBullets);
        renderObjects(heroBullets);
        renderObjects(enemyAircrafts);
        renderObjects(props);
        heroAircraft.render(gc);
        renderForegroundLayer(gc);

        gc.restore();
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
        y += 20;
        gc.fillText("LIFE:" + this.heroAircraft.getHp(), x, y);
    }
}

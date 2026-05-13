package edu.hitsz.application.game;

import edu.hitsz.aircraft.enemy.ExoTwinsManager;
import edu.hitsz.aircraft.enemy.exotwins.ApolloEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ArtemisEnemy;
import edu.hitsz.application.AudioManager;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.application.RenderBackground;
import javafx.scene.canvas.GraphicsContext;

public class InfernumGame extends AbstractGame {

    private static final int EXO_TWINS_SPAWN_SCORE = 2000;
    private static final int EXO_TWINS_HP = 7000;
    private static final int EXPLOSION_TIME = 75;
    private static final double EXPLOSION_FLASH_DECAY = 0.90;

    private int enemySpawnCounter;
    private int difficultyCounter;
    private int postExplosionTimer = -1;
    private boolean exoTwinsSpawned = false;
    private boolean exoTwinsIntroStarted = false;
    private boolean infernumFinished = false;
    private ExoTwinsManager exoTwinsManager;
    private double explosionFlashAlpha = 0.0;

    public InfernumGame() {
        super(ImageManager.BACKGROUND_IMAGE_4);
    }

    @Override
    protected void initializeDifficulty() {
        enemyMaxNumber = 14;
        enemySpawnCycle = 16;
        enemyShootCycle = 16;
        heroShootCycle = 18;
        eliteEnemyRate = 0.26;
        enemyHpRate = 1.05;
        enemySpeedRate = 1.08;
        bossHpRate = 1.0;
        applyEnemyFactoryRates();
    }

    @Override
    protected void updateDifficulty() {
        difficultyCounter++;
        if (difficultyCounter >= 900) {
            difficultyCounter = 0;
            upgradeEnemyDifficulty(0.015, 0.2, 0.03, 0.03);
            if (enemyShootCycle > 10) {
                enemyShootCycle -= 0.3;
            }
        }
        if (explosionFlashAlpha > 0.001) {
            explosionFlashAlpha *= EXPLOSION_FLASH_DECAY;
        } else {
            explosionFlashAlpha = 0.0;
        }

        if (exoTwinsManager != null && !exoTwinsManager.isBattleFinished()) {
            exoTwinsManager.update();
            if (exoTwinsManager.isDeathExplosionTriggered() && postExplosionTimer < 0) {
                postExplosionTimer = EXPLOSION_TIME;
                explosionFlashAlpha = 0.92;
            }
            if (postExplosionTimer >= 0) {
                postExplosionTimer--;
                if (postExplosionTimer < 0 && !infernumFinished) {
                    infernumFinished = true;
                    exoTwinsManager.finishBattle();
                    finishGame(false);
                }
            }
        }
    }

    @Override
    protected void spawnEnemy() {
        if (exoTwinsIntroStarted || (exoTwinsManager != null && !exoTwinsManager.isBattleFinished())) {
            return;
        }
        enemySpawnCounter++;
        if (enemySpawnCounter >= enemySpawnCycle) {
            enemySpawnCounter = 0;
            if (enemyAircrafts.size() < enemyMaxNumber) {
                enemyAircrafts.add(chooseEnemyFactory().createEnemy());
            }
        }
    }

    @Override
    protected void spawnBoss() {
        if (!exoTwinsIntroStarted && !exoTwinsSpawned && score >= EXO_TWINS_SPAWN_SCORE) {
            exoTwinsIntroStarted = true;
            RenderBackground.resetFadeIn();
            AudioManager.playBGM(AudioManager.EXO_TWINS_BGM);
            return;
        }

        if (exoTwinsIntroStarted && !exoTwinsSpawned && RenderBackground.isFullyOpaque()) {
            ApolloEnemy apollo = new ApolloEnemy(100, -50, 0, 0, EXO_TWINS_HP);
            ArtemisEnemy artemis = new ArtemisEnemy(412, Main.WINDOW_HEIGHT + 250, 0, 0, EXO_TWINS_HP);
            enemyAircrafts.add(apollo);
            enemyAircrafts.add(artemis);
            exoTwinsManager = new ExoTwinsManager(apollo, artemis, EXO_TWINS_HP);
            exoTwinsSpawned = true;
        }

    }

    @Override
    protected void renderBackgroundLayer(GraphicsContext gc) {
        super.renderBackgroundLayer(gc);
        if (exoTwinsIntroStarted || (exoTwinsManager != null && !exoTwinsManager.isBattleFinished())) {
            RenderBackground.renderBackground(gc);
        }
    }

    @Override
    protected void renderForegroundLayer(GraphicsContext gc) {
        if (explosionFlashAlpha <= 1e-3) {
            return;
        }
        gc.save();
        gc.setGlobalAlpha(Math.min(1.0, explosionFlashAlpha));
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillRect(0, 0, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        gc.restore();
    }

    @Override
    protected String getDifficultyName() {
        return "infernum";
    }
}

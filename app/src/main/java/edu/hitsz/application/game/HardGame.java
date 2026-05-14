package edu.hitsz.application.game;

import edu.hitsz.application.AudioManager;
import edu.hitsz.application.ImageManager;
import edu.hitsz.aircraft.enemy.BossEnemy;

public class HardGame extends AbstractGame {

    private int enemySpawnCounter;
    private int difficultyCounter;

    // 召唤BOSS的分数间隔
    private final int BOSS_SCORE_INTERVAL = 1000;

    // 上次召唤BOSS时的分数
    private int lastBossScore = 0;
    private boolean bossBgmPlaying = false;

    public HardGame() {
        super(ImageManager.BACKGROUND_IMAGE_5);
    }

    @Override
    protected void initializeDifficulty() {
        enemyMaxNumber = 15;
        enemySpawnCycle = 18;
        enemyShootCycle = 18;
        heroShootCycle = 18;
        eliteEnemyRate = 0.22;
        enemyHpRate = 1.0;
        enemySpeedRate = 1.0;
        bossHpRate = 1.0;
        applyEnemyFactoryRates();
    }

    @Override
    protected void updateDifficulty() {
        difficultyCounter++;
        if (difficultyCounter >= 1000) {
            difficultyCounter = 0;
            upgradeEnemyDifficulty(0.01, 0.25, 0.03, 0.03);
            if (enemyShootCycle > 10) {
                enemyShootCycle -= 0.4;
            }
            if (heroShootCycle > 10) {
                heroShootCycle -= 0.2;
            }
            bossHpRate += 0.1;
            System.out.printf("提高难度! 精英机概率:%.2f,敌机周期:%.2f,敌机射击周期:%.2f,英雄射击周期:%.2f,敌机血量倍率:%.2f%n",
                    eliteEnemyRate, enemySpawnCycle, enemyShootCycle, heroShootCycle, enemyHpRate);
        }
    }

    @Override
    protected void spawnEnemy() {
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
        // 召唤BOSS敌机
        if (score - lastBossScore >= BOSS_SCORE_INTERVAL) {
            enemyAircrafts.add(createBossEnemyFactory().createEnemy());
            lastBossScore = score;
            bossHpRate += 0.2;
            bossBgmPlaying = true;
            AudioManager.playBGM(AudioManager.BGM_BOSS);
        }
        if (bossBgmPlaying && enemyAircrafts.stream().noneMatch(enemy -> enemy instanceof BossEnemy)) {
            bossBgmPlaying = false;
            AudioManager.playBGM(AudioManager.BGM);
        }
    }

    @Override
    protected String getDifficultyName() {
        return "hard";
    }
}

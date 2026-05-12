package edu.hitsz.application.game;

import edu.hitsz.application.ImageManager;
public class NormalGame extends AbstractGame {

    private int enemySpawnCounter;
    private int difficultyCounter;

    // 召唤BOSS的分数间隔
    private final int BOSS_SCORE_INTERVAL = 1000;

    // 上次召唤BOSS时的分数
    private int lastBossScore = 0;

    public NormalGame() {
        super(ImageManager.BACKGROUND_IMAGE_2);
    }

    @Override
    protected void initializeDifficulty() {
        enemyMaxNumber = 10;
        enemySpawnCycle = 20;
        enemyShootCycle = 22;
        heroShootCycle = 20;
        eliteEnemyRate = 0.20;
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
            upgradeEnemyDifficulty(0.01, 0.2, 0.02, 0.02);
            System.out.printf("提高难度! 精英机概率:%.2f,敌机周期:%.2f,敌机属性倍率:%.2f,敌机血量倍率:%.2f%n",
                    eliteEnemyRate, enemySpawnCycle, enemyHpRate, enemyHpRate);
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
        }
    }

    @Override
    protected String getDifficultyName() {
        return "normal";
    }
}

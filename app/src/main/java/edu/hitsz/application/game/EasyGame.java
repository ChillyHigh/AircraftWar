package edu.hitsz.application.game;

import edu.hitsz.application.ImageManager;
public class EasyGame extends AbstractGame {

    private int enemySpawnCounter;

    public EasyGame() {
        super(ImageManager.BACKGROUND_IMAGE);
    }

    @Override
    protected void initializeDifficulty() {
        enemyMaxNumber = 5;
        enemySpawnCycle = 24;
        enemyShootCycle = 24;
        heroShootCycle = 20;
        eliteEnemyRate = 0.20;
        applyEnemyFactoryRates();
    }

    @Override
    protected void updateDifficulty() {

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

    }

    @Override
    protected String getDifficultyName() {
        return "easy";
    }
}

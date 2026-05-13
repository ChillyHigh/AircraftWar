package edu.hitsz.aircraft.enemy.exotwins.combo;

import edu.hitsz.aircraft.enemy.ExoTwinsManager;
import edu.hitsz.aircraft.enemy.exotwins.ApolloEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ArtemisEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ExoTwinsState;
import edu.hitsz.application.AudioManager;
import edu.hitsz.application.Main;
import edu.hitsz.application.WindowShaker;

public class ExoTwinsDeathState extends ExoTwinsState {
    private enum Phase {
        MOVE_NEAR_HERO,
        REORIENT,
        DASH_CHARGE_UP,
        DASH,
        COLLISION_AND_EXPLODE
    }

    private static final int MOVE_NEAR_HERO_TIME = 45;
    private static final int REORIENT_TIME = 80;
    private static final int DASH_CHARGE_UP_TIME = 22;
    private static final int DASH_TIME = 12;
    private static final int[] BEEP_DELAYS = {
            14, 12, 10, 9, 8, 7, 6, 5, 5, 4, 4, 3
    };

    private static final double MOVE_NEAR_DISTANCE = 280.0;
    private static final double APOLLO_FOLLOW_DISTANCE = 320.0;
    private static final double REORIENT_RADIUS_START = 320.0;
    private static final double REORIENT_RADIUS_END = 520.0;
    private static final double CHARGE_RADIUS_START = 520.0;
    private static final double CHARGE_RADIUS_END = 900.0;
    private static final double DASH_SPEED = 56.0;

    private Phase phase = Phase.MOVE_NEAR_HERO;
    private double collisionX = 0.0;
    private double collisionY = 0.0;
    private int nextBeepTimer = BEEP_DELAYS[0];
    private int beepIndex = 0;

    double[] apolloDirection = {0, 0};
    double[] artemisDirection = {0, 0};

    @Override
    public void update(ExoTwinsManager exoTwinsManager) {
        stateTimer++;

        ApolloEnemy apollo = exoTwinsManager.getApollo();
        ArtemisEnemy artemis = exoTwinsManager.getArtemis();
        apollo.setContactDamageEnabled(false);
        artemis.setContactDamageEnabled(false);

        switch (phase) {
            case MOVE_NEAR_HERO -> updateMoveNearHero(apollo, artemis);
            case REORIENT -> updateReorient(apollo, artemis);
            case DASH_CHARGE_UP -> updateDashChargeUp(apollo, artemis);
            case DASH -> updateDash(apollo, artemis);
            case COLLISION_AND_EXPLODE -> updateCollisionAndExplode(exoTwinsManager, apollo, artemis);
        }
    }

    private void updateMoveNearHero(ApolloEnemy apollo, ArtemisEnemy artemis) {
        apollo.setState(1);
        artemis.setState(1);

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();
        double[] heroToApollo = normalize(apollo.getLocationX() - heroX, apollo.getLocationY() - heroY - 40.0);
        double apolloTargetX = clamp(heroX + heroToApollo[0] * MOVE_NEAR_DISTANCE, 70.0, Main.WINDOW_WIDTH - 70.0);
        double apolloTargetY = clamp(heroY + heroToApollo[1] * MOVE_NEAR_DISTANCE, 90.0, Main.WINDOW_HEIGHT - 160.0);

        double[] behindApollo = normalize(heroX - apolloTargetX, heroY - apolloTargetY);
        double artemisTargetX = clamp(apolloTargetX - behindApollo[0] * APOLLO_FOLLOW_DISTANCE, 70.0, Main.WINDOW_WIDTH - 70.0);
        double artemisTargetY = clamp(apolloTargetY - behindApollo[1] * APOLLO_FOLLOW_DISTANCE, 90.0, Main.WINDOW_HEIGHT - 130.0);

        apollo.setDesiredState(apolloTargetX, apolloTargetY, heroX, heroY, 8.0, 7.0, 0.80, 0);
        artemis.setDesiredState(artemisTargetX, artemisTargetY, heroX, heroY, 8.0, 7.0, 0.80, 0);

        if (stateTimer >= MOVE_NEAR_HERO_TIME) {
            nextPhase(Phase.REORIENT);
        }
    }

    private void updateReorient(ApolloEnemy apollo, ArtemisEnemy artemis) {
        apollo.setState(1);
        artemis.setState(1);

        double progress = clamp(stateTimer / (double) REORIENT_TIME, 0.0, 1.0);
        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();
        double angle = lerp(Math.PI * 0.60, Math.PI * 0.25, progress);
        double radius = lerp(REORIENT_RADIUS_START, REORIENT_RADIUS_END, progress);

        double apolloTargetX = clamp(heroX + Math.cos(angle) * radius, 60.0, Main.WINDOW_WIDTH - 60.0);
        double apolloTargetY = clamp(heroY - Math.sin(angle) * radius, 60.0, Main.WINDOW_HEIGHT - 150.0);
        double artemisTargetX = clamp(heroX - Math.cos(angle) * radius, 60.0, Main.WINDOW_WIDTH - 60.0);
        double artemisTargetY = clamp(heroY + Math.sin(angle) * radius, 60.0, Main.WINDOW_HEIGHT - 160.0);

        apollo.setDesiredState(apolloTargetX, apolloTargetY, heroX, heroY, lerp(6.5, 11.5, progress), 9.0, 0.78, 7);
        artemis.setDesiredState(artemisTargetX, artemisTargetY, heroX, heroY, lerp(6.5, 11.5, progress), 9.0, 0.78, 7);

        if (--nextBeepTimer <= 0) {
            AudioManager.playSound((beepIndex & 1) == 0
                    ? AudioManager.EXO_TWINS_WARNING_BEEP_1
                    : AudioManager.EXO_TWINS_WARNING_BEEP_2);
            beepIndex = Math.min(beepIndex + 1, BEEP_DELAYS.length - 1);
            nextBeepTimer = BEEP_DELAYS[beepIndex];
        }

        if (stateTimer >= REORIENT_TIME) {
            nextPhase(Phase.DASH_CHARGE_UP);
        }
    }

    private void updateDashChargeUp(ApolloEnemy apollo, ArtemisEnemy artemis) {
        apollo.setState(1);
        artemis.setState(1);

        double progress = clamp(stateTimer / (double) DASH_CHARGE_UP_TIME, 0.0, 1.0);
        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();
        double[] apolloDirection = normalize(apollo.getLocationX() - heroX, apollo.getLocationY() - heroY);
        double[] artemisDirection = normalize(artemis.getLocationX() - heroX, artemis.getLocationY() - heroY);
        double radius = lerp(CHARGE_RADIUS_START, CHARGE_RADIUS_END, progress * progress);

        apollo.setDesiredState(
                clamp(heroX + apolloDirection[0] * radius, 50.0, Main.WINDOW_WIDTH - 50.0),
                clamp(heroY + apolloDirection[1] * radius, 50.0, Main.WINDOW_HEIGHT - 80.0),
                heroX, heroY,
                10.0, 10.0, 0.74, 1);
        artemis.setDesiredState(
                clamp(heroX + artemisDirection[0] * radius, 50.0, Main.WINDOW_WIDTH - 50.0),
                clamp(heroY + artemisDirection[1] * radius, 50.0, Main.WINDOW_HEIGHT - 80.0),
                heroX, heroY,
                10.0, 10.0, 0.74, 1);

        if (stateTimer == 1) {
            WindowShaker.startScreenShake(8.0, 0.92, DASH_CHARGE_UP_TIME);
        }

        if (stateTimer >= DASH_CHARGE_UP_TIME) {
            collisionX = heroX;
            collisionY = heroY;
            setDashVelocity(apollo, artemis);
            WindowShaker.startScreenShake(18.0, 0.90, DASH_TIME + 10);
            nextPhase(Phase.DASH);
        }
    }

    private void updateDash(ApolloEnemy apollo, ArtemisEnemy artemis) {
        apollo.setState(2);
        artemis.setState(2);
        if (stateTimer == 1) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH);
        }

//        boolean apolloReached = distance(apollo.getLocationX(), apollo.getLocationY(), collisionX, collisionY) <= 90.0;
//        boolean artemisReached = distance(artemis.getLocationX(), artemis.getLocationY(), collisionX, collisionY) <= 90.0;
//        if ((apolloReached && artemisReached) || stateTimer >= DASH_TIME) {
//            triggerCollision(apollo, artemis);
//            nextPhase(Phase.COLLISION_AND_EXPLODE);
//
        if (stateTimer >= DASH_TIME) {
            triggerCollision(apollo, artemis);
            nextPhase(Phase.COLLISION_AND_EXPLODE);
            stopDash(apollo, artemis);
        }
    }

    private void updateCollisionAndExplode(ExoTwinsManager manager, ApolloEnemy apollo, ArtemisEnemy artemis) {
        if (stateTimer == 1) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DEATH_COLLISION);
            AudioManager.playSound(AudioManager.BOMB_EXPLOSION);
            WindowShaker.startScreenShake(26.0, 0.89, 40);
            manager.triggerDeathExplosion();
        }
    }

    private void triggerCollision(ApolloEnemy apollo, ArtemisEnemy artemis) {
        apollo.setSpeed(0.0, 0.0);
        artemis.setSpeed(0.0, 0.0);
    }

    private void setDashVelocity(ApolloEnemy apollo, ArtemisEnemy artemis) {
        apolloDirection = normalize(collisionX - apollo.getLocationX(), collisionY - apollo.getLocationY());
        artemisDirection = normalize(collisionX - artemis.getLocationX(), collisionY - artemis.getLocationY());
        apollo.setDashVelocity(apolloDirection[0] * DASH_SPEED, apolloDirection[1] * DASH_SPEED);
        artemis.setDashVelocity(artemisDirection[0] * DASH_SPEED, artemisDirection[1] * DASH_SPEED);
    }

    private void stopDash(ApolloEnemy apollo, ArtemisEnemy artemis) {
        apollo.setDashVelocity(apolloDirection[0] * 0.1, apolloDirection[1] * 0.1);
        artemis.setDashVelocity(artemisDirection[0] * 0.1, artemisDirection[1] * 0.1);
    }

    private void nextPhase(Phase next) {
        phase = next;
        stateTimer = 0;
    }

    private double[] normalize(double dx, double dy) {
        double length = Math.hypot(dx, dy);
        if (length < 1e-6) {
            return new double[]{0.0, -1.0};
        }
        return new double[]{dx / length, dy / length};
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double lerp(double start, double end, double t) {
        return start + (end - start) * clamp(t, 0.0, 1.0);
    }
}

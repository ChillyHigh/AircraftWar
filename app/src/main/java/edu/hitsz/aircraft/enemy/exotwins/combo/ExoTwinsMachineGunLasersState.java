package edu.hitsz.aircraft.enemy.exotwins.combo;

import java.util.concurrent.ThreadLocalRandom;

import edu.hitsz.aircraft.enemy.ExoTwinsManager;
import edu.hitsz.aircraft.enemy.exotwins.ApolloEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ArtemisEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ExoTwinsState;
import edu.hitsz.application.AudioManager;
import edu.hitsz.application.Main;
import edu.hitsz.application.WindowShaker;
import edu.hitsz.bullet.ArtemisLaserBullet;

public class ExoTwinsMachineGunLasersState extends ExoTwinsState {
    private static final int STATE_LOOP_COUNT_BEFORE_ADVANCE = 4;

    private static final int ARTEMIS_ATTACK_DELAY = 36;
    private static final int ARTEMIS_ATTACK_DURATION = 100;
    private static final int ARTEMIS_LASER_SHOOT_RATE = 10;
    private static final int ARTEMIS_GATLING_LOOP_RATE = 54;
    private static final double ARTEMIS_LASER_SHOOT_SPEED = 14.0;
    private static final double ARTEMIS_HOVER_DISTANCE = 180.0;
    private static final double ARTEMIS_MOVE_SPEED = 12.0;
    private static final double ARTEMIS_ROTATE_SPEED = 10.0;
    private static final int ARTEMIS_LASER_POWER = 30;

    private static final int APOLLO_REDIRECT_TIME = 26;
    private static final int APOLLO_TELEGRAPH_TIME = 20;
    private static final int APOLLO_DASH_TIME = 14;
    private static final int APOLLO_PLASMA_RELEASE_RATE = 4;
    private static final int APOLLO_CYCLE_TIME =
            APOLLO_REDIRECT_TIME + APOLLO_TELEGRAPH_TIME + APOLLO_DASH_TIME + 40;
    private static final double APOLLO_DASH_SPEED = 48.0;
    private static final double APOLLO_SLOWDOWN_FACTOR = 0.72;
    private static final double APOLLO_SIDE_DISTANCE = 230.0;
    private static final double APOLLO_VERTICAL_OFFSET = -40.0;
    private static final double APOLLO_PLASMA_SPEED = 11.0;
    private static final int APOLLO_PLASMA_POWER = 0;

    private static final int STATE_CYCLE_TIME = ARTEMIS_ATTACK_DELAY + ARTEMIS_ATTACK_DURATION;

    private double apolloDashVelocityX = 0.0;
    private double apolloDashVelocityY = 0.0;
    private int completedCycles = 0;

    @Override
    public void update(ExoTwinsManager exoTwinsManager) {
        stateTimer++;
        int wrappedTime = (stateTimer - 1) % STATE_CYCLE_TIME;
        if (stateTimer > 1 && wrappedTime == 0) {
            completedCycles++;
            if (completedCycles >= STATE_LOOP_COUNT_BEFORE_ADVANCE) {
                exoTwinsManager.advanceCycle();
                return;
            }
        }

        updateArtemis(exoTwinsManager, wrappedTime);
        updateApollo(exoTwinsManager, wrappedTime);
    }

    private void updateArtemis(ExoTwinsManager exoTwinsManager, int wrappedTime) {
        ArtemisEnemy artemis = exoTwinsManager.getArtemis();
        artemis.setState(1);

        boolean attackActive = wrappedTime >= ARTEMIS_ATTACK_DELAY;
        int attackTime = Math.max(0, wrappedTime - ARTEMIS_ATTACK_DELAY);

        double[] hoverDirection = normalizedDirection(
                artemis.getLocationX() - hero.getLocationX(),
                artemis.getLocationY() - hero.getLocationY() - 40.0);
        double targetX = clamp(hero.getLocationX() + hoverDirection[0] * ARTEMIS_HOVER_DISTANCE, 80.0, Main.WINDOW_WIDTH - 80.0);
        double targetY = clamp(hero.getLocationY() + hoverDirection[1] * ARTEMIS_HOVER_DISTANCE, 80.0, Main.WINDOW_HEIGHT - 170.0);

        double aimAngle = Math.atan2(hero.getLocationY() - artemis.getLocationY(), hero.getLocationX() - artemis.getLocationX());
        double lookAngle = attackActive ? aimAngle : aimAngle + Math.PI / 2.0;
        double lookDistance = 220.0;

        artemis.setDesiredState(
                targetX,
                targetY,
                artemis.getLocationX() + Math.cos(lookAngle) * lookDistance,
                artemis.getLocationY() + Math.sin(lookAngle) * lookDistance,
                ARTEMIS_MOVE_SPEED,
                attackActive ? 2.0 : 10.0,
                0.80,
                attackActive ? 1 : 0);

        if (attackActive && attackTime < ARTEMIS_ATTACK_DURATION - 45) {

            if (attackTime % ARTEMIS_LASER_SHOOT_RATE == ARTEMIS_LASER_SHOOT_RATE - 1) {
                double shootSpeedFactor = lerp(0.45, 1.0, attackTime / 60.0);
                double shootSpeed = ARTEMIS_LASER_SHOOT_SPEED * shootSpeedFactor * randomRange(1.0, 1.15);
                double shootAngle = Math.toRadians(artemis.getRotation() - 90.0);
                artemis.addBullet(new ArtemisLaserBullet(artemis.getLocationX(), artemis.getLocationY(),
                        Math.cos(shootAngle) * 10,
                        Math.sin(shootAngle) * 10,
                        ARTEMIS_LASER_POWER));
                AudioManager.playSound(AudioManager.EXO_LASER_SHOOT);
            }
        }
    }

    private void updateApollo(ExoTwinsManager exoTwinsManager, int wrappedTime) {
        ApolloEnemy apollo = exoTwinsManager.getApollo();

        if (wrappedTime < ARTEMIS_ATTACK_DELAY) {
            updateApolloRedirect(apollo, 0, 1.0);
            return;
        }

        int attackTime = wrappedTime - ARTEMIS_ATTACK_DELAY;
        int cycleWrapped = attackTime % APOLLO_CYCLE_TIME;

        if (cycleWrapped < APOLLO_REDIRECT_TIME) {
            updateApolloRedirect(apollo, cycleWrapped, cycleWrapped / (double) APOLLO_REDIRECT_TIME);
            return;
        }

        if (cycleWrapped < APOLLO_REDIRECT_TIME + APOLLO_TELEGRAPH_TIME) {
            updateApolloTelegraph(apollo, cycleWrapped);
            return;
        }

        if (cycleWrapped < APOLLO_REDIRECT_TIME + APOLLO_TELEGRAPH_TIME + APOLLO_DASH_TIME) {
            updateApolloDash(apollo, cycleWrapped);
            return;
        }

        updateApolloSlowdown(apollo);
    }

    private void updateApolloRedirect(ApolloEnemy apollo, int cycleWrapped, double progress) {
        apollo.setState(1);

        double leftX = clamp(hero.getLocationX() - APOLLO_SIDE_DISTANCE, 70.0, Main.WINDOW_WIDTH - 70.0);
        double rightX = clamp(hero.getLocationX() + APOLLO_SIDE_DISTANCE, 70.0, Main.WINDOW_WIDTH - 70.0);
        double leftY = clamp(hero.getLocationY() + APOLLO_VERTICAL_OFFSET, 70.0, Main.WINDOW_HEIGHT - 140.0);
        double rightY = leftY;

        double distanceLeft = Math.hypot(apollo.getLocationX() - leftX, apollo.getLocationY() - leftY);
        double distanceRight = Math.hypot(apollo.getLocationX() - rightX, apollo.getLocationY() - rightY);

        double targetX = distanceLeft < distanceRight ? leftX : rightX;
        double targetY = distanceLeft < distanceRight ? leftY : rightY;
        double[] predictedHero = predictedHeroPosition(25.0);

        apollo.setDesiredState(
                targetX,
                targetY,
                predictedHero[0],
                predictedHero[1],
                lerp(7.0, 13.0, progress),
                12.0,
                0.81,
                1);
    }

    private void updateApolloTelegraph(ApolloEnemy apollo, int cycleWrapped) {
        apollo.setState(1);

        double facingAngle = Math.toRadians(apollo.getRotation() - 90.0);
        double backX = apollo.getLocationX() - Math.cos(facingAngle) * 36.0;
        double backY = apollo.getLocationY() - Math.sin(facingAngle) * 36.0;
        double[] predictedHero = predictedHeroPosition(18.0);

        apollo.setDesiredState(
                backX,
                backY,
                predictedHero[0],
                predictedHero[1],
                6.0,
                18.0,
                0.74,
                1);

        if (cycleWrapped == APOLLO_REDIRECT_TIME + Math.max(1, (int) (APOLLO_TELEGRAPH_TIME * 0.32))) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH_TELEGRAPH);
        }

        if (cycleWrapped == APOLLO_REDIRECT_TIME + APOLLO_TELEGRAPH_TIME - 1) {
            double dashAngle = Math.toRadians(apollo.getRotation() - 90.0);
            apolloDashVelocityX = Math.cos(dashAngle) * APOLLO_DASH_SPEED;
            apolloDashVelocityY = Math.sin(dashAngle) * APOLLO_DASH_SPEED;
        }
    }

    private void updateApolloDash(ApolloEnemy apollo, int cycleWrapped) {
        if (cycleWrapped == APOLLO_REDIRECT_TIME + APOLLO_TELEGRAPH_TIME) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH);
            WindowShaker.startScreenShake(10.0, 0.88, APOLLO_DASH_TIME);
        }

        apollo.setState(2);
        apollo.setDashVelocity(apolloDashVelocityX, apolloDashVelocityY);
    }

    private void updateApolloSlowdown(ApolloEnemy apollo) {
        apollo.setState(2);
        apolloDashVelocityX *= APOLLO_SLOWDOWN_FACTOR;
        apolloDashVelocityY *= APOLLO_SLOWDOWN_FACTOR;
        apollo.setDashVelocity(apolloDashVelocityX, apolloDashVelocityY);
    }

    private double[] predictedHeroPosition(double lookAheadFrames) {
        return new double[]{
                hero.getLocationX(),
                hero.getLocationY()
        };
    }

    private double[] normalizedDirection(double dx, double dy) {
        double length = Math.hypot(dx, dy);
        if (length < 1e-6) {
            return new double[]{1.0, 0.0};
        }
        return new double[]{dx / length, dy / length};
    }

    private double randomRange(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private double lerp(double start, double end, double t) {
        return start + (end - start) * clamp(t, 0.0, 1.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

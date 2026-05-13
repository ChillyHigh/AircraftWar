package edu.hitsz.aircraft.enemy.exotwins.combo;

import java.util.concurrent.ThreadLocalRandom;

import edu.hitsz.aircraft.enemy.ExoTwinsManager;
import edu.hitsz.aircraft.enemy.exotwins.ApolloEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ArtemisEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ExoTwinsState;
import edu.hitsz.application.AudioManager;
import edu.hitsz.application.Main;
import edu.hitsz.application.WindowShaker;
import edu.hitsz.bullet.ArtemisRayBullet;

public class ExoTwinsOverloadState extends ExoTwinsState {
    private static final int OVERLOAD_LOOP_COUNT_BEFORE_ADVANCE = 3;

    private enum ArtemisPhase {
        HOVER_REDIRECT,
        SLOWDOWN,
        CHARGE_UP,
        GLEAM,
        LASER
    }

    private static final int ARTEMIS_HOVER_REDIRECT_TIME = 45;
    private static final int ARTEMIS_SLOWDOWN_TIME = 15;
    private static final int ARTEMIS_CHARGE_UP_TIME = 60;
    private static final int ARTEMIS_GLEAM_TIME = 45;
    private static final int ARTEMIS_LASER_TIME = ArtemisRayBullet.LIFETIME;
    private static final int ARTEMIS_ATTACK_DELAY = 36;

    private static final double ARTEMIS_HOVER_RADIUS_X = 210.0;
    private static final double ARTEMIS_HOVER_RADIUS_Y = 160.0;
    private static final double ARTEMIS_REPOSITION_SPEED_MIN = 5.0;
    private static final double ARTEMIS_REPOSITION_SPEED_MAX = 13.0;
    private static final double ARTEMIS_LASER_STRAFE_SPEED = 16.0;
    private static final int ARTEMIS_LASER_POWER = 30;

    private static final int APOLLO_REDIRECT_TIME = 26;
    private static final int APOLLO_TELEGRAPH_TIME = 20;
    private static final int APOLLO_DASH_TIME = 14;
    private static final int APOLLO_CYCLE_TIME =
            APOLLO_REDIRECT_TIME + APOLLO_TELEGRAPH_TIME + APOLLO_DASH_TIME + 40;
    private static final double APOLLO_DASH_SPEED = 48.0;
    private static final double APOLLO_SLOWDOWN_FACTOR = 0.72;
    private static final double APOLLO_SIDE_DISTANCE = 230.0;
    private static final double APOLLO_VERTICAL_OFFSET = -40.0;

    private ArtemisPhase artemisPhase = ArtemisPhase.HOVER_REDIRECT;

    private int artemisPhaseTimer = 0;

    private double artemisHoverOffsetAngle = Math.PI / 4.0;
    private double artemisBeamDirectionX = 0.0;
    private double artemisBeamDirectionY = 1.0;
    private double artemisStrafeDirectionX = 1.0;
    private double artemisStrafeDirectionY = 0.0;

    private double apolloDashVelocityX = 0.0;
    private double apolloDashVelocityY = 0.0;
    private int completedLoops = 0;

    @Override
    public void update(ExoTwinsManager exoTwinsManager) {
        exoTwinsManager.getApollo().setContactDamageEnabled(false);
        exoTwinsManager.getArtemis().setContactDamageEnabled(false);
        updateArtemis(exoTwinsManager);
        updateApollo(exoTwinsManager);
    }

    private void updateArtemis(ExoTwinsManager exoTwinsManager) {
        artemisPhaseTimer++;
        switch (artemisPhase) {
            case HOVER_REDIRECT -> updateArtemisHoverRedirect(exoTwinsManager);
            case SLOWDOWN -> updateArtemisSlowdown(exoTwinsManager);
            case CHARGE_UP -> updateArtemisChargeUp(exoTwinsManager);
            case GLEAM -> updateArtemisGleam(exoTwinsManager);
            case LASER -> updateArtemisLaser(exoTwinsManager);
        }
    }

    private void updateArtemisHoverRedirect(ExoTwinsManager exoTwinsManager) {
        ArtemisEnemy artemis = exoTwinsManager.getArtemis();
        artemis.clearExothermalPostRenderEffect();

        if (artemisPhaseTimer == 1) {
            artemisHoverOffsetAngle = wrapAngle(artemisHoverOffsetAngle
                    + (ThreadLocalRandom.current().nextBoolean() ? 1.0 : -1.0) * Math.PI / 2.0);
        }

        if (artemisPhaseTimer == ARTEMIS_HOVER_REDIRECT_TIME / 2) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH_TELEGRAPH);
        }

        double[] hoverDestination = computeArtemisHoverDestination();
        double progress = (artemisPhaseTimer + 1.0) / ARTEMIS_HOVER_REDIRECT_TIME;
        double moveSpeed = lerp(ARTEMIS_REPOSITION_SPEED_MIN, ARTEMIS_REPOSITION_SPEED_MAX, progress * progress);

        artemis.setState(1);
        artemis.setDesiredState(
                hoverDestination[0],
                hoverDestination[1],
                hero.getLocationX(),
                hero.getLocationY(),
                moveSpeed,
                8.0,
                0.80,
                exoTwinsManager.getPhase() == 1 ? 7 : 8);

        if (artemisPhaseTimer == ARTEMIS_HOVER_REDIRECT_TIME) {
            nextArtemisPhase(ArtemisPhase.SLOWDOWN);
        }
    }

    private void updateArtemisSlowdown(ExoTwinsManager exoTwinsManager) {
        ArtemisEnemy artemis = exoTwinsManager.getArtemis();
        artemis.clearExothermalPostRenderEffect();
        artemis.setState(1);
        artemis.setDesiredState(
                artemis.getLocationX(),
                artemis.getLocationY(),
                hero.getLocationX(),
                hero.getLocationY(),
                0.0,
                7.5,
                0.72,
                exoTwinsManager.getPhase() == 1 ? 7 : 8);

        if (artemisPhaseTimer == ARTEMIS_SLOWDOWN_TIME) {
            nextArtemisPhase(ArtemisPhase.CHARGE_UP);
        }
    }

    private void updateArtemisChargeUp(ExoTwinsManager exoTwinsManager) {
        ArtemisEnemy artemis = exoTwinsManager.getArtemis();
        double chargeProgress = Math.clamp(artemisPhaseTimer / (double) ARTEMIS_CHARGE_UP_TIME, 0.0, 1.0);

        artemis.setState(1);
        artemis.setDesiredState(
                artemis.getLocationX(),
                artemis.getLocationY(),
                hero.getLocationX(),
                hero.getLocationY(),
                0.0,
                10.0,
                0.68,
                8);
        artemis.setExothermalChargeUpEffect(chargeProgress);
        WindowShaker.startScreenShake(chargeProgress * chargeProgress * 5.0, 0.84, 2);

        if (artemisPhaseTimer == ARTEMIS_CHARGE_UP_TIME) {
            nextArtemisPhase(ArtemisPhase.GLEAM);
        }
    }

    private void updateArtemisGleam(ExoTwinsManager exoTwinsManager) {
        ArtemisEnemy artemis = exoTwinsManager.getArtemis();
        double gleamProgress = Math.clamp(artemisPhaseTimer / (double) ARTEMIS_GLEAM_TIME, 0.0, 1.0);

        artemis.setState(1);
        artemis.setDesiredState(
                artemis.getLocationX(),
                artemis.getLocationY(),
                hero.getLocationX(),
                hero.getLocationY(),
                0.0,
                10.0,
                0.68,
                8);
        artemis.setExothermalGleamEffect(gleamProgress);

        if (artemisPhaseTimer == ARTEMIS_GLEAM_TIME) {
            nextArtemisPhase(ArtemisPhase.LASER);
        }
    }

    private void updateArtemisLaser(ExoTwinsManager exoTwinsManager) {
        ArtemisEnemy artemis = exoTwinsManager.getArtemis();
        artemis.clearExothermalPostRenderEffect();

        if (artemisPhaseTimer == 1) {
            double beamAngle = Math.toRadians(artemis.getRotation() - 90.0);
            artemisBeamDirectionX = Math.cos(beamAngle);
            artemisBeamDirectionY = Math.sin(beamAngle);

            double[] toHero = normalizedDirection(
                    hero.getLocationX() - artemis.getLocationX(),
                    hero.getLocationY() - artemis.getLocationY());
            double leftX = -artemisBeamDirectionY;
            double leftY = artemisBeamDirectionX;
            double rightX = -leftX;
            double rightY = -leftY;
            double leftDot = leftX * toHero[0] + leftY * toHero[1];
            double rightDot = rightX * toHero[0] + rightY * toHero[1];

            // 这里根据英雄机的位置，判断Artemis是否需要向左或向右移动
            if (leftDot >= rightDot) {
                artemisStrafeDirectionX = leftX;
                artemisStrafeDirectionY = leftY;
            } else {
                artemisStrafeDirectionX = rightX;
                artemisStrafeDirectionY = rightY;
            }

            artemis.addBullet(new ArtemisRayBullet(artemis, ARTEMIS_LASER_POWER));
            AudioManager.playSound(AudioManager.ARTEMIS_SPIN_LASERBEAM);
            WindowShaker.startScreenShake(18.0, 0.94, 30);
        }

        artemis.setState(1);
        artemis.setDesiredState(
                Math.clamp(artemis.getLocationX() + artemisStrafeDirectionX * 36.0, 72.0, Main.WINDOW_WIDTH - 72.0),
                Math.clamp(artemis.getLocationY() + artemisStrafeDirectionY * 36.0, 72.0, Main.WINDOW_HEIGHT - 220.0),
                artemis.getLocationX() + artemisBeamDirectionX * 360.0,
                artemis.getLocationY() + artemisBeamDirectionY * 360.0,
                ARTEMIS_LASER_STRAFE_SPEED,
                4.0,
                0.84,
                8);

        if (artemisPhaseTimer == ARTEMIS_LASER_TIME) {
            completedLoops++;
            if (completedLoops >= OVERLOAD_LOOP_COUNT_BEFORE_ADVANCE) {
                exoTwinsManager.advanceCycle();
                return;
            }
            nextArtemisPhase(ArtemisPhase.HOVER_REDIRECT);
            apolloDashVelocityX = 0.0;
            apolloDashVelocityY = 0.0;
        }
    }

    private void updateApollo(ExoTwinsManager exoTwinsManager) {
        ApolloEnemy apollo = exoTwinsManager.getApollo();

        if (artemisPhaseTimer < ARTEMIS_ATTACK_DELAY) {
            updateApolloRedirect(apollo, 0, 1.0);
            return;
        }

        int attackTime = artemisPhaseTimer - ARTEMIS_ATTACK_DELAY;
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

    private void nextArtemisPhase(ArtemisPhase nextPhase) {
        artemisPhase = nextPhase;
        artemisPhaseTimer = 0;
    }

    private double[] computeArtemisHoverDestination() {
        double hoverDestinationX = Math.clamp(
                hero.getLocationX() + Math.cos(artemisHoverOffsetAngle) * ARTEMIS_HOVER_RADIUS_X,
                72.0,
                Main.WINDOW_WIDTH - 72.0);
        double hoverDestinationY = Math.clamp(
                hero.getLocationY() + Math.sin(artemisHoverOffsetAngle) * ARTEMIS_HOVER_RADIUS_Y,
                72.0,
                Main.WINDOW_HEIGHT - 210.0);
        return new double[]{hoverDestinationX, hoverDestinationY};
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

    private double wrapAngle(double angle) {
        double wrapped = angle % (Math.PI * 2.0);
        return wrapped < 0.0 ? wrapped + Math.PI * 2.0 : wrapped;
    }

    private double lerp(double start, double end, double t) {
        return start + (end - start) * Math.clamp(t, 0.0, 1.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

}

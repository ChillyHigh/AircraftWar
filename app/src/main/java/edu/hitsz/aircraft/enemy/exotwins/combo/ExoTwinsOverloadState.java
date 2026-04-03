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

    private enum ArtemisPhase {
        HOVER_REDIRECT,
        SLOWDOWN,
        CHARGE_UP,
        GLEAM,
        LASER
    }

    private enum ApolloPhase {
        SPIN,
        DASH_DELAY,
        DASH_REPOSITION,
        DASHING
    }

    private static final int ARTEMIS_HOVER_REDIRECT_TIME = 45;
    private static final int ARTEMIS_SLOWDOWN_TIME = 15;
    private static final int ARTEMIS_CHARGE_UP_TIME = 60;
    private static final int ARTEMIS_GLEAM_TIME = 45;
    private static final int ARTEMIS_LASER_TIME = ArtemisRayBullet.LIFETIME;

    private static final double ARTEMIS_HOVER_RADIUS_X = 210.0;
    private static final double ARTEMIS_HOVER_RADIUS_Y = 160.0;
    private static final double ARTEMIS_REPOSITION_SPEED_MIN = 5.0;
    private static final double ARTEMIS_REPOSITION_SPEED_MAX = 13.0;
    private static final double ARTEMIS_LASER_STRAFE_SPEED = 16.0;
    private static final int ARTEMIS_LASER_POWER = 0;

    private static final int APOLLO_SPIN_TIME = 45;
    private static final int APOLLO_DASH_DELAY = 14;
    private static final int APOLLO_DASH_REPOSITION_TIME = 5;
    private static final int APOLLO_DASH_TIME = 27;

    private static final double APOLLO_SPIN_RADIUS_START = 150.0;
    private static final double APOLLO_SPIN_RADIUS_GROWTH = 3.0;
    private static final double APOLLO_DASH_SPEED = 52.0;
    private static final double APOLLO_TRAIL_SHOOT_SPEED = 6.5;
    private static final double APOLLO_RESET_DISTANCE = 330.0;
    private static final int APOLLO_TRAIL_RELEASE_RATE = 4;
    private static final int APOLLO_TRAIL_POWER = 0;

    private ArtemisPhase artemisPhase = ArtemisPhase.HOVER_REDIRECT;
    private ApolloPhase apolloPhase = ApolloPhase.SPIN;

    private int artemisPhaseTimer = 0;
    private int apolloPhaseTimer = 0;

    private double artemisHoverOffsetAngle = Math.PI / 4.0;
    private double artemisBeamDirectionX = 0.0;
    private double artemisBeamDirectionY = 1.0;
    private double artemisStrafeDirectionX = 1.0;
    private double artemisStrafeDirectionY = 0.0;

    private double apolloSpinOffsetAngle = 0.0;
    private double apolloSpinRadius = APOLLO_SPIN_RADIUS_START;
    private double apolloDashVelocityX = 0.0;
    private double apolloDashVelocityY = 0.0;

    @Override
    public void update(ExoTwinsManager exoTwinsManager) {
        updateArtemis(exoTwinsManager);
//        updateApollo(exoTwinsManager);
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
                7);

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
                7);

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

            artemis.queueExothermalDisintegrationRay(ARTEMIS_LASER_POWER);
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
            nextArtemisPhase(ArtemisPhase.HOVER_REDIRECT);
        }
    }

    private void updateApollo(ExoTwinsManager exoTwinsManager) {
        apolloPhaseTimer++;
        switch (apolloPhase) {
            case SPIN -> updateApolloSpin(exoTwinsManager);
            case DASH_DELAY -> updateApolloDashDelay(exoTwinsManager);
            case DASH_REPOSITION -> updateApolloDashReposition(exoTwinsManager);
            case DASHING -> updateApolloDashing(exoTwinsManager);
        }
    }

    private void updateApolloSpin(ExoTwinsManager exoTwinsManager) {
        ApolloEnemy apollo = exoTwinsManager.getApollo();

        if (apolloPhaseTimer == 1) {
            apolloSpinOffsetAngle = ThreadLocalRandom.current().nextInt(8) * Math.PI / 4.0;
            apolloSpinRadius = APOLLO_SPIN_RADIUS_START;
        }

        apollo.setState(1);
        apolloSpinOffsetAngle += Math.PI / 38.0;

        double targetX = Math.clamp(
                hero.getLocationX() + Math.cos(apolloSpinOffsetAngle) * apolloSpinRadius,
                70.0,
                Main.WINDOW_WIDTH - 70.0);
        double targetY = Math.clamp(
                hero.getLocationY() + Math.sin(apolloSpinOffsetAngle) * apolloSpinRadius,
                70.0,
                Main.WINDOW_HEIGHT - 150.0);

        double moveSpeed = apolloPhaseTimer <= APOLLO_SPIN_TIME - 14
                ? lerp(4.0, 11.0, apolloPhaseTimer / (double) APOLLO_SPIN_TIME)
                : 4.0;

        apollo.setDesiredState(
                targetX,
                targetY,
                hero.getLocationX(),
                hero.getLocationY(),
                moveSpeed,
                12.0,
                0.78,
                7);

        apolloSpinRadius += APOLLO_SPIN_RADIUS_GROWTH;
        if (apolloPhaseTimer == 12) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH_TELEGRAPH);
        }

        if (apolloPhaseTimer == APOLLO_SPIN_TIME) {
            nextApolloPhase(ApolloPhase.DASH_DELAY);
        }
    }

    private void updateApolloDashDelay(ExoTwinsManager exoTwinsManager) {
        ApolloEnemy apollo = exoTwinsManager.getApollo();
        apollo.setState(1);
        apollo.setDesiredState(
                apollo.getLocationX(),
                apollo.getLocationY(),
                hero.getLocationX(),
                hero.getLocationY(),
                0.0,
                16.0,
                0.72,
                7);

        if (apolloPhaseTimer == APOLLO_DASH_DELAY) {
            nextApolloPhase(ApolloPhase.DASH_REPOSITION);
        }
    }

    private void updateApolloDashReposition(ExoTwinsManager exoTwinsManager) {
        ApolloEnemy apollo = exoTwinsManager.getApollo();
        apollo.setState(1);
        apollo.setDesiredState(
                apollo.getLocationX(),
                apollo.getLocationY(),
                hero.getLocationX(),
                hero.getLocationY(),
                8.0,
                20.0,
                0.70,
                7);

        if (apolloPhaseTimer == 1) {
            double dashAngle = Math.toRadians(apollo.getRotation() - 90.0);
            apolloDashVelocityX = Math.cos(dashAngle) * APOLLO_DASH_SPEED;
            apolloDashVelocityY = Math.sin(dashAngle) * APOLLO_DASH_SPEED;
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH);
            WindowShaker.startScreenShake(12.0, 0.90, 16);
        }

        if (apolloPhaseTimer == APOLLO_DASH_REPOSITION_TIME) {
            nextApolloPhase(ApolloPhase.DASHING);
        }
    }

    private void updateApolloDashing(ExoTwinsManager exoTwinsManager) {
        ApolloEnemy apollo = exoTwinsManager.getApollo();
        apollo.setState(2);
        apollo.setDashVelocity(apolloDashVelocityX, apolloDashVelocityY);

        if (apolloPhaseTimer % APOLLO_TRAIL_RELEASE_RATE == APOLLO_TRAIL_RELEASE_RATE - 1) {
            double reverseAngle = Math.atan2(-apolloDashVelocityY, -apolloDashVelocityX);
            apollo.queuePlasmaBulletByAngle(
                    reverseAngle + randomSpread(Math.toRadians(18.0)),
                    APOLLO_TRAIL_SHOOT_SPEED,
                    APOLLO_TRAIL_POWER);
        }

        if (apolloPhaseTimer == APOLLO_DASH_TIME) {
            resetApolloPosition(apollo);
            nextApolloPhase(ApolloPhase.SPIN);
        }
    }

    private void nextArtemisPhase(ArtemisPhase nextPhase) {
        artemisPhase = nextPhase;
        artemisPhaseTimer = 0;
    }

    private void nextApolloPhase(ApolloPhase nextPhase) {
        apolloPhase = nextPhase;
        apolloPhaseTimer = 0;
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

    private void resetApolloPosition(ApolloEnemy apollo) {
        double[] awayDirection = normalizedDirection(
                apollo.getLocationX() - hero.getLocationX(),
                apollo.getLocationY() - hero.getLocationY());
        apollo.setLocation(
                Math.clamp(hero.getLocationX() + awayDirection[0] * APOLLO_RESET_DISTANCE, 70.0, Main.WINDOW_WIDTH - 70.0),
                Math.clamp(hero.getLocationY() + awayDirection[1] * APOLLO_RESET_DISTANCE, 70.0, Main.WINDOW_HEIGHT - 160.0));
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

    private double randomSpread(double spread) {
        return (ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * spread;
    }

    private double lerp(double start, double end, double t) {
        return start + (end - start) * Math.clamp(t, 0.0, 1.0);
    }

}

package edu.hitsz.aircraft.enemy.exotwins.combo;

import edu.hitsz.aircraft.enemy.ExoTwinsManager;
import edu.hitsz.aircraft.enemy.exotwins.ExoTwinsState;
import edu.hitsz.application.AudioManager;
import edu.hitsz.application.WindowShaker;

public class ExoTwinsDashState extends ExoTwinsState {
    private static final int DASH_LOOP_COUNT_BEFORE_ADVANCE = 4;

    private enum Phase {
        ENTER_SIDE,
        ORBIT,
        DASH_PREPARE,
        DASHING,
        RETURN_SIDE
    }

    private static final double INIT_ORBIT_ANGLE = Math.toRadians(-45.0);
    private static final double ORBIT_RADIUS_X = 150.0;
    private static final double ORBIT_RADIUS_Y = 170.0;
    private static final double ENTER_SPEED = 8.0;
    private static final double ORBIT_SPEED = 40.0;
    private static final double PREPARE_SPEED = 7.0;
    private static final double ORBIT_ANGULAR_SPEED = Math.toRadians(180.0 * 6) / 50;
    private static final double ORBIT_DURATION = Math.toRadians(180.0 * 2.0);
    private static final double RETURN_SPEED = ORBIT_ANGULAR_SPEED * Math.hypot(ORBIT_RADIUS_X, ORBIT_RADIUS_Y);
    private static final double ROTATE_SPEED = 6.0;
    private static final int PREPARE_DURATION = 35;
    private static final double DASH_SPEED = 60.0;
    private static final int DASH_DURATION = 24;
    private static final double ARRIVE_THRESHOLD = 10.0;

    private Phase phase = Phase.ENTER_SIDE;
    private double orbitAngle = INIT_ORBIT_ANGLE;
    private double lastOrbitAngle = orbitAngle;
    private int completedLoops = 0;

    @Override
    public void update(ExoTwinsManager exoTwinsManager) {
        stateTimer++;
        switch (phase) {
            case ENTER_SIDE -> updateEnterSide(exoTwinsManager);
            case ORBIT -> updateOrbit(exoTwinsManager);
            case DASH_PREPARE -> updateDashPrepare(exoTwinsManager);
            case DASHING -> updateDashing(exoTwinsManager);
            case RETURN_SIDE -> updateReturnSide(exoTwinsManager);
        }
    }

    private void updateEnterSide(ExoTwinsManager exoTwinsManager) {
        exoTwinsManager.getApollo().setState(1);
        exoTwinsManager.getArtemis().setState(1);
        exoTwinsManager.getApollo().setContactDamageEnabled(false);
        exoTwinsManager.getArtemis().setContactDamageEnabled(false);

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();
        double apolloTargetX = heroX + Math.cos(orbitAngle) * ORBIT_RADIUS_X;
        double apolloTargetY = heroY + Math.sin(orbitAngle) * ORBIT_RADIUS_Y;
        double artemisTargetX = heroX + Math.cos(orbitAngle + Math.PI) * ORBIT_RADIUS_X;
        double artemisTargetY = heroY + Math.sin(orbitAngle + Math.PI) * ORBIT_RADIUS_Y;

        exoTwinsManager.getApollo().setDesiredState(apolloTargetX, apolloTargetY, heroX, heroY, ENTER_SPEED, ROTATE_SPEED, 0.7,
                exoTwinsManager.getPhase() == 1 ? 0 : 6);
        exoTwinsManager.getArtemis().setDesiredState(artemisTargetX, artemisTargetY, heroX, heroY, ENTER_SPEED, ROTATE_SPEED, 0.7,
                exoTwinsManager.getPhase() == 1 ? 0 : 6);

        boolean apolloArrived = near(exoTwinsManager.getApollo().getLocationX(), exoTwinsManager.getApollo().getLocationY(),
                apolloTargetX, apolloTargetY, ARRIVE_THRESHOLD);
        boolean artemisArrived = near(exoTwinsManager.getArtemis().getLocationX(), exoTwinsManager.getArtemis().getLocationY(),
                artemisTargetX, artemisTargetY, ARRIVE_THRESHOLD);

        if (apolloArrived && artemisArrived) {
            lastOrbitAngle = orbitAngle;
            nextPhase(Phase.DASH_PREPARE);
        }
    }

    private void updateOrbit(ExoTwinsManager exoTwinsManager) {
        exoTwinsManager.getApollo().setState(1);
        exoTwinsManager.getArtemis().setState(1);
        exoTwinsManager.getApollo().setContactDamageEnabled(false);
        exoTwinsManager.getArtemis().setContactDamageEnabled(false);

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double t = Math.min(1.0, (orbitAngle - lastOrbitAngle) / ORBIT_DURATION);
        double speedFactor = 1.0 - t;
        orbitAngle += ORBIT_ANGULAR_SPEED * speedFactor;

        double apolloTargetX = heroX + Math.cos(orbitAngle) * ORBIT_RADIUS_X;
        double apolloTargetY = heroY + Math.sin(orbitAngle) * ORBIT_RADIUS_Y;
        double artemisTargetX = heroX + Math.cos(orbitAngle + Math.PI) * ORBIT_RADIUS_X;
        double artemisTargetY = heroY + Math.sin(orbitAngle + Math.PI) * ORBIT_RADIUS_Y;

        exoTwinsManager.getApollo().setDesiredState(
                apolloTargetX, apolloTargetY,
                heroX, heroY,
                ORBIT_SPEED, Double.POSITIVE_INFINITY,
                0.2,
                exoTwinsManager.getPhase() == 1 ? 0 : 6);
        exoTwinsManager.getArtemis().setDesiredState(
                artemisTargetX, artemisTargetY,
                heroX, heroY,
                ORBIT_SPEED, Double.POSITIVE_INFINITY,
                0.2,
                exoTwinsManager.getPhase() == 1 ? 0 : 6);

        if (orbitAngle - lastOrbitAngle >= ORBIT_DURATION - 0.5) {
            nextPhase(Phase.DASH_PREPARE);
        }
    }

    private void updateDashPrepare(ExoTwinsManager exoTwinsManager) {
        exoTwinsManager.getApollo().setState(1);
        exoTwinsManager.getArtemis().setState(1);
        exoTwinsManager.getApollo().setContactDamageEnabled(false);
        exoTwinsManager.getArtemis().setContactDamageEnabled(false);

        if (stateTimer == 1) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH_TELEGRAPH);
        }

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();
        double apolloDx = exoTwinsManager.getApollo().getLocationX() - heroX;
        double apolloDy = exoTwinsManager.getApollo().getLocationY() - heroY;
        double artemisDx = exoTwinsManager.getArtemis().getLocationX() - heroX;
        double artemisDy = exoTwinsManager.getArtemis().getLocationY() - heroY;
        double pullBack = 80.0;
        double apolloLen = Math.max(1e-6, Math.hypot(apolloDx, apolloDy));
        double artemisLen = Math.max(1e-6, Math.hypot(artemisDx, artemisDy));

        exoTwinsManager.getApollo().setDesiredState(
                exoTwinsManager.getApollo().getLocationX() + apolloDx / apolloLen * pullBack,
                exoTwinsManager.getApollo().getLocationY() + apolloDy / apolloLen * pullBack,
                heroX, heroY, PREPARE_SPEED, ROTATE_SPEED, 0.7,
                exoTwinsManager.getPhase() == 1 ? 1 : 7);
        exoTwinsManager.getArtemis().setDesiredState(
                exoTwinsManager.getArtemis().getLocationX() + artemisDx / artemisLen * pullBack,
                exoTwinsManager.getArtemis().getLocationY() + artemisDy / artemisLen * pullBack,
                heroX, heroY, PREPARE_SPEED, ROTATE_SPEED, 0.7,
                exoTwinsManager.getPhase() == 1 ? 1 : 7);

        if (stateTimer == PREPARE_DURATION / 4) {
            setDashVelocity(exoTwinsManager);
        }
        if (stateTimer >= PREPARE_DURATION) {
            WindowShaker.startScreenShake(15.0, 0.9, DASH_DURATION);
            nextPhase(Phase.DASHING);
        }
    }

    private void updateDashing(ExoTwinsManager exoTwinsManager) {
        exoTwinsManager.getApollo().setState(2);
        exoTwinsManager.getArtemis().setState(2);
        exoTwinsManager.getApollo().setContactDamageEnabled(true);
        exoTwinsManager.getArtemis().setContactDamageEnabled(true);

        if (stateTimer == 1) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH);
        }
        if (stateTimer >= DASH_DURATION) {
            exoTwinsManager.getApollo().setContactDamageEnabled(false);
            exoTwinsManager.getArtemis().setContactDamageEnabled(false);
            orbitAngle -= Math.PI / 2.0;
            lastOrbitAngle = orbitAngle;
            nextPhase(Phase.RETURN_SIDE);
        }
    }

    private void updateReturnSide(ExoTwinsManager exoTwinsManager) {
        exoTwinsManager.getApollo().setState(1);
        exoTwinsManager.getArtemis().setState(1);

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();
        double apolloTargetX = heroX + Math.cos(orbitAngle) * ORBIT_RADIUS_X;
        double apolloTargetY = heroY + Math.sin(orbitAngle) * ORBIT_RADIUS_Y;
        double artemisTargetX = heroX + Math.cos(orbitAngle + Math.PI) * ORBIT_RADIUS_X;
        double artemisTargetY = heroY + Math.sin(orbitAngle + Math.PI) * ORBIT_RADIUS_Y;

        exoTwinsManager.getApollo().setDesiredState(apolloTargetX, apolloTargetY, heroX, heroY, RETURN_SPEED, Double.POSITIVE_INFINITY, 0.7,
                exoTwinsManager.getPhase() == 1 ? 0 : 6);
        exoTwinsManager.getArtemis().setDesiredState(artemisTargetX, artemisTargetY, heroX, heroY, RETURN_SPEED, Double.POSITIVE_INFINITY, 0.7,
                exoTwinsManager.getPhase() == 1 ? 0 : 6);

        boolean apolloArrived = near(exoTwinsManager.getApollo().getLocationX(), exoTwinsManager.getApollo().getLocationY(),
                apolloTargetX, apolloTargetY, ARRIVE_THRESHOLD * 4.0);
        boolean artemisArrived = near(exoTwinsManager.getArtemis().getLocationX(), exoTwinsManager.getArtemis().getLocationY(),
                artemisTargetX, artemisTargetY, ARRIVE_THRESHOLD * 4.0);
        if (apolloArrived && artemisArrived) {
            lastOrbitAngle = orbitAngle;
            if (completedLoops >= DASH_LOOP_COUNT_BEFORE_ADVANCE - 1) {
                exoTwinsManager.advanceCycle();
                return;
            }
            completedLoops++;
            nextPhase(Phase.ORBIT);
        }
    }

    private void setDashVelocity(ExoTwinsManager exoTwinsManager) {
        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double apolloDx = heroX - exoTwinsManager.getApollo().getLocationX();
        double apolloDy = heroY - exoTwinsManager.getApollo().getLocationY();
        double apolloLen = Math.max(1e-6, Math.hypot(apolloDx, apolloDy));
        exoTwinsManager.getApollo().setDashVelocity(apolloDx / apolloLen * DASH_SPEED, apolloDy / apolloLen * DASH_SPEED);

        double artemisDx = heroX - exoTwinsManager.getArtemis().getLocationX();
        double artemisDy = heroY - exoTwinsManager.getArtemis().getLocationY();
        double artemisLen = Math.max(1e-6, Math.hypot(artemisDx, artemisDy));
        exoTwinsManager.getArtemis().setDashVelocity(artemisDx / artemisLen * DASH_SPEED, artemisDy / artemisLen * DASH_SPEED);
    }

    private void nextPhase(Phase next) {
        phase = next;
        stateTimer = 0;
    }

    private boolean near(double x1, double y1, double x2, double y2, double threshold) {
        return Math.hypot(x1 - x2, y1 - y2) <= threshold;
    }
}

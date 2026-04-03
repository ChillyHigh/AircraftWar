package edu.hitsz.aircraft.enemy.exotwins.combo;

import edu.hitsz.aircraft.enemy.ExoTwinsManager;
import edu.hitsz.aircraft.enemy.exotwins.ExoTwinsState;
import edu.hitsz.application.AudioManager;
import edu.hitsz.application.WindowShaker;

public class ExoTwinsDashState extends ExoTwinsState {

    private enum Phase {
        ENTER_SIDE,
        ORBIT,
        DASH_PREPARE,
        DASH_START,
        DASHING,
        RETURN_SIDE
    }

    private Phase phase = Phase.ENTER_SIDE;

    private static final double INIT_ORBIT_ANGLE = Math.toRadians(-45.0);

    private double orbitAngle = INIT_ORBIT_ANGLE;
    private double lastOrbitAngle = orbitAngle;

    private int cycleCount = 0;

    private static final double ORBIT_RADIUS_X = 150.0;
    private static final double ORBIT_RADIUS_Y = 170.0;
    private static final double ENTER_SPEED = 8.0;
    private static final double ORBIT_SPEED = 40.0;
    private static final double PREPARE_SPEED = 7.0;
    private static final double ORBIT_ANGULAR_SPEED = Math.toRadians(180.0 * 6) / 50;
    private static final double RETURN_SPEED = ORBIT_ANGULAR_SPEED * Math.hypot(ORBIT_RADIUS_X, ORBIT_RADIUS_Y);
    private static final double ROTATE_SPEED = 6.0;
    private static final double ORBIT_DURATION = Math.toRadians(180 * 2);
    private static final int PREPARE_DURATION = 35;
    private static final double DASH_SPEED = 60.0;
    private static final int DASH_DURATION = 24;
    private static final double ARRIVE_THRESHOLD = 10.0;

//    public ExoTwinsDashState(ExoTwinsManager exoTwinsManager) {
//        super(exoTwinsManager);
//    }

    public void update(ExoTwinsManager exoTwinsManager) {
        stateTimer++;
        switch (phase) {
            case ENTER_SIDE -> updateEnterSide(exoTwinsManager);
            case ORBIT -> updateOrbit(exoTwinsManager);
            case DASH_PREPARE -> updateDashPrepare(exoTwinsManager);
            case DASH_START -> dashStart(exoTwinsManager);
            case DASHING -> updateDashing(exoTwinsManager);
            case RETURN_SIDE -> updateReturnSide(exoTwinsManager);
        }
    }


    private void updateEnterSide(ExoTwinsManager exoTwinsManager) {
        exoTwinsManager.getApollo().setState(1);
        exoTwinsManager.getArtemis().setState(1);

        orbitAngle = INIT_ORBIT_ANGLE;

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double apolloTargetX = heroX + Math.cos(orbitAngle) * ORBIT_RADIUS_X;
        double apolloTargetY = heroY + Math.sin(orbitAngle) * ORBIT_RADIUS_Y;

        double artemisTargetX = heroX + Math.cos(orbitAngle + Math.PI) * ORBIT_RADIUS_X;
        double artemisTargetY = heroY + Math.sin(orbitAngle + Math.PI) * ORBIT_RADIUS_Y;

        exoTwinsManager.getApollo().setDesiredState(
                apolloTargetX, apolloTargetY,
                heroX, heroY,
                ENTER_SPEED, ROTATE_SPEED,
                0.7,
                0);

        exoTwinsManager.getArtemis().setDesiredState(
                artemisTargetX, artemisTargetY,
                heroX, heroY,
                ENTER_SPEED, ROTATE_SPEED,
                0.7,
                0);

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
                0);

        exoTwinsManager.getArtemis().setDesiredState(
                artemisTargetX, artemisTargetY,
                heroX, heroY,
                ORBIT_SPEED, Double.POSITIVE_INFINITY,
                0.2,
                0);

        if (orbitAngle - lastOrbitAngle >= ORBIT_DURATION - 0.5) {
            if (cycleCount == 5) {
                exoTwinsManager.changeState(new ExoTwinsDashesAndLasersState());
                return;
            }
            cycleCount++;
            nextPhase(Phase.DASH_PREPARE);
        }
    }

    private void updateDashPrepare(ExoTwinsManager exoTwinsManager) {
        exoTwinsManager.getApollo().setState(1);
        exoTwinsManager.getArtemis().setState(1);

        if (stateTimer == 1) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH_TELEGRAPH);
        }

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double apolloDx = exoTwinsManager.getApollo().getLocationX() - heroX;
        double apolloDy = exoTwinsManager.getApollo().getLocationY() - heroY;
        double apolloLen = Math.max(1e-6, Math.hypot(apolloDx, apolloDy));

        double artemisDx = exoTwinsManager.getArtemis().getLocationX() - heroX;
        double artemisDy = exoTwinsManager.getArtemis().getLocationY() - heroY;
        double artemisLen = Math.max(1e-6, Math.hypot(artemisDx, artemisDy));

        double pullBack = 80.0;

        double apolloTargetX = exoTwinsManager.getApollo().getLocationX() + apolloDx / apolloLen * pullBack;
        double apolloTargetY = exoTwinsManager.getApollo().getLocationY() + apolloDy / apolloLen * pullBack;

        double artemisTargetX = exoTwinsManager.getArtemis().getLocationX() + artemisDx / artemisLen * pullBack;
        double artemisTargetY = exoTwinsManager.getArtemis().getLocationY() + artemisDy / artemisLen * pullBack;

        exoTwinsManager.getApollo().setDesiredState(
                apolloTargetX, apolloTargetY,
                heroX, heroY,
                PREPARE_SPEED, ROTATE_SPEED,
                0.7,
                1);

        exoTwinsManager.getArtemis().setDesiredState(
                artemisTargetX, artemisTargetY,
                heroX, heroY,
                PREPARE_SPEED, ROTATE_SPEED,
                0.7,
                1);

        if (stateTimer == PREPARE_DURATION / 4) {
            dashStart(exoTwinsManager);
        }

        if (stateTimer >= PREPARE_DURATION) {
            nextPhase(Phase.DASHING);
            WindowShaker.startScreenShake(15.0, 0.9, DASH_DURATION);
        }
    }

    private void dashStart(ExoTwinsManager exoTwinsManager) {
        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double apolloDx = heroX - exoTwinsManager.getApollo().getLocationX();
        double apolloDy = heroY - exoTwinsManager.getApollo().getLocationY();
        double apolloVx = apolloDx / Math.max(1e-6, Math.hypot(apolloDx, apolloDy)) * DASH_SPEED;
        double apolloVy = apolloDy / Math.max(1e-6, Math.hypot(apolloDx, apolloDy)) * DASH_SPEED;
        exoTwinsManager.getApollo().setDashVelocity(apolloVx, apolloVy);

        double artemisDx = heroX - exoTwinsManager.getArtemis().getLocationX();
        double artemisDy = heroY - exoTwinsManager.getArtemis().getLocationY();
        double artemisVx = artemisDx / Math.max(1e-6, Math.hypot(artemisDx, artemisDy)) * DASH_SPEED;
        double artemisVy = artemisDy / Math.max(1e-6, Math.hypot(artemisDx, artemisDy)) * DASH_SPEED;
        exoTwinsManager.getArtemis().setDashVelocity(artemisVx, artemisVy);

    }

    private void updateDashing(ExoTwinsManager exoTwinsManager) {
        exoTwinsManager.getApollo().setState(2);
        exoTwinsManager.getArtemis().setState(2);
        if (stateTimer == 5) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH);
        }
        if (stateTimer >= DASH_DURATION) {
            orbitAngle -= Math.PI / 2;
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

        exoTwinsManager.getApollo().setDesiredState(
                apolloTargetX, apolloTargetY,
                heroX, heroY,
                RETURN_SPEED, Double.POSITIVE_INFINITY,
                0.7,
                0);

        exoTwinsManager.getArtemis().setDesiredState(
                artemisTargetX, artemisTargetY,
                heroX, heroY,
                RETURN_SPEED, Double.POSITIVE_INFINITY,
                0.7,
                0);

        boolean apolloArrived = near(exoTwinsManager.getApollo().getLocationX(), exoTwinsManager.getApollo().getLocationY(),
                apolloTargetX, apolloTargetY, ARRIVE_THRESHOLD * 8);
        boolean artemisArrived = near(exoTwinsManager.getArtemis().getLocationX(), exoTwinsManager.getArtemis().getLocationY(),
                artemisTargetX, artemisTargetY, ARRIVE_THRESHOLD * 8);

        if (apolloArrived && artemisArrived) {
            lastOrbitAngle = orbitAngle;
            nextPhase(Phase.ORBIT);
        }
    }


    private void nextPhase(Phase next) {
        phase = next;
        stateTimer = 0;
    }

    private boolean near(double x1, double y1, double x2, double y2, double threshold) {
        return Math.hypot(x1 - x2, y1 - y2) <= threshold;
    }

}

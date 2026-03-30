package edu.hitsz.aircraft.enemy;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.AudioManager;

/**
 * 控制Apollo和Artemis的生成和行为
 */
public class ExoTwinsManager {

    public enum TwinPhase {
        IDLE,
        ENTER_SIDE,
        ORBIT,
        DASH_PREPARE,
        DASH_START,
        DASHING,
        RETURN_SIDE,
        FREE1,
    }

    private static final double INIT_ORBIT_ANGLE = Math.toRadians(-45.0);

    private TwinPhase phase = TwinPhase.ENTER_SIDE;
    private int phaseTimer = 0;
    private double orbitAngle = INIT_ORBIT_ANGLE;
    private double lastOrbitAngle = orbitAngle;
    private int cycleCount = 0;

    private static final double ORBIT_RADIUS_X = 150.0;
    private static final double ORBIT_RADIUS_Y = 170.0;
    private static final double ENTER_SPEED = 8.0;
    private static final double ORBIT_SPEED = 40.0;
    private static final double PREPARE_SPEED = 7.0;
    private static final double RETURN_SPEED = ORBIT_SPEED * 0.7;
    private static final double ROTATE_SPEED = 6.0;
    private static final double ORBIT_ANGULAR_SPEED = Math.toRadians(180.0 * 3) / 50;
    private static final double ORBIT_DURATION = Math.toRadians(180);
    private static final int PREPARE_DURATION = 35;
    private static final double DASH_SPEED = 60.0;
    private static final int DASH_DURATION = 24;
    private static final double ARRIVE_THRESHOLD = 10.0;

    private static final int FREE1_DURATION = 600;

    // Apollo和Artemis的引用
    private final ApolloEnemy apollo;
    private final ArtemisEnemy artemis;

    public ExoTwinsManager(ApolloEnemy apollo, ArtemisEnemy artemis) {
        this.apollo = apollo;
        this.artemis = artemis;
    }

    public void update(HeroAircraft hero) {
        phaseTimer++;
        switch (phase) {
            case IDLE -> {
                apollo.setState(1);
                artemis.setState(1);
                // nextPhase(TwinPhase.ENTER_SIDE);
            }
            case FREE1 -> updateFree1(hero);
            case ENTER_SIDE -> updateEnterSide(hero);
            case ORBIT -> updateOrbit(hero);
            case DASH_PREPARE -> updateDashPrepare(hero);
            case DASH_START -> updateDashStart(hero);
            case DASHING -> updateDashing(hero);
            case RETURN_SIDE -> updateReturnSide(hero);
        }
    }

    private void updateEnterSide(HeroAircraft hero) {
        apollo.setState(1);
        artemis.setState(1);

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double apolloTargetX = heroX + Math.cos(orbitAngle) * ORBIT_RADIUS_X;
        double apolloTargetY = heroY + Math.sin(orbitAngle) * ORBIT_RADIUS_Y;

        double artemisTargetX = heroX + Math.cos(orbitAngle + Math.PI) * ORBIT_RADIUS_X;
        double artemisTargetY = heroY + Math.sin(orbitAngle + Math.PI) * ORBIT_RADIUS_Y;

        apollo.setDesiredState(
                apolloTargetX, apolloTargetY,
                heroX, heroY,
                ENTER_SPEED, ROTATE_SPEED,
                0.7,
                0);

        artemis.setDesiredState(
                artemisTargetX, artemisTargetY,
                heroX, heroY,
                ENTER_SPEED, ROTATE_SPEED,
                0.7,
                0);

        boolean apolloArrived = near(apollo.getLocationX(), apollo.getLocationY(),
                apolloTargetX, apolloTargetY, ARRIVE_THRESHOLD);
        boolean artemisArrived = near(artemis.getLocationX(), artemis.getLocationY(),
                artemisTargetX, artemisTargetY, ARRIVE_THRESHOLD);

        if (apolloArrived && artemisArrived) {
            lastOrbitAngle = orbitAngle;
            nextPhase(TwinPhase.DASH_PREPARE);
        }
    }

    private void updateOrbit(HeroAircraft hero) {
        apollo.setState(1);
        artemis.setState(1);

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double t = Math.min(1.0, (orbitAngle - lastOrbitAngle) / (double) ORBIT_DURATION);
        double speedFactor = 1.0 - t;
        orbitAngle += ORBIT_ANGULAR_SPEED * speedFactor;

        double apolloTargetX = heroX + Math.cos(orbitAngle) * ORBIT_RADIUS_X;
        double apolloTargetY = heroY + Math.sin(orbitAngle) * ORBIT_RADIUS_Y;

        double artemisTargetX = heroX + Math.cos(orbitAngle + Math.PI) * ORBIT_RADIUS_X;
        double artemisTargetY = heroY + Math.sin(orbitAngle + Math.PI) * ORBIT_RADIUS_Y;

        apollo.setDesiredState(
                apolloTargetX, apolloTargetY,
                heroX, heroY,
                ORBIT_SPEED, Double.POSITIVE_INFINITY,
                0.2,
                0);

        artemis.setDesiredState(
                artemisTargetX, artemisTargetY,
                heroX, heroY,
                ORBIT_SPEED, Double.POSITIVE_INFINITY,
                0.2,
                0);

        if (orbitAngle - lastOrbitAngle >= ORBIT_DURATION - 0.5) {
            if (cycleCount == 5) {
                nextPhase(TwinPhase.FREE1);
                cycleCount = 0;
                return;
            }
            cycleCount++;
            nextPhase(TwinPhase.DASH_PREPARE);
        }
    }

    private void updateDashPrepare(HeroAircraft hero) {
        apollo.setState(1);
        artemis.setState(1);

        if (phaseTimer == 1) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH_TELEGRAPH);
        }

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double apolloDx = apollo.getLocationX() - heroX;
        double apolloDy = apollo.getLocationY() - heroY;
        double apolloLen = Math.max(1e-6, Math.hypot(apolloDx, apolloDy));

        double artemisDx = artemis.getLocationX() - heroX;
        double artemisDy = artemis.getLocationY() - heroY;
        double artemisLen = Math.max(1e-6, Math.hypot(artemisDx, artemisDy));

        double pullBack = 80.0;

        double apolloTargetX = apollo.getLocationX() + apolloDx / apolloLen * pullBack;
        double apolloTargetY = apollo.getLocationY() + apolloDy / apolloLen * pullBack;

        double artemisTargetX = artemis.getLocationX() + artemisDx / artemisLen * pullBack;
        double artemisTargetY = artemis.getLocationY() + artemisDy / artemisLen * pullBack;

        apollo.setDesiredState(
                apolloTargetX, apolloTargetY,
                heroX, heroY,
                PREPARE_SPEED, ROTATE_SPEED,
                0.7,
                1);

        artemis.setDesiredState(
                artemisTargetX, artemisTargetY,
                heroX, heroY,
                PREPARE_SPEED, ROTATE_SPEED,
                0.7,
                1);

        if (phaseTimer == PREPARE_DURATION / 4) {
            updateDashStart(hero);
        }

        if (phaseTimer >= PREPARE_DURATION) {
            nextPhase(TwinPhase.DASHING);
        }
    }

    private void updateDashStart(HeroAircraft hero) {
        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double apolloDx = heroX - apollo.getLocationX();
        double apolloDy = heroY - apollo.getLocationY();
        double apolloVx = apolloDx / Math.max(1e-6, Math.hypot(apolloDx, apolloDy)) * DASH_SPEED;
        double apolloVy = apolloDy / Math.max(1e-6, Math.hypot(apolloDx, apolloDy)) * DASH_SPEED;
        apollo.setDashVelocity(apolloVx, apolloVy);

        double artemisDx = heroX - artemis.getLocationX();
        double artemisDy = heroY - artemis.getLocationY();
        double artemisVx = artemisDx / Math.max(1e-6, Math.hypot(artemisDx, artemisDy)) * DASH_SPEED;
        double artemisVy = artemisDy / Math.max(1e-6, Math.hypot(artemisDx, artemisDy)) * DASH_SPEED;
        artemis.setDashVelocity(artemisVx, artemisVy);

    }

    private void updateDashing(HeroAircraft hero) {
        apollo.setState(2);
        artemis.setState(2);
        if (phaseTimer == 5) {
            AudioManager.playSound(AudioManager.EXO_TWINS_DASH);
        }
        if (phaseTimer >= DASH_DURATION) {
            orbitAngle -= Math.PI / 2;
            lastOrbitAngle = orbitAngle;
            nextPhase(TwinPhase.RETURN_SIDE);
        }
    }

    private void updateReturnSide(HeroAircraft hero) {
        apollo.setState(1);
        artemis.setState(1);

        double heroX = hero.getLocationX();
        double heroY = hero.getLocationY();

        double apolloTargetX = heroX + Math.cos(orbitAngle) * ORBIT_RADIUS_X;
        double apolloTargetY = heroY + Math.sin(orbitAngle) * ORBIT_RADIUS_Y;

        double artemisTargetX = heroX + Math.cos(orbitAngle + Math.PI) * ORBIT_RADIUS_X;
        double artemisTargetY = heroY + Math.sin(orbitAngle + Math.PI) * ORBIT_RADIUS_Y;

        apollo.setDesiredState(
                apolloTargetX, apolloTargetY,
                heroX, heroY,
                RETURN_SPEED, Double.POSITIVE_INFINITY,
                0.7,
                0);

        artemis.setDesiredState(
                artemisTargetX, artemisTargetY,
                heroX, heroY,
                RETURN_SPEED, Double.POSITIVE_INFINITY,
                0.7,
                0);

        boolean apolloArrived = near(apollo.getLocationX(), apollo.getLocationY(),
                apolloTargetX, apolloTargetY, ARRIVE_THRESHOLD * 4);
        boolean artemisArrived = near(artemis.getLocationX(), artemis.getLocationY(),
                artemisTargetX, artemisTargetY, ARRIVE_THRESHOLD * 4);

        if (apolloArrived && artemisArrived) {
            lastOrbitAngle = orbitAngle;
            nextPhase(TwinPhase.ORBIT);
        }
    }

    private void updateFree1(HeroAircraft hero) {
        apollo.setState(0);
        artemis.setState(0);

        if (phaseTimer >= FREE1_DURATION) {
            nextPhase(TwinPhase.ENTER_SIDE);
        }
    }

    private void nextPhase(TwinPhase next) {
        phase = next;
        phaseTimer = 0;
    }

    private boolean near(double x1, double y1, double x2, double y2, double threshold) {
        return Math.hypot(x1 - x2, y1 - y2) <= threshold;
    }

}

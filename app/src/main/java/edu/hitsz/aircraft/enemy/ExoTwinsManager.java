package edu.hitsz.aircraft.enemy;

import edu.hitsz.aircraft.enemy.exotwins.ApolloEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ArtemisEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ExoTwinsState;
import edu.hitsz.aircraft.enemy.exotwins.combo.ExoTwinsDashState;
import edu.hitsz.aircraft.enemy.exotwins.combo.ExoTwinsMachineGunLasersState;
import edu.hitsz.aircraft.enemy.exotwins.combo.ExoTwinsOverloadState;

public class ExoTwinsManager {

    public enum AttackPattern {
        DASH,
        MACHINE_GUN_LASERS,
        OVERLOAD_RAY
    }

    private ExoTwinsState state;
    private final ApolloEnemy apollo;
    private final ArtemisEnemy artemis;
    private final int maxSharedHp;
    private int sharedHp;
    private int phase = 1;
    private int patternIndex = 0;

    public ExoTwinsManager(ApolloEnemy apollo, ArtemisEnemy artemis, int sharedHp) {
        this.apollo = apollo;
        this.artemis = artemis;
        this.maxSharedHp = sharedHp;
        this.sharedHp = sharedHp;
        this.apollo.setManager(this);
        this.artemis.setManager(this);
        syncSharedHp();
        this.state = createState(nextPattern());
    }

    public void update() {
        if (isDefeated()) {
            apollo.vanish();
            artemis.vanish();
            return;
        }

        if (sharedHp <= maxSharedHp / 2) {
            phase = 2;
        }
        syncSharedHp();
        state.update(this);
    }

    public ApolloEnemy getApollo() {
        return apollo;
    }

    public ArtemisEnemy getArtemis() {
        return artemis;
    }

    public int getSharedHp() {
        return sharedHp;
    }

    public int getMaxSharedHp() {
        return maxSharedHp;
    }

    public int getPhase() {
        return phase;
    }

    public boolean isDefeated() {
        return sharedHp <= 0 || apollo.notValid() || artemis.notValid();
    }

    public void applyDamage(int damage) {
        if (damage <= 0 || isDefeated()) {
            return;
        }
        sharedHp = Math.max(0, sharedHp - damage);
        syncSharedHp();
        if (sharedHp == 0) {
            apollo.vanish();
            artemis.vanish();
        }
    }

    public void changeState(ExoTwinsState next) {
        state = next;
    }

    public void advanceCycle() {
        state = createState(nextPattern());
    }

    private void syncSharedHp() {
        apollo.setSharedHp(sharedHp);
        artemis.setSharedHp(sharedHp);
    }

    private AttackPattern nextPattern() {
        AttackPattern[] phaseOne = {
                AttackPattern.DASH,
                AttackPattern.MACHINE_GUN_LASERS
        };
        AttackPattern[] phaseTwo = {
                AttackPattern.DASH,
                AttackPattern.MACHINE_GUN_LASERS,
                AttackPattern.OVERLOAD_RAY
        };
        AttackPattern[] patterns = phase == 1 ? phaseOne : phaseTwo;
        AttackPattern pattern = patterns[patternIndex % patterns.length];
        patternIndex++;
        return pattern;
    }

    private ExoTwinsState createState(AttackPattern pattern) {
        return switch (pattern) {
            case DASH -> new ExoTwinsDashState();
            case MACHINE_GUN_LASERS -> new ExoTwinsMachineGunLasersState();
            case OVERLOAD_RAY -> new ExoTwinsOverloadState();
        };
    }
}

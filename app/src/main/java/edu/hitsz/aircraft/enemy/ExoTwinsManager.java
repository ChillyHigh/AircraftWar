package edu.hitsz.aircraft.enemy;

import edu.hitsz.aircraft.enemy.exotwins.ApolloEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ArtemisEnemy;
import edu.hitsz.aircraft.enemy.exotwins.ExoTwinsState;
import edu.hitsz.aircraft.enemy.exotwins.combo.ExoTwinsOverloadState;

/**
 * 控制Apollo和Artemis的生成和行为
 */
public class ExoTwinsManager {

    private ExoTwinsState state;

    // Apollo和Artemis的引用
    private final ApolloEnemy apollo;
    private final ArtemisEnemy artemis;

    public ExoTwinsManager(ApolloEnemy apollo, ArtemisEnemy artemis) {
        this.apollo = apollo;
        this.artemis = artemis;
        state = new ExoTwinsOverloadState();

    }

    public void update() {
        state.update(this);
    }

    public ApolloEnemy getApollo() {
        return apollo;
    }

    public ArtemisEnemy getArtemis() {
        return artemis;
    }

    public void changeState(ExoTwinsState next) {
        state = next;
    }

}

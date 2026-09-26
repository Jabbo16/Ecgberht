package ecgberht.BehaviourTrees.Harass;

import ecgberht.Agents.WorkerScoutAgent;
import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;

public class Explore extends BrainAction {

    public Explore(String name, GameState gh) {
        super(name, gh);
    }


    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenHarasser != null) {
                gameState.mil.agents.put(gameState.chosenHarasser, new WorkerScoutAgent(gameState.chosenHarasser, gameState.enemyMainBase));
                gameState.naughtySCV = gameState.chosenHarasser;
                gameState.chosenHarasser = null;
                return BrainStatus.SUCCESS;
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}







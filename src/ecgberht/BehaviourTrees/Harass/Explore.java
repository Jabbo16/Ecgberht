package ecgberht.BehaviourTrees.Harass;

import ecgberht.Agents.WorkerScoutAgent;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;

public class Explore extends Conditional {

    public Explore(String name, GameState gh) {
        super(name, gh);
    }


    @Override
    public State execute() {
        try {
            if (gameState.chosenHarasser != null) {
                gameState.agents.put(gameState.chosenHarasser, new WorkerScoutAgent(gameState.chosenHarasser, gameState.enemyMainBase));
                gameState.naughtySCV = gameState.chosenHarasser;
                gameState.chosenHarasser = null;
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}

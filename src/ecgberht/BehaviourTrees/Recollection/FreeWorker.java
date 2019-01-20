package ecgberht.BehaviourTrees.Recollection;

import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class FreeWorker extends Action {

    public FreeWorker(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            gameState.chosenWorker = null;
            if (!gameState.workerIdle.isEmpty()) {
                int frame = gameState.frameCount;
                for (Unit w : gameState.workerIdle) {
                    if (w.getLastCommandFrame() != frame) {
                        gameState.chosenWorker = w;
                        return State.SUCCESS;
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
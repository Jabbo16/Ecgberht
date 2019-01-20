package ecgberht.BehaviourTrees.Recollection;

import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

import java.util.Map.Entry;

public class CollectMineral extends Action {

    public CollectMineral(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Unit chosen = gameState.chosenWorker;
            if (!gameState.mineralsAssigned.isEmpty()) {
                Unit closestMineral = null;
                int workerPerPatch = 2;
                if (gameState.workerMining.size() < 7) workerPerPatch = 1;
                for (Entry<Unit, Integer> m : gameState.mineralsAssigned.entrySet()) {
                    if ((closestMineral == null || chosen.getDistance(m.getKey()) < chosen.getDistance(closestMineral))
                            && m.getValue() < workerPerPatch) {
                        closestMineral = m.getKey();
                    }
                }
                if (closestMineral != null && chosen.gather(closestMineral, false)) {
                    gameState.mineralsAssigned.put(closestMineral, gameState.mineralsAssigned.get(closestMineral) + 1);
                    gameState.workerMining.put(chosen, closestMineral);
                    gameState.workerIdle.remove(chosen);
                    gameState.chosenWorker = null;
                    gameState.mining++;
                    return State.SUCCESS;
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

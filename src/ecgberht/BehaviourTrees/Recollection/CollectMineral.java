package ecgberht.BehaviourTrees.Recollection;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Map.Entry;

public class CollectMineral extends Action {

    public CollectMineral(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker chosen = gameState.chosenWorker;
            if (!gameState.mineralsAssigned.isEmpty()) {
                MineralPatch closestMineral = null;
                int workerPerPatch = 2;
                if (gameState.workerMining.size() < 7) workerPerPatch = 1;
                for (Entry<MineralPatch, Integer> m : gameState.mineralsAssigned.entrySet()) {
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

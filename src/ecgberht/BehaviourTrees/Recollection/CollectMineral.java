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
            Worker chosen = this.handler.chosenWorker;
            if (!this.handler.mineralsAssigned.isEmpty()) {
                MineralPatch closestMineral = null;
                int workerPerPatch = 2;
                if (this.handler.workerMining.size() < 7) workerPerPatch = 1;
                for (Entry<MineralPatch, Integer> m : this.handler.mineralsAssigned.entrySet()) {
                    if ((closestMineral == null || chosen.getDistance(m.getKey()) < chosen.getDistance(closestMineral))
                            && m.getValue() < workerPerPatch) {
                        closestMineral = m.getKey();
                    }
                }
                if (closestMineral != null && chosen.gather(closestMineral, false)) {
                    this.handler.mineralsAssigned.put(closestMineral, this.handler.mineralsAssigned.get(closestMineral) + 1);
                    this.handler.workerMining.put(chosen, closestMineral);
                    this.handler.workerIdle.remove(chosen);
                    this.handler.chosenWorker = null;
                    this.handler.mining++;
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

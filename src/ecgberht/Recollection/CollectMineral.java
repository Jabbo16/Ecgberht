package ecgberht.Recollection;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Map.Entry;

public class CollectMineral extends Action {

    public CollectMineral(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker chosen = ((GameState) this.handler).chosenWorker;
            if (!((GameState) this.handler).mineralsAssigned.isEmpty()) {
                MineralPatch closestMineral = null;
                for (Entry<MineralPatch, Integer> m : ((GameState) this.handler).mineralsAssigned.entrySet()) {
                    if ((closestMineral == null || chosen.getDistance(m.getKey()) < chosen.getDistance(closestMineral)) && m.getValue() < 2) {
                        closestMineral = m.getKey();
                    }
                }
                if (closestMineral != null) {
                    if (chosen.gather(closestMineral, false)) {
                        ((GameState) this.handler).mineralsAssigned.put(closestMineral, ((GameState) this.handler).mineralsAssigned.get(closestMineral) + 1);
                        ((GameState) this.handler).workerIdle.remove(chosen);
                        ((GameState) this.handler).workerMining.put(chosen, closestMineral);

                        ((GameState) this.handler).chosenWorker = null;
                        ((GameState) this.handler).mining++;
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

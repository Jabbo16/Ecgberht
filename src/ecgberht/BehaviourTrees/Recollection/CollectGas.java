package ecgberht.BehaviourTrees.Recollection;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.GasMiningFacility;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Map.Entry;

public class CollectGas extends Action {

    public CollectGas(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.getPlayer().gas() >= 400) {
                return BehavioralTree.State.FAILURE;
            }
            Worker chosen = this.handler.chosenWorker;
            if (!this.handler.refineriesAssigned.isEmpty()) {
                GasMiningFacility closestGeyser = null;
                int workerGas = this.handler.strat.workerGas == 0 ? 3 : this.handler.strat.workerGas;
                for (Entry<GasMiningFacility, Integer> g : this.handler.refineriesAssigned.entrySet()) {
                    if ((closestGeyser == null || chosen.getDistance(g.getKey()) < chosen.getDistance(closestGeyser)) && g.getValue() < workerGas && this.handler.mining > 3) {
                        closestGeyser = g.getKey();
                    }
                }
                if (closestGeyser != null) {
                    if (chosen.gather(closestGeyser, false)) {
                        Integer aux = this.handler.refineriesAssigned.get(closestGeyser);
                        aux++;
                        this.handler.refineriesAssigned.put(closestGeyser, aux);
                        this.handler.workerIdle.remove(chosen);
                        this.handler.workerGas.put(chosen, closestGeyser);
                        this.handler.chosenWorker = null;
                        return BehavioralTree.State.SUCCESS;
                    }
                }
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}

package ecgberht.BehaviourTrees.Recollection;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.GasMiningFacility;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Map.Entry;

public class CollectGas extends Action {

    public CollectGas(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).getPlayer().gas() >= 400) {
                return State.FAILURE;
            }
            Worker chosen = ((GameState) this.handler).chosenWorker;
            if (!((GameState) this.handler).refineriesAssigned.isEmpty()) {
                GasMiningFacility closestGeyser = null;
                int workerGas = ((GameState) this.handler).strat.workerGas == 0 ? 3 : ((GameState) this.handler).strat.workerGas;
                for (Entry<GasMiningFacility, Integer> g : ((GameState) this.handler).refineriesAssigned.entrySet()) {
                    if ((closestGeyser == null || chosen.getDistance(g.getKey()) < chosen.getDistance(closestGeyser)) && g.getValue() < workerGas && ((GameState) this.handler).mining > 3) {
                        closestGeyser = g.getKey();
                    }
                }
                if (closestGeyser != null) {
                    if (chosen.gather(closestGeyser, false)) {
                        Integer aux = ((GameState) this.handler).refineriesAssigned.get(closestGeyser);
                        aux++;
                        ((GameState) this.handler).refineriesAssigned.put(closestGeyser, aux);
                        ((GameState) this.handler).workerIdle.remove(chosen);
                        ((GameState) this.handler).workerGas.put(chosen, closestGeyser);
                        ((GameState) this.handler).chosenWorker = null;
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

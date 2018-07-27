package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.unit.Mineral;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;


public class CheckBlockingMinerals extends Conditional {

    public CheckBlockingMinerals(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker scv = ((GameState) this.handler).chosenWorkerDrop;
            MineralPatch chosen = null;
            if (((GameState) this.handler).chosenIsland == null) return State.FAILURE;
            for (Mineral p : ((GameState) this.handler).chosenIsland.getBlockingMinerals()) {
                if (!p.getUnit().exists()) continue;
                chosen = (MineralPatch) p.getUnit();
                break;
            }
            if (chosen != null) {
                if (scv.getOrderTarget() != null && scv.getOrderTarget().equals(chosen)) return State.FAILURE;
                scv.gather(chosen);
                return State.FAILURE;
            }
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}

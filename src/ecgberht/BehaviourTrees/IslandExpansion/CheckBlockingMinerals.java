package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.unit.Mineral;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;


public class CheckBlockingMinerals extends Conditional {

    public CheckBlockingMinerals(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            Worker scv = this.handler.chosenWorkerDrop;
            MineralPatch chosen = null;
            if (this.handler.chosenIsland == null) return BehavioralTree.State.FAILURE;
            for (Mineral p : this.handler.chosenIsland.getBlockingMinerals()) {
                if (!p.getUnit().exists()) continue;
                chosen = (MineralPatch) p.getUnit();
                break;
            }
            if (chosen != null) {
                if (scv.getOrderTarget() != null && scv.getOrderTarget().equals(chosen)) return BehavioralTree.State.FAILURE;
                scv.gather(chosen);
                return BehavioralTree.State.FAILURE;
            }
            return BehavioralTree.State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}

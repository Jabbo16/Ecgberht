package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.Mineral;
import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;


public class CheckBlockingMinerals extends BrainAction {

    public CheckBlockingMinerals(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            Worker scv = gameState.chosenWorkerDrop;
            MineralPatch chosen = null;
            if (gameState.chosenIsland == null) return BrainStatus.FAILURE;
            for (Mineral p : gameState.chosenIsland.getBlockingMinerals()) {
                if (!p.getUnit().exists()) continue;
                chosen = (MineralPatch) p.getUnit();
                break;
            }
            if (chosen != null) {
                if (scv.getOrderTarget() != null && scv.getOrderTarget().equals(chosen)) return BrainStatus.FAILURE;
                scv.gather(chosen);
                return BrainStatus.FAILURE;
            }
            return BrainStatus.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}







package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseVultureMines extends Action {

    public ChooseVultureMines(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.UBs.isEmpty()) return State.FAILURE;
            for (ResearchingFacility u : this.handler.UBs) {
                if (!(u instanceof MachineShop)) continue;
                if (!this.handler.getPlayer().hasResearched(TechType.Spider_Mines) && u.canResearch(TechType.Spider_Mines) && !u.isResearching() && !u.isUpgrading()) {
                    this.handler.chosenUnitUpgrader = u;
                    this.handler.chosenResearch = TechType.Spider_Mines;
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

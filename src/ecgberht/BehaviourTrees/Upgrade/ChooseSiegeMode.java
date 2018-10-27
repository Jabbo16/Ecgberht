package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseSiegeMode extends Action {

    public ChooseSiegeMode(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.UBs.isEmpty()) {
                return State.FAILURE;
            }
            for (ResearchingFacility u : this.handler.UBs) {
                if (!(u instanceof MachineShop)) continue;
                if (!this.handler.getPlayer().hasResearched(TechType.Tank_Siege_Mode) && u.canResearch(TechType.Tank_Siege_Mode) && !u.isResearching() && !u.isUpgrading()) {
                    this.handler.chosenUnitUpgrader = u;
                    this.handler.chosenResearch = TechType.Tank_Siege_Mode;
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

package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseVultureSpeed extends Action {

    public ChooseVultureSpeed(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.UBs.isEmpty()) return State.FAILURE;
            for (ResearchingFacility u : this.handler.UBs) {
                if (!(u instanceof MachineShop)) continue;
                if (u.canUpgrade(UpgradeType.Ion_Thrusters) && !u.isResearching() && !u.isUpgrading() && this.handler.getPlayer().getUpgradeLevel(UpgradeType.Ion_Thrusters) < 1) {
                    this.handler.chosenUnitUpgrader = u;
                    this.handler.chosenUpgrade = UpgradeType.Ion_Thrusters;
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

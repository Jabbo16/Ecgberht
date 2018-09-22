package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseCharonBoosters extends Action {

    public ChooseCharonBoosters(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).UBs.isEmpty() || !((GameState)this.handler).strat.trainUnits.contains(UnitType.Terran_Goliath) || ((GameState)this.handler).maxGoliaths == 0) {
                return State.FAILURE;
            }
            for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                if (!(u instanceof MachineShop)) continue;
                if (((GameState) this.handler).getPlayer().getUpgradeLevel(UpgradeType.Charon_Boosters) < 1 && u.canUpgrade(UpgradeType.Charon_Boosters) && !u.isResearching() && !u.isUpgrading()) {
                    ((GameState) this.handler).chosenUnitUpgrader = u;
                    ((GameState) this.handler).chosenUpgrade = UpgradeType.Charon_Boosters;
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

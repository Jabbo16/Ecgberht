package ecgberht.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.Armory;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseArmorMechUp extends Action {

    public ChooseArmorMechUp(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).UBs.isEmpty()) {
                return State.FAILURE;
            }
            for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                if (!(u instanceof Armory)) continue;
                if (u.canUpgrade(UpgradeType.Terran_Vehicle_Plating) && !u.isResearching() && !u.isUpgrading() && ((GameState) this.handler).getPlayer().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) < 3) {
                    ((GameState) this.handler).chosenUnitUpgrader = u;
                    ((GameState) this.handler).chosenUpgrade = UpgradeType.Terran_Vehicle_Plating;
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

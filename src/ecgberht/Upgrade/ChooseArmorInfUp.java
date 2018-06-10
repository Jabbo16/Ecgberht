package ecgberht.Upgrade;

import bwapi.Unit;
import bwapi.UpgradeType;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseArmorInfUp extends Action {

    public ChooseArmorInfUp(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).UBs.isEmpty()) {
                return State.FAILURE;
            }
            for (Unit u : ((GameState) this.handler).UBs) {
                if (u.canUpgrade(UpgradeType.Terran_Infantry_Armor) && !u.isResearching() && !u.isUpgrading()) {
                    ((GameState) this.handler).chosenUnitUpgrader = u;
                    ((GameState) this.handler).chosenUpgrade = UpgradeType.Terran_Infantry_Armor;
                    return State.SUCCESS;
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}

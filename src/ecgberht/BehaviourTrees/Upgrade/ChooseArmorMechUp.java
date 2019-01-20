package ecgberht.BehaviourTrees.Upgrade;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseArmorMechUp extends Action {

    public ChooseArmorMechUp(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.isEmpty()) return State.FAILURE;
            for (Unit u : gameState.UBs) {
                if (u.getType() != UnitType.Terran_Armory) continue;
                if (u.canUpgrade(UpgradeType.Terran_Vehicle_Plating) && !u.isResearching() && !u.isUpgrading() && gameState.getPlayer().getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) < 3) {
                    gameState.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.Terran_Vehicle_Plating;
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

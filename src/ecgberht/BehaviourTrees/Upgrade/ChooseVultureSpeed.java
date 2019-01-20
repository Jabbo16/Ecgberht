package ecgberht.BehaviourTrees.Upgrade;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseVultureSpeed extends Action {

    public ChooseVultureSpeed(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.isEmpty()) return State.FAILURE;
            for (Unit u : gameState.UBs) {
                if (u.getType() != UnitType.Terran_Machine_Shop) continue;
                if (u.canUpgrade(UpgradeType.Ion_Thrusters) && !u.isResearching() && !u.isUpgrading() && gameState.getPlayer().getUpgradeLevel(UpgradeType.Ion_Thrusters) < 1) {
                    gameState.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.Ion_Thrusters;
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

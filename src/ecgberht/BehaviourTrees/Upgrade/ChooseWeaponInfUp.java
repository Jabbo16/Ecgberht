package ecgberht.BehaviourTrees.Upgrade;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseWeaponInfUp extends Action {

    public ChooseWeaponInfUp(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.isEmpty()) return State.FAILURE;
            for (Unit u : gameState.UBs) {
                if (u.getType() != UnitType.Terran_Engineering_Bay) continue;
                if (u.canUpgrade(UpgradeType.Terran_Infantry_Weapons) && !u.isResearching() && !u.isUpgrading() && gameState.getPlayer().getUpgradeLevel(UpgradeType.Terran_Infantry_Weapons) < 3) {
                    gameState.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.Terran_Infantry_Weapons;
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

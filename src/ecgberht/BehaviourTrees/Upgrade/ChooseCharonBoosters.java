package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseCharonBoosters extends Action {

    public ChooseCharonBoosters(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.isEmpty() || !gameState.getStrat().trainUnits.contains(UnitType.Terran_Goliath) || gameState.maxGoliaths == 0) {
                return State.FAILURE;
            }
            for (ResearchingFacility u : gameState.UBs) {
                if (!(u instanceof MachineShop)) continue;
                if (gameState.getPlayer().getUpgradeLevel(UpgradeType.Charon_Boosters) < 1 && u.canUpgrade(UpgradeType.Charon_Boosters) && !u.isResearching() && !u.isUpgrading()) {
                    gameState.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.Charon_Boosters;
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

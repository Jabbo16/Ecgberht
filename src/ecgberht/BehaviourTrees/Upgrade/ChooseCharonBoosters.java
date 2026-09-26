package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseCharonBoosters extends BrainAction {

    public ChooseCharonBoosters(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.tech.UBs.isEmpty() || !gameState.getStrat().trainUnits.contains(UnitType.Terran_Goliath) || gameState.maxGoliaths == 0) {
                return BrainStatus.FAILURE;
            }
            for (ResearchingFacility u : gameState.tech.UBs) {
                if (!(u instanceof MachineShop)) continue;
                if (gameState.getPlayer().getUpgradeLevel(UpgradeType.Charon_Boosters) < 1 && u.canUpgrade(UpgradeType.Charon_Boosters) && !u.isResearching() && !u.isUpgrading()) {
                    gameState.tech.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.Charon_Boosters;
                    return BrainStatus.SUCCESS;
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}







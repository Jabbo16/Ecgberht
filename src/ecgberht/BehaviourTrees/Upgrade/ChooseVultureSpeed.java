package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseVultureSpeed extends BrainAction {

    public ChooseVultureSpeed(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.tech.UBs.isEmpty()) return BrainStatus.FAILURE;
            for (ResearchingFacility u : gameState.tech.UBs) {
                if (!(u instanceof MachineShop)) continue;
                if (u.canUpgrade(UpgradeType.Ion_Thrusters) && !u.isResearching() && !u.isUpgrading() && gameState.getPlayer().getUpgradeLevel(UpgradeType.Ion_Thrusters) < 1) {
                    gameState.tech.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.Ion_Thrusters;
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







package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.Armory;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseWeaponMechUp extends BrainAction {

    public ChooseWeaponMechUp(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.tech.UBs.isEmpty()) return BrainStatus.FAILURE;
            for (ResearchingFacility u : gameState.tech.UBs) {
                if (!(u instanceof Armory)) continue;
                if (u.canUpgrade(UpgradeType.Terran_Vehicle_Weapons) && !u.isResearching() && !u.isUpgrading() && gameState.getPlayer().getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) < 3) {
                    gameState.tech.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.Terran_Vehicle_Weapons;
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







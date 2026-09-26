package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.EngineeringBay;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseArmorInfUp extends BrainAction {

    public ChooseArmorInfUp(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.tech.UBs.isEmpty()) return BrainStatus.FAILURE;
            for (ResearchingFacility u : gameState.tech.UBs) {
                if (!(u instanceof EngineeringBay)) continue;
                if (u.canUpgrade(UpgradeType.Terran_Infantry_Armor) && !u.isResearching() && !u.isUpgrading() && gameState.getPlayer().getUpgradeLevel(UpgradeType.Terran_Infantry_Armor) < 3) {
                    gameState.tech.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.Terran_Infantry_Armor;
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







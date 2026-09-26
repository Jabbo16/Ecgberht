package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseMarineRange extends BrainAction {

    public ChooseMarineRange(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.tech.UBs.isEmpty()) return BrainStatus.FAILURE;
            String strat = gameState.getStrat().name;
            if (strat.equals("BioMech") || strat.equals("BioMechGreedyFE") || strat.equals("BioMechFE")) {
                Player self = gameState.getPlayer();
                if (!self.isResearching(TechType.Tank_Siege_Mode) && !self.hasResearched(TechType.Tank_Siege_Mode)) {
                    return BrainStatus.FAILURE;
                }
            }
            for (ResearchingFacility u : gameState.tech.UBs) {
                if (!(u instanceof Academy)) continue;
                if (gameState.getPlayer().hasResearched(TechType.Stim_Packs) && gameState.getPlayer().getUpgradeLevel(UpgradeType.U_238_Shells) < 1 && u.canUpgrade(UpgradeType.U_238_Shells) && !u.isResearching() && !u.isUpgrading()) {
                    gameState.tech.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.U_238_Shells;
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







package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseSiegeMode extends BrainAction {

    public ChooseSiegeMode(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.UBs.isEmpty()) return BrainStatus.FAILURE;
            for (ResearchingFacility u : gameState.UBs) {
                if (!(u instanceof MachineShop)) continue;
                if (!gameState.getPlayer().hasResearched(TechType.Tank_Siege_Mode) && u.canResearch(TechType.Tank_Siege_Mode) && !u.isResearching() && !u.isUpgrading()) {
                    gameState.chosenUnitUpgrader = u;
                    gameState.chosenResearch = TechType.Tank_Siege_Mode;
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


package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseVultureMines extends BrainAction {

    public ChooseVultureMines(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.UBs.isEmpty()) return BrainStatus.FAILURE;
            for (ResearchingFacility u : gameState.UBs) {
                if (!(u instanceof MachineShop)) continue;
                if (!gameState.getPlayer().hasResearched(TechType.Spider_Mines) && u.canResearch(TechType.Spider_Mines) && !u.isResearching() && !u.isUpgrading()) {
                    gameState.chosenUnitUpgrader = u;
                    gameState.chosenResearch = TechType.Spider_Mines;
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


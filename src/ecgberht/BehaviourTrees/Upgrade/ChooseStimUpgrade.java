package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseStimUpgrade extends BrainAction {

    public ChooseStimUpgrade(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.tech.UBs.isEmpty()) return BrainStatus.FAILURE;
            for (ResearchingFacility u : gameState.tech.UBs) {
                if (!(u instanceof Academy)) continue;
                if (!gameState.getPlayer().hasResearched(TechType.Stim_Packs) && u.canResearch(TechType.Stim_Packs) && !u.isResearching() && !u.isUpgrading()) {
                    gameState.tech.chosenUnitUpgrader = u;
                    gameState.chosenResearch = TechType.Stim_Packs;
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







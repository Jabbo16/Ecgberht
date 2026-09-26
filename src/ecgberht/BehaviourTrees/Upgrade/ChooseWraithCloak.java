package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.ControlTower;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseWraithCloak extends BrainAction {

    public ChooseWraithCloak(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.tech.UBs.isEmpty()) return BrainStatus.FAILURE;
            for (ResearchingFacility u : gameState.tech.UBs) {
                if (!(u instanceof ControlTower)) continue;
                if (!gameState.getPlayer().hasResearched(TechType.Cloaking_Field) && u.canResearch(TechType.Cloaking_Field) && !u.isResearching() && !u.isUpgrading()) {
                    gameState.tech.chosenUnitUpgrader = u;
                    gameState.chosenResearch = TechType.Cloaking_Field;
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







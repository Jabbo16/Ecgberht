package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.ResearchingFacility;
import org.openbw.bwapi4j.unit.ScienceFacility;

public class ChooseEMP extends BrainAction {

    public ChooseEMP(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.enemyRace != Race.Protoss) return BrainStatus.FAILURE;
            boolean found = false;
            ScienceFacility chosen = null;
            for (ResearchingFacility r : gameState.tech.UBs) {
                if (r instanceof ScienceFacility && !r.isResearching()) {
                    found = true;
                    chosen = (ScienceFacility) r;
                    break;
                }
            }
            if (!found) return BrainStatus.FAILURE;
            if (!gameState.getPlayer().isResearching(TechType.EMP_Shockwave) &&
                    !gameState.getPlayer().hasResearched(TechType.EMP_Shockwave)) {
                gameState.tech.chosenUnitUpgrader = chosen;
                gameState.chosenResearch = TechType.EMP_Shockwave;
                return BrainStatus.SUCCESS;
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}







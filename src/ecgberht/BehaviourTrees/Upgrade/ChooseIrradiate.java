package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.ResearchingFacility;
import org.openbw.bwapi4j.unit.ScienceFacility;

public class ChooseIrradiate extends Action {

    public ChooseIrradiate(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.enemyRace != Race.Zerg) return State.FAILURE;
            boolean found = false;
            ScienceFacility chosen = null;
            for (ResearchingFacility r : gameState.UBs) {
                if (r instanceof ScienceFacility && !r.isResearching()) {
                    found = true;
                    chosen = (ScienceFacility) r;
                    break;
                }
            }
            if (!found) return State.FAILURE;
            if (!gameState.getPlayer().isResearching(TechType.Irradiate) &&
                    !gameState.getPlayer().hasResearched(TechType.Irradiate)) {
                gameState.chosenUnitUpgrader = chosen;
                gameState.chosenResearch = TechType.Irradiate;
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}

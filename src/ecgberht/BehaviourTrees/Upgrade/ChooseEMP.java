package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.ResearchingFacility;
import org.openbw.bwapi4j.unit.ScienceFacility;

public class ChooseEMP extends Action {

    public ChooseEMP(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.enemyRace != Race.Protoss) return BehavioralTree.State.FAILURE;
            boolean found = false;
            ScienceFacility chosen = null;
            for (ResearchingFacility r : this.handler.UBs) {
                if (r instanceof ScienceFacility && !r.isResearching()) {
                    found = true;
                    chosen = (ScienceFacility) r;
                    break;
                }
            }
            if (!found) return BehavioralTree.State.FAILURE;
            if (!this.handler.getPlayer().isResearching(TechType.EMP_Shockwave) &&
                    !this.handler.getPlayer().hasResearched(TechType.EMP_Shockwave)) {
                this.handler.chosenUnitUpgrader = chosen;
                this.handler.chosenResearch = TechType.EMP_Shockwave;
                return BehavioralTree.State.SUCCESS;
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}

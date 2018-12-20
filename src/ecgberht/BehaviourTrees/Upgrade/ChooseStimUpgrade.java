package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseStimUpgrade extends Action {

    public ChooseStimUpgrade(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.UBs.isEmpty()) return State.FAILURE;
            for (ResearchingFacility u : this.handler.UBs) {
                if (!(u instanceof Academy)) continue;
                if (!this.handler.getPlayer().hasResearched(TechType.Stim_Packs) && u.canResearch(TechType.Stim_Packs) && !u.isResearching() && !u.isUpgrading()) {
                    this.handler.chosenUnitUpgrader = u;
                    this.handler.chosenResearch = TechType.Stim_Packs;
                    return State.SUCCESS;
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}

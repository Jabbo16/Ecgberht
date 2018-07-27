package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseStimUpgrade extends Action {

    public ChooseStimUpgrade(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).UBs.isEmpty()) {
                return State.FAILURE;
            }
            for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                if (!(u instanceof Academy)) continue;
                if (!((GameState) this.handler).getPlayer().hasResearched(TechType.Stim_Packs) && u.canResearch(TechType.Stim_Packs) && !u.isResearching() && !u.isUpgrading()) {
                    ((GameState) this.handler).chosenUnitUpgrader = u;
                    ((GameState) this.handler).chosenResearch = TechType.Stim_Packs;
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

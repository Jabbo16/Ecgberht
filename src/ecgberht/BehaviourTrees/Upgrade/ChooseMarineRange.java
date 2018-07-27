package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseMarineRange extends Action {

    public ChooseMarineRange(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).UBs.isEmpty()) return State.FAILURE;
            String strat = ((GameState) this.handler).strat.name;
            if (strat.equals("BioMech") || strat.equals("BioMechGreedyFE") || strat.equals("BioMechFE")) {
                Player self = ((GameState) this.handler).getPlayer();
                if (!self.isResearching(TechType.Tank_Siege_Mode) && !self.hasResearched(TechType.Tank_Siege_Mode)) {
                    return State.FAILURE;
                }
            }
            for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                if (!(u instanceof Academy)) continue;
                if (((GameState) this.handler).getPlayer().hasResearched(TechType.Stim_Packs) && ((GameState) this.handler).getPlayer().getUpgradeLevel(UpgradeType.U_238_Shells) < 1 && u.canUpgrade(UpgradeType.U_238_Shells) && !u.isResearching() && !u.isUpgrading()) {
                    ((GameState) this.handler).chosenUnitUpgrader = u;
                    ((GameState) this.handler).chosenUpgrade = UpgradeType.U_238_Shells;
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

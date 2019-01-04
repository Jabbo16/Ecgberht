package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseMarineRange extends Action {

    public ChooseMarineRange(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.isEmpty()) return State.FAILURE;
            String strat = gameState.strat.name;
            if (strat.equals("BioMech") || strat.equals("BioMechGreedyFE") || strat.equals("BioMechFE")) {
                Player self = gameState.getPlayer();
                if (!self.isResearching(TechType.Tank_Siege_Mode) && !self.hasResearched(TechType.Tank_Siege_Mode)) {
                    return State.FAILURE;
                }
            }
            for (ResearchingFacility u : gameState.UBs) {
                if (!(u instanceof Academy)) continue;
                if (gameState.getPlayer().hasResearched(TechType.Stim_Packs) && gameState.getPlayer().getUpgradeLevel(UpgradeType.U_238_Shells) < 1 && u.canUpgrade(UpgradeType.U_238_Shells) && !u.isResearching() && !u.isUpgrading()) {
                    gameState.chosenUnitUpgrader = u;
                    gameState.chosenUpgrade = UpgradeType.U_238_Shells;
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

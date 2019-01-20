package ecgberht.BehaviourTrees.Upgrade;

import bwapi.*;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseMarineRange extends Action {

    public ChooseMarineRange(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.isEmpty()) return State.FAILURE;
            String strat = gameState.getStrat().name;
            if (strat.equals("BioMech") || strat.equals("BioMechGreedyFE") || strat.equals("BioMechFE")) {
                Player self = gameState.getPlayer();
                if (!self.isResearching(TechType.Tank_Siege_Mode) && !self.hasResearched(TechType.Tank_Siege_Mode)) {
                    return State.FAILURE;
                }
            }
            for (Unit u : gameState.UBs) {
                if (u.getType() != UnitType.Terran_Academy) continue;
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

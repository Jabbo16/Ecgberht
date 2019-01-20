package ecgberht.BehaviourTrees.Upgrade;

import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseVultureMines extends Action {

    public ChooseVultureMines(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.isEmpty()) return State.FAILURE;
            for (Unit u : gameState.UBs) {
                if (u.getType() != UnitType.Terran_Machine_Shop) continue;
                if (!gameState.getPlayer().hasResearched(TechType.Spider_Mines) && u.canResearch(TechType.Spider_Mines) && !u.isResearching() && !u.isUpgrading()) {
                    gameState.chosenUnitUpgrader = u;
                    gameState.chosenResearch = TechType.Spider_Mines;
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

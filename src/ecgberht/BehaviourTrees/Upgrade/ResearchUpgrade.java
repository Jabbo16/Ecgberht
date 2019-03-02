package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;

public class ResearchUpgrade extends Action {

    public ResearchUpgrade(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!gameState.defense && gameState.chosenToBuild == UnitType.Terran_Command_Center) {
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Command_Center) {
                        found = true;
                        break;
                    }
                }
                if (!found) return State.FAILURE;
            }
            if (gameState.chosenUpgrade != null) {
                if (gameState.chosenUnitUpgrader.upgrade(gameState.chosenUpgrade)) {
                    gameState.chosenUpgrade = null;
                    return State.SUCCESS;
                }
            } else if (gameState.chosenResearch != null) {
                if (gameState.chosenUnitUpgrader.research(gameState.chosenResearch)) {
                    gameState.chosenResearch = null;
                    return State.SUCCESS;
                }
            }
            gameState.chosenUnitUpgrader = null;
            gameState.chosenUpgrade = null;
            gameState.chosenResearch = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}

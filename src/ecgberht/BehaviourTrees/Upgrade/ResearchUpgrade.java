package ecgberht.BehaviourTrees.Upgrade;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;

public class ResearchUpgrade extends BrainAction {

    public ResearchUpgrade(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (!gameState.mil.defense && gameState.chosenToBuild == UnitType.Terran_Command_Center) {
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : gameState.eco.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Command_Center) {
                        found = true;
                        break;
                    }
                }
                if (!found) return BrainStatus.FAILURE;
            }
            if (gameState.chosenUpgrade != null) {
                if (gameState.tech.chosenUnitUpgrader.upgrade(gameState.chosenUpgrade)) {
                    gameState.chosenUpgrade = null;
                    return BrainStatus.SUCCESS;
                }
            } else if (gameState.chosenResearch != null) {
                if (gameState.tech.chosenUnitUpgrader.research(gameState.chosenResearch)) {
                    gameState.chosenResearch = null;
                    return BrainStatus.SUCCESS;
                }
            }
            gameState.tech.chosenUnitUpgrader = null;
            gameState.chosenUpgrade = null;
            gameState.chosenResearch = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}







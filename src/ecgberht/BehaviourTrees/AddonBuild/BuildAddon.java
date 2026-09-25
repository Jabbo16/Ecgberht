package ecgberht.BehaviourTrees.AddonBuild;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;

public class BuildAddon extends BrainAction {

    public BuildAddon(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (!gameState.defense && gameState.chosenToBuild == UnitType.Terran_Command_Center) {
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Command_Center) {
                        found = true;
                        break;
                    }
                }
                if (!found) return BrainStatus.FAILURE;
            }
            if (gameState.chosenBuildingAddon.getAddon() == null && gameState.chosenBuildingAddon.build(gameState.chosenAddon)) {
                gameState.chosenBuildingAddon = null;
                gameState.chosenAddon = null;
                return BrainStatus.SUCCESS;
            }
            gameState.chosenBuildingAddon = null;
            gameState.chosenAddon = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

